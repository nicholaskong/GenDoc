package org.eclipse.gendoc.tags.html.impl;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.tags.html.IHtmlService;

public class DefaultHtmlService extends AbstractService implements IHtmlService {

	@Override
	public String convert(String html) {
		// TODO: Strip the html tags, and get just the text content.
		// Use tidy for that.
		return html;
	}

	@Override
	public void setVersion(String version) {
	}

	@Override
	public void setInTable(String inTable) {
	}

	@Override
	public void setIncludePic(String includePic) {
	}

	@Override
	public void setFormat(String format) {
	}

	@Override
	public void addAdditionalStyles(Document document) {
	}

}
