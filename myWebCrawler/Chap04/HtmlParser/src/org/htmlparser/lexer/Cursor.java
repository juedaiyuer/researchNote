// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/lexer/Cursor.java $
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

package org.htmlparser.lexer;

import java.io.Serializable;
import org.htmlparser.util.sort.Ordered;

/**
 * A bookmark in a page.
 * This class remembers the page it came from and its position within the page.
 */
public class Cursor
    implements
        Serializable,
        Ordered,
        Cloneable
{
    /**
     * This cursor's position.
     */
    protected int mPosition;

    /**
     * This cursor's page.
     */
    protected Page mPage;

    /**
     * Construct a <code>Cursor</code> from the page and position given.
     * @param page The page this cursor is on.
     * @param offset The character offset within the page.
     */
    public Cursor (Page page, int offset)
    {
        mPage = page;
        mPosition = offset;
    }

    /**
     * Get this cursor's page.
     * @return The page associated with this cursor.
     */
    public Page getPage ()
    {
        return (mPage);
    }

    /**
     * Get the position of this cursor.
     * @return The cursor position.
     */
    public int getPosition ()
    {
        return (mPosition);
    }

    /**
     * Set the position of this cursor.
     * @param position The new cursor position.
     */
    public void setPosition (int position)
    {
        mPosition = position;
    }

    /**
     * Move the cursor position ahead one character.
     */
    public void advance ()
    {
        mPosition++;
    }

    /**
     * Move the cursor position back one character.
     */
    public void retreat ()
    {
        mPosition--;
        if (0 > mPosition)
            mPosition = 0;
    }

    /**
     * Make a new cursor just like this one.
     * @return The new cursor positioned where <code>this</code> one is,
     * and referring to the same page.
     */
    public Cursor dup ()
    {
        try
        {
            return ((Cursor)clone ());
        }
        catch (CloneNotSupportedException cnse)
        {
            return (new Cursor (getPage (), getPosition ()));
        }
    }

    /**
     * Return a string representation of this cursor
     * @return A string of the form "n[r,c]", where n is the character position,
     * r is the row (zero based) and c is the column (zero based) on the page.
     */
    public String toString ()
    {
        StringBuffer ret;

        ret = new StringBuffer (9 * 3 + 3); // three ints and delimiters
        ret.append (getPosition ());
        ret.append ("[");
        if (null != mPage)
            ret.append (mPage.row (this));
        else
            ret.append ("?");
        ret.append (",");
        if (null != mPage)
            ret.append (mPage.column (this));
        else
            ret.append ("?");
        ret.append ("]");

        return (ret.toString ());
    }

    //
    // Ordered interface
    //

    /**
     * Compare one reference to another.
     * @param that The object to compare this to.
     * @return A negative integer, zero, or a positive
     * integer as this object is less than, equal to,
     * or greater than that object.
     * @see org.htmlparser.util.sort.Ordered
     */
    public int compare (Object that)
    {
        Cursor r = (Cursor)that;
        return (getPosition () - r.getPosition ());
    }
}
