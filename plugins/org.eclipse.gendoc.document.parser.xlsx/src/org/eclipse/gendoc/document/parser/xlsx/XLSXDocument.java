/*****************************************************************************
 * Copyright (c) 2009 ATOS ORIGIN INTEGRATION.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Tristan FAURE (ATOS ORIGIN INTEGRATION) tristan.faure@atosorigin.com - Initial API and implementation
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Adding handling of XLSX parts. 
 *
 *****************************************************************************/
package org.eclipse.gendoc.document.parser.xlsx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.gendoc.document.parser.documents.AbstractZipDocument;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.documents.helper.OfficeHelper;
import org.eclipse.gendoc.document.parser.documents.helper.XMLHelper;
import org.eclipse.gendoc.document.parser.xlsx.helper.XLSXHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Class XLSXDocument.
 */
public class XLSXDocument extends AbstractZipDocument
{
    /** The name of nodes indicating a new sheet */
    private static final String SHEET_NODE = "sheet";//$NON-NLS-1$

    /** The name of nodes indicating a new cell value */
    private static final String NEW_CELL_NODE = "si"; //$NON-NLS-1$

    /** The name of nodes indicating a new textual value (part of a cell value) */
    private static final String TEXT_VALUE_NODE = "t";//$NON-NLS-1$

    private String[] strings = null;
    private NodeList[] nodes = null;
	private HashMap<String, XMLParser> subdocuments = new HashMap<String,XMLParser>();

    public XLSXDocument(File documentFile) throws IOException
    {
        this(documentFile.toURI().toURL(), null);
    }

    public XLSXDocument(File documentFile, Map<CONFIGURATION, Boolean> configuration) throws IOException
    {
        this(documentFile.toURI().toURL(), configuration);

    }

    public XLSXDocument(URL documentFile, Map<CONFIGURATION, Boolean> configuration)
    {
        super(documentFile,configuration);
    }
 
    public String getSharedString(int ref) {
    	if (ref < 0 || ref > strings.length)
    		throw new IllegalArgumentException("Shared String with index '"+ref+"' does not exists.");
    	return strings[ref];
    }
    
    public NodeList getSharedStringNodes(int ref) {
    	if (ref < 0 || ref > strings.length)
    		throw new IllegalArgumentException("Shared String with index '"+ref+"' does not exists.");
    	return nodes[ref];
    }

    /**
     * Fill the array of shared strings to get them later
     */
    private void initStrings()
    {
        XMLParser parser = new XMLParser(getUnzipper().getFile(XLSXHelper.SHARED_STRING_FILE));
        Node item = parser.getCurrentNode().getAttributes().getNamedItem(XLSXHelper.ATTRIBUTE_COUNT);
        if (item == null)
        {
            return;
        }
        int nb = Integer.valueOf(item.getTextContent());
        strings = new String[nb];
        nodes = new NodeList[nb];
        
        int i = -1;
        do
        {
            String nodeName = parser.getCurrentNode().getNodeName();
            if (NEW_CELL_NODE.equals(nodeName))
            {
                i++;
                nodes[i] = parser.getCurrentNode().getChildNodes();
            }
            if (TEXT_VALUE_NODE.equals(nodeName))
            {
                String text = parser.getCurrentNode().getTextContent();
                if (strings[i] != null)
                {
                    strings[i] = strings[i].concat(text);
                }
                else
                {
                    strings[i] = text;
                }
            }
        }
        while (parser.next());

    }

    @Override
    protected Collection<XMLParser> getXmlParsers(CONFIGURATION idForDocument)
    {
    	if (strings == null)
    		initStrings();
        Collection<XMLParser> parsers = new LinkedList<XMLParser>();
        
        switch (idForDocument)
        {
            case content:
            	XLSXParserProvider provider = new XLSXParserProvider(this);
            	XMLParser workbook = new XMLParser(getUnzipper().getFile(XLSXHelper.WORKBOOK_FILE_NAME), idForDocument);
                do
                {
                    if (SHEET_NODE.equals(workbook.getCurrentNode().getNodeName()))
                    {
                        Node item = workbook.getCurrentNode().getAttributes().getNamedItem(XLSXHelper.SHEET_ID);
                        if (item != null)
                        {
                            OfficeHelper.fillCollection(getUnzipper(), provider, parsers, XLSXHelper.WORKSHEET_RELATIONSHIP, idForDocument, XLSXHelper.WORKBOOK_RELS_FILE_NAME, item.getTextContent());
                        }
                    }
                }
                while (workbook.next());
                break;
            case footer:
                break;
            case header:
                break;
            case comment:
                // TODO
            default:
        }
        
        return parsers;
    }

	public String getNextDocumentName(String relpath) {
		File rel = new File(relpath.replace("/", File.separator));
		String[] nameParts = rel.getName().split("\\*");
		File f = new File(getUnzipLocationDocumentFile(),rel.getPath());
		File folder = f.getParentFile();
		int index = 0;
		if (folder.exists()) {
			String names[] = folder.list();			
			for (int i=0; names != null && i<names.length; i++) {
				if (names[i].startsWith(nameParts[0]) && names[i].endsWith(nameParts[1])) {
					try {
						index = Math.max(Integer.valueOf(
								names[i].replace(nameParts[0], "").replace(nameParts[1], "")),index);
					} catch (NumberFormatException e) {}					
				}				
			}
		}
		
		String res = rel.getParent()+File.separator+rel.getName().replace("*",String.valueOf(index+1));
		return res.replace(File.separator, "/");
	}

