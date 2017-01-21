package org.eclipse.gendoc.services.xlsx.tests;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.gendoc.document.parser.documents.helper.UnitsHelper;
import org.eclipse.gendoc.document.parser.documents.helper.UnitsHelper.Unit;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.services.xlsx.tests.impl.XlsxTestInputTemplate;
import org.eclipse.gendoc.services.xlsx.tests.impl.XlsxTestSupport;
import org.eclipse.gendoc.services.xlsx.tests.impl.XlsxVerifyHelper;
import org.junit.Test;

public class XlsxSimpleTest {
	
	
	@Test
	public void simpleTestLoopMultipleRowMultipleCols() throws Exception {
		String testName = "simpleTestLoopMultipleRowMultipleCols";
		XlsxTestInputTemplate template = new XlsxTestInputTemplate(new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
	            "resources/template.xlsx"));
	    template.replaceXmlNode("/xl/worksheets/sheet1.xml", "//:sheetData", 
	    		XlsxTestSupport.generateSheetDataXml(new CellRef("A1"), new String[][] {
	    			new String[] {
	    					"<config>\n<output path='${output}'/>\n</config>\n<context model='${model}' importedBundles='gmf;papyrus;commons' "
	    					+ "searchMetamodels='false'/>\n<drop/>",
	    					null},	    			
	    			new String[] {
	    					"<gendoc>\n[for (p : uml::Package | self.ownedElement->filter(uml::Package)->sortedBy(name))]\nIndex: [i/]",
	    					"Name: [p.name/]"},	 
	    			new String[] {
	    					"[/for]\n</gendoc> <drop/>",
	    					null
	    			}
	    		}));
	    
