/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Worker<P> {

    public Worker(String url) {

    }

    @JsMethod(name = "postMessage")
    public native void postMessage(P s);

    @JsProperty(name = "onmessage")
    public native void setOnMessage(MessageListener listener);

}
