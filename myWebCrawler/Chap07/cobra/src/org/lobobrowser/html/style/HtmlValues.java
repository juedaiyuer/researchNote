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
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.*;

import org.lobobrowser.util.gui.ColorFactory;
import org.w3c.dom.css.*;
import org.lobobrowser.util.*;
import org.lobobrowser.html.domimpl.*;

public class HtmlValues {
	private static final Map SYSTEM_FONTS = new HashMap();
	private static final Logger logger = Logger.getLogger(HtmlValues.class.getName());
	
	public static final float DEFAULT_FONT_SIZE = 14.0f;
	public static final int DEFAULT_FONT_SIZE_INT = 14;
	public static final Float DEFAULT_FONT_SIZE_BOX = new Float(14.0f);
	
	static {
		FontInfo systemFont = new FontInfo();
		SYSTEM_FONTS.put("caption", systemFont);
		SYSTEM_FONTS.put("icon", systemFont);
		SYSTEM_FONTS.put("menu", systemFont);
		SYSTEM_FONTS.put("message-box", systemFont);
		SYSTEM_FONTS.put("small-caption", systemFont);
		SYSTEM_FONTS.put("status-bar", systemFont);		
	}
	
	private HtmlValues() {
	}
	
	public static boolean isBorderStyle(String token) {
		return token.equalsIgnoreCase("solid") ||
			token.equalsIgnoreCase("double") ||
			token.equalsIgnoreCase("none") ||
			token.equalsIgnoreCase("dashed") ||
			token.equalsIgnoreCase("dotted") ||
			token.equalsIgnoreCase("hidden") ||
			token.equalsIgnoreCase("groove") ||
			token.equalsIgnoreCase("ridge") ||
			token.equalsIgnoreCase("inset") ||
			token.equalsIgnoreCase("outset");
	}
	
	public static Insets getMarginInsets(CSS2Properties cssProperties, RenderState renderState) {
		String shortcutText = cssProperties.getMargin();
		Insets insets = null;
		if(shortcutText != null && !"".equals(shortcutText)) {
			insets = getInsets(shortcutText, renderState, true);
		}
		String topText = cssProperties.getMarginTop();
		insets = updateTopInset(insets, topText, renderState);
		String leftText = cssProperties.getMarginLeft();
		insets = updateLeftInset(insets, leftText, renderState);
		String bottomText = cssProperties.getMarginBottom();
		insets = updateBottomInset(insets, bottomText, renderState);
		String rightText = cssProperties.getMarginRight();
		insets = updateRightInset(insets, rightText, renderState);
		return insets;
	}
	
	public static Insets getPaddingInsets(CSS2Properties cssProperties, RenderState renderState) {
		String shortcutText = cssProperties.getPadding();
		Insets insets = null;
		if(shortcutText != null && !"".equals(shortcutText)) {
			insets = getInsets(shortcutText, renderState, false);
		}
		String topText = cssProperties.getPaddingTop();
		insets = updateTopInset(insets, topText, renderState);
		String leftText = cssProperties.getPaddingLeft();
		insets = updateLeftInset(insets, leftText, renderState);
		String bottomText = cssProperties.getPaddingBottom();
		insets = updateBottomInset(insets, bottomText, renderState);
		String rightText = cssProperties.getPaddingRight();
		insets = updateRightInset(insets, rightText, renderState);
		return insets;
	}

	public static Insets getBorderInsets(Insets insets, CSS2Properties cssProperties, RenderState renderState) {
		String shortcutText = cssProperties.getBorderWidth();
		if(shortcutText != null && !"".equals(shortcutText)) {
			insets = getInsets(shortcutText, renderState, false);
		}
		String topText = cssProperties.getBorderTopWidth();
		insets = updateTopInset(insets, topText, renderState);
		String leftText = cssProperties.getBorderLeftWidth();
		insets = updateLeftInset(insets, leftText, renderState);
		String bottomText = cssProperties.getBorderBottomWidth();
		insets = updateBottomInset(insets, bottomText, renderState);
		String rightText = cssProperties.getBorderRightWidth();
		insets = updateRightInset(insets, rightText, renderState);
		return insets;
	}

