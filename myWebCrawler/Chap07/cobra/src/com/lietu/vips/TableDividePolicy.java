package com.lietu.vips;

import java.util.ArrayList;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.w3c.dom.NodeList;


//当遇到Table标签的时候，该策略将返回所有的属于该表格的TD结点
public class TableDividePolicy extends DividePolicy
{
	private ArrayList explicitSpList = null;

	public TableDividePolicy()
	{
		explicitSpList = new ArrayList();
	}

	//获取当前结点下的所有的TD结点
	private void getTDNode(CHTMLNode node,NodePool pool)
	{
		if(node.tagName == "TD" || node.tagName == "TH")
		{
			pool.addToPool(node);
			return;
		}

		/*
		IHTMLElementCollection allChild1 = (IHTMLElementCollection)node.htmlElement.children;
		foreach(IHTMLElement child in allChild1)
		{
			CHTMLNode childNode = new CHTMLNode(child);
			if(childNode.isSplitterNode())
			{
				explicitSpList.Add(childNode);
				continue;
			}

			if(childNode.isValidNode())
				getTDNode(childNode,pool);
			else
				continue;
		}
		*/

		HTMLElementImpl domNode = node.htmlElement;
		NodeList allchild = domNode.getChildNodes();
		for(int i=0;i<allchild.getLength();i++)
		{
			HTMLElementImpl cdomnode = (HTMLElementImpl)allchild.item(i);
			if(cdomnode.getNodeName().equals("#text") )
			{
				CHTMLNode textNode = new CHTMLNode(cdomnode.getNodeValue());
				pool.addToPool(textNode);
				continue;
			}
			else
			{
				HTMLElementImpl child = (HTMLElementImpl)cdomnode;
				CHTMLNode childNode = new CHTMLNode(child);
				if(childNode.isSplitterNode())
				{
					explicitSpList.add(childNode);
					continue;
				}

				if(childNode.isValidNode())
				{
					if(childNode.tagName == "TR" || childNode.tagName == "TBODY")
						getTDNode(childNode,pool);
					else
						pool.addToPool(childNode);
				}
				else
					continue;
			}
		}

		//如果只有一个结点，则继续分割
	}

	public ArrayList divideNode(CHTMLNode node,NodePool pool,int pDOC)
	{
		getTDNode(node,pool);

		//只有一个TD结点，那么此时将对该TD结点进行深入的进一步处理
		if(pool.getCount() == 1)
		{
			CHTMLNode tdNode = (CHTMLNode)pool.elementAt(0);
			pool.removeAll();
			//TdDividePolicy tdPolicy = new TdDividePolicy();
			//return tdPolicy.divideNode(tdNode,pool,pDOC);
			return tdNode.divideDOMTree(pool,pDOC);
		}
		return explicitSpList;
	}
}