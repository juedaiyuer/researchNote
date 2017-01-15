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
 * Created on Nov 19, 2005
 */
package org.lobobrowser.html.renderer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.*;

class RTable extends BaseElementRenderable {
	private final TableMatrix tableMatrix;
	private SortedSet positionedRenderables;
	private int otherOrdinal;
	
	public RTable(HTMLElementImpl modelNode, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext, RenderableContainer container) {
		super(container, modelNode, pcontext);
		this.tableMatrix = new TableMatrix(modelNode, pcontext, rcontext, frameContext, this, this);
	}

	public int getVAlign() {
		// Not used
		return VALIGN_BASELINE;
	}

	protected void applyStyle() {
		super.applyStyle();
		Insets bi = this.borderInsets;
		if(bi == null) {
			HTMLElementImpl element = (HTMLElementImpl) this.modelNode;
			String borderText = element.getAttribute("border");
			int border = 0;
			if(borderText != null) {
				try {
					border = Integer.parseInt(borderText);
					if(border < 0) {
						border = 0;
					}
				} catch(NumberFormatException nfe) {
					// ignore
				}
			}
			if(border > 0) {
				this.borderInsets = new Insets(border, border, border, border);
			}
		}
		if(this.borderTopColor == null && this.borderLeftColor == null) {
			this.borderTopColor = this.borderLeftColor = Color.LIGHT_GRAY;
		}
		if(this.borderBottomColor == null && this.borderRightColor == null) {
			this.borderBottomColor = this.borderRightColor = Color.DARK_GRAY;
		}
	}

	public void paint(Graphics g) {
		try {
			this.prePaint(g);
			Dimension size = this.getSize();
			//TODO: No scrollbars
			TableMatrix tm = this.tableMatrix;
			tm.paint(g, size);
			Collection prs = this.positionedRenderables;
			if(prs != null) {
				Iterator i = prs.iterator();
				while(i.hasNext()) {
					PositionedRenderable pr = (PositionedRenderable) i.next();
					BoundableRenderable r = pr.renderable;					
					r.paintTranslated(g);
				}
			}
		} finally {
			// Must always call super implementation
			super.paint(g);
		}
	}
	
	private volatile int lastAvailWidth = -1;
	private volatile int lastAvailHeight = -1;
//	private volatile Dimension layoutSize = null;
	
	public void doLayout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight) {
		if (availWidth != this.lastAvailWidth || availHeight != this.lastAvailHeight) {
			Collection prs = this.positionedRenderables;
			if(prs != null) {
				prs.clear();
			}
			this.otherOrdinal = 0;
			this.clearGUIComponents();
			this.clearDelayedPairs();
			this.applyStyle();
			this.lastAvailHeight = availHeight;
			this.lastAvailWidth = availWidth;
			TableMatrix tm = this.tableMatrix;
			Insets insets = this.getInsets(false, false);
			tm.reset(insets, availWidth, availHeight);
			//TODO: No scrollbars
			tm.build(availWidth, availHeight);
			tm.doLayout(insets);
			this.width = tm.getTableWidth();
			this.height = tm.getTableHeight();
			
			// Import applicable delayed pairs.
			// Only needs to be done if layout was
			// forced. Otherwise, they should've
			// been imported already.
			Collection pairs = this.delayedPairs;
			if(pairs != null) {
				Iterator i = pairs.iterator();
				while(i.hasNext()) {
					DelayedPair pair = (DelayedPair) i.next();
					if(pair.targetParent == this) {
						this.importDelayedPair(pair);
					}
				}
			}
			
		} else {
			// Nothing to do - dimensions already set.
		}
		this.sendGUIComponentsToParent();
		this.sendDelayedPairsToParent();
	}
	
