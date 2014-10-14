/*****************************************************************************
 * Copyright (c) 2012 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Anne Haugommard (Atos) anne.haugommard@atos.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.gendoc.documents.url;

import java.io.BufferedInputStream;

public abstract class UrlTransformer {

	public static final String PROTO_FILE = "file:/";
	public static final String PROTO_HTTP = "http:/";

	protected String oriUrl = "";
	protected String curUrl = "";

	protected UrlTransformer() {

	}

	/**
	 * Get stream of input url.
	 * 
	 * @param urlOri
	 * @return
	 */
	public abstract BufferedInputStream getInputStream(String urlOri);

	/**
	 * Get extention of file.
	 * 
	 * @return
	 */
	public abstract String getExtention();

	/**
	 * Get size of stream.
	 * 
	 * @return
	 */
	public abstract int getSizeOfStream();

	/**
	 * get url to put in original html part to have link priority in mime html
	 * instead of external priority.
	 * 
	 * @param toConvert
	 * @return
	 */
	public abstract String getConvertMhtUrl(String toConvert);

}
