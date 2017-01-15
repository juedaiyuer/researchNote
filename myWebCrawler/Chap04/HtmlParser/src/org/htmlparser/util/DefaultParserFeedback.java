// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Claude Duguay
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/util/DefaultParserFeedback.java $
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

import java.io.Serializable;

/**
 * Default implementation of the HTMLParserFeedback interface.
 * This implementation prints output to the console but users
 * can implement their own classes to support alternate behavior.
 *
 * @see ParserFeedback
 * @see FeedbackManager
 */
public class DefaultParserFeedback
    implements
        ParserFeedback,
        Serializable
{
    /**
     * Constructor argument for a quiet feedback.
     */
    public static final int QUIET = 0;

    /**
     * Constructor argument for a normal feedback.
     */
    public static final int NORMAL = 1;

    /**
     * Constructor argument for a debugging feedback.
     */
    public static final int DEBUG = 2;

    /**
     * Verbosity level.
     * Corresponds to constructor arguments:
     * <pre>
     *   DEBUG = 2;
     *   NORMAL = 1;
     *   QUIET = 0;
     * </pre>
     */
    protected int mMode;

    /**
     * Construct a feedback object of the given type.
     * @param mode The type of feedback:
     * <pre>
     *   DEBUG - verbose debugging with stack traces
     *   NORMAL - normal messages
     *   QUIET - no messages
     * </pre>
     * @exception IllegalArgumentException if mode is not
     * QUIET, NORMAL or DEBUG.
     */
    public DefaultParserFeedback (int mode)
    {
        if (mode<QUIET||mode>DEBUG)
            throw new IllegalArgumentException (
                "illegal mode ("
                + mode
                + "), must be one of: QUIET, NORMAL, DEBUG");
        mMode = mode;
    }

    /**
     * Construct a NORMAL feedback object.
     */
    public DefaultParserFeedback ()
    {
        this (NORMAL);
    }

    /**
     * Print an info message.
     * @param message The message to print.
     */
    public void info (String message)
    {
        if (QUIET != mMode)
            System.out.println ("INFO: " + message);
    }

    /**
     * Print an warning message.
     * @param message The message to print.
     */
    public void warning (String message)
    {
        if (QUIET != mMode)
            System.out.println ("WARNING: " + message);
    }

    /**
     * Print an error message.
     * @param message The message to print.
     * @param exception The exception for stack tracing.
     */
    public void error (String message, ParserException exception)
    {
        if (QUIET != mMode)
        {
            System.out.println ("ERROR: " + message);
            if (DEBUG == mMode && (null != exception))
                exception.printStackTrace ();
        }
    }
}

