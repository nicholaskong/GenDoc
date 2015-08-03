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
package org.eclipse.gendoc.tags.handlers.impl;

import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.handlers.AbstractPrePostTagHandler;

/**
 * An abstract tag handler that handles the services attribute of a tag.
 * 
 * @author Kris Robertson
 */
public abstract class AbstractServicesTagHandler extends AbstractPrePostTagHandler{

    /** Delimiter use to separate services. */
    private static final String delimiter = "\\s*;\\s*";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.AbstractTagHandler#runAttributes(org.eclipse.gendoc.tags.ITag, String)
     */
    @Override
    protected String runAttributes(ITag tag, String value) throws GenDocException
    {
        super.runAttributes(tag, value);
        for (String key : tag.getAttributes().keySet())
        {
            if (RegisteredTags.CONFIG_SERVICES.equalsIgnoreCase(key))
            {
                String services = tag.getAttributes().get(key);
                String[] serviceIds = services.trim().split(AbstractServicesTagHandler.delimiter);
                for (String serviceId : serviceIds)
                {
                    GendocServices.getDefault().setService(serviceId);
                }
            }
        }
        return value;
    }

}
