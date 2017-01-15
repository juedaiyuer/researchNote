// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/OptionTag.java $
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

/**
 * An option tag within a form.
 */
public class OptionTag extends CompositeTag
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"OPTION"};

    /**
     * The set of tag names that indicate the end of this tag.
     */
    private static final String[] mEnders = new String[] {"INPUT", "TEXTAREA", "SELECT", "OPTION"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"SELECT", "FORM", "BODY", "HTML"};

    /**
     * Create a new option tag.
     */
    public OptionTag ()
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
     * Return the set of tag names that cause this tag to finish.
     * @return The names of following tags that stop further scanning.
     */
    public String[] getEnders ()
    {
        return (mEnders);
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
     * Get the <code>VALUE</code> attribute, if any.
     * @return The value of the <code>VALUE</code> attribute,
     * or <code>null</code> if the attribute doesn't exist.
     */
    public String getValue ()
    {
        return (getAttribute ("VALUE"));
    }

    /**
     * Set the value of the value attribute.
     * @param value The new value of the <code>VALUE</code> attribute.
     */
    public void setValue (String value)
    {
        this.setAttribute("VALUE",value);
    }

    /**
     * Get the text of this option.
     * @return The textual contents of this <code>OPTION</code> tag.
     */
    public String getOptionText()
    {
        return toPlainTextString();
    }

    /**
     * Return a string representation of this node suitable for debugging.
     * @return The value and text of this tag in a string.
     */
    public String toString()
    {
        String output = "OPTION VALUE: " + getValue() + " TEXT: " + getOptionText()+"\n";
        return output;
    }

}
