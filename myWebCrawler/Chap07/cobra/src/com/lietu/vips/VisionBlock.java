package com.lietu.vips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//Vision是VIPS算法中最重要的类,它用以描述最终的视觉内容块
//最初的整个页面属于一个大的语义块,当进行了第一次迭代后，该VisionBlock被分割为多个新的
//小的内容块，因此内容块的结构应该是自包含的
public class VisionBlock {
	// 位置信息
	public long blockLeft; // 当前内容块的左边缘位置
	public long blockTop; // 当前内容块的上边缘位置
	public long blockRight; // 当前内容块的右边缘位置
	public long blockButtom; // 当前内容块的下边缘位置
	public long blockWidth; // 当前内容块的宽度
	public long blockHeight; // 当前内容块的高度

	// 分割条信息
	public Splitter upSplitter;
	public Splitter downSplitter;
	public Splitter leftSplitter;
	public Splitter rightSplitter;

	// 邻居信息
	private ArrayList<CHTMLNode> leftVisionBlock; // 当前内容块的左侧语义块
	private ArrayList<CHTMLNode> topVisionBlock; // 当前内容块的上侧语义块
	private ArrayList<CHTMLNode> rightVisionBlock; // 当前内容块的右侧语义块
	private ArrayList<CHTMLNode> buttomVisionBlock; // 当前内容块的下侧语义块

	// 结构信息
	private ArrayList<VisionBlock> containedVisionBlockList; // 该Block内部包含的Block们
	private ArrayList<CHTMLNode> containedHTMLNodeList; // 当前语义块所包含的

	// 当前Block中包含的结点分隔条
	public ArrayList<CHTMLNode> nodeSplitterList;

	// 当前逻辑块的doc值,用以描述逻辑块内部的关联程度的强弱
	// DOC用于描述当前VisionBlock的内部的关联性,DOC的值越大,关联性越大
	// DOC的值越小,关联性越小。DOC的值介于1-10之间
	public int DOC;
	private String blockName;

	// 当前Block的类型
	public BlockType blockType;

	// 当前语义块的父亲语义块
	public VisionBlock parentBlock;

	// 内部的Block是垂直Block还是水平Block
	public int blockDirection;
	public int divideDirection;// 当前block被分割的方向
	// 判断当前的Block是否是文本Block
	public int isTextBlock;
	// 子结点的数目
	public int childNums;

	public VisionBlock() {
		blockLeft = 0;
		blockRight = 0;
		blockTop = 0;
		blockButtom = 0;
		blockHeight = 0;
		blockWidth = 0;

		leftVisionBlock = new ArrayList();
		topVisionBlock = new ArrayList();
		rightVisionBlock = new ArrayList();
		buttomVisionBlock = new ArrayList();
		containedVisionBlockList = new ArrayList();
		containedHTMLNodeList = new ArrayList();
		nodeSplitterList = new ArrayList();

		upSplitter = null;
		downSplitter = null;
		leftSplitter = null;
		rightSplitter = null;

		parentBlock = null;

		DOC = 0;

		blockDirection = 0;
		isTextBlock = 0;
		childNums = 0;
	}

	public VisionBlock(String textString) {
		isTextBlock = 1;

		blockLeft = 0;
		blockRight = 0;
		blockTop = 0;
		blockButtom = 0;
		blockHeight = 0;
		blockWidth = 0;

		leftVisionBlock = new ArrayList();
		topVisionBlock = new ArrayList();
		rightVisionBlock = new ArrayList();
		buttomVisionBlock = new ArrayList();
		containedVisionBlockList = new ArrayList();
		containedHTMLNodeList = new ArrayList();
		nodeSplitterList = new ArrayList();

		upSplitter = null;
		downSplitter = null;
		leftSplitter = null;
		rightSplitter = null;

		parentBlock = null;

		DOC = 15;
		childNums = 0;
	}

	// 类方法定义

	public ArrayList getContainedVisionBlockList() {
		return containedVisionBlockList;
	}

	public ArrayList<CHTMLNode> getContainedHTMLNodeList() {
		return containedHTMLNodeList;
	}

