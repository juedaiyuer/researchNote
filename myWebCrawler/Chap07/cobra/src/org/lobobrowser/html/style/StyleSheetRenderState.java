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
package org.lobobrowser.html.style;
import java.awt.*;
import java.util.*;

import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.util.gui.ColorFactory;
import org.lobobrowser.util.gui.FontFactory;
import org.w3c.dom.css.*;
import org.w3c.dom.html2.*;

/**
 * @author J. H. S.
 */
public class StyleSheetRenderState implements RenderState {
	private static final FontFactory FONT_FACTORY = FontFactory.getInstance();
	private static final String DEFAULT_FONT_FAMILY = "Times New Roman";
	private static final Font DEFAULT_FONT = FONT_FACTORY.getFont(DEFAULT_FONT_FAMILY, null, null, null, HtmlValues.DEFAULT_FONT_SIZE);
	protected static final Insets INVALID_INSETS = new Insets(0, 0, 0, 0);
	protected static final BackgroundInfo INVALID_BACKGROUND_INFO = new BackgroundInfo();
	protected static final Color INVALID_COLOR = new Color(100, 0, 100);

	protected final HTMLElementImpl element;
	protected final HTMLDocumentImpl document;
	protected final RenderState prevRenderState;
	
	private Font iFont;
	private FontMetrics iFontMetrics;
	private Color iColor;
	private Color iBackgroundColor = INVALID_COLOR;
	private Color iTextBackgroundColor = INVALID_COLOR;
	private Color iOverlayColor = INVALID_COLOR;
	private BackgroundInfo iBackgroundInfo = INVALID_BACKGROUND_INFO;
	private int iTextDecoration = -1;
	private int iBlankWidth = -1;
	private boolean iHighlight;

	static {
	}
	
	public StyleSheetRenderState(RenderState prevRenderState, HTMLElementImpl element) {
		this.prevRenderState = prevRenderState;
		this.element = element;
		this.document = (HTMLDocumentImpl) element.getOwnerDocument();
	}

	public StyleSheetRenderState(HTMLDocumentImpl document) {
		this.prevRenderState = null;
		this.element = null;
		this.document = document;
	}

//	public TextRenderState(RenderState prevRenderState) {
//		this.css2properties = new CSS2PropertiesImpl(this);
//		this.prevRenderState = prevRenderState;
//	}
	
	protected int getDefaultDisplay() {
		return DISPLAY_INLINE;
	}

	private Integer iDisplay;
	
	public int getDisplay() {
		Integer d = this.iDisplay;
		if(d != null) {
			return d.intValue();
		}
		CSS2Properties props = this.getCssProperties();
		String displayText = props == null ? null : props.getDisplay();
		int displayInt;
		if(displayText != null) {
			String displayTextTL = displayText.toLowerCase();
			if("block".equals(displayTextTL)) {
				displayInt = DISPLAY_BLOCK;
			}
			else if("inline".equals(displayTextTL)) {
				displayInt = DISPLAY_INLINE;
			}
			else if("none".equals(displayTextTL)) {
				displayInt = DISPLAY_NONE;
			}
			else if("list-item".equals(displayTextTL)) {
				displayInt = DISPLAY_LIST_ITEM;
			}
			else if("table".equals(displayTextTL)) {
				displayInt = DISPLAY_TABLE;
			}
			else if("table-cell".equals(displayTextTL)) {
				displayInt = DISPLAY_TABLE_CELL;
			}
			else if("table-row".equals(displayTextTL)) {
				displayInt = DISPLAY_TABLE_ROW;
			}
			else {
				displayInt = this.getDefaultDisplay();
			}
		}
		else {
			displayInt = this.getDefaultDisplay();
		}
		d = new Integer(displayInt);
		this.iDisplay = d;
		return displayInt;		
	}
	
	public RenderState getPreviousRenderState() {
		return this.prevRenderState;
	}

	public int getFontBase() {
		RenderState prs = this.prevRenderState;
		return prs == null ? 3 : prs.getFontBase();
	}

	public void repaint() {
		// Dummy implementation
	}

