package org.lobobrowser.html.js;

import org.w3c.dom.Document;
import org.lobobrowser.html.*;
import org.lobobrowser.js.*;
import org.mozilla.javascript.*;
import java.util.logging.*;

public class XMLHttpRequest extends AbstractScriptableDelegate {
	private static final Logger logger = Logger.getLogger(XMLHttpRequest.class.getName());
	private final HttpRequest request;
	private final UserAgentContext pcontext;
	private final Scriptable scope;
	private final java.net.URL codeSource;
	
	public XMLHttpRequest(UserAgentContext pcontext, java.net.URL codeSource, Scriptable scope) {
		this.request = pcontext.createHttpRequest();
		this.pcontext = pcontext;
		this.scope = scope;
		this.codeSource = codeSource;
	}

	public void abort() {
		request.abort();
	}

	public String getAllResponseHeaders() {
		return request.getAllResponseHeaders();
	}

	public int getReadyState() {
		return request.getReadyState();
	}

	public byte[] getResponseBytes() {
		return request.getResponseBytes();
	}

	public String getResponseHeader(String headerName) {
		return request.getResponseHeader(headerName);
	}

	public String getResponseText() {
		return request.getResponseText();
	}

	public Document getResponseXML() {
		return request.getResponseXML();
	}

	public int getStatus() {
		return request.getStatus();
	}

	public String getStatusText() {
		return request.getStatusText();
	}

	public void open(String method, String url, boolean asyncFlag, String userName, String password) {
		request.open(method, url, asyncFlag, userName, password);
	}

	public void open(String method, String url, boolean asyncFlag, String userName) {
		request.open(method, url, asyncFlag, userName);
	}

	public void open(String method, String url, boolean asyncFlag) {
		request.open(method, url, asyncFlag);
	}

	public void open(String method, String url) {
		request.open(method, url);
	}	

	private Function onreadystatechange;
	private boolean listenerAdded;
	
	public Function getOnreadystatechange() {
		synchronized(this) {
			return this.onreadystatechange;
		}
	}

	public void setOnreadystatechange(final Function value) {
		synchronized(this) {
			this.onreadystatechange = value;
			if(value != null && !this.listenerAdded) {
				this.request.addReadyStateChangeListener(new ReadyStateChangeListener() {
					public void readyStateChanged() {
						java.awt.EventQueue.invokeLater(new Runnable() {
							public void run() {
								executeReadyStateChange();
							}
						});
					}
				});
				this.listenerAdded = true;
			}
		}
	}
	
	private void executeReadyStateChange() {
		// Expected to be called in GUI thread.
		try {
			Function f = XMLHttpRequest.this.getOnreadystatechange();
			if(f != null) {
				Context ctx = Executor.createContext(this.codeSource, this.pcontext);
				try {
					Scriptable newScope = (Scriptable) JavaScript.getInstance().getJavascriptObject(XMLHttpRequest.this, this.scope);
					f.call(ctx, newScope, newScope, new Object[0]);
				} finally {
					Context.exit();
				}
			}	
		} catch(Exception err) {
			logger.log(Level.WARNING, "Error processing ready state change.", err);
		}		
	}
	
}

