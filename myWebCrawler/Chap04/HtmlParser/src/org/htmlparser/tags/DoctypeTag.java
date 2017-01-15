// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/DoctypeTag.java $
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

package org.htmlparser.tags;

import org.htmlparser.nodes.TagNode;

/**
 * The HTML Document Declaration Tag can identify &lt;&#33;DOCTYPE&gt; tags.
 */
public class DoctypeTag
    extends
        TagNode
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"!DOCTYPE"};

    /**
     * Create a new &#33;doctype tag.
     */
    public DoctypeTag ()
    {
    }

    /**
     * Return the set of names handled by this tag.
     * @return The names to be matched that create tags of this type.
     */
    public String[] getIds ()
    {
        return (mIds);
    }

    /**
     * Return a string representation of the contents of this <code>!DOCTYPE</code> tag suitable for debugging.
     * @return The contents of the document declaration tag as a string.
     */
    public String toString()
    {
        String guts = toHtml();
        guts = guts.substring (1, guts.length () - 2);
        return "Doctype Tag : "+guts+"; begins at : "+getStartPosition ()+"; ends at : "+getEndPosition ();
    }
}
