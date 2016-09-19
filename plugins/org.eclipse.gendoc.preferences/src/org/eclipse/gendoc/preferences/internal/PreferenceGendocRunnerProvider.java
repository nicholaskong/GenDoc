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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.preferences.Activator;
import org.eclipse.gendoc.preferences.GendocNature;
import org.eclipse.gendoc.preferences.PreferenceConstants;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocRunnerProvider;

/**
 * The Class PreferenceGendocRunnerProvider implements the IGendocRunnerProvider 
 * interface to provide the list of {@link IGendocRunner} configured for all the
 * projects open in the workspace and the ones configured in the workspace
 * preferences.  
 */
public class PreferenceGendocRunnerProvider implements IGendocRunnerProvider {

	/**
	 * Instantiates a new preference gendoc runner provider.
	 */
	public PreferenceGendocRunnerProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.wizard.IGendocRunnerProvider#getGendocRunners()
	 */
	@Override
	public IGendocRunner[] getGendocRunners() {
		List<IGendocRunner> runners = new ArrayList<IGendocRunner>();
		String templatesStr = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, 
				PreferenceConstants.P_GENDOC_TEMPLATES, 
				"<templates/>", 
				null);
		try {
			IGendocRunner[] templates = GendocPreferenceUtils.unmarshallTemplates(new ByteArrayInputStream(templatesStr.getBytes(Charset.forName("UTF-8"))));
			runners.addAll(Arrays.asList(templates));
		} catch (ParseException e) {}
		
		for (IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				GendocNature nature = (GendocNature)proj.getNature(GendocNature.ID);
				if (nature == null)
					continue;
				
				runners.addAll(nature.getGendocConfiguration().getGendocRunners());
			} catch (CoreException e) {
                Activator.getDefault().getLog().log(new Status(
                		IStatus.ERROR,Activator.PLUGIN_ID, 
                		"Error retriving GenDocRunners from GendocRunnerProvider.", 
                		e));
			}
		}
		return runners.toArray(new IGendocRunner[runners.size()]);
	}

}
