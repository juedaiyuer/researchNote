package com.lietu.vips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.w3c.dom.html2.HTMLDocument;

import com.lietu.vips.VisionBlock.BlockTopCompare;

/// <summary>
/// SplitterList 的摘要说明。
/// </summary>
/// 分割条列表,其中保存所有的被检测出来的分割条
public class SplitterPool {
	// 内部数据结构
	private ArrayList removeList;// 其中保存需要移除的list
	private ArrayList newAddList;// 找到新检测到的list,之所以并不把插入到removeList和newAddList中
	// 是因为splitterList正在进行enumer操作,一旦该操作进行,任何元素都
	// 不允许插入,否则会异常,因此正确的做法就是把所有需要改动的纪录先
	// 临时保存起来,但枚举结束再修改

	private ArrayList verticalSplitterList;// 垂直分隔条列表
	private ArrayList horizontalSplitterList;// 水平分隔条列表

	public int validSplitterDirection;
	private long marginalTop;
	private long marginalButtom;
	private long marginalLeft;
	private long marginalRight;

	public SplitterPool(HTMLDocument document) {
		verticalSplitterList = new ArrayList();
		horizontalSplitterList = new ArrayList();

		removeList = new ArrayList();
		newAddList = new ArrayList();

		Splitter spVertical = new Splitter(document, true);
		Splitter spHorizontal = new Splitter(document, false);

		// 将当前的整个页面作为分割条,分别是垂直分割条和水平分割条
		spVertical.setSplitterPool(this);
		spHorizontal.setSplitterPool(this);

		verticalSplitterList.add(spVertical);
		horizontalSplitterList.add(spHorizontal);

		validSplitterDirection = -1;

		HTMLElementImpl body = (HTMLElementImpl) document.getBody();
		CHTMLNode node = new CHTMLNode(body);

		marginalTop = node.offsetTop;
		marginalButtom = node.offsetButtom;
		marginalLeft = node.offsetLeft;
		marginalRight = node.offsetRight;
	}

	// 基于逻辑块开始进行分割
	public SplitterPool(CHTMLNode node) {
		verticalSplitterList = new ArrayList();
		horizontalSplitterList = new ArrayList();

		removeList = new ArrayList();
		newAddList = new ArrayList();

		Splitter spVertical = new Splitter(node, true);
		Splitter spHorizontal = new Splitter(node, false);

		// 将当前的整个页面作为分割条,分别是垂直分割条和水平分割条
		spVertical.setSplitterPool(this);
		spHorizontal.setSplitterPool(this);

		verticalSplitterList.add(spVertical);
		horizontalSplitterList.add(spHorizontal);

		validSplitterDirection = -1;

		marginalTop = node.offsetTop;
		marginalButtom = node.offsetButtom;
		marginalLeft = node.offsetLeft;
		marginalRight = node.offsetRight;
	}

	public SplitterPool(VisionBlock block) {
		verticalSplitterList = new ArrayList();
		horizontalSplitterList = new ArrayList();

		removeList = new ArrayList();
		newAddList = new ArrayList();

		Splitter spVertical = new Splitter(block, true);
		Splitter spHorizontal = new Splitter(block, false);

		// 将当前的整个页面作为分割条,分别是垂直分割条和水平分割条
		spVertical.setSplitterPool(this);
		spHorizontal.setSplitterPool(this);

		verticalSplitterList.add(spVertical);
		horizontalSplitterList.add(spHorizontal);

		validSplitterDirection = -1;

		marginalTop = block.blockTop;
		marginalButtom = block.blockButtom;
		marginalLeft = block.blockLeft;
		marginalRight = block.blockRight;
	}

