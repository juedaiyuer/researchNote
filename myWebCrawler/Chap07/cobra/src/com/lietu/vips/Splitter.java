package com.lietu.vips;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.w3c.dom.html2.HTMLDocument;

public class Splitter {
	// 描述该分割条的左上角的坐标
	public long left;
	public long top;
	public long right;
	public long buttom;

	public long width;
	public long height;
	// 当前分割条的权重，权重越大,成为真正分割条的可能性也就越大
	public int weight;

	public boolean vertical;//是水平分割条还是垂直分割条,true表示垂直;false表示水平

	public NodePool leftUpBlock;//对于水平分割条,保存该分割条左侧的block
	// 对于垂直分割条,保存该分割条上面的block
	public NodePool rightButtomBlock;
	// 对于水平分割条,保存该分割条右侧的block
	// 对于垂直分割条,保存该分割条下侧的block

	// 该标记用于判断当前分隔条是否是显示分隔条
	// 比如HR,空的Table,TR,TD以及宽度和高度高于20的分割条都属于显示分隔条
	// 其余的分隔条属于隐式分隔条。
	public boolean isExplicit;

	public Splitter() {
		left = 0;
		top = 0;
		right = 0;
		buttom = 0;
		vertical = true;

		leftUpBlock = new NodePool();
		rightButtomBlock = new NodePool();

		isExplicit = false;
	}

	public Splitter(VisionBlock block, boolean isVertical) {
		left = block.blockLeft;
		right = block.blockRight;
		top = block.blockTop;
		buttom = block.blockButtom;
		width = right - left;
		height = buttom - top;

		vertical = isVertical;
		leftUpBlock = new NodePool();
		rightButtomBlock = new NodePool();

		isExplicit = false;
	}

	// 初始的时候,分割条为整个页面宽度
	public Splitter(HTMLDocument document, boolean isVertical) {
		HTMLElementImpl body = (HTMLElementImpl) document.getBody();

		left = body.getOffsetLeft();
		top = body.getOffsetTop();
		right = left + body.getOffsetWidth();
		buttom = top + body.getOffsetHeight();

		width = body.getOffsetWidth();
		height = body.getOffsetHeight();

		vertical = isVertical;
		leftUpBlock = new NodePool();
		rightButtomBlock = new NodePool();

		isExplicit = false;
	}

	// 把一个新的VisionBlock作为分隔条检测的起点
	public Splitter(CHTMLNode beginNode, boolean isVertical) {
		left = beginNode.offsetLeft;
		top = beginNode.offsetTop;
		right = beginNode.offsetRight;
		buttom = beginNode.offsetButtom;

		width = right - left;
		height = buttom - top;

		vertical = isVertical;
		leftUpBlock = new NodePool();
		rightButtomBlock = new NodePool();

		isExplicit = false;
	}

	// 通过手工指定分隔条的大小
	public Splitter(long inleft, long intop, long inright, long inbuttom,
			boolean isVertical) {
		left = inleft;
		top = intop;
		right = inright;
		buttom = inbuttom;

		width = right - left;
		height = buttom - top;

		vertical = isVertical;
		leftUpBlock = new NodePool();
		rightButtomBlock = new NodePool();

		isExplicit = false;
	}

	// 检查给定的block是否在分割条内部
	// left,top splitter
	// ___________________________________
	// | |
	// | left top |
	// | ______________________ |
	// | | | |
	// | | node | |
	// | | | |
	// | |_____________________| |
	// | right,buttom |
	// |___________________________________|
	// right,buttom
	public boolean blockIsInSplitter(CHTMLNode node) {
		// 为了判断node是否位于给分割条之内,则可以检测它的left,top和right和buttom
		if (node.offsetLeft >= left && node.offsetTop >= top
				&& node.offsetLeft + node.offsetWidth <= right
				&& node.offsetTop + node.offsetHeight <= buttom)
			return true;
		else
			return false;
	}

	// 检查当前block是否包含当前的分割条
	// 检查给定的block是否在分割条内部
	// left,top block
	// ___________________________________
	// | |
	// | left top |
	// | ______________________ |
	// | | | |
	// | | splitter | |
	// | | | |
	// | |_____________________| |
	// | right,buttom |
	// |___________________________________|
	// right,buttom
	public boolean splitterIsInBlock(CHTMLNode node) {
		if (left >= node.offsetLeft && top >= node.offsetTop
				&& right <= node.offsetLeft + node.offsetWidth
				&& buttom <= node.offsetTop + node.offsetHeight)
			return true;
		else
			return false;
	}

	// 检查当前的block是否被该splitter割断
	// 包括两种情况
	// block
	// _______________
	// | |
	// | |
	// _________________ | |
	// | | | |
	// |_________________| | |
	// |______________|
	// 或者
	// _____
	// | |
	// | |
	// | | splitter
	// |_____|
	// _________________________
	// | |
	// | node |
	// | |
	// |_________________________|
	public boolean blockIsAcrossSplitter(CHTMLNode node) {
		// 对于第一种情况,只需要直接判断top
		if (top > node.offsetTop && top < node.offsetButtom
				&& buttom > node.offsetTop && buttom < node.offsetButtom)
			return true;

		if (left > node.offsetLeft && left < node.offsetRight
				&& right > node.offsetLeft && right < node.offsetRight)
			return true;

		return false;
	}

