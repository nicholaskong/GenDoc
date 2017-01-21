package org.eclipse.gendoc.services.xlsx.tests.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.gendoc.document.parser.documents.Unzipper;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.documents.Zipper;
import org.eclipse.gendoc.document.parser.xlsx.XLSXNamespaceContext;
import org.eclipse.gendoc.document.parser.xlsx.helper.XPathXlsxUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XlsxTestInputTemplate {	
	public XlsxTestInputTemplate(URL baseTemplate) throws IOException, URISyntaxException {
		this.baseTemplate = baseTemplate;
		tmpLocation = URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(
				new URL("platform:/meta/org.eclipse.gendoc.services.xlsx/"+
	            "test/unzipped/"+baseTemplate.getFile()))));
		tmpLocation.mkdirs();
		unzipper = new Unzipper(baseTemplate,tmpLocation.getAbsolutePath());
		unzipper.unzip();
		parsers = new HashMap<String, XMLParser>();
	}
	
	public String generate(File output) throws TransformerException, IOException {
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT,"no");
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,"no");

		for (XMLParser p : parsers.values()) {
			writeFile(p);
		}
		Zipper zipper = new Zipper(tmpLocation);
		zipper.zip(output.getAbsolutePath());
		deleteFolder(tmpLocation);
		return output.getAbsolutePath();
	}

	public void replaceFile(String xlFile, File inputFile) throws IOException {
		replaceFile(xlFile, new FileInputStream(inputFile));
	}
	
	public void replaceFile(String xlFile, URL inputUrl) throws IOException {
		replaceFile(xlFile, inputUrl.openStream());
	}

	public void replaceFile(String xlFile, InputStream inputStream) throws IOException {		
		writeFile(xlFile, inputStream);
	}

	public void replaceFile(String xlFile, StringBuffer buf) throws IOException {
		XMLParser parser = getParser(xlFile);
		if (parser != null) {
			parsers.remove(xlFile);
		}
		writeFile(xlFile, new ByteArrayInputStream(buf.toString().getBytes()));
	}
	
	public void appendXmlNode(String xlFile, String xpath, CharSequence seq) throws 
		IOException, ParserConfigurationException, SAXException, XPathExpressionException {
	
		XMLParser parser = getParser(xlFile);
		if (parser == null) {
			throw new IllegalArgumentException("File '"+xlFile+"' not found");
		}			
		
		Node parentNode = XPathXlsxUtils.evaluateNode(parser.getDocument(), xpath);
		appendNodeBeforeNode(parser, parentNode, null, XPathXlsxUtils.parserXmlFragment(seq.toString()));
	}
	

	public void appendXmlNodeBefore(String xlFile, String xpath, CharSequence seq) throws 
			IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		
		XMLParser parser = getParser(xlFile);
		if (parser == null) {
			throw new IllegalArgumentException("File '"+xlFile+"' not found");
		}			
		
		Node refNode = XPathXlsxUtils.evaluateNode(parser.getDocument(), xpath);
		appendNodeBeforeNode(parser, refNode.getParentNode(), refNode, XPathXlsxUtils.parserXmlFragment(seq.toString()));
	}
	
	public void replaceXmlNode(String xlFile, String xpath, CharSequence seq) throws 
			IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		XMLParser parser = getParser(xlFile);
		if (parser == null) {
			throw new IllegalArgumentException("File '"+xlFile+"' not found");
		}			
		
		Node currentNode = XPathXlsxUtils.evaluateNode(parser.getDocument(), xpath);
		if (currentNode instanceof Attr) {
			Attr newAttr = (Attr)currentNode.getOwnerDocument().createAttribute(currentNode.getNodeName());
			newAttr.setValue(seq.toString());
			replaceNode(parser, currentNode, newAttr);
		} else if (currentNode instanceof Text) {
			replaceNode(parser, currentNode, currentNode.getOwnerDocument().createTextNode(seq.toString()));			
		} else {
			replaceNode(parser, currentNode, XPathXlsxUtils.parserXmlFragment(seq.toString()));
		}		
	}
	
	public void setXmlAttribute(String xlFile, String xpath, String attr, CharSequence val) throws 
	IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		XMLParser parser = getParser(xlFile);
		if (parser == null) {
			throw new IllegalArgumentException("File '"+xlFile+"' not found");
		}			
		
		Element currentNode = (Element)XPathXlsxUtils.evaluateNode(parser.getDocument(), xpath);
		if (attr.contains(":")) {
			String[] parts = attr.split("\\:");
			String uri = XLSXNamespaceContext.INSTANCE.getNamespaceURI(parts[0]);
			currentNode.setAttributeNS(uri, parts[1], val.toString());			
		} else {
			currentNode.setAttribute(attr, val.toString());
		}
	}

	private void replaceNode(XMLParser parser, Node nodeToReplace, Node n) throws IOException {
		if (nodeToReplace instanceof Attr) {
			Attr curAttr = (Attr)nodeToReplace;
			Element owner = curAttr.getOwnerElement(); 
			owner.removeAttributeNode(curAttr);
			owner.setAttributeNode((Attr)n);
		} else {
			Node pn = nodeToReplace.getParentNode();
			if (pn == null) {
				if (nodeToReplace.getOwnerDocument().getDocumentElement() == nodeToReplace) {
					nodeToReplace.getOwnerDocument().removeChild(nodeToReplace);
					nodeToReplace.getOwnerDocument().appendChild(n);
					return;
				} else {
					throw new IllegalArgumentException("The node to replace has no parent.");
				}
			}
			
			Node nextSibling = nodeToReplace.getNextSibling();
			pn.removeChild(nodeToReplace);
			appendNodeBeforeNode(parser, nextSibling.getParentNode(), nextSibling, n);
		}		
	}

	private void appendNodeBeforeNode(XMLParser parser, Node pn, Node refNode, Node n) throws IOException {
		if (pn == null)
			throw new IllegalArgumentException("No parent node.");

		if (n instanceof DocumentFragment) {
			NodeList nl = n.getChildNodes();
			for (int i=0; i<nl.getLength(); i++) {
				Node newNode = nl.item(i);
				if (newNode.getOwnerDocument() != refNode.getOwnerDocument())
					newNode = refNode.getOwnerDocument().importNode(newNode, true);
				pn.insertBefore(newNode, refNode);
			}
		} else {
			if (n.getOwnerDocument() != pn.getOwnerDocument())
				n = pn.getOwnerDocument().importNode(n, true);
			pn.insertBefore(n, refNode);				
		}	
	}
	
	private XMLParser getParser(String xlFile) {
		if (!xlFile.startsWith("/"))
			xlFile = "/"+xlFile;
		
		XMLParser parser = parsers.get(xlFile);
		if (parser != null)
			return parser;
		
		File f = new File(tmpLocation,xlFile.replace('/', File.separatorChar));
		if (!f.exists())
			return null;
		parser = new XMLParser(f);
		parsers.put(xlFile, parser);
		return parser;
	}
	
