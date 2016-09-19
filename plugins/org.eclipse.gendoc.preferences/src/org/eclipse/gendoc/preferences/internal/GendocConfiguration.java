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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.gendoc.preferences.IGendocConfiguration;
import org.eclipse.gendoc.wizard.IGendocRunner;

// TODO: Auto-generated Javadoc
/**
 * The default implementation of {@link IGendocConfiguration}.
 */
public class GendocConfiguration implements IGendocConfiguration {
	
	/** The project to which this configuration is applied. */
	private IProject project;
	
	/** The list of runners defined for this configuration. */
	private List<IGendocRunner> runners;

	/**
	 * Instantiates a new gendoc configuration for the given project.
	 *
	 * @param project the project to which the configuration will applied.
	 */
	public GendocConfiguration(IProject project) {
		super();
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.IGendocConfiguration#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.IGendocConfiguration#getGendocRunners()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<IGendocRunner> getGendocRunners() {
		if (runners == null)
			return Collections.EMPTY_LIST;
		return Collections.unmodifiableList(runners);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.IGendocConfiguration#setGendocRunners(java.util.List)
	 */
	@Override
	public void setGendocRunners(List<IGendocRunner> runners) {
		this.runners = new ArrayList<IGendocRunner>(runners);
	}
}
