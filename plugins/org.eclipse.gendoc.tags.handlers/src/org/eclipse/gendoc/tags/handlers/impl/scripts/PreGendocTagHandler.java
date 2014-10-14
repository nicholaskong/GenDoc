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
 * Papa Malick Wade (Atos) papa-malick.wade@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.scripts;

import java.util.Iterator;

import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IBookmarkService;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.IPreTagHandler;
import org.eclipse.gendoc.tags.ITag;

/**
 * @author pmwade
 *
 */
public class PreGendocTagHandler implements IPreTagHandler
{
   /* (non-Javadoc)
     * @see org.eclipse.gendoc.tags.IPreTagHandler#preRun(org.eclipse.gendoc.tags.ITag)
     */
    public ITag preRun(ITag tag) throws GenDocException
    {
        IBookmarkService bookmarkRegistry = GendocServices.getDefault().getService(IBookmarkService.class);
        if (bookmarkRegistry != null){
            Iterator<Object> iterator = bookmarkRegistry.getBookmarkRegistry().keySet().iterator();
            ITag tagTest = tag;
            
            while(iterator. hasNext()){        
                Object key = iterator.next();
                Object value = bookmarkRegistry.get(key);
                String st = tagTest.getValue().replaceAll(key.toString(),value.toString());
                tagTest.setValue(st);
            }
             
        return tagTest;
        }
        return tag;
    }

}
