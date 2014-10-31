package org.eclipse.gendoc.tags.handlers.process.buffers;

import java.util.List;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.documents.IDocumentService;
import org.eclipse.gendoc.services.AbstractService;
import org.eclipse.gendoc.services.GendocServices;
import org.eclipse.gendoc.services.exception.InvalidContentException;
import org.w3c.dom.Node;

public class StringTagAnalyserBuffer extends AbstractService implements
		ITagAnalyserBuffer {
	
	public void bufferize(Document document, Node currentNode,
			StringBuffer finalText, List<Node> nodesList) {
		IDocumentService documentService = GendocServices.getDefault().getService(IDocumentService.class);
		Node sibling;
		try {
			sibling = documentService.injectNode(currentNode, finalText.toString());
			// Remove old Node
			for (Node node : nodesList)
			{
				node.getParentNode().removeChild(node);
			}
			document.getXMLParser().setCurrentNode(sibling);
		} catch (InvalidContentException e) {
			e.printStackTrace();
		}
	}

	public void flush() {
		// DO NOTHING
	}

}
