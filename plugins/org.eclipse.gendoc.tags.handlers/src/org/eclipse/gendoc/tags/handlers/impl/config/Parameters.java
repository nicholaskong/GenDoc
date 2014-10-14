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
 *  Tristan FAURE (Atos Origin) tristan.faure@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.gendoc.tags.handlers.impl.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * This class manages parameters given by user and parameters subscribed by extension point
 * 
 * @author tfaure
 * 
 */
public class Parameters
{

    private static String extensionPointId = "org.eclipse.gendoc.tags.handlers.parameters";

    private Map<String, IDeferredValue> map = new HashMap<String, IDeferredValue>();

    public Parameters()
    {
        // fill the map with dynamic parameters
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId);
        for (IConfigurationElement e : elements)
        {
            IParameterValue value = null;
            try
            {
                value = (IParameterValue) e.createExecutableExtension("instance");

            }
            catch (CoreException e1)
            {
            }
            if (value != null)
            {
                if (value instanceof IDeferredParameterValue)
                {
                    IDeferredParameterValue iDeferredParameterValue = (IDeferredParameterValue) value;
                    Map<String, IDeferredValue> tmp = iDeferredParameterValue.getDeferredValues();
                    putAll(tmp);
                }
                putAll(value.getValue());
            }
        }
    }

    protected void putAll(Map<String, ? > value)
    {
        for (String s : value.keySet())
        {
            Object x = value.get(s);
            if (x instanceof IDeferredValue)
            {
                map.put(s.toLowerCase(), (IDeferredValue) x);
            }
            else if (x instanceof String)
            {
                map.put(s.toLowerCase(), new IDeferredValue.StaticValue((String) x));
            }
        }

    }

    public void clear()
    {
        map.clear();
    }

    public String get(String lowerCase)
    {
        String tmp = null;
        IDeferredValue iDeferredValue = map.get(lowerCase);
        if (iDeferredValue != null)
        {
            tmp = iDeferredValue.get();
        }
        return tmp != null ? tmp : "";
    }

    public void put(String key, String value)
    {
        map.put(key.toLowerCase(), new IDeferredValue.StaticValue(value));
    }

}
