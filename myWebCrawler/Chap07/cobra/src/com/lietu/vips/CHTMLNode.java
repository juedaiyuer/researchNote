package com.lietu.vips;

import java.util.ArrayList;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.lobobrowser.html.domimpl.UINode;
import org.w3c.dom.NodeList;

public class CHTMLNode {
	// 下面的属性为HTML本身的属性
	public String innerHTML;
	public String outerHTML;
	public String innerText;
	public String outerText;

	public String tagName;

	public long offsetTop;
	public long offsetLeft;
	public long offsetRight;
	public long offsetButtom;

	public long offsetWidth;
	public long offsetHeight;

	public HTMLElementImpl htmlElement;
	// public HTMLElementImpl htmlElement2;
	// public HTMLElementImpl htmlElement3;
	// public HTMLElementImpl htmlElement4;

	private int behaviorID;// 用以标记当前Node上的行为

	// 下面的属性属于VIPS本身的属性
	// 当前结点所在的结点池
	public NodePool nodePool;

	private ArrayList nodeBehaviorList;
	private ArrayList heightList;
	private boolean isTRNode;
	public int DOC;
	// 如果topLevel为3,同时它在整个兄弟中排行4,那么该结点的编号为1-2-3-4

	// 该结点四周的分割条
	public Splitter spLeft; // 该结点左侧的分割条
	public Splitter spUp; // 上侧的分割条
	public Splitter spRight; // 右侧的分割条
	public Splitter spButtom; // 下侧的分割条

	// 当前结点中可能存在的显式分隔条
	private ArrayList explicitSpList;

	public boolean textNode;

	public CHTMLNode(String textString) {
		innerHTML = textString;
		innerText = innerHTML;
		textNode = true;
		tagName = "#text";
	}

	// 传入当前结点的IHTMLElement结构
	public CHTMLNode(HTMLElementImpl inNode) {
		nodePool = null;
		// 分隔条初始化
		spLeft = null;
		spRight = null;
		spUp = null;
		spButtom = null;

		nodeBehaviorList = new ArrayList();
		explicitSpList = new ArrayList();

		htmlElement = inNode;

		innerHTML = inNode.getInnerHTML();
		outerHTML = inNode.getOuterHTML();
		innerText = inNode.getInnerText();
		outerText = inNode.getInnerText();

		HTMLElementImpl bodyNode = inNode;
		// while(bodyNode.offsetParent !=null)
		// bodyNode = bodyNode.offsetParent;

		HTMLElementImpl currentNode = inNode;
		if (currentNode.getParentNode() != null) {
			// if(currentNode.offsetParent == bodyNode) {
			//    offsetTop = currentNode.offsetParent.offsetTop + currentNode.offsetTop;
			//	  offsetLeft = currentNode.offsetParent.offsetLeft + currentNode.offsetLeft;
			// }
			// else
			{
				while (currentNode.getParentNode() != null) {
					offsetTop = offsetTop + (int) currentNode.getUINode().getBounds().y;
					offsetLeft = offsetLeft + currentNode.getUINode().getBounds().x;

					currentNode = (HTMLElementImpl) currentNode.getParentNode();
				}
				// offsetTop = bodyNode + offsetTop;
				// offsetLeft = bodyNode.offsetLeft + offsetLeft;
			}
		} else {
			offsetTop = inNode.getUINode().getBounds().y;
			offsetLeft = inNode.getUINode().getBounds().x;
		}

		offsetWidth = inNode.getUINode().getBounds().width;
		offsetHeight = inNode.getUINode().getBounds().height;

		offsetRight = offsetLeft + offsetWidth;
		offsetButtom = offsetTop + offsetHeight;

		// 根据IHTMLElement结点得到该结点的IHTMLElement2结构
		// int sourceindex = inNode.getId();
		// IHTMLDocument2 docu =(IHTMLDocument2)inNode.document;
		// htmlElement2 = (IHTMLElement2)docu.all.item(sourceindex,0);
		// htmlElement3 = (IHTMLElement3)docu.all.item(sourceindex,0);
		// htmlElement4 = (IHTMLElement4)docu.all.item(sourceindex,0);

		tagName = inNode.getTagName();

		if (inNode.getTagName() == "TR") {
			heightList = new ArrayList(1);
			htmlElement = inNode;
			isTRNode = true;
		} else
			isTRNode = false;

		textNode = false;
	}

