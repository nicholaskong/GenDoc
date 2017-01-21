package org.eclipse.gendoc.services.xlsx.tests.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.gendoc.document.parser.documents.Unzipper;
import org.eclipse.gendoc.document.parser.documents.XMLParser;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXNamespaceContext;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

public class XlsxVerifyHelper {
	public XlsxVerifyHelper(String xlsxFilePath) throws IOException, ParserConfigurationException {
		this.xlsxFilePath = xlsxFilePath; 
		unzipper = new Unzipper(new File(xlsxFilePath));
		unzipper.unzip();

		xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XLSXNamespaceContext());
		
		parsers = new HashMap<String, XMLParser>();
		try {
			verifyXmls(URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(new URL(
					 "platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/resources/schemas")))));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public void dispose() {
		if (unzipper != null) {
			unzipper.clean();
			unzipper = null;
		}
	}
	
	public void verifyXmls(final File schemasLoc) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", 
				"http://www.w3.org/2001/XMLSchema");
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", 
				schemasLoc.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".xsd");
					}
				}));				
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {			
			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				File f = null;
				if (systemId != null) {
					int index = systemId.lastIndexOf('/');
					if (index >= 0)
						f = new File(schemasLoc,systemId.substring(index+1));
					else
						f = new File(schemasLoc,systemId);
				} else { 			 					
					return null;
				}
				
				return new InputSource(new FileInputStream(f));
			}
		});
		
		builder.setErrorHandler(new ErrorHandler() {			
			@Override
			public void warning(SAXParseException exception) throws SAXException {
				warnings.add(exception);
			}
			
			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				errors.add(exception);
			}
			
			@Override
			public void error(SAXParseException exception) throws SAXException {
				errors.add(exception);
			}
		});
		verifyXmlsImpl(unzipper.getUnzipDocumentFile(), builder);
	}
	
	private void verifyXmlsImpl(File dir, DocumentBuilder builder) {
		File[] files = dir.listFiles(new FileFilter() {			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || 
					   pathname.getName().endsWith(".xml") ||
					   pathname.getName().equals(".xml.rels");
			}
		});
		
		if (files == null)
			return;
		
		for (File f : files) {
			String relPath = f.getPath().replace(
					unzipper.getUnzipDocumentFile().getPath(), "").
					replace(File.separator, "/");
			if (f.isDirectory()) {
				verifyXmlsImpl(f, builder);
			} else {			
				try {
					InputStream in = new FileInputStream(f);
					warnings = new ArrayList<SAXException>();
					errors = new ArrayList<SAXException>();
					Document doc = builder.parse(new InputSource(in));
					filterErrorsFromIgnorablesNS(doc,errors);
					TestCase.assertEquals("XML parsing errors '"+relPath+"': " + Arrays.toString(errors.toArray()), 0, errors.size());
					TestCase.assertEquals("XML parsing warnings: '"+relPath+"'" + Arrays.toString(warnings.toArray()), 0, warnings.size());
				} catch (Exception e) {				
					TestCase.fail(String.format("Fail verifying XLSX content '%s': %s",relPath, e.getMessage()));
				}
			}			
		}
	}
	
	private void filterErrorsFromIgnorablesNS(Document doc, List<SAXException> errors) {
		if (errors.isEmpty())
			return;
		String text = doc.getDocumentElement().getAttributeNS("http://schemas.openxmlformats.org/markup-compatibility/2006","Ignorable");
		if (text.isEmpty())
			text = "";
		String[] ignorables = ("xml mc "+text).split(" ");  
		for (int i=0; i<ignorables.length; i++) {
			Iterator<SAXException> it = errors.iterator();
			while (it.hasNext()) {
				SAXException e = it.next();
				String str = e.getMessage();
				Pattern p = Pattern.compile("\\'"+ignorables[i]+"[a-zA-Z0-9]*\\:");
				Matcher m = p.matcher(str);
				if (m.find()) {
					it.remove();
				}
			}
		}
	}
	
	public void verifyCellEqual(String cell, String file, String expected) {
		CellRef ref = new CellRef(cell);
		verifyEqual(cell, file, "//:sheetData/:row[@r='"+(ref.getRow()+1)+"']/:c[@r='"+cell+"']", expected);
	}
	
	public void verifyCellNotEqual(String cell, String file, String expected) {
		CellRef ref = new CellRef(cell);
		verifyNotEqual(cell, file, "//:sheetData/:row[@r='"+(ref.getRow()+1)+"']/:c[@r='"+cell+"']", expected);
		
	}

	public void verifyEqual(String message, String file, String reg, String expected) {
		try {
			verify(message, 0, file, reg, expected);
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public void verifyNotEqual(String message, String file, String reg, String expected) {
		try {
			verify(message, 1, file, reg, expected);
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void verifyNull(String message, String file, String reg) {
		try {
			verify(message, 2, file, reg, null);
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}		
	}
	
	public void verifyNotNull(String message, String file, String reg) {
		try {
			verify(message, 3, file, reg, null);
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}		
	}
	
	private void verify(String message, int op, String file, String reg, Object expected) throws XPathExpressionException {
		XMLParser parser = parsers.get(file);
		if (parser == null) {
			parser = new XMLParser(new File(unzipper.getUnzipDocumentFile(),file));
			parsers.put(file,parser);
		}
			
		Object value = (expected instanceof String) ? 
				xpath.evaluate(reg, parser.getDocument()).trim() :
				xpath.evaluate(reg, parser.getDocument(), XPathConstants.NODE);
				
		switch (op) {
			case 0:
				Assert.assertEquals(message,expected,value); 
				break;
			case 1:
				Assert.assertNotEquals(message,expected,value);
				break;
			case 2:
				Assert.assertNull(message,value);
				break;
			case 3:
				Assert.assertNotNull(message,value);
				break;
			default:
		}
		
	}
	
	private String xlsxFilePath;
	private Unzipper unzipper;
	private XPath xpath;	
	private Map<String,XMLParser> parsers;
	
	private List<SAXException> warnings; 
	private List<SAXException> errors; 
}
