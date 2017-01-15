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
 * Created on Nov 20, 2005
 */
package org.lobobrowser.html.style;

import java.util.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSS2Properties;
import org.w3c.dom.css.CSSStyleDeclaration;

public class CSS2PropertiesImpl implements CSS2Properties {
	private final CSS2PropertiesContext context;
	private Object styleDeclarations = null;
	private CSS2PropertiesImpl localStyleProperties;
	private Map valueMap = null;

	public CSS2PropertiesImpl(CSS2PropertiesContext context) {
		this.context = context;
	}

	public void addStyleDeclaration(CSSStyleDeclaration styleDeclaration) {
		synchronized(this) {
			Object sd = this.styleDeclarations;
			if(sd == null) {
				this.styleDeclarations = styleDeclaration;
			}
			else if(sd instanceof Collection) {
				((Collection) sd).add(styleDeclaration);
			}
			else {
				//Note: Must be ArrayList, as assumed by getPropertyValue.
				Collection sdc = new ArrayList();
				sdc.add(sd);
				sdc.add(styleDeclaration);
				this.styleDeclarations = sdc;
			}
		}
	}

	public void setLocalStyleProperties(CSS2PropertiesImpl properties) {
		if(properties == this) {
			throw new IllegalStateException("setting same");
		}
		synchronized(this) {
			this.localStyleProperties = properties;
		}
	}
	
	public CSS2PropertiesImpl getLocalStyleProperties() {
		synchronized(this) {
			return this.localStyleProperties;
		}		
	}

	private final String getPropertyValue(String name) {
		String lowerCase = name.toLowerCase();
		return this.getPropertyValueLC(lowerCase);
	}

	private final String getPropertyValueLC(String lowerCaseName) {
		Map vm = this.valueMap;
		synchronized(this) {
			// Must check for local properties before caching value.
			CSS2PropertiesImpl localProps = this.localStyleProperties;
			if(localProps != null) {
				String value = localProps.getPropertyValueLC(lowerCaseName);
				if(value != null) {
					return value;
				}
			}
			if(vm != null) {
				if(vm.containsKey(lowerCaseName)) {
					return (String) vm.get(lowerCaseName);
				}
			}
			else {
				vm = new HashMap(1);
				this.valueMap = vm;
			}
			String declaredValue = this.getDeclaredPropertyValueImpl(lowerCaseName);
			vm.put(lowerCaseName, declaredValue);			
			return declaredValue;
		}		
	}

	private final void setPropertyValueLC(String lowerCaseName, String value) {
		Map vm = this.valueMap;
		synchronized(this) {
			if(vm == null) {
				vm = new HashMap(1);
				this.valueMap = vm;
			}
			vm.put(lowerCaseName, value);
		}
	}
	
	/**
	 * Should be called within a sychronized block.
	 * @param lowerCaseName
	 * @return
	 */
	private final String getDeclaredPropertyValueImpl(String lowerCaseName) {
		// Converts blanks to nulls.
		String resultValue;
		Object sd = this.styleDeclarations;
		if(sd instanceof CSSStyleDeclaration) {
			resultValue = ((CSSStyleDeclaration) sd).getPropertyValue(lowerCaseName);
			if(resultValue != null && resultValue.length() == 0) {
				resultValue = null;
			}
		}
		else if(sd instanceof ArrayList) {
			ArrayList sds = (ArrayList) sd;
			resultValue = null;
			int size = sds.size();
			// Reverse order is important here.
			for(int i = size; --i >= 0;) {
				Object styleObject = sds.get(i);
				if(styleObject instanceof CSSStyleDeclaration) {
					CSSStyleDeclaration styleDeclaration = (CSSStyleDeclaration) sds.get(i);
					String pv = styleDeclaration.getPropertyValue(lowerCaseName);
					if(pv != null && pv.length() != 0) {
						resultValue = pv;
						break;
					}
				}
			}
		}
		else {
			resultValue = null;
		}
		if(resultValue != null && "inherit".equalsIgnoreCase(resultValue)) {
			//TODO: When an ancestor is invalidated, how is the
			//"inherit" value recalculated?
			CSS2PropertiesImpl parent = this.context.getParentStyle();
			if(parent != null) {
				resultValue = parent.getPropertyValueLC(lowerCaseName);
			}
		}
		return resultValue;
	}

