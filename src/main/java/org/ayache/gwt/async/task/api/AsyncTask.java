/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@SuppressWarnings("UseSpecificCatch")
public abstract class AsyncTask<Params, Result> {

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ParamsHolder {

        private String type;
        private Object params;
        private String dataType;

        @JsOverlay
        static ParamsHolder build(String type, Object params) {
            ParamsHolder paramsHolder = new ParamsHolder();
            paramsHolder.type = type;
            if (params instanceof String) {
                paramsHolder.params = params;
            } else if (params instanceof Number) {
                paramsHolder.dataType = "Number";
                paramsHolder.params = JavaScriptObject.createObject();
                JSONObject jSONObject = new JSONObject((JavaScriptObject) paramsHolder.params);
                jSONObject.put(paramsHolder.dataType, new JSONNumber(((Number) params).doubleValue()));
            } else if (params instanceof JavaScriptObject) {
                paramsHolder.params = (JavaScriptObject) params;
            } else {
                Logger.getLogger("").log(Level.SEVERE, "Params type of AsyncTask<Params, Result> must be JavascriptObject or @JsType annotated class");
            }
            return paramsHolder;
        }

    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ResultHolder<Result> {

        private Result result;
        private String dataType;
        private String exceptionType;
        private String exceptionMessage;

        @JsOverlay
        static <Result> ResultHolder<Result> build(Result result) {
            if (result instanceof String || result instanceof Double) {
                ResultHolder<Result> resultHolder = new ResultHolder<>();
                resultHolder.result = result;
                return resultHolder;
            } else if (result instanceof Number) {
                ResultHolder<Number> resultHolder = new ResultHolder<>();
                resultHolder.dataType = result.getClass().getName();
                resultHolder.result = Double.valueOf(String.valueOf(result));
                return (ResultHolder<Result>) resultHolder;
            } else if (result instanceof JavaScriptObject) {
                ResultHolder<JavaScriptObject> resultHolder = new ResultHolder<>();
                resultHolder.result = (JavaScriptObject) result;
                return (ResultHolder<Result>) resultHolder;
            } else {
                Logger.getLogger("").log(Level.SEVERE, "Result type of AsyncTask<Params, Result> must be JavascriptObject or @JsType annotated class");
            }
            return new ResultHolder<>();
        }

        @JsOverlay
        static ResultHolder build(Throwable result) {
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

    static {
        setOnMessage((MessageListener<ParamsHolder>) (MessageEvent<ParamsHolder> s) -> {
            if (!s.getData().type.matches("[a-z]+\\.([a-z]+\\.)*([a-zA-Z][A-Za-z\\d]*\\$?)+")) {
                throw new IllegalArgumentException(s.getData().type + " is not a valid class name. Implementation of AsyncTask can't be anonymous inner class.");
            }
            AsyncTask construct = evalQuietly("new " + s.getData().type.replace('$', '.'));
            if (construct == null) {
                throw new IllegalArgumentException(s.getData().type + " must be annotated with @JsType");
            }
            try {
                Object doInBackground;
                if ("Number".equals(s.getData().dataType)) {
                    doInBackground = construct.doInBackground(new JSONObject((JavaScriptObject) s.getData().params).get("Number").isNumber().doubleValue());
                } else {
                    doInBackground = construct.doInBackground(s.getData().params);
                }
                ResultHolder build = ResultHolder.build(doInBackground);
                Object[] transferList = getTransferList(build.result);
                if (transferList != null) {
                    postMessage(build, transferList);
                } else {
                    postMessage(build);
                }
            } catch (Throwable ex) {
                postMessage(ResultHolder.build(ex));
                error(ex);
            }
        });
    }

    private static <T> T evalQuietly(String s) {
        try {
            return eval(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Object[] getTransferList(Object params) {
        if (params instanceof String || params instanceof Number) {
            return null;
        }
        if (TypeHelper.isTransferable(params)) {
            Object[] transferList = new Object[1];
            transferList[0] = params;
            return transferList;
        } else {
            JSONObject jsParams = new JSONObject((JavaScriptObject) params);
            List transferList = new ArrayList();
            for (String key : jsParams.keySet()) {
                JSONObject object = jsParams.get(key).isObject();
                if (object != null && TypeHelper.isTransferable(object.getJavaScriptObject())) {
                    transferList.add(object.getJavaScriptObject());
                }
            }
            return transferList.toArray();
        }
    }

    private Worker<ParamsHolder> worker;

    public void execute(Params p) {
        if (worker == null) {
            worker = new Worker(GWT.<WorkerScriptFactory>create(WorkerScriptFactory.class
            ).getWorkerScriptURL());
            worker.setOnMessage((MessageListener<ResultHolder<Result>>) (MessageEvent<ResultHolder<Result>> s) -> {
                if (s.getData().failed()) {
                    onError(s.getData().exceptionType, s.getData().exceptionMessage);
                } else {
                    if (s.getData().dataType != null) {
                        done(castDoubleTo(s.getData().dataType, (Double) s.getData().result));
                    } else {
                        done(s.getData().result);
                    }
                }
            });
        }
        ParamsHolder paramsHolder = ParamsHolder.build(getClass().getName(), p);
        Object[] transferList = getTransferList(paramsHolder.params);
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

    private <T> T castDoubleTo(String type, Double o) {
        if (type.equals(Integer.class.getName())) {
            return (T) Integer.valueOf(o.toString());
        } else if (type.equals(Long.class.getName())) {
            return (T) Long.valueOf(o.toString());
        } else if (type.equals(Short.class.getName())) {
            return (T) Short.valueOf(o.toString());
        } else if (type.equals(Byte.class.getName())) {
            return (T) Byte.valueOf(o.toString());
        } else if (type.equals(Float.class.getName())) {
            return (T) Float.valueOf(o.toString());
        }
        return (T) o;
    }

    @JsProperty(namespace = JsPackage.GLOBAL, name = "onmessage")
    private static native void setOnMessage(MessageListener listener);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native <Transferable> void postMessage(ResultHolder p);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native <Transferable> void postMessage(ResultHolder p, Transferable[] transferList);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "eval")
    private static native <T> T eval(String s) throws Exception;

    @JsMethod(namespace = "console", name = "error")
    protected static native void error(Object o);

}
