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
 *  Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers.impl.config;

import java.util.Map;

/**
 * {@link IDeferredParameterValue} allows developers to specify maps of stringsas it inherits from
 * {@link IParameterValue} and maps of deferred strings, the value of {@link IDeferredValue}
 * 
 * @author tfaure
 * 
 */
public interface IDeferredParameterValue extends IParameterValue
{
    Map<String, IDeferredValue> getDeferredValues();
}
