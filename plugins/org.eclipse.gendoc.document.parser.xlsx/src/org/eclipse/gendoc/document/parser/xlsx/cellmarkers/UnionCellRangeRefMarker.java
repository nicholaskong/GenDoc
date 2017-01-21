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
import java.util.Map;

public class UnionCellRangeRefMarker extends CellRangeRefMarker implements ICellRefMarker {

	public UnionCellRangeRefMarker(String mark, String xpath, String attrPath) {
		super(mark, xpath, attrPath);
	}

	public UnionCellRangeRefMarker(String mark, String relationType, String xpath, String attrPath) {
		super(mark, relationType, xpath, attrPath);
	}

	@Override
	protected Map<String, List<CellMark>[]> calculateMarkRanges(List<CellMark> marks) {
		Map<String, List<CellMark>[]> markRanges = super.calculateMarkRanges(marks);
		for (List<CellMark>[] ls : markRanges.values()) {
			for (int r=0; r<ls.length; r+=2) {
				CellMark min = null;
				CellMark max = null;

				for (CellMark m : ls[r]) {
					if (min == null || m.cell.compareTo(min.cell) < 0)
						min = m;
				}
	
				for (CellMark m : ls[r+1]) {
					if (max == null || m.cell.compareTo(max.cell) > 0)
						max = m;				
				}
				
				ls[r] = Collections.singletonList(min);
				ls[r+1] = Collections.singletonList(max);
			}
		}
		return markRanges;
	}

	
}
