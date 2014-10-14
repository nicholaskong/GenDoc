/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Caroline Bourdeu d'Aguerre (Atos Origin) caroline.bourdeudaguerre@atosorigin.com - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.documents.impl;

import org.eclipse.osgi.util.NLS;

public class DocumentTypesExtensionPoint extends NLS
{

	// extension point ID
	public static String EXTENSION_POINT_ID;

	// documentType
	public static String DOCUMENT_TYPE;
	public static String DOCUMENT_TYPE_EXTENSION;
	public static String DOCUMENT_TYPE_DOCUMENT_SERVICE;

	// initialize message bundle
	static
	{
		NLS.initializeMessages(DocumentTypesExtensionPoint.class.getName(), DocumentTypesExtensionPoint.class);
	}

}
