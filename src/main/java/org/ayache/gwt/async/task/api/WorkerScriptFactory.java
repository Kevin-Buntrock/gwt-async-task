/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.api;

import com.google.gwt.core.client.GWT;

/**
 *
 * @author Ayache
 */
class WorkerScriptFactory {

    String getWorkerScriptURL() {
        return GWT.getModuleBaseURL() + GWT.getPermutationStrongName() + ".worker.js";
    }

    void addScriptListener(IScriptListener listener){}
    
    interface IScriptListener {

        void onScriptReceived(String url);

    }
}
