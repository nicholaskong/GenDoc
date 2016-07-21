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

package org.eclipse.gendoc.wizard.papyrus.popup.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.wizard.GendocWizard;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.Utils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @deprecated, menu not used anymore
 *
 */
public class GenerateDocumentationModelExplorerHandler extends org.eclipse.core.commands.AbstractHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if(selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            EObject selectedElement = EMFHelper.getEObject(structuredSelection.getFirstElement());
            if(selectedElement == null) {
                  return null;
            }
            URI resourceURI = selectedElement.eResource().getURI();
            //URI s = resourceURI.trimFileExtension();
            
            IFile selectedObject = org.eclipse.papyrus.infra.emf.utils.EMFFileUtil.getIFile(resourceURI.toString());
            
            //IFile diFile = ((IPapyrusFile)selectedObject).getMainFile();
            //IFile selectedObject = ((IPapyrusFile)org.eclipse.papyrus.infra.emf.utils.EMFFileUtil.getIFile(resourceURI.toString())).getMainFile();
            List<IFile> files = new LinkedList<IFile>();
            files.add((IFile) selectedObject);
            
            List<IGendocRunner> runners = (selectedObject != null) ? Utils.getRunners(selectedObject) : null;
            if (runners != null && !runners.isEmpty())
            {
                GendocWizard wizard = new GendocWizard(runners, files.toArray(new IFile[]{}));
                WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
                wizardDialog.open();
            }
		}

		
		return null;
	}

}
