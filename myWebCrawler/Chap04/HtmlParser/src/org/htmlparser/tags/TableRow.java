// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/tags/TableRow.java $
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
 * A table row tag.
 */
public class TableRow extends CompositeTag
{
    /**
     * The set of names handled by this tag.
     */
    private static final String[] mIds = new String[] {"TR"};
    
    /**
     * The set of tag names that indicate the end of this tag.
     */
    private static final String[] mEnders = new String[] {"TBODY", "TFOOT", "THEAD"};
    
    /**
     * The set of end tag names that indicate the end of this tag.
     */
    private static final String[] mEndTagEnders = new String[] {"TBODY", "TFOOT", "THEAD", "TABLE"};

    /**
     * Create a new table row tag.
     */
    public TableRow ()
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
     * Get the column tags within this <code>TR</code> (table row) tag.
     * @return The {@.html <TD>} tags contained by this tag.
     */
    public TableColumn[] getColumns ()
    {
        NodeList kids;
        NodeClassFilter cls;
        HasParentFilter recursion;
        NodeFilter filter;
        TableColumn[] ret;

        kids = getChildren ();
        if (null != kids)
        {
            cls = new NodeClassFilter (TableRow.class);
            recursion = new HasParentFilter (null);
            filter = new OrFilter (
                        new AndFilter (
                            cls, 
                            new IsEqualFilter (this)),
                        new AndFilter ( // recurse up the parent chain
                            new NotFilter (cls), // but not past the first row
                            recursion));
            recursion.setParentFilter (filter);
            kids = kids.extractAllNodesThatMatch (
                // it's a column, and has this row as it's enclosing row
                new AndFilter (
                    new NodeClassFilter (TableColumn.class),
                    filter), true);
            ret = new TableColumn[kids.size ()];
            kids.copyToNodeArray (ret);
        }
        else
            ret = new TableColumn[0];
        
        return (ret);
    }

    /**
     * Get the number of columns in this row.
     * @return The number of columns in this row.
     * <em>Note: this is a a simple count of the number of {@.html <TD>} tags and
     * may be incorrect if the {@.html <TD>} tags span multiple columns.</em>
     */
    public int getColumnCount ()
    {
        return (getColumns ().length);
    }

    /**
     * Get the header of this table
     * @return Table header tags contained in this row.
     */
    public TableHeader[] getHeaders ()
    {
        NodeList kids;
        NodeClassFilter cls;
        HasParentFilter recursion;
        NodeFilter filter;
        TableHeader[] ret;

        kids = getChildren ();
        if (null != kids)
        {
            cls = new NodeClassFilter (TableRow.class);
            recursion = new HasParentFilter (null);
            filter = new OrFilter (
                        new AndFilter (
                            cls, 
                            new IsEqualFilter (this)),
                        new AndFilter ( // recurse up the parent chain
                            new NotFilter (cls), // but not past the first row
                            recursion));
            recursion.setParentFilter (filter);
            kids = kids.extractAllNodesThatMatch (
                // it's a header, and has this row as it's enclosing row
                new AndFilter (
                    new NodeClassFilter (TableHeader.class),
                    filter), true);
            ret = new TableHeader[kids.size ()];
            kids.copyToNodeArray (ret);
        }
        else
            ret = new TableHeader[0];
        
        return (ret);
    }

    /**
     * Get the number of headers in this row.
     * @return The count of header tags in this row.
     */
    public int getHeaderCount ()
    {
        return (getHeaders ().length);
    }

    /**
     * Checks if this table has a header
     * @return <code>true</code> if there is a header tag.
     */
    public boolean hasHeader ()
    {
        return (0 != getHeaderCount ());
    }
}