	public SplitterPool(NodePool pool) {
		verticalSplitterList = new ArrayList();
		horizontalSplitterList = new ArrayList();

		removeList = new ArrayList();
		newAddList = new ArrayList();

		marginalTop = pool.elementAt(0).offsetTop;
		marginalLeft = pool.elementAt(0).offsetLeft;
		marginalRight = pool.elementAt(0).offsetRight;
		marginalButtom = pool.elementAt(0).offsetButtom;

		for (int i = 1; i < pool.getCount(); i++) {
			if (pool.elementAt(i).offsetTop <= marginalTop)
				marginalTop = pool.elementAt(i).offsetTop;

			if (pool.elementAt(i).offsetButtom >= marginalButtom)
				marginalButtom = pool.elementAt(i).offsetButtom;

			if (pool.elementAt(i).offsetLeft <= marginalLeft)
				marginalLeft = pool.elementAt(i).offsetLeft;

			if (pool.elementAt(i).offsetRight >= marginalRight)
				marginalRight = pool.elementAt(i).offsetRight;
		}
		/*
		 * marginalRight = 0; marginalButtom = 0; marginalTop =
		 * ((CHTMLNode)pool.elementAt(0)).offsetTop; marginalLeft =
		 * ((CHTMLNode)pool.elementAt(0)).offsetLeft;
		 * 
		 * for(int i=0;i<pool.getCount();i++) { CHTMLNode cnode =
		 * (CHTMLNode)pool.elementAt(i); if(cnode.offsetButtom>marginalButtom)
		 * marginalButtom = cnode.offsetButtom;
		 * 
		 * if(cnode.offsetRight >marginalRight) marginalRight =
		 * cnode.offsetRight;
		 * 
		 * if(cnode.offsetTop < marginalTop) marginalTop = cnode.offsetTop;
		 * 
		 * if(cnode.offsetLeft < marginalLeft) marginalLeft = cnode.offsetLeft; }
		 */

		Splitter spVertical = new Splitter(marginalLeft, marginalTop, marginalRight, marginalButtom, true);
		Splitter spHorizontal = new Splitter(marginalLeft, marginalTop, marginalRight, marginalButtom, false);

		// 将当前的整个页面作为分割条,分别是垂直分割条和水平分割条
		spVertical.setSplitterPool(this);
		spHorizontal.setSplitterPool(this);

		verticalSplitterList.add(spVertical);
		horizontalSplitterList.add(spHorizontal);
	}

	public ArrayList getVerticalSplitter() {
		return verticalSplitterList;
	}

	public ArrayList getHorizontalSplitter() {
		return horizontalSplitterList;
	}

	// 将分割条保存在分割条池子中
	public void addToPool(Splitter sp) {
		if (sp.vertical == true)
			verticalSplitterList.add(sp);
		else
			horizontalSplitterList.add(sp);
	}

	// 标记该分隔条，这样，在结束的时候这个分隔条就可以被删除了
	private void setRemoveFlag(Splitter sp) {
		removeList.add(sp);
	}

	private void setNewAddFlag(Splitter sp) {
		newAddList.add(sp);
	}

	// 从pool中移除该sp
	public void removeSplitter(Splitter sp) {
		if (sp.vertical == true)
			verticalSplitterList.remove(sp);
		else
			horizontalSplitterList.remove(sp);
	}

	// 在初次检测的时候，每个结点周围都具有四个分隔条，
	// 对于pool中的每一个页面块，如果它的相邻的分隔条中包含sp，那么
	// 删除该分隔条
	private boolean removeNeighbourSplitter(CHTMLNode node, Splitter sp) {
		if (node.spLeft == sp) {
			node.spLeft = null;
			return true;
		} else if (node.spRight == sp) {
			node.spRight = null;
			return true;
		} else if (node.spUp == sp) {
			node.spUp = null;
			return true;
		} else if (node.spButtom == sp) {
			node.spButtom = null;
			return true;
		} else
			return false;

	}

	// 在当前的结点池中，对于任意一个结点，如果它的边界有分隔条sp，那么将该分隔条从
	// 边界分隔条中移出
	private void removeSpFromNeighbour(NodePool pool, Splitter sp) {
		for (int i = 0; i < pool.getCount(); i++) {
			removeNeighbourSplitter((CHTMLNode) pool.elementAt(i), sp);
		}
	}

