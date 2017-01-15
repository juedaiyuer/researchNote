/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The XAMJ Project

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
package org.lobobrowser.html.renderer;

import java.awt.*;

class OffsetFloatingBounds implements FloatingBounds {
	private final FloatingBounds ancestorFloatingBounds;
	private final RBlockViewport ancestor;
	private final RBlockViewport target;
	private final Insets ancestorPadding;
	private final Insets targetPadding;
	
	public OffsetFloatingBounds(final FloatingBounds ancestorFloatingBounds, final RBlockViewport ancestor, Insets ancestorPadding, final RBlockViewport target, Insets targetPadding) {
		this.ancestorFloatingBounds = ancestorFloatingBounds;
		this.ancestor = ancestor;
		this.target = target;
		this.ancestorPadding = ancestorPadding;
		this.targetPadding = targetPadding;
	}

	public int getClearY(int y) {
		int ancestorY = this.translateToAncestorY(y);
		int ancestorClearY = this.ancestorFloatingBounds.getClearY(ancestorY);
		return this.translateAncestorY(ancestorClearY);
	}

	public int getLeft(int y) {
		int ancestorY = this.translateToAncestorY(y);
		Insets ap = this.ancestorPadding;
		int aleft = ap == null ? 0 : ap.left;
		int ancestorX = this.ancestorFloatingBounds.getLeft(ancestorY) + aleft;
		Insets tp = this.targetPadding;
		int tleft = tp == null ? 0 : tp.left;
		int localX = this.translateAncestorX(ancestorX) - tleft;		
		return localX > 0 ? localX : 0;
	}

	public int getRight(int y) {
		int ancestorY = this.translateToAncestorY(y);
		Insets ancestorPadding = this.ancestorPadding;
		Insets targetPadding = this.targetPadding;
		int aleft = ancestorPadding == null ? 0 : ancestorPadding.left;
		int tleft = targetPadding == null ? 0 : targetPadding.left;
		int ancestorX = aleft + this.ancestor.getAvailContentWidth() - this.ancestorFloatingBounds.getRight(ancestorY);
		int right = tleft + this.target.getAvailContentWidth() - this.translateAncestorX(ancestorX);
		return right > 0 ? right : 0;
	}
	
	private final int translateToAncestorY(int y) {
		//OPTIMIZE: It could be assumed that the offset is always the same?
		RBlockViewport ancestor = this.ancestor;
		BoundableRenderable current = this.target;
		while(current != null && current != ancestor) {
			y += current.getY();
			current = current.getParent();
		}
		return y;
	}

	private final int translateAncestorY(int y) {
		//OPTIMIZE: It could be assumed that the offset is always the same?
		RBlockViewport ancestor = this.ancestor;
		BoundableRenderable current = this.target;
		while(current != null && current != ancestor) {
			y -= current.getY();
			current = current.getParent();
		}
		return y;
	}

	private final int translateAncestorX(int x) {
		//OPTIMIZE: It could be assumed that the offset is always the same?
		RBlockViewport ancestor = this.ancestor;
		BoundableRenderable current = this.target;
		while(current != null && current != ancestor) {
			x -= current.getX();
			current = current.getParent();
		}
		return x;
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof OffsetFloatingBounds)) {
			return false;
		}
		OffsetFloatingBounds ofb = (OffsetFloatingBounds) other;
		return ofb.ancestor == this.ancestor &&
			ofb.target == this.target &&
			org.lobobrowser.util.Objects.equals(ofb.ancestorFloatingBounds, this.ancestorFloatingBounds);
	}
}
