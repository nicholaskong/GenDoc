/*****************************************************************************
 * Copyright (c) 2008 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Tristan Faure (Atos Origin) tristan.faure@atosorigin.com 
 * 		- Initial API and implementation
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com 
 *  	- Adding other parts types, namespaces, prefixes 
 *
 *****************************************************************************/
package org.eclipse.gendoc.document.parser.xlsx;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;

public class XLSXNamespaceContext implements NamespaceContext
{
	private static final String[] MAPPING = new String[] {
		"", XLSXHelper.WORKSHEET_NAMESPACE,
		"mc", "http://schemas.openxmlformats.org/markup-compatibility/2006",		
		"xdr", XLSXHelper.DRAWING_NAMESPACE,		
		"a", "http://schemas.openxmlformats.org/drawingml/2006/main",
		"gendoc", XLSXHelper.GENDOC_NS,
		"ct", "http://schemas.openxmlformats.org/package/2006/content-types",
		"rel", XLSXHelper.PACKAGE_RELATIONSHIPS_NAMESPACE,
		"r", XLSXHelper.RELATIONSHIPS_NAMESPACE 
	};
	public static final String PREFIX_MAPPING = getPrefixMapping(); 
	
	private static final String getPrefixMapping() {
		StringBuffer buf = new StringBuffer();
		buf.append("xmlns=\"").append(MAPPING[1]).append("\" ");
		
		for (int i=2; i<MAPPING.length; i+=2) {
			buf.append("xmlns:").append(MAPPING[i]).append("=\"").append(MAPPING[i+1]).append("\" ");
		}
		
		return buf.toString().trim();
	}
	
	public static final XLSXNamespaceContext INSTANCE = new XLSXNamespaceContext();
	
    public String getNamespaceURI(String prefix)
    {
    	if (prefix == null || prefix.equals(""))
        {
            return MAPPING[1]; 
        }

    	for (int i=2; i<MAPPING.length; i+=2) {
			if (prefix.equals(MAPPING[i]))
				return MAPPING[i+1];
		}

    	return XMLConstants.NULL_NS_URI;

    }

    public String getPrefix(String uri)
    {
    	for (int i=0; i<MAPPING.length; i+=2) {
			if (uri.equals(MAPPING[i+1]))
				return MAPPING[i];
		}
    	return null;
    }

    @SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String uri)
    {
    	return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return pos < MAPPING.length;
			}

			@Override
			public String next() {
				String str = MAPPING[pos];
				pos += 2;
				return str;
			}
			
			private int pos = 0; 
		};
    }

}
