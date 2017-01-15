package org.lobobrowser.html.js;

import java.awt.*;

import org.lobobrowser.js.*;

public class Screen extends AbstractScriptableDelegate {
	private final GraphicsEnvironment graphicsEnvironment;
	private final GraphicsDevice graphicsDevice;
	
	/**
	 * @param context
	 */
	public Screen() {
		super();
		this.graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.graphicsDevice = this.graphicsEnvironment.getDefaultScreenDevice();
	}

	public int getHeight() {
		return this.graphicsDevice.getDisplayMode().getHeight();
	}

	public int getPixelDepth() {
		return this.getColorDepth();
	}

	public int getWidth() {
		GraphicsDevice gd = this.graphicsEnvironment.getDefaultScreenDevice();
		return gd.getDisplayMode().getWidth();
	}

	public int getAvailHeight() {
		return this.graphicsEnvironment.getMaximumWindowBounds().height;
	}

	public int getAvailWidth() {
		return this.graphicsEnvironment.getMaximumWindowBounds().width;
	}

	public int getColorDepth() {
		return this.graphicsDevice.getDisplayMode().getBitDepth();
	}
}
