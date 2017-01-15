/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The Lobo Project

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Contact info: xamjadmin@users.sourceforge.net
*/
/*
 * Created on Dec 3, 2005
 */
package org.lobobrowser.html.domimpl;

import org.lobobrowser.js.*;
import org.w3c.dom.Node;
import org.w3c.dom.html2.HTMLCollection;

public class DescendentHTMLCollection extends AbstractScriptableDelegate implements HTMLCollection {
	//TODO: This collection is very inefficient for iteration.
	private final NodeImpl rootNode;
	private final NodeFilter nodeFilter;

	/**
	 * @param node
	 * @param filter
	 */
	public DescendentHTMLCollection(NodeImpl node, NodeFilter filter) {
		super();
		rootNode = node;
		nodeFilter = filter;
	}
	
	public int getLength() {
		NodeCounter nc = new NodeCounter();
		this.rootNode.visit(nc);
		return nc.getCount();
	}

	public Node item(int index) {
		NodeScanner ns = new NodeScanner(index);
		try {
			this.rootNode.visit(ns);
		} catch(StopVisitorException sve) {
			//ignore
		}
		return ns.getNode();
	}

	public Node namedItem(String name) {
		org.w3c.dom.Document doc = this.rootNode.getOwnerDocument();
		if(doc == null) {
			return null;
		}
		//TODO: This might get elements that are not descendents.
		Node node = (Node) doc.getElementById(name);
		if(node != null && this.nodeFilter.accept(node)) {
			return node;
		}
		return null;
	}
	
	public int indexOf(Node node) {
		NodeScanner2 ns = new NodeScanner2(node);
		try {
			this.rootNode.visit(ns);
		} catch(StopVisitorException sve) {
			//ignore
		}
		return ns.getIndex();
	}

	private final class NodeCounter implements NodeVisitor {
		private int count = 0;
		
		public final void visit(Node node) {
			if(nodeFilter.accept(node)) {
				this.count++;
				throw new SkipVisitorException();
			}
		}
		
		public int getCount() {
			return this.count;
		}
	}	

	private final class NodeScanner implements NodeVisitor {
		private int count = 0;
		private Node foundNode = null;
		private final int targetIndex;
		
		public NodeScanner(int idx) {
			this.targetIndex = idx;
		}
		
		public final void visit(Node node) {
			if(nodeFilter.accept(node)) {
				if(this.count == this.targetIndex) {
					this.foundNode = node;
					throw new StopVisitorException();
				}
				this.count++;
				throw new SkipVisitorException();
			}
		}
		
		public Node getNode() {
			return this.foundNode;
		}
	}	

	private final class NodeScanner2 implements NodeVisitor {
		private int count = 0;
		private int foundIndex = -1;
		private final Node targetNode;
		
		public NodeScanner2(Node node) {
			this.targetNode = node;
		}
		
		public final void visit(Node node) {
			if(nodeFilter.accept(node)) {
				if(node == this.targetNode) {
					this.foundIndex = this.count;
					throw new StopVisitorException();
				}
				this.count++;
				throw new SkipVisitorException();
			}
		}
		
		public int getIndex() {
			return this.foundIndex;
		}
	}	

}
