package org.lobobrowser.html.test;

import java.security.Policy;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

import org.lobobrowser.html.HttpRequest;
import org.lobobrowser.html.UserAgentContext;

/**
 * Simple implementation of {@link org.lobobrowser.html.UserAgentContext}.
 */
public class SimpleUserAgentContext implements UserAgentContext {
	private static final Logger logger = Logger.getLogger(SimpleUserAgentContext.class.getName());
	private static final Set mediaNames = new HashSet();
	
	static {
		// Media names claimed by this context.
		Set mn = mediaNames;
	    mn.add("screen");	
	    mn.add("tv");	
	    mn.add("tty");	
	    mn.add("all");	
	}
	
	/**
	 * This implementation returns true for certain media names,
	 * such as <code>screen</code>.
	 */
	public boolean isMedia(String mediaName) {
		return mediaNames.contains(mediaName.toLowerCase());
	}

	/**
	 * Creates a {@link org.lobobrowser.html.test.SimpleHttpRequest} instance. 
	 * Override if a custom mechanism to make requests is needed.
	 */
	public HttpRequest createHttpRequest() {
		return new SimpleHttpRequest(this, this.getProxy());
	}	

	/**
	 * Gets the connection proxy used in requests created
	 * by {@link #createHttpRequest()} by default. It returns
	 * <code>Proxy.NO_PROXY</code>, but it may be overridden.
	 */
	protected java.net.Proxy getProxy() {
		return java.net.Proxy.NO_PROXY;
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

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getAppCodeName()
	 */
	public String getAppCodeName() {
		return "Cobra";
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getAppMinorVersion()
	 */
	public String getAppMinorVersion() {
		return "0";
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getAppName()
	 */
	public String getAppName() {
		return "Browser";
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getAppVersion()
	 */
	public String getAppVersion() {
		return "1";
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getBrowserLanguage()
	 */
	public String getBrowserLanguage() {
		return "EN";
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getPlatform()
	 */
	public String getPlatform() {
		return System.getProperty("os.name");
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#getUserAgent()
	 */
	public String getUserAgent() {
		return "Mozilla/4.0 (compatible; MSIE 6.0;) Cobra/Simple";
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.HtmlParserContext#isCookieEnabled()
	 */
	public boolean isCookieEnabled() {
		this.warn("isCookieEnabled(): Not overridden - returning false");
		return false;
	}

	public String getCookie(java.net.URL url) {
		this.warn("getCookie(): Method not overridden; returning null.");
		return "";
	}

	/**
	 * Returns <code>true</code>. Implementations wishing to
	 * disable JavaScript may override this method.
	 */
	public boolean isScriptingEnabled() {
		return true;
	}

	public void setCookie(java.net.URL url, String cookieSpec) {
		this.warn("setCookie(): Method not overridden.");
	}

	/**
	 * Returns <code>null</code>. This method must be overridden
	 * if JavaScript code is untrusted.
	 */
	public Policy getSecurityPolicy() {
		return null;
	}

	/**
	 * Returns -1. Override to provide a different Rhino optimization level.
	 */
	public int getScriptingOptimizationLevel() {
		return -1;
	}
}
