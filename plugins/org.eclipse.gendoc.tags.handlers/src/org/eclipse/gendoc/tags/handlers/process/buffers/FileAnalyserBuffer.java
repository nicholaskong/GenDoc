package org.eclipse.gendoc.tags.handlers.process.buffers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.w3c.dom.Node;

public class FileAnalyserBuffer extends AbstractService implements
		ITagAnalyserBuffer {

	List<BufferStruct> listOfBuffers = new ArrayList<BufferStruct>();
	
	public void bufferize(Document document, Node currentNode,
			StringBuffer finalText, List<Node> nodesList) {
		BufferStruct buf = new BufferStruct(document, currentNode, finalText, nodesList);
		listOfBuffers.add(buf);
	}

	public void flush() {
		// DO NOTHING
		Iterator<BufferStruct> i = listOfBuffers.iterator();
		while (i.hasNext()){
			BufferStruct b = i.next();
			IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
			Node sibling;
			try {
				sibling = documentService.injectNode(b.currentNode, b.getFinalText());
				// Remove old Node
				for (Node node : b.nodesList)
				{
					node.getParentNode().removeChild(node);
				}
				b.document.getXMLParser().setCurrentNode(sibling);
			} catch (InvalidContentException e) {
				e.printStackTrace();
			}
			i.remove();
		}
	}
	
	private static class BufferStruct {

		private final Document document;
		private final Node currentNode;
		private String filePath = null;
		private final List<Node> nodesList;
		public BufferStruct(Document document, Node currentNode,
			StringBuffer finalText, List<Node> nodesList) {
			this.document = document;
			this.currentNode = currentNode;
			this.nodesList = nodesList;
			File f;
			BufferedWriter writer = null;
			try {
				f = File.createTempFile("gendoc2",null);
				f.deleteOnExit();
				filePath = f.getAbsolutePath();
				FileWriter fileWriter = new FileWriter(f);
				writer = new BufferedWriter(fileWriter);
				writer.write(finalText.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (writer != null){
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
				
		}

		public String getFinalText() {
			String lineSeparator = System.getProperty("line.separator");
			StringBuffer buffer = new StringBuffer();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File(filePath)));
				String tmp = reader.readLine();
				while (tmp != null){
					buffer.append(tmp);
					buffer.append(lineSeparator);
					tmp = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (reader != null){
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return buffer.toString();
		}
		
	}

}