	public final CSSStyleDeclaration getStyleDeclaration(String lowerCaseName) {
		// Converts blanks to nulls.
		Object sd = this.styleDeclarations;
		if(sd instanceof CSSStyleDeclaration) {
			return (CSSStyleDeclaration) sd;
		}
		else if(sd instanceof ArrayList) {
			ArrayList sds = (ArrayList) sd;
			int size = sds.size();
			// Reverse order is important here.
			for(int i = size; --i >= 0;) {
				Object styleObject = sds.get(i);
				if(styleObject instanceof CSSStyleDeclaration) {
					CSSStyleDeclaration styleDeclaration = (CSSStyleDeclaration) sds.get(i);
					String pv = styleDeclaration.getPropertyValue(lowerCaseName);
					if(pv != null && pv.length() != 0) {
						return styleDeclaration;
					}
				}
			}
		}
		return null;
	}

	//---------- NonStandard properties
	
	private String overlayColor;
	
	public String getOverlayColor() {
		return this.overlayColor;
	}
	
	public void setOverlayColor(String value) {
		this.overlayColor = value;
		this.context.informLookInvalid();
	}

	public String getFloat() {
		return this.getPropertyValueLC("float");
	}

	public void setFloat(String value) {
		this.setPropertyValueLC("float", value);		
	}

	//---------- Implemented properties

	public String getAzimuth() {
		return this.getPropertyValueLC("azimuth");
	}

	public String getBackground() {
		return this.getPropertyValueLC("background");
	}

	public String getBackgroundAttachment() {
		return this.getPropertyValueLC("background-attachment");
	}

	public String getBackgroundColor() {
		return this.getPropertyValueLC("background-color");
	}

	public String getBackgroundImage() {
		return this.getPropertyValueLC("background-image");
	}

	public String getBackgroundPosition() {
		return this.getPropertyValueLC("background-position");
	}

	public String getBackgroundRepeat() {
		return this.getPropertyValueLC("background-repeat");
	}

	public String getBorder() {
		return this.getPropertyValueLC("border");
	}

	public String getBorderBottom() {
		return this.getPropertyValueLC("border-bottom");
	}

	public String getBorderBottomColor() {
		return this.getPropertyValueLC("border-bottom-color");
	}

	public String getBorderBottomStyle() {
		return this.getPropertyValueLC("bottom-style");
	}

	public String getBorderBottomWidth() {
		return this.getPropertyValueLC("bottom-width");
	}

	public String getBorderCollapse() {
		return this.getPropertyValueLC("border-collapse");
	}

	public String getBorderColor() {
		return this.getPropertyValueLC("border-color");
	}

	public String getBorderLeft() {
		return this.getPropertyValueLC("border-left");
	}

	public String getBorderLeftColor() {
		return this.getPropertyValueLC("border-left-color");
	}

	public String getBorderLeftStyle() {
		return this.getPropertyValueLC("border-left-style");
	}

	public String getBorderLeftWidth() {
		return this.getPropertyValueLC("border-left-width");
	}

	public String getBorderRight() {
		return this.getPropertyValueLC("");
	}

	public String getBorderRightColor() {
		return this.getPropertyValueLC("border-right-color");
	}

	public String getBorderRightStyle() {
		return this.getPropertyValueLC("border-right-style");
	}

	public String getBorderRightWidth() {
		return this.getPropertyValueLC("border-right-width");
	}

	public String getBorderSpacing() {
		return this.getPropertyValueLC("border-spacing");
	}

	public String getBorderStyle() {
		return this.getPropertyValueLC("border-style");
	}

	public String getBorderTop() {
		return this.getPropertyValueLC("border-top");
	}

	public String getBorderTopColor() {
		return this.getPropertyValueLC("border-top-color");
	}

	public String getBorderTopStyle() {
		return this.getPropertyValueLC("border-top-style");
	}

	public String getBorderTopWidth() {
		return this.getPropertyValueLC("border-top-width");
	}

	public String getBorderWidth() {
		return this.getPropertyValueLC("border-width");
	}

	public String getBottom() {
		return this.getPropertyValueLC("bottom");
	}

	public String getCaptionSide() {
		return this.getPropertyValueLC("caption-side");
	}

	public String getClear() {
		return this.getPropertyValueLC("clear");
	}

	public String getClip() {
		return this.getPropertyValueLC("clip");
	}

	public String getColor() {
		
		return this.getPropertyValueLC("color");
	}

	public String getContent() {
		
		return this.getPropertyValueLC("content");
	}

	public String getCounterIncrement() {
		
		return this.getPropertyValueLC("counter-increment");
	}

	public String getCounterReset() {
		
		return this.getPropertyValueLC("counter-reset");
	}

