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
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.context;

import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.IContextService;
import org.eclipse.gendoc.tags.handlers.impl.AbstractServicesTagHandler;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * Handler for &lt;context&gt; tags.
 * 
 * @author Kris Robertson
 */
public class ContextTagHandler extends AbstractServicesTagHandler
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#run(org.eclipse.gendoc.tags.ITag)
     */
    @Override
    public String doRun(ITag tag) throws GenDocException
    {
        super.doRun(tag);
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#runAttributes(org.eclipse.gendoc.tags.ITag, String)
     */
    @Override
    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        super.runAttributes(tag, value);
        IContextService contextService = GendocServices.getDefault().getService(IContextService.class);
        // model must be set ...
        for (String key : tag.getAttributes().keySet())
        {
            if (RegisteredTags.CONTEXT_MODEL.equalsIgnoreCase(key))
            {
                String modelPath = this.replaceParameters(tag.getAttributes().get(key));
                contextService.setModel(modelPath.replaceAll("\\\\", "/"));
            }
        }
        // ... before other attributes?
        for (String key : tag.getAttributes().keySet())
        {
            if (RegisteredTags.CONTEXT_ELEMENT.equalsIgnoreCase(key))
            {
                String elementPath = this.replaceParameters(tag.getAttributes().get(key));
                contextService.setElementPath(elementPath);
            }
            if (RegisteredTags.CONTEXT_FEATURE_LABEL.equalsIgnoreCase(key))
            {
                contextService.setFeatureLabel(tag.getAttributes().get(key));
            }
            if (RegisteredTags.CONTEXT_IMPORTED_BUNDLES.equalsIgnoreCase(key))
            {
                contextService.setImportedBundles(tag.getAttributes().get(key));
            }
            if (RegisteredTags.CONTEXT_SEARCH_METAMODELS.equalsIgnoreCase(key))
            {
                contextService.setSearchMetamodels(Boolean.parseBoolean(tag.getAttributes().get(key)));
            }
        }
        return value;
    }

    /**
     * Replaces param references in the given path with their actual values.
     * 
     * @param path the path
     * 
     * @return the path with parameter references replaced by actual values
     * 
     * @throws InvalidTemplateParameterException if a referenced parameter does not exist
     */
    private String replaceParameters(String path) throws InvalidTemplateParameterException
    {
        IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
        return configService.replaceParameters(path);
    }

}
