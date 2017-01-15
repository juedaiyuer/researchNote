// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/util/SimpleNodeIterator.java $
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

import org.htmlparser.Node;

/**
 * The HTMLSimpleEnumeration interface is similar to NodeIterator,
 * except that it does not throw exceptions. This interface is useful
 * when using HTMLVector, to enumerate through its elements in a simple
 * manner, without needing to do class casts for Node.
 * @author Somik Raha
 */
public interface SimpleNodeIterator extends NodeIterator
{
    /**
     * Check if more nodes are available.
     * @return <code>true</code> if a call to <code>nextHTMLNode()</code> will
     * succeed.
     */
    public boolean hasMoreNodes();

    /**
     * Get the next node.
     * @return The next node in the HTML stream, or null if there are no more
     * nodes.
     */
    public Node nextNode();
}
