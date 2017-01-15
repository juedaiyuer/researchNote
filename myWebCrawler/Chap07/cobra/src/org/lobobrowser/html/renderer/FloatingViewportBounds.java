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

class FloatingViewportBounds implements FloatingBounds {
	private final FloatingBounds prevBounds;
	private final int alignment;
	private final int y;
	private final int width;
	private final int height;
	
	public FloatingViewportBounds(FloatingBounds prevBounds, int alignment, int y, int width, int height) {
		this.prevBounds = prevBounds;
		this.alignment = alignment;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public int getLeft(int y) {
		int left = 0;
		FloatingBounds prev = this.prevBounds;
		if(prev != null) {
			left = prev.getLeft(y);
		}
		if(this.alignment < 0 && y >= this.y && y < this.y + height) {
			left += this.width;
		}
		return left;
	}
	
	/**
	 * The offset from the right edge, not counting padding.
	 */
	public int getRight(int y) {
		int right = 0;
		FloatingBounds prev = this.prevBounds;
		if(prev != null) {
			right = prev.getRight(y);
		}
		if(this.alignment > 0 && y >= this.y && y < this.y + this.height) {
			right += this.width;
		}
		return right;		
	}
	
	public int getClearY(int y) {
		int cleary = Math.max(y, this.y + this.height);
		FloatingBounds prev = this.prevBounds;
		if(prev != null) {
			int pcy = prev.getClearY(y);
			if(pcy > cleary) {
				cleary = pcy;
			}
		}
		return cleary;
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof FloatingViewportBounds)) {
			return false;
		}
		FloatingViewportBounds olm = (FloatingViewportBounds) other;
		return olm.alignment == this.alignment &&
				olm.y == this.y &&
				olm.height == this.height &&
				olm.width == this.width &&
				org.lobobrowser.util.Objects.equals(olm.prevBounds, this.prevBounds);
	}
}
