// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/Remark.java $
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

package org.htmlparser;

/**
 * This interface represents a comment in the HTML document.
 */
public interface Remark
    extends
        Node
{
    /**
     * Returns the text contents of the comment tag.
     * @return The contents of the text inside the comment delimiters.
     * @see #setText
     */
    String getText();

    /**
     * Sets the string contents of the node.
     * If the text has the remark delimiters (&lt;!-- --&gt;),
     * these are stripped off.
     * @param text The new text for the node.
     * @see #getText
     */
    void setText (String text);

    //
    // Node interface
    //

//    public void accept (NodeVisitor visitor)
//    {
//    }
//
//    public void collectInto (NodeList collectionList, NodeFilter filter)
//    {
//    }
//
//    public NodeList getChildren ()
//    {
//    }
//
//    public int getEndPosition ()
//    {
//    }
//
//    public Node getParent ()
//    {
//    }
//
//    public int getStartPosition ()
//    {
//    }
//
//    public void setChildren (NodeList children)
//    {
//    }
//
//    public void setEndPosition (int position)
//    {
//    }
//
//    public void setParent (Node node)
//    {
//    }
//
//    public void setStartPosition (int position)
//    {
//    }
//
//    public String toHtml ()
//    {
//    }
//
//    public String toPlainTextString ()
//    {
//    }
}
