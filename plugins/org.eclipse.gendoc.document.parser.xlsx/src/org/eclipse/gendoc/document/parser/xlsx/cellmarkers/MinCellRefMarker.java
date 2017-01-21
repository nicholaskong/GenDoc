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

import java.util.Collections;
import java.util.List;

import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;

public class MinCellRefMarker extends CellRefMarker implements ICellRefMarker {
	public MinCellRefMarker(String mark, String xpath, String attrPath) {
		super(mark, xpath, attrPath);
	}

	public MinCellRefMarker(String mark, String relationType, String xpath, String attrPath) {
		super(mark, relationType, xpath, attrPath);
	}

	@Override
	public void layoutCellReference(XLSXParser xlsxParser, CellRef source, List<CellMark> targets) {
		CellMark min = null;
		for (CellMark m : targets) {
			if (min == null || m.cell.compareTo(min.cell) < 0)
				min = m;
		}
		
		if (min == null)
			return;
		super.layoutCellReference(xlsxParser, source, Collections.singletonList(min));
	}

}
