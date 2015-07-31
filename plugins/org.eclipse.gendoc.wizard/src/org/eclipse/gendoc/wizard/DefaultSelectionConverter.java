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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

public class DefaultSelectionConverter implements ISelectionConverter {

	@Override
	public boolean matches(Object selectedObject) {
		return getFile(selectedObject) != null;
	}

	@Override
	public IFile getFile(Object selectedObject) {
		IFile result = null;
		if (selectedObject instanceof IFile){
			result = (IFile) selectedObject;
		}
		if (selectedObject instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) selectedObject;
			result = (IFile) adaptable.getAdapter(IFile.class);
		}
		if (result == null){
			result = (IFile) Platform.getAdapterManager().getAdapter(selectedObject, IFile.class);
		}
		return result;
	}

}
