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

package org.lobobrowser.html.test;

import org.lobobrowser.html.*;

import java.util.logging.*;

/**
 * The <code>SimpleHtmlParserContext</code> is a simple implementation
 * of the {@link org.lobobrowser.html.HtmlParserContext} interface. Methods
 * in this class should be overridden to provide functionality
 * such as cookies.
 * @author J. H. S.
 */
public class SimpleHtmlParserContext implements HtmlParserContext {
	private static final Logger logger = Logger.getLogger(SimpleHtmlParserContext.class.getName());
	
	public SimpleHtmlParserContext() {
		super();
	}

	public String getCookie() {
		return "";
	}
	
	public void setCookie(String cookie) {
		this.warn("setCookie(): Not overridden");
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
}

