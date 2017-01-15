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
 * Created on Oct 22, 2005
 */
package org.lobobrowser.html.test;

import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.util.io.*;
import org.w3c.dom.html2.*;

import java.util.logging.*;

/**
 * The <code>SimpleHtmlRendererContext</code> class implements
 * the {@link org.lobobrowser.html.HtmlRendererContext} interface.
 * Note that this class provides simple implementations
 * of most methods, which should be overridden to provide
 * real-world functionality. 
 */
public class SimpleHtmlRendererContext implements HtmlRendererContext {
	private static final Logger logger = Logger.getLogger(SimpleHtmlRendererContext.class.getName());
	
	private final HtmlPanel htmlPanel;
	private final HtmlRendererContext parentRcontext;
	
	/**
	 * Constructs a SimpleHtmlRendererContext.
	 * @param contextComponent The component that will render HTML.
	 */
	public SimpleHtmlRendererContext(HtmlPanel contextComponent) {
		super();
		this.htmlPanel = contextComponent;
		this.parentRcontext = null;
	}
	
	/**
	 * Constructs a simple <code>HtmlRendererContext</code> without
	 * a parent. This constructor should not be used to create
	 * the context of a frame with a parent.
	 * @param contextComponent
	 * @param pcontext
	 * @deprecated HtmlParserContext is no longer used in this class.
	 */
	public SimpleHtmlRendererContext(HtmlPanel contextComponent, HtmlParserContext pcontext) {
		this(contextComponent, pcontext, null);
	}

	/**
	 * Constructs a SimpleHtmlRendererContext.
	 * @param contextComponent The component that will render HTML.
	 * @param pcontext A parser context.
	 * @param parentRcontext The parent's renderer context. This is <code>null</code> for the root renderer context.
	 * Normally ony frame renderer contexts would have parents.
	 * @deprecated HtmlParserContext is no longer used in this class.
	 */
	public SimpleHtmlRendererContext(HtmlPanel contextComponent, HtmlParserContext pcontext, HtmlRendererContext parentRcontext) {
		super();
		this.htmlPanel = contextComponent;
		this.parentRcontext = parentRcontext;
	}
	
	/**
	 * Constructs a SimpleHtmlRendererContext.
	 * @param contextComponent The component that will render HTML.
	 * @param parentRcontext The parent's renderer context. This is <code>null</code> for the root renderer context.
	 * Normally ony frame renderer contexts would have parents.
	 */
	public SimpleHtmlRendererContext(HtmlPanel contextComponent, HtmlRendererContext parentRcontext) {
		super();
		this.htmlPanel = contextComponent;
		this.parentRcontext = parentRcontext;
	}

	private volatile String sourceCode;
	
	public String getSourceCode() {
		return this.sourceCode;
	}

	public HTMLCollection getFrames() {
		Object rootNode = this.htmlPanel.getRootNode();
		if(rootNode instanceof HTMLDocumentImpl) {
			return ((HTMLDocumentImpl) rootNode).getFrames();
		}
		else {
			return null;
		}
	}

	/**
	 * This method is called by the local navigate() implementation
	 * and creates a {@link SimpleHtmlParserContext}.
	 * Override this method if you need to use the local navigate()
	 * implementation with an overridden parser context. 
	 */
	protected HtmlParserContext createParserContext(java.net.URL url) {
		return new SimpleHtmlParserContext();
	}
	
	/**
	 * Implements reload as navigation to current URL.
	 * Override to implement a more robust reloading
	 * mechanism.
	 */
	public void reload() {
		HTMLDocumentImpl document = (HTMLDocumentImpl) this.htmlPanel.getRootNode();
		if(document != null) {
			try {
				URL url = new URL(document.getDocumentURI());
				this.navigate(url, null);
			} catch(java.net.MalformedURLException throwable) {
				this.warn("reload(): Malformed URL", throwable);
			}
		}
	}
	
	/**
	 * Implements the link click handler by invoking {@link #navigate(URL, String)}.
	 */
	public void linkClicked(HTMLElement linkNode, URL url, String target) {
		this.navigate(url, target);
	}

	/**
	 * Gets the connection proxy used in {@link #navigate(URL, String)}.
	 * This implementation calls {@link SimpleUserAgentContext#getProxy()}
	 * if {@link #getUserAgentContext()} returns an instance assignable to {@link SimpleUserAgentContext}.
	 * The method may be overridden to provide a different proxy setting.
	 */
	protected Proxy getProxy() {
		Object ucontext = this.getUserAgentContext();
		if(ucontext instanceof SimpleUserAgentContext) {
			return ((SimpleUserAgentContext) ucontext).getProxy();
		}
		return Proxy.NO_PROXY;
	}
	
