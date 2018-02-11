/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 *
 * @author Ayache
 * @param <T>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface Reflect<T> {
    
    T construct(Object cls, Object[] args);
}