	private boolean checkRight(ArrayList heightList) {
		// IEnumerator enumer = heightList.GetEnumerator();
		// 定位到第一个元素之前
		int i = 0;

		int firstHeight = (Integer) heightList.get(0);
		for (Object Current : heightList) {
			if (firstHeight == (Integer) Current) {
				i++;
				continue;
			}
			// 出现不相等的情况
			break;
		}

		if (i == heightList.size() - 1)
			return true;

		// 如果发现并不是所有的矩形的右边
		return false;
	}

	// 如果当前结点是矩形的，则什么都不处理
	// 如果当前结点是非矩形的，则分别取出所有的结点
	public ArrayList getAllSubNode() {
		ArrayList blockList = new ArrayList();
		if (isRectangular()) {
			blockList.add(this);
		} else {
			NodeList allChild = htmlElement.getChildNodes();
			for (int i = 0; i < allChild.getLength(); ++i) {
				CHTMLNode node = new CHTMLNode((HTMLElementImpl) allChild.item(i));
				if (node.isValidNode()) {
					blockList.add(node);
				} else {
					continue;
				}
			}
		}

		return blockList;
	}

	// 判断当前TR的结点所代表的区域是否是矩形
	public boolean isRectangular() {
		// 遍历处理htmlNode的每一个孩子结点,分别记录它们的大小
		// 得到所有结点的集合
		if (isTRNode == true) {
			NodeList allChild = htmlElement.getChildNodes();
			for (int i = 0; i < allChild.getLength(); ++i) {
				UINode child = ((HTMLElementImpl) allChild).getUINode();
				heightList.add(child.getBounds().y + child.getBounds().height);
			}

			return checkRight(heightList);
		} else{
			return true;
		}
	}

	public int DrawNode(Object obj) {
		// 对于当前结点的每一个child,都调用addbehavior
		// 有一些特殊的结点，比如TR，CENTER，DIV等等，使用绘制时候不会显示任何内容，此时必须对孩子结点进行
		// 绘制
		if(this.tagName.equals("TR") || this.tagName.equals("CENTER") || this.tagName.equals("DIV")) {
			NodeList allChild =  htmlElement.getChildNodes();
			for(int i=0;i<allChild.getLength();++i) {
				HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
				CHTMLNode node = new CHTMLNode(child);
				int childBehaviorID = node.DrawNode(obj);
				// behaviorID = htmlElement2.addBehavior(null,ref obj);
				NodeInfo info = new NodeInfo();
				info.node = node.htmlElement;
				info.behaviorID = childBehaviorID;
				nodeBehaviorList.add(info);
			}
			return 0;
		} else {
			//behaviorID = htmlElement.addBehavior(null,obj);
			return behaviorID;
		}
	}

	//
	/*public void UnDrawNode() {
		if (this.tagName.equals("TR") 
				|| this.tagName.equals("CENTER")
				|| this.tagName.equals("DIV") ) {
			NodeList allChild = htmlElement.getChildNodes();
			for (int i = 0; i < allChild.getLength(); i++) {
				// 从behaviorList中得到第i个behaviorID
				NodeInfo info = (NodeInfo) nodeBehaviorList.getElementAt(i);
				info.node.removeBehavior(info.behaviorID);
			}
		} else{
			htmlElement2.removeBehavior(behaviorID);
		}
	}*/

	public void Scroll(int size) {
		//htmlElement.scrollTop = size;
		// htmlElement2.doScroll("down");
	}

