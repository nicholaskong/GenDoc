/*****************************************************************************
 * Copyright (c) 2010 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Alexia Allanic (Atos Origin) alexia.allanic@atosorigin.com - Initial API and implementation
 *  Anne Haugommard (Atos) anne.haugommard@atos.net - Remove references to Papyrus Documentation services
 *
 *****************************************************************************/
package org.eclipse.gendoc.bundle.acceleo.papyrus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gendoc.bundle.acceleo.commons.files.CommonService;
import org.eclipse.gendoc.bundle.acceleo.gmf.service.GMFServices;
import org.eclipse.gendoc.bundle.acceleo.papyrus.Activator;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.services.exception.ModelNotFoundException;
import org.eclipse.gendoc.tags.handlers.IEMFModelLoaderService;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class PapyrusServices extends GMFServices {

	private static final String ASSOCIATED_RESOURCES_SOURCE = "http://www.topcased.org/resources";
	private static final String PAPYRUS_DOCUMENTATION_STEREOTYPE_QUALIFIED_NAME = "Papyrus::Documentation::Documentation";
	private static final String PREFIX_WORKSPACE_RESOURCE = "WR"; //$NON-NLS-1$
	private static final String PREFIX_EXTERNAL_RESOURCE = "ER"; //$NON-NLS-1$
	private static final String PREFIX_REMOTE_RESOURCE = "RR"; //$NON-NLS-1$	
	private static Pattern LINK_PATTERN = Pattern.compile("\\{@link #(\\w*)(\\s|$)*\\}");

	@Override
	public List<Diagram> getDiagrams(EObject e, URI uri) {
		// ensure first diagrams resource is correctly loaded in Model set
		IEMFModelLoaderService modelLoader = GendocServices.getDefault()
				.getService(IEMFModelLoaderService.class);
		try {
			modelLoader.getModel(uri);
		} catch (ModelNotFoundException e1) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(Status.WARNING, Activator.PLUGIN_ID, e1
							.getMessage(), e1));
		}
		return super.getDiagrams(e, uri);
	}

	/**
	 * Get the diagram list associated with the object.
	 * 
	 * @param object
	 *            the object
	 * 
	 * @return the diagram list
	 */
	public List<Diagram> getPapyrusDiagrams(EObject object) {

		List<Diagram> result = getDiagramsUsingNotation(object);
		// load main ".di" file in addition in resource set
		//        URI diURI = object.eResource().getURI().trimFileExtension().appendFileExtension("di");//$NON-NLS-1$
		// if (!object.eResource().getURI().equals(diURI))
		// {
		// try
		// {
		// object.eResource().getResourceSet().getResource(diURI, true);
		// }
		// catch (WrappedException ex)
		// {
		// IGendocDiagnostician diag =
		// GendocServices.getDefault().getService(IGendocDiagnostician.class);
		// diag.addDiagnostic(new BasicDiagnostic(Diagnostic.ERROR,
		// Activator.PLUGIN_ID, 0,
		// String.format("Resource %s not found", diURI.toString()), new
		// Object[] {object}));
		// }
		//
		// }
		return result;
	}

	/**
	 * Get the documentation.
	 * 
	 * @param eObject
	 *            the object
	 * 
	 * @return the documentation
	 */
	public String getDocumentation(EObject eObject) {
		if (eObject instanceof Element) {
			EList<Comment> ownedComments = ((Element) eObject)
					.getOwnedComments();

			for (Comment comment : ownedComments) {
				if (comment
						.getAppliedStereotype(PAPYRUS_DOCUMENTATION_STEREOTYPE_QUALIFIED_NAME) != null) {
					String body = comment.getBody();
					return body;
				}
			}
		}
		return null;
	}

	/**
	 * Get the documentation resources.
	 * 
	 * @param object
	 *            the object
	 * 
	 * @return the documentation resources list. A list of absolute path
	 *         resources.
	 */
	public List<String> getDocumentationResources(EObject object) {
		List<String> absolutePaths = new ArrayList<String>();
		List<URI> uris = getAssociatedResources(object);
		for (URI uri : uris) {
			String absolutePath;
			if (uri.isPlatform()) {
				// transform relative uri to absolute
				IPath filePath = Path.fromPortableString(uri
						.toPlatformString(true));
				absolutePath = ResourcesPlugin.getWorkspace().getRoot()
						.getLocation().toOSString()
						+ filePath;
			} else if (uri.isFile()) {
				absolutePath = uri.toFileString();
			} else {
				absolutePath = uri.toString();
			}
			absolutePaths.add(absolutePath);
		}
		return absolutePaths;
	}
	
	/**
	 * Replace links text with the appropriate name of the linked object
	 * @param body, the string potentially containing {@link #(\w*)(\s|$)*} 
	 * @param context, a given EObject, links will be searched inside this eobject's resource
	 * @return the modified String
	 */
	public String replaceLinksByNameOrLabel(String body, EObject context){
		ILogger logger = GendocServices.getDefault().getService(ILogger.class);
		String result = "" ;
		Matcher m = LINK_PATTERN.matcher(body);
		int startIndex = 0;
		int lastIndex = 0 ;
		if (context == null || context.eResource() == null){
			IGendocDiagnostician diag = GendocServices.getDefault().getService(IGendocDiagnostician.class);
			diag.addDiagnostic(new BasicDiagnostic(Diagnostic.WARNING, Activator.PLUGIN_ID, 0, String.format("error in script, invalid parameter for operation replaceLinksByNameOrLabel for text : %s",body), new Object[]{body}));
			return body;
		}
		while(m.find()){
			String theLink = m.group(1);
			startIndex = m.start();
			EObject linkedEObject = context.eResource().getEObject(theLink);
			if (linkedEObject == null){
				logger.log(String.format("The linked object (%s) from %s is null", theLink, CommonService.getText(context)), IStatus.WARNING);
			}
			String toInsert = null;
			if (linkedEObject == null){
				// Papyrus displays UNKNOWN when the element does not exist
				toInsert = "UNKNOWN" ;
			}
			//it the object is a NamedElement return its name, otherwise get the label
			else if(linkedEObject instanceof NamedElement){ 
				toInsert = ((NamedElement) linkedEObject).getName();	
			}
			else{
				toInsert = CommonService.getText(linkedEObject);
			}
			// part between the linked objects
			String staticPart = body.substring(lastIndex, startIndex); 
			result += staticPart + toInsert ;
			lastIndex = m.end();
		}
		return result.concat(body.substring(lastIndex));
	}
	
	private List<URI> getAssociatedResources(EObject eObject) {
		if (eObject instanceof EModelElement
				&& !(eObject instanceof EAnnotation)) {
			EAnnotation annotation = ((EModelElement) eObject)
					.getEAnnotation(ASSOCIATED_RESOURCES_SOURCE);
			if (annotation != null) {
				return convertDetailsToURIs(annotation.getDetails());
			}
		}
		return Collections.emptyList();
	}

	private static List<URI> convertDetailsToURIs(EMap<String, String> details) {
		List<URI> uris = new LinkedList<URI>();
		for (Entry<String, String> detail : details) {
			String value = detail.getValue();

			String prefix = detail.getKey().substring(0, 2);
			URI uri = null;
			if (PREFIX_REMOTE_RESOURCE.equals(prefix)) {
				uri = URI.createURI(value, false);
			} else if (PREFIX_EXTERNAL_RESOURCE.equals(prefix)) {
				uri = URI.createFileURI(value);
			} else if (PREFIX_WORKSPACE_RESOURCE.equals(prefix)) {
				uri = URI.createPlatformResourceURI(value, true);
			}
			uris.add(uri);
		}
		return uris;
	}

}
