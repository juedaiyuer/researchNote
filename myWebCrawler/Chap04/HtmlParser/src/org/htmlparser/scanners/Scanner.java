// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/scanners/Scanner.java $
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

import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * Generic interface for scanning.
 * Tags needing specialized operations can provide an object that implements
 * this interface via getThisScanner().
 * By default non-composite tags simply perform the semantic action and
 * return while composite tags will gather their children.
 */
public interface Scanner
{
    /**
     * Scan the tag.
     * The Lexer is provided in order to do a lookahead operation.
     * @param tag HTML tag to be scanned for identification.
     * @param lexer Provides html page access.
     * @param stack The parse stack. May contain pending tags that enclose
     * this tag. Nodes on the stack should be considered incomplete.
     * @return The resultant tag (may be unchanged).
     * @exception ParserException if an unrecoverable problem occurs.
     */
    public Tag scan (Tag tag, Lexer lexer, NodeList stack) throws ParserException;
}
