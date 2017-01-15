// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/TagNameFilter.java $
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

import java.util.Locale;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;

/**
 * This class accepts all tags matching the tag name.
 */
public class TagNameFilter
    implements
        NodeFilter
{
    /**
     * The tag name to match.
     */
    protected String mName;

    /**
     * Creates a new instance of TagNameFilter.
     * With no name, this would always return <code>false</code>
     * from {@link #accept}.
     */
    public TagNameFilter ()
    {
        this ("");
    }

    /**
     * Creates a TagNameFilter that accepts tags with the given name.
     * @param name The tag name to match.
     */
    public TagNameFilter (String name)
    {
        mName = name.toUpperCase (Locale.ENGLISH);
    }

    /**
     * Get the tag name.
     * @return Returns the name of acceptable tags.
     */
    public String getName ()
    {
        return (mName);
    }

    /**
     * Set the tag name.
     * @param name The name of the tag to accept.
     */
    public void setName (String name)
    {
        mName = name;
    }

    /**
     * Accept nodes that are tags and have a matching tag name.
     * This discards non-tag nodes and end tags.
     * The end tags are available on the enclosing non-end tag.
     * @param node The node to check.
     * @return <code>true</code> if the tag name matches,
     * <code>false</code> otherwise.
     */
    public boolean accept (Node node)
    {
        return ((node instanceof Tag)
                && !((Tag)node).isEndTag ()
                && ((Tag)node).getTagName ().equals (mName));
    }
}