	public ArrayList getLeftVisionBlock() {
		return leftVisionBlock;
	}

	public ArrayList getRightVisionBlock() {
		return rightVisionBlock;
	}

	public ArrayList getTopVisionBlock() {
		return topVisionBlock;
	}

	public ArrayList getButtomVisionBlock() {
		return buttomVisionBlock;
	}

	// 增加该Block的子Block
	public void addContainedVisionBlock(VisionBlock block) {
		containedVisionBlockList.add(block);
		block.parentBlock = this;
	}

	// 增加该Block对应的HTML结点
	public void addContainedHTMLNode(CHTMLNode node) {
		containedHTMLNodeList.add(node);
	}

	public VisionBlock getBlockByIndex(int index) {
		return (VisionBlock) containedVisionBlockList.get(index);
	}

	public CHTMLNode getHTMLNodeByIndex(int index) {
		return (CHTMLNode) containedHTMLNodeList.get(index);
	}

	// 返回当前的VisionBlock在整个list中的索引
	private int indexInList() {
		if (parentBlock != null) {
			// IEnumerator enumer =
			// parentBlock.getContainedVisionBlockList().GetEnumerator();
			// enumer.Reset();
			int i = 0;
			for (Object Current : parentBlock.getContainedVisionBlockList()) {
				if ((VisionBlock) Current == this)
					break;
				else
					i++;
			}

			return i;
		}
		return 0;
	}

	// 得到当前Block的下一个兄弟Block
	public VisionBlock getNextSibingBlock() {
		VisionBlock parentBlock = null;
		if (this.parentBlock != null)
			parentBlock = this.parentBlock;

		if (parentBlock != null) {
			if (indexInList() == parentBlock.getContainedVisionBlockList()
					.size() - 1)
				return null;
			return (VisionBlock) parentBlock.getContainedVisionBlockList().get(
					indexInList() + 1);
		}

		return null;
	}

	// 返回当前Block的名字,格式通常为1-2-4
	public String getBlockName() {
		if (parentBlock == null)
			blockName = "VB1";
		else
			blockName = parentBlock.getBlockName() + "_" + (indexInList() + 1);

		return blockName;
	}

	public void removeBlock(VisionBlock block) {
		containedVisionBlockList.remove(block);
	}

	public void removeHTMLNode(CHTMLNode node) {
		containedHTMLNodeList.remove(node);
	}

	public boolean hasSubBlocks() {
		if (containedVisionBlockList.size() == 0)
			return false;
		else
			return true;
	}

	// 判断当前的Block是否是最上层的Block
	public void drawBlock(Object obj) {
		for (Object Current : containedHTMLNodeList) {
			((CHTMLNode) Current).DrawNode(obj);
		}
	}

	/*public void undrawBlock() {
		for (Object Current : containedHTMLNodeList)
			((CHTMLNode) Current).UnDrawNode();
	}*/

	// 从node中获取一些Block需要的信息
	public void convertFromNode(CHTMLNode node) {
		blockLeft = node.offsetLeft;
		blockTop = node.offsetTop;
		blockRight = node.offsetLeft + node.offsetWidth;
		blockButtom = node.offsetTop + node.offsetHeight;
	}

	// 判断给定的node是否在当前VisionBlock中
	private boolean isNodeInBlock(CHTMLNode node) {
		for (Object Current : containedHTMLNodeList) {
			if (node == (CHTMLNode) Current)
				return true;
		}
		return false;
	}

	private long max(long first, long second) {
		if (first >= second)
			return first;
		else
			return second;
	}

	private long min(long first, long second) {
		if (first < second)
			return first;
		else
			return second;
	}

	// 判断给定的结点node是否在block中
	private boolean nodeIsInBlock(NodePool pool) {
		CHTMLNode node = (CHTMLNode) pool.elementAt(0);
		// IEnumerator enumer = containedHTMLNodeList.GetEnumerator();
		// enumer.Reset();
		for (Object Current : containedHTMLNodeList) {
			if (node == (CHTMLNode) Current)
				return true;
		}
		return false;
	}

