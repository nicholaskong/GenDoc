/*****************************************************************************
 * (c) Copyright 2016 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.preferences.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gendoc.wizard.ISelectionConverter;

/**
 * The Class PreferenceGendocSelectionConverter implements {@link ISelectionConverter}
 * to adapt the selected object in the workbench to a model file that the 
 * {@link PreferenceGendocRunner} will use as model file.
 * 
 */
public class PreferenceGendocSelectionConverter implements ISelectionConverter {
	
	
	/** The {@link PreferenceGendocRunner} for which this converter has been created for.  */
	private PreferenceGendocRunner preferenceGenDocRunner; 

	/**
	 * Instantiates a new preference gendoc selection converter associated 
	 * with the given @{link PreferenceGendocRunner}.
	 *
	 * @param preferenceGenDocRunner the @{link PreferenceGendocRunner} owning this
	 *        PreferenceGendocSelectionConverter.
	 */
	public PreferenceGendocSelectionConverter(PreferenceGendocRunner preferenceGenDocRunner) {
		super();
		this.preferenceGenDocRunner = preferenceGenDocRunner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.ISelectionConverter#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object selectedObject) {
		if (preferenceGenDocRunner.getProject() == null)
			return true;
		
		IFile file = getFile(selectedObject);
		if (file == null)
			return false;
		
		if (file.getProject() == preferenceGenDocRunner.getProject())
			return  true;
		
		try {
			IProject[] refProjects = file.getProject().getReferencedProjects();
			for (IProject p : refProjects) {
				if (p == file.getProject())
					return true;
			}
		} catch (CoreException e) {}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.ISelectionConverter#getFile(java.lang.Object)
	 */
	@Override
	public IFile getFile(Object selectedObject) {
		EObject eobj = getEObject(selectedObject); 
        if (eobj instanceof EObject) {
        	Resource eresource = eobj.eResource();
        	URI eUri = eresource.getURI();
        	if (eUri.isPlatformResource()) {
        		String platformString = eUri.toPlatformString(true);
        		selectedObject = ResourcesPlugin.getWorkspace().getRoot().findMember(platformString);
    		}
        }        	
        
        IFile file = (IFile) Platform.getAdapterManager().getAdapter(selectedObject, IFile.class);
        if (file == null && selectedObject instanceof IAdaptable)
        	file = (IFile)((IAdaptable)selectedObject).getAdapter(IFile.class);

        // Papyrus set file support.
        if (file.getFileExtension().endsWith("di")) {
    		String name = file.getName();
    		IResource r = file.getParent().findMember(name.substring(0,name.length()-2)+"uml");
    		if (r instanceof IFile)
    			file = (IFile)r;
    	}

        return file;
	}
		
	/**
	 * Adapt the given object to an EMF @{link EObject}.
	 *
	 * @param obj the object to adapt. 
	 * @return the @{link EObject} to which the given object adapts to.
	 */
	private EObject getEObject(Object obj) {
        EObject eobj = (EObject) Platform.getAdapterManager().getAdapter(obj, EObject.class);
        if (eobj == null && obj instanceof IAdaptable)
        	eobj = (EObject)((IAdaptable)obj).getAdapter(EObject.class);
        return eobj;
  	}
}
