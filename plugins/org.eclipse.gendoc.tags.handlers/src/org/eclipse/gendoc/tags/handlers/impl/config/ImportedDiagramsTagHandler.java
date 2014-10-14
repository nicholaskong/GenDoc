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
package org.eclipse.gendoc.tags.handlers.impl.config;

import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * Handler for &lt;importedDiagrams&gt; tags.
 * 
 * @author Kris Robertson
 */
public class ImportedDiagramsTagHandler extends AbstractPrePostTagHandler
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#runAttributes(org.eclipse.gendoc.tags.ITag, String)
     */
    @Override
    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        super.runAttributes(tag, value);
        IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
        for (String key : tag.getAttributes().keySet())
        {
            if (RegisteredTags.IMPORTED_DIAGRAMS_PATH.equalsIgnoreCase(key))
            {
                String path = this.replaceParameters(tag.getAttributes().get(key));
                configService.setImportedDiagrams(path.replaceAll("\\\\", "/"));
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
