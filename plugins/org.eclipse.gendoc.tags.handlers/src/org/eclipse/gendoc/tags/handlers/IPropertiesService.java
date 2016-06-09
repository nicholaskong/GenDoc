/*****************************************************************************
 * Copyright (c) 2016 Atos
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Tristan FAURE (Atos) tristan.faure@atos.net - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.tags.handlers;

import java.io.File;

import org.eclipse.gendoc.services.IService;

public interface IPropertiesService extends IService
{

	/**
	 * Registers properties defined in a file
	 * @param path
	 */
	void setPropertiesFile (File file) ;
}
