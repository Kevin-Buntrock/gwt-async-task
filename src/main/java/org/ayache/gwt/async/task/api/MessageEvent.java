/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface MessageEvent<DataType> {
    
    @JsProperty
    DataType getData();
    
}
