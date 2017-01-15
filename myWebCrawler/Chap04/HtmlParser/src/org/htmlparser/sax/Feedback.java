// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/sax/Feedback.java $
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

package org.htmlparser.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;
import org.xml.sax.SAXException;

/**
 * Mediates between the feedback mechanism of the htmlparser and an error handler.
 */
public class Feedback
    implements
        ParserFeedback
{
    /**
     * The error handler to call back on.
     */
    protected ErrorHandler mErrorHandler;

    /**
     * The locator for tag positions.
     */
    protected Locator mLocator;

    /**
     * Create a feedback/error handler mediator.
     * @param handler The callback object.
     * @param locator A locator for error locations.
     */
    public Feedback (ErrorHandler handler, Locator locator)
    {
        mErrorHandler = handler;
        mLocator = locator;
    }

    /**
     * Information message.
     * <em>Just eats the info message.</em>
     * @param message {@inheritDoc} 
     */
    public void info (String message)
    {
        // swallow
    }

    /**
     * Warning message.
     * Calls {@link ErrorHandler#warning(SAXParseException) ErrorHandler.warning}.
     * @param message {@inheritDoc} 
     */
    public void warning (String message)
    {
        try
        {
            mErrorHandler.warning (
                new SAXParseException (message, mLocator));
        }
        catch (SAXException se)
        {
            se.printStackTrace ();
        }
    }

    /**
     * Error message.
     * Calls {@link ErrorHandler#error(SAXParseException) ErrorHandler.error}.
     * @param message {@inheritDoc} 
     * @param e {@inheritDoc} 
     */
    public void error (String message, ParserException e)
    {
        try
        {
            mErrorHandler.error (
                new SAXParseException (message, mLocator, e));
        }
        catch (SAXException se)
        {
            se.printStackTrace ();
        }
    }
}
