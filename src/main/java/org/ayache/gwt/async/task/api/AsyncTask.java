/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 * @param <Params>
 * @param <Result>
 */
@JsType
public abstract class AsyncTask<Params, Result> {

    static {
        setOnMessage(new MessageListener<ParamsHolder>() {
            @Override
            public void on(MessageEvent<ParamsHolder> s) {
                if (!s.getData().type.matches("([a-z]+\\.([a-z]+\\.)*[A-Za-z0-9]+)")) {
                    throw new IllegalArgumentException(s.getData().type + " is not a valid class name");
                }
                AsyncTask construct = (AsyncTask) getReflect().construct(eval(s.getData().type), new Object[0]);
                Object doInBackground;
                try {
                    doInBackground = construct.doInBackground(s.getData().params);
                    postMessage(ResultHolder.build(doInBackground));
                } catch (Exception ex) {
                    postMessage(ResultHolder.build(ex));
                    
                }
            }
        });
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ParamsHolder {

        private String type;
        private Object params;

        @JsOverlay
        static ParamsHolder build(String type, Object params) {
            ParamsHolder paramsHolder = new ParamsHolder();
            paramsHolder.type = type;
            paramsHolder.params = params;
            return paramsHolder;
        }

    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ResultHolder<Result> {

        private Result result;
        private String exceptionType;
        private String exceptionMessage;

        @JsOverlay
        static <Result> ResultHolder<Result> build(Result result) {
            ResultHolder<Result> resultHolder = new ResultHolder();
            resultHolder.result = result;
            return resultHolder;
        }

        @JsOverlay
        static ResultHolder build(Exception result) {
            ResultHolder resultHolder = new ResultHolder();
            resultHolder.exceptionType = result.getClass().getName();
            resultHolder.exceptionMessage = result.getMessage();
            return resultHolder;
        }

        @JsOverlay
        private boolean failed() {
            return exceptionType != null;
        }

    }

    private Worker<ParamsHolder> worker;

    public AsyncTask() {
        if (isWorkerSupported()) {
            worker = new Worker(GWT.getModuleBaseURL() + GWT.getModuleName() + ".worker.js");
            worker.setOnMessage(new MessageListener<ResultHolder<Result>>() {
                public void on(MessageEvent<ResultHolder<Result>> s) {
                    if (s.getData().failed()) {
                        onError(s.getData().exceptionType, s.getData().exceptionMessage);
                    } else {
                        done(s.getData().result);
                    }
                }
            });
        }
    }

    public void execute(Params p) {
        worker.postMessage(ParamsHolder.build(getClass().getName(), p));
    }

    protected abstract Result doInBackground(Params p) throws Exception;

    protected abstract void done(Result r);

    protected void onError(String type, String message) {
    }

    @JsProperty(namespace = JsPackage.GLOBAL, name = "onmessage")
    private static native void setOnMessage(MessageListener listener);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native void postMessage(ResultHolder p);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "eval")
    private static native <T> T eval(String s);

    @JsProperty(namespace = JsPackage.GLOBAL, name = "Reflect")
    private static native <T> Reflect<T> getReflect();

    @JsProperty(namespace = JsPackage.GLOBAL, name = "Worker")
    private static native <Func> Func worker();

    private boolean isWorkerSupported() {
        return worker() != null;
    }
    
}
