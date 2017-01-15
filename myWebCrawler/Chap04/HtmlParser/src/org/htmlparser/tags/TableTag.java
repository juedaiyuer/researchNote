// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/TableTag.java $
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

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.IsEqualFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.util.NodeList;

/**
 * A table tag.
 */
public class TableTag extends CompositeTag
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"TABLE"};

    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"BODY", "HTML"};

    /**
     * Create a new table tag.
     */
    public TableTag ()
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
     * Get the row tags within this table.
     * @return The rows directly contained by this table.
     */
    public TableRow[] getRows ()
    {
        NodeList kids;
        NodeClassFilter cls;
        HasParentFilter recursion;
        NodeFilter filter;
        TableRow[] ret;

        kids = getChildren ();
        if (null != kids)
        {
            cls = new NodeClassFilter (TableTag.class);
            recursion = new HasParentFilter (null);
            filter = new OrFilter (
                        new AndFilter (
                            cls, 
                            new IsEqualFilter (this)),
                        new AndFilter ( // recurse up the parent chain
                            new NotFilter (cls), // but not past the first table
                            recursion));
            recursion.setParentFilter (filter);
            kids = kids.extractAllNodesThatMatch (
                // it's a row, and has this table as it's enclosing table
                new AndFilter (
                    new NodeClassFilter (TableRow.class),
                    filter), true);
            ret = new TableRow[kids.size ()];
            kids.copyToNodeArray (ret);
        }
        else
            ret = new TableRow[0];
        
        return (ret);
    }

    /**
     * Get the number of rows in this table.
     * @return The number of rows in this table.
     * <em>Note: this is a a simple count of the number of {@.html <TR>} tags and
     * may be incorrect if the {@.html <TR>} tags span multiple rows.</em>
     */
    public int getRowCount ()
    {
        return (getRows ().length);
    }

    /**
     * Get the row at the given index.
     * @param index The row number (zero based) to get. 
     * @return The row for the given index.
     */
    public TableRow getRow (int index)
    {
        TableRow[] rows;
        TableRow ret;

        rows = getRows ();
        if (index < rows.length)
            ret = rows[index];
        else
            ret = null;
        
        return (ret);
    }

    /**
     * Return a string suitable for debugging display.
     * @return The table as HTML, sorry.
     */
    public String toString()
    {
        return
            "TableTag\n" +
            "********\n"+
            toHtml();
    }

}
