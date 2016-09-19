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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The Class GendocTemplateParameterDialog is a dialog to add 
 * a new or edit an existing {@link IGendocTemplate} of a 
 * {@link IGendocRunner}
 */
public class GendocTemplateParameterDialog extends TitleAreaDialog {
	
	/** The input object the dialog edits. */
	private PreferenceGendocRunner.Parameter input;
	
	/** The binding context used to hold the data binding between the 
	  * UI widgets in the dialog and the input object and its properties. */
	private DataBindingContext bindingCtx = new DataBindingContext();

	/**
	 * Instantiates a new gendoc template parameter dialog.
	 *
	 * @param parentShell the parent SWT shell
	 */
	public GendocTemplateParameterDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Gets the input of the dialog, a {@link PreferenceGendocRunner#Parameter}.
	 *
	 * @return the input of the dialog.
	 */
	protected PreferenceGendocRunner.Parameter getInput() {
		return input;
	}

	/**
	 * Sets the input of the dialog. If the dialog is cancel the object
	 * is not restore to its original state, that is responsability of the
	 * client, that should clone or copy the object if needed.
	 *  
	 * @param input the new input of the dialog.
	 */
	protected void setInput(PreferenceGendocRunner.Parameter input) {
		this.input = input;
		bindingCtx.updateTargets();
	}

	/** 
	 * The dialog provided by this dialog consists of input fields to edit:
	 * <ul>
	 * <li> Parameter Key: the name of the parameter. </li>
	 * <li> Parameter Value: The value of the parameter.</li>
	 * </ul>
	 *  
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle("Configure the GenDoc template.");

		Composite c = (Composite)super.createDialogArea(parent);
		
		Composite composite = new Composite(c,SWT.NONE);
		GridLayout layout = new GridLayout(2,false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		
		Label l = new Label(composite, SWT.FILL);
		l.setText("&Parameter Key:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text name = new Text(composite, SWT.BORDER); 
		name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		l = new Label(composite, SWT.FILL);
		l.setText("&Parameter Label:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text paramLabel = new Text(composite, SWT.BORDER);
		paramLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		IValidator nameValidator = new IValidator() {					
			@Override
			public IStatus validate(Object value) {
				if (value == null || "".equals(value))
					return ValidationStatus.error("GenDoc Template Parameters must have a name.");
				return Status.OK_STATUS;
			}
		}; 
		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(name), 
				PojoProperties.value("name").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).
					setAfterConvertValidator(nameValidator),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		IValidator descriptionValidator = new IValidator() {					
			@Override
			public IStatus validate(Object value) {
				if (value == null || "".equals(value))
					return ValidationStatus.error("GenDoc Template Parameters must have a label.");
				return Status.OK_STATUS;
			}
		}; 
		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(paramLabel), 
				PojoProperties.value("label").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).
					setAfterConvertValidator(descriptionValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		TitleAreaDialogSupport.create(this, bindingCtx);  

		bindingCtx.updateTargets();
		return composite;
	}
	
}
