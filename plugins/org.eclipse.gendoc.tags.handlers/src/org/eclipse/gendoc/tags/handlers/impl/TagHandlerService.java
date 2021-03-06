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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.tags.ITag;
import org.eclipse.gendoc.tags.ITagExtension;
import org.eclipse.gendoc.tags.ITagExtensionService;
import org.eclipse.gendoc.tags.ITagHandler;
import org.eclipse.gendoc.tags.handlers.ITagHandlerService;

/**
 * A tag handler service implementation that returns the handler for a tag as specified by the tag extension registered
 * to the tags extension point.
 * 
 * @author Kris Robertson
 */
public class TagHandlerService extends AbstractService implements ITagHandlerService
{

    private Map<String, ITagHandler> tagHandlers = new HashMap<String, ITagHandler>();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.IService#clear()
     */
    @Override
    public void clear()
    {
        super.clear();
        this.tagHandlers.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.ITagHandlerService#getHandlerFor(org.eclipse.gendoc.tags.ITag)
     */
    public ITagHandler getHandlerFor(ITag tag)
    {
        return this.getHandlerFor(tag.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.tags.handlers.ITagHandlerService#getHandlerFor(String)
     */
    public ITagHandler getHandlerFor(String tagName)
    {
        ITagHandler tagHandler = this.tagHandlers.get(tagName);
        if ((tagName != null) && (tagHandler == null))
        {
            ITagExtensionService tagsExtensionService = GendocServices.getDefault().getService(ITagExtensionService.class);
            ITagExtension tagExtension = tagsExtensionService.getTagExtension(tagName);
            if (tagExtension != null)
            {
                tagHandler = tagExtension.getHandler();
                this.tagHandlers.put(tagName, tagHandler);
            }
        }
        return tagHandler;
    }

}
