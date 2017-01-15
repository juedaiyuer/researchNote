// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/FrameSetTag.java $
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

import java.util.Locale;

import org.htmlparser.Node;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * Identifies an frame set tag.
 */
public class FrameSetTag extends CompositeTag
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"FRAMESET"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"HTML"};

    /**
     * Create a new frame set tag.
     */
    public FrameSetTag ()
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
     * Return the set of end tag names that cause this tag to finish.
     * @return The names of following end tags that stop further scanning.
     */
    public String[] getEndTagEnders ()
    {
        return (mEndTagEnders);
    }

    /**
     * Return a string representation of the contents of this <code>FRAMESET</code> tag suitable for debugging.
     * @return A string with this tag's contents.
     */
    public String toString()
    {
        return "FRAMESET TAG : begins at : "+getStartPosition ()+"; ends at : "+getEndPosition ();
    }

    /**
     * Returns the frames.
     * @return The children of this tag.
     */
    public NodeList getFrames()
    {
        return (getChildren());
    }

    /**
     * Gets a frame by name.
     * Names are checked without case sensitivity and conversion to uppercase
     * is performed with an English locale.
     * @param name The name of the frame to retrieve.
     * @return The specified frame or <code>null</code> if it wasn't found.
     */
    public FrameTag getFrame (String name)
    {
        return (getFrame (name, Locale.ENGLISH));
    }

    /**
     * Gets a frame by name.
     * Names are checked without case sensitivity and conversion to uppercase
     * is performed with the locale provided.
     * @param name The name of the frame to retrieve.
     * @param locale The locale to use when converting to uppercase.
     * @return The specified frame or <code>null</code> if it wasn't found.
     */
    public FrameTag getFrame (String name, Locale locale)
    {
        Node node;
        FrameTag ret;

        ret = null;
        
        name = name.toUpperCase (locale);
        for (SimpleNodeIterator e = getFrames ().elements (); e.hasMoreNodes () && (null == ret); )
        {
            node = e.nextNode();
            if (node instanceof FrameTag)
            {
                ret = (FrameTag)node;
                if (!ret.getFrameName ().toUpperCase (locale).equals (name))
                    ret = null;
            }
        }

        return (ret);
    }

    /**
     * Sets the frames (children of this tag).
     * @param frames The frames to set
     */
    public void setFrames(NodeList frames)
    {
        setChildren (frames);
    }
}
