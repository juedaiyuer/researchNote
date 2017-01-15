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
 * Created on Nov 19, 2005
 */
package com.closing.vips;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.EventObject;
import java.util.Map;
import java.util.logging.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.lobobrowser.html.*;
import org.lobobrowser.util.*;
import org.lobobrowser.util.io.IORoutines;
import org.w3c.dom.Document;

/**
 * The <code>SimpleHttpRequest</code> class implements
 * the {@link org.lobobrowser.html.HttpRequest} interface.
 * The <code>HttpRequest</code> implementation provided
 * by this class is simple, with no caching. It creates
 * a new thread for each new asynchronous request.
 * @author J. H. S.
 */
public class SimpleHttpRequest implements HttpRequest {
	private static final Logger logger = Logger.getLogger(SimpleHttpRequest.class.getName());
	private int readyState;
	private int status;
	private String statusText;
	private byte[] responseBytes;
	private final UserAgentContext context;
	private final Proxy proxy;


	/**
	 * Response headers are set in this map after
	 * a response is received.
	 */
	protected java.util.Map responseHeadersMap;
	
	/**
	 * Response headers are set in this string after
	 * a response is received.
	 */	
	protected String responseHeaders;
	
	/**
	 * The <code>URLConnection</code> is assigned to
	 * this field while it is ongoing.
	 */
	protected java.net.URLConnection connection;

	public SimpleHttpRequest(UserAgentContext context, java.net.Proxy proxy) {
		super();
		this.context = context;
		this.proxy = proxy;
	}

	public synchronized int getReadyState() {
		return this.readyState;
	}

	public synchronized String getResponseText() {
		byte[] bytes = this.responseBytes;
		//TODO: proper charset
		try {
			return bytes == null ? null : new String(bytes, "ISO-8859-1");
		} catch(UnsupportedEncodingException uee) {
			return null;
		}
	}

	public synchronized Document getResponseXML() {
		byte[] bytes = this.responseBytes;
		if(bytes == null) {
			return null;
		}
		java.io.InputStream in = new ByteArrayInputStream(bytes);
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		} catch(Exception err) {
			logger.log(Level.WARNING, "Unable to parse response as XML.", err);
			return null;
		}
	}

	public synchronized byte[] getResponseBytes() {
		return this.responseBytes;
	}
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.HttpRequest#getResponseImage()
	 */
	public synchronized Image getResponseImage() {
		byte[] bytes = this.responseBytes;
		if(bytes == null) {
			return null;
		}
		return Toolkit.getDefaultToolkit().createImage(bytes);
	}

	public synchronized int getStatus() {
		return this.status;
	}

	public synchronized String getStatusText() {
		return this.statusText;
	}

	public void abort() {
		URLConnection c;
		synchronized(this) {
			c = this.connection;
		}
		if(c instanceof HttpURLConnection) {
			((HttpURLConnection) c).disconnect();
		}
		else if(c != null) {
			try {
				c.getInputStream().close();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public synchronized String getAllResponseHeaders() {
		return this.responseHeaders;
	}

	public synchronized String getResponseHeader(String headerName) {
		Map headers = this.responseHeadersMap;
		return headers == null ? null : (String) headers.get(headerName);
	}

	public void open(String method, String url) {
		this.open(method, url, true);
	}

	public void open(String method, URL url) {
		this.open(method, url, true, null, null);
	}

	public void open(String method, URL url, boolean asyncFlag) {
		this.open(method, url, asyncFlag, null, null);
	}

	public void open(String method, String url, boolean asyncFlag) {
		this.open(method, url, asyncFlag, null);
	}

	public void open(String method, String url, boolean asyncFlag,
			String userName) {
		this.open(method, url, asyncFlag, userName, null);
	}

	public void open(String method, String url, boolean asyncFlag,
			String userName, String password) {
		try {
			URL urlObj = new URL(url);
			this.open(method, urlObj, asyncFlag, userName, password);
		} catch(MalformedURLException mfu) {
			logger.log(Level.WARNING,"Bad request URL:" + url, mfu);
			this.changeState(HttpRequest.STATE_COMPLETE, 400, "Malformed URI", null);			
		}
	}

	/**
	 * Opens the request by invoking {@link #openSync(String, URL, String, String)}. 
	 * For asynchronous requests, a new thread is created before proceeding.
	 * @param method The request method.
	 * @param url The request URL.
	 * @param asyncFlag Whether the request should be asynchronous.
	 * @param userName The user name of the request (not supported.)
	 * @param password The password of the request (not supported.)
	 */
	public void open(final String method, final java.net.URL url, boolean asyncFlag,
			final String userName, final String password) {
		if(asyncFlag) {
			// Should use a thread pool instead
			new Thread("Request") {
				public void run() {
					openSync(method, url, userName, password);
				}
			}.start();
		}
		else {
			this.openSync(method, url, userName, password);
		}
	}

	private void changeState(int readyState, int status, String statusMessage, byte[] bytes) {
		synchronized(this) {
			this.readyState = readyState;
			this.status = status;
			this.statusText = statusMessage;
			this.responseBytes = bytes;
		}
		this.readyEvent.fireEvent(null);
	}
	
	private String getAllResponseHeaders(URLConnection c) {
		int idx = 0;
		String value;
		StringBuffer buf = new StringBuffer();
		while((value = c.getHeaderField(idx)) != null) {
			String key = c.getHeaderFieldKey(idx);
			buf.append(key); buf.append(": "); buf.append(value);
			idx++;
		}
		return buf.toString();
	}

	/**
	 * This method performs a synchronous URL connection.
	 * It may be overridden.
	 * @param method The request method.
	 * @param url The request URL.
	 * @param userName An optional username.
	 * @param password An optional password.
	 */
	protected void openSync(String method, java.net.URL url, 
			String userName, String password) {
		try {	
			this.abort();
			Proxy proxy = this.proxy;
			URLConnection c = proxy == null || proxy == Proxy.NO_PROXY ? url.openConnection() : url.openConnection(proxy);
			synchronized(this) {
				this.connection = c;
			}
			try {
				c.setRequestProperty("User-Agent", this.context.getUserAgent());
				this.changeState(HttpRequest.STATE_LOADING, 0, "", null);
				java.io.InputStream in = c.getInputStream();
				int contentLength = c.getContentLength();
				byte[] bytes = IORoutines.load(in, contentLength == -1 ? 4096 : contentLength);
				int status = 0;
				String statusText = "";
				if(c instanceof HttpURLConnection) {
					HttpURLConnection hc = (HttpURLConnection) c;
					status = hc.getResponseCode();
					statusText = hc.getResponseMessage();
				}
				synchronized(this) {
					this.responseHeaders = this.getAllResponseHeaders(c);
					this.responseHeadersMap = c.getHeaderFields();
				}
				this.changeState(HttpRequest.STATE_COMPLETE, status, statusText, bytes);
			} finally {
				synchronized(this) {
					this.connection = null;
				}
			}
		} catch(Exception err) {
			this.changeState(HttpRequest.STATE_COMPLETE, err instanceof java.io.FileNotFoundException ? 404 : 400, err.getMessage(), null);
			logger.log(Level.WARNING, "Request failed on url=" + url, err);
		}
	}

	private final EventDispatch readyEvent = new EventDispatch(); 
	
	public void addReadyStateChangeListener(final ReadyStateChangeListener listener) {
		readyEvent.addListener(new GenericEventListener() {
		    public void processEvent(EventObject event) {
		    	listener.readyStateChanged();
		    }
		});
	}
}
