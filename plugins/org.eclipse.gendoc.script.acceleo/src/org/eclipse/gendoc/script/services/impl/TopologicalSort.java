/*****************************************************************************
 * Copyright (c) 2015 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	Tristan faure (Atos) - tristan.faure@atos.net - Initial API and implementation
 * 
 *****************************************************************************/
package org.eclipse.gendoc.script.services.impl;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Implementation of Topological Sort
 * @author 
 *
 * @param <N>
 */
public class TopologicalSort {
	public static <N> List<N> sort (Collection<? extends N> list, Edge<N> edgeRetriever) throws CycleException {
		if (edgeRetriever == null || list == null){
			throw new IllegalArgumentException();
		}
		List<N> result = new LinkedList<N>(); 
		Set<? super N> markedNodes = new HashSet<N>(); 
		Set<? super N> unmarkedNodes = new HashSet<N>((Collection<? extends N>) list);
		Set<? super N> temporaryNodes= new HashSet<N>();
		
		while (!unmarkedNodes.isEmpty()){
			// select an unmarked node
			Iterator<? super N> i = unmarkedNodes.iterator();
			N node = (N) i.next();
			i.remove();
			
			visit(result, node, edgeRetriever, unmarkedNodes, markedNodes, temporaryNodes);
		}
		
		
		return result ;
	}
	
	private static <N> void visit (List<? super N> result, N node, Edge<N> edgeRetriever,Set<? super N> unmark, Set<? super N> markedNodes, Set<? super N> temporaryNodes) throws CycleException {
		if (temporaryNodes.contains(node)){
			throw new CycleException(node);
		}
		if (!markedNodes.contains(node)){
			markTmp (node, unmark, unmark, temporaryNodes);
			List<? super N> outgoings = edgeRetriever.from(node);
			if (outgoings != null){
				for (Object o : outgoings){
					N m = (N) o ;
					visit(result, m, edgeRetriever,unmark,markedNodes,temporaryNodes);
				}
				mark(node, unmark, markedNodes, temporaryNodes);
				unmarkTmp(node, unmark, markedNodes, temporaryNodes);
				result.add(0,node);
			}
		}
		
	}
	
	private static <N> void unmarkTmp (N node, Set<? super N> unmark, Set<? super N> markedNodes, Set<? super N> temporaryNodes) {
		unmark.add(node);
		markedNodes.add(node);
		temporaryNodes.remove(node);
	}
	
	private static <N> void markTmp (N node, Set<? super N> unmark, Set<? super N> markedNodes, Set<? super N> temporaryNodes) {
		unmark.remove(node);
		markedNodes.remove(node);
		temporaryNodes.add(node);
	}
	
	private static <N> void mark (N node, Set<? super N> unmark, Set<? super N> markedNodes, Set<? super N> temporaryNodes) {
		unmark.remove(node);
		temporaryNodes.remove(node);
		markedNodes.add(node);
	}
	
	private static <N> void unmark (N node, Set<? super N> unmark, Set<? super N> markedNodes, Set<? super N> temporaryNodes) {
		unmark.add(node);
		temporaryNodes.remove(node);
		markedNodes.remove(node);
	}
	
	private static <N> Set<? super N> getAllNoIncomingEdges (Collection<? extends N> list, Edge<N> edgeRetriever) {
		Set<? super N> result = new HashSet<N>();
		for (Object o : list){
			N n = (N) o ;
			List<? super N> edges = edgeRetriever.from(n);
			if (edges == null || edges.isEmpty()){
				result.add(n);
			}
		}
		return result;
	}
	
	public interface Edge<N> {
		public List<? super N> from (N n) ;
	}
	
	public static class CycleException extends Exception {
		private static final long serialVersionUID = 1L;
		private Object node;

		public CycleException(Object node) {
			this.node = node;
		}
		
		public Object getNode (){
			return node;
		}
	}
}
