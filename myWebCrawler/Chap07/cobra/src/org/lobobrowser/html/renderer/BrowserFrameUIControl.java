package org.lobobrowser.html.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import org.lobobrowser.html.*;
import org.lobobrowser.html.style.HtmlValues;
import org.w3c.dom.html2.*;

class BrowserFrameUIControl implements UIControl {
	//private final BrowserFrame browserFrame;
	private final Component component;
	private final HTMLElement element;
	
	public BrowserFrameUIControl(HTMLElement element, BrowserFrame browserFrame) {
		this.component = browserFrame.getComponent();
		this.element = element;
	}
	
	public int getVAlign() {
		return RElement.VALIGN_BASELINE;
	}
	
	public float getAlignmentY() {
		return 0;
	}

	public Color getBackgroundColor() {
		return this.component.getBackground();
	}

	public Component getComponent() {
		return this.component;
	}

	private int availWidth;
	private int availHeight;
	
	public void reset(int availWidth, int availHeight) {
		this.availWidth = availWidth;
		this.availHeight = availHeight;
	}
	
	public Dimension getPreferredSize() {
		int width = HtmlValues.getOldSyntaxPixelSize(element.getAttribute("width"), this.availWidth, 100);
		int height = HtmlValues.getOldSyntaxPixelSize(element.getAttribute("height"), this.availHeight, 100);
		return new Dimension(width, height);
	}

	public void invalidate() {
		this.component.invalidate();
	}

	public void paint(Graphics g) {
		// We actually have to paint it.
		this.component.paint(g);
	}

	public boolean paintSelection(Graphics g, boolean inSelection,
			RenderableSpot startPoint, RenderableSpot endPoint) {
		// Selection does not cross in here?
		return false;
	}

	public void setBounds(int x, int y, int width, int height) {
		this.component.setBounds(x, y, width, height);
	}

	public void setRUIControl(RUIControl ruicontrol) {
		//this.ruiControl = ruicontrol;
	}
}
