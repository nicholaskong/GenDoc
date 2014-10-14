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
 *  Tristan FAURE (Atos) tristan.faure@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.config;

/**
 * An {@link IDeferredValue} is a value computed when the code is running not at initialization to ensure unique value
 * after computation the developer has to manage it by itself
 */
public interface IDeferredValue
{
    public String get();

    public static class StaticValue implements IDeferredValue
    {

        private final String string;

        public StaticValue(String s)
        {
            this.string = s;
        }

        public String get()
        {
            return string;
        }

    }
}