	public String getCssFloat() {
		
		return this.getPropertyValueLC("css-float");
	}

	public String getCue() {
		
		return this.getPropertyValueLC("cue");
	}

	public String getCueAfter() {
		
		return this.getPropertyValueLC("cue-after");
	}

	public String getCueBefore() {
		
		return this.getPropertyValueLC("cue-before");
	}

	public String getCursor() {
		
		return this.getPropertyValueLC("cursor");
	}

	public String getDirection() {
		
		return this.getPropertyValueLC("direction");
	}

	public String getDisplay() {
		
		return this.getPropertyValueLC("display");
	}

	public String getElevation() {
		
		return this.getPropertyValueLC("elevation");
	}

	public String getEmptyCells() {
		
		return this.getPropertyValueLC("empty-cells");
	}

	public String getFont() {
		
		return this.getPropertyValueLC("font");
	}

	public String getFontFamily() {
		
		return this.getPropertyValueLC("font-family");
	}

	public String getFontSize() {
		
		return this.getPropertyValueLC("font-size");
	}

	public String getFontSizeAdjust() {
		
		return this.getPropertyValueLC("font-size-adjust");
	}

	public String getFontStretch() {
		
		return this.getPropertyValueLC("font-stretch");
	}

	public String getFontStyle() {
		
		return this.getPropertyValueLC("font-style");
	}

	public String getFontVariant() {
		
		return this.getPropertyValueLC("font-variant");
	}

	public String getFontWeight() {
		
		return this.getPropertyValueLC("font-weight");
	}

	public String getHeight() {
		
		return this.getPropertyValueLC("height");
	}

	public String getLeft() {
		
		return this.getPropertyValueLC("left");
	}

	public String getLetterSpacing() {
		
		return this.getPropertyValueLC("letter-spacing");
	}

	public String getLineHeight() {
		
		return this.getPropertyValueLC("line-height");
	}

	public String getListStyle() {
		return this.getPropertyValueLC("list-style");
	}

	public String getListStyleImage() {
		return this.getPropertyValueLC("list-style-image");
	}

	public String getListStylePosition() {
		return this.getPropertyValueLC("list-style-position");
	}

	public String getListStyleType() {
		return this.getPropertyValueLC("list-style-type");
	}

	public String getMargin() {
		
		return this.getPropertyValueLC("margin");
	}

	public String getMarginBottom() {
		
		return this.getPropertyValueLC("margin-bottom");
	}

	public String getMarginLeft() {
		
		return this.getPropertyValueLC("margin-left");
	}

	public String getMarginRight() {
		
		return this.getPropertyValueLC("margin-right");
	}

	public String getMarginTop() {
		
		return this.getPropertyValueLC("margin-top");
	}

	public String getMarkerOffset() {
		
		return this.getPropertyValueLC("marker-offset");
	}

	public String getMarks() {
		
		return this.getPropertyValueLC("marks");
	}

	public String getMaxHeight() {
		
		return this.getPropertyValueLC("max-height");
	}

	public String getMaxWidth() {
		
		return this.getPropertyValueLC("max-width");
	}

	public String getMinHeight() {
		
		return this.getPropertyValueLC("min-height");
	}

	public String getMinWidth() {
		
		return this.getPropertyValueLC("min-width");
	}

	public String getOrphans() {
		
		return this.getPropertyValueLC("orphans");
	}

	public String getOutline() {
		
		return this.getPropertyValueLC("outline");
	}

	public String getOutlineColor() {
		
		return this.getPropertyValueLC("outline-color");
	}

	public String getOutlineStyle() {
		
		return this.getPropertyValueLC("outline-style");
	}

	public String getOutlineWidth() {
		
		return this.getPropertyValueLC("outline-width");
	}

	public String getOverflow() {
		
		return this.getPropertyValueLC("overflow");
	}

	public String getPadding() {
		
		return this.getPropertyValueLC("padding");
	}

	public String getPaddingBottom() {
		
		return this.getPropertyValueLC("padding-bottom");
	}

	public String getPaddingLeft() {
		
		return this.getPropertyValueLC("padding-left");
	}

	public String getPaddingRight() {
		
		return this.getPropertyValueLC("padding-right");
	}

	public String getPaddingTop() {
		
		return this.getPropertyValueLC("padding-top");
	}

	public String getPage() {
		
		return this.getPropertyValueLC("page");
	}

	public String getPageBreakAfter() {
		
		return this.getPropertyValueLC("page-break-after");
	}

