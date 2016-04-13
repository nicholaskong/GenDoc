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
 *  Papa Malick Wade (Atos Origin) papa-malick.wade@atosorigin.com - extension the format of diagrams 
 *****************************************************************************/
package org.eclipse.gendoc.bundle.acceleo.gmf.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain.Factory;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gendoc.documents.FileRunnable;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.ILogger;
import org.eclipse.gendoc.tags.handlers.Activator;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.image.ImageFileFormat;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.render.clipboard.DiagramGenerator;
import org.eclipse.gmf.runtime.diagram.ui.render.internal.DiagramUIRenderPlugin;
import org.eclipse.gmf.runtime.diagram.ui.render.util.CopyToImageUtil;
import org.eclipse.gmf.runtime.diagram.ui.util.DiagramEditorUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GMFDiagramRunnable implements FileRunnable {

	private Diagram diagram;

	private enum FileFormat {
		PNG, JPEG, GIF, BMP, JPG;
		public String getFullExtension() {
			return "." + name().toLowerCase();
		}
	}

	/** The extension of diagram. */
	private FileFormat extension;
	private final List<EObject> visibleElements;

	public GMFDiagramRunnable(Diagram diagram, String extension) {
		this(diagram, extension, null);
	}

	public GMFDiagramRunnable(Diagram diagram, String extension,
			List<EObject> visibleElements) {
		this.diagram = diagram;
		this.extension = transformToFormat(extension);
		if (visibleElements != null) {
			this.visibleElements = visibleElements;
		} else {
			this.visibleElements = Collections.emptyList();
		}

	}

	private FileFormat transformToFormat(String ext) {
		FileFormat format;
		try {
			format = FileFormat.valueOf(ext.toUpperCase());
			return format;
		} catch (IllegalArgumentException e) {
			IGendocDiagnostician diagnostician = GendocServices.getDefault()
					.getService(IGendocDiagnostician.class);
			String message = "The format " + ext + " is not supported";
			diagnostician.addDiagnostic(new BasicDiagnostic(Diagnostic.ERROR,
					Activator.PLUGIN_ID, 0, message, null));
			return FileFormat.valueOf(ext);
		}

	}

	/**
	 * Instantiates a new diagram runnable.
	 * 
	 * @param diagram
	 *            the diagram
	 */
	public void run(final String resourceId, final String outputResourceFolder) {
		if (Realm.getDefault() == null) {
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				@Override
				public void run() {
					doRun(resourceId, outputResourceFolder);
				}
			});
		}
		else {
			doRun(resourceId, outputResourceFolder);
		}
		
	}
	
	protected void doRun(String resourceId, String outputResourceFolder) {
		if (extension != null) {
			MultiElementsCopytoImageUtils c = new MultiElementsCopytoImageUtils();
			new File(outputResourceFolder).mkdirs();
			IPath path = new Path(outputResourceFolder + "/" + resourceId
					+ extension.getFullExtension());
			try {
				// this part of code is necessary for Transactional Editing
				// Domain by GMF
				// if this section is removed a NPE occurs in copyToImage
				// Function (no Editing Domain)
				{
					Resource eResource = diagram.eResource();
					if (eResource != null) {
						ResourceSet resourceSet = eResource.getResourceSet();
						if (TransactionUtil.getEditingDomain(resourceSet) == null) {
							Factory factory = TransactionalEditingDomain.Factory.INSTANCE;
							factory.createEditingDomain(resourceSet);
						}
					}
				}
				if (visibleElements == null || visibleElements.isEmpty()) {
					c.copyToImage(diagram, path, getImageFileFormat(extension),
							new NullProgressMonitor(),
							PreferencesHint.USE_DEFAULTS);
				} else {
					c.copyToImage(diagram, path, visibleElements,
							getImageFileFormat(extension),
							new NullProgressMonitor(),
							PreferencesHint.USE_DEFAULTS);
				}
			} catch (CoreException e) {
				IGendocDiagnostician diag = GendocServices.getDefault().getService(IGendocDiagnostician.class);
				if (diag != null)
				{
					diag.addDiagnostic(IStatus.WARNING,"no image can be generated for Diagram : " + diagram.toString(), new Object[]{diagram} );
				}
				e.printStackTrace();
			} catch (Exception ex) {
				IGendocDiagnostician diag = GendocServices.getDefault().getService(IGendocDiagnostician.class);
				if (diag != null)
				{
					diag.addDiagnostic(IStatus.WARNING,"no image can be generated for Diagram : " + diagram.toString(), new Object[]{diagram} );
				}
				ex.printStackTrace();
			}
		}

	}

	private ImageFileFormat getImageFileFormat(FileFormat format) {
		return ImageFileFormat.resolveImageFormat(format.name());
	}

	public String getFileExtension() {
		return extension.toString().toLowerCase();
	}

	/**
	 * A subclass to manage list of elements and diagram in parameter
	 * 
	 * @author tfaure
	 * 
	 */
	public class MultiElementsCopytoImageUtils extends CopyToImageUtil {

		public List copyToImage(Diagram diagram, IPath destination,
				ImageFileFormat format, NullProgressMonitor monitor,
				PreferencesHint preferencesHint)
						throws CoreException {

			Trace.trace(DiagramUIRenderPlugin.getInstance(),
					"Copy diagram to Image " + destination + " as " + format); //$NON-NLS-1$ //$NON-NLS-2$

			List partInfo = Collections.EMPTY_LIST;

			DiagramEditor openedDiagramEditor = findOpenedDiagramEditor(diagram);
			if (openedDiagramEditor != null) {
				DiagramGenerator generator = copyToImage(openedDiagramEditor.getDiagramEditPart(),
						destination, format, monitor);
				partInfo = generator.getDiagramPartInfo(openedDiagramEditor.getDiagramEditPart());
			} else {
				Shell shell = new Shell();
				try {
					DiagramEditPart diagramEditPart = createDiagramEditPart(diagram,
							shell, preferencesHint);
					Assert.isNotNull(diagramEditPart);
					DiagramGenerator generator = copyToImage(diagramEditPart,
							destination, format, monitor);
					partInfo = generator.getDiagramPartInfo(diagramEditPart);
				} finally {
					shell.dispose();
				}
			}

			return partInfo;
		}

		private DiagramEditor findOpenedDiagramEditor(Diagram diagram) {
			DiagramEditor result = DiagramEditorUtil.findOpenedDiagramEditorForID(ViewUtil.getIdStr(diagram));
			if (result != null){
				IPath iPathDiagEditor =getIPath(result.getDiagram());
				IPath iPathDiag = getIPath(diagram) ;

				if (iPathDiagEditor == null || iPathDiag == null || !iPathDiag.equals(iPathDiagEditor)){
					((ILogger) GendocServices.getDefault().getService(ILogger.class)).log("Two diagrams in separate files " + iPathDiagEditor + " and " + iPathDiag + " have the same identifier", Status.WARNING);
					return null ;
				}
			}
			return result ;
		}

		private IPath getIPath(Diagram diagram) {
			if (diagram != null){
				Resource resource = diagram.eResource();
				if (resource != null){
					IFile file = WorkspaceSynchronizer.getUnderlyingFile(resource);
					if (file != null){
						return file.getFullPath();
					}
				}
			}
			return null;
		}

		public List copyToImage(Diagram diagram, IPath destination,
				List<EObject> visibleElements, ImageFileFormat format,
				NullProgressMonitor monitor, PreferencesHint preferencesHint)
						throws CoreException {
			Shell shell = null ;
			try {
				Trace.trace(DiagramUIRenderPlugin.getInstance(),
						"Copy diagram to Image " + destination + " as " + format); //$NON-NLS-1$ //$NON-NLS-2$

				List partInfo = Collections.EMPTY_LIST;

				DiagramEditor openedDiagramEditor = findOpenedDiagramEditor(diagram);
				DiagramEditPart diagramEditPart = null ;

				if (openedDiagramEditor != null) {
					diagramEditPart = openedDiagramEditor.getDiagramEditPart(); 
				} else {
					shell = new Shell();
					diagramEditPart = createDiagramEditPart(
							diagram, shell, preferencesHint);
				}
				Assert.isNotNull(diagramEditPart);
				copyToImage(diagramEditPart,
						getEditParts(visibleElements, diagramEditPart),
						destination, format, monitor);
				return partInfo;
			} finally {
				if (shell != null && !shell.isDisposed())
				{
					shell.dispose();
				}
			}
		}

	}

	public List<?> getEditParts(List<EObject> visibleElements,
			DiagramEditPart diagramEditPart) {
		return getEditParts(visibleElements, diagramEditPart, true);
	}

	public List<?> getEditParts(List<EObject> visibleElements,
			DiagramEditPart diagramEditPart, boolean includeConnections) {
		List<GraphicalEditPart> result = new LinkedList<GraphicalEditPart>();
		for (EObject e : visibleElements) {
			Object model = diagramEditPart.getModel();
			if (model instanceof Diagram) {
				Diagram diagram = (Diagram) model;
				for (TreeIterator<EObject> i = EcoreUtil.getAllProperContents(
						diagram, true); i.hasNext();) {
					EObject current = i.next();
					if (current instanceof View) {
						View view = (View) current;
						if (equals(e, view.getElement())) {
							Object part = diagramEditPart.getViewer()
									.getEditPartRegistry().get(view);
							if (part instanceof GraphicalEditPart) {
								result.add((GraphicalEditPart) part);
							}
						}
					}
				}
			}
		}
		if (includeConnections)
		{
			// the process is made twice but copying the code 
			// in this case is better than overriding to avoid maintenance problem
			ArrayList<GraphicalEditPart> tmp = new ArrayList<GraphicalEditPart>(result);
			for(GraphicalEditPart g : tmp)
			{
				result.addAll(findConnectionsToPaint(g, result));
			}
		}
		return result;
	}

	protected boolean equals(EObject e, EObject fromView) {
		boolean result = false ;
		if (fromView == e)
		{
			result = true ;
		}
		else
		{
			// if the diagram editor is the opened one the eobjects are not the same
			if (e.eResource() != null && fromView.eResource() != null)
			{
				Resource eResource =  e.eResource();
				Resource viewResoure =  fromView.eResource();
				if (eResource.getURI() != null && eResource.getURI().equals(viewResoure.getURI()))
				{
					result = (eResource.getURIFragment(e) != null && eResource.getURIFragment(e).equals(viewResoure.getURIFragment(fromView)));
				}
			}
		}
		return result ;
	}

	/**
	 * Collects all connections contained within the given edit part
	 * Code copy from {@link CopyToImageUtil}
	 * @param editPart
	 *            the container editpart
	 * @return connections within it
	 */
	protected Collection<ConnectionEditPart> findConnectionsToPaint(
			GraphicalEditPart editPart, List<GraphicalEditPart> relatedEditParts) {
		/*
		 * Set of node editparts contained within the given editpart
		 */
		HashSet<GraphicalEditPart> editParts = new HashSet<GraphicalEditPart>();

		/*
		 * All connection editparts that have a source contained within the
		 * given editpart
		 */
		HashSet<ConnectionEditPart> connectionEPs = new HashSet<ConnectionEditPart>();

		/*
		 * Connections contained within the given editpart (or just the
		 * connections to paint
		 */
		HashSet<ConnectionEditPart> connectionsToPaint = new HashSet<ConnectionEditPart>();

		/*
		 * Populate the set of node editparts
		 */
		getNestedEditParts(editPart, editParts);

		/*
		 * Populate the set of connections whose source is within the given
		 * editpart
		 */
		for (GraphicalEditPart gep : editParts) {
			connectionEPs.addAll(getAllConnectionsFrom(gep));
		}

		/*
		 * Populate the set of connections whose source is the given editpart
		 */
		connectionEPs.addAll(getAllConnectionsFrom(editPart));

		/*
		 * Create a set of connections constained within the given editpart
		 */
		while (!connectionEPs.isEmpty()) {
			/*
			 * Take the first connection and check whethe there is a path
			 * through that connection that leads to the target contained within
			 * the given editpart
			 */
			Stack<ConnectionEditPart> connectionsPath = new Stack<ConnectionEditPart>();
			ConnectionEditPart conn = connectionEPs.iterator().next();
			connectionEPs.remove(conn);
			connectionsPath.add(conn);

			/*
			 * Initialize the target for the current path
			 */
			EditPart target = conn.getTarget();
			while (connectionEPs.contains(target)) {
				/*
				 * If the target end is a connection, check if it's one of the
				 * connection's whose target is a connection and within the
				 * given editpart. Append it to the path if it is. Otherwise
				 * check if the target is within the actual connections or nodes
				 * contained within the given editpart
				 */
				ConnectionEditPart targetConn = (ConnectionEditPart) target;
				connectionEPs.remove(targetConn);
				connectionsPath.add(targetConn);

				/*
				 * Update the target for the new path
				 */
				target = targetConn.getTarget();
			}

			/*
			 * The path is built, check if it's target is a node or a connection
			 * contained within the given editpart
			 */
			if (editParts.contains(target)
					|| connectionsToPaint.contains(target)
					|| relatedEditParts.contains(target)) {
				connectionsToPaint.addAll(connectionsPath);
			}
		}
		return connectionsToPaint;
	}

	/**
	 * This method is used to obtain the list of child edit parts for shape
	 * compartments.
	 * 
	 * @param childEditPart
	 *            base edit part to get the list of children editparts
	 * @param editParts
	 *            list of nested shape edit parts
	 */
	protected void getNestedEditParts(GraphicalEditPart baseEditPart,
			Collection<GraphicalEditPart> editParts) {

		for (Object child : baseEditPart.getChildren()) {
			if (child instanceof GraphicalEditPart) {
				GraphicalEditPart childEP = (GraphicalEditPart) child;
				editParts.add(childEP);
				getNestedEditParts(childEP, editParts);
			}
		}
	}

	/**
	 * Returns all connections orginating from a given editpart. All means that
	 * connections originating from connections that have a source given
	 * editpart will be included
	 * 
	 * @param ep
	 *            the editpart
	 * @return all source connections
	 */
	protected List<ConnectionEditPart> getAllConnectionsFrom(
			GraphicalEditPart ep) {
		LinkedList<ConnectionEditPart> connections = new LinkedList<ConnectionEditPart>();
		for (Object sourceConnObj : ep.getSourceConnections()) {
			if (sourceConnObj instanceof ConnectionEditPart) {
				ConnectionEditPart sourceConn = (ConnectionEditPart) sourceConnObj;
				connections.add(sourceConn);
				connections.addAll(getAllConnectionsFrom(sourceConn));
			}
		}
		return connections;
	}

}
