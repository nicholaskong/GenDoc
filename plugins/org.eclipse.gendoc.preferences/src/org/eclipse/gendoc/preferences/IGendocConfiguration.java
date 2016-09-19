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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.gendoc.wizard.IGendocRunner;

/**
 * The interface IGendocConfiguration define the Gendoc configuration
 * that is associated with a project through the use of the {@link GenDocNature}.
 */
public interface IGendocConfiguration {
	
	/**
	 * Gets the project to which this configuration is applied.
	 *
	 * @return the project
	 */
	public IProject getProject();
	
	/**
	 * Gets a list of {@link IGendocRunner} this configuration provides.
	 *
	 * @return the gendoc runners
	 */
	public List<IGendocRunner> getGendocRunners();
	
	/**
	 * Set the list of {@link IGendocRunner} this configuration provides.
	 *
	 * @param runners
	 *            the new gendoc runners
	 */
	public void setGendocRunners(List<IGendocRunner> runners);
}
