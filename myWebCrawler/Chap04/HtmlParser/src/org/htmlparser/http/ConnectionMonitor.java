// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/http/ConnectionMonitor.java $
// $Author: derrickoswald $
// $Date: 2006-09-16 10:44:17 -0400 (Sat, 16 Sep 2006) $
// $Revision: 4 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the Common Public License; either
// version 1.0 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// Common Public License for more details.
//
// You should have received a copy of the Common Public License
// along with this library; if not, the license is available from
// the Open Source Initiative (OSI) website:
//   http://opensource.org/licenses/cpl1.0.php
                                                                                                                            
package org.htmlparser.http;

import java.net.HttpURLConnection;

import org.htmlparser.util.ParserException;

/**
 * Interface for HTTP connection notification callbacks.
 */
public interface ConnectionMonitor
{
    /**
     * Called just prior to calling connect.
     * The connection has been conditioned with proxy, URL user/password,
     * and cookie information. It is still possible to adjust the
     * connection, to alter the request method for example. 
     * @param connection The connection which is about to be connected.
     * @exception ParserException This exception is thrown if the connection
     * monitor wants the ConnectionManager to bail out.
     */
    void preConnect (HttpURLConnection connection)
    	throws
            ParserException;

    /** Called just after calling connect.
     * The response code and header fields can be examined.
     * @param connection The connection that was just connected.
     * @exception ParserException This exception is thrown if the connection
     * monitor wants the ConnectionManager to bail out.
     */
    void postConnect (HttpURLConnection connection)
    	throws
            ParserException;
}
