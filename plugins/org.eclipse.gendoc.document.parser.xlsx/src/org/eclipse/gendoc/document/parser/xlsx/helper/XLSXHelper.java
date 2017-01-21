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
package org.eclipse.gendoc.document.parser.xlsx.helper;

/**
 * The Class XLSXHelper.
 */
public final class XLSXHelper
{    
	public static final String GENDOC_NS = "http://gendoc.eclipse.org/spreadsheetml/main";

	/** The Constant CONTENTS_FILE_NAME. */
    
    public static final String ROW = "row";

    public static final String CELL = "c";
    
    public static final String CELL_TYPE = "t";

    public static final String CELL_VALUE = "v";

    public static final String CELL_VALUE_SHARED_STRING = "s";

    public static final String CELL_INLINE_STRING = "is";

    public static final String CELL_INLINE_STRING_TEXT = "t";

    public static final String SHEET_ID = "r:id";

    public static final String SHARED_STRING_FILE = "sharedStrings.xml";

    public static final String ATTRIBUTE_COUNT = "uniqueCount";

    // Content Type(s):
    public static final String WORKBOOK_FILE_NAME = "workbook.xml";    
    public static final String WORKBOOK_RELS_FILE_NAME = "workbook.xml.rels";
    public static final String WORKBOOK_PATH = "/xl/";
    public static final String WORKBOOK_REL_PATH = "/xl/_rels/";
    public static final String WORKBOOK_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.main+xml";
    public static final String WORKBOOK_TEMPLATE_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.template.main+xml";
    public static final String WORKBOOK_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
    
    // Worksheet
    public static final String WORKSHEET_PATH = "/xl/worksheets/";
    public static final String WORKSHEET_REL_PATH = "/xl/worksheets/_rels/";
    public static final String WORKSHEET_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";
    public static final String WORKSHEET_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    public static final String WORKSHEET_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet";

    // Drawing
    public static final String DRAWING_PATH = "/xl/drawings/";
    public static final String DRAWING_REL_PATH = "/xl/drawings/_rels/";
    public static final String DRAWING_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.drawing+xml";
    public static final String DRAWING_NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing";
    public static final String DRAWING_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing";
    
    // Comments
    public static final String COMMENTS_PATH = "/xl/comments/";
    public static final String COMMENTS_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.comments+xml";
    public static final String COMMENTS_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";

    // Styles
    public static final String STYLES_PATH = "/xl/";
    public static final String STYLES_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml";
    public static final String STYLES_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";

    // Single Cell Table
    public static final String SINGLE_CELL_TABLE__PATH = "/xl/tables/";
    public static final String SINGLE_CELL_TABLE_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.tableSingleCells+xml";
    public static final String SINGLE_CELL_TABLE_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableSingleCells";
    
    // Pivot Table 
    public static final String PIVOT_TABLE_PATH = "/xl/pivotTables/";
    public static final String PIVOT_TABLE_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.pivotTable+xml";
    public static final String PIVOT_TABLE_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotTable";

    // Volatile Dependencies
    public static final String VOLATILE_DEPS_PATH = "/";
    public static final String VOLATILE_DEPS_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.volatileDependencies+xml";
    public static final String VOLATILE_DEPS_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/volatileDependencies";
    
    public static final String DRAWING_ML_NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";
    public static final String RELATIONSHIPS_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    public static final String PACKAGE_RELATIONSHIPS_NAMESPACE = "http://schemas.openxmlformats.org/package/2006/relationships";
    public static final String CONTENT_TYPES_NAMESPACE = "http://schemas.openxmlformats.org/package/2006/content-types";
}
