/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 * @param <P>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Worker<P> {

    public Worker(String url) {

    }

    @JsMethod(name = "postMessage")
    public native void postMessage(P s);

    @JsMethod(name = "postMessage")
    public native <Transferable extends JavaScriptObject> void postMessage(P s, JsArray<Transferable> transferList);

    @JsMethod(name = "terminate")
    public native void terminate();

    @JsProperty(name = "onmessage")
    public native void setOnMessage(MessageListener listener);

    @JsProperty(namespace = JsPackage.GLOBAL, name = "DedicatedWorkerGlobalScope")
    public static native Object workerScope();

}
