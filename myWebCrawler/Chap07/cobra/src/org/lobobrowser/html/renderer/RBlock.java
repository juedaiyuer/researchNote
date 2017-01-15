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
 * Created on Apr 16, 2005
 */
package org.lobobrowser.html.renderer;

import java.util.*;
import java.util.logging.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.awt.image.ImageObserver;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.html.style.*;
import org.lobobrowser.util.*;
import org.w3c.dom.Node;

/**
 * @author J. H. S.
 */
public class RBlock extends BaseElementRenderable implements RenderableContainer, ImageObserver {
	static final Logger logger = Logger.getLogger(RBlock.class.getName());
	private static final boolean loggableInfo = logger.isLoggable(Level.INFO);
	private static final FloatingBounds INVALID_FLOAT_BOUNDS = new FloatingViewportBounds(null, 0, 0, 0, 0);
	
	protected final FrameContext frameContext;
	protected final int listNesting;
	protected final HtmlRendererContext rendererContext;
	protected final int defaultOverflow;
	protected final RBlockViewport bodyLayout;

	protected RenderableSpot startSelection;
	protected RenderableSpot endSelection;
	
	protected JScrollBar vScrollBar;
	protected JScrollBar hScrollBar;
	protected boolean hasHScrollBar = false;
	protected boolean hasVScrollBar = false;


	// Validation-dependent variables...
	//private Dimension layoutSize = null;
	private Boolean lastExpandWidth = null;
	private Boolean lastExpandHeight = null;
	private int lastAvailHeight = -1;
	private int lastAvailWidth = -1;
	private int lastWhiteSpace = -1;
	private FloatingBounds lastFloatBounds = INVALID_FLOAT_BOUNDS;
	private Font lastFont = null;	

	public RBlock(NodeImpl modelNode, int listNesting, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext, RenderableContainer parentContainer) {
		this(modelNode, listNesting, pcontext, rcontext, frameContext, parentContainer, RBlock.OVERFLOW_NONE);
	}
	
	public RBlock(NodeImpl modelNode, int listNesting, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext, RenderableContainer parentContainer, int defaultOverflow) {
		super(parentContainer, modelNode, pcontext);
		this.listNesting = listNesting;
		this.frameContext = frameContext;
		this.rendererContext = rcontext;
		this.defaultOverflow = defaultOverflow;
		RBlockViewport bl = new RBlockViewport(modelNode, this, this.getViewportListNesting(listNesting), pcontext, rcontext, frameContext, this);
		this.bodyLayout = bl;
		bl.setOriginalParent(this);
		// Initialize origin of RBlockViewport to be as far top-left as possible.
		// This will be corrected on first layout.
		bl.setX(Short.MAX_VALUE);
		bl.setY(Short.MAX_VALUE);
	}
			
	/**
	 * Gets the width the vertical scrollbar has when shown.
	 */
	public int getVScrollBarWidth() {
		return SCROLL_BAR_THICKNESS;
	}
	
	public void finalize() throws Throwable {
		super.finalize();
	}
	
	public int getVAlign() {
		// Not used
		return VALIGN_BASELINE;
	}

