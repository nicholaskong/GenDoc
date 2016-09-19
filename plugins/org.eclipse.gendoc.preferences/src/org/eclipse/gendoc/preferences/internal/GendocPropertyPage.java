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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.preferences.Activator;
import org.eclipse.gendoc.preferences.GendocNature;
import org.eclipse.gendoc.preferences.PreferenceConstants;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The Class GendocPropertyPage add a property page to 
 * projects where {@link IGendocRunner} can be defined
 * and associated to the project.<br>
 * The runners defined in that way can be used, later on,
 * in the GenDoc Wizard when it is launched from a model 
 * belonging to the project.   
 */
public class GendocPropertyPage extends PropertyPage {

	/** A field editor used to edit a list of {@link IGendocRunner}. */
	private GendocTemplateFieldEditor editor;

	/**
	 * Constructor for GendocPropertyPage.
	 */
	public GendocPropertyPage() {
		super();
	}

	/**
	 * Adds the field editors used in this property page. In this case,
	 * it create a list editor to configure the runners.
	 *
	 * @param parent the co
	 * mposite parent where the 
	 */
	private void addEditors(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);

		editor = new GendocTemplateFieldEditor((IProject)getElement().getAdapter(IProject.class), PreferenceConstants.P_GENDOC_TEMPLATES, "GenDoc &Templates:", composite);
	}

	/**
	 * Adds a separator widget to the given parent composite.
	 *
	 * @param parent the parent composite where the separator is added.
	 */
	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addEditors(composite);
		addSeparator(composite);
		
		try {
			IProject project =  ((IResource) getElement()).getProject();
			GendocNature gendocNature = (GendocNature)project.getNature(GendocNature.ID);
			if (gendocNature != null) {
				gendocNature.load();
				editor.setInput(new ArrayList(gendocNature.getGendocConfiguration().getGendocRunners()));
			}
			
		} catch (ParseException e) {
			Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR,Activator.PLUGIN_ID,e.getMessage(),e));
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR,Activator.PLUGIN_ID,e.getMessage(),e));
		}
		return composite;
	}

	/**
	 * The implementation of the {@link PropertyPage#performOk()} in the
	 * GendocPropertyPage add the GendocNature from the project when at least
	 * one runner is configured and the project was not have it. It also remove
	 * the GenDocNature from the project when there are no runners configured.
	 *
	 * @return true, if successful
	 * @see PreferencePage#performOk()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean performOk() {
		// store the value in the owner text field
		try {
			IProject project =  ((IResource) getElement()).getProject();
			GendocNature gendocNature = (GendocNature)project.getNature(GendocNature.ID);
			
			if (editor.getInput().size() == 0) {
				if (gendocNature != null) {
					IProjectDescription description = project.getDescription();
					List<String> natures = new ArrayList<String>(Arrays.asList(description.getNatureIds()));
					natures.remove(GendocNature.ID);
					description.setNatureIds(natures.toArray(new String[0]));
					project.setDescription(description, new NullProgressMonitor());
					gendocNature = (GendocNature)project.getNature(GendocNature.ID);
				}				
				return true;
			} else {
				if (gendocNature == null) {
					IProjectDescription description = project.getDescription();
					List<String> natures = new ArrayList<String>(Arrays.asList(description.getNatureIds()));
					natures.add(GendocNature.ID);
					description.setNatureIds(natures.toArray(new String[0]));
					project.setDescription(description, new NullProgressMonitor());
					gendocNature = (GendocNature)project.getNature(GendocNature.ID);
				}
			
				gendocNature.getGendocConfiguration().setGendocRunners((List)editor.getInput());
				gendocNature.save();
			}
		} catch (CoreException e) {
			return false;
		} catch (TransformerException e) {
			return false;
		}
		return true;
	}

}