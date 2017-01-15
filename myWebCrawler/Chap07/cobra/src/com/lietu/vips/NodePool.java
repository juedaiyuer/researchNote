package com.lietu.vips;

import java.util.ArrayList;

public class NodePool {

	public ArrayList nodeList;

	public NodePool() {
		nodeList = new ArrayList(0);
	}

	public void addToPool(CHTMLNode node) {
		nodeList.add(node);
		node.nodePool = this;
	}

	public CHTMLNode elementAt(int index) {
		return (CHTMLNode) nodeList.get(index);
	}

	public void drawAllNode(Object obj) {
		// IEnumerator enumer = nodeList.GetEnumerator();
		// enumer.Reset();
		for (Object cur : nodeList) {
			((CHTMLNode) cur).DrawNode(obj);
		}
	}

	public void removeNode(CHTMLNode node) {
		nodeList.remove(node);
	}

	public void removeAll() {
		nodeList.removeAll(nodeList);
	}

	public int getCount() {
		return nodeList.size();
	}
}
