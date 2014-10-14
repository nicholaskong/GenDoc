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
package org.eclipse.gendoc.script.acceleo;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.services.IService;

/**
 * IFileAndMetaModel Registry
 * @author tfaure
 *
 */
public interface IFileAndMMRegistry extends IService {

	/**
	 * Returns the metamodels for a given eobject
	 * @param eobject
	 * @return a collection of metamodels uri in String
	 */
	Collection<String> getMetamodels(EObject eobject);
	
}