	public void ensureVisible(Point point) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			boolean hscroll = this.hasHScrollBar;
			boolean vscroll = this.hasVScrollBar;
			int origX = bodyLayout.x;
			int origY = bodyLayout.y;
			Insets insets = this.getInsets(hscroll, vscroll);
			if(hscroll) {
				if(point.x < insets.left) {
					bodyLayout.x += (insets.left - point.x);
				}
				else if(point.x > this.width - insets.right) {
					bodyLayout.x -= (point.x - this.width + insets.right);
				}
			}
			if(vscroll) {
				if(point.y < insets.top) {
					bodyLayout.y += (insets.top - point.y);
				}
				else if(point.y > this.height - insets.bottom) {
					bodyLayout.y -= (point.y - this.height + insets.bottom);
				}
			}
			if(hscroll || vscroll) {
				this.correctViewportOrigin(insets, this.width, this.height);
				if(origX != bodyLayout.x || origY != bodyLayout.y) {
					this.resetScrollBars(null);
					//TODO: This could be paintImmediately.
					this.repaint();
				}
			}
		}
	}
	
	private JScrollBar getHScrollBar() {
		JScrollBar sb = this.hScrollBar;
		if(sb == null) {
			// Should never go back to null
			sb = new JScrollBar(JScrollBar.HORIZONTAL);
			sb.addAdjustmentListener(new LocalAdjustmentListener(JScrollBar.HORIZONTAL));
			this.hScrollBar = sb;
		}
		return sb;
	}
 		
	private JScrollBar getVScrollBar() {
		JScrollBar sb = this.vScrollBar;
		if(sb == null) {
			// Should never go back to null
			sb = new JScrollBar(JScrollBar.VERTICAL);
			sb.addAdjustmentListener(new LocalAdjustmentListener(JScrollBar.VERTICAL));
			this.vScrollBar = sb;
		}
		return sb;
	}

	public final boolean couldBeScrollable() {
		int overflow = this.getOverflow();
		return overflow != OVERFLOW_NONE && (overflow == OVERFLOW_SCROLL || overflow == OVERFLOW_VERTICAL || overflow == OVERFLOW_AUTO);
	}	
	
	private Insets defaultPaddingInsets = null;
	
	public void setDefaultPaddingInsets(Insets insets) {
		this.defaultPaddingInsets = insets;
	}
	
	public void setDefaultMarginInsets(Insets insets) {
		this.defaultMarginInsets = insets;
	}

	public int getFirstLineHeight() {
		return this.bodyLayout.getFirstLineHeight();
	}

	public int getFirstBaselineOffset() {
		return this.bodyLayout.getFirstBaselineOffset();
	}

	public void setSelectionEnd(RenderableSpot rpoint) {
		this.endSelection = rpoint;
	}
	
	public void setSelectionStart(RenderableSpot rpoint) {
		this.startSelection = rpoint;
	}
	
	protected final Insets getPaddingInsets(RenderState rs) {
		Insets mi = rs.getPaddingInsets();
		if(mi == null) {
			return this.defaultPaddingInsets;
		}
		return mi;
	}

	public int getViewportListNesting(int blockNesting) {
		return blockNesting;
	}

	public void paint(Graphics g) {
		boolean linfo = loggableInfo;
		long time1 = linfo ? System.currentTimeMillis() : 0;
		this.prePaint(g);
		long time2 = linfo ? System.currentTimeMillis() : 0;
		long time3 = 0; 
		try {
			Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
			RBlockViewport bodyLayout = this.bodyLayout;
			if(bodyLayout != null) {
				int overflow = this.getOverflow();
				if(overflow == OVERFLOW_NONE || overflow == OVERFLOW_VISIBLE) {
					// Simply translate.
					int bx = bodyLayout.x;
					int by = bodyLayout.y;
					g.translate(bx, by);
					try {
						bodyLayout.paint(g);
					} finally {
						g.translate(-bx, -by);
					}
				}
				else {
					// Clip when there potential scrolling or hidden overflow was requested.
					Graphics newG = g.create(insets.left, insets.top, this.width - insets.left - insets.right, this.height - insets.top - insets.bottom);
					try {
						// Second, translate
						newG.translate(bodyLayout.x - insets.left, bodyLayout.y - insets.top);
						// Third, paint in clipped + translated region.
						bodyLayout.paint(newG);
					} finally {
						newG.dispose();
					}
				}

				if(linfo) {
					time3 = System.currentTimeMillis();
				}
			} else {
				// nop
			}

			// Paint FrameContext selection.
			// This is only done by root RBlock.

			RenderableSpot start = this.startSelection;
			RenderableSpot end = this.endSelection;
			boolean inSelection = false;
			if(start != null && end != null && !start.equals(end)) {
				this.paintSelection(g, inSelection, start, end);
			}
			// Must paint scrollbars too.
			JScrollBar hsb = this.hScrollBar;
			if(hsb != null) {
				Graphics sbg = g.create(insets.left, this.height - insets.bottom, this.width - insets.left - insets.right, SCROLL_BAR_THICKNESS);
				try {
					hsb.paint(sbg);
				} finally {
					sbg.dispose();
				}
			}
			JScrollBar vsb = this.vScrollBar;
			if(vsb != null) {
				Graphics sbg = g.create(this.width - insets.right, insets.top, SCROLL_BAR_THICKNESS, this.height - insets.top - insets.bottom);
				try {
					vsb.paint(sbg);
				} finally {
					sbg.dispose();
				}
			}

		} finally {
			// Must always call super implementation
			super.paint(g);
		}
		if(linfo) {
			long time4 = System.currentTimeMillis();
			if(time4 - time1 > 100) {
				logger.info("paint(): Elapsed: " + (time4 - time1) + " ms. Prepaint: " + (time2 - time1) + " ms. Viewport: " + (time3 - time2) + " ms. RBlock: " + this + ".");
			}
		}
	}
			
	/**
	 * @param width The width available, including insets.
	 * @param height The height available, including insets.
	 */
	protected Dimension doCellLayout(int width, int height, boolean expandWidth, boolean expandHeight) {
		try {
			this.doLayout(width, height, expandWidth, expandHeight);
			return new Dimension(this.width, this.height);
		} finally {
			this.layoutUpTreeCanBeInvalidated = true;
		}
	}
		
	public final void layout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight, int defaultOverflow) {
		this.layout(availWidth, availHeight, expandWidth, expandHeight, null, 0, defaultOverflow);
	}

	public final void layout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight, FloatingBounds floatBounds, int tentativeY) {
		this.layout(availWidth, availHeight, expandWidth, expandHeight, floatBounds, tentativeY, this.defaultOverflow);
	}

	public final void layout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight, FloatingBounds floatBounds, int tentativeY, int defaultOverflow) {
		try {
			this.doLayout(availWidth, availHeight, expandWidth, expandHeight, floatBounds, tentativeY, defaultOverflow);
		} finally {
			this.layoutUpTreeCanBeInvalidated = true;
			this.layoutDeepCanBeInvalidated = true;
//			this.renderStyleCanBeInvalidated = true;
		}
	}

	public final void doLayout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight) {
		this.doLayout(availWidth, availHeight, expandWidth, expandHeight, null, 0, this.defaultOverflow);
	}

	public final void doLayout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight, int tentativeY) {
		this.doLayout(availWidth, availHeight, expandWidth, expandHeight, null, tentativeY, this.defaultOverflow);
	}

	/**
	 * Lays out and sets dimensions only if RBlock is invalid (or never before
	 * layed out), if the parameters passed differ from the last layout, or
	 * if the current font differs from the font for the last layout.
	 * @param availWidth
	 * @param availHeight
	 */
	public void doLayout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight, FloatingBounds floatBounds, int tentativeY, int defaultOverflow) {
		// Expected to be invoked in the GUI thread.
		int prevAvailWidth = this.lastAvailWidth;
		int prevAvailHeight = this.lastAvailHeight;		
		RenderState renderState = this.modelNode.getRenderState();
		boolean forced = availHeight != prevAvailHeight || availWidth != prevAvailWidth || !Boolean.valueOf(expandWidth).equals(this.lastExpandWidth) || !Boolean.valueOf(expandHeight).equals(this.lastExpandHeight);
		if(!forced) {
			if(renderState != null) {
				Font font = renderState.getFont();
				if(!font.equals(this.lastFont)) {
					forced = true;
				}
				else {
					int newWhiteSpace = renderState.getWhiteSpace();
					if(newWhiteSpace != this.lastWhiteSpace) {
						forced = true;
					}
				}
			}
 			if(!forced) {
 				if(this.lastFloatBounds == INVALID_FLOAT_BOUNDS) {
					forced = true;
				}
				else {
					forced = !Objects.equals(this.lastFloatBounds, floatBounds);
				}
			}
		}
		if(forced) {
			this.forceLayout(renderState, availWidth, availHeight, expandWidth, expandHeight, floatBounds, tentativeY, defaultOverflow);
		}
		else {
			// nothing to do
		}
		
//		// Even if we didn't do layout, it is essential
//		// to add exported renderables in ancestors.
//		//TODO: Repace float mechanism with delayed pairs.
//		RBlockViewport localTarget = this.bodyLayout;
//		Iterator eri = localTarget.getExportedRenderables();
//		if(eri != null) {
//			while(eri.hasNext()) {
//				ExportedRenderable er = (ExportedRenderable) eri.next();
//				RBlockViewport parentViewport = localTarget.getParentViewport(er);
//				if(parentViewport == null) {
//					localTarget.importRenderable(er);
//				}
//				else if(parentViewport.isImportable(er)) {
//					parentViewport.importRenderable(er);
//				}
//				else {
//					// Must be scheduled at each level of the hierarchy, 
//					// otherwise it could be lost when layout is valid.
//					parentViewport.addToExportedRenderables(er);
//				}
//			}
//		}
		
		// Even if we didn't do layout, the parent is
		// expected to have removed its GUI components.
		this.sendGUIComponentsToParent();		
		
		// Even if we didn't do layout, the parent is
		// expected to have removed its delayed pairs.
		this.sendDelayedPairsToParent();
	}
	
	private final boolean correctViewportOrigin(Insets insets, int blockWidth, int blockHeight) {
		RBlockViewport bodyLayout = this.bodyLayout;
		int viewPortX = bodyLayout.x;
		int viewPortY = bodyLayout.y;
		boolean corrected = false;
		if(viewPortX > insets.left) {
			bodyLayout.x = insets.left;
			corrected = true;
		}
		else if(viewPortX < blockWidth - insets.right - bodyLayout.width) {
			bodyLayout.x = Math.min(insets.left, blockWidth - insets.right - bodyLayout.width);
			corrected = true;
		}
		if(viewPortY > insets.top) {
			bodyLayout.y = insets.top;
			corrected = true;
		}
		else if(viewPortY < blockHeight - insets.bottom - bodyLayout.height) {
			bodyLayout.y = Math.min(insets.top, blockHeight - insets.bottom - bodyLayout.height);
			corrected = true;
		}
		return corrected;
	}
	
	/**
	 * Lays out the block without checking for prior dimensions.
	 * @param availWidth
	 * @param availHeight
	 * @return
	 */
	private final void forceLayout(RenderState renderState, int availWidth, int availHeight, boolean expandWidth, boolean expandHeight, FloatingBounds floatBounds, int tentativeY, int defaultOverflow) {
		// Expected to be invoked in the GUI thread.
		//TODO: Not necessary to do full layout if only expandWidth or expandHeight change (specifically in tables).
		RenderState rs = renderState;
		if(rs == null) {
			rs = new BlockRenderState(null);
		}
		if(this.lastAvailWidth == -1) {
			// invalid or first time
			rs.invalidate();
			this.applyStyle();
		}

		int dw = this.getDeclaredWidth(rs, availWidth);
		int dh = this.getDeclaredHeight(rs, availHeight);
		this.lastExpandHeight = Boolean.valueOf(expandHeight);
		this.lastExpandWidth = Boolean.valueOf(expandWidth);
		Font newFont = rs.getFont();
		this.lastFont = newFont;		
		this.lastAvailHeight = availHeight;
		this.lastAvailWidth = availWidth;
		this.lastFloatBounds = floatBounds;
		this.lastWhiteSpace = rs.getWhiteSpace();
		
		RBlockViewport bodyLayout = this.bodyLayout;
		NodeImpl node = (NodeImpl) this.modelNode;
		if(node == null || bodyLayout == null) {
			Insets insets = this.getInsets(false, false);
			this.width = insets.left + insets.right;
			this.height = insets.bottom + insets.top;
			this.hasHScrollBar = false;
			this.hasVScrollBar = false;
			return;
		}

		// Remove all GUI components previously added by descendents
		// The RBlockViewport.layout() method is expected to add all of them back.
		this.clearGUIComponents();
		
		int tentativeWidth;
		int tentativeHeight;
		// Adjust dw and dh for margin
		if(dw != -1 || dh != -1) {
			Insets marginInsets = this.getMarginInsets(rs);
			if(marginInsets != null) {
				//TODO: Is this right?
				dw = dw == -1 ? -1 : dw + marginInsets.left + marginInsets.right;
				dh = dh == -1 ? -1 : dh + marginInsets.top + marginInsets.bottom;
			}
		}
		tentativeWidth = dw == -1 ? availWidth : dw;
		tentativeHeight = dh == -1 ? availHeight : dh;
		int overflow = this.getOverflow();
		if(overflow == OVERFLOW_NONE) {
			overflow = defaultOverflow;
		}
		boolean vertical = overflow == OVERFLOW_VERTICAL;
		boolean auto = vertical || overflow == OVERFLOW_AUTO;
		boolean bothScrollBars = overflow == OVERFLOW_SCROLL;
		boolean hscroll = bothScrollBars;
		boolean vscroll = bothScrollBars || vertical;
		boolean mayScroll = auto || bothScrollBars || vertical;
		Insets paddingInsets = this.getPaddingInsets(rs);
		if(paddingInsets == null) {
			paddingInsets = RBlockViewport.ZERO_INSETS;
		}
		Insets insets = null;
		for(int tries = ((auto && !vscroll) ? 0 : 1); tries < 2; tries++) {
			try {
				insets = this.getInsets(hscroll, vscroll);
				int maxY = tries == 0 ? (dh == -1 ? -1 : dh - insets.bottom - insets.top - paddingInsets.bottom) : -1;
				int desiredViewportWidth = tentativeWidth - insets.left - insets.right; 
				int desiredViewportHeight = tentativeHeight - insets.top - insets.bottom;
				bodyLayout.layout(desiredViewportWidth, desiredViewportHeight, paddingInsets, node, maxY, floatBounds);
				break;
			} catch(SizeExceededException hee) {
				if(tries != 0) {
					throw new IllegalStateException("tries=" + tries + ",auto=" + auto);
				}
				vscroll = true;
			}
		}
		this.hasVScrollBar = vscroll;
		Dimension size = bodyLayout.getSize();
		Dimension rblockSize = new Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom);
		if(auto && !hscroll && ((dw != -1 && rblockSize.width > dw) || (rblockSize.width > availWidth))) {
			hscroll = true;
			insets = this.getInsets(hscroll, vscroll);
			rblockSize = new Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom);
		}		
		this.hasHScrollBar = hscroll;
		boolean visible = !auto && !bothScrollBars && (overflow != OVERFLOW_HIDDEN);
		int resultingWidth;
		int resultingHeight;
		if(dw == -1) {
			resultingWidth = rblockSize.width;
			if(expandWidth && resultingWidth < availWidth) {
				resultingWidth = availWidth;
			}
			else if(hscroll && resultingWidth > availWidth) {
				resultingWidth = Math.max(availWidth, SCROLL_BAR_THICKNESS);
			}
		}
		else {
			resultingWidth = visible ? Math.max(rblockSize.width, dw) : dw;
		}
		if(dh == -1) {
			resultingHeight = rblockSize.height;
			if(expandHeight && resultingHeight < availHeight) {
				resultingHeight = availHeight;
			}
			else if(vscroll && resultingHeight > availHeight) {
				resultingHeight = Math.max(availHeight, SCROLL_BAR_THICKNESS);
			}
		}
		else {
			resultingHeight = visible ? Math.max(rblockSize.height, dh) : dh;
		}
		if(vscroll) {
			JScrollBar sb = this.getVScrollBar();
			this.add(sb);
			// Bounds set by updateWidgetBounds
		}
		if(hscroll) {
			JScrollBar sb = this.getHScrollBar();
			this.add(sb);
			// Bounds set by updateWidgetBounds
		}
				
		this.width = resultingWidth;
		this.height = resultingHeight;
		
		// Align viewport
		int alignmentXPercent = rs.getAlignXPercent();
		int alignmentYPercent = rs.getAlignYPercent();
		if(alignmentXPercent > 0 || alignmentYPercent > 0) {
			//TODO: alignment should not be done in table cell sizing determination.
			int canvasWidth = Math.max(bodyLayout.width, resultingWidth - insets.left - insets.right);
			int canvasHeight = Math.max(bodyLayout.height, resultingHeight - insets.top - insets.bottom);
			bodyLayout.align(alignmentXPercent, alignmentYPercent, canvasWidth, canvasHeight, paddingInsets);
		}
		if(hscroll || vscroll) {
			// In this case, viewport origin should not be changed.
			// We don't want to cause the document to scroll back
			// up while rendering.			
			this.correctViewportOrigin(insets, resultingWidth, resultingHeight);
			// Depends on width, height and origin
			this.resetScrollBars(rs);
		}
		else {
			bodyLayout.x = insets.left;
			bodyLayout.y = insets.top;			
		}		
	}

	private int getVUnitIncrement(RenderState renderState) {
		if(renderState != null) {
			return renderState.getFontMetrics().getHeight();
		}
		else {
			return new BlockRenderState(null).getFontMetrics().getHeight();
		}
	}
	
	private boolean resettingScrollBars = false;
	
	/**
	 * Changes scroll bar state to match viewport origin.
	 */
	private void resetScrollBars(RenderState renderState) {
		// Expected to be called only in the GUI thread.
		this.resettingScrollBars = true;
		try {
			RBlockViewport bodyLayout = this.bodyLayout;
			if(bodyLayout != null) {
				Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
				JScrollBar vsb = this.vScrollBar;
				if(vsb != null) {
					int newValue = insets.top - bodyLayout.y;
					int newExtent = this.height - insets.top - insets.bottom;
					int newMin = 0;
					int newMax = bodyLayout.height;
					vsb.setValues(newValue, newExtent, newMin, newMax);
					vsb.setUnitIncrement(this.getVUnitIncrement(renderState));
					vsb.setBlockIncrement(newExtent);
				}
				JScrollBar hsb = this.hScrollBar;
				if(hsb != null) {
					int newValue = insets.left - bodyLayout.x;
					int newExtent = this.width - insets.left - insets.right;
					int newMin = 0;
					int newMax = bodyLayout.width;
					hsb.setValues(newValue, newExtent, newMin, newMax);	
				}			
			}
		} finally {
			this.resettingScrollBars = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.UIControl#paintSelection(java.awt.Graphics, boolean, org.xamjwg.html.renderer.RenderablePoint, org.xamjwg.html.renderer.RenderablePoint)
	 */
	public boolean paintSelection(Graphics g, boolean inSelection, RenderableSpot startPoint, RenderableSpot endPoint) {
		Graphics newG = g.create();
		try {
			Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
			// Just clip, don't translate.
			newG.clipRect(insets.left, insets.top, this.width - insets.left - insets.right, this.height - insets.top - insets.bottom);
			return super.paintSelection(newG, inSelection, startPoint, endPoint);
		} finally {
			newG.dispose();
		}
//		boolean endSelectionLater = false;
//		if(inSelection) {
//			if(startPoint.renderable == this || endPoint.renderable == this) {
//				return false;
//			}
//		}
//		else {
//			if(startPoint.renderable == this || endPoint.renderable == this) {
//				// This can only occur if the selection point
//				// is on the margin or border or the block.
//				inSelection = true;
//				if(startPoint.renderable == this && endPoint.renderable == this) {
//					// Start and end selection points on margin or border.
//					endSelectionLater = true;
//				}
//			}
//		}
//		RBlockViewport bodyLayout = this.bodyLayout;
//		if(bodyLayout != null) {
//			Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
//			Graphics newG = g.create(insets.left, insets.top, this.width - insets.left - insets.right, this.height - insets.top - insets.bottom);
//			try {
//				newG.translate(bodyLayout.x - insets.left, bodyLayout.y - insets.top);
//				boolean newInSelection = bodyLayout.paintSelection(newG, inSelection, startPoint, endPoint);
//				if(endSelectionLater) {
//					return false;
//				}
//				return newInSelection;
//			} finally {
//				newG.dispose();
//			}
//		}
//		else {
//			return inSelection;
//		}
	}
		
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#getRenderablePoint(int, int)
	 */
	public RenderableSpot getLowestRenderableSpot(int x, int y) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
			if(x > insets.left && x < this.width - insets.right && y > insets.top && y < this.height - insets.bottom) {
				return bodyLayout.getLowestRenderableSpot(x - bodyLayout.x, y - bodyLayout.y);
			}
			else {
				return new RenderableSpot(this, x, y);
			}
		}
		else {
	    	return new RenderableSpot(this, x, y);
	    }
	}
	
	/**
	 * RBlocks should only be invalidated if one of their
	 * properties change, or if a descendent changes, or
	 * if a style property of an ancestor is such that
	 * it could produce layout changes in this RBlock.
	 */
	public void invalidateLayoutLocal() {
		super.invalidateLayoutLocal();
		this.lastAvailHeight = -1;
		this.lastAvailWidth = -1;
		this.lastWhiteSpace = -1;
		this.lastExpandHeight = null;
		this.lastExpandWidth = null;
		this.lastFloatBounds = INVALID_FLOAT_BOUNDS;
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseClick(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMouseClick(MouseEvent event, int x, int y) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			if(!bodyLayout.onMouseClick(event, x - bodyLayout.x, y - bodyLayout.y)) {
				return false;
			}
		}
		if(!HtmlController.getInstance().onMouseClick(this.modelNode, event, x, y)) {
			return false;
		}
		if(this.backgroundColor != null) {
			return false;
		}
		return true;
	}

	public boolean onDoubleClick(MouseEvent event, int x, int y) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			if(!bodyLayout.onDoubleClick(event, x - bodyLayout.x, y - bodyLayout.y)) {
				return false;
			}
		}
		if(this.backgroundColor != null) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseDisarmed(java.awt.event.MouseEvent)
	 */
	public boolean onMouseDisarmed(MouseEvent event) {
		BoundableRenderable br = this.armedRenderable;
		if(br != null) {
			try {
				return br.onMouseDisarmed(event);
			} finally {
				this.armedRenderable = null;
			}
		}
		else {
			return true;
		}
	}

	private BoundableRenderable armedRenderable;
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMousePressed(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMousePressed(MouseEvent event, int x, int y) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			int newX = x - bodyLayout.x;
			int newY = y - bodyLayout.y;
			if(bodyLayout.contains(newX, newY)) {
				this.armedRenderable = bodyLayout;
				if(!bodyLayout.onMousePressed(event, newX, newY)) {
					return false;
				}
			}
			else {
				this.armedRenderable = null;
			}
		}
		else {
			this.armedRenderable = null;
		}
		if(!HtmlController.getInstance().onMouseDown(this.modelNode, event, x, y)) {
			return false;
		}
		if(this.backgroundColor != null) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseReleased(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMouseReleased(MouseEvent event, int x, int y) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			int newX = x - bodyLayout.x;
			int newY = y - bodyLayout.y;
			if(bodyLayout.contains(newX, newY)) {
				this.armedRenderable = null;
				if(!bodyLayout.onMouseReleased(event, newX, newY)) {
					return false;
				}
			}
			else {
				BoundableRenderable br = this.armedRenderable;
				if(br != null) {
					br.onMouseDisarmed(event);
				}
			}
		}
		if(!HtmlController.getInstance().onMouseUp(this.modelNode, event, x, y)) {
			return false;
		}
		if(this.backgroundColor != null) {
			return false;
		}
		return true;
	}

	public Color getPaintedBackgroundColor() {
		return this.backgroundColor;
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.RCollection#getRenderables()
	 */
	public Iterator getRenderables() {
		final RBlockViewport bodyLayout = this.bodyLayout;
		return new Iterator() {
			private RBlockViewport bl = bodyLayout;
			
			public boolean hasNext() {
				return bl != null;
			}

			public Object next() {
				if(bl == null) {
					throw new NoSuchElementException();
				}
				try {
					return bl;
				} finally {
					bl = null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.domimpl.ContainingBlockContext#repaint(org.xamjwg.html.domimpl.RenderableContext)
	 */
	public void repaint(ModelNode modelNode) {
		//this.invalidateRenderStyle();
		this.repaint();
	}

//	public boolean extractSelectionText(StringBuffer buffer, boolean inSelection, RenderableSpot startPoint, RenderableSpot endPoint) {
//		RBlockViewport bodyLayout = this.bodyLayout;
//		if(bodyLayout != null) {
//			inSelection = inSelection ? endPoint.renderable != this : startPoint.renderable == this;
//			return bodyLayout.extractSelectionText(buffer, inSelection, startPoint, endPoint);
//		}
//		else {
//			return inSelection;
//		}
//	}

	public void updateWidgetBounds(int guiX, int guiY) {
		super.updateWidgetBounds(guiX, guiY);
		boolean hscroll = this.hasHScrollBar;
		boolean vscroll = this.hasVScrollBar;
		if(hscroll || vscroll) {
			Insets insets = this.getInsets(hscroll, vscroll);
			if(hscroll) {
				JScrollBar hsb = this.hScrollBar;
				if(hsb != null) {
					hsb.setBounds(guiX + insets.left, guiY + this.height - insets.bottom, this.width - insets.left - insets.right, SCROLL_BAR_THICKNESS);
				}
			}
			if(vscroll) {
				JScrollBar vsb = this.vScrollBar;
				if(vsb != null) {
					vsb.setBounds(guiX + this.width - insets.right, guiY + insets.top, SCROLL_BAR_THICKNESS, this.height - insets.top - insets.bottom);
				}
			}
		}
	}
	
	public void scrollHorizontalTo(int newX) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
			int viewPortX = newX;
			if(viewPortX > insets.left) {
				bodyLayout.x = insets.left;
			}
			else if(viewPortX < this.width - insets.right - bodyLayout.width) {
				bodyLayout.x = Math.min(insets.left, this.width - insets.right - bodyLayout.width);
			}
			else {
				bodyLayout.x = viewPortX;
			}
			this.resetScrollBars(null);
			this.updateWidgetBounds();
			this.repaint();			
		}
	}
	
	public void scrollVerticalTo(int newY) {
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
			int viewPortY = newY;
			if(viewPortY > insets.top) {
				bodyLayout.y = insets.top;
			}
			else if(viewPortY < this.height - insets.bottom - bodyLayout.height) {
				bodyLayout.y = Math.min(insets.top, this.height - insets.bottom - bodyLayout.height);
			}
			else {
				bodyLayout.y = viewPortY;
			}
			this.resetScrollBars(null);
			this.updateWidgetBounds();
			this.repaint();			
		}
	}

	public void scrollByUnits(int orientation, int units) {		
		int offset = orientation == JScrollBar.VERTICAL ? this.getVUnitIncrement(null) * units : units;
		this.scrollBy(orientation, offset);
	}

	public void scrollBy(int orientation, int offset) {		
		RBlockViewport bodyLayout = this.bodyLayout;
		if(bodyLayout != null) {
			switch(orientation) {
			case JScrollBar.HORIZONTAL:
				this.scrollHorizontalTo(bodyLayout.x - offset);
				break;
			case JScrollBar.VERTICAL:
				this.scrollVerticalTo(bodyLayout.y - offset);
				break;
			}
		}
	}
	
	private void scrollToSBValue(int orientation, int value) {
		Insets insets = this.getInsets(this.hasHScrollBar, this.hasVScrollBar);
		switch(orientation) {
		case JScrollBar.HORIZONTAL:
			int xOrigin = insets.left - value;
			this.scrollHorizontalTo(xOrigin);
			break;
		case JScrollBar.VERTICAL:
			int yOrigin = insets.top - value;
			this.scrollVerticalTo(yOrigin);
			break;
		}		
	}
	
//	public Iterator getExportedRenderables() {
//		RBlockViewport bodyLayout = this.bodyLayout;
//		return bodyLayout == null ? null : bodyLayout.getExportedRenderables();
//	}
//
	public RBlockViewport getRBlockViewport() {
		return this.bodyLayout;
	}
	
	public String toString() {
		return "RBlock[node=" + this.modelNode + "]";
	}
	
	private class LocalAdjustmentListener implements AdjustmentListener {
		private final int orientation;
		
		public LocalAdjustmentListener(int orientation) {
			this.orientation = orientation;
		}
		
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if(RBlock.this.resettingScrollBars) {
				return;
			}
			switch(e.getAdjustmentType()) {
			case AdjustmentEvent.UNIT_INCREMENT:
				// fall through
			case AdjustmentEvent.UNIT_DECREMENT: 
				// fall through
			case AdjustmentEvent.BLOCK_INCREMENT: 
				// fall through
			case AdjustmentEvent.BLOCK_DECREMENT: 
				// fall through
			case AdjustmentEvent.TRACK: {
				int value = e.getValue();
				RBlock.this.scrollToSBValue(this.orientation, value);
				break;
			}
			}
		}
	}
	
	private static class BodyFilter implements NodeFilter {
		public boolean accept(Node node) {
			return node instanceof org.w3c.dom.html2.HTMLBodyElement;
		}
	}
}