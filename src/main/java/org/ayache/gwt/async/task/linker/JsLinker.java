/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ayache.gwt.async.task.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.linker.SingleScriptLinker;

/**
 *
 * @author Ayache
 */
@LinkerOrder(Order.POST)
public class JsLinker extends SingleScriptLinker {
    
    @Override
    public String getDescription() {
        return "Javascript Linker";
    }

    @Override
    protected EmittedArtifact emitSelectionScript(TreeLogger logger, LinkerContext context, ArtifactSet artifacts) throws UnableToCompleteException {
        if (artifacts.find(CompilationResult.class).isEmpty()){
            return null;
        }
        StringBuilder builder = new StringBuilder("var $wnd=self;");
        builder.append(artifacts.find(CompilationResult.class).first().getJavaScript()[0]);
        return emitString(logger, builder.toString(), context.getModuleName()+".worker.js");
    }

    

}
