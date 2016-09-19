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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.gendoc.wizard.IGendocRunner;
import org.eclipse.gendoc.wizard.IGendocTemplate;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The Class GendocTemplateFileDialog is a dialog to add a 
 * new or edit an existing {@link IGendocTemplate} of a {@link IGendocRunner} 
 */
public class GendocTemplateFileDialog extends TitleAreaDialog {
	
	/** The input object the dialog edits. */
	private IGendocTemplate input;
	
	/** The binding context used to hold the data binding between the 
	  * UI widgets in the dialog and the input object and its properties. */
	private DataBindingContext bindingCtx = new DataBindingContext();

	/**
	 * Instantiates a new gendoc template file dialog.
	 *
	 * @param parentShell the parent SWT shell
	 */
	public GendocTemplateFileDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Gets the input of the dialog, a {@link IGendocTemplate}.
	 *
	 * @return the input of the dialog.
	 */
	public IGendocTemplate getInput() {
		return input;
	}

	/**
	 * Sets the input of the dialog. If the dialog is cancel the object
	 * is not restore to its original state, that is responsability of the
	 * client, that should clone or copy the object if needed.
	 *  
	 * @param input the new input of the dialog.
	 */
	public void setInput(IGendocTemplate input) {
		this.input = input;
		bindingCtx.updateTargets();
	}
	
	/** 
	 * The dialog provided by this dialog consists of input fields to edit:
	 * <ul>
	 * <li> Template File URL: the URL where the file is located. </li>
	 * <li> Output Format: The format file of the template file.</li>
	 * <li> Model parameter: The name of the parameter that contains the model. </li>
	 * <li> Output parameter: The name of the parameter indicating the output path of 
	 *      the result file. </li>
	 * <li> Description: A human readable description for the template file. </li>
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
		GridLayout layout = new GridLayout(4,false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		
		Label l = new Label(composite, SWT.FILL);
		l.setText("&Template File URL:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text file = new Text(composite, SWT.BORDER); 
		file.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("File...");
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[]{"*."+input.getOutPutExtension(),"*.*"});
				dialog.setText(String.format("Select the GenDoc template file (*.%s).",input.getOutPutExtension()));
				URL current = input.getTemplate(); 
				if (current != null && current.getProtocol().equals("file")) {
					File f = new File(current.getPath().replace("/", File.separator));
					dialog.setFileName(f.getName());
					dialog.setFilterPath(f.getParent());
				}				
				String res = dialog.open();
				if (res != null) {
					try {
						((PreferenceGendocTemplate)input).setTemplate(URIUtil.toURI(res).toURL());
						bindingCtx.updateTargets();
					} catch (MalformedURLException e1) {}
				}
			}			
		});

		Button browse2Button = new Button(composite, SWT.PUSH);
		browse2Button.setText("Workspace...");
		browse2Button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		browse2Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), 
					WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
					new WorkbenchContentProvider());
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				PatternFilter filter = new PatternFilter() {
					@Override
					public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
						if (viewer == null)
							return elements;
						return super.filter(viewer, parent, elements);
					}
				};
				filter.setPattern("*."+input.getOutPutExtension());
				dialog.addFilter(filter);
				dialog.setMessage(String.format("Select the GenDoc template file (*.%s).",input.getOutPutExtension()));
				dialog.setValidator(new ISelectionStatusValidator() {
					@Override
					public IStatus validate(Object[] selection) {
						if (selection == null || selection.length != 1)
							return ValidationStatus.error(String.format("A template file (*.%s) must be selected",input.getOutPutExtension()));
						if (!(selection[0] instanceof IFile))
							return ValidationStatus.error(String.format("A template file (*.%s) must be selected",input.getOutPutExtension()));
						IFile file = (IFile) selection[0];
						if (!file.exists())
							return ValidationStatus.error(String.format("The selected template file '%s' does not exists",file.getFullPath().toString()));
						return Status.OK_STATUS;
					}
				});
				URL current = input.getTemplate(); 
				if (current != null && current.getProtocol().equals("platform")) {
					String path = current.getPath();
					if (path.startsWith("/resource")) {
						path = path.substring("/resource".length());
						dialog.setInitialSelection(
								ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path)));
					}
				}
				int res = dialog.open();
				if (res == Dialog.OK) {
					IFile result = (IFile)dialog.getResult()[0];
					try {
						((PreferenceGendocTemplate)input).setTemplate(new URL("platform:/resource/"+
								URIUtil.toURI(result.getFullPath().makeRelative())));
						bindingCtx.updateTargets();
					} catch (MalformedURLException e1) {}
				}
			}			
		});
		
		l = new Label(composite, SWT.FILL);
		l.setText("&Output Format:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		ComboViewer outputFormatViewer = new ComboViewer(composite, SWT.READ_ONLY | SWT.BORDER); 
		outputFormatViewer.setContentProvider(new ArrayContentProvider());
		outputFormatViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,3, 0));
		List<String> formats = new ArrayList<String>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.gendoc.document.parser.factory");
        for (IConfigurationElement element : elements)
        	formats.add(element.getAttribute("extension"));
		outputFormatViewer.setInput(formats);

/*		Button defaultCheck = new Button(composite, SWT.CHECK);
		defaultCheck.setText("Default format");
		defaultCheck.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,4, 0));*/

