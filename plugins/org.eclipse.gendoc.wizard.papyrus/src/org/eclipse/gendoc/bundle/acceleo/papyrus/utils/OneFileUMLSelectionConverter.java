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

package org.eclipse.gendoc.bundle.acceleo.papyrus.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gendoc.wizard.ISelectionConverter;
import org.eclipse.papyrus.infra.onefile.model.IPapyrusFile;

public class OneFileUMLSelectionConverter implements ISelectionConverter {

	public boolean matches(Object selectedObject) {
		return (getFile(selectedObject) != null);
	}

	public IFile getFile(Object selectedObject) {
       	IFile selectedFile = null;
        if (selectedObject != null) {
        	if (selectedObject instanceof IPapyrusFile) {
        		for (IResource resource : ((IPapyrusFile) selectedObject).getAssociatedResources()) {
        			if ((resource instanceof IFile) && (isUMLFile((IFile) resource))) {
        				selectedFile = (IFile) resource;
        				break;
        			} // else continue
        		}
        	}
        	else if (selectedObject instanceof EObject){
	    		EObject eobject = (EObject) selectedObject;
				selectedFile = WorkspaceSynchronizer.getFile(eobject.eResource());
        	} else if (selectedObject instanceof IAdaptable) {
        		selectedFile = (IFile)((IAdaptable) selectedObject).getAdapter(IFile.class); // Can be null
        		if (selectedFile == null){
        			EObject eobject = (EObject) ((IAdaptable) selectedObject).getAdapter(EObject.class);
        			if (eobject != null){
        				selectedFile = WorkspaceSynchronizer.getFile(eobject.eResource());
        			}
        		}
        	} else if (selectedObject instanceof IFile) {
        		selectedFile = (IFile) selectedObject;
        	} else {
        		// No valid selection
        	}        	

        } else {
            	// No valid selection
        }
        
        return selectedFile;
	}

	private boolean isUMLFile(IFile file) {
		return "uml".equals(file.getFileExtension());
	}

}