	// 检查当前的分割条是否与给定的block相交,包括水平相交和垂直相交
	// _______________________
	// | splitter |
	// | __________ |
	// |__________|_________|_|
	// | |
	// | |block
	// ___________|_________|__
	// | |_________| |
	// |________________________|

	// __________ ____________
	// | |splitter | |
	// | _____|___________|___ |splitter
	// | | | | | block |
	// | |_____|___________|___| |
	// |__________| |____________|

	// 下面的四种情况在HTML中不可能出现,因此我们不予考虑
	// __________
	// | |splitter
	// | _____|_______
	// |____|_____| | block
	// |_____________|

	// ___________
	// | |
	// _________|_______ |splitter
	// | |_______|__|
	// |_________________|block

	// __________
	// _________|_______ |block
	// | | | |
	// | |_______|__|
	// |_________________|splitter

	// __________
	// | |block
	// | _____|_______
	// |____|_____| | splitter
	// |_____________|

	// 交叉不同于前面的contain和across
	// 该函数用以比较num是否位于begin和end之间
	// 通过该函数进行相交判断
	private boolean isBetween(long num, long begin, long end) {
		if (num >= begin && num <= end)
			return true;
		else
			return false;
	}

	// 判断一个点是否在另外一个矩形内
	private boolean isInRect(long x, long y, long left, long top, long right,
			long buttom) {
		if (isBetween(x, left, right) && isBetween(y, top, buttom))
			return true;
		else
			return false;
	}

	// 返回1,上部相交
	// 返回2,下部相交
	// 返回3,左部相交
	// 返回4,右部相交
	// 否则返回-1
	public int isIntersectWithBlock(CHTMLNode node) {
		long nodeLeft = node.offsetLeft;
		long nodeTop = node.offsetTop;
		long nodeRight = node.offsetLeft + node.offsetWidth;
		long nodeButtom = node.offsetTop + node.offsetHeight;

		// 上部相交
		if (isInRect(nodeLeft, nodeTop, left, top, right, buttom)
				&& isInRect(nodeRight, nodeTop, left, top, right, buttom)
				&& isBetween(buttom, nodeTop, nodeButtom))
			return 1;

		// 下部相交
		if (isInRect(nodeLeft, nodeButtom, left, top, right, buttom)
				&& isInRect(nodeRight, nodeButtom, left, top, right, buttom)
				&& isBetween(top, nodeTop, nodeButtom))
			return 2;

		// 左部相交
		if (isInRect(nodeLeft, nodeTop, left, top, right, buttom)
				&& isInRect(nodeLeft, nodeButtom, left, top, right, buttom)
				&& isBetween(right, nodeLeft, nodeRight))
			return 3;

		// 右部相交
		if (isInRect(nodeRight, nodeTop, left, top, right, buttom)
				&& isInRect(nodeRight, nodeButtom, left, top, right, buttom)
				&& isBetween(left, nodeLeft, nodeRight))
			return 4;

		return -1;

	}

	// 调整当前分割条的坐标位置
	public void adjust(long inleft, long intop, long inright, long inbuttom) {
		left = inleft;
		top = intop;
		right = inright;
		buttom = inbuttom;

		width = right - left;
		height = buttom - top;
	}

	// 得到当前分割条的重量,重量越大,该分割条成为实际分割条的可能性越大
	public int getWeight() {
		return 10;
		// 通常情况下,如果分割条的
	}

	// 在给定的结点结合中查找top最小的结点
	private long minTop(NodePool upPool) {
		if (upPool.getCount() == 0)
			return -1;
		if (upPool.getCount() == 1)
			return ((CHTMLNode) upPool.elementAt(0)).offsetTop;

		long min = ((CHTMLNode) upPool.elementAt(0)).offsetTop;
		for (int i = 1; i < upPool.getCount(); i++) {
			if (((CHTMLNode) upPool.elementAt(i)).offsetTop <= min)
				min = ((CHTMLNode) upPool.elementAt(i)).offsetTop;
		}

		return min;
	}

	private long maxButtom(NodePool downPool) {
		if (downPool.getCount() == 0)
			return -1;
		if (downPool.getCount() == 1)
			return ((CHTMLNode) downPool.elementAt(0)).offsetButtom;

		long max = ((CHTMLNode) downPool.elementAt(0)).offsetButtom;
		for (int i = 1; i < downPool.getCount(); i++) {
			if (((CHTMLNode) downPool.elementAt(0)).offsetButtom >= max)
				max = ((CHTMLNode) downPool.elementAt(0)).offsetButtom;
		}

		return max;
	}

