// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/StyleTag.java $
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

import org.htmlparser.scanners.StyleScanner;

/**
 * A StyleTag represents a &lt;style&gt; tag.
 */
public class StyleTag extends CompositeTag
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"STYLE"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"BODY", "HTML"};

    /**
     * Create a new style tag.
     */
    public StyleTag ()
    {
        setThisScanner (new StyleScanner ());
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
     * Return the set of end tag names that cause this tag to finish.
     * @return The names of following end tags that stop further scanning.
     */
    public String[] getEndTagEnders ()
    {
        return (mEndTagEnders);
    }

    /**
     * Get the style data in this tag.
     * @return The HTML of the children of this tag.
     */
    public String getStyleCode ()
    {
        return (getChildrenHTML ());
    }

    /**
     * Print the contents of the style node.
     * @return A string suitable for debugging or a printout.
     */
    public String toString()
    {
        String guts;
        StringBuffer ret;
        
        ret = new StringBuffer ();

        guts = toHtml ();
        guts = guts.substring (1, guts.length () - 1);
        ret.append ("Style node :\n");
        ret.append (guts);
        ret.append ("\n");

        return (ret.toString ());
    }
}
