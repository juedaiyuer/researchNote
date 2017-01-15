package com.lietu.vips;

import java.util.ArrayList;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.w3c.dom.NodeList;

public class NonVisualDividePolicy {

	private ArrayList explicitSpList = null;

	public NonVisualDividePolicy()
	{
		explicitSpList = new ArrayList();
	}

	public ArrayList divideNode(CHTMLNode node,NodePool pool,int pDOC)
	{
		if(node.isVirtualTextNode())
		{
			pool.addToPool(node);
			return null;
		}

		/*
		IHTMLElementCollection allChild = (IHTMLElementCollection)node.htmlElement.children;
		foreach(IHTMLElement child in allChild)
		{
			CHTMLNode cnode = new CHTMLNode(child);
			if(cnode.tagName == "SCRIPT")
				continue;

			if(cnode.isSplitterNode())
			{
				explicitSpList.Add(cnode);
				continue;
			}

			if(cnode.isVirtualTextNode())
			{
				pool.addToPool(cnode);
				continue;
			}

			if(cnode.isValidNode())
			{
				if(!cnode.isNonVisualNode())
					pool.addToPool(cnode);
				else
					divideNode(cnode,pool,pDOC);
			}
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
				HTMLElementImpl child = cdomnode;
				CHTMLNode cnode = new CHTMLNode(child);
				if(cnode.tagName == "SCRIPT")
					continue;

				if(cnode.isSplitterNode())
				{
					explicitSpList.add(cnode);
					continue;
				}

				if(cnode.isVirtualTextNode())
				{
					pool.addToPool(cnode);
					continue;
				}

				if(cnode.isValidNode())
				{
					if(!cnode.isNonVisualNode())
						pool.addToPool(cnode);
					else
						divideNode(cnode,pool,pDOC);
				}
				continue;
			}
		}
		//如果池子中只有一个结点，那么对该结点进行继续分割
		//if(pool.getCount() == 1)
		//{
		//	pool.removeAll();
		//	CHTMLNode cnode = (CHTMLNode)pool.elementAt(0);
		//	return cnode.divideDOMTree(pool,pDOC);
		//}

		return explicitSpList;
	}
}
