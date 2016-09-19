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
package org.eclipse.gendoc.preferences.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.gendoc.preferences.IGendocConfiguration;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The Class GendocPreferenceUtils provides convenient methods
 * to marshall and unmarshall {@link GendocConfiguration} and 
 * {@link IGendocRunner} lists.
 */
public class GendocPreferenceUtils {
	
	/** A Constant document builder to use in the marshall / unmarshall operations. */
	private static final DocumentBuilder DOC_BUILDER = initialize();
	
	/** The Constant XML Transformer used for in preferences. */
	private static final Transformer TRANSFORMER = initializeTr();
	
	/** The Constant XML Transformer used for stand alone documents, such as project configuration files. */
	private static final Transformer TRANSFORMER_NICE = initializeTrNice();

	/**
	 * Initialize a static {@link DocumentBuilder} to be used for the 
	 * as xml parser and xml node factory.
	 *
	 * @return the document builder
	 */
	private static DocumentBuilder initialize() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return null;
		}
	}
	
	/**
	 * Creates a simple XML node to text transformation to generate the textual XML 
	 * representation of a XML DOM node.<br>
	 * The transformed generated is a simple one, with no encoding and no xml directives.
	 *
	 * @return a simple the XML transformer.
	 */
	private static Transformer initializeTr() {
		try {
			return TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			return null;
		}
	}

	/**
	 * Create a nice XML node to text transformation to generate the textual XML 
	 * representation of a XML DOM node.<br>
	 * The transformed generated will generate xml directives with the encoding 
	 * parameter and indentation.
	 *
	 * @return a simple the XML transformer.
	 */
	private static Transformer initializeTrNice() {
		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.STANDALONE, "yes");
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			return tr;
		} catch (TransformerConfigurationException e) {
			return null;
		}
	}

	/**
	 * Read and build a {@link IGendocConfiguration} for the given {@link IProject} from the given {@link InputStream}.
	 *
	 * @param project  the project for which the configuration is loaded.
	 * @param in the input stream from which the configuration is read.
	 * @return a Gendoc configuration with the gendoc templates provided by the given project 
	 * @throws ParseException throws a {@link ParseException} if the given {@link InputStream} does not contain a 
	 *         valid Gendoc configuration xml content.
	 */
	public static IGendocConfiguration unmarshall(IProject project, InputStream in) throws ParseException {
		try {
			Document doc = DOC_BUILDER.parse(in);
			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals("gendoc"))
				throw new ParseException(String.format("Invalid root element '%s'",root.getTagName()),0);

			IGendocConfiguration configuration = new GendocConfiguration(project);

			NodeList templatesEls = root.getElementsByTagName("templates");			
			if (templatesEls.getLength() == 1)
				configuration.setGendocRunners(Arrays.asList(unmarshallTemplatesImpl(project,(Element)templatesEls.item(0))));
			
			return configuration;
		} catch (SAXException e) {
			throw new ParseException(e.getMessage(),0);
		} catch (IOException e) {
			throw new ParseException(e.getMessage(),0);
		}
	}
	
	/**
	 * Read a list of {@link IGendocRunner} from the given {@link InputStream}.
	 *
	 * @param in the input stream from which the configuration is read.
	 * @return a Gendoc configuration with the gendoc templates provided by the given project 
	 * @throws ParseException throws a {@link ParseException} if the given {@link InputStream} does not contain a 
	 *         valid Gendoc configuration xml content.
	 */
	public static IGendocRunner[] unmarshallTemplates(InputStream in) throws ParseException {
		try {
			Document doc = DOC_BUILDER.parse(in);
			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals("templates"))
				throw new ParseException(String.format("Invalid root element '%s'",root.getTagName()),0);

			return unmarshallTemplatesImpl(null, root);
		} catch (SAXException e) {
			throw new ParseException(e.getMessage(),0);
		} catch (IOException e) {
			throw new ParseException(e.getMessage(),0);
		}
	}
	
	/**
	 * Read a list of {@link IGendocRunner} from the given {@link InputStream}.
	 *
	 * @param project  the project for which the configuration is loaded. It can be <code>null</code> to indicate that
	 *        the returning runners are not provided by any project, but by the workspace configuration.
	 * @param templatesEl the XML element used to create the runners.  
	 * @return a Gendoc configuration with the gendoc templates provided by the given project 
	 * @throws ParseException throws a {@link ParseException} if the given {@link InputStream} does not contain a 
	 *         valid Gendoc configuration xml content.
	 */
	private static IGendocRunner[] unmarshallTemplatesImpl(IProject project, Element templatesEl) throws ParseException {
		if (!templatesEl.getTagName().equals("templates"))
			throw new ParseException(String.format("Invalid root element '%s'",templatesEl.getTagName()),0);
		
		List<IGendocRunner> runners = new ArrayList<IGendocRunner>();
		NodeList templateTypes = templatesEl.getElementsByTagName("templateType");
		for (int i=0; i<templateTypes.getLength(); i++) {
			Element templateTypeEl = (Element)templateTypes.item(i);
			PreferenceGendocRunner runner = new PreferenceGendocRunner(project, templateTypeEl.getAttribute("name"));
			runner.setDescription(templateTypeEl.getAttribute("description"));			
			try {
				runner.setPattern(Pattern.compile(templateTypeEl.getAttribute("pattern")));
			} catch (Exception e) {}
			
			NodeList parameters = templateTypeEl.getElementsByTagName("parameter");
			for (int j=0; j<parameters.getLength(); j++) {
				Element parameterEl = (Element)parameters.item(j);
				runner.addParameter(parameterEl.getAttribute("name"), parameterEl.getAttribute("description"));
			}
			
			NodeList templates = templateTypeEl.getElementsByTagName("template");
			for (int j=0; j<templates.getLength(); j++) {
				Element templateEl = (Element)templates.item(j);
				try {
					PreferenceGendocTemplate template = new PreferenceGendocTemplate(
							templateEl.getAttribute("extension"), 
							new URL(templateEl.getAttribute("url")));
					template.setDescription(templateEl.getAttribute("description"));
					template.setModelKey(templateEl.getAttribute("modelKey"));
					template.setOutputKey(templateEl.getAttribute("outputKey"));
					if (templateEl.getAttribute("default").equals("true"))
						runner.addDefaultTemplate(template);
					else
						runner.addTemplate(template);
				} catch (Exception e) {	}
			}	
			
			runners.add(runner);
		}
		return runners.toArray(new IGendocRunner[0]);
	}
	
	/**
	 * Store the given {@link IGendocConfiguration} in the given {@link OutputStream}. 
	 *
	 * @param config the Gendoc configuration to store.
	 * @param out an output stream used to store the Gendoc configuration.
	 * @throws ParseException a TransformerException if the XML can not be written.
	 */
	public static void marshall(IGendocConfiguration config, OutputStream out) throws TransformerException {
		Document doc = DOC_BUILDER.newDocument();
		Element root = (Element)doc.appendChild(doc.createElement("gendoc"));
		marshallTemplatesImpl(config.getGendocRunners().toArray(new IGendocRunner[0]), root);
		TRANSFORMER_NICE.transform(new DOMSource(doc), new StreamResult(out));
	}

	/**
	 * Store the list of {@link IGendocRunner} in the given {@link OutputStream}. 
	 *
	 * @param runners a list of {@link IGendocRunner} to store.
	 * @param out an output stream used to store the given runners.
	 * @throws ParseException a TransformerException if the XML can not be written.
	 */
	public static void marshallTemplates(IGendocRunner[] runners, OutputStream out) throws TransformerException {
		Document doc = DOC_BUILDER.newDocument();
		marshallTemplatesImpl(runners, doc);
		TRANSFORMER.transform(new DOMSource(doc), new StreamResult(out));
	}
	
	/**
	 * Creates the XML structure containing the given list of {@link IGendocRunner} under the given XML node. 
	 *
	 * @param runners a list of {@link IGendocRunner} to store.
	 * @param parent an DOM node where the runners node representation will be created. 
	 * @throws ParseException a TransformerException if the XML can not be written.
	 */
	public static void marshallTemplatesImpl(IGendocRunner[] runners, Node parent) {
		Document doc = parent instanceof Document ? (Document)parent : parent.getOwnerDocument();
		if (runners.length==0)
			return;
		Element templatesEl = (Element)parent.appendChild(doc.createElement("templates"));
		for (IGendocRunner r : runners) {
			Element templateTypeEl = (Element)templatesEl.appendChild(doc.createElement("templateType"));
			templateTypeEl.setAttribute("name", r.getLabel());
			if (r.getPattern() != null)
				templateTypeEl.setAttribute("pattern", r.getPattern().toString());			
			if (r instanceof PreferenceGendocRunner) {
				PreferenceGendocRunner pr = (PreferenceGendocRunner)r;
				if (pr.getDescription() != null)
					templateTypeEl.setAttribute("description", pr.getDescription());
			}
			
			for (Map.Entry<String, String> e : r.getAdditionnalParameters().entrySet()) {
				Element paramEl = (Element)templateTypeEl.appendChild(doc.createElement("parameter"));
				paramEl.setAttribute("name", e.getKey());
				paramEl.setAttribute("description", e.getValue());
			}
			
			for (IGendocTemplate template : r.getGendocTemplates()) {
				boolean isDef = false;
				if (r instanceof PreferenceGendocRunner) {
					isDef = (((PreferenceGendocRunner) r).getDefaultTemplate() == template);
				}
				
				Element templateEl = (Element)templateTypeEl.appendChild(doc.createElement("template"));
				templateEl.setAttribute("extension", template.getOutPutExtension());
				templateEl.setAttribute("url", template.getTemplate().toExternalForm());
				templateEl.setAttribute("description", template.getDescription());
				templateEl.setAttribute("modelKey", template.getModelKey());
				templateEl.setAttribute("outputKey", template.getOutputKey());
				templateEl.setAttribute("default", isDef ? "true" : "false");				
			}
		}
	}
}
