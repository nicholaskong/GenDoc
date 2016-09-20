/*****************************************************************************
 * (c) Copyright 2016 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.documents.metadata.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.documents.Document.CONFIGURATION;
import org.eclipse.gendoc.documents.metadata.IDocumentMetadataService;
import org.eclipse.gendoc.process.AbstractStepProcess;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IRegistryService;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.services.exception.InvalidTemplateParameterException;

/**
 * The Class DocumentMetadataProcess implements the process that inject configuration parameters or 
 * values defined in the {@link IRegistryService} in the metadata properties values.<br/>
 * Any string of the type ${&lt;key&gt;} in the value of a metadata property, will be replaced with the
 * value of the attribute with name equal to &lt;key&gt; in the {@link IRegistryService}. In order to be able to
 * extract information from the model and make it available in the {@link IRegistryService} using the
 * <code>gPut(...)</code> query, the process runs after the 'generate' process before save the 
 * output document.     
 */
public class DocumentMetadataProcess extends AbstractStepProcess {
	 
	/** The pattern param. */
	private static Pattern PATTERN_PARAM = Pattern.compile("\\$\\{[a-zA-Z0-9_]+\\}");
	 
	 /**
	 * Instantiates a new document metadata process.
	 */
	public DocumentMetadataProcess() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.process.AbstractStepProcess#step(org.eclipse.gendoc.document.parser.documents.Document)
	 */
	@Override
	protected void step(Document document) throws GenDocException {
		if (document == null) {
			this.worked(1);
			return;
		}

		do {
			if (document.getXMLParser() == null || document.getXMLParser().getKind() != CONFIGURATION.metadata)
				continue;

			IDocumentMetadataService propertyService = ((IDocumentMetadataService)GendocServices.getDefault().getService(IDocumentMetadataService.class));
			if (propertyService != null) {
				boolean modify = false;
				List<String> propertyNames = propertyService.getMetadataProperties(document);
				for (String prop : propertyNames) {
					String value = propertyService.getMetadataValue(document, prop);
					String newValue = replaceValue(value);
					if (newValue != null && newValue.equals(value)) {
						continue;
					}
					propertyService.setMetadataValue(document, prop, newValue);
					modify = true;
				}
				if (modify) {
					propertyService.saveMetadata(document);
				}
				
			}		
		} while (document.jumpToNextFile());
		
		document.getXMLParser().setCurrentNode(null);
		this.worked(1);
	}
	
    /**
	 * Replace the string in the form ${&lt;key&gt;} with the  corresponding value of the <key> 
	 * entry in the {@link IRegistryService} if the entry contains a {@link String} value.
	 *
	 * @param value the input string with the ${&lt;key&gt;} strings to be replaced
	 * @return the output string with the replaced content.
	 */
    public String replaceValue(String value) 
    {
        if (value == null) {
            return null;
        }
		
        IRegistryService regService = ((IRegistryService)GendocServices.getDefault().getService(IRegistryService.class));
        Matcher m = PATTERN_PARAM.matcher(value);
        StringBuffer buffer = new StringBuffer();
        int index = 0; // Current index
        while (m.find())
        {
            buffer.append(value.substring(index, m.start()));
            String paramName = value.substring(m.start() + 2, m.end() - 1);
            Object objRegValue = regService.get(paramName);
            if (objRegValue instanceof String)
            {
            	buffer.append(objRegValue);
            } else {
            	buffer.append("<<Cannot resolve parameter '"+paramName+"' in the registry>>");
            }
            index = m.end();
        }
        buffer.append(value.substring(index, value.length()));
        return buffer.toString();
    }
}
