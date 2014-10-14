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
package org.eclipse.gendoc.script.acceleo.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gendoc.script.acceleo.IFileAndMMRegistry;
import org.eclipse.gendoc.services.AbstractService;

public class FileAndMMRegistry extends AbstractService implements
		IFileAndMMRegistry {
	private Map<URI, Collection<String>> file2MetaModels = new LinkedHashMap<URI, Collection<String>>();

	@Override
	public void clear() {
		super.clear();
		file2MetaModels.clear();
	};

	/**
     * Associate a meta model to a file
     * 
     * @param metamodel the metamodel URI
     * @param file file URI
     */
    public void addMetaModelForFiles(String metamodel, List<URI> files)
    {
        for (URI file : files)
        {
            if (file2MetaModels.get(file) == null)
            {
                file2MetaModels.put(file, new LinkedHashSet<String>());
            }
            file2MetaModels.get(file).add(metamodel);

        }
    }

	/**
     * Get all metamodels for the model associated to the eObject
     * 
     * @param eObject
     * @return
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getMetamodels(EObject eObject)
    {
        URI fileURI = eObject.eResource().getURI();
        ResourceSet set = eObject.eResource().getResourceSet();
        set.getPackageRegistry().keySet();
        if (file2MetaModels.get(fileURI) == null)
        {
            Resource modelRoot = getModelRootElement(eObject);
            // al lthe content of the resource will be browsed to assign to each sub file the corresponding metamodels
            TreeIterator<EObject> allModelObjects = EcoreUtil.getAllContents(modelRoot,true);
            String metamodel;
            EObject currentObject;
            Stack<URI> modelFiles = new Stack<URI>();
            while (allModelObjects.hasNext())
            {
                currentObject = allModelObjects.next();
                EPackage epackage = currentObject.eClass().getEPackage();
                if (EPackage.Registry.INSTANCE.containsKey(epackage.getNsURI()))
                {
                    metamodel = epackage.getNsURI();
                    URI file = currentObject.eResource().getURI();
                    if (modelFiles.isEmpty())
                    {
                        modelFiles.push(file);
                    }
                    else if (modelFiles.peek() != null && !modelFiles.peek().equals(file))
                    {
                        if (currentObject.eContainer() != null)
                        {
                            URI parentURI = currentObject.eContainer().eResource().getURI();
                            if (parentURI.equals(modelFiles.peek()))
                            {
                                // If first element on a new file, add file to stack
                                modelFiles.push(file);
                            }
                            else
                            {
                                // Else, pop all elements until parentFile is found.
                                while (!modelFiles.isEmpty() && parentURI != modelFiles.peek())
                                {
                                    modelFiles.pop();
                                }
                                // all the inconsistent parent are removed 
                                // the hierarchy of tree can be modified adding the current uri 
                                modelFiles.add(file);
                            }
                            
                        }
                    }
                    addMetaModelForFiles(metamodel, modelFiles);
                }
            }
        }
        Collection<String> collection = file2MetaModels.get(fileURI);
        return collection == null ? Collections.EMPTY_LIST : collection;
    }
    
    /**
     * 
     * @param eObject
     * @return
     */
    private Resource getModelRootElement(EObject eObject)
    {
        EObject current = eObject;
        while (current.eContainer() != null)
        {
            current = current.eContainer();
        }
        return current.eResource();
    }

}