	// 检查指定的node是否在pool中存在
	private boolean nodeIsExisit(NodePool pool, CHTMLNode node) {
		for (int i = 0; i < pool.getCount(); i++) {
			if ((CHTMLNode) pool.elementAt(i) == node)
				return true;
			continue;
		}

		return false;
	}

	// 对于给定的结点,查找它周围的分隔条,
	// 该函数仅仅适用于不相交,不重合,不覆盖的情况
	private void getHorizontalSplitterAround(CHTMLNode node) {
		// 调用该函数的时候顶部和底部的分隔条尚未被移除，因此至少存在两个分隔条

		// 将所有的分隔条按照从小到大的顺序进行排序
		sortHorizontalSplitter("top");
		// 查找当前结点的上部分隔条
		// 查找依据,该分隔条的下部小于结点的下部,同时距离也最小,即是最靠近的分隔条
		long minDistance = node.offsetTop
				- ((Splitter) horizontalSplitterList.get(0)).buttom;
		int i;
		for (i = 1; i < horizontalSplitterList.size(); i++) {
			Splitter sp = (Splitter) horizontalSplitterList.get(i);
			if (minDistance > node.offsetTop - sp.buttom && node.offsetTop - sp.buttom >= 0)
				minDistance = node.offsetTop - sp.buttom;
			else
				break;
		}
		// 第i-1个分隔条就是我们所要找的分隔条
		node.setNeighbourSplitter((Splitter) horizontalSplitterList.get(i - 1),0);
		((Splitter) horizontalSplitterList.get(i - 1)).rightButtomBlock.addToPool(node);
		// horizontalSplitterList[i-1]的上册结点尚未确定
		// 由于horizontalSplitterList[i-2]分割条可以确定，因此[i-1]的上面的分割条就是[i-1]分割条的下侧
		if (i - 2 >= 0) {
			for (int j = 0; j < ((Splitter) horizontalSplitterList.get(i - 2)).rightButtomBlock.getCount(); j++) {
				CHTMLNode nodeTemp = (CHTMLNode) (((Splitter) horizontalSplitterList.get(i - 2)).rightButtomBlock.elementAt(j));
				// 检查nodeTemp是否已经在leftUpBlock中存在
				if (!nodeIsExisit(((Splitter) horizontalSplitterList.get(i - 1)).leftUpBlock,nodeTemp))
					((Splitter) horizontalSplitterList.get(i - 1)).leftUpBlock.addToPool(nodeTemp);

				// ///////////////////////////////////
				nodeTemp.setNeighbourSplitter((Splitter) horizontalSplitterList.get(i - 1), 1);
			}
		}
		// 查找当前结点的下侧分隔条
		node.setNeighbourSplitter((Splitter) horizontalSplitterList.get(i), 1);
		((Splitter) horizontalSplitterList.get(i)).leftUpBlock.addToPool(node);
		// 如果存在i+1个分割条

		// i不是最后一个结点
		// 同时i后面存在下一个分割条
		if (i <= horizontalSplitterList.size() - 2) {
			for (int j = 0; j < ((Splitter) horizontalSplitterList.get(i + 1)).leftUpBlock.getCount(); j++) {
				CHTMLNode nodeTemp = (CHTMLNode) (((Splitter) horizontalSplitterList.get(i + 1)).leftUpBlock.elementAt(j));
				// 检查nodeTemp是否已经在leftUpBlock中存在
				if (!nodeIsExisit(((Splitter) horizontalSplitterList.get(i)).rightButtomBlock,nodeTemp))
					((Splitter) horizontalSplitterList.get(i)).rightButtomBlock.addToPool(nodeTemp);

				// //////////////////////////////////////////
				nodeTemp.setNeighbourSplitter((Splitter) horizontalSplitterList.get(i), 0);
			}
		}
		// ((Splitter)horizontalSplitterList[i]).rightButtomBlock.addToPool();
	}