	/**
	 * Implements simple navigation with incremental
	 * rendering, and target processing, including
	 * frame lookup. Should be overridden to allow for
	 * more robust browser navigation.
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>Encoding ISO-8859-1 assumed always.
	 * <li>Caching is not implemented.
	 * <li>Cookies are not implemented.
	 * <li>Incremental rendering is not optimized for
	 *     ignorable document change notifications.
	 * <li>Other HTTP features are not implemented.
	 * </ul>
	 */
	public void navigate(final URL href, String target) {
		// This method implements simple incremental rendering.
		if(target != null) {
			HtmlRendererContext topCtx = this.getTop();
			HTMLCollection frames = topCtx.getFrames();
			if(frames != null) {
				org.w3c.dom.Node frame = frames.namedItem(target);
				if(frame instanceof FrameNode) {
					BrowserFrame bframe = ((FrameNode) frame).getBrowserFrame();
					if(bframe == null) {
						throw new IllegalStateException("Frame node without a BrowserFrame instance: " + frame);
					}
					if(bframe.getHtmlRendererContext() != this) {
						bframe.loadURL(href);
						return;	
					}
				}
			}
			target = target.trim().toLowerCase();
			if("_top".equals(target)) {
				this.getTop().navigate(href, null);
				return;
			}
			else if ("_parent".equals(target)) {
				HtmlRendererContext parent = this.getParent();
				if(parent != null) {
					parent.navigate(href, null);
					return;
				}
			}
			else if("_blank".equals(target)) {
				this.open(href.toExternalForm(), "cobra.blank", "", false);
				return;
			}
			else {
				// fall through
			}
		}
		
		URL urlForLoading;
		if(href.getProtocol().equals("file")) {
			// Remove query so it works.
			try {
				urlForLoading = new URL(href.getProtocol(), href.getHost(), href.getPort(), href.getPath());
			} catch(java.net.MalformedURLException throwable) {
				this.warn("malformed", throwable);
				urlForLoading = href;
			}
		}
		else {
			urlForLoading = href;
		}
		final URL finalURLForLoading = urlForLoading;		
		// Make request asynchronously.
		new Thread() {
			public void run() {
				try {
					URL uri = href;
					logger.info("process(): Loading URI=[" + uri + "].");
					long time0 = System.currentTimeMillis();
					// Using potentially different URL for loading.
					Proxy proxy = SimpleHtmlRendererContext.this.getProxy();
					URLConnection connection = proxy == null || proxy == Proxy.NO_PROXY ? finalURLForLoading.openConnection() : finalURLForLoading.openConnection(proxy);
					connection.setRequestProperty("User-Agent", getUserAgentContext().getUserAgent());
					connection.setRequestProperty("Cookie", "");
					if (connection instanceof HttpURLConnection) {
						HttpURLConnection hc = (HttpURLConnection) connection;
						hc.setInstanceFollowRedirects(true);
						int responseCode = hc.getResponseCode();
						logger.info("process(): HTTP response code: "
								+ responseCode);
					}
					InputStream in = connection.getInputStream();
					try {
						SimpleHtmlRendererContext.this.sourceCode = null;
						long time1 = System.currentTimeMillis();
						RecordedInputStream rin = new RecordedInputStream(in);
						InputStream bin = new BufferedInputStream(rin, 8192);
						HtmlParserContext pcontext = createParserContext(uri);
						DocumentBuilderImpl builder = new DocumentBuilderImpl(
								pcontext, SimpleHtmlRendererContext.this);
						String actualURI = uri.toExternalForm();
						// Only create document, don't parse.
						HTMLDocumentImpl document = (HTMLDocumentImpl) builder
						.createDocument(new InputSourceImpl(bin, actualURI,
						"ISO-8859-1"));
						// Set document in HtmlPanel. Safe to call outside GUI thread.
						SimpleHtmlRendererContext.this.htmlPanel.setDocument(document, SimpleHtmlRendererContext.this);
						// Now start loading.
						document.load();
						long time2 = System.currentTimeMillis();
						logger.info("Parsed URI=[" + uri + "]: Parse elapsed: "
								+ (time2 - time1) + " ms. Connection elapsed: "
								+ (time1 - time0) + " ms.");
						SimpleHtmlRendererContext.this.sourceCode = rin.getString("ISO-8859-1");
					} finally {
						in.close();
					}
				} catch (Exception err) {
					SimpleHtmlRendererContext.this.error(
							"navigate(): Error loading or parsing request.",
							err);
				}
			}
		}.start();
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlContext#submitForm(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.xamjwg.html.FormInput[])
	 */
	public void submitForm(String method, java.net.URL action, String target, String enctype, FormInput[] formInputs) {
		StringBuffer sb = new StringBuffer();
		String lineBreak = System.getProperty("line.separator"); 
		if(formInputs != null) {
			for(int i = 0; i < formInputs.length; i++) {
				sb.append("INPUT: " + formInputs[i].toString());
				sb.append(lineBreak);
			}
		}
		this.warn("submitForm(): Not overridden; method=" + method + "; action=" + action + "; target=" + target + "; enctype=" + enctype + lineBreak + sb);
	}

	// Methods useful to Window below:
	
	public void alert(String message) {
		JOptionPane.showMessageDialog(this.htmlPanel, message);
	}
	
	public void back() {
		this.warn("back(): Not overridden");
	}
	
	public void blur() {
		this.warn("back(): Not overridden");
	}

	public void close() {
		this.warn("close(): Not overridden");
	}
	
	public boolean confirm(String message) {
		int retValue = JOptionPane.showConfirmDialog(htmlPanel, message, "Confirm", JOptionPane.YES_NO_OPTION);
		return retValue == JOptionPane.YES_OPTION;
	}
		
	public void focus() {
		this.warn("focus(): Not overridden");
	}
	
	public HtmlRendererContext open(String url, String windowName, String windowFeatures, boolean replace) {
		this.warn("open(): Not overridden");
		return null;
	}

	public HtmlRendererContext open(java.net.URL url, String windowName, String windowFeatures, boolean replace) {
		this.warn("open(): Not overridden");
		return null;
	}

	public String prompt(String message, String inputDefault) {
		return JOptionPane.showInputDialog(htmlPanel, message);
	}

	public void scroll(int x, int y) {
		this.warn("scroll(): Not overridden");
	}
	
	public boolean isClosed() {
		this.warn("isClosed(): Not overridden");
		return false;
	}
	
	public String getDefaultStatus() {
		this.warn("getDefaultStatus(): Not overridden");
		return "";
	}
		
	public int getLength() {
		this.warn("getLength(): Not overridden");
		return 0;
	}
	
	public String getName() {
		this.warn("getName(): Not overridden");
		return "";
	}
	
	public HtmlRendererContext getParent() {
		return this.parentRcontext;
	}

	private volatile HtmlRendererContext opener;
	
	public HtmlRendererContext getOpener() {
		return this.opener;
	}
	
	public void setOpener(HtmlRendererContext opener) {
		this.opener = opener;
	}
	
	public String getStatus() {
		this.warn("getStatus(): Not overridden");
		return "";
	}
	
	public void setStatus(String message) {
		this.warn("setStatus(): Not overridden");
	}
	
	public HtmlRendererContext getTop() {
		HtmlRendererContext ancestor = this.parentRcontext;
		if(ancestor == null) {
			return this;
		}
		return ancestor.getTop();
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlContext#createBrowserFrame()
	 */
	public BrowserFrame createBrowserFrame() {
		return new SimpleBrowserFrame(this);
	}		
	
	public void warn(String message, Throwable throwable) {
		if(logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, message, throwable);
		}
	}
	
	public void error(String message, Throwable throwable) {
		if(logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE, message, throwable);
		}
	}
	
