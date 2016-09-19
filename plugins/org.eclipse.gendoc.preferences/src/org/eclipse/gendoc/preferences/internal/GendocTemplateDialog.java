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

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.preferences.internal.PreferenceGendocRunner.Parameter;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;


/**
 * The Class GendocTemplateDialog is used to configure a new
 * Gendoc runner or edit a existing one. <br>
 * This class is used from the {@link GendocTemplateFieldEditor}.<br>
 * The dialog use the databinding packages to bind the UI widgets with 
 * the input object, in this case {@link IGendocRunner}.  
 */
public class GendocTemplateDialog extends TitleAreaDialog {
	
	/** The object that the dialog edits. */
	private IGendocRunner input;
	
	/** The binding context that contains the bindings between the 
	 * UI widgets of the dialog and the input object. */
	private DataBindingContext bindingCtx = new DataBindingContext();

	/**
	 * Instantiates a new gendoc template dialog.
	 *
	 * @param parentShell the parent SWT shell. 
	 */
	public GendocTemplateDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Gets access to the {@link IGendocRunner} the dialog edits.
	 *
	 * @return the input the dialog edits.
	 */
	protected IGendocRunner getInput() {
		return input;
	}

	/**
	 * Sets the {@link IGendocRunner} the dialog will edit.
	 *
	 * @param input the new input the dialog will edit.
	 */
	protected void setInput(IGendocRunner input) {
		this.input = input;
		bindingCtx.updateTargets();
	}