	private void getVerticalSplitterAround(CHTMLNode node) {
		// 将所有的垂直分隔条按照从小到大的顺序进行排序
		sortVerticalSplitter("left");
		// 查找当前结点的上部分隔条
		// 查找依据,该分隔条的下部小于结点的下部,同时距离也最小,即是最靠近的分隔条
		long minDistance = node.offsetLeft - ((Splitter) verticalSplitterList.get(0)).right;
		int i;
		for (i = 1; i < verticalSplitterList.size(); i++) {
			Splitter sp = (Splitter) verticalSplitterList.get(i);
			if (minDistance > node.offsetLeft - sp.right && node.offsetLeft - sp.right >= 0)
				minDistance = node.offsetLeft - sp.right;
			else
				break;
		}
		// 第i-1个分隔条就是我们所要找的分隔条
		node.setNeighbourSplitter((Splitter) verticalSplitterList.get(i - 1), 2);
		((Splitter) verticalSplitterList.get(i - 1)).rightButtomBlock.addToPool(node);
		// verticalSplitterList[i-1]的上册结点尚未确定
		// 由于verticalSplitterList[i-2]分割条可以确定，因此[i-1]的上面的分割条就是[i-1]分割条的下侧

		if (i - 2 >= 0) {
			for (int j = 0; j < ((Splitter) verticalSplitterList.get(i - 2)).rightButtomBlock.getCount(); j++) {
				CHTMLNode nodeTemp = (CHTMLNode) (((Splitter) verticalSplitterList.get(i - 2)).rightButtomBlock.elementAt(j));
				if (!nodeIsExisit(((Splitter) verticalSplitterList.get(i - 1)).leftUpBlock,nodeTemp))
					((Splitter) verticalSplitterList.get(i - 1)).leftUpBlock.addToPool(nodeTemp);

				nodeTemp.setNeighbourSplitter((Splitter) verticalSplitterList.get(i - 1), 2);
			}
		}

		// 查找当前结点的下侧分隔条
		node.setNeighbourSplitter((Splitter) verticalSplitterList.get(i), 3);
		((Splitter) verticalSplitterList.get(i)).leftUpBlock.addToPool(node);

		if (i <= this.verticalSplitterList.size() - 2) {
			for (int j = 0; j < ((Splitter) verticalSplitterList.get(i + 1)).leftUpBlock.getCount(); j++) {
				CHTMLNode nodeTemp = (CHTMLNode) (((Splitter) verticalSplitterList.get(i + 1)).leftUpBlock.elementAt(j));
				// 检查nodeTemp是否已经在leftUpBlock中存在
				if (!nodeIsExisit(((Splitter) verticalSplitterList.get(i)).rightButtomBlock,nodeTemp))
					((Splitter) verticalSplitterList.get(i)).rightButtomBlock.addToPool(nodeTemp);

				// nodeTemp.setNeighbourSplitter((Splitter)verticalSplitterList[i-1],3);
				nodeTemp.setNeighbourSplitter((Splitter) verticalSplitterList.get(i), 3);
			}
		}
	}

