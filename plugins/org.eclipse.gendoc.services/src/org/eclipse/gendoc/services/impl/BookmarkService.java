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
package org.eclipse.gendoc.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.IBookmarkService;

/**
 * @author pmwade
 *
 */
public class BookmarkService extends AbstractService implements IBookmarkService
{

    private final Map<Object, Object> bookmarkRegistry = new HashMap<Object, Object>();

    /**
     * @return the bookmarkRegistry
     */
    public Map<Object, Object> getBookmarkRegistry()
    {
        return bookmarkRegistry;
    }
    
    /**
     * @param key
     * @return
     */
    public Object get(Object key) {
        return this.bookmarkRegistry.get(key);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Object put(Object key, Object value)
    {
        return this.bookmarkRegistry.put(key, value);
    }

	@Override
	public void clear() {
		super.clear();
		bookmarkRegistry.clear();
	}
    
    
}