	protected final CSS2PropertiesImpl getCssProperties() {
		HTMLElementImpl element = this.element;
		return element == null ? null : element.getCurrentStyle();
	}

	public void invalidate() {
		Map map = this.iWordInfoMap;
		if(map != null) {
			map.clear();
		}
		this.iFont = null;
		this.iFontMetrics = null;
		this.iColor = null;
		this.iTextDecoration = -1;
		this.iBlankWidth = -1;
		this.alignXPercent = -1;
		this.iBackgroundColor = INVALID_COLOR;
		this.iTextBackgroundColor = INVALID_COLOR;
		this.iOverlayColor = INVALID_COLOR;
		this.iBackgroundInfo = INVALID_BACKGROUND_INFO;
		this.iDisplay = null;
		this.iTextIndentText = null;
		this.iWhiteSpace = null;
		this.marginInsets = INVALID_INSETS;
		this.paddingInsets = INVALID_INSETS;
		// Should NOT invalidate parent render state.
	}
		
//	private final String getLocalFont() {
//		String value = this.getCssProperties().getFont();
//		if(value != null && value.length() == 0) {
//			value = null;
//		}
//		return value;
//	}
//	
//	private final String getLocalFontFamily() {
//		String value = this.getCssProperties().getFontFamily();
//		if(value != null && value.length() == 0) {
//			value = null;
//		}
//		return value;
//	}
//	
//	private final String getLocalFontStyle() {
//		String value = this.getCssProperties().getFontStyle();
//		if(value != null && value.length() == 0) {
//			value = null;
//		}
//		return value;
//	}
//	
//	private final String getLocalFontVariant() {
//		String value = this.getCssProperties().getFontVariant();
//		if(value != null && value.length() == 0) {
//			value = null;
//		}		
//		return value;
//	}
//	
//	private final String getLocalFontWeight() {
//		String value = this.getCssProperties().getFontWeight();
//		if(value != null && value.length() == 0) {
//			value = null;
//		}
//		return value;
//	}
//	
//	private final String getLocalFontSize() {
//		String value = this.getCssProperties().getFontSize();
//		if(value != null && value.length() == 0) {
//			value = null;
//		}		
//		return value;
//	}

	
	public Font getFont() {
		Font f = this.iFont;
		if(f != null) {
			return f;
		}
		CSS2PropertiesImpl style = this.getCssProperties();
		RenderState prs = this.prevRenderState;
		if(style == null) {
			if(prs != null) {
				f = prs.getFont();
				this.iFont = f;
				return f;
			}
			f = DEFAULT_FONT;
			this.iFont = f;
			return f;
		}
		Float fontSize = null;
		String fontStyle = null;
		String fontVariant = null;
		String fontWeight = null;
		String fontFamily = null;
		
		String fontSpec = style == null ? null : style.getFont();
		if(fontSpec != null) {
			FontInfo fontInfo = HtmlValues.createFontInfo(fontSpec, this.prevRenderState);
			fontSize = fontInfo.fontSize;
			fontStyle = fontInfo.fontStyle;
			fontVariant = fontInfo.fontVariant;
			fontWeight = fontInfo.fontWeight;
			fontFamily = fontInfo.fontFamily;
		}		
		String newFontSize = style == null ? null : style.getFontSize();
		String newFontFamily = style == null ? null : style.getFontFamily();
		String newFontStyle = style == null ? null : style.getFontStyle();
		String newFontVariant = style == null ? null : style.getFontVariant();
		String newFontWeight = style == null ? null : style.getFontWeight();
		if(fontSpec == null && newFontSize == null && newFontWeight == null && newFontStyle == null && newFontFamily == null && newFontVariant == null) {
			if(prs != null) {
				f = prs.getFont();
				this.iFont = f;
				return f;
			}
			f = DEFAULT_FONT;
			this.iFont = f;
			return f;
		}
		if(newFontSize != null) {
			try {
				fontSize = new Float(HtmlValues.getFontSize(newFontSize, prs));
			} catch(Exception err) {
				fontSize = HtmlValues.DEFAULT_FONT_SIZE_BOX;
			}
		}
		else if(fontSize == null) {
			if(prs != null) {
				fontSize = new Float(prs.getFont().getSize());
			} else {
				fontSize = HtmlValues.DEFAULT_FONT_SIZE_BOX;
			}
		}
		if(newFontFamily != null) {
			fontFamily = newFontFamily;
		}
		else if(fontFamily == null && prs != null) {
			fontFamily = prs.getFont().getFamily();
		}
		if(fontFamily == null) {
			fontFamily = DEFAULT_FONT_FAMILY;
		}
		if(newFontStyle != null) {
			fontStyle = newFontStyle;
		}
		else if(fontStyle == null && prs != null) {
			int fstyle = prs.getFont().getStyle();
			if((fstyle & Font.ITALIC) != 0) {
				fontStyle = "italic";
			}
		}
		if(newFontVariant != null) {
			fontVariant = newFontVariant;
		}
		else if(prs != null) {
			// TODO: smallcaps?
		}
		if(newFontWeight != null) {
			fontWeight = newFontWeight;
		}
		else if(fontWeight == null && prs != null) {
			int fstyle = prs.getFont().getStyle();
			if((fstyle & Font.BOLD) != 0) {
				fontWeight = "bold";
			}			
		}
		f = FONT_FACTORY.getFont(fontFamily, fontStyle, fontVariant, fontWeight, fontSize.floatValue());
		this.iFont = f;
		return f;
	}
	
