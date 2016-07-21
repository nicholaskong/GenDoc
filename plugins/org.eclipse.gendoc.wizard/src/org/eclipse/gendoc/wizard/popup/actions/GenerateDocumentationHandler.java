/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Anne Haugommard (Atos Origin) anne.haugommard@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.wizard.popup.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gendoc.wizard.GendocWizard;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocSelectionConverterRunner;
import org.eclipse.gendoc.wizard.Utils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class GenerateDocumentationHandler extends org.eclipse.core.commands.AbstractHandler
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection selec = (IStructuredSelection) selection;
            Object firstElement = selec.getFirstElement();
			List<IGendocRunner> runners = Utils.getRunners(firstElement);
            if (runners == null || runners.isEmpty()){
            	IFile adapted = null;
            	if (firstElement instanceof IAdaptable) {
					IAdaptable iada = (IAdaptable) firstElement;
					adapted = (IFile) iada.getAdapter(IFile.class);
				}
            	if (adapted == null){
            		adapted = (IFile) Platform.getAdapterManager().getAdapter(firstElement, IFile.class);
            	}
            	
            	if (adapted != null){
            		runners = Utils.getRunners(adapted);
            	}
            }
            if (runners != null && !runners.isEmpty())
            {
            	// this part collects all the runners as the IFiles from any
            	// implementation of IGendocSelectionConverterRunner
            	Set<IFile> files = new HashSet<IFile>(Arrays.asList(Utils.getIFiles(firstElement)));
            	for (IGendocRunner r : runners){
            		if (r instanceof IGendocSelectionConverterRunner) {
						IGendocSelectionConverterRunner converterRunner = (IGendocSelectionConverterRunner) r;
						if (converterRunner.getSelectionConverter() != null){
							files.add(converterRunner.getSelectionConverter().getFile(firstElement));
						}
					}
            	}
                IFile[] iFiles = files.toArray(new IFile[]{});
				GendocWizard wizard = new GendocWizard(runners, iFiles);
                WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
                wizardDialog.open();
            }

        }
        return null;
    }
}
