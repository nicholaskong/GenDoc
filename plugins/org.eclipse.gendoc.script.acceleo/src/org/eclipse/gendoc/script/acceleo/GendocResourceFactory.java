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

import org.eclipse.acceleo.model.mtl.Module;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gendoc.script.services.IModuleManagerService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;

/**
 * A resource factory matchin Gendocbundle scheme
 * @author tfaure
 *
 */
public class GendocResourceFactory implements Resource.Factory {

	public Resource createResource(URI uri) {
		if (Messages.GendocResourceFactory_BUNDLE_URI_SCHEME.equals(uri.scheme()))
		{
			IModuleManagerService manager = GendocServices.getDefault().getService(IModuleManagerService.class);
			Module m = manager.getModule(uri);
			if (m!= null)
			{
				return m.eResource();
			}
		}
		IGendocDiagnostician diag = GendocServices.getDefault().getService(IGendocDiagnostician.class);
		diag.addDiagnostic(IStatus.ERROR, String.format("the needed fragment %s could not be loaded",uri.path()), uri);
		return null;
	}

}
