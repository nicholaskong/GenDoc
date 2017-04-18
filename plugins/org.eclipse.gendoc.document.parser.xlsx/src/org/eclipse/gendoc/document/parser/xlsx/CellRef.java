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
package org.eclipse.gendoc.document.parser.xlsx;

public class CellRef implements Comparable<CellRef> {
	private static final int COLUMN_BASE = 'Z'-'A' +1;

	public CellRef(String ref) {
		this.ref = ref;
		updateIndex();
	}
	
	public CellRef(int row, int col) {
		this.row = row;
		this.col = col;
		updateRef();
	}

	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
		updateIndex();
	}
	
	public int getRow() {
		return row;
	}
	
	public void setRow(int row) {
		this.row = row;
		updateRef();
	}
	
	public int getCol() {
		return col;
	}
	
	public void setCol(int col) {
		this.col = col;
		updateRef();
	}

	private void updateRef() {
		int c = col;
		int r = row;
			
		if (r < 0 || c < 0)
			throw new IllegalArgumentException("Invalid Cell index: ("+r+","+c+")");
		StringBuffer buf = new StringBuffer();
		do {
			int rest = c % COLUMN_BASE;
			buf.append((char)('A'+rest));
			c = c / COLUMN_BASE;
		} while (c != 0);
		
		buf.reverse().append(String.valueOf(r+1));
		ref = buf.toString();
	}
	
	private void updateIndex() {
		int c = 0;
		int r = -1;
		for (int i=0; i<ref.length(); i++) {
			char ch = ref.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				if (i != 0)
					c = (c+1) * COLUMN_BASE;
				c += ch-'A';
			} else if (i> 0 && ch >= '0' && ch<='9' ) {
				r = Integer.valueOf(ref.substring(i))-1;
				break;
			}
		}
		
		if (r < 0)
			throw new IllegalArgumentException("Invalid cell reference: " +ref);
		this.col = c;
		this.row = r;
	}
	
	@Override
	public int compareTo(CellRef other) {
		int res = this.row - other.row;
		return res == 0 ? (this.col - other.col) : res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellRef other = (CellRef) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ref;
	}

	private String ref;
	private int row;
	private int col;
}
