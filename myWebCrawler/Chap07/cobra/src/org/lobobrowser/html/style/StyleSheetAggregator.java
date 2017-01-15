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

import java.net.MalformedURLException;
import java.util.*;

import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.w3c.dom.css.*;
import org.w3c.dom.stylesheets.*;

/**
 * Aggregates all style sheets in a document.
 * Every time a new STYLE element is found, it is
 * added to the style sheet aggreagator by means
 * of the {@link #addStyleSheet(CSSStyleSheet)} method.
 * HTML elements have a <code>style</code> object
 * that has a list of <code>CSSStyleDeclaration</code>
 * instances. The instances inserted in that list
 * are obtained by means of the {@link #getStyleDeclarations(HTMLElementImpl, String, String, String)}
 * method.
 * @author user
 *
 */
public class StyleSheetAggregator {
	private final HTMLDocumentImpl document;
	private final Map classMapsByElement = new HashMap();
	private final Map idMapsByElement = new HashMap();
	private final Map rulesByElement = new HashMap();
	
	public StyleSheetAggregator(HTMLDocumentImpl document) {
		this.document = document;
	}
	
	public final void addStyleSheets(Collection styleSheets) throws MalformedURLException {
		Iterator i = styleSheets.iterator();
		while(i.hasNext()) {
			CSSStyleSheet sheet = (CSSStyleSheet) i.next();
			this.addStyleSheet(sheet);
		}
	}

	private final void addStyleSheet(CSSStyleSheet styleSheet) throws MalformedURLException {
		CSSRuleList ruleList = styleSheet.getCssRules();
		int length = ruleList.getLength();
		for(int i = 0; i < length; i++) {
			CSSRule rule = ruleList.item(i);
			this.addRule(styleSheet, rule);
		}
	}
	
	private final void addRule(CSSStyleSheet styleSheet, CSSRule rule) throws MalformedURLException {
		HTMLDocumentImpl document = this.document;
		if(rule instanceof CSSStyleRule) {
			CSSStyleRule sr = (CSSStyleRule) rule;
			String selectorText = sr.getSelectorText();
			StringTokenizer commaTok = new StringTokenizer(selectorText, ",");
			while(commaTok.hasMoreTokens()) {
				String selectorPart = commaTok.nextToken().toLowerCase();
				ArrayList ancestorSelectors = null;
				String selector = null;
				StringTokenizer tok = new StringTokenizer(selectorPart, " \t\r\n");
				while(tok.hasMoreTokens()) {
					String token = tok.nextToken();
					if(tok.hasMoreTokens()) {
						if(ancestorSelectors == null) {
							ancestorSelectors = new ArrayList();
						}
						ancestorSelectors.add(token);
					}
					else {
						selector = token;
						break;
					}
				}
				if(selector != null) {
					int colonIdx = selector.indexOf(':');
					String pseudoName = colonIdx == -1 ? null : selector.substring(colonIdx+1);
					String actualSelector = colonIdx == -1 ? selector : selector.substring(0, colonIdx);
					int dotIdx = actualSelector.indexOf('.');
					if(dotIdx != -1) {
						String elemtl = actualSelector.substring(0, dotIdx);
						String classtl = actualSelector.substring(dotIdx+1);
						this.addClassRule(elemtl, classtl, sr, ancestorSelectors, pseudoName);
					}
					else {
						int poundIdx = actualSelector.indexOf('#');
						if(poundIdx != -1) {
							String elemtl = actualSelector.substring(0, poundIdx);
							String idtl = actualSelector.substring(poundIdx+1);
							this.addIdRule(elemtl, idtl, sr, ancestorSelectors, pseudoName);
						}
						else {
							String elemtl = actualSelector;
							this.addElementRule(elemtl, sr, ancestorSelectors, pseudoName);
						}
					}
				}
			}
			//TODO: Other types of selectors
		}
		else if(rule instanceof CSSImportRule) {
			CSSImportRule importRule = (CSSImportRule) rule;
			if(CSSUtilities.matchesMedia(importRule.getMedia(), document.getUserAgentContext())) {
				String href = importRule.getHref();
				String styleHref = styleSheet.getHref();
				String baseHref = styleHref == null ? document.getBaseURI() : styleHref;
				CSSStyleSheet sheet = CSSUtilities.parse(href, document, baseHref, false);
				if(sheet != null) {
					this.addStyleSheet(sheet);
				}
			}
		}
		else if(rule instanceof CSSMediaRule) {
			CSSMediaRule mrule = (CSSMediaRule) rule;
			MediaList mediaList = mrule.getMedia();
			if(CSSUtilities.matchesMedia(mediaList, document.getUserAgentContext())) {
				CSSRuleList ruleList = mrule.getCssRules();
				int length = ruleList.getLength();
				for(int i = 0; i < length; i++) {
					CSSRule subRule = ruleList.item(i);
					this.addRule(styleSheet, subRule);
				}
			}
		}		
	}

