/*****************************************************************************
 * Copyright (c) 2015 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jean-François Rolland (ATOS) - Initial API and implementation
 * Tristan Faure (ATOS)
 *
 *****************************************************************************/

package org.eclipse.gendoc.documents;

import org.eclipse.gendoc.services.IService;

public interface IImageManipulationServiceFactory extends IService{

	public  IImageManipulationService getService(String ext);
}
