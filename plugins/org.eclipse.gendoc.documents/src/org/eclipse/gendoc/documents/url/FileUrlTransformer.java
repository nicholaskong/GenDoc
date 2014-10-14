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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;

public class FileUrlTransformer extends UrlTransformer {

	private File curFile = null;

	public FileUrlTransformer() {
		super();
	}

	public BufferedInputStream getInputStream(String urlOri) {
		BufferedInputStream in = null;
		oriUrl = urlOri;

		try {
			curUrl = URLDecoder.decode(urlOri, "UTF-8").replace("///", "/")
					.replace(PROTO_FILE, "");
		} catch (UnsupportedEncodingException e) {
			ILogger logger = GendocServices.getDefault().getService(
					ILogger.class);
			logger.log("Bad encoding for url", Status.ERROR);
		}
		curFile = new File(curUrl);
		if (curFile.exists()) {
			try {
				in = new BufferedInputStream(new FileInputStream(curFile));
			} catch (FileNotFoundException e) {
				ILogger logger = GendocServices.getDefault().getService(
						ILogger.class);
				logger.log("Unable to find file : " + curUrl, Status.ERROR);
			}
		}

		return in;
	}

	public String getExtention() {
		return Path.fromOSString(curUrl).getFileExtension();
	}

	public int getSizeOfStream() {
		long size = curFile.length();
		if (size > Integer.MAX_VALUE) {
			return 0;
		}
		return (int) curFile.length();
	}

	public String getConvertMhtUrl(String toConvert) {
		return toConvert.replace("///", "/").replace(PROTO_FILE, PROTO_HTTP);
	}

}
