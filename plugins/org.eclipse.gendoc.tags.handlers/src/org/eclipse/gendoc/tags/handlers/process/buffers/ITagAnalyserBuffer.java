package org.eclipse.gendoc.tags.handlers.process.buffers;

import java.util.List;

import org.eclipse.gendoc.document.parser.documents.Document;
import org.eclipse.gendoc.services.IService;
import org.w3c.dom.Node;

public interface ITagAnalyserBuffer extends IService{

	void bufferize(Document document, Node currentNode, StringBuffer finalText,
			List<Node> nodesList);

	void flush();

}
