/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.typedarrays.client.JsUtils;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 *
 * @author Ayache
 */
class WorkerScriptDevModeFactory extends WorkerScriptFactory {

    private static final String URL;

    static {
        XMLHttpRequest create = XMLHttpRequest.create();
        create.open("GET", GWT.getModuleBaseURL().replace("8888", "9876") + GWT.getModuleName() + ".worker.js", false);
        create.send();
        Blob.Options options = new Blob.Options();
        options.type = "application/javascript";
        Blob blob = new Blob(new ArrayBuffer[]{JsUtils.arrayBufferFromString(create.getResponseText())}, options);
        URL = Blob.createObjectURL(blob);
    }

    @Override
    public String getWorkerScriptURL() {
        return URL;
    }

}
