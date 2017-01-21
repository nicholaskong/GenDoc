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
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.gendoc.document.parser.xlsx.XLSXParser;

public abstract class AbstractCellRefMarker extends AbstractCellMarker implements ICellRefMarker {
	protected final String mark;
	protected final String relationType;
	protected final String xpath;
	
	public AbstractCellRefMarker(String mark, String xpath) {
		this(mark, null,xpath);
	}
	
	public AbstractCellRefMarker(String mark, String relationType, String xpath) {
		super(mark, relationType, xpath);
		this.mark = mark;
		this.relationType = relationType;
		this.xpath = xpath;
	}
	
	@Override
	public final void layoutCells(XLSXParser parser, List<CellMark> marks) {
	}
	
	@Override
	public void verifyCellReference(XLSXParser parser, Set<String> cellRefs) {
		try {
			for (CellMark m : getMarksToApply(parser))
				getCell(parser, m.cell, true);
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void setMarks(XLSXParser parser) {
		try {
			List<CellMark> marks = getMarksToApply(parser);
			for (CellMark m : marks) {
				markCell(parser, m);
			}
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException(e);
		}		
	}
}