	public String getPageBreakBefore() {
		
		return this.getPropertyValueLC("page-break-before");
	}

	public String getPageBreakInside() {
		
		return this.getPropertyValueLC("page-break-inside");
	}

	public String getPause() {
		
		return this.getPropertyValueLC("pause");
	}

	public String getPauseAfter() {
		
		return this.getPropertyValueLC("pause-after");
	}

	public String getPauseBefore() {
		
		return this.getPropertyValueLC("pause-before");
	}

	public String getPitch() {
		
		return this.getPropertyValueLC("pitch");
	}

	public String getPitchRange() {
		
		return this.getPropertyValueLC("pitch-range");
	}

	public String getPlayDuring() {
		
		return this.getPropertyValueLC("play-during");
	}

	public String getPosition() {
		
		return this.getPropertyValueLC("position");
	}

	public String getQuotes() {
		
		return this.getPropertyValueLC("quotes");
	}

	public String getRichness() {
		
		return this.getPropertyValueLC("richness");
	}

	public String getRight() {
		
		return this.getPropertyValueLC("right");
	}

	public String getSize() {
		
		return this.getPropertyValueLC("size");
	}

	public String getSpeak() {
		
		return this.getPropertyValueLC("speak");
	}

	public String getSpeakHeader() {
		
		return this.getPropertyValueLC("speak-header");
	}

	public String getSpeakNumeral() {
		
		return this.getPropertyValueLC("speak-numeral");
	}

	public String getSpeakPunctuation() {
		
		return this.getPropertyValueLC("speak-punctuation");
	}

	public String getSpeechRate() {
		
		return this.getPropertyValueLC("speech-rate");
	}

	public String getStress() {
		
		return this.getPropertyValueLC("stress");
	}

	public String getTableLayout() {
		
		return this.getPropertyValueLC("table-layout");
	}

	public String getTextAlign() {		
		return this.getPropertyValueLC("text-align");
	}

	public String getTextDecoration() {
		
		return this.getPropertyValueLC("text-decoration");
	}

	public String getTextIndent() {
		
		return this.getPropertyValueLC("text-indent");
	}

	public String getTextShadow() {
		
		return this.getPropertyValueLC("text-shadow");
	}

	public String getTextTransform() {
		
		return this.getPropertyValueLC("text-transform");
	}

	public String getTop() {
		
		return this.getPropertyValueLC("top");
	}

	public String getUnicodeBidi() {
		
		return this.getPropertyValueLC("unicode-bidi");
	}

	public String getVerticalAlign() {
		
		return this.getPropertyValueLC("vertical-align");
	}

	public String getVisibility() {
		
		return this.getPropertyValueLC("visibility");
	}

	public String getVoiceFamily() {
		
		return this.getPropertyValueLC("voice-family");
	}

	public String getVolume() {
		
		return this.getPropertyValueLC("volume");
	}

	public String getWhiteSpace() {
		
		return this.getPropertyValueLC("white-space");
	}

	public String getWidows() {
		
		return this.getPropertyValueLC("widows");
	}

	public String getWidth() {
		
		return this.getPropertyValueLC("width");
	}

	public String getWordSpacing() {
		
		return this.getPropertyValueLC("word-spacing");
	}

	public String getZIndex() {
		
		return this.getPropertyValueLC("z-index");
	}

	public void setAzimuth(String azimuth) throws DOMException {
		this.setPropertyValueLC("azimuth", azimuth);
	}

	public void setBackground(String background) throws DOMException {
		this.setPropertyValueLC("background", background);
		this.context.informLookInvalid();
	}

	public void setBackgroundAttachment(String backgroundAttachment) throws DOMException {
		this.setPropertyValueLC("background-attachment", backgroundAttachment);
		this.context.informLookInvalid();
	}

	public void setBackgroundColor(String backgroundColor) throws DOMException {
		this.setPropertyValueLC("background-color", backgroundColor);
		this.context.informLookInvalid();
	}

	public void setBackgroundImage(String backgroundImage) throws DOMException {
		this.setPropertyValueLC("background-image", backgroundImage);
		this.context.informLookInvalid();
	}

	public void setBackgroundPosition(String backgroundPosition) throws DOMException {
		this.setPropertyValueLC("background-position", backgroundPosition);
		this.context.informLookInvalid();
	}

	public void setBackgroundRepeat(String backgroundRepeat) throws DOMException {
		this.setPropertyValueLC("background-repeat", backgroundRepeat);
		this.context.informLookInvalid();
	}

