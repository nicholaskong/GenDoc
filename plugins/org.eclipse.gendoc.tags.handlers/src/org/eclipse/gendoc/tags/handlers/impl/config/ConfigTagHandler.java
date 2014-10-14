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
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.eclipse.gendoc.tags.handlers.impl.AbstractServicesTagHandler;
import org.eclipse.gendoc.tags.handlers.impl.RegisteredTags;

/**
 * Handler for &lt;config&gt; tags.
 * 
 * @author Kris Robertson
 */
public class ConfigTagHandler extends AbstractServicesTagHandler
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
        IConfigurationService configService = GendocServices.getDefault().getService(IConfigurationService.class);
        for (String key : tag.getAttributes().keySet())
        {
            if (RegisteredTags.CONFIG_LANGUAGE.equalsIgnoreCase(key))
            {
                configService.setLanguage(tag.getAttributes().get(key));
            }
            else if (RegisteredTags.CONFIG_RUN_V1.equalsIgnoreCase(key))
            {
                configService.setRunV1(tag.getAttributes().get(key));
            }
            else if (RegisteredTags.CONFIG_VERSION.equalsIgnoreCase(key))
            {
                configService.setVersion(tag.getAttributes().get(key));
            }
        }
        return value;
    }

}