	// 对于该node,调整合适的分割条，包括三个主要的步骤
	// 1.检测水平分割条
	// 2.设置当前分隔条的两侧结点
	// 3.设置对应结点的两侧分隔条
	private void detectHorizontal(CHTMLNode node) {
		for (Object Current : horizontalSplitterList) {
			Splitter sp = (Splitter) Current;
			// 根据sp和node的关系进行各种处理
			// 当前node横跨整个分割条,此时,该分割条必须被移除
			if (sp.blockIsAcrossSplitter(node)) {
				// 由于当前block跨越某个某个分割条,此时将该分割条两侧的
				// 分割条合并为一块
				// 如果该分割条为水平分割条,获取该
				setRemoveFlag(sp);
				removeSpFromNeighbour(node.nodePool, sp);

				// 遍历所有的Block，如果某个Block的相邻分隔条为sp，那么从它的相邻列表中
				// 移除该分隔条

				continue;
			}
			// 当前node在分割条内部,此时,将分割条分割四个小的分割条,
			// 水平方向两个,垂直方向两个，在该函数中仅仅检测水平分隔
			// 1.确定当前分隔条两侧的Block
			if (sp.blockIsInSplitter(node)) {
				// spUp表示结点上部的分隔条
				// spDown表示结点下部的分隔条
				Splitter spUp = new Splitter();
				Splitter spDown = new Splitter();

				// 设置上部分隔条的位置和大小
				spUp.left = sp.left;
				spUp.top = sp.top;
				spUp.right = sp.right;
				spUp.buttom = node.offsetTop;
				spUp.width = spUp.right - spUp.left;
				spUp.height = spUp.buttom - spUp.top;
				spUp.vertical = false;
				spUp.setSplitterPool(this);

				// 将node作为分隔条spUp的下部Block保存
				// spUp.rightButtomBlock.addToPool(node);
				setNewAddFlag(spUp);

				// 设置下部分隔条的位置和大小
				spDown.left = sp.left;
				spDown.top = node.offsetTop + node.offsetHeight;
				spDown.right = sp.right;
				spDown.buttom = sp.buttom;
				spDown.width = spDown.right - spDown.left;
				spDown.height = spDown.buttom - spDown.top;

				spDown.vertical = false;
				spDown.setSplitterPool(this);

				// spDown.leftUpBlock.addToPool(node);
				setNewAddFlag(spDown);

				setRemoveFlag(sp);

				// 将该分隔条作为结点node的上分隔条
				// node.setNeighbourSplitter(spUp,0);
				// node.setNeighbourSplitter(spDown,1);
				continue;
			}

			// 如果当前的分割条与block相交,则此时必须调整分割条的begin和end
			// 对于部分相交，根据相交的位置调整分隔条
			int result = sp.isIntersectWithBlock(node);
			// 对于上部相交
			if (result == 1) {
				sp.adjust(sp.left, sp.top, sp.right, node.offsetTop);
				// node.setNeighbourSplitter(sp,0);
				// sp.rightButtomBlock.addToPool(node);
			}
			// 下部相交
			else if (result == 2) {
				sp.adjust(sp.left, node.offsetTop + node.offsetHeight,sp.right, sp.buttom);
				// node.setNeighbourSplitter(sp,1);
				// sp.leftUpBlock.addToPool(node);
			}

			// 如果不相交,不覆盖,不交叉
			// 此时需要做的仅仅是查找当前结点的相邻分隔条
			// 同时调整分隔条的相邻结点
			// 首先查找该结点对应的分隔条
		}

		// //////////////////////////////////////////////
		for (Object Current : removeList) {
			removeSplitter((Splitter) Current);
		}
		removeList.clear();

		for (Object Current : newAddList) {
			addToPool((Splitter) Current);
		}
		newAddList.clear();

		getHorizontalSplitterAround(node);
	}