    public XMLParser createSubdocument(String path, CharSequence content) throws IOException {
		File f = new File(getUnzipLocationDocumentFile(),path.replace("/", File.separator));
		f.getParentFile().mkdirs();
    	FileWriter writer = new FileWriter(f);
    	writer.write(content.toString());
    	writer.flush();
    	writer.close();
    	return getSubdocument(path);
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<XMLParser> getSubdocuments() {
		return (Collection)(subdocuments == null ? 
				Collections.emptyList() : 
				subdocuments.values());
	}

	public XMLParser getSubdocument(String relpath) {
		if (subdocuments == null)
			subdocuments = new HashMap<String, XMLParser>();
		relpath = relpath.replace(File.separatorChar, '/');
		if (!relpath.startsWith("/"))
			relpath = "/"+relpath;
		XMLParser p = subdocuments.get(relpath);
		if (p != null)
			return p;
		
		File f = new File(getUnzipLocationDocumentFile(),relpath.replace("/", File.separator));
		if (f.exists()) {
			p = new XMLParser(f);
			subdocuments.put(relpath, p);
		}
		return p;
	}   
    
    public String getStyle()
    {
        return "";
    }

    public String getText()
    {
    	Node node = getXMLParser().getCurrentNode();
    	if (XLSXHelper.ROW.equals(node.getNodeName())) {
        	StringBuffer buf = new StringBuffer();
        	getTextRow(node, buf);
        	return buf.toString();
    	} else if (XLSXHelper.CELL.equals(node.getNodeName())) {
        	StringBuffer buf = new StringBuffer();
        	getTextCell(node, buf);
        	return buf.toString();
    	} else 
    		return "";
    }
    
    public Node getTextRow(Node n, StringBuffer buf) {
    	n = XMLHelper.next(n);
        while (n != null && XLSXHelper.CELL.equals(n.getNodeName())) {
        	n = getTextCell(n,buf);
        }
        return n;
    }

    // TODO: Refactor it to use getCellText() and getRowText()
    public Node  getTextCell(Node n, StringBuffer buf) {
    	String result = "";
        Node item = n.getAttributes().getNamedItem(XLSXHelper.CELL_TYPE);
        n = XMLHelper.next(n);
        while (n != null)
        {
        	if (XLSXHelper.CELL_VALUE.equals(n.getNodeName())) {
                String value = n.getTextContent();
                if (item != null && XLSXHelper.CELL_VALUE_SHARED_STRING.equals(item.getTextContent()))
                {
                    try
                    {
                        int index = Integer.valueOf(value);
                        if (index < strings.length)
                        {
                            result = strings[index];
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        // DO NOTHING
                    }
                }
                else
                {
                    result = value;
                }
                break;            		
        	}
	        else if (XLSXHelper.CELL_INLINE_STRING.equals(n.getNodeName())) {
	        	Node is = n; 
	        	n = XMLHelper.next(n);
	            StringBuffer s = new StringBuffer("");
	            while (n != null && XMLHelper.isAncestor(n,is)) {
	                if (XLSXHelper.CELL_INLINE_STRING_TEXT.equals(n.getNodeName()))
	                {
	                    s.append(n.getTextContent());
	                }
	            	n = XMLHelper.next(n);
	            }
	            result = s.toString();
	            break;
	        } else {
	        	if (XLSXHelper.ROW.equals(n.getNodeName()) || XLSXHelper.CELL.equals(n.getNodeName()))
	        		break;
	        	n = XMLHelper.next(n);
	        }
        }
        
        buf.append(result.replace("\n", ""));
        return n;
    }

    public String getTextCorrespondingToCurrentStyle()
    {
        return null;
    }

    @Override
    public int getColumnNumber()
    {
        // we init to -1 to not be matched if there is a problem
        int column = -1;
        Node currentNode = getXMLParser().getCurrentNode();
        if (XLSXHelper.CELL.equals(currentNode.getNodeName()))
        {
            Node item = currentNode.getAttributes().getNamedItem("r");
            if (item != null)
            {
                String[] columnArray = item.getTextContent().split("\\d");
                if (columnArray.length > 0)
                {
                    String columnString = columnArray[0];
                    column = getColumnNumber(columnString);
                }
            }
        }
        return column;
    }

    private int getColumnNumber(String columnString)
    {
        if (columnString == null || columnString.length() == 0)
        {
            return -1;
        }
        int finalValue = 0;
        int j = 0;
        for (int i = columnString.length() - 1; i >= 0; i--)
        {
            finalValue += getColumnNumber(columnString.charAt(i)) * Math.pow(26, j);
            j++;
        }
        return finalValue - 1;
    }

    /**
     * Return the column number for a character A ==> 1
     * 
     * @param charAt
     * @return
     */
    private int getColumnNumber(char charAt)
    {
        return charAt - 'A' + 1;
    }

    public Object get(PROPERTY property)
    {
        switch (property)
        {
            case row:
                return getRowNumber();
            case column:
                return getColumnNumber();
            case text:
                return getText();
            default:
                return null;
        }
    }

    private int getRowNumber()
    {
        return 0;
    }

}