	/**
	 * 
	 * It create the UI widget to edit the name, a description, the model
	 * regular expression. The two tableused to configure the parameters that
	 * the template is expecting and the one to configure the actual template
	 * files, possible various, in different file formats (docx, odt, ...) are
	 * created in {@link #createParameterArea(Composite)} and
	 * {@link #createTemplateArea(Composite)} <br>
	 * 
	 * The method also create and initialize the data bindings between UI
	 * widgets and input object.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle("Configure the GenDoc template sets.");

		Composite c = (Composite)super.createDialogArea(parent);
		
		Composite composite = new Composite(c,SWT.NONE);
		GridLayout layout = new GridLayout(2,false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		
		Label l = new Label(composite, SWT.FILL);
		l.setText("&Name:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text name = new Text(composite, SWT.BORDER); 
		name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		l = new Label(composite, SWT.FILL);
		l.setText("&Description:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text description = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData)description.getLayoutData()).minimumHeight = 80;
		
		l = new Label(composite, SWT.FILL);
		l.setText("&Model File regexp:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL,false, false));
		
		Text pattern = new Text(composite, SWT.BORDER);
		pattern.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		l = new Label(composite, SWT.NONE);
		l = new Label(composite, SWT.FILL | SWT.WRAP);
		l.setText("Note: This regular expression define for which model files, this GenDoc templates will be enable.\n " +
				  "   Example: .*\\.uml will enable this template to be used agains any file with extension 'uml'.\n" +
				  "The regular expression is according to java regular expressions.");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, false));

		IValidator nameValidator = new IValidator() {					
			@Override
			public IStatus validate(Object value) {
				if (value == null || "".equals(value))
					return ValidationStatus.error("GenDoc Template Sets must have a name");
				try {
					Pattern.compile((String)value);
				} catch(Exception e) {
					return ValidationStatus.error("Model RegExp: "+e.getLocalizedMessage(), e);
				}
				return Status.OK_STATUS;
			}
		}; 
		IValidator patternValidator = new IValidator() {					
			@Override
			public IStatus validate(Object value) {
				if (value == null || "".equals(value))
					return ValidationStatus.error("GenDoc Template Sets must specify a regular expresion used to match model files.");
				return Status.OK_STATUS;
			}
		}; 
		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(name), 
				PojoProperties.value("label").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).
					setAfterConvertValidator(nameValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		IValidator descriptionValidator = new IValidator() {					
			@Override
			public IStatus validate(Object value) {
				if (value == null || "".equals(value))
					return ValidationStatus.warning("GenDoc Template Parameters Set should have a description.");
				return Status.OK_STATUS;
			}
		}; 
		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(description), 
				PojoProperties.value("description").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).
					setAfterConvertValidator(descriptionValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(pattern), 
				PojoProperties.value("pattern").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).setConverter(
						new Converter(String.class, Pattern.class) {						
							@Override
							public Object convert(Object fromObject) {
								if (fromObject == null || "".equals(fromObject))
									return null;
								try {
									return Pattern.compile((String)fromObject);
								} catch(Exception e) {
									return null;
								}
							}
						}).setAfterConvertValidator(patternValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST).setConverter(						
						new Converter(Pattern.class, String.class) {						
							@Override
							public Object convert(Object fromObject) {
								if (fromObject == null)
									return "";
								return fromObject.toString();
							}
						})); 


		Composite paramArea = createParameterArea(composite);
		paramArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true,2,1));
		
		Composite TemplateArea = createTemplateArea(composite);
		TemplateArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true,2,1));

		TitleAreaDialogSupport.create(this, bindingCtx);  

		bindingCtx.updateTargets();
		bindingCtx.updateModels();
		return composite;
	}
	
	/**
	 * Creates a dialog area with a list editor to add and / or remove 
	 * template files.
	 *
	 * @param parent the parent composite where the editor will be located.
	 * @return the root composite of this dialog area. 
	 */
	@SuppressWarnings("unchecked")
	private Composite createTemplateArea(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1,false));
		TableFieldEditor editor = new TableFieldEditor("templates", "Templates:", c) {			
			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#createTableControl(org.eclipse.swt.widgets.Composite)
			 */
			protected Table createTableControl(Composite parent) {
				Table t = super.createTableControl(parent);
				t.setHeaderVisible(true);
				TableColumn col = new TableColumn(t, SWT.LEFT);
				col.setText("Template File");
				col.setWidth(300);
				
				col = new TableColumn(t, SWT.LEFT);
				col.setText("Output Type");
				col.setWidth(80);
		    	return t;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#getNewInputObject()
			 */
			@Override
			protected Object getNewInputObject() {
				GendocTemplateFileDialog dlg = new GendocTemplateFileDialog(Display.getCurrent().getActiveShell());
				dlg.setInput(new PreferenceGendocTemplate("docx", null));
				int res = dlg.open();
				if (res == Dialog.OK) {
					return dlg.getInput();
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#editInputObject(java.lang.Object)
			 */
			@Override
			protected Object editInputObject(Object input) {
				PreferenceGendocTemplate cur = (PreferenceGendocTemplate)input;
				GendocTemplateFileDialog dlg = new GendocTemplateFileDialog(Display.getCurrent().getActiveShell());
				try {
					dlg.setInput((PreferenceGendocTemplate)cur.clone());
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
				return new ITableLabelProvider() {
					@Override
					public void removeListener(ILabelProviderListener listener) {}				
					@Override
					public boolean isLabelProperty(Object element, String property) {return true;}					
					@Override
					public void dispose() {}					
					@Override
					public void addListener(ILabelProviderListener listener) {}					
					
					@Override
					public String getColumnText(Object element, int columnIndex) {
						IGendocTemplate template = (IGendocTemplate)element;
						switch (columnIndex) {
							case 0: 
								return template.getTemplate() == null ? "" : template.getTemplate().toExternalForm();
							case 1: 
								return template.getOutPutExtension();
							default:
								return "";								
						}
					}
					
					@Override
					public Image getColumnImage(Object element, int columnIndex) {
						return null;
					}
				};
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#refreshViewer()
			 */
			@Override
			protected void refreshViewer() {
				super.refreshViewer();
				bindingCtx.updateModels();
			}			
		};
		editor.adjustForNumColumns(2);
		IValidator templatesValidator = new IValidator() {					
			/* (non-Javadoc)
			 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
			 */
			@SuppressWarnings("rawtypes")
			@Override
			public IStatus validate(Object value) {
				if (value == null || ((List)value).size() == 0)
					return ValidationStatus.error("GenDoc Template Sets must have at least one GenDoc Template specified.");
				List<IGendocTemplate> list = (List<IGendocTemplate>)value;
				HashSet<String> outFormats = new HashSet<String>();
				for (IGendocTemplate t : list) {
					if (outFormats.contains(t.getOutPutExtension())) {
						return ValidationStatus.error("Only one GenDoc Template associated with a output format can be specified.");
					}
				}
				return Status.OK_STATUS;
			}
		}; 

		bindingCtx.bindValue(
				PojoProperties.value("input").observe(editor), 
				PojoProperties.value("gendocTemplates").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).
					setAfterConvertValidator(templatesValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));
		
		return c;
	}

	/**
	 * Creates a dialog area with a list editor to edit the template parameters 
	 *
	 * @param parent the parent composite where the editor will be located.
	 * @return the root composite of this dialog area. 
	 */
	@SuppressWarnings("unchecked")
	private Composite createParameterArea(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1,false));
		TableFieldEditor editor = new TableFieldEditor("parameters", "Parameters:", c) {

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#createTableControl(org.eclipse.swt.widgets.Composite)
			 */
			protected Table createTableControl(Composite parent) {
				Table t = super.createTableControl(parent);
				t.setHeaderVisible(true);
				TableColumn col = new TableColumn(t, SWT.LEFT);
				col.setText("Parameter");
				col.setWidth(150);
				
				col = new TableColumn(t, SWT.LEFT);
				col.setText("Description");
				col.setWidth(300);
		    	return t;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#getNewInputObject()
			 */
			@Override
			protected Object getNewInputObject() {
				GendocTemplateParameterDialog dlg = new GendocTemplateParameterDialog(Display.getCurrent().getActiveShell());
				dlg.setInput(new PreferenceGendocRunner.Parameter("new_parameter","New Parameter"));
				int res = dlg.open();
				if (res == Dialog.OK) {
					return dlg.getInput();
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#editInputObject(java.lang.Object)
			 */
			@Override
			protected Object editInputObject(Object input) {
				PreferenceGendocRunner.Parameter cur = (PreferenceGendocRunner.Parameter)input;
				GendocTemplateParameterDialog dlg = new GendocTemplateParameterDialog(Display.getCurrent().getActiveShell());
				dlg.setInput(new PreferenceGendocRunner.Parameter(cur.getName(),cur.getLabel()));
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
				return new ITableLabelProvider() {
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
					 */
					@Override
					public void removeListener(ILabelProviderListener listener) {}				
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
					 */
					@Override
					public boolean isLabelProperty(Object element, String property) {return true;}					
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
					 */
					@Override
					public void dispose() {}					
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
					 */
					@Override
					public void addListener(ILabelProviderListener listener) {}					
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
					 */
					@Override
					public String getColumnText(Object element, int columnIndex) {
						switch (columnIndex) {
							case 0: 
								return ((Parameter)element).getName();
							case 1: 
								return ((Parameter)element).getLabel();
							default:
								return "";								
						}
					}
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
					 */
					@Override
					public Image getColumnImage(Object element, int columnIndex) {
						return null;
					}
				};
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gendoc.preferences.internal.TableFieldEditor#refreshViewer()
			 */
			@Override
			protected void refreshViewer() {
				super.refreshViewer();
				bindingCtx.updateModels();
			}			
		};
		editor.adjustForNumColumns(2);
		bindingCtx.bindValue(
				PojoProperties.value("input").observe(editor), 
				PojoProperties.value("parameters").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));
		return c;
	}

}
