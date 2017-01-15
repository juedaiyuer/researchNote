// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/nodes/RemarkNode.java $
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

package org.htmlparser.nodes;

import org.htmlparser.Remark;
import org.htmlparser.lexer.Cursor;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * The remark tag is identified and represented by this class.
 */
public class RemarkNode
    extends
        AbstractNode
    implements
        Remark
{
    /**
     * The contents of the remark node, or override text.
     */
    protected String mText;

    /**
     * Constructor takes in the text string.
     * @param text The string node text. For correct generation of HTML, this
     * should not contain representations of tags (unless they are balanced).
     */
    public RemarkNode (String text)
    {
        super (null, 0, 0);
        setText (text);
    }

    /**
     * Constructor takes in the page and beginning and ending posns.
     * @param page The page this remark is on.
     * @param start The beginning position of the remark.
     * @param end The ending positiong of the remark.
     */
    public RemarkNode (Page page, int start, int end)
    {
        super (page, start, end);
        mText = null;
    }

    /**
     * Returns the text contents of the comment tag.
     * @return The contents of the text inside the comment delimiters.
     */
    public String getText ()
    {
        int start;
        int end;
        String ret;

        if (null == mText)
        {
            start = getStartPosition () + 4; // <!--
            end = getEndPosition () - 3; // -->
            if (start >= end)
                ret = "";
            else
                ret = mPage.getText (start, end);
        }
        else
            ret = mText;

        return (ret);
    }

    /**
     * Sets the string contents of the node.
     * If the text has the remark delimiters (&lt;!-- --&gt;), these are stripped off.
     * @param text The new text for the node.
     */
    public void setText (String text)
    {
        mText = text;
        if (text.startsWith ("<!--") && text.endsWith ("-->"))
            mText = text.substring (4, text.length () - 3);
        nodeBegin = 0;
        nodeEnd = mText.length ();
    }

    /**
     * Return the remark text.
     * @return The HTML comment.
     */
    public String toPlainTextString ()
    {
        return ("");
    }

    /**
     * Return The full HTML remark.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @return The comment, i.e. {@.html <!-- this is a comment -->}.
     */
    public String toHtml (boolean verbatim)
    {
        StringBuffer buffer;
        String ret;
        
        if (null == mText)
            ret = mPage.getText (getStartPosition (), getEndPosition ());
        else
        {
            buffer = new StringBuffer (mText.length () + 7);
            buffer.append ("<!--");
            buffer.append (mText);
            buffer.append ("-->");
            ret = buffer.toString ();
        }

        return (ret);
    }

    /**
     * Print the contents of the remark tag.
     * This is suitable for display in a debugger or output to a printout.
     * Control characters are replaced by their equivalent escape
     * sequence and contents is truncated to 80 characters.
     * @return A string representation of the remark node.
     */
    public String toString()
    {
        int startpos;
        int endpos;
        Cursor start;
        Cursor end;
        char c;
        StringBuffer ret;

        startpos = getStartPosition ();
        endpos = getEndPosition ();
        ret = new StringBuffer (endpos - startpos + 20);
        if (null == mText)
        {
            start = new Cursor (getPage (), startpos);
            end = new Cursor (getPage (), endpos);
            ret.append ("Rem (");
            ret.append (start);
            ret.append (",");
            ret.append (end);
            ret.append ("): ");
            start.setPosition (startpos + 4); // <!--
            endpos -= 3; // -->
            while (start.getPosition () < endpos)
            {
                try
                {
                    c = mPage.getCharacter (start);
                    switch (c)
                    {
                        case '\t':
                            ret.append ("\\t");
                            break;
                        case '\n':
                            ret.append ("\\n");
                            break;
                        case '\r':
                            ret.append ("\\r");
                            break;
                        default:
                            ret.append (c);
                    }
                }
                catch (ParserException pe)
                {
                    // not really expected, but we're only doing toString, so ignore
                }
                if (77 <= ret.length ())
                {
                    ret.append ("...");
                    break;
                }
            }
        }
        else
        {
            ret.append ("Rem (");
            ret.append (startpos);
            ret.append (",");
            ret.append (endpos);
            ret.append ("): ");
            for (int i = 0; i < mText.length (); i++)
            {
                c = mText.charAt (i);
                switch (c)
                {
                    case '\t':
                        ret.append ("\\t");
                        break;
                    case '\n':
                        ret.append ("\\n");
                        break;
                    case '\r':
                        ret.append ("\\r");
                        break;
                    default:
                        ret.append (c);
                }
                if (77 <= ret.length ())
                {
                    ret.append ("...");
                    break;
                }
            }
        }

        return (ret.toString ());
    }

    /**
     * Remark visiting code.
     * @param visitor The <code>NodeVisitor</code> object to invoke 
     * <code>visitRemarkNode()</code> on.
     */
    public void accept (NodeVisitor visitor)
    {
        visitor.visitRemarkNode (this);
    }
}