		l = new Label(composite, SWT.FILL);
		l.setText("&Output parameter :");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text outputParameter = new Text(composite, SWT.BORDER);
		outputParameter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,3, 0));

		l = new Label(composite, SWT.FILL);
		l.setText("&Model parameter :");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text modelParameter = new Text(composite, SWT.BORDER);
		modelParameter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,3, 0));

		l = new Label(composite, SWT.FILL);
		l.setText("&Description:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Text description = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,3, 0));
		((GridData)description.getLayoutData()).minimumHeight = 80;
		
		IValidator templateValidator = new IValidator() {
			@Override
			public IStatus validate(Object value) {
				if (value == null)
					return ValidationStatus.error("A GenDoc template must specify a template file.");
				URL url = (URL)value;
				if (url.getProtocol().equals("file")) {
					if (!new File(url.getFile()).exists())
						return ValidationStatus.error("The GenDoc template file does not exists.");						
				} else if (url.getProtocol().equals("platform")) {
					String uri = url.getFile();
					if (uri.startsWith("/resource"))
						uri = uri.substring(9);
					IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(uri));
					if (file == null)
						return ValidationStatus.error("The GenDoc template file does not exists.");
				} else {
					return ValidationStatus.error("The URL schema for the GenDoc template file is not supported.");
				}
				return Status.OK_STATUS;
				
			}
		};
		
		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(file), 
				PojoProperties.value("template").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).setConverter(
						new Converter(String.class, URL.class) {							
							@Override
							public Object convert(Object fromObject) {
								if (fromObject == null || "".equals(fromObject))
									return null;
								try {
									return new URL(fromObject.toString());
								} catch (MalformedURLException e) {
									throw new IllegalArgumentException(e);
								}
							}
						}).setAfterConvertValidator(templateValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST).setConverter(
						new Converter(URL.class, String.class) {							
							@Override
							public Object convert(Object fromObject) {
								if (fromObject == null)
									return "";
								return ((URL)fromObject).toExternalForm(); 
							}
						}));

		bindingCtx.bindValue(
				ViewerProperties.singleSelection().observe(outputFormatViewer), 
				PojoProperties.value("outPutExtension").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

/*		bindingCtx.bindValue(
				WidgetProperties.selection().observe(defaultCheck), 
				PojoProperties.value("default").observe(this), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));*/

		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(outputParameter), 
				PojoProperties.value("outputKey").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		bindingCtx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(modelParameter), 
				PojoProperties.value("modelKey").observe(input), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE), 
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
					setAfterGetValidator(descriptionValidator), 
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		TitleAreaDialogSupport.create(this, bindingCtx);  

		bindingCtx.updateTargets();
		bindingCtx.updateModels();
		return composite;
	}
}
