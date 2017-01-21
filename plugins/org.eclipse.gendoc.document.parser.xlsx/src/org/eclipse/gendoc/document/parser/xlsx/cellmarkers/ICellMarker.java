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
package org.eclipse.gendoc.document.parser.xlsx.cellmarkers;

import java.util.List;

import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;

public interface ICellMarker {
	
	public String getId();
	public void setMarks(XLSXParser parser);
	public List<CellMark> getMarksToApply(XLSXParser xlsxParser);
	public List<CellMark> getAppliedMarks(XLSXParser xlsxParser);
	public void layoutCells(XLSXParser parser, List<CellMark> marks);
	public void cleanup(XLSXParser parser);
}
