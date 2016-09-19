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
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

/**
 * The Class TableFieldEditor provides the ground for a @{link FieldEditor} that 
 * handle a list of values, displaying a table and a set of button to add, edit,
 * remove and order those object.
 */
public abstract class TableFieldEditor extends FieldEditor {

    /** The viewer wrapping the table. */
    private TableViewer viewer;
    
    /** The object list input. */
    private List<Object> input;
    
    /** The selection listener. */
    private SelectionListener selectionListener;
	
	/** The parent composite where the buttons are created. */
	private Composite buttonBox;
	
	/** The add button. */
	private Button addButton;
	
	/** The edit button. */
	private Button editButton;
	
	/** The remove button. */
	private Button removeButton;
	
	/** The up button. */
	private Button upButton;
	
	/** The down button. */
	private Button downButton;

	/**
	 * Instantiates a new table field editor.
	 */
	public TableFieldEditor() {
	}

	/**
	 * Instantiates a new table field editor.
	 *
	 * @param name the name of the preference this editor works on.
	 * @param labelText the label text of the field editor.
	 * @param parent the parent of the field editor's control.
	 */
	public TableFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

    /**
	 * This method is invoked to create the table widget used by the editor.<br>
	 * 
	 * Extensions should override this method to customize the table, i.e to 
	 * provides some columns or provide a table with different styles. 
	 *
	 * @param parent the parent of the table control.
	 * @return the table to be used by the editor.
	 */
    protected Table createTableControl(Composite parent) {
		Table t = new Table(parent, SWT.SIMPLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL);
    	return t;
	}
    
	/**
	 * Gets the list object this editor work on.
	 *
	 * @return a list of objects
	 */
	public List<Object> getInput() {
		return input;
	}

	/**
	 * Gets the list object this editor work on.
	 *
	 * @param input the new input list.
	 */
	public void setInput(List<Object> input) {
		this.input = input;
		if (viewer != null)
			viewer.setInput(input);
	}

	/**
	 * Gets the table viewer wrapping the table control.
	 *
	 * @return the table viewer
	 */
	protected TableViewer getTableViewer() {
		return viewer;
	}
	
	/**
	 * Gets the table control of this editor.
	 *
	 * @return the table control
	 */
	protected Table getTable() {
		return viewer.getTable();
	}

	/**
	 * This method is invoke to retrieve a String representation of a set 
	 * of object. This string can be used to store it in the preference or 
	 * as a workbench object property. 
	 *
	 * @param objs the objs to serialize.
	 * @return the string representing the list of given objects
	 */
	protected String createList(Object[] objs) {throw new UnsupportedOperationException();}
	
	/**
	 * This method is invoke to retrieve an array of object represented by the
	 * given String.
	 *
	 * @param s the string representation a list of objects
	 * @return the object[]
	 */
	protected Object[] parseString(String s) {throw new UnsupportedOperationException();}

    
    /**
	 * Create a new object to add to the list this editor works on. The method
	 * is invoked when a new object is required to be added to the list. <br>
	 * 
	 * The extensions of this class should be override this method to provide a 
	 * object initialize to a set of default values, or provide a mechanism to
	 * ask the user for the required values. 
	 *
	 * @return the new input object
	 */
    protected abstract Object getNewInputObject();
    
    /**
	 * Create a new object after being mofify for any means by the user. <br>
	 * 
	 * The extensions of this class should be override this method to provide 
	 * an edit mechanism user, i.e. a dialog. 
	 *
	 * @param input the input object to edit
	 * @return a object with the changes provided by the user. It probably not 
	 *         the same as the input object. 
	 */
    protected abstract Object editInputObject(Object input);
    
    /**
	 * Creates the label provider to be used by the table viewer to 
	 * provide human readable representation of the objects the editor 
	 * work on.
	 *
	 * @return the table label provider
	 */
    protected abstract ITableLabelProvider createLabelProvider();
	
    /**
	 * This method is invoked when the Adds button is pressed.<br>
	 *  
	 * The default implementation calls the {@link #getNewInputObject()} and 
	 * it uses the retrned object to add to the input object list and to the 
	 * table control at the position currently selected.  
	 */
    protected void addPressed() {
        setPresentsDefaultValue(false);
        Object obj = getNewInputObject();

        if (obj != null) {
            int index = viewer.getTable().getSelectionIndex();
            if (index >= 0) {
				input.add(index + 1, obj);
			} else {
				input.add(0, obj);
			}
            refreshViewer();
            selectionChanged();
        }
	}
	
    /**
	 * This method is invoked when the Edit button is pressed.<br>
	 *  
	 * The default implementation calls the {@link #editInputObject()} with 
	 * the object select in the table and uses the returned object to replace it 
	 * in the input object list and in the table control.  
	 */
    protected void editPressed() {
        setPresentsDefaultValue(false);
        int index = viewer.getTable().getSelectionIndex();
        if (index >= 0) {
        	Object obj = input.get(index);
        	obj = editInputObject(obj);
        	if (obj != null) {
        		input.set(index, obj);
                refreshViewer();
                selectionChanged();
        	}
        }
    }

	/**
	 * This method is invoked when the Remove button is pressed.<br>
	 * 
	 * The default implementation, removes the object selected from the 
	 * table control and from the input object list.  
	 */
	protected void removePressed() {
        setPresentsDefaultValue(false);
        int index = viewer.getTable().getSelectionIndex();
        if (index >= 0) {
            input.remove(index);
			viewer.getTable().select(index >= input.size() ? index - 1 : index);
			refreshViewer();
            selectionChanged();
        }
	}