	// 判断当前的结点是否是inline结点
	// the DOM node with inline text HTML tags, which affect that appearence of
	// text and can be applied to a String
	// of characters without introcducing line break;,such as <B> <BIG> <EM>
	// inline结点通常只影响文字的外观,因此对于布局本身影响不大
	public boolean isInlineNode() {
		// 判断当前结点的tag是否是下面的一些组合即可
		if (htmlElement.getNodeName().equals("B")
				|| htmlElement.getNodeName().equals("BIG")
				|| htmlElement.getNodeName().equals("#text")
				|| htmlElement.getNodeName().equals("EM")
				|| htmlElement.getNodeName().equals("STRONG")
				|| htmlElement.getNodeName().equals("FONT")
				|| htmlElement.getNodeName().equals("I")
				|| htmlElement.getNodeName().equals("U")
				|| htmlElement.getNodeName().equals("SMALL")
				|| htmlElement.getNodeName().equals("STRIKE")
				|| htmlElement.getNodeName().equals("TT")
				|| htmlElement.getNodeName().equals("CODE")
				|| htmlElement.getNodeName().equals("SUB")
				|| htmlElement.getNodeName().equals("SUP")
				|| htmlElement.getNodeName().equals("ADDRESS")
				|| htmlElement.getNodeName().equals("BLOCKQUOTE")
				|| htmlElement.getNodeName().equals("DFN")
				|| htmlElement.getNodeName().equals("SPAN")
				|| htmlElement.getNodeName().equals("IMG")
				|| htmlElement.getNodeName().equals("A")
				|| htmlElement.getNodeName().equals("LI")
				|| htmlElement.getNodeName().equals("VAR")
				|| htmlElement.getNodeName().equals("KBD")
				|| htmlElement.getNodeName().equals("SAMP")
				|| htmlElement.getNodeName().equals("CITE")
				|| htmlElement.getNodeName().equals("H1")
				|| htmlElement.getNodeName().equals("H2")
				|| htmlElement.getNodeName().equals("H3")
				|| htmlElement.getNodeName().equals("H4")
				|| htmlElement.getNodeName().equals("H5")
				|| htmlElement.getNodeName().equals("H6")
				|| htmlElement.getNodeName().equals("BASE")){
			return true;
		} else {
			return false;
		}
	}

	// inline之外的所有的结点我们统统称之为linebreak node
	public boolean isLineBreakNode() {
		return !isInlineNode();
	}

