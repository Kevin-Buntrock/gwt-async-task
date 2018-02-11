/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import jsinterop.annotations.JsFunction;

/**
 *
 * @author Ayache
 */
@JsFunction
public interface MessageListener<DataType> {
    
   void on(MessageEvent<DataType> s);
}
