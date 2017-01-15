// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/http/HttpHeader.java $
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility methods to display HTTP headers.
 */
public class HttpHeader
{
    
    /**
     * Private constructor.
     * This class is completely static.
     */
    private HttpHeader ()
    {
    }

    /**
     * Gets the request header for the connection.
     * <em>This header is generated from the contents of the connection
     * and may not be exactly the same as the request that will be sent.</em>
     * @param connection The connection to convert into an HTTP request header.
     * @return The string that would be sent by the HTTP request.
     */
    public static String getRequestHeader (HttpURLConnection connection)
    {
        // dump it
        StringBuffer buffer;
        Map map;
        String key;
        List items;

        buffer = new StringBuffer (1024);
        buffer.append (connection.getRequestMethod ());
        buffer.append (" ");
        buffer.append (connection.getURL ());
        buffer.append (" HTTP/1.1\n");
        map  = connection.getRequestProperties ();
        for (Iterator iter = map.keySet ().iterator (); iter.hasNext (); )
        {
            key = (String)iter.next ();
            items = (List)map.get (key);
            buffer.append (key);
            buffer.append (": ");
            for (int i = 0; i < items.size (); i++)
            {
                if (0 != i)
                    buffer.append (", ");
                buffer.append (items.get (i));
            }
            buffer.append ("\n");
        }

        return (buffer.toString ());
    }

    /**
     * Gets the response header for the connection.
     * Calling this method on an un-connected connection will
     * generate an error, as will an attempt to get information
     * from a connected but invalid connection.
     * <em>This header is generated from the contents of the connection
     * and may not be exactly the same as the response that was received.</em>
     * @param conn The connection to convert into an HTTP response header.
     * @return The string that was sent as the HTTP response.
     */
    public static String getResponseHeader (HttpURLConnection conn)
    {
        // dump it
        StringBuffer buffer;
        int code;
        String message;
        String key;
        String value;

        buffer = new StringBuffer (1024);
        try
        {
            code = conn.getResponseCode ();
            if (-1 != code)
            {
                message = conn.getResponseMessage ();
                for (int i = 0; null != (value = conn.getHeaderField (i)); i++)
                {
                    key = conn.getHeaderFieldKey (i);
                    if ((null == key) && (0 == i))
                    {
                        buffer.append ("HTTP/1.1 ");
                        buffer.append (code);
                        buffer.append (" ");
                        buffer.append (message);
                        buffer.append ("\n");
                    }
                    else
                    {
                        if (null != key)
                        {
                            buffer.append (key);
                            buffer.append (": ");
                        }
                        buffer.append (value);
                        buffer.append ("\n");
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            buffer.append (ioe.toString ());
        }

        return (buffer.toString ());
    }
}
