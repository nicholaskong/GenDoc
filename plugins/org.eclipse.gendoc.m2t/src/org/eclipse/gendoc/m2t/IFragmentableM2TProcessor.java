/*****************************************************************************
 * Copyright (c) 2011 Atos
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.m2t;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.m2t.model.Fragment;
import org.eclipse.gendoc.services.exception.GenDocException;

public interface IFragmentableM2TProcessor extends IM2TProcessor {
	/**
     * Execute the given script with the given context and return the generated text.
     * 
     * @param element to run the script with 
     * @return the generated text as a String
     * @throws GenDocException a gendoc exception
     */
    String runFragmentScript(EObject element, Fragment fragment) throws GenDocException;
}