	private final void addClassRule(String elemtl, String classtl, CSSStyleRule styleRule, ArrayList ancestorSelectors, String pseudoName) {
		Map classMap = (Map) this.classMapsByElement.get(elemtl);
		if(classMap == null) {
			classMap = new HashMap();
			this.classMapsByElement.put(elemtl, classMap);
		}
		Collection rules = (Collection) classMap.get(classtl);
		if(rules == null) {
			rules = new LinkedList();
			classMap.put(classtl, rules);
		}
		rules.add(new StyleRuleInfo(ancestorSelectors, styleRule, pseudoName));
	}
	
	private final void addIdRule(String elemtl, String idtl, CSSStyleRule styleRule, ArrayList ancestorSelectors, String pseudoName) {
		Map idsMap = (Map) this.idMapsByElement.get(elemtl);
		if(idsMap == null) {
			idsMap = new HashMap();
			this.idMapsByElement.put(elemtl, idsMap);
		}
		Collection rules = (Collection) idsMap.get(idtl);
		if(rules == null) {
			rules = new LinkedList();
			idsMap.put(idtl, rules);
		}
		rules.add(new StyleRuleInfo(ancestorSelectors, styleRule, pseudoName));
	}
	
	private final void addElementRule(String elemtl, CSSStyleRule styleRule, ArrayList ancestorSelectors, String pseudoName) {
		Collection rules = (Collection) this.rulesByElement.get(elemtl);
		if(rules == null) {
			rules = new LinkedList();
			this.rulesByElement.put(elemtl, rules);
		}
		rules.add(new StyleRuleInfo(ancestorSelectors, styleRule, pseudoName));
	}
		
