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
package org.eclipse.gendoc.document.parser.xlsx.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.gendoc.document.parser.xlsx.XLSXNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathXlsxUtils {
    private static DocumentBuilderFactory DOM_FACTORY = DocumentBuilderFactory.newInstance();
	private static XPath xpath;
	static {
		xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XLSXNamespaceContext());	    
	    DOM_FACTORY.setNamespaceAware(true);
	}
	
	public static XPathExpression compile(String expr) throws XPathExpressionException {
		return xpath.compile(expr);
	}
	
	public static Node evaluateNode(Node contextNode, String expr) throws XPathExpressionException {
		return (Node)xpath.evaluate(expr, contextNode, XPathConstants.NODE);
	}

	public static NodeList evaluateNodes(Node contextNode, String expr) throws XPathExpressionException {
		return (NodeList)xpath.evaluate(expr, contextNode, XPathConstants.NODESET);
	}

	public static String evaluateText(Node contextNode, String expr) throws XPathExpressionException {
		return (String)xpath.evaluate(expr, contextNode, XPathConstants.STRING);
	}

	public static List<String> evaluateValues(Node contextNode, String expr) throws XPathExpressionException {
		NodeList nl = evaluateNodes(contextNode, expr);
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0;i<nl.getLength(); i++)
			values.add(nl.item(i).getTextContent());
		return values;
	}

	public static List<Integer> evaluateInts(Node contextNode, String expr) throws XPathExpressionException {
		NodeList nl = evaluateNodes(contextNode, expr);
		ArrayList<Integer> values = new ArrayList<Integer>();
		for (int i=0;i<nl.getLength(); i++) {
			String str = nl.item(i).getTextContent();
			try {
				values.add(Integer.valueOf(str));
			} catch (NumberFormatException e) {}
		}
		return values;
	}

	public static int evaluateMax(Node contextNode, String expr) throws XPathExpressionException {
		NodeList nl = evaluateNodes(contextNode, expr);
		int max = Integer.MIN_VALUE;		
		for (int i=0;i<nl.getLength(); i++) {
			String str = nl.item(i).getTextContent();
			try {
				max = Math.max(max,Integer.valueOf(str));
			} catch (NumberFormatException e) {}
		}
		return max;
	}

	public static int evaluateNumber(Node contextNode, String expr, int defval) throws XPathExpressionException {
		Number number = (Number)xpath.evaluate(expr, contextNode, XPathConstants.NUMBER);
		return number == null ? defval : number.intValue(); 		
	}

	public static long evaluateNumber(Node contextNode, String expr, long defval) throws XPathExpressionException {
		Number number = (Number)xpath.evaluate(expr, contextNode, XPathConstants.NUMBER);
		return number == null ? defval : number.longValue(); 		
	}


	public static float evaluateNumber(Node contextNode, String expr, float defval) throws XPathExpressionException {
		Number number = (Number)xpath.evaluate(expr, contextNode, XPathConstants.NUMBER);
		return number == null ? defval : number.floatValue(); 		
	}

	public static double evaluateNumber(Node contextNode, String expr, double defval) throws XPathExpressionException {
		Number number = (Number)xpath.evaluate(expr, contextNode, XPathConstants.NUMBER);
		return number == null ? defval : number.doubleValue(); 		
	}
	
	public static Element evaluateFirstOf(Node contextNode, String expr, String... childs) throws XPathExpressionException {
		Element el = (expr == null || expr.equals(".")) ? 
			(Element)contextNode : 
			(Element)evaluateNode(contextNode, expr);
		if (el == null) {
			return null;
		}
		
		HashSet<String> s = new HashSet<String>(Arrays.asList(childs));
		NodeList nl = el.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			Node n = nl.item(i);
			if (!(n instanceof Element))
				continue;
			
			if (s.contains(n.getNodeName()))
				return (Element)n;
		}
		return null;
	}
	
	public static Node parserXmlFragment(String xmlFragment) throws ParserConfigurationException, SAXException, IOException {
		boolean createFragment = false;
		DocumentBuilder builder= DOM_FACTORY.newDocumentBuilder();
	    if (!xmlFragment.startsWith("<?xml ")) {
	    	xmlFragment = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><documentFragment "+XLSXNamespaceContext.PREFIX_MAPPING+">"+xmlFragment+"</documentFragment>";
	    	createFragment = true;
	    }
	    
	    Document document = builder.parse(new ByteArrayInputStream(xmlFragment.getBytes("UTF-8")));
	    Element rootElement = document.getDocumentElement();
	    
	    if (!createFragment) 
	    	return rootElement;
	    
    	NodeList children = rootElement.getChildNodes();
    	if (children.getLength() == 1) {
    		return children.item(0);
    	}
    	
    	DocumentFragment frag = document.createDocumentFragment();
    	for (int i=0; i<children.getLength(); i++) {
    		frag.appendChild(children.item(i));
    	}
    	return frag;
	}
	
	public static String getNodeXPath(Element el) {		
		StringBuffer buf = new StringBuffer();
		Node parent = el.getParentNode();
		while (parent != null && parent instanceof Element) {
			NodeList nl = parent.getChildNodes();
			int index = 0;
			for (int i=0; i<nl.getLength(); i++) {
				Node n = nl.item(i); 
				if (el.getNodeName().equals(n.getNodeName()))
					index++; // First index is 1						
				if (n == el)
					break;
			}
			String prefix = XLSXNamespaceContext.INSTANCE.getPrefix(el.getNamespaceURI());
			buf.insert(0,String.format("/%s:%s[%d]", prefix, el.getLocalName(), index));
			el = (Element)parent;
			parent = el.getParentNode();
		}
		buf.insert(0,"/"+XLSXNamespaceContext.INSTANCE.getPrefix(el.getNamespaceURI())+":"+el.getLocalName());
		return buf.toString();
	}
	

}

