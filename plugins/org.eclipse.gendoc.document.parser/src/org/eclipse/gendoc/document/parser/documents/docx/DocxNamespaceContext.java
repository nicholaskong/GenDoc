/*****************************************************************************
 * Copyright (c) 2008 Atos Origin.
 * 
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Tristan Faure (Atos Origin) tristan.faure@atosorigin.com -
 * Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.document.parser.documents.docx;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * The Class ODTNamespaceContext.
 * 
 * @author tristan.faure@atosorigin.com
 */
public class DocxNamespaceContext implements NamespaceContext
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix)
    {

        if (prefix == null)
        {
            throw new NullPointerException("Null prefix");
        }
        else if ("w".equals(prefix))
        {
            return "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
        }
        else if ("r".equals(prefix))
        {
            return "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        }
        return XMLConstants.NULL_NS_URI;

    }

    // This method isn't necessary for XPath processing.
    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri)
    {
        throw new UnsupportedOperationException();

    }

    // This method isn't necessary for XPath processing either.
    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator getPrefixes(String uri)
    {
        throw new UnsupportedOperationException();

    }

}
