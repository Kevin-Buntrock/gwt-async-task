/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
@JsType
@SuppressWarnings("UseSpecificCatch")
public abstract class AsyncTask<Params, Progress, Result> {

    private static final WorkerScriptFactory FACTORY = GWT.<WorkerScriptFactory>create(WorkerScriptFactory.class);

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class DataHolder<Type> {

        protected Type data;
        protected String dataType;
        protected boolean progressData;

        @JsOverlay
        static <T extends DataHolder> T build(Object data) {
            DataHolder dataHolder = new DataHolder();
            if (data instanceof String || data instanceof Double) {
                dataHolder.data = data;
            } else if (data instanceof Number) {
                dataHolder.dataType = data.getClass().getName();
                dataHolder.data = Double.valueOf(String.valueOf(data));
            } else if (data instanceof JavaScriptObject) {
                dataHolder.data = (JavaScriptObject) data;
            } else {
                Logger.getLogger("").log(Level.SEVERE, "Params type of AsyncTask<Params, Result> must be JavascriptObject or @JsType annotated class");
            }
            return (T) dataHolder;
        }

        @JsOverlay
        static <T extends DataHolder> T build(Object data, boolean progressData) {
            DataHolder dataHolder = build(data);
            dataHolder.progressData = true;
            return (T) dataHolder;
        }

    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ParamsHolder extends DataHolder {

        private String type;

        @JsOverlay
        static ParamsHolder build(String type, Object params) {
            ParamsHolder paramsHolder = new ParamsHolder();
            paramsHolder.type = type;
            DataHolder dataHolder = DataHolder.build(params);
            paramsHolder.dataType = dataHolder.dataType;
            paramsHolder.data = dataHolder.data;
            return paramsHolder;
        }

    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class ResultHolder<Result> extends DataHolder<Result> {

        private String exceptionType;
        private String exceptionMessage;

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
        if (Worker.workerScope() != null) {
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
                    if (s.getData().dataType != null) {
                        doInBackground = construct.doInBackground(castDoubleTo(s.getData().dataType, Double.valueOf(s.getData().data.toString())));
                    } else {
                        doInBackground = construct.doInBackground(s.getData().data);
                    }
                    ResultHolder build = ResultHolder.build(doInBackground);
                    JsArray transferList = getTransferList(build.data);
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
    }

    private static <T> T evalQuietly(String s) {
        try {
            return eval(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private static JsArray getTransferList(Object params) {
        if (params instanceof String || params instanceof Number) {
            return null;
        }
        if (TypeHelper.isTransferable(params)) {
            JsArray transferList = (JsArray) JavaScriptObject.createArray();
            transferList.set(0, (JavaScriptObject) params);
            return transferList;
        } else {
            JSONObject jsParams = new JSONObject((JavaScriptObject) params);
            JsArray transferList = (JsArray) JavaScriptObject.createArray();
            for (String key : jsParams.keySet()) {
                JSONObject object = jsParams.get(key).isObject();
                if (object != null && TypeHelper.isTransferable(object.getJavaScriptObject())) {
                    transferList.push(object.getJavaScriptObject());
                }
            }
            return transferList;
        }
    }

    private static <T> T castDoubleTo(String type, Double o) {
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

    private Worker<ParamsHolder> worker;

    public final void execute(Params p) {
        if (FACTORY.getWorkerScriptURL() == null) {
            FACTORY.addScriptListener(new WorkerScriptFactory.IScriptListener() {
                @Override
                public void onScriptReceived(String url) {
                    execute(p);
                }
            });
        } else {
            if (worker == null) {
                worker = new Worker(FACTORY.getWorkerScriptURL());
                worker.setOnMessage((MessageListener<ResultHolder>) (MessageEvent<ResultHolder> s) -> {
                    if (s.getData().failed()) {
                        onError(s.getData().exceptionType, s.getData().exceptionMessage);
                    } else if (s.getData().progressData) {
                        if (s.getData().dataType != null) {
                            process(castDoubleTo(s.getData().dataType, (Double) s.getData().data));
                        } else {
                            process((Progress) s.getData().data);
                        }
                    } else {
                        if (s.getData().dataType != null) {
                            done(castDoubleTo(s.getData().dataType, (Double) s.getData().data));
                        } else {
                            done((Result) s.getData().data);
                        }
                    }
                });
            }
            ParamsHolder paramsHolder = ParamsHolder.build(getClass().getName(), p);
            JsArray transferList = getTransferList(paramsHolder.data);
            if (transferList != null) {
                worker.postMessage(paramsHolder, transferList);
            } else {
                worker.postMessage(paramsHolder);
            }
        }
    }

    public final void publish(Progress r) {
        ResultHolder build = ResultHolder.build(r, true);
        JsArray transferList = getTransferList(build.data);
        if (transferList != null) {
            postMessage(build, transferList);
        } else {
            postMessage(build);
        }
    }

    protected abstract Result doInBackground(Params p) throws Exception;

    protected abstract void done(Result r);

    protected void process(Progress r) {
    }

    protected void onError(String type, String message) {
    }

    @JsProperty(namespace = JsPackage.GLOBAL, name = "onmessage")
    private static native void setOnMessage(MessageListener listener);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native <Transferable> void postMessage(ResultHolder p);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "postMessage")
    private static native <Transferable extends JavaScriptObject> void postMessage(ResultHolder p, JsArray<Transferable> transferList);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "eval")
    private static native <T> T eval(String s) throws Exception;

    @JsMethod(namespace = "console", name = "error")
    protected static native void error(Object o);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "toString")
    protected static native void scope();

    @JsMethod(namespace = JsPackage.GLOBAL, name = "Object.create")
    protected static native <P,T> T create(P prototype);

}