	// 在子结点中是否存在Line Break结点
	private boolean hasLineBreakNodeInChildrens(){
		NodeList allChild =  htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode node = new CHTMLNode(child);
			if(node.isLineBreakNode()){
				return true;
			}
		}
		return false;
	}

	private boolean hasHRNodeInChildrens(){
		NodeList allChild =  htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			if(child.getTagName().equals("HR")){
				return true;
			}
		}
		return false;
	}

	private boolean isVirtual(CHTMLNode node) {
		boolean isVirtualNode = false;

		if(!node.isInlineNode())
			return false;

		NodeList allChild = node.htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode cnode = new CHTMLNode(child);
			isVirtualNode =isVirtual(cnode);
			if(isVirtualNode == false)
				return false;
			if(isVirtualNode == true)
				continue;
		}

		return true;
	}

	// 如果当前结点内部包含IMG标签，则肯定不是InValid结点
	public boolean hasImgInChilds(CHTMLNode node) {
		boolean hasImage = false;

		if(node.tagName == "IMG")
			hasImage = true;

		NodeList allChild = node.htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) 
		{
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode cnode = new CHTMLNode(child);
			hasImage = hasImgInChilds(cnode);
			if(hasImage == false)
				continue;
			if(hasImage == true)
				return true;
		}
		return hasImage;
	}

	// 判断当前的给定字符是否是类似于\r\n \r\n的格式
	private boolean isRNChar(String inStr) {
		if (inStr == null)
			return true;

		inStr = inStr.trim();
		/*
		 * while(inStr[0]=='\r'&&inStr[1]=='\n') { if(inStr.Length == 2) return
		 * true; else inStr = inStr.SubString(3);
		 * 
		 * if(inStr == "") return true; }
		 */
		if (inStr == "")
			return true;

		return false;
	}

	// 判断当前的结点是否是分隔条结点
	public boolean isSplitterNode() {
		// 判断一个结点是否可能是分隔条结点可以根据下面的几个方面
		// 如果当前结点是HR
		if (this.tagName == "HR")
			return true;

		if (this.tagName == "#comment")
			return false;

		if (this.tagName == "")
			return true;

		if (this.tagName.startsWith("/"))
			return false;

		// form标签的大小为0,0，因此可能被误判为splitternode
		if (this.tagName == "FORM" && this.innerText.trim() != "")
			return false;

		// 有的时候网页设计人员会使用比较细的图片做为分隔符，因此我们必须能够检测出来

		// 如果当前结点的宽度或者高度小于15，那么应该是分隔条
		if (this.offsetHeight <= 10 || this.offsetWidth <= 10)
			return true;

		// 如果一个TR被认定为是分隔条结点，那么它的所有的TD结点都不需要再判断
		return false;
	}

	// 判断当前结点是否是有效的结点
	// 所谓有效的结点就是能够从浏览器中观察到的结点
	public boolean isValidNode() {
		// 如果结点的宽度或者高度为0,那么该结点为无效结点,比如<P>,<BR>等等
		if (htmlElement.getUINode().getBounds().width == 0 
				|| htmlElement.getUINode().getBounds().height == 0) {
			if (this.tagName == "FORM" && this.innerText.trim() != "")
				return true;
			return false;
		}

		if (htmlElement.getNodeName().equals("SCRIPT") )
			return false;

		// 对于表格而言,<TD></TD>也应该为无效结点
		// 如果结点的innerText为null，那么必须检查该结点内部是否有
		// IMG标签，

		// innerText或者是null,或者是\r\n \r\n ...形式

		if (isRNChar(htmlElement.getInnerText())) {
			if (htmlElement.getNodeName().equals("IMG"))
				return true;

			// 检查当前结点内部是否有IMG标签
			if (hasImgInChilds(this))
				return true;

			return false;
		} else if (htmlElement.getInnerText().trim().equals(""))
			return false;

		return true;
	}

	// 该结点是文本结点,纯粹的文本结点
	public boolean isTextNode() {
		if (htmlElement.getNodeName().equals("#text"))
			return true;
		else
			return false;
	}

	// 判断当前结点已经当前结点下的所有孩子结点是否都是虚拟文本结点

	// 判断当前结点是否是虚拟文本结点
	// 如果当前结点是inlineNode,并且它的内部的所有结点都是inline node
	// 则该结点就是虚拟文本结点

	// 判断当前结点的孩子结点全部是虚拟文本结点
	// 一个结点是虚拟文本结点，必须满足下面的几个条件
	// 1.所有结点都是文本结点
	// 2.如果一个结点是虚拟文本结点，该结点的父结点不是inline结点，
	// 但是该结点的父结点只有它一个孩子结点，没有其余的孩子结点，那么它的父结点也是
	// 虚拟文本结点

	// 以当前结点为根结点,如果当前结点的孩子结点中出现一个linebreak结点,则该结点并不是
	// RealVirtualText结点
	private boolean isRealVirtualTextNode(CHTMLNode node) {
		NodeList allChild = node.htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode cnode = new CHTMLNode(child);
			if(isVirtual(cnode) == false)
				return false;
			continue;
		}
		return true;
	}

	public boolean isVirtualTextNode() {
		if (this.tagName == "LI" || this.tagName == "SELECT" || this.tagName == "MARQUEE" || this.tagName == "FORM")
			return true;

		// 如果当前结点中

		if (this.tagName == "UL" || this.tagName == "OL")
			return false;
		// 如果当前结点是虚拟文本结点,
		if (isRealVirtualTextNode(this))
			return true;

		// 对当前结点的所有孩子结点进行处理,如果当前的孩子结点中存在一个
		// LineBreak结点,则返回false

		// 如果当前结点并不是虚拟结点,但是它的第一个孩子结点数目不为1的结点是虚拟结点
		// 那么该结点也是虚拟结点
		CHTMLNode tmpNode = getNonOneChildNode(this);
		if (isRealVirtualTextNode(tmpNode))
			return true;

		return false;
	}

	// 如果当前结点的孩子结点中包含<HR>标签,则继续进行分割

	// 得到当前结点的孩子结点数目
	public int getChildrenNum() {
		// 先转换为IHTMLDOMNode结点
		//IHTMLDOMNode domNode = (IHTMLDOMNode) this.htmlElement;
		//IHTMLDOMChildrenCollection allchild = (IHTMLDOMChildrenCollection) domNode.childNodes;
		
		return this.htmlElement.getChildNodes().getLength();

		// HTMLElementCollection allChild =
		// (HTMLElementCollection)htmlElement.children;
		// return allChild.length;
	}

	// 该结点具有唯一的一个结点,并且该结点非文本结点
	private boolean hasOnlyOneNoneText(CHTMLNode node) {
		if (node.getChildrenNum() == 1) {
			/*
			 * HTMLElementCollection allChild =
			 * (HTMLElementCollection)htmlElement.children;
			 * foreach(IHTMLElement child in allChild) { CHTMLNode node = new
			 * CHTMLNode(child); if(node.tagName == "#text") return false; }
			 */

			return true;
		}
		return false;
	}

	// 下面的几个方法用以更方面的获取当前结点的属性
	// 获取当前结点的背景色
	public String getBgColor() {
		return htmlElement.getStyle().getBackgroundColor();
	}

	// 获取字体的颜色
	public String getFgColor() {
		return htmlElement.getStyle().getColor();
	}

	// 获取字体的大小
	public String getFontSize() {
		return htmlElement.getStyle().getFontSize();
	}

	// 获取字体的粗细
	public String getFontWeight() {
		return htmlElement.getStyle().getFontWeight();
	}

	// 获取字体的类型
	public String getFontFamily() {
		return htmlElement.getStyle().getFontFamily();
	}

	// 判断当前结点的背景色是否与所有的子结点都相同
	// 如果不相同,则继续分割
	public boolean isSameBgColor() {
		String bgColor = getBgColor();
		// 判断每一个子结点的背景是否相同
		NodeList allChild = htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode node = new CHTMLNode(child);
			if(bgColor != node.getBgColor()) {
				return false;
			}
		}
		return true;

	}

	// 判断当前结点的字体粗细是否与子结点的完全相同
	public boolean isSameFontWeight() {
		String fontWeight = getFontWeight();
		NodeList allChild =  htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode node = new CHTMLNode(child);
			if(fontWeight != node.getFontWeight())
				return false;
		}		
		return true;
	}

	// 判断当前结点的字体粗细是否与子结点的完全相同
	public boolean isSameFontSize() {
		String fontSize = getFontSize();
		NodeList allChild =  htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode node = new CHTMLNode(child);
			if(fontSize != node.getFontSize())
				return false;
		}
		return true;
	}

	public boolean isSameForeColor() {
		String foreColor = getFgColor();
		// 判断每一个子结点的背景是否相同
		NodeList allChild =  htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode node = new CHTMLNode(child);
			if(foreColor != node.getFgColor())
				return false;
		}
		return true;

	}

	// 当前结点的所有的子结点的字体是否一致
	// 如果有一个不一致,返回false
	public boolean isSameFontFamily() {
		String fontFamily = getFontFamily();
		NodeList allChild =  htmlElement.getChildNodes();
		for(int i=0;i<allChild.getLength();++i) {
			HTMLElementImpl child = (HTMLElementImpl)allChild.item(i);
			CHTMLNode node = new CHTMLNode(child);
			if(fontFamily != node.getFontFamily())
				return false;
		}		
		return true;
	}

	// 判断当前结点是否是End结点,即不可继续分割
	// 对于所有的End Node,它的DOC的值将被设置为10,表示不再可以被分割
	private boolean isEndNode() {
		// End Node包括两种,一种就是图片IMG,一种就是文本
		// is IHTMLImgElement || htmlElement is IHTMLTextElement
		if(htmlElement.getNodeName().equals("IMG")
				|| htmlElement.getNodeName().equals("TEXT") )
			return true;
		else
			return false;
	}

	// 判断当前结点的孩子结点是否都是同一类型,通常情况下
	private boolean isSameTypeNode() {
		return false;
	}

	// 判断当前结点是否可以继续分割为更小的结点
	// 判断给定的结点是否可以分割,主要通过以下以下原则
	// 1.当前结点本身的属性
	// 1.如果当前结点并不是矩形,则必须进行分割
	// 2.如果当前结点的DOC小于PDOC,则必须进行分割
	// 2.当前结点的子结点的属性
	// 1.子结点的类型
	// 2.子结点的数目
	public boolean isDividable(int currentLevel, int pDOC) {
		// 当分割层次达到了pDOC后通过返回false禁止继续分割
		if (currentLevel == pDOC)
			return false;

		// 如果当前结点是非矩形,必须继续进行分割
		if (!isRectangular())
			return true;

		// If the DOM node has only one valid child and the child is not a text
		// node,
		// then divide this node
		if (hasOnlyOneNoneText(this))
			return true;

		// 如果当前结点是该页面块DOM树的根结点，同时只有一个孩子结点，那么分割该结点

		// 如果当前结点的所有的孩子结点都是文本结点或者是虚拟文本结点，那么不分割该节点。
		// 如果当前所有孩子结点的字体大小和字体重量都是相同的，那么该页面块的DoC设置为10，否则设置为9。
		// 如果当前结点的所有结点都是虚拟文本结点，那么该结点将不再分隔
		if (isVirtualTextNode())
			return false;

		// If one of the child nodes of the DOM node is line-break node, then
		// divide this DOM node
		// Rule5
		// 如果当前DOM结点的孩子结点中有一个line-break结点，那么该结点将被继续分割
		if (hasLineBreakNodeInChildrens())
			return true;

		// 如果当前结点内部包含HR标签,则必须进行继续分割
		// Rule6
		if (hasHRNodeInChildrens())
			return true;

		// If the background color of this node is different from one of its
		// children's
		// divide this node and at the same time,the child node with different
		// background color
		// will not divide in this round
		if (!isSameBgColor())
			return true;

		// If the node has at least one text node child or at least one virtual
		// text node child ,and the node's

		return false;
	}

	// 整个处理过程分为三步:
	// 1.检测逻辑块
	// 2.检测各个逻辑块之间的分割条
	// 3.重新合并

	// pDOC是预先设定的DOC值
	// currentLevel是当前的分割层次,由于pDOC本身的概念并不是特别的容易掌握
	// 因此我们先尝试使用level的概念替代,每次divideDOMTree后currentLevel都将增加一
	// 当currentLevel达到pDOC之后,分割将停止
	// pool目前是body

	// 获取当前结点的第一个孩子结点
	public CHTMLNode getFirstChildNode() {
		return (CHTMLNode) htmlElement.getFirstChild();
		//if (allChild.length > 0)
		//	return new CHTMLNode((IHTMLElement) allChild.item(0, 0));
		//else
		//	return null;
	}

	// 获取该给定结点中第一个孩子结点不为1的结点
	// 如果当前结点是文本接点，直接返回
	private CHTMLNode getNonOneChildNode(CHTMLNode node) {
		CHTMLNode childNode = node;
		while (hasOnlyOneNoneText(childNode)) {
			childNode = childNode.getFirstChildNode();
			if (childNode.isVirtualTextNode())
				break;
		}

		return childNode;
	}

	// 判断当前结点是不是可视结点，所谓可视结点，就是可以通过绘制边框显示出来的
	public boolean isNonVisualNode() {
		if (this.isVirtualTextNode())
			return false;

		if (this.tagName == "CENTER" || this.tagName == "DIV"
				|| this.tagName == "TR" || this.tagName == "P")
			return true;

		return false;
	}

	// 函数返回的是所有的当前分隔条获取的所有的分隔条结点列表
	public ArrayList divideDOMTree(NodePool pool, int pDOC) {
		ArrayList list = new ArrayList();
		ArrayList spNodeList = new ArrayList();

		if (this.tagName.equals("TABLE")) {
			TableDividePolicy tablePolicy = new TableDividePolicy();
			return tablePolicy.divideNode(this, pool, pDOC);
		} else if (this.tagName.equals("TD")) {
			TdDividePolicy tdPolicy = new TdDividePolicy();
			return tdPolicy.divideNode(this, pool, pDOC);
		} else if (this.isNonVisualNode()) {
			NonVisualDividePolicy nonVisualPolicy = new NonVisualDividePolicy();
			return nonVisualPolicy.divideNode(this, pool, pDOC);
		} else {
			OtherDividePolicy otherPolicy = new OtherDividePolicy();
			return otherPolicy.divideNode(this, pool, pDOC);
		}
	}

	// direction 0:up,1:buttom,2:left,3:right
	public void setNeighbourSplitter(Splitter sp, int direction) {
		if (direction == 0)
			spUp = sp;
		else if (direction == 1)
			spButtom = sp;
		else if (direction == 2)
			spLeft = sp;
		else
			spRight = sp;
	}

	// 得到当前结点的父亲结点
	public CHTMLNode getParent() {
		return new CHTMLNode((HTMLElementImpl) htmlElement.getParentNode());
	}

	// 获取可视结点
}
