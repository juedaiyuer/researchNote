// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Claude Duguay
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/util/EncodingChangeException.java $
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

package org.htmlparser.util;

/**
 * The encoding is changed invalidating already scanned characters.
 * When the encoding is changed, as for example when encountering a &lt;META&gt;
 * tag that includes a charset directive in the content attribute that
 * disagrees with the encoding specified by the HTTP header (or the default
 * encoding if none), the parser retraces the bytes it has interpreted so far
 * comparing the characters produced under the new encoding. If the new
 * characters differ from those it has already yielded to the application, it
 * throws this exception to indicate that processing should be restarted under
 * the new encoding.
 * This exception is the object thrown so that applications may distinguish
 * between an encoding change, which may be successfully cured by restarting
 * the parse from the beginning, from more serious errors.
 * @see IteratorImpl
 * @see ParserException
 **/
public class EncodingChangeException
    extends
        ParserException
{
    /**
     * Create an exception idicative of a problematic encoding change.
     * @param message The message describing the error condifion.
     */
    public EncodingChangeException (String message)
    {
        super(message);
    }
}

