package org.lobobrowser.html;

/**
 * The user agent context.
 */
public interface UserAgentContext {
//	/**
//	 * Informs context about a warning.
//	 */
//	public void warn(String message, Throwable throwable);
//
//	/**
//	 * Informs context about an error.
//	 */
//	public void error(String message, Throwable throwable);
//	
//	/**
//	 * Informs context about a warning.
//	 */
//	public void warn(String message);
//
//	/**
//	 * Informs context about an error.
//	 */
//	public void error(String message);
//		
	/**
	 * Creates an instance of {@link org.lobobrowser.html.HttpRequest} which
	 * can be used by the renderer to load images and implement the
	 * Javascript XMLHttpRequest class. 
	 */
	public HttpRequest createHttpRequest();

	/**
	 * Gets browser "code" name.
	 */
	public String getAppCodeName();

	/**
	 * Gets browser application name.
	 */
	public String getAppName();

	/**
	 * Gets browser application version.
	 */
	public String getAppVersion();

	/**
	 * Gets browser application minor version.
	 */
	public String getAppMinorVersion();

	/**
	 * Gets browser language code. See <a href="http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">ISO 639-1 codes</a>.
	 */
	public String getBrowserLanguage();

	/**
	 * Returns a boolean value indicating whether cookies are
	 * enabled in the user agent.
	 */
	public boolean isCookieEnabled();
	
	/**
	 * Returns a boolean value indicating whether scripting
	 * is enabled in the user agent.
	 */
	public boolean isScriptingEnabled();
	
	/**
	 * Gets the name of the user's operating system.
	 */
	public String getPlatform();

	/**
	 * Should return the string used in
	 * the User-Agent header. 
	 */
	public String getUserAgent();
	
	/**
	 * Method used to implement document.cookie property.
	 */
	public String getCookie(java.net.URL url);

	/**
	 * Method used to implement document.cookie property.
	 * @param cookieSpec Specification of cookies, as they
	 *                   would appear in the Set-Cookie
	 *                   header value of HTTP.
	 */
	public void setCookie(java.net.URL url, String cookieSpec);
	
	/**
	 * Gets the security policy for scripting. Return <code>null</code>
	 * if JavaScript code is trusted.
	 */
	public java.security.Policy getSecurityPolicy();
	
	/**
	 * Gets the scripting optimization level, which is a value
	 * equivalent to Rhino's optimization level.
	 */
	public int getScriptingOptimizationLevel();
	
	/**
	 * Returns true if the current media matches the name provided.
	 * @param mediaName Media name, which
	 * may be screen, tty, etc. (See <a href="http://www.w3.org/TR/REC-html40/types.html#type-media-descriptors">HTML Specification</a>).
	 */
	public boolean isMedia(String mediaName);
}
