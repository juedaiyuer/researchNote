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
 * Created on Nov 12, 2005
 */
package org.lobobrowser.html.js;

import java.util.*;
import java.lang.ref.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.util.logging.*;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.js.*;
import org.lobobrowser.util.ID;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.html2.*;

public class Window extends AbstractScriptableDelegate {
	private static final Logger logger = Logger.getLogger(Window.class.getName());
	private static final Map CONTEXT_WINDOWS = new WeakHashMap();
	//private static final JavaClassWrapper IMAGE_WRAPPER = JavaClassWrapperFactory.getInstance().getClassWrapper(Image.class);	
	private static final JavaClassWrapper XMLHTTPREQUEST_WRAPPER = JavaClassWrapperFactory.getInstance().getClassWrapper(XMLHttpRequest.class);	
	
	private final HtmlRendererContext rcontext;
	private final UserAgentContext uaContext;

	private Navigator navigator;
	private Screen screen;
	private Location location;
	private Map taskMap; 
	private volatile Document document;
	
	public Window(HtmlRendererContext rcontext, UserAgentContext uaContext) {
		//TODO: Probably need to create a new Window instance
		//for every document. Sharing of Window state between
		//different documents is not correct.
		this.rcontext = rcontext;
		this.uaContext = uaContext;
	}

	public HtmlRendererContext getHtmlRendererContext() {
		return this.rcontext;
	}
	
	public void setDocument(Document document) {
		Document prevDocument = this.document;
		if(prevDocument !=  document) {
			this.forgetAllTasks();
			Function onunload = this.onunload;
			if(onunload != null) {
				HTMLDocumentImpl oldDoc = (HTMLDocumentImpl) this.document;
				Executor.executeFunction(this.getWindowScope(), onunload, oldDoc.getDocumentURL(), this.uaContext);
				this.onunload = null;
			}
			this.document = document;
		}
	}

	public Document getDocument() {
		return this.document;
	}

	private void putTask(Integer timeoutID, Timer timer) {
		synchronized(this) {
			Map taskMap = this.taskMap;
			if(taskMap == null) {
				taskMap = new HashMap();
				this.taskMap = taskMap;
			}
			taskMap.put(timeoutID, timer);
		}		
	}
	
	private void forgetTask(Integer timeoutID, boolean cancel) {
		synchronized(this) {
			Map taskMap = this.taskMap;
			if(taskMap != null) {
				Timer timer = (Timer) taskMap.remove(timeoutID);
				if(timer != null && cancel) {
					timer.stop();
				}
			}			
		}
	}
	
	private void forgetAllTasks() {
		synchronized(this) {
			Map taskMap = this.taskMap;
			if(taskMap != null) {
				Iterator i = taskMap.values().iterator();
				while(i.hasNext()) {
					Timer timer = (Timer) i.next();
					timer.stop();
				}
				this.taskMap = null;
			}
		}
	}

	//	private Timer getTask(Long timeoutID) {
//		synchronized(this) {
//			Map taskMap = this.taskMap;
//			if(taskMap != null) {
//				return (Timer) taskMap.get(timeoutID);
//			}
//		}				
//		return null;
//	}
	
	public void alert(String message) {
		if(this.rcontext != null) {
			this.rcontext.alert(message);
		}
	}
	
