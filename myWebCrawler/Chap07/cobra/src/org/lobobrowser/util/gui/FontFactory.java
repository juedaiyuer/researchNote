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
 * Created on Apr 17, 2005
 */
package org.lobobrowser.util.gui;

import java.util.*;
import java.awt.*;
import java.util.logging.*;

import org.lobobrowser.util.Objects;

/**
 * @author J. H. S.
 */
public class FontFactory {
	private static final Logger logger = Logger.getLogger(FontFactory.class.getName());
	private static final boolean loggableInfo = logger.isLoggable(Level.INFO);
	private static final FontFactory instance = new FontFactory();
	private final Set fontFamilies = new HashSet();
	private final Map fontMap = new HashMap();
	
	/**
	 * 
	 */
	private FontFactory() {
		boolean liflag = loggableInfo;
		String[] ffns = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		Set fontFamilies = this.fontFamilies;
		synchronized(this) {
			for(int i = 0; i < ffns.length; i++) {
				if(liflag) {
					logger.info("FontFactory(): family=" + ffns[i]);
				}
				fontFamilies.add(ffns[i].toLowerCase());
			}
		}
	}
	
	public static final FontFactory getInstance() {
		return instance;
	}

	private final Map registeredFonts = new HashMap(0);
	
	/**
	 * Registers a font family. It does not close the stream provided.
	 * Fonts should be registered before the renderer has a chance to
	 * cache document font specifications.
	 * @param fontName The name of a font as it would appear in a font-family specification.
	 * @param fontFormat Should be {@link Font#TRUETYPE_FONT}.
	 */
	public void registerFont(String fontName, int fontFormat, java.io.InputStream fontStream) throws java.awt.FontFormatException, java.io.IOException {
		Font f = Font.createFont(fontFormat, fontStream);
		synchronized(this) {
			this.registeredFonts.put(fontName.toLowerCase(), f);
		}
	}
	
	/**
	 * Unregisters a font previously registered with {@link #registerFont(String, int, java.io.InputStream)}.
	 * @param fontName The font name to be removed.
	 */
	public void unregisterFont(String fontName) {
		synchronized(this) {
			this.registeredFonts.remove(fontName.toLowerCase());
		}
	}
	
	public Font getFont(String fontFamily, String fontStyle, String fontVariant, String fontWeight, float fontSize) {
		FontKey key = new FontKey(fontFamily, fontStyle, fontVariant, fontWeight, fontSize);
		synchronized(this) {
			Font font = (Font) this.fontMap.get(key);
			if(font == null) {
				font = this.createFont(key);
				this.fontMap.put(key, font);
			}
			return font;
		}
	}
	
	private final Font createFont(FontKey key) {
		String fontNames = key.fontFamily;
		String fontFam = null;
		Set fontFamilies = this.fontFamilies;
		Map registeredFonts = this.registeredFonts; 
		Font baseFont = null;
		if(fontNames != null) {
			StringTokenizer tok = new StringTokenizer(fontNames, ",");
			while(tok.hasMoreTokens()) {
				fontFam = tok.nextToken().trim();
				String fontFamTL = fontFam.toLowerCase();
				if(registeredFonts.containsKey(fontFamTL)) {
					baseFont = (Font) registeredFonts.get(fontFamTL);
					break;
				}
				else if(fontFamilies.contains(fontFamTL)) {
					break;
				}
			}
		}
		int fontStyle = Font.PLAIN;
		if("italic".equalsIgnoreCase(key.fontStyle)) {
			fontStyle |= Font.ITALIC;
		}
		if("bold".equalsIgnoreCase(key.fontWeight) || "bolder".equalsIgnoreCase(key.fontWeight)) {
			fontStyle |= Font.BOLD;
		}		
		if(baseFont != null) {
			return baseFont.deriveFont(fontStyle, key.fontSize);
		}
		else {
			return new Font(fontFam, fontStyle,(int) Math.round(key.fontSize));
		}
	}
	
	private static class FontKey {
		public final String fontFamily;
		public final String fontStyle; 
		public final String fontVariant; 
		public final String fontWeight; 
		public final float fontSize;
		
		
		/**
		 * @param fontFamily
		 * @param fontStyle
		 * @param fontVariant
		 * @param fontWeight
		 * @param fontSize
		 */
		public FontKey(final String fontFamily, final String fontStyle,
				final String fontVariant, final String fontWeight,
				final float fontSize) {
			this.fontFamily = fontFamily;
			this.fontStyle = fontStyle;
			this.fontVariant = fontVariant;
			this.fontWeight = fontWeight;
			this.fontSize = fontSize;
		}
		
		public boolean equals(Object other) {
			if(!(other instanceof FontKey)) {
				return false;
			}
			FontKey ors = (FontKey) other;
			// fontSize is primitive
			return this.fontSize == ors.fontSize &&
				   Objects.equals(this.fontFamily,ors.fontFamily) &&
				   Objects.equals(this.fontStyle,ors.fontStyle) &&
				   Objects.equals(this.fontWeight,ors.fontWeight) &&
				   Objects.equals(this.fontVariant,ors.fontVariant);
		}
		
		public int hashCode() {
			String ff = this.fontFamily;
			if(ff == null) {
				ff = "";
			}
			String fw = this.fontWeight;
			if(fw == null) {
				fw = "";
			}
			String fs = this.fontStyle;
			if(fs == null) {
				fs = "";
			}
			return ff.hashCode() ^ 
				   fw.hashCode() ^ 
				   fs.hashCode() ^
				   (int) this.fontSize;
		}
		
		public String toString() {
			return "FontKey[family=" + this.fontFamily + ",size=" + this.fontSize + ",style=" + this.fontStyle + ",weight=" + this.fontWeight + ",variant=" + this.fontVariant + "]";
		}
	}
}