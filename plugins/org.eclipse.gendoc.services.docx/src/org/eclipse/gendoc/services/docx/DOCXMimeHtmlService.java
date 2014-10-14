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
package org.eclipse.gendoc.services.docx;

import org.eclipse.gendoc.documents.MimeHtmlService;

public class DOCXMimeHtmlService extends MimeHtmlService {

	public DOCXMimeHtmlService() {
		super();
	}

	public String convertToMimeHtml(String sourceHtml) {
		String tempHtml = sourceHtml;
		String tempHtml1 = "";
		String tempHtml2 = "";
		String finalHtml = null;
		String binHtml = "";
		String extLink = null;
		String code64;
		int posLink = 0;
		int posOpen = -1;
		int posClose = -1;

		if (sourceHtml == null || sourceHtml.equals("")) {
			return finalHtml;
		}
		while (posLink < tempHtml.length()
				&& (posLink = findExternalLink(tempHtml, posLink)) > -1) {

			extLink = getExternalLink(tempHtml, posLink);
			if (extLink == null) {
				break;
			}
			posOpen = findOpen(tempHtml, posLink);
			posClose = findClose(tempHtml, posLink);
			tempHtml1 = tempHtml.substring(posOpen, posClose);
			tempHtml2 = add3D(tempHtml1, true);
			if (tempHtml1.equals(tempHtml2)) {
				posLink = posClose;
				continue;
			}
			code64 = generateBase64(extLink);

			if (code64.equals("")) {
				posLink = posClose;
			} else {
				binHtml = binHtml + code64;

				posLink = posClose + tempHtml2.length() - tempHtml1.length();
				tempHtml = tempHtml.replaceFirst(tempHtml1, tempHtml2);
			}
		}

		if (tempHtml.equals(sourceHtml) || binHtml == null
				|| binHtml.equals("")) {
			return finalHtml;
		}

		beginPart = MIME_VERSION + LINE_SEP + MIME_CONTENT + "\"" + MHT_IDF
				+ "\"" + LINE_SEP + LINE_SEP + MIME_SEP + MHT_IDF + LINE_SEP
				+ MIME_HTML_TYPE + LINE_SEP + MIME_HTML_TRANSFERT + LINE_SEP
				+ LINE_SEP + MIME_DOCTYPE + LINE_SEP;
		endPart = LINE_SEP + binHtml + MIME_SEP + MHT_IDF + MIME_SEP;
		tempHtml = tempHtml.replace(MIME_HEAD, MIME_META);
		finalHtml = tempHtml;

		posLink = 0;
		while (posLink<tempHtml.length() && (posLink = findTable(tempHtml, posLink)) > -1) {
			posClose = findClose(tempHtml, posLink);
			tempHtml1 = tempHtml.substring(posLink, posClose);
			tempHtml2 = add3D(tempHtml1, false);
			if (tempHtml1.equals(tempHtml2)) {
				posLink = posClose;
				continue;
			}
			tempHtml = tempHtml.replaceFirst(tempHtml1, tempHtml2);
			posLink = posClose;
		}
		finalHtml = tempHtml;

		return finalHtml;

	}

}
