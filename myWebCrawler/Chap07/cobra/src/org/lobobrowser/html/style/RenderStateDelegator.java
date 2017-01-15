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
package org.lobobrowser.html.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;


public abstract class RenderStateDelegator implements RenderState {
	protected final RenderState prevRenderState;

	public RenderStateDelegator(final RenderState prevRenderState) {
		super();
		this.prevRenderState = prevRenderState;
	}

	public RenderState getPreviousRenderState() {
		return this.prevRenderState;
	}

	public int getAlignXPercent() {
		return prevRenderState.getAlignXPercent();
	}

	public int getAlignYPercent() {
		return prevRenderState.getAlignYPercent();
	}

	public int getBlankWidth() {
		return prevRenderState.getBlankWidth();
	}

	public Color getColor() {
		return prevRenderState.getColor();
	}

	public Font getFont() {
		return prevRenderState.getFont();
	}

	public int getFontBase() {
		return prevRenderState.getFontBase();
	}

	public FontMetrics getFontMetrics() {
		return prevRenderState.getFontMetrics();
	}

	public Color getOverlayColor() {
		return prevRenderState.getOverlayColor();
	}

	public Color getBackgroundColor() {
		return prevRenderState.getBackgroundColor();
	}

	public int getTextDecorationMask() {
		return prevRenderState.getTextDecorationMask();
	}

	public WordInfo getWordInfo(String word) {
		return prevRenderState.getWordInfo(word);
	}

	public void invalidate() {
		prevRenderState.invalidate();
	}

	public boolean isHighlight() {
		return prevRenderState.isHighlight();
	}

	public void setHighlight(boolean highlight) {
		prevRenderState.setHighlight(highlight);
	}

	public int getCount(String counter, int nesting) {
		return this.prevRenderState.getCount(counter, nesting);
	}

	public void resetCount(String counter, int nesting, int value) {
		this.prevRenderState.resetCount(counter, nesting, value);
	}

	public int incrementCount(String counter, int nesting) {
		return this.prevRenderState.incrementCount(counter, nesting);
	}

	public BackgroundInfo getBackgroundInfo() {
		return this.prevRenderState.getBackgroundInfo();
	}

	public int getDisplay() {
		return this.prevRenderState.getDisplay();
	}

	public Color getTextBackgroundColor() {
		return this.prevRenderState.getTextBackgroundColor();
	}

	public int getTextIndent(int availWidth) {
		return this.prevRenderState.getTextIndent(availWidth);
	}

	public String getTextIndentText() {
		return this.prevRenderState.getTextIndentText();
	}

	public int getWhiteSpace() {
		return this.prevRenderState.getWhiteSpace();
	}

	public Insets getMarginInsets() {
		return this.prevRenderState.getMarginInsets();
	}

	public Insets getPaddingInsets() {
		return this.prevRenderState.getPaddingInsets();
	}
}