	private static Insets updateTopInset(Insets insets, String sizeText, RenderState renderState) {
		if(sizeText == null || "".equals(sizeText)) {
			return insets;
		}
		if(insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}
		insets.top = getPixelSize(sizeText, renderState, 0);
		return insets;
	}

	private static Insets updateLeftInset(Insets insets, String sizeText, RenderState renderState) {
		if(sizeText == null || "".equals(sizeText)) {
			return insets;
		}
		if(insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}
		insets.left = getPixelSize(sizeText, renderState, 0);
		return insets;
	}

	private static Insets updateBottomInset(Insets insets, String sizeText, RenderState renderState) {
		if(sizeText == null || "".equals(sizeText)) {
			return insets;
		}
		if(insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}
		insets.bottom = getPixelSize(sizeText, renderState, 0);
		return insets;
	}

	private static Insets updateRightInset(Insets insets, String sizeText, RenderState renderState) {
		if(sizeText == null || "".equals(sizeText)) {
			return insets;
		}
		if(insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}
		insets.right = getPixelSize(sizeText, renderState, 0);
		return insets;
	}

	public static Insets getInsets(String insetsSpec, RenderState renderState, boolean negativeOK) {
		int[] insetsArray = new int[4];
		int size = 0;
		StringTokenizer tok = new StringTokenizer(insetsSpec);
		if(tok.hasMoreTokens()) {
			String token = tok.nextToken();
			insetsArray[0] = getPixelSize(token, renderState, 0);
			if(negativeOK || insetsArray[0] >= 0) {
				size = 1;
				if(tok.hasMoreTokens()) {
					token = tok.nextToken();
					insetsArray[1] = getPixelSize(token, renderState, 0);
					if(negativeOK || insetsArray[1] >= 0) {
						size = 2;
						if(tok.hasMoreTokens()) {
							token = tok.nextToken();
							insetsArray[2] = getPixelSize(token, renderState, 0);
							if(negativeOK || insetsArray[2] >= 0) {
								size = 3;
								if(tok.hasMoreTokens()) {
									token = tok.nextToken();
									insetsArray[3] = getPixelSize(token, renderState, 0);
									size = 4;
									if(negativeOK || insetsArray[3] >= 0) {
										// nop
									}
									else {
										insetsArray[3] = 0;
									}
								}
							}
							else {
								size = 4;
								insetsArray[2] = 0;
							}
						}
					}
					else {
						size = 4;
						insetsArray[1] = 0;
					}
				}
			}
			else {
				size = 1;
				insetsArray[0] = 0;
			}
		}
		if(size == 4) {
			return new Insets(insetsArray[0], insetsArray[3], insetsArray[2], insetsArray[1]);
		}
		else if (size == 1) {
			int val = insetsArray[0];
			return new Insets(val, val, val, val);
		}
		else if(size == 2) {
			return new Insets(insetsArray[0], insetsArray[1], insetsArray[0], insetsArray[1]);
		}
		else if(size == 3) {
			return new Insets(insetsArray[0], insetsArray[1], insetsArray[2], insetsArray[1]);
		}
		else {
			return null;
		}
	}

	/**
	 * Gets an array of top-left-bottom-right colors
	 * given a spec.
	 */
	public static Color[] getColors(String colorsSpec) {
		Color[] colorsArray = new Color[4];
		String[] colorStrings = HtmlValues.splitCssValue(colorsSpec);
		int size = colorStrings.length;
		if(size > 4) {
			size = 4;
		}
		for(int i = 0; i < size; i++) {
			colorsArray[i] = ColorFactory.getInstance().getColor(colorStrings[i]);
		}
		if(size == 4) {
			return colorsArray;
		}
		else if (size == 1) {
			Color color = colorsArray[0];
			colorsArray[1] = color;
			colorsArray[2] = color;
			colorsArray[3] = color;
			return colorsArray;
		}
		else if(size == 2) {
			colorsArray[2] = colorsArray[0];
			colorsArray[3] = colorsArray[1];
			return colorsArray;
		}
		else if(size == 3) {
			colorsArray[3] = colorsArray[1];
			return colorsArray;
		}
		else {
			throw new IllegalStateException("size=" + size);
		}
	}

