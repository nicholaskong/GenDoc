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
package org.eclipse.gendoc.services;

import java.util.Map;

/**
 * @author pmwade
 *
 */
public interface IBookmarkService extends IService
{
 
    /**
     * @return the bookmarkRegistry
     */
    public Map<Object, Object> getBookmarkRegistry();
    
    /**
     * @param key
     * @return
     */
    public Object get(Object key);

    /**
     * @param key
     * @param value
     * @return
     */
    public Object put(Object key, Object value);
}
