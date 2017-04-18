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

import org.eclipse.gendoc.document.parser.xlsx.CellRef;

public class CellMark implements Comparable<CellMark> {
	public CellMark(CellRef src, CellRef cell, String id, String xlPart, String path, String kind) {
		super();
		this.src = src;
		this.cell = cell;
		this.id = id;
		this.xlPart = xlPart;
		this.path = path;
		this.kind = kind;
	}
	
	@Override
	public int compareTo(CellMark o) {
		int res = id.compareTo(o.id);
		if (res != 0)
			return res;

		res = cell.compareTo(o.cell);
		if (res != 0)
			return res;
		
		res = Integer.valueOf(kind).compareTo(Integer.valueOf(o.kind));
		if (res != 0)
			return res;

		res = path.compareTo(o.path);
		if (res != 0)
			return res;

		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((xlPart == null) ? 0 : xlPart.hashCode());
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
		CellMark other = (CellMark) obj;
		if (cell == null) {
			if (other.cell != null)
				return false;
		} else if (!cell.equals(other.cell))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (xlPart == null) {
			if (other.xlPart != null)
				return false;
		} else if (!xlPart.equals(other.xlPart))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CellMark [id=" + id + ", src=" + src +", cell=" + cell + ", xlPart=" + xlPart + ", path=" + path + ", kind=" + kind
				+ "]";
	}

	public final CellRef src; 
	public final CellRef cell;
	public final String id;		
	public final String xlPart;		
	public final String path;
	public final String kind;
}
