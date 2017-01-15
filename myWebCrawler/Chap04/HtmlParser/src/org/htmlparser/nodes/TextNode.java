// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/nodes/TextNode.java $
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

import org.htmlparser.Text;
import org.htmlparser.lexer.Cursor;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Normal text in the HTML document is represented by this class.
 */
public class TextNode
    extends
        AbstractNode
    implements
        Text
{
    /**
     * The contents of the string node, or override text.
     */
    protected String mText;

    /**
     * Constructor takes in the text string.
     * @param text The string node text. For correct generation of HTML, this
     * should not contain representations of tags (unless they are balanced).
     */
    public TextNode (String text)
    {
        super (null, 0, 0);
        setText (text);
    }

    /**
     * Constructor takes in the page and beginning and ending posns.
     * @param page The page this string is on.
     * @param start The beginning position of the string.
     * @param end The ending positiong of the string.
     */
    public TextNode (Page page, int start, int end)
    {
        super (page, start, end);
        mText = null;
    }

    /**
     * Returns the text of the node.
     * This is the same as {@link #toHtml} for this type of node.
     * @return The contents of this text node.
     */
    public String getText ()
    {
        return (toHtml ());
    }

    /**
     * Sets the string contents of the node.
     * @param text The new text for the node.
     */
    public void setText (String text)
    {
        mText = text;
        nodeBegin = 0;
        nodeEnd = mText.length ();
    }

    /**
     * Returns the text of the node.
     * This is the same as {@link #toHtml} for this type of node.
     * @return The contents of this text node.
     */
    public String toPlainTextString ()
    {
        return (toHtml ());
    }

    /**
     * Returns the text of the node.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @return The contents of this text node.
     */
    public String toHtml (boolean verbatim)
    {
        String ret;
        
        ret = mText;
        if (null == ret)
            ret = mPage.getText (getStartPosition (),  getEndPosition ());

        return (ret);
    }

    /**
     * Express this string node as a printable string
     * This is suitable for display in a debugger or output to a printout.
     * Control characters are replaced by their equivalent escape
     * sequence and contents is truncated to 80 characters.
     * @return A string representation of the string node.
     */
    public String toString ()
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
            ret.append ("Txt (");
            ret.append (start);
            ret.append (",");
            ret.append (end);
            ret.append ("): ");
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
            ret.append ("Txt (");
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
     * Returns if the node consists of only white space.
     * White space can be spaces, new lines, etc.
     */
    public boolean isWhiteSpace()
    {
        if (mText == null || mText.trim().equals(""))
            return true;
        return false;
    }
    
    /**
     * String visiting code.
     * @param visitor The <code>NodeVisitor</code> object to invoke 
     * <code>visitStringNode()</code> on.
     */
    public void accept (NodeVisitor visitor)
    {
        visitor.visitStringNode (this);
    }
}
