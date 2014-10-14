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
package org.eclipse.gendoc.script.services;

import java.util.List;

import org.eclipse.acceleo.model.mtl.Module;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.services.IService;
import org.eclipse.gendoc.services.exception.GenDocException;

/**
 * Manage the modules compiled from the fragments
 * 
 * @author tfaure
 * 
 */
public interface IModuleManagerService extends IService
{

    /**
     * Get the modules to import from an eobject
     * 
     * @param element
     * @return symbolic names
     * @throws GenDocException
     */
    List<String> getImportedModules(EObject element) throws GenDocException;

    /**
     * Get the modules to import from an eobject
     * 
     * @param element
     * @return uris to load (uri with scheme equals to Gendocbundle://)
     */
    List<URI> getModuleURIs(EObject element);

    /**
     * Return a Module corresponding to the uri Gendocbundle:// The uri is formed like this :
     * scheme://generated_id/module_name
     * 
     * @param uri
     * @return
     */
    Module getModule(URI uri);
}
