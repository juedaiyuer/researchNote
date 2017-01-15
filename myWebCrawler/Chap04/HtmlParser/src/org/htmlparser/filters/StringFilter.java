// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/filters/StringFilter.java $
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
import org.htmlparser.Text;

/**
 * This class accepts all string nodes containing the given string.
 * This is a fairly simplistic filter, so for more sophisticated
 * string matching, for example newline and whitespace handling,
 * use a {@link RegexFilter} instead.
 */
public class StringFilter
    implements
        NodeFilter
{
    /**
     * The string to search for.
     */
    protected String mPattern;

    /**
     * The string to really search for (converted to uppercase if necessary).
     */
    protected String mUpperPattern;

    /**
     * Case sensitive toggle.
     * If <code>true</code> strings are compared with case sensitivity.
     */
    protected boolean mCaseSensitive;

    /**
     * The locale to use converting to uppercase in case insensitive searches.
     */
    protected Locale mLocale;

    /**
     * Creates a new instance of StringFilter that accepts all string nodes.
     */
    public StringFilter ()
    {
        this ("", false);
    }

    /**
     * Creates a StringFilter that accepts text nodes containing a string.
     * The comparison is case insensitive, with conversions done using
     * the default <code>Locale</code>.
     * @param pattern The pattern to search for.
     */
    public StringFilter (String pattern)
    {
        this (pattern, false);
    }

    /**
     * Creates a StringFilter that accepts text nodes containing a string.
     * @param pattern The pattern to search for.
     * @param sensitive If <code>true</code>, comparisons are performed
     * respecting case, with conversions done using the default
     * <code>Locale</code>.
     */
    public StringFilter (String pattern, boolean sensitive)
    {
        this (pattern, sensitive, null);
    }

    /**
     * Creates a StringFilter that accepts text nodes containing a string.
     * @param pattern The pattern to search for.
     * @param sensitive If <code>true</code>, comparisons are performed
     * respecting case.
     * @param locale The locale to use when converting to uppercase.
     * If <code>null</code>, the default <code>Locale</code> is used.
     */
    public StringFilter (String pattern, boolean sensitive, Locale locale)
    {
        mPattern = pattern;
        mCaseSensitive = sensitive;
        mLocale = (null == locale) ? Locale.getDefault () : locale;
        setUpperPattern ();
    }

    //
    // protected methods
    //

    /**
     * Set the real (upper case) comparison string.
     */
    protected void setUpperPattern ()
    {
        if (getCaseSensitive ())
            mUpperPattern = getPattern ();
        else
            mUpperPattern = getPattern ().toUpperCase (getLocale ());
   }

    /**
     * Get the case sensitivity.
     * @return Returns the case sensitivity.
     */
    public boolean getCaseSensitive ()
    {
        return (mCaseSensitive);
    }

    /**
     * Set case sensitivity on or off.
     * @param sensitive If <code>false</code> searches for the
     * string are case insensitive.
     */
    public void setCaseSensitive (boolean sensitive)
    {
        mCaseSensitive = sensitive;
        setUpperPattern ();
    }

    /**
     * Get the locale for uppercase conversion.
     * @return Returns the locale.
     */
    public Locale getLocale ()
    {
        return (mLocale);
    }

    /**
     * Set the locale for uppercase conversion.
     * @param locale The locale to set.
     */
    public void setLocale (Locale locale)
    {
        mLocale = locale;
        setUpperPattern ();
    }

    /**
     * Get the search pattern.
     * @return Returns the pattern.
     */
    public String getPattern ()
    {
        return (mPattern);
    }

    /**
     * Set the search pattern.
     * @param pattern The pattern to set.
     */
    public void setPattern (String pattern)
    {
        mPattern = pattern;
        setUpperPattern ();
    }

    //
    // NodeFilter interface
    //

    /**
     * Accept string nodes that contain the string.
     * @param node The node to check.
     * @return <code>true</code> if <code>node</code> is a {@link Text} node
     * and contains the pattern string, <code>false</code> otherwise.
     */
    public boolean accept (Node node)
    {
        String string;
        boolean ret;

        ret = false;
        if (node instanceof Text)
        {
            string = ((Text)node).getText ();
            if (!getCaseSensitive ())
                string = string.toUpperCase (getLocale ());
            ret = (-1 != string.indexOf (mUpperPattern));
        }

        return (ret);
    }
}
