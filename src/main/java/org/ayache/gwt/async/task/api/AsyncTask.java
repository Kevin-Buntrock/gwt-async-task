/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
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
                if (!s.getData().type.matches("[a-z]+\\.([a-z]+\\.)*([a-zA-Z][A-Za-z\\d]*\\$?)+")) {
                    throw new IllegalArgumentException(s.getData().type + " is not a valid class name. Implementation of AsyncTask can't be anonymous inner class.");
                }
                Object eval = evalQuietly(s.getData().type.replace('$', '.'));
                if (eval == null) {
                    throw new IllegalArgumentException(s.getData().type + " must be annotated with @JsType");
                }
                try {
                    AsyncTask construct = (AsyncTask) getReflect().construct(eval, new Object[0]);
                    Object doInBackground;
                    doInBackground = construct.doInBackground(s.getData().params);
                    ResultHolder build = ResultHolder.build(doInBackground);
                    Object[] transferList = build.getTransferList();
                    if (transferList != null) {
                        postMessage(build, transferList);
                    }
                } catch (Exception ex) {
                    postMessage(ResultHolder.build(ex));
                    error(ex);
                }
            }
        });
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ParamsHolder<Param> {

        private String type;
        private Param params;

        @JsOverlay
        private final Object[] getTransferList() {
            if (arrayBufferType().equals(type(params))) {
                ArrayBuffer[] transferList = new ArrayBuffer[1];
                transferList[0] = (ArrayBuffer) params;
                return transferList;
            } else if (isArray(params) && arrayBufferType().equals(type(getReflect().get(params, "0")))) {
                Object result = params;
                params = (Param) JavaScriptObject.createObject();
                Number get = getReflect().get(result, "length");
                for (int i = 0; i < get.intValue(); i++) {
                    getReflect().set(params, String.valueOf(i), getReflect().get(result, String.valueOf(i)));
                }
                return (Object[]) result;
            }
            return null;
        }

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
        private final Object[] getTransferList() {
            if (arrayBufferType().equals(type(result))) {
                ArrayBuffer[] transferList = new ArrayBuffer[1];
                transferList[0] = (ArrayBuffer) result;
                return transferList;
            } else if (isArray(result) && arrayBufferType().equals(type(getReflect().get(result, "0")))) {
                Object res = result;
                result = (Result) JavaScriptObject.createObject();
                Number get = getReflect().get(res, "length");
                for (int i = 0; i < get.intValue(); i++) {
                    getReflect().set(result, String.valueOf(i), getReflect().get(res, String.valueOf(i)));
                }
                return (Object[]) res;
            }
            return null;
        }

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
        if (isMainThread()) {
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
        ParamsHolder paramsHolder = ParamsHolder.build(getClass().getName(), p);
        Object[] transferList = paramsHolder.getTransferList();
        if (transferList != null) {
            worker.postMessage(paramsHolder, transferList);
        } else {
            worker.postMessage(paramsHolder);
        }
    }

    protected abstract Result doInBackground(Params p) throws Exception;

    protected abstract void done(Result r);

    protected void onError(String type, String message) {
    }

    @JsProperty(namespace = JsPackage.GLOBAL, name = "onmessage")
    private static native void setOnMessage(MessageListener listener);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native <Transferable> void postMessage(ResultHolder p);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native <Transferable> void postMessage(ResultHolder p, Transferable[] transferList);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "eval")
    private static native <T> T eval(String s) throws Exception;

    @JsProperty(namespace = JsPackage.GLOBAL, name = "Reflect")
    private static native <T> Reflect<T> getReflect();

    @JsProperty(namespace = JsPackage.GLOBAL, name = "window")
    private static native <Func> Func window();

    @JsMethod(namespace = "console", name = "error")
    protected static native void error(Object o);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "Object.prototype.toString.call")
    private static native String type(Object o);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "Array.isArray")
    private static native boolean isArray(Object o);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "ArrayBuffer.prototype.toString")
    private static native String arrayBufferType();

    private boolean isMainThread() {
        return window() != null;
    }

    private static Object evalQuietly(String s) {
        try {
            return eval(s);
        } catch (Exception ex) {
            return null;
        }

    }

}