		XlsxVerifyHelper verifier = XlsxTestSupport.execute(testName, 
				template, 
				new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
						"resources/XlsxSimpleTest.uml"), 
				new URL("platform:/meta/org.eclipse.gendoc.services.xlsx/test/"+
						testName+".res.xlsx"));

		try {
			verifier.verifyCellEqual("B1", "/xl/worksheets/sheet1.xml", "Name: Deployment View");
			verifier.verifyCellEqual("A1", "/xl/worksheets/sheet1.xml", "Index: 1");

			verifier.verifyCellEqual("B2", "/xl/worksheets/sheet1.xml", "Name: Implementation View");
			verifier.verifyCellEqual("A2", "/xl/worksheets/sheet1.xml", "Index: 2");

			verifier.verifyCellEqual("B3", "/xl/worksheets/sheet1.xml", "Name: Logical View");
			verifier.verifyCellEqual("A3", "/xl/worksheets/sheet1.xml", "Index: 3");

			verifier.verifyCellEqual("B4", "/xl/worksheets/sheet1.xml", "Name: Use Case View");
			verifier.verifyCellEqual("A4", "/xl/worksheets/sheet1.xml", "Index: 4");

			verifier.verifyNull("Marks", "/xl/worksheets/sheet1.xml", "//:sheetData//gendoc-mark");
		} finally {
			verifier.dispose();
		}
	}
	
	@Test
	public void simpleTestLoopMultipleRowMultipleColsWithGaps() throws Exception {
		String testName = "simpleTestLoopMultipleRowMultipleColsWithGaps";
		XlsxTestInputTemplate template = new XlsxTestInputTemplate(new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
	            "resources/template.xlsx"));
	    template.replaceXmlNode("/xl/worksheets/sheet1.xml", "//:sheetData", 
	    		XlsxTestSupport.generateSheetDataXml(new CellRef("A1"), new String[][] {
	    			new String[] {
	    					"<config>\n<output path='${output}'/>\n</config>\n<context model='${model}' importedBundles='gmf;papyrus;commons' "
	    					+ "searchMetamodels='false'/>\n<drop/>"},	    			
	    			null,
	    			new String[] {
	    					"<gendoc>\n[for (p : uml::Package | self.ownedElement->filter(uml::Package)->sortedBy(name))]<drop/>"},
	    			null, 
	    			new String[] {
	    					"Index: [i/]",
	    					"Name: [p.name/]"},
	    			null,
	    			new String[] {
	    					"[/for]\n</gendoc> <drop/>"
	    			}
	    		}));
	    
    	XlsxVerifyHelper verifier = XlsxTestSupport.execute(testName, 
				template, 
				new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
						"resources/XlsxSimpleTest.uml"), 
				new URL("platform:/meta/org.eclipse.gendoc.services.xlsx/test/"+
						testName+".res.xlsx"));

		try {
			verifier.verifyCellEqual("B3", "/xl/worksheets/sheet1.xml", "Name: Deployment View");
			verifier.verifyCellEqual("A3", "/xl/worksheets/sheet1.xml", "Index: 1");

			verifier.verifyCellEqual("B6", "/xl/worksheets/sheet1.xml", "Name: Implementation View");
			verifier.verifyCellEqual("A6", "/xl/worksheets/sheet1.xml", "Index: 2");

			verifier.verifyCellEqual("B9", "/xl/worksheets/sheet1.xml", "Name: Logical View");
			verifier.verifyCellEqual("A9", "/xl/worksheets/sheet1.xml", "Index: 3");

			verifier.verifyCellEqual("B12", "/xl/worksheets/sheet1.xml", "Name: Use Case View");
			verifier.verifyCellEqual("A12", "/xl/worksheets/sheet1.xml", "Index: 4");

			verifier.verifyNull("Marks", "/xl/worksheets/sheet1.xml", "//:sheetData//gendoc-mark");
		} finally {
			verifier.dispose();
		}
	}
	
	@Test
	public void simpleTestLoopMultipleRowMultipleColsGapsAndBreaks() throws Exception {
		String testName = "simpleTestLoopMultipleRowMultipleColsGapsAndBreaks";
		XlsxTestInputTemplate template = new XlsxTestInputTemplate(new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
	            "resources/template.xlsx"));
	    template.replaceXmlNode("/xl/worksheets/sheet1.xml", "//:sheetData", 
	    		XlsxTestSupport.generateSheetDataXml(new CellRef("A1"), new String[][] {
	    			new String[] {
	    					"<config>\n<output path='${output}'/>\n</config>\n<context model='${model}' importedBundles='gmf;papyrus;commons' "
	    					+ "searchMetamodels='false'/>\n<drop/>"},	    			
	    			null,
	    			new String[] {
	    					"<gendoc>\n[for (p : uml::Package | self.ownedElement->filter(uml::Package)->sortedBy(name))]<drop/>"},
	    			null, 
	    			new String[] {
	    					"Index: [i/]",
	    					"Name: [p.name/]"},
	    			null, 	    			
	    			new String[] {
	    					"[/for]\n</gendoc> <drop/>"
	    			}
	    		}));
	    
	    template.appendXmlNode("/xl/worksheets/sheet1.xml", "//:worksheet",
	    		"<rowBreaks count=\"1\" manualBreakCount=\"1\">"+
	    		"<brk id=\"6\" max=\"16383\" man=\"1\"/>"+
	    		"</rowBreaks>");
		XlsxVerifyHelper verifier = XlsxTestSupport.execute(testName, 
				template, 
				new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
						"resources/XlsxSimpleTest.uml"), 
				new URL("platform:/meta/org.eclipse.gendoc.services.xlsx/test/"+
						testName+".res.xlsx"));

		try {
			verifier.verifyCellEqual("B3", "/xl/worksheets/sheet1.xml", "Name: Deployment View");
			verifier.verifyCellEqual("A3", "/xl/worksheets/sheet1.xml", "Index: 1");
			verifier.verifyNotNull("Break Row 4", "/xl/worksheets/sheet1.xml", 
					"//:worksheet/:rowBreaks/:brk[@id='4' and @max='16383' and @man='1']");

			verifier.verifyCellEqual("B6", "/xl/worksheets/sheet1.xml", "Name: Implementation View");
			verifier.verifyCellEqual("A6", "/xl/worksheets/sheet1.xml", "Index: 2");
			verifier.verifyNotNull("Break Row 7", "/xl/worksheets/sheet1.xml", 
					"//:worksheet/:rowBreaks/:brk[@id='7' and @max='16383' and @man='1']");

			verifier.verifyCellEqual("B9", "/xl/worksheets/sheet1.xml", "Name: Logical View");
			verifier.verifyCellEqual("A9", "/xl/worksheets/sheet1.xml", "Index: 3");
			verifier.verifyNotNull("Break Row 10", "/xl/worksheets/sheet1.xml", 
					"//:worksheet/:rowBreaks/:brk[@id='10' and @max='16383' and @man='1']");

			verifier.verifyCellEqual("B12", "/xl/worksheets/sheet1.xml", "Name: Use Case View");
			verifier.verifyCellEqual("A12", "/xl/worksheets/sheet1.xml", "Index: 4");			
			verifier.verifyNotNull("Break Row 13", "/xl/worksheets/sheet1.xml", 
					"//:worksheet/:rowBreaks/:brk[@id='13' and @max='16383' and @man='1']");
			
			verifier.verifyNull("Marks", "/xl/worksheets/sheet1.xml", "//:sheetData//gendoc:mark");
		} finally {
			verifier.dispose();
		}
	}


	@Test
	public void simpleTestLoopMultipleRowMultipleColsGapsAndDiagrams() throws Exception {
		
		new XlsxVerifyHelper(URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
	            "resources/template.xlsx")))).getPath());
		
		String testName = "simpleTestLoopMultipleRowMultipleColsGapsAndDiagrams";
		XlsxTestInputTemplate template = new XlsxTestInputTemplate(new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
	            "resources/template.xlsx"));

		template.replaceXmlNode("/xl/worksheets/sheet1.xml", "//:sheetData", 
	    		XlsxTestSupport.generateSheetDataXml(new CellRef("A1"), new String[][] {
	    			new String[] {
	    					"<config>\n<output path='${output}'/>\n</config>\n<context model='${model}' importedBundles='gmf;papyrus;commons' "
	    					+ "searchMetamodels='false'/>\n<drop/>"},	    			
	    			null,
	    			new String[] {
	    					"<gendoc>\n[for (p : uml::Package | self.ownedElement->filter(uml::Package)->sortedBy(name))]<drop/>"},
	    			null, 
	    			new String[] {
	    					"Index: [i/]",
	    					"Name: [p.name/]"},
	    			new String[] {
	    					"[for (d : notation::Diagram | p.getPapyrusDiagrams())]<drop/>",
	    			},
	    			new String[] {
	    					"<image object='[d.getDiagram()/]'></image>"
	    			},	    			
	    			new String[] {
	    					"[/for][/for]\n</gendoc><drop/>"
	    			}
	    		}));
	    
	    template.replaceXmlNode("/xl/worksheets/sheet1.xml", 
	    		"//:worksheet/:cols/:col[1]", 
	    		"<col min=\"1\" max=\"1\" width=\"60\" customWidth=\"1\"/>");

	    template.setXmlAttribute("/xl/worksheets/sheet1.xml", 
	    		"//:worksheet/:sheetData/:row[@r='7']",
	    		"ht", "130.0");

	    template.setXmlAttribute("/xl/worksheets/sheet1.xml", 
	    		"//:worksheet/:sheetData/:row[@r='7']",
	    		"customHeight", "1");
	    
	    // Adding drawing1.xml	    
	    template.replaceFile("/xl/worksheets/_rels/sheet1.xml.rels", 
	    		new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
			    "<Relationships "+
			    	"xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"+
			    	"<Relationship Id=\"rId1\" "+
			    		"Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing\" "+
			    		"Target=\"../drawings/drawing1.xml\" />"+
			    "</Relationships>"));
	    	    
			template.appendXmlNode("[Content_Types].xml",
	    		"/ct:Types",
	    		"<ct:Override PartName=\"/xl/drawings/drawing1.xml\" "+
	    		"ContentType=\"application/vnd.openxmlformats-officedocument.drawing+xml\"/>");

		template.replaceFile("/xl/drawings/drawing1.xml",
	    	     URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(new URL(
	    	    		 "platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/resources/template_drawing.xml"))))); 

		// Moving picture to row 7 and marging of 4 points
		template.replaceXmlNode("/xl/drawings/drawing1.xml",
	    		"/xdr:wsDr/xdr:twoCellAnchor[1]/xdr:from", 
	    		"<xdr:from><xdr:col>0</xdr:col><xdr:colOff>"+(long)UnitsHelper.convert(Unit.POINT, 4, Unit.EMU)+"</xdr:colOff>" +
	    		"<xdr:row>6</xdr:row><xdr:rowOff>"+(long)UnitsHelper.convert(Unit.POINT, 4, Unit.EMU)+"</xdr:rowOff></xdr:from>");
	    		
	    template.replaceXmlNode("/xl/drawings/drawing1.xml",
	    		"/xdr:wsDr/xdr:twoCellAnchor[1]/xdr:to", 
	    		"<xdr:to><xdr:col>0</xdr:col><xdr:colOff>"+(long)UnitsHelper.convert(Unit.POINT, 122, Unit.EMU)+"</xdr:colOff>" +
	    		"<xdr:row>6</xdr:row><xdr:rowOff>"+(long)UnitsHelper.convert(Unit.POINT, 122, Unit.EMU)+"</xdr:rowOff></xdr:to>");
			
	    template.replaceFile("/xl/drawings/_rels/drawing1.xml.rels", 
	    		new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
	    				"<Relationships "+
	    					"xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"+
	    					"<Relationship Id=\"rId1\" "+
	    					"Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" "+
	    					"Target=\"../media/template_image.png\" />" +
	    				"</Relationships>"));
	    
	    template.appendXmlNode("/xl/worksheets/sheet1.xml",
	    		"/:worksheet",
	    		"<drawing r:id=\"rId1\"/>");
	    
	    // Add Media File
	    template.appendXmlNodeBefore("[Content_Types].xml",
	    		"//ct:Types//ct:Override[1]",
	    		"<ct:Default Extension=\"png\" ContentType=\"image/png\"/>");	    
	    
	    template.replaceFile("/xl/media/template_image.png",
	    	     URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(new URL(
	    	    		 "platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/resources/template_image.png")))));

	    XlsxVerifyHelper verifier = XlsxTestSupport.execute(testName, 
				template, 
				new URL("platform:/fragment/org.eclipse.gendoc.services.xlsx.tests/"+
						"resources/XlsxSimpleTest.uml"), 
				new URL("platform:/meta/org.eclipse.gendoc.services.xlsx/test/"+
						testName+".res.xlsx"));

		try {
			verifier.verifyCellEqual("B3", "/xl/worksheets/sheet1.xml", "Name: Deployment View");
			verifier.verifyCellEqual("A3", "/xl/worksheets/sheet1.xml", "Index: 1");

			verifier.verifyCellEqual("B5", "/xl/worksheets/sheet1.xml", "Name: Implementation View");
			verifier.verifyCellEqual("A5", "/xl/worksheets/sheet1.xml", "Index: 2");

			verifier.verifyCellEqual("B8", "/xl/worksheets/sheet1.xml", "Name: Logical View");
			verifier.verifyCellEqual("A8", "/xl/worksheets/sheet1.xml", "Index: 3");

			verifier.verifyCellEqual("B10", "/xl/worksheets/sheet1.xml", "Name: Use Case View");
			verifier.verifyCellEqual("A10", "/xl/worksheets/sheet1.xml", "Index: 4");			
			
			verifier.verifyNull("Marks", "/xl/worksheets/sheet1.xml", "//:sheetData//gendoc:mark");
		} finally {
			verifier.dispose();
		}
	}
}