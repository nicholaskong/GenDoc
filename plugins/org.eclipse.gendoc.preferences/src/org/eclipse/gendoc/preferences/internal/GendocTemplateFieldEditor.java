/*****************************************************************************
 * (c) Copyright 2016 Telefonaktiebolaget LM Ericsson
 *
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
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The class GendocTemplateFieldEditor is a {@link TableFieldEditor} specialized
 * to edit a list of {@link IGendocRunner}.
 */
public class GendocTemplateFieldEditor extends TableFieldEditor {

	
	/** 
	 * The project for which the list of runners will be configured, if any. 
	 * Otherwise, the configured runners will be configure global for the workspace.
	 */
	private IProject owner;

	/**
	 * Instantiates a new gendoc template field editor.
	 */
	public GendocTemplateFieldEditor() {
	}

	/**
	 * Instantiates a new gendoc template field editor.
	 *
	 * @param owner the resource this editor configure. It can be <code>null</code> to 
	 *              indicate the configuration is applied to the workspace. 
	 * @param name the name of the field, property or preference, this editor configure.
	 * @param labelText the text of the label associated with this field editor.
	 * @param parent the composite parent where the editor will be created.
	 */
	public GendocTemplateFieldEditor(IProject owner, String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		this.owner = owner;
	}

	/**
	 * Create the specialized table this field editor presents,  
	 * two columns: Template Name and Description.<br>
	 * 
	 * The template name column correspond to the {@link IGendocRunner} name.    
	 * 
	 * @param parent the parent composite where the table it will be created.
	 * @return the table widget that the field editor provides.
	 * @see TableFieldEditor#createTableControl(Composite) 
	 */
	protected Table createTableControl(Composite parent) {
		Table t = super.createTableControl(parent);
		t.setHeaderVisible(true);
		TableColumn col = new TableColumn(t, SWT.LEFT);
		col.setText("Template Name");
		col.setWidth(150);
		
		col = new TableColumn(t, SWT.LEFT);
		col.setText("Description");
		col.setWidth(300);
    	return t;
	}

	/**
	 * A specialized implementation of {@link TableFieldEditor#getNewInputObject()} that 
	 * display a dialog to the user in order to configure a new template, corresponding to 
	 * a new {@link IGendocRunner}. 
	 * 
	 * @return a configured runner or <code>null</code> if the operation was cancel by the 
	 *         user and no runner is configured.
	 */
	@Override
	protected IGendocRunner getNewInputObject() {
		GendocTemplateDialog dlg = new GendocTemplateDialog(Display.getCurrent().getActiveShell());
		dlg.setInput(new PreferenceGendocRunner(owner, "New Template Type"));
		int res = dlg.open();
		if (res == Dialog.OK) {
			return dlg.getInput();
		}
		return null;
	}

	/**
	 * A specialized implementation of {@link TableFieldEditor#getNewInputObject()} that 
	 * display a dialog to the user in order to edit an existing template, corresponding to 
	 * an existing {@link IGendocRunner}. 
	 *
	 * @param input the object to edit. In this case a PreferenceGendocRunner is expected.<br>
	 *              The given object is clone, so the operation can be cancel without disturbing 
	 *              the existing template.   
	 * @return a new configured runner or <code>null</code> if the operation was cancel by the 
	 *         user and no runner is configured.<br>
	 *         The client should not expect the returned object to be the same as the one is passed
	 *         as argument. 
	 */
	@Override
	protected Object editInputObject(Object input) {
		GendocTemplateDialog dlg = new GendocTemplateDialog(Display.getCurrent().getActiveShell());
		try {
			dlg.setInput((PreferenceGendocRunner)((PreferenceGendocRunner)input).clone());
		} catch (CloneNotSupportedException e) {
			return null;
		}
		int res = dlg.open();
		if (res == Dialog.OK) {
			return dlg.getInput();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#createLabelProvider()
	 */
	@Override
	protected ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#createList(java.lang.Object[])
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected String createList(Object[] objs) {
		List<IGendocRunner> l = new ArrayList<IGendocRunner>();
		l.addAll((List)Arrays.asList(objs));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			GendocPreferenceUtils.marshallTemplates(l.toArray(new IGendocRunner[0]), out);
			return out.toString("UTF-8");
		} catch (Exception e) {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#parseString(java.lang.String)
	 */
	@Override
	protected Object[] parseString(String s) {
		try {
			return GendocPreferenceUtils.unmarshallTemplates(new ByteArrayInputStream(s.getBytes(Charset.forName("UTF-8"))));
		} catch (ParseException e) {
			return new IGendocRunner[0];
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		super.adjustForNumColumns(numColumns);
		((GridData)super.getTable().getLayoutData()).heightHint = 500;
	}

	/**
	 * The class LabelProvider provides the text to display in the field editor table,
	 * Template name and description corresponding to the {@link PreferenceGendocRunner#getLabel()}   
	 * and {@link PreferenceGendocRunner#getDescription()}
	 */
	private class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((IGendocRunner)element).getLabel();
			case 1:
				if (element instanceof PreferenceGendocRunner) {
					return ((PreferenceGendocRunner) element).getDescription();
				}
			}
			return "";
		}		
	}
}
