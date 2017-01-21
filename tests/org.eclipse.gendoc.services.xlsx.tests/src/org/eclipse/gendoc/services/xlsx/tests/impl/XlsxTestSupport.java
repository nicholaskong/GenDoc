package org.eclipse.gendoc.services.xlsx.tests.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.gendoc.GendocProcess;
import org.eclipse.gendoc.document.parser.xlsx.CellRef;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.IGendocDiagnostician;
import org.eclipse.gendoc.services.IProgressMonitorService;
import org.eclipse.gendoc.services.exception.GenDocException;
import org.eclipse.gendoc.tags.handlers.IConfigurationService;
import org.junit.Assert;

public class XlsxTestSupport {
	public static String generateSheetDataXml(CellRef ref, String[][] cellValues) {
		StringBuffer buf = new StringBuffer();
		buf.append("<sheetData xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		int rIndex = ref.getRow(); 
		for (String[] rowCells : cellValues) {
			if (rowCells != null) {
				buf.append(generateRowXml(new CellRef(rIndex, ref.getCol()), rowCells));				
			}
			rIndex++;
		}
		buf.append("</sheetData>");
		return buf.toString();
	}
	
	public static String generateRowXml(CellRef ref, String[] cells) {
		StringBuffer buf = new StringBuffer();
		buf.append("<row xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" r=\"").append(ref.getRow()+1).append("\">");
		int nCol = ref.getCol();
		for (String cell : cells) {
			if (cell != null) {
				buf.append("<c r=\"").append(new CellRef(ref.getRow(),nCol).getRef()).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">");
				buf.append(cell.replace("&","&amp;").replace("<", "&lt;").replace(">", "&gt;"));
				buf.append("</t></is></c>");
			}
			nCol++;
		}		
		buf.append("</row>");
		return buf.toString();
	}

	public static XlsxVerifyHelper execute(String test, XlsxTestInputTemplate input, URL model, URL output) 
			throws IOException, URISyntaxException, GenDocException, TransformerException, ParserConfigurationException  {
		URL newInput = new URL("platform:/meta/org.eclipse.gendoc.services.xlsx/test/"+
				test+".input.xlsx");
	    File f = URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(newInput)));
	    f.getParentFile().mkdirs();
	    input.generate(f);
		return execute(test, newInput, model, output);
	}
	
	public static XlsxVerifyHelper execute(String test, URL input, URL model, URL output) 
			throws IOException, URISyntaxException, GenDocException, ParserConfigurationException  {
		IGendocDiagnostician diagnostician = GendocServices.getDefault().getService(IGendocDiagnostician.class);
		diagnostician.init();
		IProgressMonitorService monitorService = (IProgressMonitorService) GendocServices.getDefault().getService(IProgressMonitorService.class);
		monitorService.setMonitor(new NullProgressMonitor());

		File f = URIUtil.toFile(URIUtil.toURI(FileLocator.resolve(output)));
		f.getParentFile().mkdirs();

		IConfigurationService parameter = GendocServices.getDefault().getService(IConfigurationService.class);
		parameter.addParameter("output", f.getAbsolutePath());
		parameter.addParameter("model", FileLocator.resolve(model).toExternalForm()); 
		
		GendocProcess gendocProcess = new GendocProcess();
		String resultFile = gendocProcess.runProcess(
				URIUtil.toURI(FileLocator.resolve(input)).toURL());		
		
		StringBuffer buf = new StringBuffer();
		int severity = diagnostician.getResultDiagnostic().getSeverity();
		if (severity != Diagnostic.OK) {
			for (Diagnostic d : diagnostician.getResultDiagnostic().getChildren()) {
				buf.append(d.getMessage()).append("\n");
			}
		}
		
		Assert.assertEquals(test+":" + buf.toString(), 
				Diagnostic.OK, 
				diagnostician.getResultDiagnostic().getSeverity());

		return new XlsxVerifyHelper(resultFile);
	}
}
