// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/FrameTag.java $
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
 * Identifies a frame tag
 */
public class FrameTag
    extends
        TagNode
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"FRAME"};

    /**
     * Create a new frame tag.
     */
    public FrameTag ()
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
     * Returns the location of the frame.
     * @return The contents of the SRC attribute converted to an absolute URL.
     */
    public String getFrameLocation ()
    {
        String ret;
        
        ret = getAttribute ("SRC");
        if (null == ret)
            ret = "";
        else if (null != getPage ())
            ret = getPage ().getAbsoluteURL (ret);
        
        return (ret);
    }

    /**
     * Sets the location of the frame.
     * @param url The new frame location.
     */
    public void setFrameLocation (String url)
    {
        setAttribute ("SRC", url);
    }

    /**
     * Get the <code>NAME</code> attribute, if any.
     * @return The value of the <code>NAME</code> attribute,
     * or <code>null</code> if the attribute doesn't exist.
     */
    public String getFrameName()
    {
        return (getAttribute ("NAME"));
    }

    /**
     * Return a string representation of the contents of this <code>FRAME</code> tag suitable for debugging.
     * @return A string with this tag's contents.
     */
    public String toString()
    {
        return "FRAME TAG : Frame " +getFrameName() + " at "+getFrameLocation()+"; begins at : "+getStartPosition ()+"; ends at : "+getEndPosition ();
    }
}
