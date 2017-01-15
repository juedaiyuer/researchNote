// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/scanners/TagScanner.java $
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

package org.htmlparser.scanners;

import java.io.Serializable;

import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * TagScanner is an abstract superclass, subclassed to create specific scanners.
 * When asked to scan the tag, this class does nothing other than perform the
 * tag's semantic action.
 * Use TagScanner when you have a meta task to do like setting the BASE url for
 * the page when a BASE tag is encountered.
 * If you want to match end tags and handle special syntax between tags,
 * then you'll probably want to subclass {@link CompositeTagScanner} instead.
 */
public class TagScanner
    implements
        Scanner,
        Serializable
{
    /**
     * Create a (non-composite) tag scanner.
     */
    public TagScanner ()
    {
    }

    /**
     * Scan the tag.
     * For this implementation, the only operation is to perform the tag's
     * semantic action.
     * @param tag The tag to scan.
     * @param lexer Provides html page access.
     * @param stack The parse stack. May contain pending tags that enclose
     * this tag.
     * @return The resultant tag (may be unchanged).
     */
    public Tag scan (Tag tag, Lexer lexer, NodeList stack) throws ParserException
    {
        tag.doSemanticAction ();

        return (tag);
    }
}