	public void setBorder(String border) throws DOMException {
		this.setPropertyValueLC("border", border);
		this.context.informInvalid();
	}

	public void setBorderBottom(String borderBottom) throws DOMException {
		this.setPropertyValueLC("border-bottom", borderBottom);
	}

	public void setBorderBottomColor(String borderBottomColor) throws DOMException {
		this.setPropertyValueLC("border-bottom-color", borderBottomColor);
		this.context.informLookInvalid();
		this.context.informInvalid();
	}

	public void setBorderBottomStyle(String borderBottomStyle) throws DOMException {
		this.setPropertyValueLC("border-bottom-style", borderBottomStyle);
		this.context.informLookInvalid();
	}

	public void setBorderBottomWidth(String borderBottomWidth) throws DOMException {
		this.setPropertyValueLC("border-bottom-width", borderBottomWidth);
		this.context.informInvalid();
	}

	public void setBorderCollapse(String borderCollapse) throws DOMException {
		this.setPropertyValueLC("border-collapse", borderCollapse);
		this.context.informInvalid();
	}

	public void setBorderColor(String borderColor) throws DOMException {
		this.setPropertyValueLC("border-color", borderColor);
		this.context.informLookInvalid();
	}

	public void setBorderLeft(String borderLeft) throws DOMException {
		this.setPropertyValueLC("border-left", borderLeft);
		this.context.informInvalid();
	}

	public void setBorderLeftColor(String borderLeftColor) throws DOMException {
		this.setPropertyValueLC("border-left-color", borderLeftColor);
		this.context.informLookInvalid();
	}

	public void setBorderLeftStyle(String borderLeftStyle) throws DOMException {
		this.setPropertyValueLC("border-left-style", borderLeftStyle);
		this.context.informLookInvalid();
	}

	public void setBorderLeftWidth(String borderLeftWidth) throws DOMException {
		this.setPropertyValueLC("border-left-width", borderLeftWidth);
		this.context.informInvalid();
	}

	public void setBorderRight(String borderRight) throws DOMException {
		this.setPropertyValueLC("border-right", borderRight);
		this.context.informInvalid();
	}

	public void setBorderRightColor(String borderRightColor) throws DOMException {
		this.setPropertyValueLC("border-right-color", borderRightColor);
		this.context.informLookInvalid();
	}

	public void setBorderRightStyle(String borderRightStyle) throws DOMException {
		this.setPropertyValueLC("border-right-style", borderRightStyle);
		this.context.informLookInvalid();
	}

	public void setBorderRightWidth(String borderRightWidth) throws DOMException {
		this.setPropertyValueLC("border-right-width", borderRightWidth);
		this.context.informInvalid();
	}

	public void setBorderSpacing(String borderSpacing) throws DOMException {
		this.setPropertyValueLC("border-spacing", borderSpacing);
		this.context.informInvalid();
	}

	public void setBorderStyle(String borderStyle) throws DOMException {
		this.setPropertyValueLC("border-style", borderStyle);
		this.context.informLookInvalid();
	}

	public void setBorderTop(String borderTop) throws DOMException {
		this.setPropertyValueLC("border-top", borderTop);
		this.context.informInvalid();
	}

	public void setBorderTopColor(String borderTopColor) throws DOMException {
		this.setPropertyValueLC("border-top-color", borderTopColor);
		this.context.informLookInvalid();
	}

	public void setBorderTopStyle(String borderTopStyle) throws DOMException {
		this.setPropertyValueLC("border-top-style", borderTopStyle);
		this.context.informLookInvalid();
	}

	public void setBorderTopWidth(String borderTopWidth) throws DOMException {
		this.setPropertyValueLC("border-top-width", borderTopWidth);
		this.context.informInvalid();
	}

	public void setBorderWidth(String borderWidth) throws DOMException {
		this.setPropertyValueLC("border-width", borderWidth);
		this.context.informInvalid();
	}

	public void setBottom(String bottom) throws DOMException {
		this.setPropertyValueLC("bottom", bottom);
		this.context.informPositionInvalid();
	}

	public void setCaptionSide(String captionSide) throws DOMException {
		this.setPropertyValueLC("captionSide", captionSide);
	}

	public void setClear(String clear) throws DOMException {
		this.setPropertyValueLC("clear", clear);
		this.context.informInvalid();
	}

	public void setClip(String clip) throws DOMException {
		this.setPropertyValueLC("clip", clip);
	}

	public void setColor(String color) throws DOMException {
		this.setPropertyValueLC("color", color);
		this.context.informLookInvalid();
	}