	public final Collection getStyleDeclarations(HTMLElementImpl element, String elementName, String elementId, String className, Set pseudoNames) {
		Collection styleDeclarations = null;
		String elementTL = elementName.toLowerCase();
		Collection elementRules = (Collection) this.rulesByElement.get(elementTL);
		if(elementRules != null) {
			Iterator i = elementRules.iterator();
			while(i.hasNext()) {
				StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
				if(styleRuleInfo.matches(element, pseudoNames)) {
					CSSStyleRule styleRule = styleRuleInfo.styleRule;
					if(styleDeclarations == null) {
						styleDeclarations = new LinkedList();
					}
					styleDeclarations.add(styleRule.getStyle());
				}
				else {
				}
			}
		}
		elementRules = (Collection) this.rulesByElement.get("*");
		if(elementRules != null) {
			Iterator i = elementRules.iterator();
			while(i.hasNext()) {
				StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
				if(styleRuleInfo.matches(element, pseudoNames)) {
					CSSStyleRule styleRule = styleRuleInfo.styleRule;
					if(styleDeclarations == null) {
						styleDeclarations = new LinkedList();
					}
					styleDeclarations.add(styleRule.getStyle());
				}
			}
		}
		if(className != null) {
			String classNameTL = className.toLowerCase();
			Map classMaps = (Map) this.classMapsByElement.get(elementTL);
			if(classMaps != null) {
				Collection classRules = (Collection) classMaps.get(classNameTL);
				if(classRules != null) {
					Iterator i = classRules.iterator();
					while(i.hasNext()) {
						StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
						if(styleRuleInfo.matches(element, pseudoNames)) {
							CSSStyleRule styleRule = styleRuleInfo.styleRule;
							if(styleDeclarations == null) {
								styleDeclarations = new LinkedList();
							}
							styleDeclarations.add(styleRule.getStyle());
						}
					}
				}
			}
			classMaps = (Map) this.classMapsByElement.get("*");
			if(classMaps != null) {
				Collection classRules = (Collection) classMaps.get(classNameTL);
				if(classRules != null) {
					Iterator i = classRules.iterator();
					while(i.hasNext()) {
						StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
						if(styleRuleInfo.matches(element, pseudoNames)) {
							CSSStyleRule styleRule = styleRuleInfo.styleRule;
							if(styleDeclarations == null) {
								styleDeclarations = new LinkedList();
							}
							styleDeclarations.add(styleRule.getStyle());
						}
					}
				}
			}
		}
		if(elementId != null) {
			Map idMaps = (Map) this.idMapsByElement.get(elementTL);
			if(idMaps != null) {
				String elementIdTL = elementId.toLowerCase();
				Collection idRules = (Collection) idMaps.get(elementIdTL);
				if(idRules != null) {
					Iterator i = idRules.iterator();
					while(i.hasNext()) {
						StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
						if(styleRuleInfo.matches(element, pseudoNames)) {
							CSSStyleRule styleRule = styleRuleInfo.styleRule;
							if(styleDeclarations == null) {
								styleDeclarations = new LinkedList();
							}
							styleDeclarations.add(styleRule.getStyle());
						}
					}				
				}
			}
			idMaps = (Map) this.idMapsByElement.get("*");
			if(idMaps != null) {
				String elementIdTL = elementId.toLowerCase();
				Collection idRules = (Collection) idMaps.get(elementIdTL);
				if(idRules != null) {
					Iterator i = idRules.iterator();
					while(i.hasNext()) {
						StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
						if(styleRuleInfo.matches(element, pseudoNames)) {
							CSSStyleRule styleRule = styleRuleInfo.styleRule;
							if(styleDeclarations == null) {
								styleDeclarations = new LinkedList();
							}
							styleDeclarations.add(styleRule.getStyle());
						}
					}				
				}
			}
		}
		return styleDeclarations;
	}

