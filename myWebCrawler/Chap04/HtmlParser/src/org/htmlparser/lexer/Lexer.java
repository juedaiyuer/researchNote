// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/lexer/Lexer.java $
// $Author: derrickoswald $
// $Date: 2006-09-23 00:23:10 -0400 (Sat, 23 Sep 2006) $
// $Revision: 13 $
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
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.NodeFactory;
import org.htmlparser.Remark;
import org.htmlparser.Text;
import org.htmlparser.Tag;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.ParserException;

/**
 * This class parses the HTML stream into nodes.
 * There are three major types of nodes (lexemes):
 * <ul>
 * <li>Remark</li>
 * <li>Text</li>
 * <li>Tag</li>
 * </ul>
 * Each time <code>nextNode()</code> is called, another node is returned until
 * the stream is exhausted, and <code>null</code> is returned.
 */
public class Lexer
    implements
        Serializable,
        NodeFactory
{
    // Please don't change the formatting of the version variables below.
    // This is done so as to facilitate ant script processing.

    /**
     * The floating point version number ({@value}).
     */
    public static final double
    VERSION_NUMBER = 2.0
    ;

    /**
     * The type of version ({@value}).
     */
    public static final String
    VERSION_TYPE = "Release Build"
    ;

    /**
     * The date of the version ({@value}).
     */
    public static final String
    VERSION_DATE = "Sep 17, 2006"
    ;

    // End of formatting

    /**
     * The display version ({@value}).
     */
    public static final String VERSION_STRING =
            "" + VERSION_NUMBER
            + " (" + VERSION_TYPE + " " + VERSION_DATE + ")";

    /**
     * Process remarks strictly flag.
     * If <code>true</code>, remarks are not terminated by ---$gt;
     * or --!$gt;, i.e. more than two dashes. If <code>false</code>,
     * a more lax (and closer to typical browser handling) remark parsing
     * is used.
     * Default <code>true</code>.
     */
    public static boolean STRICT_REMARKS = true;

    /**
     * The page lexemes are retrieved from.
     */
    protected Page mPage;

    /**
     * The current position on the page.
     */
    protected Cursor mCursor;

    /**
     * The factory for new nodes.
     */
    protected NodeFactory mFactory;

    /**
     * Line number to trigger on.
     * This is tested on each <code>nextNode()</code> call, as a debugging aid.
     * Alter this value and set a breakpoint on the guarded statement.
     * Remember, these line numbers are zero based, while most editors are
     * one based.
     * @see #nextNode
     */
    protected static int mDebugLineTrigger = -1;

    //
    // Static methods
    //

    /**
     * Return the version string of this parser.
     * @return A string of the form:
     * <pre>
     * "[floating point number] ([build-type] [build-date])"
     * </pre>
     */
    public static String getVersion ()
    {
        return (VERSION_STRING);
    }

    //
    // Constructors
    //

    /**
     * Creates a new instance of a Lexer.
     */
    public Lexer ()
    {
        this (new Page (""));
    }

    /**
     * Creates a new instance of a Lexer.
     * @param page The page with HTML text.
     */
    public Lexer (Page page)
    {
        setPage (page);
        setCursor (new Cursor (page, 0));
        setNodeFactory (this);
    }

    /**
     * Creates a new instance of a Lexer.
     * @param text The text to parse.
     */
    public Lexer (String text)
    {
        this (new Page (text));
    }

    /**
     * Creates a new instance of a Lexer.
     * @param connection The url to parse.
     * @exception ParserException If an error occurs opening the connection.
     */
    public Lexer (URLConnection connection)
        throws
            ParserException
    {
        this (new Page (connection));
    }

    //
    // Bean patterns
    //

    /**
     * Get the page this lexer is working on.
     * @return The page that nodes are being read from.
     */
    public Page getPage ()
    {
        return (mPage);
    }

    /**
     * Set the page this lexer is working on.
     * @param page The page that nodes will be read from.
     */
    public void setPage (Page page)
    {
        if (null == page)
            throw new IllegalArgumentException ("page cannot be null");
        // todo: sanity checks
        mPage = page;
    }

    /**
     * Get the current scanning position.
     * @return The lexer's cursor position.
     */
    public Cursor getCursor ()
    {
        return (mCursor);
    }

    /**
     * Set the current scanning position.
     * @param cursor The lexer's new cursor position.
     */
    public void setCursor (Cursor cursor)
    {
        if (null == cursor)
            throw new IllegalArgumentException ("cursor cannot be null");
        // todo: sanity checks
        mCursor = cursor;
    }

    /**
     * Get the current node factory.
     * @return The lexer's node factory.
     */
    public NodeFactory getNodeFactory ()
    {
        return (mFactory);
    }

    /**
     * Set the current node factory.
     * @param factory The node factory to be used by the lexer.
     */
    public void setNodeFactory (NodeFactory factory)
    {
        if (null == factory)
            throw new IllegalArgumentException ("node factory cannot be null");
        mFactory = factory;
    }

    /**
     * Get the current cursor position.
     * @return The current character offset into the source.
     */
    public int getPosition ()
    {
        return (getCursor ().getPosition ());
    }

    /**
     * Set the current cursor position.
     * @param position The new character offset into the source.
     */
    public void setPosition (int position)
    {
        // todo: sanity checks
        getCursor ().setPosition (position);
    }

    /**
     * Get the current line number.
     * @return The line number the lexer's working on.
     */
    public int getCurrentLineNumber ()
    {
        return (getPage ().row (getCursor ()));
    }

    /**
     * Get the current line.
     * @return The string the lexer's working on.
     */
    public String getCurrentLine ()
    {
        return (getPage ().getLine (getCursor ()));
    }

    //
    // Public methods
    //

    /**
     * Reset the lexer to start parsing from the beginning again.
     * The underlying components are reset such that the next call to
     * <code>nextNode()</code> will return the first lexeme on the page.
     */
    public void reset ()
    {
        getPage ().reset ();
        setCursor (new Cursor (getPage (), 0));
    }

    /**
     * Get the next node from the source.
     * @return A Remark, Text or Tag, or <code>null</code> if no
     * more lexemes are present.
     * @exception ParserException If there is a problem with the
     * underlying page.
     */
    public Node nextNode ()
        throws
            ParserException
    {
        return nextNode (false);
    }

    /**
     * Get the next node from the source.
     * @param quotesmart If <code>true</code>, strings ignore quoted contents.
     * @return A Remark, Text or Tag, or <code>null</code> if no
     * more lexemes are present.
     * @exception ParserException If there is a problem with the
     * underlying page.
     */
    public Node nextNode (boolean quotesmart)
        throws
            ParserException
    {
        int start;
        char ch;
        Node ret;

        // debugging suppport
        if (-1 != mDebugLineTrigger)
        {
            Page page = getPage ();
            int lineno = page.row (mCursor);
            if (mDebugLineTrigger < lineno)
                mDebugLineTrigger = lineno + 1; // trigger on next line too
        }
        start = mCursor.getPosition ();
        ch = mPage.getCharacter (mCursor);
        switch (ch)
        {
            case Page.EOF:
                ret = null;
                break;
            case '<':
                ch = mPage.getCharacter (mCursor);
                if (Page.EOF == ch)
                    ret = makeString (start, mCursor.getPosition ());
                else if ('%' == ch)
                {
                    mPage.ungetCharacter (mCursor);
                    ret = parseJsp (start);
                }
                else if ('?' == ch)
                {
                    mPage.ungetCharacter (mCursor);
                    ret = parsePI (start);
                }
                else if ('/' == ch || '%' == ch || Character.isLetter (ch))
                {
                    mPage.ungetCharacter (mCursor);
                    ret = parseTag (start);
                }
                else if ('!' == ch)
                {
                    ch = mPage.getCharacter (mCursor);
                    if (Page.EOF == ch)
                        ret = makeString (start, mCursor.getPosition ());
                    else
                    {
                        if ('>' == ch) // handle <!>
                            ret = makeRemark (start, mCursor.getPosition ());
                        else
                        {
                            mPage.ungetCharacter (mCursor); // remark/tag need this char
                            if ('-' == ch)
                                ret = parseRemark (start, quotesmart);
                            else
                            {
                                mPage.ungetCharacter (mCursor); // tag needs prior one too
                                ret = parseTag (start);
                            }
                        }
                    }
                }
                else
                {
                    mPage.ungetCharacter (mCursor); // see bug #1547354 <<tag> parsed as text
                    ret = parseString (start, quotesmart);
                }
                break;
            default:
                mPage.ungetCharacter (mCursor); // string needs to see leading foreslash
                ret = parseString (start, quotesmart);
                break;
        }

        return (ret);
    }

    /**
     * Return CDATA as a text node.
     * According to appendix <a href="http://www.w3.org/TR/html4/appendix/notes.html#notes-specifying-data">
     * B.3.2 Specifying non-HTML data</a> of the
     * <a href="http://www.w3.org/TR/html4/">HTML 4.01 Specification</a>:<br>
     * <quote>
     * <b>Element content</b><br>
     * When script or style data is the content of an element (SCRIPT and STYLE),
     * the data begins immediately after the element start tag and ends at the
     * first ETAGO ("&lt;/") delimiter followed by a name start character ([a-zA-Z]);
     * note that this may not be the element's end tag.
     * Authors should therefore escape "&lt;/" within the content. Escape mechanisms
     * are specific to each scripting or style sheet language.
     * </quote>
     * @return The <code>TextNode</code> of the CDATA or <code>null</code> if none.
     * @exception ParserException If a problem occurs reading from the source.
     */
    public Node parseCDATA ()
        throws
            ParserException
    {
        return (parseCDATA (false));
    }

    /**
     * Return CDATA as a text node.
     * Slightly less rigid than {@link #parseCDATA()} this method provides for
     * parsing CDATA that may contain quoted strings that have embedded
     * ETAGO ("&lt;/") delimiters and skips single and multiline comments.
     * @param quotesmart If <code>true</code> the strict definition of CDATA is
     * extended to allow for single or double quoted ETAGO ("&lt;/") sequences.
     * @return The <code>TextNode</code> of the CDATA or <code>null</code> if none.
     * @see #parseCDATA()
     * @exception ParserException If a problem occurs reading from the source.
     */
    public Node parseCDATA (boolean quotesmart)
        throws
            ParserException
    {
        int start;
        int state;
        boolean done;
        char quote;
        char ch;
        int end;
        boolean comment;

        start = mCursor.getPosition ();
        state = 0;
        done = false;
        quote = 0;
        comment = false;

        while (!done)
        {
            ch = mPage.getCharacter (mCursor);
            switch (state)
            {
                case 0: // prior to ETAGO
                    switch (ch)
                    {
                        case Page.EOF:
                            done = true;
                            break;
                        case '\'':
                            if (quotesmart && !comment)
                                if (0 == quote)
                                    quote = '\''; // enter quoted state
                                else if ('\'' == quote)
                                    quote = 0; // exit quoted state
                            break;
                        case '"':
                            if (quotesmart && !comment)
                                if (0 == quote)
                                    quote = '"'; // enter quoted state
                                else if ('"' == quote)
                                    quote = 0; // exit quoted state
                            break;
                        case '\\':
                            if (quotesmart)
                                if (0 != quote)
                                {
                                    ch = mPage.getCharacter (mCursor); // try to consume escaped character
                                    if (Page.EOF == ch)
                                        done = true;
                                    else if (  (ch != '\\') && (ch != quote))
                                        // unconsume char if character was not an escapable char.
                                        mPage.ungetCharacter (mCursor);
                                }
                            break;
                        case '/':
                            if (quotesmart)
                                if (0 == quote)
                                {
                                    // handle multiline and double slash comments (with a quote)
                                    ch = mPage.getCharacter (mCursor);
                                    if (Page.EOF == ch)
                                        done = true;
                                    else if ('/' == ch)
                                        comment = true;
                                    else if ('*' == ch)
                                    {
                                        do
                                        {
                                            do
                                                ch = mPage.getCharacter (mCursor);
                                            while ((Page.EOF != ch) && ('*' != ch));
                                            ch = mPage.getCharacter (mCursor);
                                            if (ch == '*')
                                                mPage.ungetCharacter (mCursor);
                                        }
                                        while ((Page.EOF != ch) && ('/' != ch));
                                    }
                                    else
                                        mPage.ungetCharacter (mCursor);
                                }
                            break;
                        case '\n':
                            comment = false;
                            break;
                        case '<':
                            if (quotesmart)
                            {
                                if (0 == quote)
                                    state = 1;
                            }
                            else
                                state = 1;
                            break;
                        default:
                            break;
                    }
                    break;
                case 1: // <
                    switch (ch)
                    {
                        case Page.EOF:
                            done = true;
                            break;
                        case '/':
                            state = 2;
                            break;
                        case '!':
                            ch = mPage.getCharacter (mCursor);
                            if (Page.EOF == ch)
                                done = true;
                            else if ('-' == ch)
                            {
                                ch = mPage.getCharacter (mCursor);
                                if (Page.EOF == ch)
                                    done = true;
                                else if ('-' == ch)
                                    state = 3;
                                else
                                    state = 0;
                            }
                            else
                                state = 0;
                            break;
                        default:
                            state = 0;
                            break;
                    }
                    break;
                case 2: // </
                    comment = false;
                    if (Page.EOF == ch)
                        done = true;
                    else if (Character.isLetter (ch))
                    {
                        done = true;
                        // back up to the start of ETAGO
                        mPage.ungetCharacter (mCursor);
                        mPage.ungetCharacter (mCursor);
                        mPage.ungetCharacter (mCursor);
                    }
                    else
                        state = 0;
                    break;
                case 3: // <!
                    comment = false;
                    if (Page.EOF == ch)
                        done = true;
                    else if ('-' == ch)
                    {
                        ch = mPage.getCharacter (mCursor);
                        if (Page.EOF == ch)
                            done = true;
                        else if ('-' == ch)
                        {
                            ch = mPage.getCharacter (mCursor);
                            if (Page.EOF == ch)
                                done = true;
                            else if ('>' == ch)
                                state = 0;
                            else
                            {
                                mPage.ungetCharacter (mCursor);
                                mPage.ungetCharacter (mCursor);
                            }
                        }
                        else
                            mPage.ungetCharacter (mCursor);
                    }
                    break;
                default:
                    throw new IllegalStateException ("how the fuck did we get in state " + state);
            }
        }
        end = mCursor.getPosition ();

        return (makeString (start, end));
    }

    //
    // NodeFactory interface
    //

    /**
     * Create a new string node.
     * @param page The page the node is on.
     * @param start The beginning position of the string.
     * @param end The ending positiong of the string.
     * @return The created Text node.
     */
    public Text createStringNode (Page page,  int start, int end)
    {
        return (new TextNode (page, start, end));
    }

    /**
     * Create a new remark node.
     * @param page The page the node is on.
     * @param start The beginning position of the remark.
     * @param end The ending positiong of the remark.
     * @return The created Remark node.
     */
    public Remark createRemarkNode (Page page,  int start, int end)
    {
        return (new RemarkNode (page, start, end));
    }

    /**
     * Create a new tag node.
     * Note that the attributes vector contains at least one element,
     * which is the tag name (standalone attribute) at position zero.
     * This can be used to decide which type of node to create, or
     * gate other processing that may be appropriate.
     * @param page The page the node is on.
     * @param start The beginning position of the tag.
     * @param end The ending positiong of the tag.
     * @param attributes The attributes contained in this tag.
     * @return The created Tag node.
     */
    public Tag createTagNode (Page page, int start, int end, Vector attributes)
    {
        return (new TagNode (page, start, end, attributes));
    }

    //
    // Internal methods
    //

    /**
     * Advance the cursor through a JIS escape sequence.
     * @param cursor A cursor positioned within the escape sequence.
     * @exception ParserException If a problem occurs reading from the source.
     */
    protected void scanJIS (Cursor cursor)
        throws
            ParserException
    {
        boolean done;
        char ch;
        int state;

        done = false;
        state = 0;
        while (!done)
        {
            ch = mPage.getCharacter (cursor);
            if (Page.EOF == ch)
                done = true;
            else
                switch (state)
                {
                    case 0:
                        if (0x1b == ch) // escape
                            state = 1;
                        break;
                    case 1:
                        if ('(' == ch)
                            state = 2;
                        else
                            state = 0;
                        break;
                    case 2:
                        if ('J' == ch)
                            done = true;
                        else
                            state = 0;
                        break;
                    default:
                        throw new IllegalStateException ("state " + state);
                }
        }
    }

    /**
     * Parse a string node.
     * Scan characters until "&lt;/", "&lt;%", "&lt;!" or &lt; followed by a
     * letter is encountered, or the input stream is exhausted, in which
     * case <code>null</code> is returned.
     * @param start The position at which to start scanning.
     * @param quotesmart If <code>true</code>, strings ignore quoted contents.
     * @return The parsed node.
     * @exception ParserException If a problem occurs reading from the source.
     */
    protected Node parseString (int start, boolean quotesmart)
        throws
            ParserException
    {
        boolean done;
        char ch;
        char quote;

        done = false;
        quote = 0;
        while (!done)
        {
            ch = mPage.getCharacter (mCursor);
            if (Page.EOF == ch)
                done = true;
            else if (0x1b == ch) // escape
            {
                ch = mPage.getCharacter (mCursor);
                if (Page.EOF == ch)
                    done = true;
                else if ('$' == ch)
                {
                    ch = mPage.getCharacter (mCursor);
                    if (Page.EOF == ch)
                        done = true;
                    else if ('B' == ch)
                        scanJIS (mCursor);
                    else
                    {
                        mPage.ungetCharacter (mCursor);
                        mPage.ungetCharacter (mCursor);
                    }
                }
                else
                    mPage.ungetCharacter (mCursor);
            }
            else if (quotesmart && (0 == quote)
                && (('\'' == ch) || ('"' == ch)))
                quote = ch; // enter quoted state
            // patch from Gernot Fricke to handle escaped closing quote
            else if (quotesmart && (0 != quote) && ('\\' == ch))
            {
                ch = mPage.getCharacter (mCursor); // try to consume escape
                if ((Page.EOF != ch)
                    && ('\\' != ch) // escaped backslash
                    && (ch != quote)) // escaped quote character
                       // ( reflects ["] or [']  whichever opened the quotation)
                    mPage.ungetCharacter (mCursor); // unconsume char if char not an escape
            }
            else if (quotesmart && (ch == quote))
                quote = 0; // exit quoted state
            else if (quotesmart && (0 == quote) && (ch == '/'))
            {
                // handle multiline and double slash comments (with a quote)
                // in script like:
                // I can't handle single quotations.
                ch = mPage.getCharacter (mCursor);
                if (Page.EOF == ch)
                    done = true;
                else if ('/' == ch)
                {
                    do
                        ch = mPage.getCharacter (mCursor);
                    while ((Page.EOF != ch) && ('\n' != ch));
                }
                else if ('*' == ch)
                {
                    do
                    {
                        do
                            ch = mPage.getCharacter (mCursor);
                        while ((Page.EOF != ch) && ('*' != ch));
                        ch = mPage.getCharacter (mCursor);
                        if (ch == '*')
                            mPage.ungetCharacter (mCursor);
                    }
                    while ((Page.EOF != ch) && ('/' != ch));
                }
                else
                    mPage.ungetCharacter (mCursor);
            }
            else if ((0 == quote) && ('<' == ch))
            {
                ch = mPage.getCharacter (mCursor);
                if (Page.EOF == ch)
                    done = true;
                // the order of these tests might be optimized for speed:
                else if ('/' == ch || Character.isLetter (ch)
                    || '!' == ch || '%' == ch || '?' == ch)
                {
                    done = true;
                    mPage.ungetCharacter (mCursor);
                    mPage.ungetCharacter (mCursor);
                }
                else
                {
                    // it's not a tag, so keep going, but check for quotes
                    mPage.ungetCharacter (mCursor);
                }
            }
        }

        return (makeString (start, mCursor.getPosition ()));
    }

    /**
     * Create a string node based on the current cursor and the one provided.
     * @param start The starting point of the node.
     * @param end The ending point of the node.
     * @exception ParserException If the nodefactory creation of the text
     * node fails.
     * @return The new Text node.
     */
    protected Node makeString (int start, int end)
        throws
            ParserException
    {
        int length;
        Node ret;

        length = end - start;
        if (0 != length)
            // got some characters
            ret = getNodeFactory ().createStringNode (
                this.getPage (), start, end);
        else
            ret = null;

        return (ret);
    }

    /**
     * Generate a whitespace 'attribute',
     * @param attributes The list so far.
     * @param bookmarks The array of positions.
     */
    private void whitespace (Vector attributes, int[] bookmarks)
    {
        if (bookmarks[1] > bookmarks[0])
            attributes.addElement (new PageAttribute (
                mPage, -1, -1, bookmarks[0], bookmarks[1], (char)0));
    }

    /**
     * Generate a standalone attribute -- font.
     * @param attributes The list so far.
     * @param bookmarks The array of positions.
     */
    private void standalone (Vector attributes, int[] bookmarks)
    {
        attributes.addElement (new PageAttribute (
            mPage, bookmarks[1], bookmarks[2], -1, -1, (char)0));
    }

    /**
     * Generate an empty attribute -- color=.
     * @param attributes The list so far.
     * @param bookmarks The array of positions.
     */
    private void empty (Vector attributes, int[] bookmarks)
    {
        attributes.addElement (new PageAttribute (
            mPage, bookmarks[1], bookmarks[2], bookmarks[2] + 1, -1, (char)0));
    }

    /**
     * Generate an unquoted attribute -- size=1.
     * @param attributes The list so far.
     * @param bookmarks The array of positions.
     */
    private void naked (Vector attributes, int[] bookmarks)
    {
        attributes.addElement (new PageAttribute (
            mPage, bookmarks[1], bookmarks[2], bookmarks[3],
            bookmarks[4], (char)0));
    }

    /**
     * Generate an single quoted attribute -- width='100%'.
     * @param attributes The list so far.
     * @param bookmarks The array of positions.
     */
    private void single_quote (Vector attributes, int[] bookmarks)
    {
        attributes.addElement (new PageAttribute (
            mPage, bookmarks[1], bookmarks[2], bookmarks[4] + 1,
            bookmarks[5], '\''));
    }

    /**
     * Generate an double quoted attribute -- CONTENT="Test Development".
     * @param attributes The list so far.
     * @param bookmarks The array of positions.
     */
    private void double_quote (Vector attributes, int[] bookmarks)
    {
        attributes.addElement (new PageAttribute (
            mPage, bookmarks[1], bookmarks[2], bookmarks[5] + 1,
            bookmarks[6], '"'));
    }

    /**
     * Parse a tag.
     * Parse the name and attributes from a start tag.<p>
     * From the <a href="http://www.w3.org/TR/html4/intro/sgmltut.html#h-3.2.2">
     * HTML 4.01 Specification, W3C Recommendation 24 December 1999</a>
     * http://www.w3.org/TR/html4/intro/sgmltut.html#h-3.2.2<p>
     * <cite>
     * 3.2.2 Attributes<p>
     * Elements may have associated properties, called attributes, which may
     * have values (by default, or set by authors or scripts). Attribute/value
     * pairs appear before the final ">" of an element's start tag. Any number
     * of (legal) attribute value pairs, separated by spaces, may appear in an
     * element's start tag. They may appear in any order.<p>
     * In this example, the id attribute is set for an H1 element:
     * <code>
     * &lt;H1 id="section1"&gt;
     * </code>
     * This is an identified heading thanks to the id attribute
     * <code>
     * &lt;/H1&gt;
     * </code>
     * By default, SGML requires that all attribute values be delimited using
     * either double quotation marks (ASCII decimal 34) or single quotation
     * marks (ASCII decimal 39). Single quote marks can be included within the
     * attribute value when the value is delimited by double quote marks, and
     * vice versa. Authors may also use numeric character references to
     * represent double quotes (&amp;#34;) and single quotes (&amp;#39;).
     * For doublequotes authors can also use the character entity reference
     * &amp;quot;.<p>
     * In certain cases, authors may specify the value of an attribute without
     * any quotation marks. The attribute value may only contain letters
     * (a-z and A-Z), digits (0-9), hyphens (ASCII decimal 45),
     * periods (ASCII decimal 46), underscores (ASCII decimal 95),
     * and colons (ASCII decimal 58). We recommend using quotation marks even
     * when it is possible to eliminate them.<p>
     * Attribute names are always case-insensitive.<p>
     * Attribute values are generally case-insensitive. The definition of each
     * attribute in the reference manual indicates whether its value is
     * case-insensitive.<p>
     * All the attributes defined by this specification are listed in the
     * attribute index.<p>
     * </cite>
     * <p>
     * This method uses a state machine with the following states:
     * <ol>
     * <li>state 0 - outside of any attribute</li>
     * <li>state 1 - within attributre name</li>
     * <li>state 2 - equals hit</li>
     * <li>state 3 - within naked attribute value.</li>
     * <li>state 4 - within single quoted attribute value</li>
     * <li>state 5 - within double quoted attribute value</li>
     * <li>state 6 - whitespaces after attribute name could lead to state 2 (=)or state 0</li>
     * </ol>
     * <p>
     * The starting point for the various components is stored in an array
     * of integers that match the initiation point for the states one-for-one,
     * i.e. bookmarks[0] is where state 0 began, bookmarks[1] is where state 1
     * began, etc.
     * Attributes are stored in a <code>Vector</code> having
     * one slot for each whitespace or attribute/value pair.
     * The first slot is for attribute name (kind of like a standalone attribute).
     * @param start The position at which to start scanning.
     * @return The parsed tag.
     * @exception ParserException If a problem occurs reading from the source.
     */
    protected Node parseTag (int start)
        throws
            ParserException
    {
        boolean done;
        char ch;
        int state;
        int[] bookmarks;
        Vector attributes;

        done = false;
        attributes = new Vector ();
        state = 0;
        bookmarks = new int[8];
        bookmarks[0] = mCursor.getPosition ();
        while (!done)
        {
            bookmarks[state + 1] = mCursor.getPosition ();
            ch = mPage.getCharacter (mCursor);
            switch (state)
            {
                case 0: // outside of any attribute
                    if ((Page.EOF == ch) || ('>' == ch) || ('<' == ch))
                    {
                        if ('<' == ch)
                        {
                            // don't consume the opening angle
                            mPage.ungetCharacter (mCursor);
                            bookmarks[state + 1] = mCursor.getPosition ();
                        }
                        whitespace (attributes, bookmarks);
                        done = true;
                    }
                    else if (!Character.isWhitespace (ch))
                    {
                        whitespace (attributes, bookmarks);
                        state = 1;
                    }
                    break;
                case 1: // within attribute name
                    if ((Page.EOF == ch) || ('>' == ch) || ('<' == ch))
                    {
                        if ('<' == ch)
                        {
                            // don't consume the opening angle
                            mPage.ungetCharacter (mCursor);
                            bookmarks[state + 1] = mCursor.getPosition ();
                        }
                        standalone (attributes, bookmarks);
                        done = true;
                    }
                    else if (Character.isWhitespace (ch))
                    {
                        // whitespaces might be followed by next attribute or an equal sign
                        // see Bug #891058 Bug in lexer.
                        bookmarks[6] = bookmarks[2]; // setting the bookmark[0] is done in state 6 if applicable
                        state = 6;
                    }
                    else if ('=' == ch)
                        state = 2;
                    break;
                case 2: // equals hit
                    if ((Page.EOF == ch) || ('>' == ch))
                    {
                        empty (attributes, bookmarks);
                        done = true;
                    }
                    else if ('\'' == ch)
                    {
                        state = 4;
                        bookmarks[4] = bookmarks[3];
                    }
                    else if ('"' == ch)
                    {
                        state = 5;
                        bookmarks[5] = bookmarks[3];
                    }
                    else if (Character.isWhitespace (ch))
                    { 
                        // collect white spaces after "=" into the assignment string;
                        // do nothing
                        // see Bug #891058 Bug in lexer.
                    }
                    else
                        state = 3;
                    break;
                case 3: // within naked attribute value
                    if ((Page.EOF == ch) || ('>' == ch))
                    {
                        naked (attributes, bookmarks);
                        done = true;
                    }
                    else if (Character.isWhitespace (ch))
                    {
                        naked (attributes, bookmarks);
                        bookmarks[0] = bookmarks[4];
                        state = 0;
                    }
                    break;
                case 4: // within single quoted attribute value
                    if (Page.EOF == ch)
                    {
                        single_quote (attributes, bookmarks);
                        done = true; // complain?
                    }
                    else if ('\'' == ch)
                    {
                        single_quote (attributes, bookmarks);
                        bookmarks[0] = bookmarks[5] + 1;
                        state = 0;
                    }
                    break;
                case 5: // within double quoted attribute value
                    if (Page.EOF == ch)
                    {
                        double_quote (attributes, bookmarks);
                        done = true; // complain?
                    }
                    else if ('"' == ch)
                    {
                        double_quote (attributes, bookmarks);
                        bookmarks[0] = bookmarks[6] + 1;
                        state = 0;
                    }
                    break;
                // patch for lexer state correction by
                // Gernot Fricke
                // See Bug # 891058 Bug in lexer.
                case 6: // undecided for state 0 or 2
                        // we have read white spaces after an attributte name
                    if (Page.EOF == ch)
                    {
                        // same as last else clause
                        standalone (attributes, bookmarks);
                  	    bookmarks[0]=bookmarks[6];
                  	    mPage.ungetCharacter (mCursor);
                  	    state=0;
                    }
                    else if (Character.isWhitespace (ch))
                    { 
                        // proceed
                    } 
                    else if ('=' == ch) // yepp. the white spaces belonged to the equal.
                    {
                        bookmarks[2] = bookmarks[6];
                        bookmarks[3] = bookmarks[7];
                        state=2;
                    }
                    else
                    {
                        // white spaces were not ended by equal
                        // meaning the attribute was a stand alone attribute
                        // now: create the stand alone attribute and rewind 
                        // the cursor to the end of the white spaces
                        // and restart scanning as whitespace attribute.
                  	    standalone (attributes, bookmarks);
                  	    bookmarks[0]=bookmarks[6];
                  	    mPage.ungetCharacter (mCursor);
                  	    state=0;
                   	}
                    break;
                default:
                    throw new IllegalStateException ("how the fuck did we get in state " + state);
            }
        }

        return (makeTag (start, mCursor.getPosition (), attributes));
    }

    /**
     * Create a tag node based on the current cursor and the one provided.
     * @param start The starting point of the node.
     * @param end The ending point of the node.
     * @param attributes The attributes parsed from the tag.
     * @exception ParserException If the nodefactory creation of the tag node fails.
     * @return The new Tag node.
     */
    protected Node makeTag (int start, int end, Vector attributes)
        throws
            ParserException
    {
        int length;
        Node ret;

        length = end - start;
        if (0 != length)
        {   // return tag based on second character, '/', '%', Letter (ch), '!'
            if (2 > length)
                // this is an error
                return (makeString (start, end));
            ret = getNodeFactory ().createTagNode (this.getPage (), start, end, attributes);
        }
        else
            ret = null;

        return (ret);
    }

    /**
     * Parse a comment.
     * Parse a remark markup.<p>
     * From the <a href="http://www.w3.org/TR/html4/intro/sgmltut.html#h-3.2.4">
     * HTML 4.01 Specification, W3C Recommendation 24 December 1999</a>
     * http://www.w3.org/TR/html4/intro/sgmltut.html#h-3.2.4<p>
     * <cite>
     * 3.2.4 Comments<p>
     * HTML comments have the following syntax:<p>
     * <code>
     * &lt;!-- this is a comment --&gt;<p>
     * &lt;!-- and so is this one,<p>
     *     which occupies more than one line --&gt;<p>
     * </code>
     * White space is not permitted between the markup declaration
     * open delimiter("&lt;!") and the comment open delimiter ("--"),
     * but is permitted between the comment close delimiter ("--") and
     * the markup declaration close delimiter ("&gt;").
     * A common error is to include a string of hyphens ("---") within a comment.
     * Authors should avoid putting two or more adjacent hyphens inside comments.
     * Information that appears between comments has no special meaning
     * (e.g., character references are not interpreted as such).
     * Note that comments are markup.<p>
     * </cite>
     * <p>
     * This method uses a state machine with the following states:
     * <ol>
     * <li>state 0 - prior to the first open delimiter (first dash)</li>
     * <li>state 1 - prior to the second open delimiter (second dash)</li>
     * <li>state 2 - prior to the first closing delimiter (first dash)</li>
     * <li>state 3 - prior to the second closing delimiter (second dash)</li>
     * <li>state 4 - prior to the terminating &gt;</li>
     * </ol>
     * <p>
     * All comment text (everything excluding the &lt; and &gt;), is included
     * in the remark text.
     * We allow terminators like --!&gt; even though this isn't part of the spec.
     * @param start The position at which to start scanning.
     * @param quotesmart If <code>true</code>, strings ignore quoted contents.
     * @return The parsed node.
     * @exception ParserException If a problem occurs reading from the source.
     */
    protected Node parseRemark (int start, boolean quotesmart)
        throws
            ParserException
    {
        boolean done;
        char ch;
        int state;

        done = false;
        state = 0;
        while (!done)
        {
            ch = mPage.getCharacter (mCursor);
            if (Page.EOF == ch)
                done = true;
            else
                switch (state)
                {
                    case 0: // prior to the first open delimiter
                        if ('>' == ch)
                            done = true;
                        if ('-' == ch)
                            state = 1;
                        else
                            return (parseString (start, quotesmart));
                        break;
                    case 1: // prior to the second open delimiter
                        if ('-' == ch)
                        {
                            // handle <!--> because netscape does
                            ch = mPage.getCharacter (mCursor);
                            if (Page.EOF == ch)
                                done = true;
                            else if ('>' == ch)
                                done = true;
                            else
                            {
                                mPage.ungetCharacter (mCursor);
                                state = 2;
                            }                        
                        }
                        else
                            return (parseString (start, quotesmart));
                        break;
                    case 2: // prior to the first closing delimiter
                        if ('-' == ch)
                            state = 3;
                        else if (Page.EOF == ch)
                            return (parseString (start, quotesmart)); // no terminator
                        break;
                    case 3: // prior to the second closing delimiter
                        if ('-' == ch)
                            state = 4;
                        else
                            state = 2;
                        break;
                    case 4: // prior to the terminating >
                        if ('>' == ch)
                            done = true;
                        else if (Character.isWhitespace (ch))
                        {
                            // stay in state 4
                        }
                        else
                            if (!STRICT_REMARKS && (('-' == ch) || ('!' == ch)))
                            {
                                // stay in state 4
                            }
                            else
                                // bug #1345049 HTMLParser should not terminate a comment with --->
                                // should maybe issue a warning mentioning STRICT_REMARKS
                                state = 2;
                        break;
                    default:
                        throw new IllegalStateException ("how the fuck did we get in state " + state);
                }
        }

        return (makeRemark (start, mCursor.getPosition ()));
    }

    /**
     * Create a remark node based on the current cursor and the one provided.
     * @param start The starting point of the node.
     * @param end The ending point of the node.
     * @exception ParserException If the nodefactory creation of the remark node fails.
     * @return The new Remark node.
     */
    protected Node makeRemark (int start, int end)
        throws
            ParserException
    {
        int length;
        Node ret;

        length = end - start;
        if (0 != length)
        {   // return tag based on second character, '/', '%', Letter (ch), '!'
            if (2 > length)
                // this is an error
                return (makeString (start, end));
            ret = getNodeFactory ().createRemarkNode (this.getPage (), start, end);
        }
        else
            ret = null;
        
        return (ret);
    }

    /**
     * Parse a java server page node.
     * Scan characters until "%&gt;" is encountered, or the input stream is
     * exhausted, in which case <code>null</code> is returned.
     * @param start The position at which to start scanning.
     * @return The parsed node.
     * @exception ParserException If a problem occurs reading from the source.
     */
    protected Node parseJsp (int start)
        throws
            ParserException
    {
        boolean done;
        char ch;
        int state;
        Vector attributes;
        int code;

        done = false;
        state = 0;
        code = 0;
        attributes = new Vector ();
        // <%xyz%>
        // 012223d
        // <%=xyz%>
        // 0122223d
        // <%@xyz%d
        // 0122223d
        while (!done)
        {
            ch = mPage.getCharacter (mCursor);
            switch (state)
            {
                case 0: // prior to the percent
                    switch (ch)
                    {
                        case '%': // <%
                            state = 1;
                            break;
                        // case Page.EOF: // <\0
                        // case '>': // <>
                        default:
                            done = true;
                            break;
                    }
                    break;
                case 1: // prior to the optional qualifier
                    switch (ch)
                    {
                        case Page.EOF:   // <%\0
                        case '>': // <%>
                            done = true;
                            break;
                        case '=': // <%=
                        case '@': // <%@
                            code = mCursor.getPosition ();
                            attributes.addElement (new PageAttribute (mPage, start + 1, code, -1, -1, (char)0));
                            state = 2;
                            break;
                        default:  // <%x
                            code = mCursor.getPosition () - 1;
                            attributes.addElement (new PageAttribute (mPage, start + 1, code, -1, -1, (char)0));
                            state = 2;
                            break;
                    }
                    break;
                case 2: // prior to the closing percent
                    switch (ch)
                    {
                        case Page.EOF: // <%x\0
                        case '>': // <%x>
                            done = true;
                            break;
                        case '\'':
                        case '"':// <%???"
                            state = ch;
                            break;
                        case '%': // <%???%
                            state = 3;
                            break;
                        case '/': // // or /*
                            ch = mPage.getCharacter (mCursor);
                            if (ch == '/') 
                            {   // find the \n or \r
                                while(true)
                                {
                                    ch = mPage.getCharacter (mCursor);
                                    if (ch == Page.EOF)
                                    {
                                        done = true;
                                        break;
                                    }
                                    else if (ch == '\n' || ch == '\r')
                                    {
                                        break;
                                    }
                                }
                            }
                            else if (ch == '*')
                            {
                                do
                                {
                                    do
                                        ch = mPage.getCharacter (mCursor);
                                    while ((Page.EOF != ch) && ('*' != ch));
                                    ch = mPage.getCharacter (mCursor);
                                    if (ch == '*')
                                        mPage.ungetCharacter (mCursor);
                                }
                                while ((Page.EOF != ch) && ('/' != ch));
                            }
                            else
                                mPage.ungetCharacter (mCursor);
                            break;
                        default:  // <%???x
                            break;
                    }
                    break;
                case 3:
                    switch (ch)
                    {
                        case Page.EOF: // <%x??%\0
                            done = true;
                            break;
                        case '>':
                            state = 4;
                            done = true;
                            break;
                        default:  // <%???%x
                            state = 2;
                            break;
                    }
                    break;
                case '"':
                    switch (ch)
                    {
                        case Page.EOF: // <%x??"\0
                            done = true;
                            break;
                        case '"':
                            state = 2;
                            break;
                        default:  // <%???'??x
                            break;
                    }
                    break;
                case '\'':
                    switch (ch)
                    {
                        case Page.EOF: // <%x??'\0
                            done = true;
                            break;
                        case '\'':
                            state = 2;
                            break;
                        default:  // <%???"??x
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException ("how the fuck did we get in state " + state);
            }
        }

        if (4 == state) // normal exit
        {
            if (0 != code)
            {
                state = mCursor.getPosition () - 2; // reuse state
                attributes.addElement (new PageAttribute (mPage, code, state, -1, -1, (char)0));
                attributes.addElement (new PageAttribute (mPage, state, state + 1, -1, -1, (char)0));
            }
            else
                throw new IllegalStateException ("jsp with no code!");
        }
        else
            return (parseString (start, true)); // hmmm, true?

        return (makeTag (start, mCursor.getPosition (), attributes));
    }

    /**
     * Parse an XML processing instruction.
     * Scan characters until "?&gt;" is encountered, or the input stream is
     * exhausted, in which case <code>null</code> is returned.
     * @param start The position at which to start scanning.
     * @return The parsed node.
     * @exception ParserException If a problem occurs reading from the source.
     */
    protected Node parsePI (int start)
        throws
            ParserException
    {
        boolean done;
        char ch;
        int state;
        Vector attributes;
        int code;

        done = false;
        state = 0;
        code = 0;
        attributes = new Vector ();
        // <?xyz?>
        // 011112d
        while (!done)
        {
            ch = mPage.getCharacter (mCursor);
            switch (state)
            {
                case 0: // prior to the question mark
                    switch (ch)
                    {
                        case '?': // <?
                            code = mCursor.getPosition ();
                            attributes.addElement (new PageAttribute (mPage, start + 1, code, -1, -1, (char)0));
                            state = 1;
                            break;
                        // case Page.EOF: // <\0
                        // case '>': // <>
                        default:
                            done = true;
                            break;
                    }
                    break;
                case 1: // prior to the closing question mark
                    switch (ch)
                    {
                        case Page.EOF: // <?x\0
                        case '>': // <?x>
                            done = true;
                            break;
                        case '\'':
                        case '"':// <?..."
                            state = ch;
                            break;
                        case '?': // <?...?
                            state = 2;
                            break;
                        default:  // <?...x
                            break;
                    }
                    break;
                case 2:
                    switch (ch)
                    {
                        case Page.EOF: // <?x..?\0
                            done = true;
                            break;
                        case '>':
                            state = 3;
                            done = true;
                            break;
                        default:  // <?...?x
                            state = 1;
                            break;
                    }
                    break;
                case '"':
                    switch (ch)
                    {
                        case Page.EOF: // <?x.."\0
                            done = true;
                            break;
                        case '"':
                            state = 1;
                            break;
                        default:  // <?...'.x
                            break;
                    }
                    break;
                case '\'':
                    switch (ch)
                    {
                        case Page.EOF: // <?x..'\0
                            done = true;
                            break;
                        case '\'':
                            state = 1;
                            break;
                        default:  // <?..."..x
                            break;
                    }
                    break;
                default:
                    throw new IllegalStateException ("how the fuck did we get in state " + state);
            }
        }

        if (3 == state) // normal exit
        {
            if (0 != code)
            {
                state = mCursor.getPosition () - 2; // reuse state
                attributes.addElement (new PageAttribute (mPage, code, state, -1, -1, (char)0));
                attributes.addElement (new PageAttribute (mPage, state, state + 1, -1, -1, (char)0));
            }
            else
                throw new IllegalStateException ("processing instruction with no content");
        }
        else
            return (parseString (start, true)); // hmmm, true?

        return (makeTag (start, mCursor.getPosition (), attributes));
    }

    //
    // Main program
    //

    /**
     * Mainline for command line operation
     * @param args [0] The URL to parse.
     * @exception MalformedURLException If the provided URL cannot be resolved.
     * @exception ParserException If the parse fails.
     */
    public static void main (String[] args)
        throws
            MalformedURLException,
            ParserException
    {
        ConnectionManager manager;
        Lexer lexer;
        Node node;

        if (0 >= args.length)
        {
            System.out.println ("HTML Lexer v" + getVersion () + "\n");
            System.out.println ();
            System.out.println ("usage: java -jar htmllexer.jar <url>");
        }
        else
        {
            try
            {
                manager = Page.getConnectionManager ();
                lexer = new Lexer (manager.openConnection (args[0]));
                while (null != (node = lexer.nextNode (false)))
                    System.out.println (node.toString ());
            }
            catch (ParserException pe)
            {
                System.out.println (pe.getMessage ());
                if (null != pe.getThrowable ())
                    System.out.println (pe.getThrowable ().getMessage ());
            }
        }
    }
}