	public Color getColor() {
		Color c = this.iColor;
		if(c != null) {
			return c;
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		String colorValue = props == null ? null : props.getColor();
		if(colorValue == null || "".equals(colorValue)) {
			RenderState prs = this.prevRenderState;
			if(prs != null) {
				c = prs.getColor();
				this.iColor = c;
				return c;
			}
			else {
				colorValue = "black";
			}
		}
		c = ColorFactory.getInstance().getColor(colorValue);
		this.iColor = c;
		return c;
	}
	
	public Color getBackgroundColor() {
		Color c = this.iBackgroundColor;
		if(c != INVALID_COLOR) {
			return c;
		}
		Color localColor;
		BackgroundInfo binfo = this.getBackgroundInfo();
		localColor = binfo == null ? null : binfo.backgroundColor;
		if(localColor == null && this.getDisplay() == DISPLAY_INLINE) {
			RenderState prs = this.prevRenderState;
			if(prs != null) {
				Color ancestorColor = prs.getBackgroundColor();
				if(ancestorColor != null) {
					this.iBackgroundColor = ancestorColor;
					return ancestorColor;
				}
			}
		}
		this.iBackgroundColor = localColor;
		return localColor;
	}
	
	public Color getTextBackgroundColor() {
		Color c = this.iTextBackgroundColor;
		if(c != INVALID_COLOR) {
			return c;
		}
		Color localColor;
		if(this.getDisplay() != DISPLAY_INLINE) {
			// Background painted by block.
			localColor = null;
		}
		else {
			BackgroundInfo binfo = this.getBackgroundInfo();
			localColor = binfo == null ? null : binfo.backgroundColor;
			if(localColor == null) {
				RenderState prs = this.prevRenderState;
				if(prs != null) {
					Color ancestorColor = prs.getTextBackgroundColor();
					if(ancestorColor != null) {
						this.iTextBackgroundColor = ancestorColor;
						return ancestorColor;
					}
				}
			}
		}
		this.iTextBackgroundColor = localColor;
		return localColor;
	}

	public Color getOverlayColor() {
		Color c = this.iOverlayColor;
		if(c != INVALID_COLOR) {
			return c;
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		String colorValue = props == null ? null : props.getOverlayColor();
		if(colorValue == null || colorValue.length() == 0) {
			RenderState prs = this.prevRenderState;
			if(prs != null) {
				c = prs.getOverlayColor();
				this.iOverlayColor = c;
				return c;
			}
			else {
				colorValue = null;
			}
		}
		c = colorValue == null ? null : ColorFactory.getInstance().getColor(colorValue);
		this.iOverlayColor = c;
		return c;
	}
	
	public int getTextDecorationMask() {
		int td = this.iTextDecoration;
		if(td != -1) {
			return td;
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		String tdText = props == null ? null : props.getTextDecoration();
		if(tdText == null) {
			RenderState prs = this.prevRenderState;
			if(prs != null) {
				td = prs.getTextDecorationMask();
				this.iTextDecoration = td;
				return td;
			}
		}
		td = 0;
		if(tdText != null) {
			StringTokenizer tok = new StringTokenizer(tdText.toLowerCase(), ", \t\n\r");
			while(tok.hasMoreTokens()) {
				String token = tok.nextToken();
				if("none".equals(token)) {
					// continue
				}
				else if("underline".equals(token)) {
					td |= StyleSheetRenderState.MASK_TEXTDECORATION_UNDERLINE;
				}
				else if("line-through".equals(token)) {
					td |= StyleSheetRenderState.MASK_TEXTDECORATION_LINE_THROUGH;
				}
				else if("blink".equals(token)) {
					td |= StyleSheetRenderState.MASK_TEXTDECORATION_BLINK;
				}
				else if("overline".equals(token)) {
					td |= StyleSheetRenderState.MASK_TEXTDECORATION_OVERLINE;
				}
			}
		}
		this.iTextDecoration = td;
		return td;
	}
	
	public final FontMetrics getFontMetrics() {
		FontMetrics fm = this.iFontMetrics;
		if(fm == null) {
			//TODO getFontMetrics deprecated. How to get text width?
			fm = Toolkit.getDefaultToolkit().getFontMetrics(this.getFont());
			this.iFontMetrics = fm;
		}
		return fm;
	}
	
	public int getBlankWidth() {
		int bw = this.iBlankWidth;
		if(bw == -1) {
			bw = this.getFontMetrics().charWidth(' ');
			this.iBlankWidth = bw;
		}
		return bw;
	}

	/**
	 * @return Returns the iHighlight.
	 */
	public boolean isHighlight() {
		return this.iHighlight;
	}
	
	/**
	 * @param highlight The iHighlight to set.
	 */
	public void setHighlight(boolean highlight) {
		this.iHighlight = highlight;
	}
			
	Map iWordInfoMap = null;
	
	public final WordInfo getWordInfo(String word) {
		// Expected to be called only in the GUI (rendering) thread.
		// No synchronization necessary.
		Map map = this.iWordInfoMap;
		if(map == null) {
			map = new HashMap(1);
			this.iWordInfoMap = map;
		}
		WordInfo wi = (WordInfo) map.get(word);
		if(wi != null) {
			return wi;
		}
		wi = new WordInfo();
		FontMetrics fm = this.getFontMetrics();
		wi.fontMetrics = fm;
		wi.ascentPlusLeading = fm.getAscent() + fm.getLeading();
		wi.descent = fm.getDescent();
		wi.height = fm.getHeight();
		wi.width = fm.stringWidth(word);
		map.put(word, wi);
		return wi;
	}
	
	private int alignXPercent = -1;
	
	public int getAlignXPercent() {
		int axp = this.alignXPercent;
		if(axp != -1) {
			return axp;
		}
		CSS2Properties props = this.getCssProperties();
		String textAlign = props == null ? null : props.getTextAlign();
		if(textAlign == null || textAlign.length() == 0) {
			// Fall back to align attribute.
			HTMLElement element = this.element;
			if(element != null) {
				textAlign = element.getAttribute("align");
				if(textAlign == null || textAlign.length() == 0) {
					RenderState prs = this.prevRenderState;
					if(prs != null) {
						return prs.getAlignXPercent();
					}
					textAlign = null;
				}
			}
		}
		if(textAlign == null) {
			axp = 0;
		}
		else if("center".equalsIgnoreCase(textAlign)) {
			axp = 50;
		}
		else if("right".equalsIgnoreCase(textAlign)) {
			axp = 100;
		}
		else {
			//TODO: justify, <string>
			axp = 0;
		}
		this.alignXPercent = axp;
		return axp;
	}
	
	public int getAlignYPercent() {
		// This is only settable in table cells.
		// TODO: Does it work with display: table-cell?		
		return 0;
	}

	private Map counters = null;
	
	public int getCount(String counter, int nesting) {
		// Expected to be called only in GUI thread.
		RenderState prs = this.prevRenderState;
		if(prs != null) {
			return prs.getCount(counter, nesting);
		}
		Map counters = this.counters;
		if(counters == null) {
			return 0;
		}
		ArrayList counterArray = (ArrayList) counters.get(counter);
		if(nesting < 0 || nesting >= counterArray.size()) {
			return 0;
		}
		Integer integer = (Integer) counterArray.get(nesting);
		return integer == null ? 0 : integer.intValue();
	}

	public void resetCount(String counter, int nesting, int value) {
		// Expected to be called only in the GUI thread.
		RenderState prs = this.prevRenderState;
		if(prs != null) {
			prs.resetCount(counter, nesting, value);
		}
		Map counters = this.counters;
		if(counters == null) {
			counters = new HashMap(2);
			this.counters = counters;
			counters.put(counter, new ArrayList(0));
		}
		ArrayList counterArray = (ArrayList) counters.get(counter);
		while(counterArray.size() <= nesting) {
			counterArray.add(null);
		}
		counterArray.set(nesting, new Integer(value));
	}

	public int incrementCount(String counter, int nesting) {
		// Expected to be called only in the GUI thread.
		RenderState prs = this.prevRenderState;
		if(prs != null) {
			return prs.incrementCount(counter, nesting);
		}
		Map counters = this.counters;
		if(counters == null) {
			counters = new HashMap(2);
			this.counters = counters;
			counters.put(counter, new ArrayList(0));
		}
		ArrayList counterArray = (ArrayList) counters.get(counter);
		while(counterArray.size() <= nesting) {
			counterArray.add(null);
		}
		Integer integer = (Integer) counterArray.get(nesting);
		int prevValue = integer == null ? 0 : integer.intValue();
		counterArray.set(nesting, new Integer(prevValue + 1));
		return prevValue;
	}
	
	public BackgroundInfo getBackgroundInfo() {
		BackgroundInfo binfo = this.iBackgroundInfo;
		if(binfo != INVALID_BACKGROUND_INFO) {
			return binfo;
		}
		binfo = null;
		CSS2PropertiesImpl props = this.getCssProperties();
		if(props != null) {
			String background = props.getBackground();
			if(background != null) {
				if(binfo == null) {
					binfo = new BackgroundInfo();
				}
				CSSStyleDeclaration backgroundDecl = props.getStyleDeclaration("background");
				this.applyBackground(binfo, background, backgroundDecl);
			}
			String backgroundColorText = props.getBackgroundColor();
			if(backgroundColorText != null) {
				if(binfo == null) {
					binfo = new BackgroundInfo();
				}
				binfo.backgroundColor = ColorFactory.getInstance().getColor(backgroundColorText);
			}		
			String backgroundImageText = props.getBackgroundImage();
			if(backgroundImageText != null) {
				CSSStyleDeclaration backgroundImageDecl = props.getStyleDeclaration("background-image");
				java.net.URL backgroundImage = HtmlValues.getURIFromStyleValue(backgroundImageText, backgroundImageDecl, this.document);
				if(backgroundImage != null) {
					if(binfo == null) {
						binfo = new BackgroundInfo();
					}
					binfo.backgroundImage = backgroundImage;
				}
			}
			String backgroundRepeatText = props.getBackgroundRepeat();
			if(backgroundRepeatText != null) {
				if(binfo == null) {
					binfo = new BackgroundInfo();
				}			
				this.applyBackgroundRepeat(binfo, backgroundRepeatText);
			}
			String backgroundPositionText = props.getBackgroundPosition();
			if(backgroundPositionText != null) {
				if(binfo == null) {
					binfo = new BackgroundInfo();
				}			
				this.applyBackgroundPosition(binfo, backgroundPositionText);
			}
		}
		this.iBackgroundInfo = binfo;
		return binfo;
	}
	
	private String iTextIndentText = null;
	
	public String getTextIndentText() {
		String tiText = this.iTextIndentText;
		if(tiText != null) {
			return tiText;
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		tiText = props == null ? null : props.getTextIndent();
		if(tiText == null) {
			RenderState prs = this.prevRenderState;
			if(prs != null) {
				String parentText = prs.getTextIndentText();
				this.iTextIndentText = parentText;
				return parentText;
			}
			else {
				tiText = "";
			}
		}
		return tiText;
	}
	
	public int getTextIndent(int availSize) {
		// No caching for this one.
		String tiText = this.getTextIndentText();
		if(tiText.length() == 0) {
			return 0;
		}
		else {
		    return HtmlValues.getPixelSize(tiText, this, 0, availSize);
		}
	}
	
	protected Integer iWhiteSpace;

	public int getWhiteSpace() {
		if(RenderThreadState.getState().overrideNoWrap) {
			return WS_NOWRAP;
		}
		Integer ws = this.iWhiteSpace;
		if(ws != null) {
			return ws.intValue();
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		String whiteSpaceText = props == null ? null : props.getWhiteSpace();
		int wsValue;
		if(whiteSpaceText == null) {
//			HTMLElementImpl element = this.element;
//			if(element != null && element.getAttributeAsBoolean("nowrap")) {
//				wsValue = WS_NOWRAP;
//			}
//			else {
				RenderState prs = this.prevRenderState;
				if(prs != null) {
					wsValue = prs.getWhiteSpace();
				}
				else {
					wsValue = WS_NORMAL;
				}
//			}
		}
		else {
			String whiteSpaceTextTL = whiteSpaceText.toLowerCase();
			if("nowrap".equals(whiteSpaceTextTL)) {
				wsValue = WS_NOWRAP;
			}
			else if("pre".equals(whiteSpaceTextTL)) {
				wsValue = WS_PRE;
			}
			else {
				wsValue = WS_NORMAL;
			}
		}
		this.iWhiteSpace = new Integer(wsValue);
		return wsValue;
	}
	
	protected java.awt.Insets marginInsets = INVALID_INSETS;
	protected java.awt.Insets paddingInsets = INVALID_INSETS;
	
	public java.awt.Insets getMarginInsets() {
		Insets mi = this.marginInsets;
		if(mi != INVALID_INSETS) {
			return mi;
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		if(props == null) { 
			mi = null;
		}
		else {
			mi = HtmlValues.getMarginInsets(props, this);
		}
		this.marginInsets = mi;
		return mi;
	}
	
	public java.awt.Insets getPaddingInsets() {
		Insets mi = this.paddingInsets;
		if(mi != INVALID_INSETS) {
			return mi;
		}
		CSS2PropertiesImpl props = this.getCssProperties();
		if(props == null) {
			mi = null;
		}
		else {
			mi = HtmlValues.getPaddingInsets(props, this);
			this.paddingInsets = mi;
		}
		return mi;
	}

	private void applyBackgroundHorizontalPositon(BackgroundInfo binfo, String xposition) {
		if(xposition.endsWith("%")) {
			binfo.backgroundXPositionAbsolute = false;
			try {
				binfo.backgroundXPosition = (int) Double.parseDouble(xposition.substring(0, xposition.length() - 1).trim());
			} catch(NumberFormatException nfe) {
				binfo.backgroundXPosition = 0;
			}
		}
		else if("center".equalsIgnoreCase(xposition)) {
			binfo.backgroundXPositionAbsolute = false;
			binfo.backgroundXPosition = 50;			
		}
		else if("right".equalsIgnoreCase(xposition)) {
			binfo.backgroundXPositionAbsolute = false;
			binfo.backgroundXPosition = 100;			
		}
		else if("left".equalsIgnoreCase(xposition)) {
			binfo.backgroundXPositionAbsolute = false;
			binfo.backgroundXPosition = 0;			
		}
		else {
			binfo.backgroundXPositionAbsolute = true;
			binfo.backgroundXPosition = HtmlValues.getPixelSize(xposition, this, 0);
		}		
	}

	private void applyBackgroundVerticalPosition(BackgroundInfo binfo, String yposition) {
		if(yposition.endsWith("%")) {
			binfo.backgroundYPositionAbsolute = false;
			try {
				binfo.backgroundYPosition = (int) Double.parseDouble(yposition.substring(0, yposition.length() - 1).trim());
			} catch(NumberFormatException nfe) {
				binfo.backgroundYPosition = 0;
			}
		}
		else if("center".equalsIgnoreCase(yposition)) {
			binfo.backgroundYPositionAbsolute = false;
			binfo.backgroundYPosition = 50;			
		}
		else if("bottom".equalsIgnoreCase(yposition)) {
			binfo.backgroundYPositionAbsolute = false;
			binfo.backgroundYPosition = 100;			
		}
		else if("top".equalsIgnoreCase(yposition)) {
			binfo.backgroundYPositionAbsolute = false;
			binfo.backgroundYPosition = 0;			
		}
		else {
			binfo.backgroundYPositionAbsolute = true;
			binfo.backgroundYPosition = HtmlValues.getPixelSize(yposition, this, 0);
		}						
	}
	
	private void applyBackgroundPosition(BackgroundInfo binfo, String position) {
		StringTokenizer tok = new StringTokenizer(position, " \t\r\n");
		if(tok.hasMoreTokens()) {
			String xposition = tok.nextToken();
			this.applyBackgroundHorizontalPositon(binfo, xposition);
			if(tok.hasMoreTokens()) {
				String yposition = tok.nextToken();
				this.applyBackgroundVerticalPosition(binfo, yposition);
			}
		}
	}
	
	private void applyBackground(BackgroundInfo binfo, String background, CSSStyleDeclaration declaration) {
		String[] tokens = HtmlValues.splitCssValue(background);
		boolean hasXPosition = false;
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if(ColorFactory.getInstance().isColor(token)) {
				binfo.backgroundColor = ColorFactory.getInstance().getColor(token);
			}	
			else if(HtmlValues.isUrl(token)) {
				binfo.backgroundImage = HtmlValues.getURIFromStyleValue(token, declaration, this.document);
			}
			else if(isBackgroundRepeat(token)) {
				this.applyBackgroundRepeat(binfo, token);
			}
			else if(isBackgroundPosition(token)) {
				if(hasXPosition) {
					this.applyBackgroundVerticalPosition(binfo, token);
				}
				else {
					hasXPosition = true;
					this.applyBackgroundHorizontalPositon(binfo, token);
				}
			}
		}
	}

	private boolean isBackgroundPosition(String token) {
		return HtmlValues.isLength(token) ||
			token.endsWith("%") ||
			token.equalsIgnoreCase("top") ||
			token.equalsIgnoreCase("center") ||
			token.equalsIgnoreCase("bottom") ||
			token.equalsIgnoreCase("left") ||
			token.equalsIgnoreCase("right");			
	}
	
	private boolean isBackgroundRepeat(String repeat) {
		String repeatTL = repeat.toLowerCase();
		return repeatTL.indexOf("repeat") != -1;
	}

	private void applyBackgroundRepeat(BackgroundInfo binfo, String backgroundRepeatText) {
		String brtl = backgroundRepeatText.toLowerCase();
		if("repeat".equals(brtl)) {
			binfo.backgroundRepeat = BackgroundInfo.BR_REPEAT;
		}
		else if("repeat-x".equals(brtl)) {
			binfo.backgroundRepeat = BackgroundInfo.BR_REPEAT_X;
		}
		else if("repeat-y".equals(brtl)) {
			binfo.backgroundRepeat = BackgroundInfo.BR_REPEAT_Y;
		}
		else if("no-repeat".equals(brtl)) {
			binfo.backgroundRepeat = BackgroundInfo.BR_NO_REPEAT;
		}		
	}

	
	public String toString() {
		return "StyleSheetRenderState[font=" + this.getFont() + ",textDecoration=" + this.getTextDecorationMask() + "]";
	}
}
