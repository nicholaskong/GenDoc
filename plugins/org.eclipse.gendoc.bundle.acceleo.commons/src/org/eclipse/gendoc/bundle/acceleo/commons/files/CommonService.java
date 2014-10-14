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
 *  Anne Haugommard (Atos Origin) anne.haugommard@atosorigin.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.bundle.acceleo.commons.files;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory.Descriptor;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.gendoc.documents.IAdditionalResourceService;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.IRegistryService;
import org.eclipse.gendoc.tags.ITagExtensionService;
import org.eclipse.gendoc.tags.handlers.IEMFModelLoaderService;
import org.osgi.framework.Bundle;

public class CommonService {
	private static final Pattern newLinePattern = Pattern
			.compile("\\r\\n|\\n|\\r");

	/**
	 * Compiles patterns of Gendoc tags
	 * 
	 * @return list of Gendoc tags patterns
	 */
	static List<Pattern> compileTagsPatterns() {
		ITagExtensionService tagExtensionService = GendocServices.getDefault()
				.getService(ITagExtensionService.class);

		List<Pattern> listPattern = new ArrayList<Pattern>();

		for (String element : tagExtensionService.getAllTagNames()) {
			/*listPattern.add(Pattern
					.compile("(.*&lt;)(" + element + ".*&gt;.*)"));
			listPattern.add(Pattern.compile("(.*&lt;/)(" + element
					+ ".*&gt;.*)"));*/
			
			listPattern.add(Pattern
					.compile("(.*&lt;)(" + element + ")(.*&gt;.*)"));
			listPattern.add(Pattern.compile("(.*&lt;/)(" + element
					+ ")(.*&gt;.*)"));
		}
		return listPattern;
	}

	/**
	 * Gendoc tags patterns
	 */
	static List<Pattern> patterns = compileTagsPatterns();

	/**
	 * Replace special characters inside the given String <, >, &, ', " and
	 * replace them by corresponding XML codes.
	 * 
	 * @param string
	 *            The string to modify
	 * 
	 * @return the string without special characters
	 */
	public static String removeSpecialCharacters(String string) {
		if (string == null) {
			return null;
		}
		String result = string.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("&amp;lt;", "&lt;")
				.replaceAll(">", "&gt;").replaceAll("&amp;gt;", "&gt;").replaceAll("'", "&apos;")
				.replaceAll("\"", "&quot;");
		
		if (!result.isEmpty()) {
			/*for (Pattern pattern : patterns) {
				result = pattern.matcher(result).replaceAll("$1 $2");
			}*/

			ITagExtensionService tagExtensionService = GendocServices.getDefault()
					.getService(ITagExtensionService.class);

			for (String element : tagExtensionService.getAllTagNames()) {
				result = result.replaceAll("&lt;/" + element, "&lt;/ " + element).replaceAll("&lt;" + element, "&lt; " + element);
			}
		}

		return result;
	}

	/**
	 * Gets a tab where string is separated on the new lines
	 * 
	 * @param string
	 * @return the tab
	 */
	public static List<String> splitNewLine(String string) {
		if (string == null) {
			return null;
		}
		return Arrays.asList(newLinePattern.split(string));
	}

	/**
	 * Gendoc put Put a variable available along the whole Gendoc process
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static String gPut(Object key, Object value) {
		IRegistryService registry = GendocServices.getDefault().getService(
				IRegistryService.class);
		if (registry != null) {
			registry.put(key, value);
		}
		return "";
	}

	/**
	 * Gendoc get Get a variable available along the whole Gendoc process
	 * 
	 * @param key
	 * @return
	 */
	public static Object gGet(Object key) {
		IRegistryService registry = GendocServices.getDefault().getService(
				IRegistryService.class);
		if (registry != null) {
			return registry.get(key);
		}
		return null;
	}

	/**
	 * Returns a generic String for the given Eobject
	 * 
	 * @param e
	 *            , the eobject
	 * @return the text
	 */
	public static String getText(EObject e) {
		if (e == null) {
			return "";
		}
		AdapterFactory factory = null;
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(e.eClass().getEPackage());
		list.add(IStructuredItemContentProvider.class);
		Descriptor desc = ComposedAdapterFactory.Descriptor.Registry.INSTANCE
				.getDescriptor(list);
		if (desc != null) {
			factory = desc.createAdapterFactory();
		}
		if (factory == null) {
			factory = new ReflectiveItemProviderAdapterFactory();
		}
		return new ReflectiveItemProvider(factory).getText(e);
	}

	public static final String KEY_FOR_MAP_OF_EOBJECT = "Gendoc_map_of_eobjects";

	public static String getId(EObject e) {
		String id = null;
		Resource r = e.eResource();
		if (r instanceof XMIResource) {
			XMIResource xmi = (XMIResource) r;
			id = xmi.getID(e);
		}
		if (id == null) {
			IRegistryService service = GendocServices.getDefault().getService(
					IRegistryService.class);
			@SuppressWarnings("unchecked")
			Map<EObject, String> map = (Map<EObject, String>) service
					.get(KEY_FOR_MAP_OF_EOBJECT);
			if (map == null) {
				map = new HashMap<EObject, String>();
				service.put(KEY_FOR_MAP_OF_EOBJECT, map);
			}
			id = map.get(e);
			if (id == null) {
				id = EcoreUtil.generateUUID();
				map.put(e, id);
			}
		}
		return id;
	}

