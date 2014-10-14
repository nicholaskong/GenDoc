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
package org.eclipse.gendoc.documents;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;
import org.eclipse.gendoc.documents.url.FileUrlTransformer;
import org.eclipse.gendoc.documents.url.UrlTransformer;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.UnknownMimeTypeException;

public abstract class MimeHtmlService extends AbstractService implements
		IMimeHtmlService {
	protected static final String MIME_VERSION = "MIME-Version: 1.0";
	protected static final String MIME_CONTENT = "Content-Type: multipart/related;boundary=";

	protected static final String MIME_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
	protected static final String MIME_HEAD = "</head>";
	protected static final String MIME_META = "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head>";

	protected static final String MIME_HTML_TYPE = "Content-Type: text/html;";
	protected static final String MIME_HTML_TRANSFERT = "Content-Transfer-Encoding: quoted-printable";

	protected static final String MIME_RESSOURCE_TYPE = "Content-Type:";
	protected static final String MIME_RESSOURCE_TRANSFERT = "Content-Transfer-Encoding: base64";
	protected static final String MIME_RESSOURCE_LOCATION = "Content-Location:";

	protected static final String MIME_SEP = "--";
	protected static final String MHT_IDF = "mhtTOPCASEDgenereted";

	protected static final String LINE_SEP = System
			.getProperty("line.separator");

	protected static final String REG_FIND1 = "src=\"";
	protected static final String REG_TABLE1 = "<table";
	protected static final String REG_TABLE2 = "<td";
	protected static final String REG_TABLE3 = "<tr";
	protected static final String REG_TABLE4 = "<td";
	protected static final String REG_LINK = "\"";
	protected static final String REG_OPEN = "<";
	protected static final String REG_CLOSE = ">";
	protected static final String REG_3D_ORI = "=";
	protected static final String REG_3D_FINAL = "=3D";

	protected String currentProtocol;
	protected Set<String> fileExtensions;
	protected String beginPart;
	protected String endPart;

	protected Map<String, UrlTransformer> protocolMap = new HashMap<String, UrlTransformer>();

	public MimeHtmlService() {
		super();
		// init
		fileExtensions = new HashSet<String>();
		protocolMap.put(UrlTransformer.PROTO_FILE, new FileUrlTransformer());
	}

	public void clear() {
		// do nothing
	}

	public abstract String convertToMimeHtml(String sourceHtml);

	/**
	 * Find url for add to mime html add link protocol for him.
	 * 
	 * @param html
	 * @param minPos
	 * @return
	 */
	protected int findExternalLink(String html, int minPos) {
		// img src
		int dFind = html.substring(minPos).indexOf(REG_FIND1);
		currentProtocol = UrlTransformer.PROTO_FILE;
		return (dFind < 0 ? dFind : minPos + dFind + REG_FIND1.length());
	}

	/**
	 * Find table component for adding to mime html add link protocol for him.
	 * 
	 * @param html
	 * @param minPos
	 * @return
	 */
	protected int findTable(String html, int minPos) {
		String tHtml = html.substring(minPos);
		int dFind = -1;
		int dFind1 = tHtml.indexOf(REG_TABLE1);
		if (dFind1 > -1) {
			if (dFind == -1) {
				dFind = dFind1;
			} else if (dFind > dFind1) {
				dFind = dFind1;
			}
		}
		int dFind2 = tHtml.indexOf(REG_TABLE2);
		if (dFind2 > -1) {
			if (dFind == -1) {
				dFind = dFind2;
			} else if (dFind > dFind2) {
				dFind = dFind2;
			}
		}
		int dFind3 = tHtml.indexOf(REG_TABLE3);
		if (dFind3 > -1) {
			if (dFind == -1) {
				dFind = dFind3;
			} else if (dFind > dFind3) {
				dFind = dFind3;
			}
		}
		int dFind4 = tHtml.indexOf(REG_TABLE4);
		if (dFind4 > -1) {
			if (dFind == -1) {
				dFind = dFind4;
			} else if (dFind > dFind4) {
				dFind = dFind4;
			}
		}
		return (dFind < 0 ? dFind : minPos + dFind);
	}

	/**
	 * Get complete url for mime.
	 * 
	 * @param html
	 * @param pos
	 * @return
	 */
	protected String getExternalLink(String html, int pos) {
		int dFind = html.substring(pos).indexOf(REG_LINK);
		String sFind = html.substring(pos, pos + dFind);
		String sFind2 = Path.fromOSString(sFind).toString();
		// local file only
		if (!sFind2.startsWith(currentProtocol)) {
			sFind = null;
		}
		return sFind2;
	}

	/**
	 * Find begin of tag.
	 * 
	 * @param html
	 * @param pos
	 * @return
	 */
	protected int findOpen(String html, int pos) {
		int dFind = html.substring(0, pos).lastIndexOf(REG_OPEN);
		return dFind;
	}

	/**
	 * Find end of tag.
	 * 
	 * @param html
	 * @param pos
	 * @return
	 */
	protected int findClose(String html, int pos) {
		int dFind = html.substring(pos).indexOf(REG_CLOSE);
		return dFind + pos + 1;
	}

	/**
	 * add '3D' on attribute in selected text.
	 * 
	 * @param html
	 * @param transform
	 * @return
	 */
	protected String add3D(String html, boolean transform) {
		String s3D = html.replaceAll(REG_3D_ORI, REG_3D_FINAL);
		if (transform) {
			UrlTransformer urlTransformer = protocolMap.get(currentProtocol);
			if (urlTransformer == null) {
				return s3D;
			}
			s3D = urlTransformer.getConvertMhtUrl(s3D);
		}
		return s3D;
	}

	/**
	 * Generate base64 code for an url : image, ...
	 * and make complete mime part for this url.
	 * 
	 * @param urlToConvert
	 * @return
	 */
	protected String generateBase64(String urlToConvert) {
		UrlTransformer urlTransformer = protocolMap.get(currentProtocol);
		String code64 = "";
		String extension = "";
		String coded;
		String mimeUrl;
		String mimeType;
		BufferedInputStream bis;
		int bytes = 0;
		byte[] buffer;

		if (urlTransformer == null) {
			return code64;
		}
		try {
			bis = urlTransformer.getInputStream(urlToConvert);
			if (bis == null) {
				return code64;
			}
			extension = urlTransformer.getExtention();
			bytes = urlTransformer.getSizeOfStream();
			if (bytes <= 0) {
				return code64;
			}
			buffer = new byte[bytes];
			bis.read(buffer);
			bis.close();
			// base64
			coded = Base64.encode(buffer);
			mimeUrl = urlTransformer.getConvertMhtUrl(urlToConvert);
			mimeType = MimeTypes.getMimeTypefromExtension(extension);
			// extension
			fileExtensions.add(extension);

			code64 = MIME_SEP + MHT_IDF + LINE_SEP + MIME_RESSOURCE_TYPE + " "
					+ mimeType + LINE_SEP + MIME_RESSOURCE_TRANSFERT + LINE_SEP
					+ MIME_RESSOURCE_LOCATION + " " + mimeUrl + LINE_SEP
					+ LINE_SEP + coded + LINE_SEP + LINE_SEP;

		} catch (IOException e) {
			ILogger logger = GendocServices.getDefault().getService(
					ILogger.class);
			logger.log("Unable to read stream", Status.ERROR);
		} catch (UnknownMimeTypeException e) {
			ILogger logger = GendocServices.getDefault().getService(
					ILogger.class);
			logger.log("Unable to find mime type of " + extension, Status.ERROR);
		}
		return code64;
	}

	public Set<String> getFileExtensions() {
		return fileExtensions;
	}

	public String getBeginPart() {
		return beginPart;
	}

	public String getEndPart() {
		return endPart;
	}

}
