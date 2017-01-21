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

import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;

public class FirstCellRefMarker extends CellRefMarker implements ICellRefMarker {

	public FirstCellRefMarker(String mark, String xpath, String attrPath) {
		super(mark, xpath, attrPath);
	}

	public FirstCellRefMarker(String mark, String relationType, String xpath, String attrPath) {
		super(mark, relationType, xpath, attrPath);
	}

	@Override
	public void layoutCellReference(XLSXParser xlsxParser, CellRef source, List<CellMark> targets) {
		if (targets.size() == 0)
			return;
		super.layoutCellReference(xlsxParser, source, targets.subList(0, 1));
	}

}