	public void warn(String message) {
		if(logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, message);
		}		
	}
	
	public void error(String message) {
		if(logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE, message);
		}
	}

	public HtmlObject getHtmlObject(HTMLElement element) {
		HtmlObject result;
		if("OBJECT".equalsIgnoreCase(element.getTagName())) {
			result = null;
		}
		else {	
			result = new SimpleHtmlObject(element);
		}
		this.warn("getHtmlObject(): Not overridden; returning " + result + " for " + element + ".");
		return result;
	}

	public void setDefaultStatus(String message) {
		this.warn("setDefaultStatus(): Not overridden.");
	}

	private UserAgentContext bcontext = null;
	
	public UserAgentContext getUserAgentContext() {
		this.warn("getUserAgentContext(): Not overridden; returning simple one.");
		synchronized(this) {
			if(this.bcontext == null) {
				this.bcontext = new SimpleUserAgentContext();
			}
			return this.bcontext;
		}
	}
	
	/**
	 * Should be overridden to return true if the link
	 * has been visited.
	 */
	public boolean isVisitedLink(HTMLLinkElement link) {
		//TODO
		return false;
	}

	/**
	 * @deprecated This method has been moved to {@link UserAgentContext}.
	 *             Override method from SimpleUserAgentContext instead.
	 */
	public boolean isMedia(String mediaName) {
		return true;
	}

	/**
	 * This method must be overridden to implement a context menu.
	 */
	public void onContextMenu(HTMLElement element, MouseEvent event) {
		this.warn("onContextMenu(): Not overridden.");
	}

	/**
	 * This method can be overridden to receive notifications when the
	 * mouse leaves an element.
	 */
	public void onMouseOut(HTMLElement element, MouseEvent event) {
	}

	/**
	 * This method can be overridden to receive notifications when the
	 * mouse first enters an element.
	 */
	public void onMouseOver(HTMLElement element, MouseEvent event) {
	}
}
