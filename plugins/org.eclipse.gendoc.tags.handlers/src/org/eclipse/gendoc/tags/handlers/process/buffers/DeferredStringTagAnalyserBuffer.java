package org.eclipse.gendoc.tags.handlers.process.buffers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.w3c.dom.Node;

public class DeferredStringTagAnalyserBuffer extends AbstractService implements
		ITagAnalyserBuffer {
	
	List<BufferStruct> listOfBuffers = new ArrayList<DeferredStringTagAnalyserBuffer.BufferStruct>();
	
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
				sibling = documentService.injectNode(b.currentNode, b.finalText.toString());
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
		private final StringBuffer finalText;
		private final List<Node> nodesList;

		public BufferStruct(Document document, Node currentNode,
				StringBuffer finalText, List<Node> nodesList) {
					this.document = document;
					this.currentNode = currentNode;
					this.finalText = finalText;
					this.nodesList = nodesList;
		}
		
	}

}