	public void setContent(String content) throws DOMException {
		this.setPropertyValueLC("content", content);
		this.context.informInvalid();
	}

	public void setCounterIncrement(String counterIncrement) throws DOMException {
		this.setPropertyValueLC("counter-increment", counterIncrement);
		this.context.informLookInvalid();
	}

	public void setCounterReset(String counterReset) throws DOMException {
		this.setPropertyValueLC("counter-reset", counterReset);
		this.context.informLookInvalid();
	}

	public void setCssFloat(String cssFloat) throws DOMException {
		this.setPropertyValueLC("css-float", cssFloat);
		this.context.informInvalid();
	}

	public void setCue(String cue) throws DOMException {
		this.setPropertyValueLC("cue", cue);
	}

	public void setCueAfter(String cueAfter) throws DOMException {
		this.setPropertyValueLC("cue-after", cueAfter);
	}

	public void setCueBefore(String cueBefore) throws DOMException {
		this.setPropertyValueLC("cue-before", cueBefore);
	}

	public void setCursor(String cursor) throws DOMException {
		this.setPropertyValueLC("cursor", cursor);
		this.context.informLookInvalid();
	}

	public void setDirection(String direction) throws DOMException {
		this.setPropertyValueLC("direction", direction);
		this.context.informInvalid();
	}

	public void setDisplay(String display) throws DOMException {
		this.setPropertyValueLC("display", display);
		this.context.informInvalid();
	}

	public void setElevation(String elevation) throws DOMException {
		this.setPropertyValueLC("elevation", elevation);
		this.context.informInvalid();
	}

	public void setEmptyCells(String emptyCells) throws DOMException {
		this.setPropertyValueLC("empty-cells", emptyCells);
	}

	public void setFont(String font) throws DOMException {
		this.setPropertyValueLC("font", font);
		this.context.informInvalid();
	}

	public void setFontFamily(String fontFamily) throws DOMException {
		this.setPropertyValueLC("font-family", fontFamily);
		this.context.informInvalid();
	}

	public void setFontSize(String fontSize) throws DOMException {
		this.setPropertyValueLC("font-size", fontSize);
		this.context.informInvalid();
	}

	public void setFontSizeAdjust(String fontSizeAdjust) throws DOMException {
		this.setPropertyValueLC("font-size-adjust", fontSizeAdjust);
		this.context.informInvalid();
	}

	public void setFontStretch(String fontStretch) throws DOMException {
		this.setPropertyValueLC("font-stretch", fontStretch);
		this.context.informInvalid();
	}

	public void setFontStyle(String fontStyle) throws DOMException {
		this.setPropertyValueLC("font-style", fontStyle);
		this.context.informInvalid();
	}

	public void setFontVariant(String fontVariant) throws DOMException {
		this.setPropertyValueLC("font-variant", fontVariant);
		this.context.informInvalid();
	}

	public void setFontWeight(String fontWeight) throws DOMException {
		this.setPropertyValueLC("font-weight", fontWeight);
		this.context.informInvalid();
	}

	public void setHeight(String height) throws DOMException {
		this.setPropertyValueLC("height", height);
		this.context.informSizeInvalid();
	}

	public void setLeft(String left) throws DOMException {
		this.setPropertyValueLC("left", left);
		this.context.informPositionInvalid();
	}

	public void setLetterSpacing(String letterSpacing) throws DOMException {
		this.setPropertyValueLC("letter-spacing", letterSpacing);
		this.context.informInvalid();
	}

	public void setLineHeight(String lineHeight) throws DOMException {
		this.setPropertyValueLC("line-height", lineHeight);
		this.context.informInvalid();
	}

	public void setListStyle(String listStyle) throws DOMException {
		this.setPropertyValueLC("list-style", listStyle);
		this.context.informInvalid();
	}

	public void setListStyleImage(String listStyleImage) throws DOMException {
		this.setPropertyValueLC("list-style-image", listStyleImage);
		this.context.informLookInvalid();
	}

	public void setListStylePosition(String listStylePosition) throws DOMException {
		this.setPropertyValueLC("list-style-position", listStylePosition);
		this.context.informInvalid();
	}

	public void setListStyleType(String listStyleType) throws DOMException {
		this.setPropertyValueLC("list-style-type", listStyleType);
		this.context.informLookInvalid();
	}

	public void setMargin(String margin) throws DOMException {
		this.setPropertyValueLC("margin", margin);
		this.context.informInvalid();
	}

