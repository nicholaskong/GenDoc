/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Kris Robertson (Atos Origin) kris.robertson@atosorigin.com - Initial API and implementation
 * Papa Malick WADE (Atos Origin) papa-malick.wade@atosorigin.com - implement the id for tags
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.m2t.IM2TProcessor;
import org.eclipse.gendoc.m2t.IScriptLanguageExtensionService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.ElementNotFoundException;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * An abstract tag handler that provides methods for running scripts within a tag.
 * 
 * @author Kris Robertson
 */
public abstract class AbstractScriptTagHandler extends AbstractPrePostTagHandler
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#runScripts(org.eclipse.gendoc.tags.ITag, String)
     */
    @Override
    protected String runScripts(ITag tag, String value) throws GenDocException
    {
        IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
        IGendocDiagnostician gendocDiagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
        ILogger logger = GendocServices.getDefault().getService(ILogger.class);
        IConfigurationService configurationService = GendocServices.getDefault().getService(IConfigurationService.class);
        IM2TProcessor processor = getProcessor(configurationService.getLanguage());

        StringBuilder resultBuffer = new StringBuilder();
        // Run script
        IContextService context = GendocServices.getDefault().getService(IContextService.class);

        String cleanedTagValue = documentService.cleanContent(tag.getValue(), processor.getScriptPatterns());
        logger.log("Script before :" + cleanedTagValue, ILogger.DEBUG);
        try
        {
            resultBuffer.append(runProcessorScript(processor, context, cleanedTagValue, tag));
        }
        catch (GenDocException e)
        {
            
            // if the id of tag is not found, tagIdFound will be null
            String tagIdFound = tag.getAttributes().get(RegisteredTags.ID);
            String message= "The execution of a script failed: " + e.getUIMessage();
            if (null != tagIdFound)
            {
                message= "The execution of script with id '"+tagIdFound+"' failed : " + e.getUIMessage();
            }
            // Do not throw exception => just add a log in diagnostician in order to get all sucessfully generated parts
            // ok
            gendocDiagnostician.addDiagnostic(Diagnostic.ERROR, message, tag);
            e.printStackTrace();
            StringWriter wr = new StringWriter();
            e.printStackTrace(new PrintWriter(wr,true));
            return " --- ERROR DURING GENERATION OF THIS PART \n Error message: " + this.removeSpecialCharacters(e.getUIMessage()) + " --- "  
            	+ wr.toString() + "---" + tag.getRawText() + " --- END ERROR --- ";
        }
        logger.log("Script result :" + resultBuffer.toString(), ILogger.DEBUG);
        return resultBuffer.toString();
    }

    protected String runProcessorScript(IM2TProcessor processor, IContextService context, String cleanedTagValue, ITag tag) throws GenDocException, ModelNotFoundException, ElementNotFoundException
    {
        return processor.runScript(cleanedTagValue, context.getElement());
    }

    protected IM2TProcessor getProcessor(String language) throws GenDocException
    {
        IScriptLanguageExtensionService scriptLanguageExtensionService = GendocServices.getDefault().getService(IScriptLanguageExtensionService.class);
        IM2TProcessor processor = scriptLanguageExtensionService.getProcessor(language);
        return processor;
    }

    /**
     * Encodes special characters within the given string.
     * 
     * @param string the string to encode
     * 
     * @return the string with encoded special characters
     */
    private String removeSpecialCharacters(String string)
    {
        return string.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
    }

}
