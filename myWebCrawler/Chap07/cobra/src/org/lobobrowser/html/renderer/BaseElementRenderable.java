/*    GNU LESSER GENERAL PUBLIC LICENSE
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

package org.lobobrowser.html.renderer;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

import org.lobobrowser.util.gui.*;
import org.lobobrowser.html.HttpRequest;
import org.lobobrowser.html.ReadyStateChangeListener;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.html.style.BackgroundInfo;
import org.lobobrowser.html.style.CSS2PropertiesImpl;
import org.lobobrowser.html.style.HtmlValues;
import org.lobobrowser.html.style.RenderState;
import org.lobobrowser.util.gui.ColorFactory;
import org.w3c.dom.css.CSS2Properties;

abstract class BaseElementRenderable extends BaseRCollection implements RElement, RenderableContainer, java.awt.image.ImageObserver {
	public static final int OVERFLOW_NONE = 0;
	public static final int OVERFLOW_SCROLL = 1;
	public static final int OVERFLOW_AUTO = 2;
	public static final int OVERFLOW_HIDDEN = 3;
	public static final int OVERFLOW_VISIBLE = 4;
	public static final int OVERFLOW_VERTICAL = 5;
	
	public static final int BORDER_STYLE_NONE = 0;
	public static final int BORDER_STYLE_HIDDEN = 1;
	public static final int BORDER_STYLE_DOTTED = 2;
	public static final int BORDER_STYLE_DASHED = 3;
	public static final int BORDER_STYLE_SOLID = 4;
	public static final int BORDER_STYLE_DOUBLE = 5;
	public static final int BORDER_STYLE_GROOVE = 6;
	public static final int BORDER_STYLE_RIDGE = 7;
	public static final int BORDER_STYLE_INSET = 8;
	public static final int BORDER_STYLE_OUTSET = 9;
	
	/**
	 * A collection of all GUI components
	 * added by descendents.
	 */
	private Collection guiComponents = null;	

	/**
	 * A list of absolute positioned or
	 * float parent-child pairs.
	 */
	protected Collection delayedPairs = null;	

	//	protected boolean renderStyleCanBeInvalidated = true;

	/**
	 * Background color which may be different to
	 * that from RenderState in the case of a Document node.
	 */
	protected Color backgroundColor;
	protected volatile Image backgroundImage;
	protected int zIndex;
	protected Color borderTopColor;
	protected Color borderLeftColor;
	protected Color borderBottomColor;
	protected Color borderRightColor;	
	protected Insets borderInsets;
	protected Insets borderStyles;
	protected java.net.URL lastBackgroundImageUri;
	protected Insets defaultMarginInsets;
	private int cachedOverflow = -1;
	
	protected final UserAgentContext userAgentContext;
	
	public BaseElementRenderable(RenderableContainer container, ModelNode modelNode, UserAgentContext ucontext) {
		super(container, modelNode);
		this.userAgentContext = ucontext;
	}

	public float getAlignmentX() {
		return 0.0f;
	}

	public float getAlignmentY() {
		return 0.0f;
	}
	
	protected boolean layoutDeepCanBeInvalidated = false;
	
	/**
	 * Invalidates this Renderable and all
	 * descendents. This is only used in special
	 * cases, such as when a new style sheet is
	 * added.
	 */
	public final void invalidateLayoutDeep() {
		if(this.layoutDeepCanBeInvalidated) {
			this.layoutDeepCanBeInvalidated = false;
			this.invalidateLayoutLocal();
			Iterator i = this.getRenderables();
			if(i != null) {
				while(i.hasNext()) {
					Object r = i.next();
					if(r instanceof RCollection) {
						((RCollection) r).invalidateLayoutDeep();
					}
				}
			}
		}
	}

	protected void invalidateLayoutLocal() {
		this.cachedOverflow = -1;
	}

	protected int getDeclaredWidth(RenderState renderState, int availWidth) {
		Object rootNode = this.modelNode;
		if(rootNode instanceof HTMLElementImpl) {
			HTMLElementImpl element = (HTMLElementImpl) rootNode;
			CSS2Properties props = element.getCurrentStyle();
			if(props == null) {
				return -1;
			}
			String widthText = props.getWidth();
			if(widthText == null || "".equals(widthText)) {
				return -1;
			}
			return HtmlValues.getPixelSize(widthText, renderState, -1, availWidth);			
		}
		else {
			return -1;
		}
	}

	protected int getDeclaredHeight(RenderState renderState, int availHeight) {
		Object rootNode = this.modelNode;
		if(rootNode instanceof HTMLElementImpl) {
			HTMLElementImpl element = (HTMLElementImpl) rootNode;
			CSS2Properties props = element.getCurrentStyle();
			if(props == null) {
				return -1;
			}
			String heightText = props.getHeight();
			if(heightText == null || "".equals(heightText)) {
				return -1;
			}
			return HtmlValues.getPixelSize(heightText, renderState, -1, availHeight);			
		}
		else {
			return -1;
		}		
	}
		
	protected int getOverflow() {
		int co = this.cachedOverflow;
		if(co != -1) {
			return co;
		}
		Object rootNode = this.modelNode;
		if(rootNode instanceof HTMLElementImpl) {
			HTMLElementImpl element = (HTMLElementImpl) rootNode;
			CSS2Properties props = element.getCurrentStyle();
			if(props == null) {
				co = OVERFLOW_NONE;
			}
			else {
				String overflowText = props.getOverflow();
				if(overflowText == null) {
					co = OVERFLOW_NONE;
				}
				else {
					String overflowTextTL = overflowText.toLowerCase();
					if("scroll".equals(overflowTextTL)) {
						co = OVERFLOW_SCROLL;
					}
					else if("auto".equals(overflowTextTL)) {
						co = OVERFLOW_AUTO;
					}
					else if("vertical".equals(overflowTextTL)) {
						co = OVERFLOW_VERTICAL;
					}
					else if("hidden".equals(overflowTextTL)) {
						co = OVERFLOW_HIDDEN;
					}
					else if("visible".equals(overflowTextTL)) {
						co = OVERFLOW_VISIBLE;
					}
					else {
						co = OVERFLOW_NONE;
					}
				}
			}
		}
		else {
			co = OVERFLOW_NONE;
		}
		this.cachedOverflow = co;
		return co;
	}
	
	/**
	 * All overriders should call super implementation.
	 */
	public void paint(Graphics g) {
	}

	public final void layout(int availWidth, int availHeight) {
		this.layout(availWidth, availHeight, false, false);
	}

	/**
	 * Lays out children, and deals with "valid" state. Override doLayout method
	 * instead of this one.
	 */
	public final void layout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight) {
		// Must call doLayout regardless of validity state.
		try {
			this.doLayout(availWidth, availHeight, expandWidth, expandHeight);	
		} finally {
			this.layoutUpTreeCanBeInvalidated = true;
			this.layoutDeepCanBeInvalidated = true;
//			this.renderStyleCanBeInvalidated = true;
		}
	}	
	
	protected abstract void doLayout(int availWidth, int availHeight, boolean expandWidth, boolean expandHeight);	
	
	protected final void sendGUIComponentsToParent() {
		// Ensures that parent has all the components
		// below this renderer node. (Parent expected to have removed them).
		Collection gc = this.guiComponents;
		int count = 0;
		if(gc != null) {
			RenderableContainer rc = this.container;
			Iterator i = gc.iterator();
			while(i.hasNext()) {
				count++;
				rc.add((Component) i.next());
			}
		}		
	}
	
	protected final void clearGUIComponents() {
		Collection gc = this.guiComponents;
		if(gc != null) {
			gc.clear();
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.RenderableContainer#add(java.awt.Component)
	 */
	public Component add(Component component) {
		// Expected to be called in GUI thread.
		// Adds only in local collection.
		// Does not remove from parent.
		// Sending components to parent is done
		// by sendGUIComponentsToParent().
		Collection gc = this.guiComponents;
		if(gc == null) {
			gc = new HashSet(1);
			this.guiComponents = gc;
		}
		gc.add(component);
		return component;
	}

	public void updateAllWidgetBounds() {
		this.container.updateAllWidgetBounds();
	}

	/** 
	 * Updates widget bounds below this node only.
	 * Should not be called during general rendering.
	 */
	public void updateWidgetBounds() {
		java.awt.Point guiPoint = this.getGUIPoint(0, 0);
		this.updateWidgetBounds(guiPoint.x, guiPoint.y);
	}

	protected void applyStyle() {
		//TODO: Can be optimized if there's no style?
		//Note: Overridden by table cell
		Object rootNode = this.modelNode;
		HTMLElementImpl rootElement;
		if(rootNode instanceof HTMLDocumentImpl) {			
			HTMLDocumentImpl doc = (HTMLDocumentImpl) rootNode;
			// Need to get BODY tag, for bgcolor, etc.
			rootElement = (HTMLElementImpl) doc.getBody();
		}
		else {
			rootElement = (HTMLElementImpl) rootNode;
		}
		if(rootElement == null) {
			this.clearStyle();
			this.backgroundColor = null;
			this.backgroundImage = null;
			this.lastBackgroundImageUri = null;
			return;
		}
		RenderState rs = rootElement.getRenderState();
		if(rs == null) {
			throw new IllegalStateException("Element without render state: " + rootElement + "; parent=" + rootElement.getParentNode());
		}
		BackgroundInfo binfo = rs.getBackgroundInfo();
		this.backgroundColor = binfo == null ? null : binfo.backgroundColor;
		java.net.URL backgroundImageUri = binfo == null ? null
				: binfo.backgroundImage;
		if (backgroundImageUri == null) {
			this.backgroundImage = null;
			this.lastBackgroundImageUri = null;
		} 
		else if(!backgroundImageUri.equals(this.lastBackgroundImageUri)) {
			this.lastBackgroundImageUri = backgroundImageUri;
			this.loadBackgroundImage(backgroundImageUri);
		}
		CSS2PropertiesImpl props = rootElement.getCurrentStyle();
		if(props == null) {
			this.clearStyle();
		}
		else {
			this.borderInsets = null;
			this.borderStyles = null;
			this.borderTopColor = null;
			this.borderLeftColor = null;
			this.borderBottomColor = null;
			this.borderRightColor = null;
			String border = props.getBorder();
			if(border != null) {
				this.applyBorder(rs, border);
			}
			this.borderInsets = HtmlValues.getBorderInsets(this.borderInsets, props, rs);
			String borderColorText = props.getBorderColor();
			if(borderColorText != null) {
				Color[] colorsArray = HtmlValues.getColors(borderColorText);
				this.borderTopColor = colorsArray[0];
				this.borderLeftColor = colorsArray[1];
				this.borderBottomColor = colorsArray[2];
				this.borderRightColor = colorsArray[3];
			}
			String borderTopColorText = props.getBorderTopColor();
			if(borderTopColorText != null) {
				this.borderTopColor = ColorFactory.getInstance().getColor(borderTopColorText);
			}
			String borderLeftColorText = props.getBorderLeftColor();
			if(borderLeftColorText != null) {
				this.borderLeftColor = ColorFactory.getInstance().getColor(borderLeftColorText);
			}
			String borderBottomColorText = props.getBorderBottomColor();
			if(borderBottomColorText != null) {
				this.borderBottomColor = ColorFactory.getInstance().getColor(borderBottomColorText);
			}
			String borderRightColorText = props.getBorderRightColor();
			if(borderRightColorText != null) {
				this.borderRightColor = ColorFactory.getInstance().getColor(borderRightColorText);
			}
			String zIndex = props.getZIndex();
			if(zIndex != null) {
				try {
					this.zIndex = Integer.parseInt(zIndex);
				} catch(NumberFormatException err) {
					logger.log(Level.WARNING, "Unable to parse z-index [" + zIndex + "] in element " + this.modelNode + ".", err);
					this.zIndex = 0;
				}
			}
			else {
				this.zIndex = 0;
			}
		}

		// Check if background image needs to be loaded
	}

	protected void loadBackgroundImage(final java.net.URL imageURL) {
		ModelNode rc = this.modelNode;
		UserAgentContext ctx = this.userAgentContext;
		if(ctx != null) {
			final HttpRequest request = ctx.createHttpRequest();
			request.addReadyStateChangeListener(new ReadyStateChangeListener() {
				public void readyStateChanged() {
					int readyState = request.getReadyState();
					if(readyState == HttpRequest.STATE_COMPLETE) {
						int status = request.getStatus();
						if(status == 200 || status == 0) {
							Image img = request.getResponseImage();
							BaseElementRenderable.this.backgroundImage = img;
							// Cause observer to be called
							int w = img.getWidth(BaseElementRenderable.this);
							int h = img.getHeight(BaseElementRenderable.this);
							// Maybe image already done...
							if(w != -1 && h != -1) {
								BaseElementRenderable.this.repaint();
							}
						}							
					}
				}
			});
			SecurityManager sm = System.getSecurityManager();
			if(sm == null) {
				request.open("GET", imageURL);					
			}
			else {
				AccessController.doPrivileged(new PrivilegedAction() {
					public Object run() {
						// Code might have restrictions on accessing
						// items from elsewhere.
						request.open("GET", imageURL);
						return null;
					}
				});
			}
		}
	}

	public int getZIndex() {
		return this.zIndex;
	}

	void applyBorder(RenderState renderState, String border) {
		String[] tokens = HtmlValues.splitCssValue(border);
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if(HtmlValues.isLength(token)) {
				int pixelSize = HtmlValues.getPixelSize(token, renderState, 0);
				Insets bi = new Insets(pixelSize, pixelSize, pixelSize, pixelSize);
				this.borderInsets = bi;
			}
			else if(ColorFactory.getInstance().isColor(token)) {
				Color color = ColorFactory.getInstance().getColor(token);
				this.borderLeftColor = color;
				this.borderRightColor = color;
				this.borderTopColor = color;
				this.borderBottomColor = color;
			}
			else if(HtmlValues.isBorderStyle(token)) {
				//TODO: dotted and so forth
				if("solid".equalsIgnoreCase(token)) {
					Insets bi = this.borderInsets;
					if(bi == null) {
						bi = new Insets(4, 4, 4, 4);
						this.borderInsets = bi;
					}
					this.borderStyles = new Insets(BORDER_STYLE_SOLID,BORDER_STYLE_SOLID,BORDER_STYLE_SOLID,BORDER_STYLE_SOLID);
				}
				else if("dashed".equalsIgnoreCase(token)) {
					Insets bi = this.borderInsets;
					if(bi == null) {
						bi = new Insets(4, 4, 4, 4);
						this.borderInsets = bi;
					}
					this.borderStyles = new Insets(BORDER_STYLE_DASHED,BORDER_STYLE_DASHED,BORDER_STYLE_DASHED,BORDER_STYLE_DASHED);
				}
				else {
					this.borderStyles = null;					
				}
			}
		}
	}

	private Color getBorderTopColor() {
		Color c = this.borderTopColor;
		return c == null ? Color.black : c;
	}

	private Color getBorderLeftColor() {
		Color c = this.borderLeftColor;
		return c == null ? Color.black : c;
	}

	private Color getBorderBottomColor() {
		Color c = this.borderBottomColor;
		return c == null ? Color.black : c;
	}

	private Color getBorderRightColor() {
		Color c = this.borderRightColor;
		return c == null ? Color.black : c;
	}

	protected void prePaint(java.awt.Graphics g) {
		int startWidth = this.width;
		int startHeight = this.height;
		int totalWidth = startWidth;
		int totalHeight = startHeight;
		int startX = 0;
		int startY = 0;
		ModelNode node = this.modelNode;
		RenderState rs = node.getRenderState();
		Insets marginInsets = this.getMarginInsets(rs);
		if(marginInsets != null) {
			totalWidth -= (marginInsets.left + marginInsets.right);
			totalHeight -= (marginInsets.top + marginInsets.bottom);
			startX += marginInsets.left;
			startY += marginInsets.top;
		}
		Insets borderInsets = this.borderInsets;
		if(borderInsets != null) {
			int btop = borderInsets.top;
			int bleft = borderInsets.left;
			int bright = borderInsets.right;
			int bbottom = borderInsets.bottom;
	
			int newTotalWidth = totalWidth - (bleft + bright);
			int newTotalHeight = totalHeight - (btop + bbottom);
			int newStartX = startX + bleft;
			int newStartY = startY + btop;
			Rectangle clientRegion = new Rectangle(newStartX, newStartY, newTotalWidth, newTotalHeight);
	
			// Paint borders if the clip bounds are not contained
			// by the content area.
			Rectangle clipBounds = g.getClipBounds();
			if(!clientRegion.contains(clipBounds)) {
				Insets borderStyles = this.borderStyles;
				if(btop > 0) {
					g.setColor(this.getBorderTopColor());
					int borderStyle = borderStyles == null ? BORDER_STYLE_SOLID : borderStyles.top;
					for(int i = 0; i < btop; i++) {
						int leftOffset = (i * bleft) / btop;
						int rightOffset = (i * bright) / btop;
						if(borderStyle == BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, startX + leftOffset, startY + i, startX + totalWidth - rightOffset - 1, startY + i, 10 + btop, 6);
						}
						else {
							g.drawLine(startX + leftOffset, startY + i, startX + totalWidth - rightOffset - 1, startY + i);
						}
					}
				}
				if(bright > 0) {
					int borderStyle = borderStyles == null ? BORDER_STYLE_SOLID : borderStyles.right;
					g.setColor(this.getBorderRightColor());
					int lastX = startX + totalWidth - 1;
					for(int i = 0; i < bright; i++) {
						int topOffset = (i * btop) / bright;
						int bottomOffset = (i * bbottom) / bright;
						if(borderStyle == BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, lastX - i, startY + topOffset, lastX - i, startY + totalHeight - bottomOffset - 1, 10 + bright, 6);
						}
						else {
							g.drawLine(lastX - i, startY + topOffset, lastX - i, startY + totalHeight - bottomOffset - 1);
						}
					}				
				}
				if(bbottom > 0) {
					int borderStyle = borderStyles == null ? BORDER_STYLE_SOLID : borderStyles.bottom;
					g.setColor(this.getBorderBottomColor());
					int lastY = startY + totalHeight - 1;
					for(int i = 0; i < bbottom; i++) {
						int leftOffset = (i * bleft) / bbottom;
						int rightOffset = (i * bright) / bbottom;					
						if(borderStyle == BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, startX + leftOffset, lastY - i, startX + totalWidth - rightOffset - 1, lastY - i, 10 + bbottom, 6);
						}
						else {
							g.drawLine(startX + leftOffset, lastY - i, startX + totalWidth - rightOffset - 1, lastY - i);
						}
					}				
				}
				if(bleft > 0) {
					int borderStyle = borderStyles == null ? BORDER_STYLE_SOLID : borderStyles.left;
					g.setColor(this.getBorderLeftColor());
					for(int i = 0; i < bleft; i++) {
						int topOffset = (i * btop) / bleft;
						int bottomOffset = (i * bbottom) / bleft;
						if(borderStyle == BORDER_STYLE_DASHED) {
							GUITasks.drawDashed(g, startX + i, startY + topOffset, startX + i, startY + totalHeight - bottomOffset - 1, 10 + bleft, 6);
						}
						else {
							g.drawLine(startX + i, startY + topOffset, startX + i, startY + totalHeight - bottomOffset - 1);
						}
					}				
				}
			}
	
			// Adjust client area border
			totalWidth = newTotalWidth;
			totalHeight = newTotalHeight;
			startX = newStartX;
			startY = newStartY;
	
		}
		// Using clientG (clipped below) beyond this point.
		Graphics clientG = g.create(startX, startY, totalWidth, totalHeight);
		try {
			Rectangle bkgBounds = null;
			if(node != null) {
				Color bkg = this.backgroundColor;
				if(bkg != null && bkg.getAlpha() > 0) {
					clientG.setColor(bkg);
					bkgBounds = clientG.getClipBounds();
					clientG.fillRect(bkgBounds.x, bkgBounds.y, bkgBounds.width, bkgBounds.height);
				}
				BackgroundInfo binfo = rs == null ? null : rs.getBackgroundInfo();
				Image image = this.backgroundImage;
				if(image != null) {
					if(bkgBounds == null) {
						bkgBounds = clientG.getClipBounds();
					}
					int w = image.getWidth(this);
					int h = image.getHeight(this);
					if(w != -1 && h != -1) {
						switch(binfo == null ? BackgroundInfo.BR_REPEAT : binfo.backgroundRepeat) {
						case BackgroundInfo.BR_NO_REPEAT: {
							int imageX;
							if(binfo.backgroundXPositionAbsolute) {
								imageX = binfo.backgroundXPosition;
							}
							else {
								imageX = (binfo.backgroundXPosition * (totalWidth - w)) / 100;
							}
							int imageY;
							if(binfo.backgroundYPositionAbsolute) {
								imageY = binfo.backgroundYPosition;
							}
							else {
								imageY =(binfo.backgroundYPosition * (totalHeight - h)) / 100;
							}
							clientG.drawImage(image, imageX, imageY, w, h, this);
							break;
						}
						case BackgroundInfo.BR_REPEAT_X: {
							int imageY;
							if(binfo.backgroundYPositionAbsolute) {
								imageY = binfo.backgroundYPosition;
							}
							else {
								imageY = (binfo.backgroundYPosition * (totalHeight - h)) / 100;
							}
							// Modulate starting x.
							int x = (bkgBounds.x / w) * w;
							int topX = bkgBounds.x + bkgBounds.width;
							for(; x < topX; x += w) {
								clientG.drawImage(image, x, imageY, w, h, this);
							}
							break;
						}
						case BackgroundInfo.BR_REPEAT_Y: {
							int imageX;
							if(binfo.backgroundXPositionAbsolute) {
								imageX = binfo.backgroundXPosition;
							}
							else {
								imageX = (binfo.backgroundXPosition * (totalWidth - w)) / 100;
							}
							// Modulate starting y.
							int y = (bkgBounds.y / h) * h;
							int topY = bkgBounds.y + bkgBounds.height;						
							for(; y < topY; y += h) {
								clientG.drawImage(image, imageX, y, w, h, this);
							}
							break;
						}
						default: {
							// Modulate starting x and y.
							int baseX = (bkgBounds.x / w) * w;
							int baseY = (bkgBounds.y / h) * h;
							int topX = bkgBounds.x + bkgBounds.width;
							int topY = bkgBounds.y + bkgBounds.height;
							// Replacing this:
							for(int x = baseX; x < topX; x += w) {
								for(int y = baseY; y < topY; y += h) {
									clientG.drawImage(image, x, y, w, h, this);
								}
							}
							break;
						}
						}					
					}
				}
			}
		} finally {
			clientG.dispose();
		}
	}

	void clearStyle() {
		this.borderStyles = null;
		this.borderInsets = null;
		this.borderTopColor = null;
		this.borderLeftColor = null;
		this.borderBottomColor = null;
		this.borderRightColor = null;
		this.zIndex = 0;
	}

	protected final Insets getMarginInsets(RenderState rs) {
		Insets mi = rs.getMarginInsets();
		if(mi == null) {
			return this.defaultMarginInsets;
		}
		return mi;
	}	
	
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
		// This is so that a loading image doesn't cause
		// too many repaint events.
		if((infoflags & ImageObserver.ALLBITS) != 0 || (infoflags & ImageObserver.FRAMEBITS) != 0) {
			this.repaint();
		}
		return true;
	}		
	
	protected static final int SCROLL_BAR_THICKNESS = 16;
	
	/**
	 * Gets insets of content area. It includes margin, borders
	 * and scrollbars, but not padding.
	 */
	public Insets getInsets(boolean hscroll, boolean vscroll) {
		RenderState rs = this.modelNode.getRenderState();
		Insets mi = this.getMarginInsets(rs);
		Insets bi = this.borderInsets;
		int top = 0;
		int bottom = 0;
		int left = 0;
		int right = 0;
		if(mi != null) {
			top += mi.top;
			left += mi.left;
			bottom += mi.bottom;
			right += mi.right;
		}
		if(bi != null) {
			top += bi.top;
			left += bi.left;
			bottom += bi.bottom;
			right += bi.right;
		}
		if(hscroll) {
			bottom += SCROLL_BAR_THICKNESS;
		}
		if(vscroll) {
			right += SCROLL_BAR_THICKNESS;
		}
		return new Insets(top, left, bottom, right);
	}
	
	protected final void sendDelayedPairsToParent() {
		// Ensures that parent has all the components
		// below this renderer node. (Parent expected to have removed them).
		Collection gc = this.delayedPairs;
		if(gc != null) {
			RenderableContainer rc = this.container;
			Iterator i = gc.iterator();
			while(i.hasNext()) {
				DelayedPair pair = (DelayedPair) i.next();
				if(pair.targetParent != this) {
					rc.addDelayedPair(pair);
				}
			}
		}		
	}
	
	public final void clearDelayedPairs() {
		Collection gc = this.delayedPairs;
		if(gc != null) {
			gc.clear();
		}		
	}
	
	public final Collection getDelayedPairs() {
		return this.delayedPairs;
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.RenderableContainer#add(java.awt.Component)
	 */
	public void addDelayedPair(DelayedPair pair) {
		// Expected to be called in GUI thread.
		// Adds only in local collection.
		// Does not remove from parent.
		// Sending components to parent is done
		// by sendDelayedPairsToParent().
		Collection gc = this.delayedPairs;
		if(gc == null) {
			// Sequence is important.
			//TODO: But possibly added multiple
			//times in table layout?
			gc = new java.util.LinkedList();
			this.delayedPairs = gc;
		}
		gc.add(pair);
	}

	public RenderableContainer getParentContainer() {
		return this.container;
	}
	
	public boolean isContainedByNode() {
		return true;
	}
}