	public final boolean hasPseudoName(HTMLElementImpl element, String elementName, String elementId, String[] classArray, String pseudoName) {
		String elementTL = elementName.toLowerCase();
		Collection elementRules = (Collection) this.rulesByElement.get(elementTL);
		if(elementRules != null) {
			Iterator i = elementRules.iterator();
			while(i.hasNext()) {
				StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
				if(styleRuleInfo.hasPseudoNameMatch(element, pseudoName)) {
					return true;
				}
			}
		}
		elementRules = (Collection) this.rulesByElement.get("*");
		if(elementRules != null) {
			Iterator i = elementRules.iterator();
			while(i.hasNext()) {
				StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
				if(styleRuleInfo.hasPseudoNameMatch(element, pseudoName)) {
					return true;
				}
			}
		}
		if(classArray != null) {
			for(int cidx  = 0; cidx < classArray.length; cidx++) {
				String className = classArray[cidx];
				String classNameTL = className.toLowerCase();
				Map classMaps = (Map) this.classMapsByElement.get(elementTL);
				if(classMaps != null) {
					Collection classRules = (Collection) classMaps.get(classNameTL);
					if(classRules != null) {
						Iterator i = classRules.iterator();
						while(i.hasNext()) {
							StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
							if(styleRuleInfo.hasPseudoNameMatch(element, pseudoName)) {
								return true;
							}
						}
					}
				}
				classMaps = (Map) this.classMapsByElement.get("*");
				if(classMaps != null) {
					Collection classRules = (Collection) classMaps.get(classNameTL);
					if(classRules != null) {
						Iterator i = classRules.iterator();
						while(i.hasNext()) {
							StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
							if(styleRuleInfo.hasPseudoNameMatch(element, pseudoName)) {
								return true;
							}
						}
					}
				}
			}
		}
		if(elementId != null) {
			Map idMaps = (Map) this.idMapsByElement.get(elementTL);
			if(idMaps != null) {
				String elementIdTL = elementId.toLowerCase();
				Collection idRules = (Collection) idMaps.get(elementIdTL);
				if(idRules != null) {
					Iterator i = idRules.iterator();
					while(i.hasNext()) {
						StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
						if(styleRuleInfo.hasPseudoNameMatch(element, pseudoName)) {
							return true;
						}
					}				
				}
			}
			idMaps = (Map) this.idMapsByElement.get("*");
			if(idMaps != null) {
				String elementIdTL = elementId.toLowerCase();
				Collection idRules = (Collection) idMaps.get(elementIdTL);
				if(idRules != null) {
					Iterator i = idRules.iterator();
					while(i.hasNext()) {
						StyleRuleInfo styleRuleInfo = (StyleRuleInfo) i.next();
						if(styleRuleInfo.hasPseudoNameMatch(element, pseudoName)) {
							return true;
						}
					}				
				}
			}
		}
		return false;
	}

	private static class StyleRuleInfo {
		private final CSSStyleRule styleRule;
		private final ArrayList ancestorSelectors;
		private final String pseudoName;

		/**
		 * @param selectors A collection of selectors already in lower case.
		 * @param rule A CSS rule.
		 */
		public StyleRuleInfo(ArrayList selectors, CSSStyleRule rule, String pseudoName) {
			super();
			ancestorSelectors = selectors;
			styleRule = rule;
			this.pseudoName = pseudoName;
		}

		public boolean hasPseudoNameMatch(HTMLElementImpl element, String pseudoName) {
			String localPn = this.pseudoName;
			if(localPn == null || !pseudoName.equalsIgnoreCase(localPn)) {
				return false;
			}
			return this.isSelectorMatch(element);
		}
		
		public boolean matches(HTMLElementImpl element, Set pseudoNames) {
			if(this.pseudoName != null && (pseudoNames == null || !pseudoNames.contains(this.pseudoName))) {
				return false;
			}
			return this.isSelectorMatch(element);
		}
		
		private boolean isSelectorMatch(HTMLElementImpl element) {
			ArrayList as = this.ancestorSelectors;
			if(as == null) {
				return true;
			}
			HTMLElementImpl currentElement = element;
			int size = as.size();
			for(int i = size; --i >= 0;) {
				String selector = (String) as.get(i);
				int dotIdx = selector.indexOf('.');
				if(dotIdx != -1) {
					String elemtl = selector.substring(0, dotIdx);
					String classtl = selector.substring(dotIdx+1);
					HTMLElementImpl ancestor = currentElement.getAncestorWithClass(elemtl, classtl);
					if(ancestor == null) {
						return false;
					}
					currentElement = ancestor;					
				}
				else {
					int poundIdx = selector.indexOf('#');
					if(poundIdx != -1) {
						String elemtl = selector.substring(0, poundIdx);
						String idtl = selector.substring(poundIdx+1);
						HTMLElementImpl ancestor = currentElement.getAncestorWithId(elemtl, idtl);
						if(ancestor == null) {
							return false;
						}
						currentElement = ancestor;											
					}
					else {
						String elemtl = selector;
						HTMLElementImpl ancestor = currentElement.getAncestor(elemtl);
						if(ancestor == null) {
							return false;
						}
						currentElement = ancestor;											
					}
				}				
			}
			return true;
		}
	}
	
}
