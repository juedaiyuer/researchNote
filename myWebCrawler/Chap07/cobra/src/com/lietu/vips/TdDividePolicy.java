package com.lietu.vips;

import java.util.ArrayList;

//定义当遇到Td的时候采取的策略
//如果TD仅仅有一个结点，并且是虚拟文本结点，那么
public class TdDividePolicy extends DividePolicy {

	private ArrayList explicitSpList = null;

	public TdDividePolicy()
	{
		explicitSpList = new ArrayList();
	}

	public ArrayList getSplitterNodeList()
	{
		return explicitSpList;
	}

	//判断当前的TD是否是虚拟文本结点，如果是，则不做任何的分割
	//一个TD结点是虚拟文本结点，必须满足下面的几个条件
	//1.所有结点都是文本结点
	//2.如果一个结点是虚拟文本结点，该结点的父结点不是inline结点，
	//但是该结点的父结点只有它一个孩子结点，没有其余的孩子结点，那么它的父结点也是
	//虚拟文本结点
	private boolean tdIsVirtualTextNode(CHTMLNode tdNode)
	{
		if(tdNode.isVirtualTextNode())
			return true;

		//得到当前结点的第一个孩子数目不为1的结点
		//CHTMLNode tmpNode = tdNode.getNonOneChildNode();
		//if(tmpNode.isVirtualTextNode())
		//	return true;
		return false;
	}

	public ArrayList divideNode(CHTMLNode node,NodePool pool,int pDOC)
	{
		if(node.isSplitterNode())
		{
			explicitSpList.add(node);
			return explicitSpList;
		}
		//如果是文本结点，将不再分割
		if(tdIsVirtualTextNode(node))
		{
			pool.addToPool(node);
			return null;
		}

		//否则对TD内的结点执行通用的结点分隔策略
		OtherDividePolicy otherPolicy = new OtherDividePolicy();
		return otherPolicy.divideNode(node,pool,pDOC);
	}
}