/*	private StringBuffer readContent(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		Reader r = new InputStreamReader(in);
		char cs[] = new char[1024];
		int len = r.read(cs);
		buf.append(cs,0,len);
		while (len == 1024) {
			len = r.read(cs);
			buf.append(cs,0,len);			
		}
		return buf;
	}*/
	
	private void writeFile(XMLParser parser) throws IOException, TransformerException {
		FileOutputStream out = new FileOutputStream(parser.getXmlFile());
		transformer.transform(new DOMSource(parser.getDocument()),new StreamResult(out));
		out.flush();
		out.close();
	}
	
	private void writeFile(String xl, InputStream inputStream) throws IOException {
		File f = new File(tmpLocation,xl.replace('/', File.separatorChar));
		f.getParentFile().mkdir();
		FileOutputStream out = new FileOutputStream(f);
		byte[] bytes = new byte[1024];
		int len = 0;
		do {
			len = inputStream.read(bytes);			
			out.write(bytes, 0, len);
		} while (len == 1024);
		out.flush();
		out.close();
	}
	
	private void deleteFolder(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : dir.listFiles()) {
				if (f.isDirectory())
					deleteFolder(f);
				else
					f.delete();
			}
		}
		dir.delete();
	}
	
	private Transformer transformer;
	private URL baseTemplate;
	private File tmpLocation;
	private Unzipper unzipper;
	private HashMap<String, XMLParser> parsers;
}