	/**
	 * Get the resource containing 'object' and load the resource with same URI
	 * except for fileExtension, replaced by extensionReplacement
	 * 
	 * @param object
	 *            EObject in base resource
	 * @param extensionReplacement
	 *            the new extension for the file to load
	 * @return ""
	 */
	public static String load(org.eclipse.emf.ecore.EObject object,
			java.lang.String extensionReplacement) {
		// Get resource for current object
		Resource r = object.eResource();

		// Get transformed URI : replace file extension by
		// "extensionReplacement"
		URI uri = r.getURI().trimFileExtension()
				.appendFileExtension(extensionReplacement);
		loadModelFromURI(uri);

		return "";
	}

	/**
	 * Load the resource with URI put in parameter
	 * 
	 * @param uriString
	 *            the URI of the file to load
	 * @return ""
	 */
	public static String loadURI(java.lang.String uriString) {
		URI uri = null;
		IGendocDiagnostician diagnostician = GendocServices.getDefault()
				.getService(IGendocDiagnostician.class);
		try {
			uri = URI.createURI(uriString);
		} catch (java.lang.IllegalArgumentException ex) {
			diagnostician.addDiagnostic(IStatus.ERROR,
					"Invalid URI format. Following resource cannot be loaded : '"
							+ uriString + "'.\n" + ex.getMessage(), uri);
		}
		if (uri != null) {
			loadModelFromURI(uri);
		}
		return "";
	}

	/**
	 * Load a model from the URI in parameter
	 * 
	 * @param uri
	 *            URI of the model to load
	 */
	private static void loadModelFromURI(URI uri) {
		IGendocDiagnostician diagnostician = GendocServices.getDefault()
				.getService(IGendocDiagnostician.class);
		IEMFModelLoaderService emfModelLoaderService = GendocServices
				.getDefault().getService(IEMFModelLoaderService.class);
		try {
			// load EMF model with expected URI
			emfModelLoaderService.getModel(uri);
		} catch (RuntimeException e1) {
			diagnostician.addDiagnostic(
					IStatus.ERROR,
					"Following resource could not be loaded : '"
							+ uri.toFileString() + "'.\n" + e1.getMessage(),
					uri);
		} catch (Exception ex) {
			diagnostician.addDiagnostic(
					IStatus.ERROR,
					"Following resource could not be loaded : '"
							+ uri.toFileString() + "'.\n" + ex.getMessage(),
					uri);
		}
	}

	/**
	 * Get the resource containing 'object' and load the resource with
	 * relativePath in parameter
	 * 
	 * @param object
	 *            EObject in base resource
	 * @param relativePath
	 *            the relative URI to new resource to load
	 * @return ""
	 */
	public static String loadRelative(org.eclipse.emf.ecore.EObject object,
			java.lang.String relativePath) {
		// Get resource for current object
		Resource r = object.eResource();
		URI relativeResourceURI;
		IGendocDiagnostician diagnostician = GendocServices.getDefault()
				.getService(IGendocDiagnostician.class);
		try {
			URI relativePathURI = URI.createURI(relativePath);
			relativeResourceURI = relativePathURI.resolve(r.getURI());
			loadModelFromURI(relativeResourceURI);
		} catch (IllegalArgumentException e) {
			diagnostician.addDiagnostic(
					IStatus.ERROR,
					"Resource with relative path : '" + relativePath
							+ "' cannot be loaded from resource '"
							+ r.getURI().toFileString() + "'.\n"
							+ e.getMessage(), object);
		}
		return "";

	}

	public static String getPluginImage(String pluginId, String path) {
		if (pluginId == null || path == null)
		{
			return null ;
		}
		if (path.contains("\\")) {
			path = path.replaceAll("\\", "/");
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		IGendocDiagnostician diag = GendocServices.getDefault().getService(
				IGendocDiagnostician.class);
		Bundle b = Platform.getBundle(pluginId);
		if (b == null) {
			diag.addDiagnostic(IStatus.ERROR,
					String.format("bundle %s not found", pluginId), null);
			return null;
		}
		try {
			URL url = b.getEntry(path);
			if (url == null) {
				diag.addDiagnostic(IStatus.ERROR, String.format(
						"path %s for bundle %s not found", path, pluginId),
						null);
				return null;
			}
			IDocumentService docService = GendocServices.getDefault()
					.getService(IDocumentService.class);
			IAdditionalResourceService resourceService = docService
					.getAdditionalResourceService();
			String extension = path.substring(path.lastIndexOf(".") + 1, path.length());
			return resourceService.addNewImageRunnable(new InputStream2FileRunnable(extension, new BufferedInputStream(url.openStream())));
		} catch (IllegalStateException e) {
			diag.addDiagnostic(IStatus.ERROR, String.format(
					"path %s for bundle %s not found", path, pluginId), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
