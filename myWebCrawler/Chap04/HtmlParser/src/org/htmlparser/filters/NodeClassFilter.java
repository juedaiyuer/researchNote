// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/NodeClassFilter.java $
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

package org.htmlparser.filters;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

/**
 * This class accepts all tags of a given class.
 */
public class NodeClassFilter implements NodeFilter
{
    /**
     * The class to match.
     */
    protected Class mClass;

    /**
     * Creates a NodeClassFilter that accepts Html tags.
     */
    public NodeClassFilter ()
    {
        this (org.htmlparser.tags.Html.class);
    }

    /**
     * Creates a NodeClassFilter that accepts tags of the given class.
     * @param cls The class to match.
     */
    public NodeClassFilter (Class cls)
    {
        mClass = cls;
    }

    /**
     * Get the class to match.
     * @return Returns the class.
     */
    public Class getMatchClass ()
    {
        return (mClass);
    }

    /**
     * Set the class to match.
     * @param cls The node class to match.
     */
    public void setMatchClass (Class cls)
    {
        mClass = cls;
    }

    /**
     * Accept nodes that are assignable from the class provided in
     * the constructor.
     * @param node The node to check.
     * @return <code>true</code> if the node is the right class,
     * <code>false</code> otherwise.
     */
    public boolean accept (Node node)
    {
        return ((null != mClass) && mClass.isAssignableFrom (node.getClass ()));
    }
}