	public void back() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			rcontext.back();
		}
	}
	
	public void blur() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			rcontext.blur();
		}
	}
	
	public void clearTimeout(int timeoutID) {
		Integer key = new Integer(timeoutID);
		this.forgetTask(key, true);
	}
	
	public void close() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			rcontext.close();
		}
	}
	
	public boolean confirm(String message) {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return rcontext.confirm(message);
		}
		else {
			return false;
		}
	}
	
	public Object eval(String javascript) {
		HTMLDocumentImpl document = (HTMLDocumentImpl) this.document;
		if(document == null) {
			throw new IllegalStateException("Cannot evaluate if document is not set.");
		}
		Context ctx = Executor.createContext(document.getDocumentURL(), this.uaContext);
		try {
			Scriptable scope = this.getWindowScope();
			if(scope == null) {
				throw new IllegalStateException("Scriptable (scope) instance was expected to be keyed as UserData to document using " + Executor.SCOPE_KEY);
			}
			String scriptURI = "window.eval";
			if(logger.isLoggable(Level.INFO)) {
				logger.info("eval(): javascript follows...\r\n" + javascript);
			}
			return ctx.evaluateString(scope, javascript, scriptURI, 1, null);
		} finally {
			Context.exit();
		}
	}
	
	public void focus() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			rcontext.focus();
		}
	}

	private ScriptableObject windowScope;

	public Scriptable getWindowScope() {
		synchronized(this) {
			ScriptableObject windowScope = this.windowScope;
			if(windowScope != null) {
				return windowScope;
			}
			// Context.enter() OK in this particular case.
			Context ctx = Context.enter();
			try {
				// Window scope needs to be top-most scope.
				windowScope = (ScriptableObject) JavaScript.getInstance().getJavascriptObject(this, null);
				ctx.initStandardObjects(windowScope);

				// Special Javascript class: XMLHttpRequest
				final Scriptable ws = windowScope;
				JavaInstantiator xi = new JavaInstantiator() {
					public Object newInstance() {
						HTMLDocumentImpl doc = (HTMLDocumentImpl) document;
						if(doc == null) {
							throw new IllegalStateException("Cannot perform operation when document is unset.");
						}
						return new XMLHttpRequest(uaContext, doc.getDocumentURL(), ws);
					}
				};
				Function xmlHttpRequestC = JavaObjectWrapper.getConstructor("XMLHttpRequest", XMLHTTPREQUEST_WRAPPER, windowScope, xi);
				ScriptableObject.defineProperty(windowScope, "XMLHttpRequest", xmlHttpRequestC, ScriptableObject.READONLY);		

				// HTML element classes
				this.defineElementClass(windowScope, "Image", "img", HTMLImageElementImpl.class);
				this.defineElementClass(windowScope, "Script", "script", HTMLScriptElementImpl.class);
				this.defineElementClass(windowScope, "IFrame", "iframe", HTMLIFrameElementImpl.class);
				this.defineElementClass(windowScope, "Option", "option", HTMLOptionElementImpl.class);
				this.defineElementClass(windowScope, "Select", "select", HTMLSelectElementImpl.class);
				
				this.windowScope = windowScope;
				return windowScope;
			} finally {
				Context.exit();
			}
		}
	}
	
	private final void defineElementClass(Scriptable scope, final String jsClassName, final String elementName, Class javaClass) {
		JavaInstantiator ji = new JavaInstantiator() {
			public Object newInstance() {
				Document document = Window.this.document;
				if(document == null) {
					throw new IllegalStateException("Document not set in current context.");
				}
				return document.createElement(elementName);
			}
		};
		JavaClassWrapper classWrapper = JavaClassWrapperFactory.getInstance().getClassWrapper(javaClass);
		Function constructorFunction = JavaObjectWrapper.getConstructor(jsClassName, classWrapper, scope, ji);
		ScriptableObject.defineProperty(scope, jsClassName, constructorFunction, ScriptableObject.READONLY);		
	}
	
	public static Window getWindow(HtmlRendererContext rcontext) {
		if(rcontext == null) {
			return null;
		}
		synchronized(CONTEXT_WINDOWS) {
			Reference wref = (Reference) CONTEXT_WINDOWS.get(rcontext);
			if(wref != null) {
				Window window = (Window) wref.get();
				if(window != null) {
					return window;
				}
			}
			Window window = new Window(rcontext, rcontext.getUserAgentContext());
			CONTEXT_WINDOWS.put(rcontext, new WeakReference(window));
			return window;
		}
	}
	
	public Window open(String relativeUrl, String windowName, String windowFeatures, boolean replace) {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			java.net.URL url;
			Object document = this.document;
			if(document instanceof HTMLDocumentImpl) {
				url = ((HTMLDocumentImpl) document).getFullURL(relativeUrl);
			}
			else {
				try {
					url = new java.net.URL(relativeUrl);
				} catch(java.net.MalformedURLException mfu) {
					throw new IllegalArgumentException("Malformed URI: " + relativeUrl);
				}
			}
			HtmlRendererContext newContext = rcontext.open(url, windowName, windowFeatures, replace);
			return getWindow(newContext);
		}
		else {
			return null;
		}
	}

	public Window open(String url, String windowName) {
		return this.open(url, windowName, "", false);
	}

	public Window open(String url, String windowName, String windowFeatures) {
		return this.open(url, windowName, windowFeatures, false);
	}

	public String prompt(String message) {
		return this.prompt(message, "");
	}
	
	public String prompt(String message, int inputDefault) {
		return this.prompt(message, String.valueOf(inputDefault));
	}

	public String prompt(String message, String inputDefault) {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return rcontext.prompt(message, inputDefault);
		}
		else {
			return null;
		}
	}

	public void scroll(int x, int y) {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			rcontext.scroll(x, y);
		}
	}
	
	public int setTimeout(final String expr, double millis) {
		final int timeID = ID.generateInt();
		final Integer timeIDInt = new Integer(timeID);
		ActionListener task = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// This executes in the GUI thread and that's good.
				Window.this.forgetTask(timeIDInt, false);
				Window.this.eval(expr);
			}			
		};
		if(millis > Integer.MAX_VALUE || millis < 0) {
			throw new IllegalArgumentException("Timeout value " + millis + " is not supported.");
		}
		Timer timer = new Timer((int) millis, task);
		timer.setRepeats(false);
		timer.start();
		this.putTask(timeIDInt, timer);
		return timeID;
	}
	
	public int setTimeout(final Function function, double millis) {
		final int timeID = ID.generateInt();
		final Integer timeIDInt = new Integer(timeID);
		ActionListener task = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// This executes in the GUI thread and that's good.
				Window.this.forgetTask(timeIDInt, false);
				HTMLDocumentImpl doc = (HTMLDocumentImpl) document;
				if(doc == null) {
					throw new IllegalStateException("Cannot perform operation when document is unset.");
				}
				Executor.executeFunction(getWindowScope(), function, doc.getDocumentURL(), uaContext);
			}			
		};
		if(millis > Integer.MAX_VALUE || millis < 0) {
			throw new IllegalArgumentException("Timeout value " + millis + " is not supported.");
		}
		Timer timer = new Timer((int) millis, task);
		timer.setRepeats(false);
		timer.start();
		this.putTask(timeIDInt, timer);
		return timeID;
	}

	public boolean isClosed() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return rcontext.isClosed();
		}
		else {
			return false;
		}
	}
	
	public String getDefaultStatus() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return rcontext.getDefaultStatus();
		}
		else {
			return null;
		}
	}
	
	public HTMLCollection getFrames() {
		Document doc = this.document;
		if(doc instanceof HTMLDocumentImpl) {
			return ((HTMLDocumentImpl) doc).getFrames();					
		}
		return null;
	}
	
	/**
	 * Gets the number of frames.
	 */
	public int getLength() {
		HTMLCollection frames = this.getFrames();
		return frames == null ? 0 : frames.getLength();
	}
	
	public String getName() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return rcontext.getName();
		}
		else {
			return null;
		}
	}
	
	public Window getParent() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return Window.getWindow(rcontext.getParent());
		}
		else {
			return null;
		}
	}
	
	public Window getOpener() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return Window.getWindow(rcontext.getOpener());
		}
		else {
			return null;
		}
	}
	
	public void setOpener(Window opener) {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			if(opener == null) {
				rcontext.setOpener(null);
			}
			else {
				rcontext.setOpener(opener.rcontext);
			}
		}
	}
	
	public Window getSelf() {
		return this;
	}
	
	public String getStatus() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return rcontext.getStatus();
		}
		else {
			return null;
		}
	}
	
	public void setStatus(String message) {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			rcontext.setStatus(message);
		}
	}
		
	public Window getTop() {
		HtmlRendererContext rcontext = this.rcontext;
		if(rcontext != null) {
			return Window.getWindow(rcontext.getTop());
		}
		else {
			return null;
		}
	}
	
	public Window getWindow() {
		return this;
	}
	
	
	public Navigator getNavigator() {
		synchronized(this) {
			Navigator nav = this.navigator;
			if(nav == null) {
				nav = new Navigator(this.uaContext);
				this.navigator = nav;
			}
			return nav;
		}
	}
	
	public Screen getScreen() {
		synchronized(this) {
			Screen nav = this.screen;
			if(nav == null) {
				nav = new Screen();
				this.screen = nav;
			}
			return nav;
		}		
	}
	
	public Location getLocation() {
		synchronized(this) {
			Location location = this.location;
			if(location == null) {
				location = new Location(this);
				this.location = location;
			}
			return location;
		}
	}
	
	public void setLocation(String location) {
		this.getLocation().setHref(location);
	}
	
	public Function getOnload() {
		Document doc = this.document;
		if(doc instanceof HTMLDocumentImpl) {
			return ((HTMLDocumentImpl) doc).getOnloadHandler();
		}
		else {
			return null;
		}
	}

	public void setOnload(Function onload) {
		//Note that body.onload overrides
		//window.onload.
		Document doc = this.document;
		if(doc instanceof HTMLDocumentImpl) {
			((HTMLDocumentImpl) doc).setOnloadHandler(onload);
		}
	}

	private Function onunload;

	public Function getOnunload() {
		return onunload;
	}

	public void setOnunload(Function onunload) {
		this.onunload = onunload;
	}
}

