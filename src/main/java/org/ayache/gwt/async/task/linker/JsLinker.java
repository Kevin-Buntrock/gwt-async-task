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
import com.google.gwt.core.linker.CrossSiteIframeLinker;
import java.util.SortedSet;

/**
 *
 * @author Ayache
 */
@LinkerOrder(Order.POST)
public class JsLinker extends CrossSiteIframeLinker {

    @Override
    public String getDescription() {
        return "Javascript Linker";
    }

    @Override
    protected void maybeAddHostedModeFile(TreeLogger logger, LinkerContext context,
            ArtifactSet artifacts, CompilationResult result) throws UnableToCompleteException {
        SortedSet<CompilationResult> find = artifacts.find(CompilationResult.class);

        find.forEach((CompilationResult t) -> {
            try {
                StringBuilder builder = new StringBuilder("var $wnd=self;\n");
                builder.append(t.getJavaScript()[0]);
                artifacts.add(emitString(logger, builder.toString(), t.getStrongName() + ".worker.js"));
            } catch (UnableToCompleteException ex) {
                logger.log(TreeLogger.Type.ERROR, "Unable to create worker javascript file", ex);
            }
        });

    }

    @Override
    protected EmittedArtifact emitSelectionScript(TreeLogger logger, LinkerContext context, ArtifactSet artifacts) throws UnableToCompleteException {
        return null;
    }
    
    

}