	// 检测垂直分割条
	private void detectVertical(CHTMLNode node) {
		for (Object Current : verticalSplitterList) {
			Splitter sp = (Splitter) Current;
			if (sp.vertical == true) {
				// 根据sp和node的关系进行各种处理
				// 当前node横跨整个分割条,此时,该分割条必须被移除
				if (sp.blockIsAcrossSplitter(node)) {
					// 由于当前block跨越某个某个分割条,此时将该分割条两侧的
					// 分割条合并为一块
					// 如果该分割条为水平分割条,获取该
					setRemoveFlag(sp);
					// //////////////////////////////////////////////////////
					// //////////////////////////////////////////////////////
					removeSpFromNeighbour(node.nodePool, sp);

					continue;
				}
				// 当前node在分割条内部,此时,将分割条分割四个小的分割条,
				// 水平方向两个,垂直方向两个
				if (sp.blockIsInSplitter(node)) {
					Splitter spLeft = new Splitter();
					Splitter spRight = new Splitter();

					// 生成左侧分割条
					spLeft.left = sp.left;
					spLeft.top = sp.top;
					spLeft.right = node.offsetLeft;
					spLeft.buttom = sp.buttom;
					spLeft.width = spLeft.right - spLeft.left;
					spLeft.height = spLeft.buttom - spLeft.top;
					spLeft.vertical = true;

					spLeft.setSplitterPool(this);
					// spLeft.rightButtomBlock.addToPool(node);
					setNewAddFlag(spLeft);

					spRight.left = node.offsetLeft + node.offsetWidth;
					spRight.top = sp.top;
					spRight.right = sp.right;
					spRight.buttom = sp.buttom;
					spRight.width = spRight.right - spRight.left;
					spRight.height = spRight.buttom - spRight.top;

					spRight.vertical = true;

					spRight.setSplitterPool(this);
					// spRight.leftUpBlock.addToPool(node);
					setNewAddFlag(spRight);

					setRemoveFlag(sp);

					// node.setNeighbourSplitter(spLeft,2);
					// node.setNeighbourSplitter(spRight,3);
					continue;
				}
				// 如果当前的分割条与block相交,则此时必须调整分割条的begin和end
				int result = sp.isIntersectWithBlock(node);
				// 对于左部相交
				if (result == 3) {
					sp.adjust(sp.left, sp.top, node.offsetLeft, sp.buttom);
					// node.setNeighbourSplitter(sp,2);
					// sp.rightButtomBlock.addToPool(node);
				}
				// 对于右部相交
				else if (result == 4) {
					sp.adjust(node.offsetLeft + node.offsetWidth, sp.top,
							sp.right, sp.buttom);
					// node.setNeighbourSplitter(sp,3);
					// sp.leftUpBlock.addToPool(node);
				}
			}
		}
		for (Object Current : removeList) {
			removeSplitter((Splitter) Current);
		}
		removeList.clear();

		for (Object Current : newAddList) {
			addToPool((Splitter) Current);
		}

		getVerticalSplitterAround(node);
	}

	private boolean spIsNodeSp(Splitter sp, ArrayList spNodeList) {
		if (spNodeList != null) {
			for (int i = 0; i < spNodeList.size(); i++) {
				CHTMLNode cnode = (CHTMLNode) spNodeList.get(i);
				if (cnode.offsetTop >= sp.top && cnode.offsetLeft >= sp.left
						&& cnode.offsetRight <= sp.right
						&& cnode.offsetButtom <= sp.buttom)
					return true;
			}
		}

		return false;
	}

	public int detectHorizontalSplitter(NodePool elPool, ArrayList spNodeList) {
		for (Object Current : spNodeList) {
			CHTMLNode node = (CHTMLNode) Current;
			if (node.tagName != "#text")
				detectHorizontal(node);
			continue;
		}
		removeMarginalSplitter(0);

		for (int i = 0; i < this.horizontalSplitterList.size(); i++) {
			Splitter sp = (Splitter) this.horizontalSplitterList.get(i);
			if (this.spIsNodeSp(sp, spNodeList))
				sp.isExplicit = true;
		}

		// 对检测出来的分隔条进行判断其是否是由分隔条结点实现
		return getHorizontalSplitterCount();
	}

	public int detectVerticalSplitter(NodePool elPool, ArrayList/* VIPSArrayList */spNodeList) {
		for (Object Current : spNodeList) {
			CHTMLNode node = (CHTMLNode) Current;
			if (node.tagName != "#text")
				detectVertical(node);

			//continue;
		}
		removeMarginalSplitter(1);

		for (int i = 0; i < this.verticalSplitterList.size(); i++) {
			Splitter sp = (Splitter) this.verticalSplitterList.get(i);
			if (spIsNodeSp(sp, spNodeList))
				sp.isExplicit = true;
		}

		return this.getVerticalSplitterCount();

	}

