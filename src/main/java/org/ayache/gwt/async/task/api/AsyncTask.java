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
                AsyncTask construct = getReflect().construct(eval(s.getData().type), new Object[0]);
                Object doInBackground = construct.doInBackground(s.getData().params);
                postMessage(ResultHolder.build(doInBackground));
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

        @JsOverlay
        static <Result> ResultHolder<Result> build(Result result) {
            ResultHolder resultHolder = new ResultHolder();
            resultHolder.result = result;
            return resultHolder;
        }

    }

    private Worker<ParamsHolder> worker;

    public AsyncTask() {
        try {
            worker = new Worker(GWT.getModuleBaseURL() + GWT.getModuleName() + ".worker.js");
            worker.setOnMessage(new MessageListener<ResultHolder<Result>>() {
                public void on(MessageEvent<ResultHolder<Result>> s) {
                    done(s.getData().result);
                }
            });
        } catch (Throwable t) {

        }
    }

    public void execute(Params p) {
        worker.postMessage(ParamsHolder.build(getClass().getName(), p));
    }

    protected abstract Result doInBackground(Params p);
    
    protected abstract void done(Result r);

    @JsProperty(namespace = JsPackage.GLOBAL, name = "onmessage")
    public static native void setOnMessage(MessageListener listener);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    static native void postMessage(ResultHolder p);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "eval")
    public static native <T> T eval(String s);

    @JsProperty(namespace = JsPackage.GLOBAL, name = "Reflect")
    public static native Reflect<AsyncTask> getReflect();
}