//	/* (non-Javadoc)
//	 * @see org.xamjwg.html.renderer.UIControl#paintSelection(java.awt.Graphics, boolean, org.xamjwg.html.renderer.RenderablePoint, org.xamjwg.html.renderer.RenderablePoint)
//	 */
//	public boolean paintSelection(Graphics g, boolean inSelection, RenderableSpot startPoint, RenderableSpot endPoint) {
//		return this.tableMatrix.paintSelection(g, inSelection, startPoint, endPoint);
//	}
//
//	public boolean extractSelectionText(StringBuffer buffer, boolean inSelection, RenderableSpot startPoint, RenderableSpot endPoint) {
//		return this.tableMatrix.extractSelectionText(buffer, inSelection, startPoint, endPoint);
//	}
	
	public void invalidateLayoutLocal() {
		super.invalidateLayoutLocal();
		this.lastAvailHeight = -1;
		this.lastAvailWidth = -1;
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#getRenderablePoint(int, int)
	 */
	public RenderableSpot getLowestRenderableSpot(int x, int y) {
		Collection prs = this.positionedRenderables;
		if(prs != null) {
			Iterator i = prs.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				BoundableRenderable r = pr.renderable;
				int childX = x - r.getX();
				int childY = y - r.getY();
				RenderableSpot rs = r.getLowestRenderableSpot(childX, childY);
				if(rs != null) {
					return rs;
				}
			}
		}
		RenderableSpot rs = this.tableMatrix.getLowestRenderableSpot(x, y);
		if(rs != null) {
			return rs;
		}
		return new RenderableSpot(this, x, y);
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseClick(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMouseClick(MouseEvent event, int x, int y) {
		Collection prs = this.positionedRenderables;
		if(prs != null) {
			Iterator i = prs.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				BoundableRenderable r = pr.renderable;
				Rectangle bounds = r.getBounds();
				if(bounds.contains(x, y)) {
					int childX = x - r.getX();
					int childY = y - r.getY();
					if(!r.onMouseClick(event, childX, childY)) {
						return false;
					}
				}
			}
		}
		return this.tableMatrix.onMouseClick(event, x, y);
	}

	public boolean onDoubleClick(MouseEvent event, int x, int y) {
		Collection prs = this.positionedRenderables;
		if(prs != null) {
			Iterator i = prs.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				BoundableRenderable r = pr.renderable;
				Rectangle bounds = r.getBounds();
				if(bounds.contains(x, y)) {
					int childX = x - r.getX();
					int childY = y - r.getY();
					if(!r.onDoubleClick(event, childX, childY)) {
						return false;
					}
				}
			}
		}
		return this.tableMatrix.onDoubleClick(event, x, y);
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseDisarmed(java.awt.event.MouseEvent)
	 */
	public boolean onMouseDisarmed(MouseEvent event) {
		return this.tableMatrix.onMouseDisarmed(event);
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMousePressed(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMousePressed(MouseEvent event, int x, int y) {
		Collection prs = this.positionedRenderables;
		if(prs != null) {
			Iterator i = prs.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				BoundableRenderable r = pr.renderable;
				Rectangle bounds = r.getBounds();
				if(bounds.contains(x, y)) {
					int childX = x - r.getX();
					int childY = y - r.getY();
					if(!r.onMousePressed(event, childX, childY)) {
						return false;
					}
				}
			}
		}
		return this.tableMatrix.onMousePressed(event, x, y);
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseReleased(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMouseReleased(MouseEvent event, int x, int y) {
		Collection prs = this.positionedRenderables;
		if(prs != null) {
			Iterator i = prs.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				BoundableRenderable r = pr.renderable;
				Rectangle bounds = r.getBounds();
				if(bounds.contains(x, y)) {
					int childX = x - r.getX();
					int childY = y - r.getY();
					if(!r.onMouseReleased(event, childX, childY)) {
						return false;
					}
				}
			}
		}
		return this.tableMatrix.onMouseReleased(event, x, y);
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.RCollection#getRenderables()
	 */
	public Iterator getRenderables() {
		Collection prs = this.positionedRenderables;
		if(prs != null) {
			Collection c = new java.util.LinkedList();
			Iterator i = prs.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				BoundableRenderable r = pr.renderable;
				c.add(r);
			}
			Iterator i2 = this.tableMatrix.getRenderables();
			while(i2.hasNext()) {
				c.add(i2.next());
			}
			return c.iterator();
		}
		else {
			return this.tableMatrix.getRenderables();
		}
	}

	public void repaint(ModelNode modelNode) {
		//NOP
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.RenderableContainer#getBackground()
	 */
	public Color getPaintedBackgroundColor() {
		return this.container.getPaintedBackgroundColor();
	}
	
	private final void addPositionedRenderable(BoundableRenderable renderable, boolean verticalAlignable) {
		// Expected to be called only in GUI thread.
		SortedSet others = this.positionedRenderables;
		if(others == null) {
			others = new TreeSet(new ZIndexComparator());
			this.positionedRenderables = others;
		}
		others.add(new PositionedRenderable(renderable, verticalAlignable, this.otherOrdinal++));
		renderable.setParent(this);
		if(renderable instanceof RUIControl) {
			this.container.add(((RUIControl) renderable).widget.getComponent());
		}
	}

	private void importDelayedPair(DelayedPair pair) {
		BoundableRenderable r = pair.child;
		r.setOrigin(pair.x, pair.y);
		this.addPositionedRenderable(r, false);
	}
	
	public String toString() {
		return "RTable[this=" + System.identityHashCode(this) + ",node=" + this.modelNode + "]";
	}
}
