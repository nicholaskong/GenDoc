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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.gendoc.document.parser.Activator;
import org.eclipse.gendoc.document.parser.documents.AbstractZipDocument;
import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.documents.helper.DOCXHelper;
import org.eclipse.gendoc.document.parser.documents.helper.OfficeHelper;
import org.eclipse.gendoc.document.parser.documents.helper.XMLHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Class DocxDocument.
 * 
 * @author tristan.faure@atosorigin.com
 */
public class DocxDocument extends AbstractZipDocument
{
    /**
     * The style xml file
     */
    private XMLParser style = null;
    
    private DocxDocument subDoc = null;

    /**
     * Default constructor.
     * 
     * @param document the document
     * @throws IOException
     */
    public DocxDocument(File document) throws IOException
    {
        this(document, null);
    }

    public DocxDocument(File documentFile, Map<Document.CONFIGURATION, Boolean> configuration) throws IOException
    {
        this(documentFile.toURI().toURL(), configuration);

    }

    public DocxDocument(URL documentFile, Map<CONFIGURATION, Boolean> configuration)
    {
        super(documentFile, configuration);
        style = new XMLParser(getUnzipper().getFile(DOCXHelper.STYLE_FILE_NAME));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.topcased.doc2model.documents.Document#getStyle()
     */
    public String getStyle()
    {
        String tagValue = XMLHelper.getTagValueWithoutNamespace(getXMLParser().getCurrentNode());
        if (DOCXHelper.WORD_STYLE.equals(tagValue))
        {
            if (DOCXHelper.getNodeForStyleNode(getXMLParser().getCurrentNode()) != null)
            {
                if (DOCXHelper.isNotAModification(DOCXHelper.getNodeForStyleNode(getXMLParser().getCurrentNode())))
                {
                    String styleName = getXMLParser().getCurrentNode().getAttributes().getNamedItem(DOCXHelper.WORD_STYLE_ATTRIBUTE).getNodeValue();
                    // check if the style is not defined in styles
                    return DOCXHelper.getStringStyleForStyleNodeInStyleFile(styleName, style);
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean next() {
        if (subDoc == null)
        {
            Node node = getXMLParser().getCurrentNode();
            if (node != null && DOCXHelper.WORD_SUB_DOC.equals(node.getNodeName()))
            {
                NamedNodeMap attributes = node.getAttributes();
                if (attributes == null)
                {
                	return super.next();
                }
				Node namedItem = attributes.getNamedItem(DOCXHelper.WORD_SUB_DOC_ID);
				if (namedItem == null)
				{
					return super.next();
				}
				String id = namedItem.getNodeValue();
                String subDocFile = OfficeHelper.retrieveRelationshipSubDocument(getUnzipper(), 
                        DOCXHelper.RELATIONSHIPS_SUB_DOCUMENTS,  
                        DOCXHelper.DOCUMENT_RELS_FILE_NAME, 
                        id);
                try
                {
                    subDoc = (DocxDocument) DocxFactory.getInstance().loadDocument(getDocumentURL().toURI().resolve(subDocFile).toURL());
                }
                catch (MalformedURLException ex)
                {
                    Activator.log(ex);
                }
                catch (URISyntaxException ex) {
                    Activator.log(ex);
                }
            }
            else {
                return super.next();
            }
        }
        else {
            boolean next = subDoc.next();
            if (!next){
                subDoc = null ;
                return super.next();
            }
            else {
                return next;
            }
        }
        return true;
    }
    
	@Override
    public XMLParser getXMLParser()
    {
	    if (subDoc != null){
	        return subDoc.getXMLParser();
	    }
        return super.getXMLParser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.topcased.doc2model.documents.Document#getTextCorrespondingToCurrentStyle ()
     */
    public String getTextCorrespondingToCurrentStyle()
    {
        String result = null;
        if (getStyle() != null)
        {
            return DOCXHelper.getTextNodeForStyleNode(getXMLParser().getCurrentNode());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.topcased.doc2model.documents.Document#getText()
     */
    public String getText()
    {
        StringBuffer result = new StringBuffer("");
        Node n = getXMLParser().getCurrentNode();
        if (DOCXHelper.WORD_STYLE_PARAGRAPH_W.equals(n.getNodeName()))
        {
            Node tmp = XMLHelper.next(n);
            while (tmp != null && !DOCXHelper.WORD_STYLE_PARAGRAPH_W.equals(tmp.getNodeName()))
            {
                if (DOCXHelper.WORD_STYLE_TEXT_W.equals(tmp.getNodeName()))
                {
                    result.append(tmp.getTextContent());
                }
                tmp = XMLHelper.next(tmp);
            }
        }
        return result.toString();
        // return getXmlParser().getCurrentNode().getNodeValue();
    }

    public Collection<XMLParser> getXmlParsers(CONFIGURATION idForDocument)
    {
        Collection<XMLParser> parsers = new LinkedList<XMLParser>();
        switch (idForDocument)
        {
            case content:
                XMLParser p = new XMLParser(getUnzipper().getFile(DOCXHelper.CONTENTS_FILE_NAME), idForDocument);
                parsers.add(p);
                break;
            case footer:
                OfficeHelper.fillCollection(getUnzipper(), parsers, DOCXHelper.RELATIONSHIPS_FOOTER, idForDocument, DOCXHelper.DOCUMENT_RELS_FILE_NAME);
                break;
            case header:
                OfficeHelper.fillCollection(getUnzipper(), parsers, DOCXHelper.RELATIONSHIPS_HEADER, idForDocument, DOCXHelper.DOCUMENT_RELS_FILE_NAME);
                break;
            case comment:
                OfficeHelper.fillCollection(getUnzipper(), parsers, DOCXHelper.RELATIONSHIPS_COMMENTS, idForDocument, DOCXHelper.DOCUMENT_RELS_FILE_NAME);
                break;
            default:
        }
        return parsers;
    }

    public Object get(PROPERTY property)
    {
        switch (property)
        {
            case style:
                return getStyle();
            case text:
                return getText();
            default:
                return null;
        }
    }
}
