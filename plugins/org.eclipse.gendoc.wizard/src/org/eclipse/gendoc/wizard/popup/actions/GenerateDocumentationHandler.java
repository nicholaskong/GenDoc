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

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gendoc.wizard.GendocWizard;
import org.eclipse.gendoc.wizard.IGendocRunner;
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
                GendocWizard wizard = new GendocWizard(runners, Utils.getIFiles(firstElement));
                WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
                wizardDialog.open();
            }

        }
        return null;
    }
}