	/**
	 * Gets a number for 1 to 7.
	 * @param oldHtmlSpec A number from 1 to 7 or +1, etc.
	 */
	public static final int getFontNumberOldStyle(String oldHtmlSpec, RenderState renderState) {
		oldHtmlSpec = oldHtmlSpec.trim();
		int tentative;
		try {
			if(oldHtmlSpec.startsWith("+")) {
				tentative = renderState.getFontBase() + Integer.parseInt(oldHtmlSpec.substring(1));
			}				
			else if(oldHtmlSpec.startsWith("-")) {
				tentative = renderState.getFontBase() + Integer.parseInt(oldHtmlSpec);
			}
			else {
				tentative = Integer.parseInt(oldHtmlSpec);
			}
			if(tentative < 1) {
				tentative = 1;
			}
			else if (tentative > 7) {
				tentative = 7;
			}
		} catch(NumberFormatException nfe) {
			// ignore
			tentative = 3;
		}
		return tentative;
	}
	
	public static final float getFontSize(int fontNumber) {
		switch(fontNumber) {
		case 1:
			return 10.0f;
		case 2:
			return 11.0f;
		case 3:
			return 13.0f;
		case 4:
			return 16.0f;
		case 5:
			return 21.0f;
		case 6:
			return 29.0f;
		case 7:
			return 42.0f;
		default:
			return 63.0f;
		}
	}

	public static final float getFontSize(String spec, RenderState parentRenderState) {
		String specTL = spec.toLowerCase();
		if(specTL.endsWith("pt") || specTL.endsWith("px") || specTL.endsWith("cm") || specTL.endsWith("pc") || specTL.endsWith("cm") || specTL.endsWith("mm") || specTL.endsWith("em") || specTL.endsWith("ex")) {
			return getPixelSize(spec, parentRenderState, DEFAULT_FONT_SIZE_INT);			
		}
		else if(specTL.endsWith("%")) {
			String value = specTL.substring(0, specTL.length() - 1);
			try {
				double valued = Double.parseDouble(value);
				double parentFontSize = parentRenderState == null ? 14.0 : parentRenderState.getFont().getSize();
				return (float) (parentFontSize * valued / 100.0);
			} catch(NumberFormatException nfe) {
				return DEFAULT_FONT_SIZE;
			}			
		}
		else if("small".equals(specTL)) {
			return 12.0f;
		}
		else if("medium".equals(specTL)) {
			return 14.0f;
		}
		else if("large".equals(specTL)) {
			return 20.0f;
		}
		else if("x-small".equals(specTL)) {
			return 11.0f;
		}
		else if("xx-small".equals(specTL)) {
			return 10.0f;
		}
		else if("x-large".equals(specTL)) {
			return 26.0f; 
		}
		else if("xx-large".equals(specTL)) {
			return 40.0f; 
		}
		else if("larger".equals(specTL)) {
			int parentFontSize = parentRenderState == null ? DEFAULT_FONT_SIZE_INT : parentRenderState.getFont().getSize();
			return parentFontSize * 1.2f;
		}
		else if("smaller".equals(specTL)) {
			int parentFontSize = parentRenderState == null ? DEFAULT_FONT_SIZE_INT : parentRenderState.getFont().getSize();
			return parentFontSize * 1.2f;			
		}
		else {
			return getPixelSize(spec, parentRenderState, DEFAULT_FONT_SIZE_INT);
		}
	}
	
