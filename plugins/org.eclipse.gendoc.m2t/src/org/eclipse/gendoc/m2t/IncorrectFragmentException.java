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
package org.eclipse.gendoc.m2t;

import org.eclipse.gendoc.services.exception.GenDocException;

public class IncorrectFragmentException extends GenDocException {

	private static final long serialVersionUID = 1L;

	public IncorrectFragmentException(String fragmentName) {
		super("fragment " + fragmentName + " already exist");
	}

}
