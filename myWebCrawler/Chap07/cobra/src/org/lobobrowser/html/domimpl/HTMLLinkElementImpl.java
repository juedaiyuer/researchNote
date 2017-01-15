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
package org.lobobrowser.html.domimpl;

import java.net.MalformedURLException;
import java.net.URL;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.html2.*;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.style.*;
import org.lobobrowser.util.gui.*;

import java.util.logging.*;

public class HTMLLinkElementImpl extends HTMLAbstractUIElement implements
		HTMLLinkElement {	
	private static final Logger logger = Logger.getLogger(HTMLLinkElementImpl.class.getName());
	private static final boolean loggableInfo = logger.isLoggable(Level.INFO);
	
	public HTMLLinkElementImpl(String name) {
		super(name);
	}

	private boolean disabled;
	
	public boolean getDisabled() {
		return this.disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public String getHref() {
		return this.getAttribute("href");
	}

	public void setHref(String href) {
		this.setAttribute("href", href);
	}

	public String getHreflang() {
		return this.getAttribute("hreflang");
	}

	public void setHreflang(String hreflang) {
		this.setAttribute("hreflang", hreflang);
	}

	public String getMedia() {
		return this.getAttribute("media");
	}

	public void setMedia(String media) {
		this.setAttribute("media", media);
	}

	public String getRel() {
		return this.getAttribute("rel");
	}

	public void setRel(String rel) {
		this.setAttribute("rel", rel);
	}

	public String getRev() {
		return this.getAttribute("rev");
	}

	public void setRev(String rev) {
		this.setAttribute("rev", rev);
	}

	public String getTarget() {		
		String target = this.getAttribute("target");
		if(target != null) {
			return target;
		}
		HTMLDocumentImpl doc = (HTMLDocumentImpl) this.document;
		return doc == null ? null : doc.getDefaultTarget();
	}

	public void setTarget(String target) {
		this.setAttribute("target", target);
	}

	public String getType() {
		return this.getAttribute("type");
	}

	public void setType(String type) {
		this.setAttribute("type", type);
	}

	public Object setUserData(String key, Object data, UserDataHandler handler) {
		if(org.lobobrowser.html.parser.HtmlParser.MODIFYING_KEY.equals(key) && data != Boolean.TRUE) {
			this.processLink();
		}
		return super.setUserData(key, data, handler);
	}

	protected void processLink() {
		String rel = this.getAttribute("rel");
		if(rel != null && rel.toLowerCase().indexOf("stylesheet") != -1) {	
			String media = this.getMedia();
			if(CSSUtilities.matchesMedia(media, this.getUserAgentContext())) {
				HTMLDocumentImpl doc = (HTMLDocumentImpl) this.getOwnerDocument();
				try {
					boolean liflag = loggableInfo;
					long time1 = liflag ? System.currentTimeMillis() : 0;
					try {
						CSSStyleSheet sheet = CSSUtilities.parse(this.getHref(), doc, doc.getBaseURI(), false);
						if(sheet != null) {
							doc.addStyleSheet(sheet);
						}
					} finally {
						if(liflag) {
							long time2 = System.currentTimeMillis();
							logger.info("processLink(): Loaded and parsed CSS (or attempted to) at URI=[" + this.getHref() + "] in " + (time2 - time1) + " ms.");
						}
					}

				} catch(MalformedURLException mfe) {
					this.warn("Will not parse CSS. URI=[" + this.getHref() + "] with BaseURI=[" + doc.getBaseURI() + "] does not appear to be a valid URI.");
				} catch(Throwable err) {
					this.warn("Unable to parse CSS. URI=[" + this.getHref() + "].", err);
				}
			}
		}
	}

	public void navigate() {
		if(this.disabled) {
			return;
		}
		HtmlRendererContext rcontext = this.getHtmlRendererContext();
		if(rcontext != null) {
			String href = this.getHref();
			if(href != null && !"".equals(href)) {
				String target = this.getTarget();
				try {
					URL url = this.getFullURL(href);
					if(url == null) {
						this.warn("Unable to resolve URI: [" + href + "].");					
					}
					else {
						rcontext.linkClicked(this, url, target);
					}
				} catch(MalformedURLException mfu) {
					this.warn("Malformed URI: [" + href + "].", mfu);
				}
			}
		}
	}

	private java.awt.Color getLinkColor() {
		HTMLDocument doc = (HTMLDocument) this.document;
		if(doc != null) {
			HTMLBodyElement body = (HTMLBodyElement) doc.getBody();
			if(body != null) {
				String vlink = body.getVLink();
				String link = body.getLink();
				if(vlink != null || link != null) {
					HtmlRendererContext rcontext = this.getHtmlRendererContext();
					if(rcontext != null) {
						boolean visited = rcontext.isVisitedLink(this);
						String colorText = visited ? vlink : link;
						if(colorText != null) {
							return ColorFactory.getInstance().getColor(colorText);
						}
					}
				}
			}
		}
		return java.awt.Color.BLUE;
	}
	
	protected RenderState createRenderState(RenderState prevRenderState) {
		prevRenderState = new TextDecorationRenderState(prevRenderState, RenderState.MASK_TEXTDECORATION_UNDERLINE);
		prevRenderState = new ColorRenderState(prevRenderState, this.getLinkColor());
		return super.createRenderState(prevRenderState);
	}
}
