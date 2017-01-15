// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/HasSiblingFilter.java $
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
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;

/**
 * This class accepts all tags that have a sibling acceptable to another filter.
 * End tags are not considered to be siblings of any tag.
 */
public class HasSiblingFilter
    implements
        NodeFilter
{
    /**
     * The filter to apply to the sibling.
     */
    protected NodeFilter mSiblingFilter;

    /**
     * Creates a new instance of HasSiblingFilter.
     * With no sibling filter, this would always return <code>false</code>
     * from {@link #accept}.
     */
    public HasSiblingFilter ()
    {
        this (null);
    }

    /**
     * Creates a new instance of HasSiblingFilter that accepts nodes
     * with sibling acceptable to the filter.
     * @param filter The filter to apply to the sibling.
     */
    public HasSiblingFilter (NodeFilter filter)
    {
        setSiblingFilter (filter);
    }

    /**
     * Get the filter used by this HasSiblingFilter.
     * @return The filter to apply to siblings.
     */
    public NodeFilter getSiblingFilter ()
    {
        return (mSiblingFilter);
    }

    /**
     * Set the filter for this HasSiblingFilter.
     * @param filter The filter to apply to siblings in {@link #accept}.
     */
    public void setSiblingFilter (NodeFilter filter)
    {
        mSiblingFilter = filter;
    }

    /**
     * Accept tags with a sibling acceptable to the filter.
     * @param node The node to check.
     * @return <code>true</code> if the node has an acceptable sibling,
     * <code>false</code> otherwise.
     */
    public boolean accept (Node node)
    {
        Node parent;
        NodeList siblings;
        int count;
        boolean ret;

        ret = false;
        if (!(node instanceof Tag) || !((Tag)node).isEndTag ())
        {
            parent = node.getParent ();
            if (null != parent)
            {
                siblings = parent.getChildren ();
                if (null != siblings)
                {
                    count = siblings.size ();
                    for (int i = 0; !ret && (i < count); i++)
                        if (getSiblingFilter ().accept (siblings.elementAt (i)))
                            ret = true;
                }
            }
        }

        return (ret);
    }
}
