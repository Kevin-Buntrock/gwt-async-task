/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.typedarrays.shared.ArrayBuffer;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
class Blob {

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class Options {
        String type;
        String endings;
    }

    public Blob(ArrayBuffer[] arrayBuffers, Options type) {

    }
    
    @JsMethod(namespace = JsPackage.GLOBAL, name="URL.createObjectURL")
    static native String createObjectURL(Blob blob);
}