	public static final int getPixelSize(String spec, RenderState renderState, int errorValue, int availSize) {
		if(spec.endsWith("%")) {
			String perText = spec.substring(0, spec.length() - 1);
			try {
				double val = Double.parseDouble(perText);
				return (int) Math.round(availSize * val / 100.0);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
		}
		else {
			return getPixelSize(spec, renderState, errorValue);
		}
	}

	public static final int getPixelSize(String spec, RenderState renderState, int errorValue) {
		String lcSpec = spec.toLowerCase();
		if(lcSpec.endsWith("pt")) {
			String valText = lcSpec.substring(0, lcSpec.length() - "pt".length());
			double val;
			try {
				val = Double.parseDouble(valText);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
			int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
			double inches = val / 72;
			return (int) Math.round(dpi * inches);			
		}
		else if(lcSpec.endsWith("px")) {
			String pxText = lcSpec.substring(0, lcSpec.length() - "px".length());
			try {
				return (int) Math.round(Double.parseDouble(pxText));
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
		}
		else if(lcSpec.endsWith("pc")) {
			String valText = lcSpec.substring(0, lcSpec.length() - "pc".length());
			double val;
			try {
				val = Double.parseDouble(valText);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
			int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
			double inches = val / 6;
			return (int) Math.round(dpi * inches);			
		}
		else if(lcSpec.endsWith("cm")) {
			String valText = lcSpec.substring(0, lcSpec.length() - "cm".length());
			double val;
			try {
				val = Double.parseDouble(valText);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
			int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
			double inches = val / 2.54;
			return (int) Math.round(dpi * inches);			
		}
		else if(lcSpec.endsWith("mm")) {
			String valText = lcSpec.substring(0, lcSpec.length() - "mm".length());
			double val;
			try {
				val = Double.parseDouble(valText);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
			int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
			double inches = val / 25.4;
			return (int) Math.round(dpi * inches);			
		}
		else if(lcSpec.endsWith("ex") && renderState != null) {
			FontMetrics fm = renderState.getFontMetrics();
			String valText = lcSpec.substring(0, lcSpec.length() - "ex".length());
			double val;
			try {
				val = Double.parseDouble(valText);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
			return (int) Math.round(fm.getAscent() * val);			
		}
		else if(lcSpec.endsWith("em") && renderState != null) {
			Font f = renderState.getFont();
			String valText = lcSpec.substring(0, lcSpec.length() - "ex".length());
			double val;
			try {
				val = Double.parseDouble(valText);
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
			return (int) Math.round(f.getSize() * val);			
		}
		else {
			String pxText = lcSpec;
			try {
				return (int) Math.round(Double.parseDouble(pxText));
			} catch(NumberFormatException nfe) {
				return errorValue;
			}
		}
	}
	
	public static int getOldSyntaxPixelSize(String spec, int availSize, int errorValue) {
		if(spec == null) {
			return errorValue;
		}
		spec = spec.trim();
		try {
			if(spec.endsWith("%")) {
				return availSize * Integer.parseInt(spec.substring(0, spec.length() - 1)) / 100;
			}
			else {
				return Integer.parseInt(spec);
			}
		} catch(NumberFormatException nfe) {
			return errorValue;
		}		
	}
	
	public static java.net.URL getURIFromStyleValue(String styleValue, CSSStyleDeclaration declaration, HTMLDocumentImpl document) {
		String baseHref = null;
		if(declaration != null) {
			CSSRule rule = declaration.getParentRule();
			if(rule != null) {
				CSSStyleSheet sheet = rule.getParentStyleSheet();
				if(sheet instanceof com.steadystate.css.dom.CSSStyleSheetImpl) {
					com.steadystate.css.dom.CSSStyleSheetImpl ssheet = (com.steadystate.css.dom.CSSStyleSheetImpl) sheet;
					baseHref = ssheet.getHref();
				}
			}
		}		
		String start = "url(";
		if(!styleValue.toLowerCase().startsWith(start)) {
			return null;
		}
		int startIdx = start.length();
		int closingIdx = styleValue.lastIndexOf(')');
		if(closingIdx == -1) {
			return null;
		}
		String quotedUri = styleValue.substring(startIdx, closingIdx);
		String tentativeUri = unquoteAndUnescape(quotedUri);		
		if(baseHref == null) {
			return document.getFullURL(tentativeUri);
		}
		else {
			java.net.URL styleUrl = document.getFullURL(baseHref);
			try {
				return Urls.createURL(styleUrl, tentativeUri);
			} catch(java.net.MalformedURLException mfu) {
				logger.log(Level.WARNING,"Unable to create URL for URI=[" + tentativeUri + "], with base=[" + baseHref + "].", mfu);
				return null;
			}
		}
	}
	
	public static String unquoteAndUnescape(String text) {
		StringBuffer result = new StringBuffer();
		int index = 0;
		int length = text.length();
		boolean escape = false;
		boolean single = false;
		if(index < length) {
			char ch = text.charAt(index);
			switch(ch) {
			case '\'':
				single = true;
				break;
			case '"':
				break;
			case '\\':
				escape = true;
				break;
			default:
				result.append(ch);
			}
			index++;
		}
		OUTER:
		for(; index < length; index++) {
			char ch = text.charAt(index);
			switch(ch) {
			case '\'':
				if(escape || !single) {
					escape = false;
					result.append(ch);
				}
				else {
					break OUTER;
				}
				break;
			case '"':
				if(escape || single) {
					escape = false;
					result.append(ch);
				}
				else {
					break OUTER;
				}
				break;
			case '\\':
				if(escape) {
					escape = false;
					result.append(ch);
				}
				else {
					escape = true;
				}
				break;
			default:
				if(escape) {
					escape = false;
					result.append('\\');
				}
				result.append(ch);
			}
		}
		return result.toString();
	}
	
	public static String getColorFromBackground(String background) {
		String[] backgroundParts = HtmlValues.splitCssValue(background);
		for(int i = 0; i < backgroundParts.length; i++) {
			String token = backgroundParts[i];
			if(ColorFactory.getInstance().isColor(token)) {
				return token;
			}
		}
		return null;
	}
	
	public static boolean isLength(String token) {
		if(token.endsWith("px") ||
			token.endsWith("pt") ||
			token.endsWith("pc") ||
			token.endsWith("cm") ||
			token.endsWith("mm") ||
			token.endsWith("ex") ||
			token.endsWith("em")) {
			return true;
		}
		try {
			Double.parseDouble(token);
			return true;
		} catch(NumberFormatException nfe) {
			return false;
		}
	}
	
	public static String[] splitCssValue(String cssValue) {
		ArrayList tokens = new ArrayList();
		int len = cssValue.length();
		int parenCount = 0;
		StringBuffer currentWord = null;
		for(int i = 0; i < len; i++) {
			char ch = cssValue.charAt(i);
			switch(ch) {
			case '(':
				parenCount++;
				if(currentWord == null) {
					currentWord = new StringBuffer();
				}
				currentWord.append(ch);
				break;
			case ')':
				parenCount--;
				if(currentWord == null) {
					currentWord = new StringBuffer();
				}
				currentWord.append(ch);
				break;
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				if(parenCount == 0) {
					tokens.add(currentWord.toString());
					currentWord = null;
					break;
				}
				else {
					// Fall through - no break
				}
			default:
				if(currentWord == null) {
					currentWord = new StringBuffer();
				}
				currentWord.append(ch);
				break;
			}
		}
		if(currentWord != null) {
			tokens.add(currentWord.toString());
		}
		return (String[]) tokens.toArray(new String[tokens.size()]);
	}
	
	public static boolean isUrl(String token) {
		return token.toLowerCase().startsWith("url(");
	}
	
	public static int getListStyleType(String token) {
		String tokenTL = token.toLowerCase();
		if("disc".equals(tokenTL)) {
			return ListStyle.TYPE_DISC;
		}
		else if("circle".equals(tokenTL)) {
			return ListStyle.TYPE_CIRCLE;
		}
		else if("square".equals(tokenTL)) {
			return ListStyle.TYPE_SQUARE;
		}
		else if("decimal".equals(tokenTL)) {
			return ListStyle.TYPE_DECIMAL;
		}
		else if("lower-alpha".equals(tokenTL) || "lower-latin".equals(tokenTL)) {
			return ListStyle.TYPE_LOWER_ALPHA;
		}
		else if("upper-alpha".equals(tokenTL) || "upper-latin".equals(tokenTL)) {
			return ListStyle.TYPE_UPPER_ALPHA;
		}
		else {
			//TODO: Many types missing here
			return ListStyle.TYPE_UNSET;
		}
	}
	
	public static int getListStyleTypeDeprecated(String token) {
		String tokenTL = token.toLowerCase();
		if("disc".equals(tokenTL)) {
			return ListStyle.TYPE_DISC;
		}
		else if("circle".equals(tokenTL)) {
			return ListStyle.TYPE_CIRCLE;
		}
		else if("square".equals(tokenTL)) {
			return ListStyle.TYPE_SQUARE;
		}
		else if("1".equals(tokenTL)) {
			return ListStyle.TYPE_DECIMAL;
		}
		else if("a".equals(tokenTL)) {
			return ListStyle.TYPE_LOWER_ALPHA;
		}
		else if("A".equals(tokenTL)) {
			return ListStyle.TYPE_UPPER_ALPHA;
		}
		else {
			//TODO: Missing i, I.
			return ListStyle.TYPE_UNSET;
		}
	}

	public static int getListStylePosition(String token) {
		String tokenTL = token.toLowerCase();
		if("inside".equals(tokenTL)) {
			return ListStyle.POSITION_INSIDE;
		}
		else if("outside".equals(tokenTL)) {
			return ListStyle.POSITION_OUTSIDE;
		}
		else {
			return ListStyle.POSITION_UNSET;
		}
	}

	public static ListStyle getListStyle(String listStyleText) {
		ListStyle listStyle = new ListStyle();
		String[] tokens = HtmlValues.splitCssValue(listStyleText);
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			int listStyleType = HtmlValues.getListStyleType(token);
			if(listStyleType != ListStyle.TYPE_UNSET) {
				listStyle.type = listStyleType;
			}
			else if(HtmlValues.isUrl(token)) {
				//TODO: listStyle.image
			}
			else {
				int listStylePosition = HtmlValues.getListStylePosition(token);
				if(listStylePosition != ListStyle.POSITION_UNSET) {
					listStyle.position = listStylePosition;
				}
			}
		}
		return listStyle;
	}
	
	private static boolean isFontStyle(String token) {
		return "italic".equals(token) || "normal".equals(token) || "oblique".equals(token);
	}
	
	private static boolean isFontVariant(String token) {
		return "small-caps".equals(token) || "normal".equals(token);
	}
	private static boolean isFontWeight(String token) {
		if("bold".equals(token) || "bolder".equals(token) || "lighter".equals(token)) {
			return true;
		}
		try {
			int value = Integer.parseInt(token);
			return (value % 100) == 0 && value >= 100 && value <= 900;
		} catch(NumberFormatException nfe) {
			return false;
		}
	}

	public static FontInfo createFontInfo(String fontSpec, RenderState parentRenderState) {
		String fontSpecTL = fontSpec.toLowerCase();
		FontInfo fontInfo = (FontInfo) SYSTEM_FONTS.get(fontSpecTL);
		if(fontInfo != null) {
			return fontInfo;
		}
		fontInfo = new FontInfo();
		String[] tokens = HtmlValues.splitCssValue(fontSpecTL);
		String token = null;
		int length = tokens.length;
		int i;
		for(i = 0; i < length; i++) {
 			token = tokens[i];
			if(isFontStyle(token)) {
				fontInfo.fontStyle = token;
				continue;
			}
			if(isFontVariant(token)) {
				fontInfo.fontVariant = token;
				continue;
			}
			if(isFontWeight(token)) {
				fontInfo.fontWeight = token;
				continue;
			}
			// Otherwise exit loop
			break;
		}
		if(token != null) {
			int slashIdx = token.indexOf('/');
			String fontSizeText = slashIdx == -1 ? token : token.substring(0, slashIdx);
			fontInfo.fontSize = new java.lang.Float(HtmlValues.getFontSize(fontSizeText, parentRenderState));
			//TODO: Line height
			if(++i < length) {
				StringBuffer fontFamilyBuff = new StringBuffer();
				do {
					token = tokens[i];
					fontFamilyBuff.append(token);
					fontFamilyBuff.append(' ');
				} while(++i < length);
				fontInfo.fontFamily = fontFamilyBuff.toString();
			}
		}
		return fontInfo;
	}

	
//	if(token != null) {
//		int slashIdx = token.indexOf('/');
//		String fontSizeText = slashIdx == -1 ? token : token.substring(0, slashIdx);
//		try {
//			fontSizeText = fontSizeText.toLowerCase();
//			todo();
//		} catch(Exception err) {
//			// ignore
//		}
//		
//		// The rest is the font family
//		if(hasMoreTokens) {
//			StringBuffer fontFamilyBuf = new StringBuffer();
//			while(tok.hasMoreTokens()) {	
//				fontFamilyBuf.append(tok.nextToken());
//				fontFamilyBuf.append(' ');
//			}
//			fontInfo.fontFamily = fontFamilyBuf.toString();
//		}
//	}	

}