	/**
	 * Invoked when Up button is pressed. <br>
	 * 
	 * The default implementation
	 * reorder the object one position up, in the table and in the ¨
	 * inut object list.
	 */
	protected void upPressed() {
		swap(true);
	}

	/**
	 * Invoked when Doen button is pressed. <br>
	 * 
	 * The default implementation
	 * reorder the object one position down, in the table and in the ¨
	 * inut object list.
	 */
	protected void downPressed() {
		swap(false);
	}

	/**
	 * Implement a the swap operation on the table control and in the input
	 * object list, one position up or down, depending on the value of the given
	 * parameter.
	 *
	 * @param up true if the swap operation is with the previous object (up) or with 
	 *           the following object (down). 
	 */
	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		Table table = viewer.getTable();
		int index = table.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (index >= 0 && selection.size() == 1) {
			input.remove(index);
			input.add(target, selection.getFirstElement());
			table.setSelection(target);
		}
        refreshViewer();
		selectionChanged();
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
     */
    @Override
	protected void adjustForNumColumns(int numColumns) {
        Control control = getLabelControl();
        ((GridData) control.getLayoutData()).horizontalSpan = numColumns;
        ((GridData) viewer.getTable().getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		createSelectionListener();
        Control control = getLabelControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        control.setLayoutData(gd);

        Table table = createTableControl(parent);
        viewer = new TableViewer(table);
        viewer.setLabelProvider(createLabelProvider());
        viewer.setContentProvider(new ArrayContentProvider());
        if (input == null)
    		input = new ArrayList<Object>();
        viewer.setInput(input);
        viewer.getTable().addSelectionListener(selectionListener);
        input = (List<Object>)viewer.getInput();
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = true;
        table.setLayoutData(gd);

        buttonBox = getButtonBoxControl(parent);
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        buttonBox.setLayoutData(gd);
	}

	/**
	 * Create the pane with the buttons, Add, Edit, Remove, Up and Down 
	 *
	 * @param parent
	 *            the parent
	 * @return the button box control
	 */
	public Composite getButtonBoxControl(Composite parent) {
        if (buttonBox == null) {
            buttonBox = new Composite(parent, SWT.NULL);
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            buttonBox.setLayout(layout);
            createButtons(buttonBox);
            buttonBox.addDisposeListener(new DisposeListener() {
                @Override
				public void widgetDisposed(DisposeEvent event) {
                    addButton = null;
                    editButton = null;
                    removeButton = null;
                    upButton = null;
                    downButton = null;
                    buttonBox = null;
                }
            });

        } else {
            checkParent(buttonBox, parent);
        }

        selectionChanged();
        return buttonBox;
    }

    /**
	 * This function creates all the button controls.
	 *
	 * @param box the parent of the buttons.
	 */
    private void createButtons(Composite box) {
        addButton = createPushButton(box, JFaceResources.getString("ListEditor.add"));//$NON-NLS-1$
        editButton = createPushButton(box, "Edit...");//$NON-NLS-1$
        removeButton = createPushButton(box, JFaceResources.getString("ListEditor.remove"));//$NON-NLS-1$
        upButton = createPushButton(box, JFaceResources.getString("ListEditor.up"));//$NON-NLS-1$
        downButton = createPushButton(box, JFaceResources.getString("ListEditor.down"));//$NON-NLS-1$
    }

    /**
	 * Creates a push button and add to the button selection listener, {@link #selectionListener}. 
	 *
	 * @param parent the parentof the button
	 * @param text the text of the button
	 * @return the created button. 
	 */
    private Button createPushButton(Composite parent, String text) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        int widthHint = convertHorizontalDLUsToPixels(button,
                IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        button.addSelectionListener(selectionListener);
        return button;
    }

    /**
	 * Creates the selection listener, {@link #selectionListener}, which
	 * invoke the corresponding methods, {@link #addPressed()}, {@link #editPressed()},
	 * {@link #removePressed()}, {@link #upPressed()}, {@link #downPressed()}
	 *
	 * @return the selection listener
	 */
    protected SelectionListener createSelectionListener() {
        selectionListener = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
                Widget widget = event.widget;
                if (widget == addButton) {
                    addPressed();
                } else if (widget == removeButton) {
                    removePressed();
                } else if (widget == upButton) {
                    upPressed();
                } else if (widget == downButton) {
                    downPressed();
                } else if (widget == editButton) {
                    editPressed();
                } else if (widget == viewer.getTable()) {
                    selectionChanged();
                }
            }
        };
        return selectionListener;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doLoad()
     */
    @Override
	protected void doLoad() {
        if (viewer != null) {
            String s = getPreferenceStore().getString(getPreferenceName());
            Object[] array = parseString(s);
            for (int i = 0; i < array.length; i++) {
                input.add(array[i]);
            }
            refreshViewer();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
     */
    @Override
	protected void doLoadDefault() {
        if (viewer != null) {
            input.clear();
            String s = getPreferenceStore().getDefaultString(
                    getPreferenceName());
            Object[] array = parseString(s);
            for (int i = 0; i < array.length; i++) {
                input.add(array[i]);
            }
            refreshViewer();
        }
    }

    /**
	 * Refresh the table viewer.
	 */
    protected void refreshViewer() {
    	viewer.refresh();
	}

	/**
	 * The method is invoked when the table selection change to
	 * update the state of the buttons.
	 */
	protected void selectionChanged() {
        int index = viewer.getTable().getSelectionIndex();
        int size = viewer.getTable().getItemCount();

        editButton.setEnabled(index >= 0);
        removeButton.setEnabled(index >= 0);
        upButton.setEnabled(size > 1 && index > 0);
        downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doStore()
     */
    @Override
	protected void doStore() {
        String s = createList(input.toArray());
        if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}
}
