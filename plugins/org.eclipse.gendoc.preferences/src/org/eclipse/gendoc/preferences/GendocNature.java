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
package org.eclipse.gendoc.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;

import javax.xml.transform.TransformerException;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gendoc.preferences.internal.GendocConfiguration;
import org.eclipse.gendoc.preferences.internal.GendocPreferenceUtils;

/**
 * A GendocNature provide Gendoc configuration relative to the project. In this way
 * the Gendoc configuration can be store in repository together with the model files.
 */
public class GendocNature implements IProjectNature {
	
	/** The ID of the GendocNature. */
	public static final String ID = "org.eclipse.gendoc.preferences.GenDocNature";
	
	/** The project. */
	private IProject project;
	
	/** The gendoc configuration. */
	private IGendocConfiguration gendocConfiguration;

	/**
	 * Instantiates a new Gendoc nature.
	 */
	public GendocNature() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	@Override
	public void configure() throws CoreException {
		IFile file = project.getFile(".gendoc");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			GendocPreferenceUtils.marshall(gendocConfiguration, out);
			if (file.exists())
				file.delete(true, new NullProgressMonitor());
			file.create(new ByteArrayInputStream(out.toByteArray()), IResource.FORCE , new NullProgressMonitor());
		} catch (TransformerException e) {
			throw new CoreException(ValidationStatus.error("Can not create Gendoc configuration.",e));
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	@Override
	public void deconfigure() throws CoreException {
		IFile file = project.getFile(".gendoc");
		file.delete(true, new NullProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void setProject(IProject project) {
		this.project = project;
		if (project != null) {
			gendocConfiguration = new GendocConfiguration(project);
			try {
				load();
			} catch (Exception e) {}					
		}
	}
	
	/**
	 * Gets the Gendoc configuration for the project which this nature
	 * configures.
	 *
	 * @return the gendoc configuration
	 */
	public IGendocConfiguration getGendocConfiguration() {
		return gendocConfiguration;
	}

	/**
	 * Convenient method to persist the Gendoc configuration associated 
	 * with the project in a file called .gendoc.
	 *
	 * @throws ParseException
	 *             the parse exception
	 * @throws CoreException
	 *             the core exception
	 */
	public void save() throws TransformerException, CoreException {
		IFile file = project.getFile(".gendoc");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GendocPreferenceUtils.marshall(gendocConfiguration, out);
		file.setContents(new ByteArrayInputStream(out.toByteArray()), IResource.FORCE , new NullProgressMonitor());		
	}
	
	/**
	 * Convenient method to load the Gendoc configuration associated 
	 * with the project.
	 *
	 * @throws ParseException
	 *             the parse exception
	 * @throws CoreException
	 *             the core exception
	 */
	public void load() throws ParseException, CoreException {
		IFile file = project.getFile(".gendoc");
		gendocConfiguration = GendocPreferenceUtils.unmarshall(getProject(), file.getContents());
	}
}
