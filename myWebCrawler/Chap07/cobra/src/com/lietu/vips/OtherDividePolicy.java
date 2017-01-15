package com.lietu.vips;

import java.util.ArrayList;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.w3c.dom.NodeList;

//普通结点的处理策略,从当前结点开始遍历,直到找到第一个孩子数目不为1
//的孩子结点
public class OtherDividePolicy extends DividePolicy {

	private ArrayList explicitSpList = null;

	public OtherDividePolicy()
	{
		explicitSpList = new ArrayList();
	}

	public ArrayList divideNode(CHTMLNode node,NodePool pool,int pDOC)
	{
		//如果当前结点是虚拟文本结点,则立即返回该结点不再分割
		if(node.isVirtualTextNode())
		{
			pool.addToPool(node);
			return null;
		}
		if(node.isSplitterNode())
		{
			explicitSpList.add(node);
			return null;
		}

		if(!node.isValidNode())
			return null;

		if(node.getChildrenNum() == 1)
		{
			CHTMLNode tmpNode = node;
			pool.removeNode(node);
			return tmpNode.getFirstChildNode().divideDOMTree(pool,pDOC);
		}
		
		//得到第一个结点孩子数目不是1的结点
		//方法是从当前结点逐一往下遍历处理,如果遇到Table标签,调用TableDividePolicy进行处理
		//如果遇到TD标签,调用TdDividePolicy进行处理

		//如果当前结点只有一个孩子结点
		//allChild的孩子结点中既包括普通的结点,又包括文本结点,如果它不是虚拟文本结点
		//同时该结点的孩子个数是1个,那么逐层递归

		//如果当前结点的孩子数目不是一个,那么遍历所有的孩子,对于有效结点,直接将他们保存到
		//list中
		//IHTMLDOMNode domNode = (IHTMLDOMNode)node.htmlElement;
		//IHTMLDOMChildrenCollection allDOMChild = (IHTMLDOMChildrenCollection)domNode.childNodes;

		/*
		IHTMLElementCollection allChild = (IHTMLElementCollection)node.htmlElement.children;
		foreach(IHTMLElement child in allChild)
		{
			CHTMLNode cnode = new CHTMLNode(child);
			if(cnode.isSplitterNode())
			{
				explicitSpList.Add(cnode);
				continue;
			}
			if(cnode.isValidNode())
			{
				if(cnode.isNonVisualNode())
					cnode.divideDOMTree(pool,pDOC);
				else
					pool.addToPool(cnode);
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
				if(cnode.isSplitterNode())
				{
					explicitSpList.add(cnode);
					continue;
				}
				if(cnode.isValidNode())
				{
					if(cnode.isNonVisualNode())
						cnode.divideDOMTree(pool,pDOC);
					else
						pool.addToPool(cnode);
				}

				continue;
			}
		}

		//如果池子中只有一个结点，那么对该结点进行继续分割
		///此处被修改过
		///////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		if(pool.getCount() == 1)
		{
			CHTMLNode cnode = (CHTMLNode)pool.elementAt(0);
			if(cnode.tagName != "#text")
			{
				pool.removeAll();
				return cnode.divideDOMTree(pool,pDOC);
			}
		}

		return explicitSpList;
	}
}