	// 判断当前的结点是否已经在存在的VisionBlock中，
	// 如果是，返回该VisionBlock，否则返回null
	public VisionBlock nodeIsInVisionBlock(NodePool pool) {
		for (int i = 0; i < containedVisionBlockList.size(); i++) {
			if (((VisionBlock) containedVisionBlockList.get(i))
					.nodeIsInBlock(pool))
				return (VisionBlock) containedVisionBlockList.get(i);
			else
				continue;
		}

		return null;
	}

	private void mergeBlockBetweenHorizontalSp(Splitter sp, long splitterLimit) {
		ArrayList removeList = new ArrayList();

		NodePool upPool = sp.leftUpBlock;
		NodePool downPool = sp.rightButtomBlock;
		// 如果上面的部分被VisionBlock包含，而下面的未被包含
		// 则根据分割条的粗细或者合并，或者生成新的VisionBlock
		VisionBlock upBlock = nodeIsInVisionBlock(upPool);
		VisionBlock downBlock = nodeIsInVisionBlock(downPool);

		// 在进行合并的时候必须考虑几种不同的分隔条的处理策略
		// 1如果分隔条是由HTML结点构成
		// 2如果分隔条的宽度达到一定的程度
		// 3如果分隔条上侧的Block高度都比较小，而下侧的Block的宽度非常高，那么即使
		// 分隔条不满足通常的条件，也进行合并
		if (upBlock == null && downBlock != null) {
			// 如果当前结点是显式的分隔条
			// sp.isExplicit == true意味着sp的高度小于10，但是是由结点构成
			// if(isRealHorizontalSp(sp,splitterLimit))
			if (sp.isRealHorizontalSp(splitterLimit)) {
				// 上面的部分单独成为一个新的visionBlock
				VisionBlock newBlock = new VisionBlock();
				newBlock.blockDirection = 0;
				newBlock.parentBlock = this;

				for (int j = 0; j < upPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(upPool.elementAt(j));

				newBlock.downSplitter = sp;
				downBlock.upSplitter = sp;

				containedVisionBlockList.add(newBlock);
			}
			// 把上面的结点合并到现有的downBlock中
			else {
				for (int j = 0; j < upPool.getCount(); j++)
					downBlock.containedHTMLNodeList.add(upPool.elementAt(j));

				downBlock.upSplitter = upPool.elementAt(0).spUp;
				// upPool.elementAt(0).spUp.rightButtomBlock = downBlock;
			}
		}

		// 如果上面的结点已经存在在某个block中，而下面的还没有
		if (upBlock != null && downBlock == null) {
			// if(isRealHorizontalSp(sp,splitterLimit))
			if (sp.isRealHorizontalSp(splitterLimit)) {
				// 上面的部分单独成为一个新的visionBlock
				VisionBlock newBlock = new VisionBlock();
				newBlock.blockDirection = 0;
				newBlock.parentBlock = this;

				for (int j = 0; j < downPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(downPool.elementAt(j));

				newBlock.upSplitter = sp;
				upBlock.downSplitter = sp;

				containedVisionBlockList.add(newBlock);
			} else {
				for (int j = 0; j < downPool.getCount(); j++)
					upBlock.containedHTMLNodeList.add(downPool.elementAt(j));

				upBlock.downSplitter = downPool.elementAt(0).spButtom;
			}
		}

		// 如果上面的和下面的都尚未报包含到特定的block中
		if (upBlock == null && downBlock == null) {
			// if(isRealHorizontalSp(sp,splitterLimit))
			if (sp.isRealHorizontalSp(splitterLimit)) {
				// 创建两个不同的VisionBlock
				VisionBlock upNewBlock = new VisionBlock();
				VisionBlock downNewBlock = new VisionBlock();
				upNewBlock.blockDirection = 0;
				downNewBlock.blockDirection = 0;
				upNewBlock.parentBlock = this;
				downNewBlock.parentBlock = this;

				for (int j = 0; j < upPool.getCount(); j++)
					upNewBlock.containedHTMLNodeList.add(upPool.elementAt(j));

				for (int j = 0; j < downPool.getCount(); j++)
					downNewBlock.containedHTMLNodeList.add(downPool.elementAt(j));

				upNewBlock.downSplitter = sp;
				downNewBlock.upSplitter = sp;

				containedVisionBlockList.add(upNewBlock);
				containedVisionBlockList.add(downNewBlock);
			}
			// 此时直接合并两个block
			else {
				VisionBlock newBlock = new VisionBlock();
				newBlock.blockDirection = 0;
				newBlock.parentBlock = this;

				for (int j = 0; j < upPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(upPool.elementAt(j));

				for (int j = 0; j < downPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(downPool.elementAt(j));

				newBlock.upSplitter = upPool.elementAt(0).spUp;
				newBlock.downSplitter = downPool.elementAt(0).spButtom;

				containedVisionBlockList.add(newBlock);
			}
		}

		// 如果上下的block都已经在某个block中，那么或者合并，或者什么事情都不作
		if (upBlock != null && downBlock != null) {
			// if(isRealHorizontalSp(sp,splitterLimit))
			if (sp.isRealHorizontalSp(splitterLimit)) {
				// 什么都不作
				upBlock.downSplitter = sp;
				downBlock.upSplitter = sp;
			} else
			// 进行合并
			{
				// 把downBlock中的所有的结点都合并到upBlock中
				for (int j = 0; j < downBlock.getContainedHTMLNodeList().size(); j++)
					upBlock.containedHTMLNodeList.add(downBlock.getContainedHTMLNodeList().get(j));

				upBlock.downSplitter = downBlock.downSplitter;

				removeList.add(downBlock);
			}
		}
		// IEnumerator removeEnumer = removeList.GetEnumerator();
		// removeEnumer.Reset();
		for (Object Current : removeList) {
			containedVisionBlockList.remove((VisionBlock) Current);
		}
		removeList.removeAll(removeList);
	}

	private void mergeBlockBetweenVerticalSp(Splitter sp, long splitterLimit) {
		ArrayList removeList = new ArrayList();

		NodePool leftPool = sp.leftUpBlock;
		NodePool rightPool = sp.rightButtomBlock;
		// 如果上面的部分被VisionBlock包含，而下面的未被包含
		// 则根据分割条的粗细或者合并，或者生成新的VisionBlock
		VisionBlock leftBlock = nodeIsInVisionBlock(leftPool);
		VisionBlock rightBlock = nodeIsInVisionBlock(rightPool);

		if (leftBlock == null && rightBlock != null) {
			// if(sp.width >=splitterLimit)
			if (sp.isRealVerticalSp(splitterLimit)) {
				// 上面的部分单独成为一个新的visionBlock
				VisionBlock newBlock = new VisionBlock();
				newBlock.blockDirection = 1;
				newBlock.parentBlock = this;

				for (int j = 0; j < leftPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(leftPool.elementAt(j));

				newBlock.rightSplitter = sp;
				rightBlock.leftSplitter = sp;

				containedVisionBlockList.add(newBlock);
			}
			// 把上面的结点合并到现有的downBlock中
			else {
				for (int j = 0; j < leftPool.getCount(); j++)
					rightBlock.containedHTMLNodeList.add(leftPool.elementAt(j));

				rightBlock.leftSplitter = leftPool.elementAt(0).spLeft;
			}
		}

		// 如果上面的结点已经存在在某个block中，而下面的还没有
		if (leftBlock != null && rightBlock == null) {
			// if(sp.width>=splitterLimit)
			if (sp.isRealVerticalSp(splitterLimit)) {
				// 上面的部分单独成为一个新的visionBlock
				VisionBlock newBlock = new VisionBlock();
				newBlock.blockDirection = 1;
				newBlock.parentBlock = this;

				for (int j = 0; j < rightPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(rightPool.elementAt(j));

				newBlock.leftSplitter = sp;
				leftBlock.rightSplitter = sp;

				containedVisionBlockList.add(newBlock);
			} else {
				for (int j = 0; j < rightPool.getCount(); j++)
					leftBlock.containedHTMLNodeList.add(rightPool.elementAt(j));

				leftBlock.rightSplitter = rightPool.elementAt(0).spRight;

			}
		}

		// 如果上面的和下面的都尚未报包含到特定的block中
		if (leftBlock == null && rightBlock == null) {
			// 此时直接合并两个block
			// if(sp.width>=splitterLimit)
			if (sp.isRealVerticalSp(splitterLimit)) {
				// 创建两个不同的VisionBlock
				VisionBlock leftNewBlock = new VisionBlock();
				VisionBlock rightNewBlock = new VisionBlock();
				leftNewBlock.blockDirection = 1;
				rightNewBlock.blockDirection = 1;
				leftNewBlock.parentBlock = this;
				rightNewBlock.parentBlock = this;

				for (int j = 0; j < leftPool.getCount(); j++)
					leftNewBlock.containedHTMLNodeList.add(leftPool.elementAt(j));

				for (int j = 0; j < rightPool.getCount(); j++)
					rightNewBlock.containedHTMLNodeList.add(rightPool.elementAt(j));

				leftNewBlock.rightSplitter = sp;
				rightNewBlock.leftSplitter = sp;

				containedVisionBlockList.add(leftNewBlock);
				containedVisionBlockList.add(rightNewBlock);
			}
			// 此时直接合并两个block
			else {
				VisionBlock newBlock = new VisionBlock();
				newBlock.blockDirection = 1;
				newBlock.parentBlock = this;

				for (int j = 0; j < leftPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(leftPool.elementAt(j));

				for (int j = 0; j < rightPool.getCount(); j++)
					newBlock.containedHTMLNodeList.add(rightPool.elementAt(j));

				newBlock.leftSplitter = leftPool.elementAt(0).spUp;
				newBlock.rightSplitter = rightPool.elementAt(0).spButtom;

				containedVisionBlockList.add(newBlock);
			}
		}

		// 如果上下的block都已经在某个block中，那么或者合并，或者什么事情都不作
		if (leftBlock != null && rightBlock != null) {
			if (sp.width >= splitterLimit) {
				// 什么都不作
				leftBlock.rightSplitter = sp;
				rightBlock.leftSplitter = sp;
			} else
			// 进行合并
			{
				// 把downBlock中的所有的结点都合并到upBlock中
				for (int j = 0; j < leftBlock.getContainedHTMLNodeList().size(); j++)
					leftBlock.containedHTMLNodeList.add(leftBlock.getContainedHTMLNodeList().get(j));

				leftBlock.rightSplitter = leftBlock.rightSplitter;

				removeList.add(rightBlock);
			}
		}
		// IEnumerator removeEnumer = removeList.GetEnumerator();
		// removeEnumer.Reset();
		for (Object Current : removeList)
			containedVisionBlockList.remove((VisionBlock) Current);
		removeList.removeAll(removeList);
	}

	// 给定条件
	// 分割后NodePool中的结点
	// 针对NodePool中结点所检测出来的分隔条
	// 根据这两个条件，重构VIPS的当前层次的语义块
	// 重构方法：
	// 首先将所有的分隔条进行排序，按照从细到粗的策略
	// 然后从最细的开始合并，紧紧保留最粗的
	// direction是使用的最终的分隔条
	public void constructVisionBlock(NodePool nPool, SplitterPool spPool,
			int direction, long splitterLimit) {
		// 首先尝试进行水平合并
		// 对于检测出来的每一个分隔条，判断它的宽度，如果它的宽度高于一定的值，比如10像素，那么它之后的所有的分隔条都不再合并
		// 分隔条两侧的页面也不再合并，而是直接作为语义块
		// 首先将分隔条进行排序

		// 该数组中保存最终实际的分割条
		// 0，使用水平分隔tia
		int realSplitter = 0;
		long lastHeight = 0;

		if (direction == 0) {
			int i = 0;
			for (; i < spPool.getHorizontalSplitterCount() - 1; i++) {
				Splitter sp = spPool.horizontalSplitterAt(i);
				if (sp.isRealHorizontalSp(splitterLimit))
					realSplitter++;
			}
			if (realSplitter == 0)
				// 以为最后一个分割条的高度作为分割高度
				lastHeight = spPool.horizontalSplitterAt(i).height;
			else
				lastHeight = splitterLimit;

			ArrayList removeList = new ArrayList();
			spPool.sortHorizontalSplitter("height");

			i = 0;
			for (; i < spPool.getHorizontalSplitterCount() - 1; i++) {
				Splitter sp = spPool.horizontalSplitterAt(i);
				mergeBlockBetweenHorizontalSp(sp, lastHeight);
			}

			// 问题出在这个地方,最后一个分隔条什么时候才能真正保留
			// 最后一个分隔条
			if (i < spPool.getHorizontalSplitterCount()) {
				Splitter lastSp = spPool.horizontalSplitterAt(i);

				// 如果当前的block数目为2那么该分隔条必须作为分隔条,否则会出现循环
				if (this.containedVisionBlockList.size() <= 2)
					// 故意设置最后一个分隔条的高度为20，超过指定的值
					// lastSp.height =20;
					// lastHeight = 20;
					lastHeight = lastSp.height;

				mergeBlockBetweenHorizontalSp(lastSp, lastHeight);
			}
		}
		if (direction == 1) {
			int i = 0;
			for (; i < spPool.getVerticalSplitterCount() - 1; i++) {
				Splitter sp = spPool.verticalSplitterAt(i);
				if (sp.isRealVerticalSp(splitterLimit))
					realSplitter++;
			}
			if (realSplitter == 0)
				lastHeight = spPool.verticalSplitterAt(i).width;
			else
				lastHeight = splitterLimit;

			// 根据垂直分割条进行合并
			ArrayList removeList = new ArrayList();
			spPool.sortVerticalSplitter("width");

			i = 0;
			for (; i < spPool.getVerticalSplitterCount() - 1; i++) {
				Splitter sp = spPool.verticalSplitterAt(i);
				mergeBlockBetweenVerticalSp(sp, lastHeight);
			}

			// 对于N个分隔条,之所以之合并N-1个,是因为害怕所有的分隔条间距最后都小于10,那么此时
			// 所有的节点将被合并,相当于没有分隔,这种情况下,应该将间隔最大的最为实际分隔条
			if (i < spPool.getVerticalSplitterCount()) {
				Splitter lastSp = spPool.verticalSplitterAt(i);
				if (this.containedVisionBlockList.size() <= 2)
					// lastSp.width =20;//
					lastHeight = lastSp.width;

				mergeBlockBetweenVerticalSp(lastSp, lastHeight);
			}
		}

		for (int i = 0; i < this.containedVisionBlockList.size(); i++) {
			// ((VisionBlock)this.containedVisionBlockList[i]).blockDirection
			// =0;
			setDOC((VisionBlock) this.containedVisionBlockList.get(i));
			calculateAreaOfBlock((VisionBlock) this.containedVisionBlockList.get(i));
		}

		if (direction == 0) {
			// 根据top进行能排序
			// HeapSort hSort = new HeapSort();
			// hSort.doSort(this.containedVisionBlockList,"blockTop");
			Collections.sort(this.containedVisionBlockList, new BlockTopCompare());
		}
		if (direction == 1) {
			Collections.sort(this.containedVisionBlockList, new BlockLeftCompare());
			// HeapSort hSort = new HeapSort();
			// hSort.doSort(this.containedVisionBlockList,"blockLeft");
		}

		// 检查是否有#text结点
		for (int i = 0; i < nPool.getCount(); i++) {
			CHTMLNode node = (CHTMLNode) nPool.elementAt(i);
			if (node.tagName == "#text") {
				VisionBlock textBlock = new VisionBlock(node.innerHTML);
				textBlock.parentBlock = this;
				this.containedVisionBlockList.add(textBlock);
			}
		}
	}

	// 该函数在组成Block后立即调用,通常用于设置Block的DOC值
	private void setDOC(VisionBlock block) {
		// 为了防止分割太细,我们规定Block的最小分割尺寸

		// 当前Block的DOC值为父亲DOC的值加1
		block.DOC = block.parentBlock.DOC + 1;
		// 如果当前Block只有一个Node,并且该Node为虚拟文本结点,那么DOC设置为10
		if (block.containedHTMLNodeList.size() == 1) {
			if (((CHTMLNode) block.containedHTMLNodeList.get(0)).isVirtualTextNode())
				block.DOC = 15;
			else
				block.DOC = block.DOC + 1;
		}

		if (block.DOC >= 15)
			return;
	}

	// 计算当前Block的大小区域
	private void calculateAreaOfBlock(VisionBlock block) {
		block.blockLeft = ((CHTMLNode) block.containedHTMLNodeList.get(0)).offsetLeft;
		block.blockTop = ((CHTMLNode) block.containedHTMLNodeList.get(0)).offsetTop;

		for (int i = 0; i < block.containedHTMLNodeList.size(); i++) {
			CHTMLNode cnode = (CHTMLNode) block.containedHTMLNodeList.get(i);
			if (cnode.offsetButtom > block.blockButtom)
				block.blockButtom = cnode.offsetButtom;

			if (cnode.offsetRight > block.blockRight)
				block.blockRight = cnode.offsetRight;

			if (cnode.offsetTop < block.blockTop)
				block.blockTop = cnode.offsetTop;

			if (cnode.offsetLeft < block.blockLeft)
				block.blockLeft = cnode.offsetLeft;
		}

		block.blockWidth = block.blockRight - block.blockLeft;
		block.blockHeight = block.blockButtom - block.blockTop;
	}

	// 得到当前语义块的分割方向
	private int getBlockDivideDirection() {
		if (this.hasSubBlocks()) {
			VisionBlock childBlock = (VisionBlock) this.getContainedVisionBlockList().get(0);
			return childBlock.blockDirection;
		}
		return -1;
	}

	// 分割当前的Block,分割后的结果保存到孩子containedVisionBlock中
	public void divideBlock(int pDOC) {
		if (DOC < pDOC) {
			// 如果当前Block中包含多个Node,那么每个Node作为一个单个的SubBlock
			if (this.containedHTMLNodeList.size() > 1) {
				// 对于每一个结点都生成一个VisionBlock
				NodePool pool = new NodePool();
				for (int i = 0; i < this.containedHTMLNodeList.size(); i++) {
					CHTMLNode node = (CHTMLNode) this.containedHTMLNodeList.get(i);
					pool.addToPool(node);
				}

				SplitterPool spPool = new SplitterPool(this);
				int direction = spPool.detectAllSplitterFor(pool, null);

				spPool.sortHorizontalSplitter("top");
				spPool.sortVerticalSplitter("left");

				this.constructVisionBlock(pool, spPool, direction, 10);
				this.divideDirection = getBlockDivideDirection();
			}
			// 调用结点的DivideNode进行处理
			else {
				// 增加处理TextNode的能力
				ArrayList spNodeList = null;
				CHTMLNode node = (CHTMLNode) this.containedHTMLNodeList.get(0);
				NodePool pool = new NodePool();
				spNodeList = node.divideDOMTree(pool, 2);

				// SplitterPool spPool1 = new SplitterPool(node);
				if (pool.getCount() == 0)
					return;
				SplitterPool spPool = new SplitterPool(pool);
				int direction = spPool.detectAllSplitterFor(pool, spNodeList);

				spPool.sortHorizontalSplitter("top");
				spPool.sortVerticalSplitter("left");

				this.constructVisionBlock(pool, spPool, direction, 10);
				this.divideDirection = this.getBlockDivideDirection();
			}

			for (int i = 0; i < this.containedVisionBlockList.size(); i++)
				((VisionBlock) this.containedVisionBlockList.get(i)).divideBlock(pDOC);
		}
	}

	static class BlockTopCompare implements Comparator<VisionBlock> {
		public int compare(VisionBlock o1, VisionBlock o2) {
			if (o1.blockTop > o2.blockTop) {
				return 1;
			}
			if (o1.blockTop < o2.blockTop) {
				return -1;
			}
			return 0;
		}
	}

	static class BlockLeftCompare implements Comparator<VisionBlock> {
		public int compare(VisionBlock o1, VisionBlock o2) {
			if (o1.blockLeft > o2.blockLeft) {
				return 1;
			}
			if (o1.blockLeft < o2.blockLeft) {
				return -1;
			}
			return 0;
		}
	}
}