/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

/**
 *
 * @author Ayache
 */
class WorkerScriptDevModeFactory extends WorkerScriptFactory {

    private static String URL;
    private IScriptListener listener;

    private static String getOrigin() {
        String origin = new URL(GWT.getModuleBaseURL()).getOrigin();
        String[] split = origin.split(":");
        if (split.length == 1) {
            return origin + ":9876/";
        } else {
            return GWT.getModuleBaseURL().replace("8888", "9876");
        }
    }

    @Override
    void addScriptListener(IScriptListener listener) {
        if (URL != null){
            listener.onScriptReceived(URL);
        }
    }

    @Override
    public String getWorkerScriptURL() {
        if (URL == null) {
            XMLHttpRequest create = XMLHttpRequest.create();
            create.open("GET", getOrigin() + GWT.getPermutationStrongName() + ".worker.js");
            create.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);
            create.setOnReadyStateChange(new ReadyStateChangeHandler() {
                @Override
                public void onReadyStateChange(XMLHttpRequest xhr) {
                    if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                        Blob.Options options = new Blob.Options();
                        options.type = "text/plain";
                        URL = Blob.createObjectURL(new Blob(new ArrayBuffer[]{xhr.getResponseArrayBuffer()}, options));
                    }
                }
            });
            create.send();

        }
        return URL;
    }

}
