/*******************************************************************************
 * Copyright (c) 2011 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anne Haugommard (Atos) anne.haugommard@atos.net - initial API and implementation
 *******************************************************************************/
package org.eclipse.gendoc.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Create a table for additional parameters
 */
public class AdditionnalParametersComposite extends Composite {

  private static final String NAME_PROPERTY = "name";

  private static final String VALUE_PROPERTY = "value";

  private TableViewer viewer;
  private Table table;
  

  public AdditionnalParametersComposite(Composite parent) {
    super(parent, SWT.NULL);
    buildControls();
  }

  public TableViewer getViewer() {
	return viewer;
  }

  public Table getTable() {
	return table;
  }

  protected void buildControls() {
    FillLayout compositeLayout = new FillLayout();
    setLayout(compositeLayout);

    table = new Table(this, SWT.FULL_SELECTION);
    table.setLinesVisible(true);
    viewer = buildAndLayoutTable(table);

    attachContentProvider(viewer);
    attachLabelProvider(viewer);
    attachCellEditors(viewer, table);

  }

  private void attachLabelProvider(TableViewer viewer) {
    viewer.setLabelProvider(new ITableLabelProvider() {
      public Image getColumnImage(Object element, int columnIndex) {
        return null;
      }

      public String getColumnText(Object element, int columnIndex) {
        switch (columnIndex) {
        case 0:
          return ((AdditionnalParameterItem) element).getLabel();
        case 1:
          return ((AdditionnalParameterItem) element).getValue();
        default:
          return "Invalid column: " + columnIndex;
        }
      }

      public void addListener(ILabelProviderListener listener) {
      }

      public void dispose() {
      }

      public boolean isLabelProperty(Object element, String property) {
        return false;
      }

      public void removeListener(ILabelProviderListener lpl) {
      }
    });
  }

  private void attachContentProvider(TableViewer viewer) {
    viewer.setContentProvider(new IStructuredContentProvider() {
      public Object[] getElements(Object inputElement) {
        return (Object[]) inputElement;
      }

      public void dispose() {
      }

      public void inputChanged(Viewer viewer, Object oldInput,
          Object newInput) {
      }
    });
  }

  private TableViewer buildAndLayoutTable(final Table table) {
    TableViewer tableViewer = new TableViewer(table);

    TableLayout layout = new TableLayout();
    layout.addColumnData(new ColumnWeightData(50, 75, true));
    layout.addColumnData(new ColumnWeightData(50, 75, true));
    table.setLayout(layout);

    TableColumn nameColumn = new TableColumn(table, SWT.CENTER);
    nameColumn.setText("Name");
    TableColumn valColumn = new TableColumn(table, SWT.LEFT);
    valColumn.setText("Value");
    table.setHeaderVisible(true);
    return tableViewer;
  }

  private void attachCellEditors(final TableViewer viewer, Composite parent) {
    viewer.setCellModifier(new ICellModifier() {
      public boolean canModify(Object element, String property) {
    	  if(property.equals(NAME_PROPERTY)) {
    		  return false;
    	  }
    	  return true;
      }

      public Object getValue(Object element, String property) {
        if (NAME_PROPERTY.equals(property))
          return ((AdditionnalParameterItem) element).getLabel();
        else
          return ((AdditionnalParameterItem) element).getValue();
      }

      public void modify(Object element, String property, Object value) {
        TableItem tableItem = (TableItem) element;
        AdditionnalParameterItem data = (AdditionnalParameterItem) tableItem
            .getData();
        if (NAME_PROPERTY.equals(property))
          data.setLabel(value.toString());
        else
          data.setValue(value.toString());

        viewer.refresh(data);
      }
    });
    viewer.setCellEditors(new CellEditor[] { new TextCellEditor(parent),
        new TextCellEditor(parent) });

    viewer
        .setColumnProperties(new String[] { NAME_PROPERTY,
            VALUE_PROPERTY });
  }
  
  public void setInput(Map<String, String> additionnalParameters) {
	  if(additionnalParameters != null) {
		  Object[] inputs = new Object[additionnalParameters.size()];
		  int i = 0;
		  for(String s : additionnalParameters.keySet()) {
			  inputs[i] = new AdditionnalParameterItem(additionnalParameters.get(s), s);
			  i++;
		  }
		  viewer.setInput(inputs);
	  }
	  else {
		  viewer.setInput(null);
	  }
  }
  
  public List<AdditionnalParameterItem> getAdditionnalParametersValue() {
	  List<AdditionnalParameterItem> items = new ArrayList<AdditionnalParameterItem>();
	  Object [] params = (Object[]) viewer.getInput();
	  if(params != null) {
		  for(int i = 0; i < params.length; i++) {
			  items.add((AdditionnalParameterItem) params[i]);
		  }
	  }
	  return items;
  }
  
}
           
