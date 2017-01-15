// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 John Derrick
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/LinkRegexFilter.java $
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.LinkTag;

/**
 * This class accepts tags of class LinkTag that contain a link matching a given
 * regex pattern. Use this filter to extract LinkTag nodes with URLs that match
 * the desired regex pattern.
 */
public class LinkRegexFilter implements NodeFilter
{
    /**
     * The regular expression to use on the link.
     */
    protected Pattern mRegex;

    /**
     * Creates a LinkRegexFilter that accepts LinkTag nodes containing
     * a URL that matches the supplied regex pattern.
     * The match is case insensitive.
     * @param regexPattern The pattern to match.
     */
    public LinkRegexFilter (String regexPattern)
    {
        this (regexPattern, true);
    }

    /**
     * Creates a LinkRegexFilter that accepts LinkTag nodes containing
     * a URL that matches the supplied regex pattern.
     * @param regexPattern The regex pattern to match.
     * @param caseSensitive Specifies case sensitivity for the matching process.
     */
    public LinkRegexFilter (String regexPattern, boolean caseSensitive)
    {
        if (caseSensitive)
            mRegex = Pattern.compile (regexPattern);
        else
            mRegex = Pattern.compile (regexPattern,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /**
     * Accept nodes that are a LinkTag and have a URL
     * that matches the regex pattern supplied in the constructor.
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
            Matcher matcher = mRegex.matcher (link);
            ret = matcher.find ();
        }

        return (ret);
    }
}
