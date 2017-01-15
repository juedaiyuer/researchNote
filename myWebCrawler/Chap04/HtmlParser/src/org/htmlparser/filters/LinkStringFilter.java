// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 John Derrick
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/LinkStringFilter.java $
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
import org.htmlparser.tags.LinkTag;

/**
 * This class accepts tags of class LinkTag that contain a link matching a given
 * pattern string. Use this filter to extract LinkTag nodes with URLs containing
 * the desired string.
 */
public class LinkStringFilter implements NodeFilter
{
    /**
     * The pattern to search for in the link.
     */
    protected String mPattern;

    /**
     * Flag indicating case sensitive/insensitive search.
     */
    protected boolean mCaseSensitive;

    /**
     * Creates a LinkStringFilter that accepts LinkTag nodes containing
     * a URL that matches the supplied pattern.
     * The match is case insensitive.
     * @param pattern The pattern to match.
     */
    public LinkStringFilter (String pattern)
    {
        this (pattern, false);
    }

    /**
     * Creates a LinkStringFilter that accepts LinkTag nodes containing
     * a URL that matches the supplied pattern.
     * @param pattern The pattern to match.
     * @param caseSensitive Specifies case sensitivity for the matching process.
     */
    public LinkStringFilter (String pattern, boolean caseSensitive)
    {
        mPattern = pattern;
        mCaseSensitive = caseSensitive;
    }

    /**
     * Accept nodes that are a LinkTag and
     * have a URL that matches the pattern supplied in the constructor.
     * @param node The node to check.
     * @return <code>true</code> if the node is a link with the pattern.
     */
    public boolean accept (Node node)
    {
        boolean ret;

        ret = false;
        if (LinkTag.class.isAssignableFrom (node.getClass ()))
        {
            String link = ((LinkTag)node).getLink ();
            if (mCaseSensitive)
            {
                if (link.indexOf (mPattern) > -1)
                    ret = true;
            }
            else
            {
                if (link.toUpperCase ().indexOf (mPattern.toUpperCase ()) > -1)
                    ret = true;
            }
        }

        return (ret);
    }
}