	// 分别检测垂直和水平分隔条的情况,并确定最终的分隔情况
	public int detectAllSplitterFor(NodePool elPool, ArrayList spNodeList) {
		int horizontalCount = this.detectHorizontalSplitter(elPool, spNodeList);
		int verticalCount = this.detectVerticalSplitter(elPool, spNodeList);

		// 水平方向和垂直方向的分隔条都是0个,这意味着当前Block不再可以分隔
		if (horizontalCount == 0 && verticalCount == 0)
			return -1;

		// 如果水平方向的分隔条为0,而垂直方向不为0,则垂直分隔
		else if (horizontalCount == 0 && verticalCount != 0)
			return 1;

		// 如果水平方向的分隔条不为0,而垂直方向的分隔条为0,则水平分隔
		else if (horizontalCount != 0 && verticalCount == 0)
			return 0;

		// 如果水平和垂直方向的分隔条数目都不为0，则此时的必须综合各方面的信息进行最终的合并
		else if (horizontalCount != 0 && verticalCount != 0) {
			// 决定分隔的方向,水平分隔还是垂直分隔

			return 1;
		}

		return 0;
	}

	private void removeMarginalSplitter(int direction) {
		Iterator removeEnumer = null;

		if (direction == 1) {
			removeEnumer = verticalSplitterList.iterator();
			// removeEnumer.Reset();
			for (; removeEnumer.hasNext();) {
				Splitter sp = (Splitter) removeEnumer.next();
				if (sp.left == marginalLeft) {
					setRemoveFlag(sp);
					removeSpFromNeighbour(sp.leftUpBlock, sp);
				}

				if (sp.right == marginalRight) {
					setRemoveFlag(sp);
					removeSpFromNeighbour(sp.rightButtomBlock, sp);
				}
			}
		} else {
			removeEnumer = horizontalSplitterList.iterator();
			// removeEnumer.Reset();
			for (; removeEnumer.hasNext();) {
				Splitter sp = (Splitter) removeEnumer.next();
				if (sp.top == marginalTop) {
					setRemoveFlag(sp);
					removeSpFromNeighbour(sp.rightButtomBlock, sp);
				}

				if (sp.buttom == marginalButtom) {
					setRemoveFlag(sp);
					removeSpFromNeighbour(sp.leftUpBlock, sp);
				}
			}
		}
		// 垂直列表中删除所有的水平分割条
		for (Object Current : removeList) {
			removeSplitter((Splitter) Current);
		}
		removeList.clear();
	}

	public int getSplitterCount() {
		return verticalSplitterList.size() + horizontalSplitterList.size();
	}

	public int getVerticalSplitterCount() {
		return verticalSplitterList.size();
	}

	public int getHorizontalSplitterCount() {
		return horizontalSplitterList.size();
	}

	public Splitter verticalSplitterAt(int index) {
		return (Splitter) verticalSplitterList.get(index);
	}

	public Splitter horizontalSplitterAt(int index) {
		return (Splitter) horizontalSplitterList.get(index);
	}
	
	// 对所有的分隔条进行排序
	public void sortVerticalSplitter(String fieldName) {
		/*HeapSort hSort = new HeapSort();
		hSort.doSort(verticalSplitterList, fieldName);*/
		Collections.sort(this.verticalSplitterList, new sortVerticalCompare());
	}

	// 对水平分隔条进行排序，按照粗细程度排序
	public void sortHorizontalSplitter(String fieldName) {
		/*HeapSort hSort = new HeapSort();
		hSort.doSort(horizontalSplitterList, fieldName);*/
		Collections.sort(this.horizontalSplitterList, new sortHorizontalCompare());
	}

	static class sortVerticalCompare implements Comparator<Splitter> {
		public int compare(Splitter o1, Splitter o2) {
			if (o1.height > o2.height) {
				return 1;
			}
			if (o1.height < o2.height) {
				return -1;
			}
			return 0;
		}
	}
	static class sortHorizontalCompare implements Comparator<Splitter> {
		public int compare(Splitter o1, Splitter o2) {
			if (o1.width > o2.width) {
				return 1;
			}
			if (o1.width < o2.width) {
				return -1;
			}
			return 0;
		}
	}
}
