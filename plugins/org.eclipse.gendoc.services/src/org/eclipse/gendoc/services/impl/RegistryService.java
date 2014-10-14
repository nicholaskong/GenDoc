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
package org.eclipse.gendoc.services.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.IRegistryService;

/**
 * The default implementation of IRegistryService.
 * 
 * @author Kris Robertson
 * @see org.eclipse.gendoc.services.IRegistryService
 */
public class RegistryService extends AbstractService implements IRegistryService
{

    private final Map<Object, Object> registry = new HashMap<Object, Object>();

    private final List<Runnable> runnables = new LinkedList<Runnable>();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.IRegistryService#addCleaner(Runnable)
     */
    public void addCleaner(Runnable cleaner)
    {
        this.runnables.add(cleaner);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.IService#clear()
     */
    @Override
    public void clear()
    {
        for (Runnable runnable : this.runnables)
        {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                ILogger logger = GendocServices.getDefault().getService(ILogger.class);
                if (logger != null)
                {
                    logger.log(String.format("An unexpected error occurs during cleaning process : %s", e.getMessage()), IStatus.ERROR);
                    e.printStackTrace();
                }
            }
        }
        this.runnables.clear();
        this.registry.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.IRegistryService#containsKey(Object)
     */
    public boolean containsKey(Object key)
    {
        return this.registry.containsKey(checkLowerCase(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.IRegistryService#get(Object)
     */
    public Object get(Object key)
    {
        return this.registry.get(checkLowerCase(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gendoc.services.IRegistryService#put(Object, Object)
     */
    public Object put(Object key, Object value)
    {
        return this.registry.put(checkLowerCase(key), value);
    }

	/**
	 * If the key is a string, only use LowerCase
	 * @param key Object key
	 * @return Unchange key if key is not a string, or lowerCase key, when the key is a String
	 */
	private Object checkLowerCase(Object key) {
    	if( key != null && key instanceof String){
    		return ((String)key).toLowerCase();
    	}
		return key;
	}

}
