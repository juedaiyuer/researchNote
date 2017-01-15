package org.lobobrowser.html.renderer;

import java.awt.Graphics;

abstract class BaseRenderable implements Renderable {
	private int ordinal = 0;
	
	public int getOrdinal() {
		return this.ordinal;
	}

	public int getZIndex() {
		return 0;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
}
