/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 *****************************************************************************/

package org.eclipse.gendoc.wizard;

public interface IGendocSelectionConverterRunner extends IGendocRunner {

    /**
     * @return the selection converter
     */
    ISelectionConverter getSelectionConverter();
	
}
