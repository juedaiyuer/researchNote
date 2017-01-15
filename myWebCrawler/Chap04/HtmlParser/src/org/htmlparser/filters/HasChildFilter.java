// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/HasChildFilter.java $
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
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeList;

/**
 * This class accepts all tags that have a child acceptable to the filter.
 * It can be set to operate recursively, that is perform a scan down
 * through the node heirarchy in a breadth first traversal looking for any
 * descendant that matches the predicate filter (which stops the search).
 */
public class HasChildFilter
    implements
        NodeFilter
{
    /**
     * The filter to apply to children.
     */
    protected NodeFilter mChildFilter;

    /**
     * Performs a recursive search down the node heirarchy if <code>true</code>.
     */
    protected boolean mRecursive;

    /**
     * Creates a new instance of a HasChildFilter.
     * With no child filter, this would always return <code>false</code>
     * from {@link #accept}.
     */
    public HasChildFilter ()
    {
        this (null);
    }

    /**
     * Creates a new instance of HasChildFilter that accepts nodes
     * with a direct child acceptable to the filter.
     * @param filter The filter to apply to the children.
     */
    public HasChildFilter (NodeFilter filter)
    {
        this (filter, false);
    }

    /**
     * Creates a new instance of HasChildFilter that accepts nodes
     * with a child acceptable to the filter.
     * Of necessity, this applies only to composite tags, i.e. those that can
     * contain other nodes, for example &lt;HTML&gt;&lt;/HTML&gt;.
     * @param filter The filter to apply to children.
     * @param recursive If <code>true</code>, any enclosed node acceptable
     * to the given filter causes the node being tested to be accepted
     * (i.e. a recursive scan through the child nodes down the node
     * heirarchy is performed).
     */
    public HasChildFilter (NodeFilter filter, boolean recursive)
    {
        setChildFilter (filter);
        setRecursive (recursive);    }

    /**
     * Get the filter used by this HasParentFilter.
     * @return The filter to apply to parents.
     */
    public NodeFilter getChildFilter ()
    {
        return (mChildFilter);
    }

    /**
     * Set the filter for this HasParentFilter.
     * @param filter The filter to apply to parents in {@link #accept}.
     */
    public void setChildFilter (NodeFilter filter)
    {
        mChildFilter = filter;
    }

    /**
     * Get the recusion setting for the filter.
     * @return Returns <code>true</code> if the filter is recursive
     * up the node heirarchy.
     */
    public boolean getRecursive ()
    {
        return (mRecursive);
    }

    /**
     * Sets whether the filter is recursive or not.
     * @param recursive The recursion setting for the filter.
     */
    public void setRecursive (boolean recursive)
    {
        mRecursive = recursive;
    }

    /**
     * Accept tags with children acceptable to the filter.
     * @param node The node to check.
     * @return <code>true</code> if the node has an acceptable child,
     * <code>false</code> otherwise.
     */
    public boolean accept (Node node)
    {
        CompositeTag tag;
        NodeList children;
        boolean ret;

        ret = false;
        if (node instanceof CompositeTag)
        {
            tag = (CompositeTag)node;
            children = tag.getChildren ();
            if (null != children)
            {
                for (int i = 0; !ret && i < children.size (); i++)
                    if (getChildFilter ().accept (children.elementAt (i)))
                        ret = true;
                // do recursion after all children are checked
                // to get breadth first traversal
                if (!ret && getRecursive ())
                    for (int i = 0; !ret && i < children.size (); i++)
                        if (accept (children.elementAt (i)))
                            ret = true;
            }
        }

        return (ret);
    }
}