	public void setMarginBottom(String marginBottom) throws DOMException {
		this.setPropertyValueLC("margin-bottom", marginBottom);
		this.context.informInvalid();
	}

	public void setMarginLeft(String marginLeft) throws DOMException {
		this.setPropertyValueLC("margin-left", marginLeft);
		this.context.informInvalid();
	}

	public void setMarginRight(String marginRight) throws DOMException {
		this.setPropertyValueLC("margin-right", marginRight);
		this.context.informInvalid();
	}

	public void setMarginTop(String marginTop) throws DOMException {
		this.setPropertyValueLC("margin-top", marginTop);
		this.context.informInvalid();
	}

	public void setMarkerOffset(String markerOffset) throws DOMException {
		this.setPropertyValueLC("marker-offset", markerOffset);
	}

	public void setMarks(String marks) throws DOMException {
		this.setPropertyValueLC("marks", marks);
	}

	public void setMaxHeight(String maxHeight) throws DOMException {
		this.setPropertyValueLC("max-height", maxHeight);
		this.context.informSizeInvalid();
	}

	public void setMaxWidth(String maxWidth) throws DOMException {
		this.setPropertyValueLC("max-width", maxWidth);
		this.context.informSizeInvalid();
	}

	public void setMinHeight(String minHeight) throws DOMException {
		this.setPropertyValueLC("min-height", minHeight);
		this.context.informSizeInvalid();
	}

	public void setMinWidth(String minWidth) throws DOMException {
		this.setPropertyValueLC("min-width", minWidth);
		this.context.informSizeInvalid();
	}

	public void setOrphans(String orphans) throws DOMException {
		this.setPropertyValueLC("orphans", orphans);
	}

	public void setOutline(String outline) throws DOMException {
		this.setPropertyValueLC("outline", outline);
		this.context.informInvalid();
	}

	public void setOutlineColor(String outlineColor) throws DOMException {
		this.setPropertyValueLC("outline-color", outlineColor);
		this.context.informLookInvalid();
	}

	public void setOutlineStyle(String outlineStyle) throws DOMException {
		this.setPropertyValueLC("outline-style", outlineStyle);
		this.context.informLookInvalid();
	}

	public void setOutlineWidth(String outlineWidth) throws DOMException {
		this.setPropertyValueLC("outline-width", outlineWidth);
		this.context.informInvalid();
	}

	public void setOverflow(String overflow) throws DOMException {
		this.setPropertyValueLC("overflow", overflow);
		this.context.informInvalid();
	}

	public void setPadding(String padding) throws DOMException {
		this.setPropertyValueLC("padding", padding);
		this.context.informInvalid();
	}

	public void setPaddingBottom(String paddingBottom) throws DOMException {
		this.setPropertyValueLC("padding-bottom", paddingBottom);
		this.context.informInvalid();
	}

	public void setPaddingLeft(String paddingLeft) throws DOMException {
		this.setPropertyValueLC("padding-left", paddingLeft);
		this.context.informInvalid();
	}

	public void setPaddingRight(String paddingRight) throws DOMException {
		this.setPropertyValueLC("padding-right", paddingRight);
		this.context.informInvalid();
	}

	public void setPaddingTop(String paddingTop) throws DOMException {
		this.setPropertyValueLC("padding-top", paddingTop);
		this.context.informInvalid();
	}

	public void setPage(String page) throws DOMException {
		this.setPropertyValueLC("page", page);
	}

	public void setPageBreakAfter(String pageBreakAfter) throws DOMException {
		this.setPropertyValueLC("page-break-after", pageBreakAfter);
		this.context.informInvalid();
	}

	public void setPageBreakBefore(String pageBreakBefore) throws DOMException {
		this.setPropertyValueLC("page-break-before", pageBreakBefore);
		this.context.informInvalid();
	}

	public void setPageBreakInside(String pageBreakInside) throws DOMException {
		this.setPropertyValueLC("page-break-inside", pageBreakInside);
		this.context.informInvalid();
	}

	public void setPause(String pause) throws DOMException {
		this.setPropertyValueLC("pause", pause);
	}

	public void setPauseAfter(String pauseAfter) throws DOMException {
		this.setPropertyValueLC("pause-after", pauseAfter);
	}

	public void setPauseBefore(String pauseBefore) throws DOMException {
		this.setPropertyValueLC("pause-before", pauseBefore);
	}

	public void setPitch(String pitch) throws DOMException {
		this.setPropertyValueLC("pitch", pitch);
	}

