/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;

/**
 *
 * @author Ayache
 */
public final class TypeHelper {

    public static boolean isTransferable(Object o) {
        String type = Helper.type(o); 
        return type.equals(Helper.getArrayBufferType()) || type.equals(Helper.getImageBitmapType()) || type.equals(Helper.getMessagePortType());
    }

    private static class Helper {

        @JsMethod(namespace = JsPackage.GLOBAL, name = "ArrayBuffer.prototype.toString")
        static native String getArrayBufferType();

        @JsMethod(namespace = JsPackage.GLOBAL, name = "ImageBitmap.prototype.toString")
        static native String getImageBitmapType();

        @JsMethod(namespace = JsPackage.GLOBAL, name = "MessagePort.prototype.toString")
        static native String getMessagePortType();

        @JsMethod(namespace = JsPackage.GLOBAL, name = "Object.prototype.toString.call")
        static native String type(Object o);
    }

}