	private long minLeft(NodePool leftPool) {
		if (leftPool.getCount() == 0)
			return -1;
		if (leftPool.getCount() == 1)
			return ((CHTMLNode) leftPool.elementAt(0)).offsetLeft;

		long min = ((CHTMLNode) leftPool.elementAt(0)).offsetLeft;
		for (int i = 1; i < leftPool.getCount(); i++) {
			if (((CHTMLNode) leftPool.elementAt(i)).offsetLeft <= min)
				min = ((CHTMLNode) leftPool.elementAt(i)).offsetLeft;
		}

		return min;
	}

	private long maxRight(NodePool rightPool) {
		if (rightPool.getCount() == 0)
			return -1;
		if (rightPool.getCount() == 1)
			return ((CHTMLNode) rightPool.elementAt(0)).offsetRight;

		long max = ((CHTMLNode) rightPool.elementAt(0)).offsetRight;
		for (int i = 1; i < rightPool.getCount(); i++) {
			if (((CHTMLNode) rightPool.elementAt(0)).offsetRight >= max)
				max = ((CHTMLNode) rightPool.elementAt(0)).offsetRight;
		}

		return max;
	}

	public boolean isRealVerticalSp(long splitterLimit) {
		NodePool leftPool = this.leftUpBlock;
		NodePool rightPool = this.rightButtomBlock;

		// 首先尝试的是，如果当前分隔条的高度小于10，但是上侧的block高度很小，且下侧的block高度很大
		// 那么通常意味着上侧的block是标题block，应该与下侧的block何谓一体
		// 不过这种原则只适合非显式分隔条，而且分隔条的高度小于10，一旦分隔条的高度>10，那么意味着
		// 可能设计者有意分隔之
		long leftDistance = 0;
		long rightDistance = 0;

		if (!this.isExplicit && this.width < splitterLimit) {
			// 查找当前分隔条上侧的分隔条
			Splitter prevSp = ((CHTMLNode) leftPool.nodeList.get(0)).spLeft;
			Splitter nextSp = ((CHTMLNode) rightPool.nodeList.get(0)).spRight;
			// 第一个分隔条
			if (prevSp == null && nextSp != null) {
				leftDistance = this.top - minLeft(leftPool);
				rightDistance = nextSp.left - this.right;
				// 合并上下分隔条
			}
			if (prevSp != null && nextSp != null) {
				leftDistance = this.left - prevSp.right;
				rightDistance = nextSp.left - this.right;
			}
			if (prevSp != null && nextSp == null) {
				leftDistance = this.left - prevSp.right;
				rightDistance = maxRight(rightPool) - this.right;
			}
			if (prevSp == null && nextSp == null) {
				leftDistance = this.left - minLeft(leftPool);
				rightDistance = maxRight(rightPool) - this.right;
			}

			if (leftDistance >= 40 && rightDistance <= 40)
				return true;

			if (leftDistance <= 40 && rightDistance >= 40)
				return false;

			// if(leftDistance >=40 && rightDistance >=40)
			// return true;

		}

		if (this.isExplicit || this.width >= splitterLimit)
			return true;

		return false;
	}

	public boolean isRealHorizontalSp(long splitterLimit) {
		NodePool upPool = this.leftUpBlock;
		NodePool downPool = this.rightButtomBlock;

		// 首先尝试的是，如果当前分隔条的高度小于10，但是上侧的block高度很小，且下侧的block高度很大
		// 那么通常意味着上侧的block是标题block，应该与下侧的block何谓一体
		// 不过这种原则只适合非显式分隔条，而且分隔条的高度小于10，一旦分隔条的高度>10，那么意味着
		// 可能设计者有意分隔之
		long upDistance = 0;
		long downDistance = 0;

		if (!this.isExplicit && this.height < splitterLimit) {
			// 查找当前分隔条上侧的分隔条
			Splitter prevSp = ((CHTMLNode) upPool.nodeList.get(0)).spUp;
			Splitter nextSp = ((CHTMLNode) downPool.nodeList.get(0)).spButtom;
			// 第一个分隔条
			if (prevSp == null && nextSp != null) {
				upDistance = this.top - minTop(upPool);
				downDistance = nextSp.top - this.buttom;
				// 合并上下分隔条
			}
			if (prevSp != null && nextSp != null) {
				upDistance = this.top - prevSp.buttom;
				downDistance = nextSp.top - this.buttom;
			}
			if (prevSp != null && nextSp == null) {
				upDistance = this.top - prevSp.buttom;
				downDistance = maxButtom(downPool) - this.buttom;
			}
			if (prevSp == null && nextSp == null) {
				upDistance = this.top - minTop(upPool);
				downDistance = maxButtom(downPool) - this.buttom;
			}

			if (upDistance >= 40 && downDistance <= 40)
				return true;

			if (upDistance <= 40 && downDistance >= 40)
				return false;

			// if(upDistance >=40 && downDistance>=40)
			// return true;

			// if(upDistance <=40 && downDistance <=40)
			// return true;
		}

		if (this.isExplicit || this.height >= splitterLimit)
			return true;

		return false;
	}
}