	public void setPitchRange(String pitchRange) throws DOMException {
		this.setPropertyValueLC("pitch-range", pitchRange);
	}

	public void setPlayDuring(String playDuring) throws DOMException {
		this.setPropertyValueLC("play-during", playDuring);
	}

	public void setPosition(String position) throws DOMException {
		this.setPropertyValueLC("position", position);
		this.context.informPositionInvalid();
	}

	public void setQuotes(String quotes) throws DOMException {
		this.setPropertyValueLC("qutes", quotes);
	}

	public void setRichness(String richness) throws DOMException {
		this.setPropertyValueLC("richness", richness);
	}

	public void setRight(String right) throws DOMException {
		this.setPropertyValueLC("right", right);
		this.context.informPositionInvalid();
	}

	public void setSize(String size) throws DOMException {
		this.setPropertyValueLC("size", size);
		this.context.informInvalid();
	}

	public void setSpeak(String speak) throws DOMException {
		this.setPropertyValueLC("speak", speak);
	}

	public void setSpeakHeader(String speakHeader) throws DOMException {
		this.setPropertyValueLC("speak-header", speakHeader);
	}

	public void setSpeakNumeral(String speakNumeral) throws DOMException {
		this.setPropertyValueLC("speak-numeral", speakNumeral);
	}

	public void setSpeakPunctuation(String speakPunctuation) throws DOMException {
		this.setPropertyValueLC("speak-punctuation", speakPunctuation);
	}

	public void setSpeechRate(String speechRate) throws DOMException {
		this.setPropertyValueLC("speech-rate", speechRate);
	}

	public void setStress(String stress) throws DOMException {
		this.setPropertyValueLC("stress", stress);
	}

	public void setTableLayout(String tableLayout) throws DOMException {
		this.setPropertyValueLC("table-layout", tableLayout);
		this.context.informInvalid();
	}

	public void setTextAlign(String textAlign) throws DOMException {
		this.setPropertyValueLC("text-align", textAlign);
		this.context.informLayoutInvalid();
	}

	public void setTextDecoration(String textDecoration) throws DOMException {
		this.setPropertyValueLC("text-decoration", textDecoration);
		this.context.informLookInvalid();
	}

	public void setTextIndent(String textIndent) throws DOMException {
		this.setPropertyValueLC("text-indent", textIndent);
		this.context.informLayoutInvalid();
	}

	public void setTextShadow(String textShadow) throws DOMException {
		this.setPropertyValueLC("text-shadow", textShadow);
		this.context.informLookInvalid();
	}

	public void setTextTransform(String textTransform) throws DOMException {
		this.setPropertyValueLC("text-transform", textTransform);
		this.context.informInvalid();
	}

	public void setTop(String top) throws DOMException {
		this.setPropertyValueLC("top", top);
		this.context.informPositionInvalid();
	}

	public void setUnicodeBidi(String unicodeBidi) throws DOMException {
		this.setPropertyValueLC("unicode-bidi", unicodeBidi);
		this.context.informInvalid();
	}

	public void setVerticalAlign(String verticalAlign) throws DOMException {
		this.setPropertyValueLC("vertical-align", verticalAlign);
		this.context.informInvalid();
	}

	public void setVisibility(String visibility) throws DOMException {
		this.setPropertyValueLC("visibility", visibility);
	}

	public void setVoiceFamily(String voiceFamily) throws DOMException {
		this.setPropertyValueLC("voice-family", voiceFamily);
	}

	public void setVolume(String volume) throws DOMException {
		this.setPropertyValueLC("volue", volume);
	}

	public void setWhiteSpace(String whiteSpace) throws DOMException {
		this.setPropertyValueLC("white-space", whiteSpace);
		this.context.informInvalid();
	}

	public void setWidows(String widows) throws DOMException {
		this.setPropertyValueLC("widows", widows);
	}

	public void setWidth(String width) throws DOMException {
		this.setPropertyValueLC("width", width);
		this.context.informSizeInvalid();
	}

	public void setWordSpacing(String wordSpacing) throws DOMException {
		this.setPropertyValueLC("word-spacing", wordSpacing);
		this.context.informInvalid();
	}

	public void setZIndex(String zIndex) throws DOMException {
		this.setPropertyValueLC("z-index", zIndex);
		this.context.informPositionInvalid();
	}
	
	public String toString() {
		return "CSS2PropertiesImpl[hash=" + this.hashCode() + ",textDecoration=" + this.getPropertyValue("text-decoration") + "]";
	}
}
