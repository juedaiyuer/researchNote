// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/nodes/TagNode.java $
// $Author: derrickoswald $
// $Date: 2006-09-22 21:18:08 -0400 (Fri, 22 Sep 2006) $
// $Revision: 12 $
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Cursor;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.scanners.Scanner;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * TagNode represents a generic tag.
 * If no scanner is registered for a given tag name, this is what you get.
 * This is also the base class for all tags created by the parser.
 */
public class TagNode
    extends
        AbstractNode
    implements
        Tag
{
    /**
     * An empty set of tag names.
     */
    private final static String[] NONE = new String[0];
    
    /**
     * The scanner for this tag.
     */
    private Scanner mScanner;
    
    /**
     * The default scanner for non-composite tags.
     */
    protected final static Scanner mDefaultScanner = new TagScanner ();

    /**
     * The tag attributes.
     * Objects of type {@link Attribute}.
     * The first element is the tag name, subsequent elements being either
     * whitespace or real attributes.
     */
    protected Vector mAttributes;

    /**
     * Set of tags that breaks the flow.
     */
    protected static Hashtable breakTags;
    static
    {
        breakTags = new Hashtable (30);
        breakTags.put ("BLOCKQUOTE", Boolean.TRUE);
        breakTags.put ("BODY", Boolean.TRUE);
        breakTags.put ("BR", Boolean.TRUE);
        breakTags.put ("CENTER", Boolean.TRUE);
        breakTags.put ("DD", Boolean.TRUE);
        breakTags.put ("DIR", Boolean.TRUE);
        breakTags.put ("DIV", Boolean.TRUE);
        breakTags.put ("DL", Boolean.TRUE);
        breakTags.put ("DT", Boolean.TRUE);
        breakTags.put ("FORM", Boolean.TRUE);
        breakTags.put ("H1", Boolean.TRUE);
        breakTags.put ("H2", Boolean.TRUE);
        breakTags.put ("H3", Boolean.TRUE);
        breakTags.put ("H4", Boolean.TRUE);
        breakTags.put ("H5", Boolean.TRUE);
        breakTags.put ("H6", Boolean.TRUE);
        breakTags.put ("HEAD", Boolean.TRUE);
        breakTags.put ("HR", Boolean.TRUE);
        breakTags.put ("HTML", Boolean.TRUE);
        breakTags.put ("ISINDEX", Boolean.TRUE);
        breakTags.put ("LI", Boolean.TRUE);
        breakTags.put ("MENU", Boolean.TRUE);
        breakTags.put ("NOFRAMES", Boolean.TRUE);
        breakTags.put ("OL", Boolean.TRUE);
        breakTags.put ("P", Boolean.TRUE);
        breakTags.put ("PRE", Boolean.TRUE);
        breakTags.put ("TD", Boolean.TRUE);
        breakTags.put ("TH", Boolean.TRUE);
        breakTags.put ("TITLE", Boolean.TRUE);
        breakTags.put ("UL", Boolean.TRUE);
    }

    /**
     * Create an empty tag.
     */
    public TagNode ()
    {
        this (null, -1, -1, new Vector ());
    }

    /**
     * Create a tag with the location and attributes provided
     * @param page The page this tag was read from.
     * @param start The starting offset of this node within the page.
     * @param end The ending offset of this node within the page.
     * @param attributes The list of attributes that were parsed in this tag.
     * @see Attribute
     */
    public TagNode (Page page, int start, int end, Vector attributes)
    {
        super (page, start, end);

        mScanner = mDefaultScanner;
        mAttributes = attributes;
        if ((null == mAttributes) || (0 == mAttributes.size ()))
        {
            String[] names;

            names = getIds ();
            if ((null != names) && (0 != names.length))
                setTagName (names[0]);
            else
                setTagName (""); // make sure it's not null
        }
    }

    /**
     * Create a tag like the one provided.
     * @param tag The tag to emulate.
     * @param scanner The scanner for this tag.
     */
    public TagNode (TagNode tag, TagScanner scanner)
    {
        this (tag.getPage (), tag.getTagBegin (), tag.getTagEnd (), tag.getAttributesEx ());
        setThisScanner (scanner);
    }

    /**
     * Returns the value of an attribute.
     * @param name Name of attribute, case insensitive.
     * @return The value associated with the attribute or null if it does
     * not exist, or is a stand-alone or
     */
    public String getAttribute (String name)
    {
        Attribute attribute;
        String ret;

        ret = null;

        attribute = getAttributeEx (name);
        if (null != attribute)
            ret = attribute.getValue ();

        return (ret);
    }

    /**
     * Set attribute with given key, value pair.
     * Figures out a quote character to use if necessary.
     * @param key The name of the attribute.
     * @param value The value of the attribute.
     */
    public void setAttribute (String key, String value)
    {
        char ch;
        boolean needed;
        boolean singleq;
        boolean doubleq;
        String ref;
        StringBuffer buffer;
        char quote;
        Attribute attribute;

        // first determine if there's whitespace in the value
        // and while we'return at it find a suitable quote character
        needed = false;
        singleq = true;
        doubleq = true;
        if (null != value)
            for (int i = 0; i < value.length (); i++)
            {
                ch = value.charAt (i);
                if (Character.isWhitespace (ch))
                    needed = true;
                else if ('\'' == ch)
                    singleq  = false;
                else if ('"' == ch)
                    doubleq = false;
            }

        // now apply quoting
        if (needed)
        {
            if (doubleq)
                quote = '"';
            else if (singleq)
                quote = '\'';
            else
            {
                // uh-oh, we need to convert some quotes into character references
                // convert all double quotes into &#34;
                quote = '"';
                ref = "&quot;"; // Translate.encode (quote);
                // JDK 1.4: value = value.replaceAll ("\"", ref);
                buffer = new StringBuffer (value.length() * 5);
                for (int i = 0; i < value.length (); i++)
                {
                    ch = value.charAt (i);
                    if (quote == ch)
                        buffer.append (ref);
                    else
                        buffer.append (ch);
                }
                value = buffer.toString ();
            }
        }
        else
            quote = 0;
        attribute = getAttributeEx (key);
        if (null != attribute)
        {   // see if we can splice it in rather than replace it
            attribute.setValue (value);
            if (0 != quote)
                attribute.setQuote (quote);
        }
        else
            setAttribute (key, value, quote);
    }

    /**
     * Remove the attribute with the given key, if it exists.
     * @param key The name of the attribute.
     */
    public void removeAttribute (String key)
    {
        Attribute attribute;

        attribute = getAttributeEx (key);
        if (null != attribute)
            getAttributesEx ().remove (attribute);
    }

    /**
     * Set attribute with given key, value pair where the value is quoted by quote.
     * @param key The name of the attribute.
     * @param value The value of the attribute.
     * @param quote The quote character to be used around value.
     * If zero, it is an unquoted value.
     */
    public void setAttribute (String key, String value, char quote)
    {
        setAttribute (new Attribute (key, value, quote));
    }

    /**
     * Returns the attribute with the given name.
     * @param name Name of attribute, case insensitive.
     * @return The attribute or null if it does
     * not exist.
     */
    public Attribute getAttributeEx (String name)
    {
        Vector attributes;
        int size;
        Attribute attribute;
        String string;
        Attribute ret;

        ret = null;

        attributes = getAttributesEx ();
        if (null != attributes)
        {
            size = attributes.size ();
            for (int i = 0; i < size; i++)
            {
                attribute = (Attribute)attributes.elementAt (i);
                string = attribute.getName ();
                if ((null != string) && name.equalsIgnoreCase (string))
                {
                    ret = attribute;
                    i = size; // exit fast
                }
            }
        }

        return (ret);
    }

    /**
     * Set an attribute.
     * @param attribute The attribute to set.
     * @see #setAttribute(Attribute)
     */
    public void setAttributeEx (Attribute attribute)
    {
        setAttribute (attribute);
    }

    /**
     * Set an attribute.
     * This replaces an attribute of the same name.
     * To set the zeroth attribute (the tag name), use setTagName().
     * @param attribute The attribute to set.
     */
    public void setAttribute (Attribute attribute)
    {
        boolean replaced;
        Vector attributes;
        int length;
        String name;
        Attribute test;
        String test_name;

        replaced = false;
        attributes = getAttributesEx ();
        length =  attributes.size ();
        if (0 < length)
        {
            name = attribute.getName ();
            for (int i = 1; i < attributes.size (); i++)
            {
                test = (Attribute)attributes.elementAt (i);
                test_name = test.getName ();
                if (null != test_name)
                    if (test_name.equalsIgnoreCase (name))
                    {
                        attributes.setElementAt (attribute, i);
                        replaced = true;
                    }
            }
        }
        if (!replaced)
        {
            // add whitespace between attributes
            if ((0 != length) && !((Attribute)attributes.elementAt (length - 1)).isWhitespace ())
                attributes.addElement (new Attribute (" "));
            attributes.addElement (attribute);
        }
    }

    /**
     * Gets the attributes in the tag.
     * @return Returns the list of {@link Attribute Attributes} in the tag.
     * The first element is the tag name, subsequent elements being either
     * whitespace or real attributes.
     */
    public Vector getAttributesEx ()
    {
        return (mAttributes);
    }

    /**
     * Return the name of this tag.
     * <p>
     * <em>
     * Note: This value is converted to uppercase and does not
     * begin with "/" if it is an end tag. Nor does it end with
     * a slash in the case of an XML type tag.
     * To get at the original text of the tag name use
     * {@link #getRawTagName getRawTagName()}.
     * The conversion to uppercase is performed with an ENGLISH locale.
     * </em>
     * @return The tag name.
     */
    public String getTagName ()
    {
        String ret;

        ret = getRawTagName ();
        if (null != ret)
        {
            ret = ret.toUpperCase (Locale.ENGLISH);
            if (ret.startsWith ("/"))
                ret = ret.substring (1);
            if (ret.endsWith ("/"))
                ret = ret.substring (0, ret.length () - 1);
        }

        return (ret);
    }

    /**
     * Return the name of this tag.
     * @return The tag name or null if this tag contains nothing or only
     * whitespace.
     */
    public String getRawTagName ()
    {
        Vector attributes;
        String ret;

        ret = null;
        
        attributes = getAttributesEx ();
        if (0 != attributes.size ())
            ret = ((Attribute)attributes.elementAt (0)).getName ();

        return (ret);
    }

    /**
     * Set the name of this tag.
     * This creates or replaces the first attribute of the tag (the
     * zeroth element of the attribute vector).
     * @param name The tag name.
     */
    public void setTagName (String name)
    {
        Attribute attribute;
        Vector attributes;
        Attribute zeroth;

        attribute = new Attribute (name, null, (char)0);
        attributes = getAttributesEx ();
        if (null == attributes)
        {
            attributes = new Vector ();
            setAttributesEx (attributes);
        }
        if (0 == attributes.size ())
            // nothing added yet
            attributes.addElement (attribute);
        else
        {
            zeroth = (Attribute)attributes.elementAt (0);
            // check for attribute that looks like a name
            if ((null == zeroth.getValue ()) && (0 == zeroth.getQuote ()))
                attributes.setElementAt (attribute, 0);
            else
                attributes.insertElementAt (attribute, 0);
        }
    }

    /**
     * Return the text contained in this tag.
     * @return The complete contents of the tag (within the angle brackets).
     */
    public String getText ()
    {
        String ret;
        
        ret = toHtml ();
        ret = ret.substring (1, ret.length () - 1);
        
        return (ret);
    }

    /**
     * Sets the attributes.
     * NOTE: Values of the extended hashtable are two element arrays of String,
     * with the first element being the original name (not uppercased),
     * and the second element being the value.
     * @param attribs The attribute collection to set.
     */
    public void setAttributesEx (Vector attribs)
    {
        mAttributes = attribs;
    }

    /**
     * Sets the nodeBegin.
     * @param tagBegin The nodeBegin to set
     */
    public void setTagBegin (int tagBegin)
    {
        nodeBegin = tagBegin;
    }

    /**
     * Gets the nodeBegin.
     * @return The nodeBegin value.
     */
    public int getTagBegin ()
    {
        return (nodeBegin);
    }

    /**
     * Sets the nodeEnd.
     * @param tagEnd The nodeEnd to set
     */
    public void setTagEnd (int tagEnd)
    {
        nodeEnd = tagEnd;
    }

    /**
     * Gets the nodeEnd.
     * @return The nodeEnd value.
     */
    public int getTagEnd ()
    {
        return (nodeEnd);
    }

    /**
     * Parses the given text to create the tag contents.
     * @param text A string of the form &lt;TAGNAME xx="yy"&gt;.
     */
    public void setText (String text)
    {
        Lexer lexer;
        TagNode output;
        
        lexer = new Lexer (text);
        try
        {
            output = (TagNode)lexer.nextNode ();
            mPage = output.getPage ();
            nodeBegin = output.getStartPosition ();
            nodeEnd = output.getEndPosition ();
            mAttributes = output.getAttributesEx ();
        }
        catch (ParserException pe)
        {
            throw new IllegalArgumentException (pe.getMessage ());
        }
    }

    /**
     * Get the plain text from this node.
     * @return An empty string (tag contents do not display in a browser).
     * If you want this tags HTML equivalent, use {@link #toHtml toHtml()}.
     */
    public String toPlainTextString ()
    {
        return ("");
    }

    /**
     * Render the tag as HTML.
     * A call to a tag's <code>toHtml()</code> method will render it in HTML.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @return The tag as an HTML fragment.
     * @see org.htmlparser.Node#toHtml()
     */
    public String toHtml (boolean verbatim)
    {
        return (toTagHtml ());
    }

    /**
     * Return the tag HTML.
     * Like <code>toHtml()</code> but since this is not implemented
     * by {@link org.htmlparser.tags.CompositeTag}, only renders the start tag.
     * @return The sequence of characters for the start tag only.
     * @see #toHtml()
     */
    public String toTagHtml ()
    {
        int length;
        int size;
        Vector attributes;
        Attribute attribute;
        StringBuffer ret;

        length = 2;
        attributes = getAttributesEx ();
        size = attributes.size ();
        for (int i = 0; i < size; i++)
        {
            attribute = (Attribute)attributes.elementAt (i);
            length += attribute.getLength ();
        }
        ret = new StringBuffer (length);
        ret.append ("<");
        for (int i = 0; i < size; i++)
        {
            attribute = (Attribute)attributes.elementAt (i);
            attribute.toString (ret);
        }
        ret.append (">");

        return (ret.toString ());
    }

    /**
     * Print the contents of the tag.
     * @return An string describing the tag. For text that looks like HTML use #toHtml().
     */
    public String toString ()
    {
        String text;
        String type;
        Cursor start;
        Cursor end;
        StringBuffer ret;

        text = getText ();
        ret = new StringBuffer (20 + text.length ());
        if (isEndTag ())
            type = "End";
        else
            type = "Tag";
        start = new Cursor (getPage (), getStartPosition ());
        end = new Cursor (getPage (), getEndPosition ());
        ret.append (type);
        ret.append (" (");
        ret.append (start);
        ret.append (",");
        ret.append (end);
        ret.append ("): ");
        if (80 < ret.length () + text.length ())
        {
            text = text.substring (0, 77 - ret.length ());
            ret.append (text);
            ret.append ("...");
        }
        else
            ret.append (text);
        
        return (ret.toString ());
    }

    /**
     * Determines if the given tag breaks the flow of text.
     * @return <code>true</code> if following text would start on a new line,
     * <code>false</code> otherwise.
     */
    public boolean breaksFlow ()
    {
        return (breakTags.containsKey (getTagName ()));
    }

    /**
     * Default tag visiting code.
     * Based on <code>isEndTag()</code>, calls either <code>visitTag()</code> or
     * <code>visitEndTag()</code>.
     * @param visitor The visitor that is visiting this node.
     */
    public void accept (NodeVisitor visitor)
    {
        if (isEndTag ())
            visitor.visitEndTag (this);
        else
            visitor.visitTag (this);
    }

    /**
     * Is this an empty xml tag of the form &lt;tag/&gt;.
     * @return true if the last character of the last attribute is a '/'.
     */
    public boolean isEmptyXmlTag ()
    {
        Vector attributes;
        int size;
        Attribute attribute;
        String name;
        int length;
        boolean ret;

        ret = false;

        attributes = getAttributesEx ();
        size = attributes.size ();
        if (0 < size)
        {
            attribute = (Attribute)attributes.elementAt (size - 1);
            name = attribute.getName ();
            if (null != name)
            {
                length = name.length ();
                ret = name.charAt (length - 1) == '/';
            }
        }

        return (ret);
    }

    /**
     * Set this tag to be an empty xml node, or not.
     * Adds or removes an ending slash on the tag.
     * @param emptyXmlTag If true, ensures there is an ending slash in the node,
     * i.e. &lt;tag/&gt;, otherwise removes it.
     */
    public void setEmptyXmlTag (boolean emptyXmlTag)
    {
        Vector attributes;
        int size;
        Attribute attribute;
        String name;
        String value;
        int length;
        
        attributes = getAttributesEx ();
        size = attributes.size ();
        if (0 < size)
        {
            attribute = (Attribute)attributes.elementAt (size - 1);
            name = attribute.getName ();
            if (null != name)
            {
                length = name.length ();
                value = attribute.getValue ();
                if (null == value)
                    if (name.charAt (length - 1) == '/')
                    {
                        // already exists, remove if requested
                        if (!emptyXmlTag)
                            if (1 == length)
                                attributes.removeElementAt (size - 1);
                            else
                            {
                                // this shouldn't happen, but covers the case
                                // where no whitespace separates the slash
                                // from the previous attribute
                                name = name.substring (0, length - 1);
                                attribute = new Attribute (name, null);
                                attributes.removeElementAt (size - 1);
                                attributes.addElement (attribute);
                            }
                    }
                    else
                    {
                        // ends with attribute, add whitespace + slash if requested
                        if (emptyXmlTag)
                        {
                            attribute = new Attribute (" ");
                            attributes.addElement (attribute);
                            attribute = new Attribute ("/", null);
                            attributes.addElement (attribute);
                        }
                    }
                else
                {
                    // some valued attribute, add whitespace + slash if requested
                    if (emptyXmlTag)
                    {
                        attribute = new Attribute (" ");
                        attributes.addElement (attribute);
                        attribute = new Attribute ("/", null);
                        attributes.addElement (attribute);
                    }
                }
            }
            else
            {
                // ends with whitespace, add if requested
                if (emptyXmlTag)
                {
                    attribute = new Attribute ("/", null);
                    attributes.addElement (attribute);
                }
            }
        }
        else
            // nothing there, add if requested
            if (emptyXmlTag)
            {
                attribute = new Attribute ("/", null);
                attributes.addElement (attribute);
            }
    }

    /**
     * Predicate to determine if this tag is an end tag (i.e. &lt;/HTML&gt;).
     * @return <code>true</code> if this tag is an end tag.
     */
    public boolean isEndTag ()
    {
        String raw;
        
        raw = getRawTagName ();

        return ((null == raw) ? false : ((0 != raw.length ()) && ('/' == raw.charAt (0))));
    }

    /**
     * Get the line number where this tag starts.
     * @return The (zero based) line number in the page where this tag starts.
     */
    public int getStartingLineNumber ()
    {
        return (getPage ().row (getStartPosition ()));
    }

    /**
     * Get the line number where this tag ends.
     * @return The (zero based) line number in the page where this tag ends.
     */
    public int getEndingLineNumber ()
    {
        return (getPage ().row (getEndPosition ()));
    }

    /**
     * Return the set of names handled by this tag.
     * Since this a a generic tag, it has no ids.
     * @return The names to be matched that create tags of this type.
     */
    public String[] getIds ()
    {
        return (NONE);
    }

    /**
     * Return the set of tag names that cause this tag to finish.
     * These are the normal (non end tags) that if encountered while
     * scanning (a composite tag) will cause the generation of a virtual
     * tag.
     * Since this a a non-composite tag, the default is no enders.
     * @return The names of following tags that stop further scanning.
     */
    public String[] getEnders ()
    {
        return (NONE);
    }

    /**
     * Return the set of end tag names that cause this tag to finish.
     * These are the end tags that if encountered while
     * scanning (a composite tag) will cause the generation of a virtual
     * tag.
     * Since this a a non-composite tag, it has no end tag enders.
     * @return The names of following end tags that stop further scanning.
     */
    public String[] getEndTagEnders ()
    {
        return (NONE);
    }

    /**
     * Return the scanner associated with this tag.
     * @return The scanner associated with this tag.
     */
    public Scanner getThisScanner ()
    {
        return (mScanner);
    }

    /**
     * Set the scanner associated with this tag.
     * @param scanner The scanner for this tag.
     */
    public void setThisScanner (Scanner scanner)
    {
        mScanner = scanner;
    }

    /**
     * Get the end tag for this (composite) tag.
     * For a non-composite tag this always returns <code>null</code>.
     * @return The tag that terminates this composite tag, i.e. &lt;/HTML&gt;.
     */
    public Tag getEndTag ()
    {
        return (null);
    }

    /**
     * Set the end tag for this (composite) tag.
     * For a non-composite tag this is a no-op.
     * @param end The tag that terminates this composite tag, i.e. &lt;/HTML&gt;.
     */
    public void setEndTag (Tag end)
    {
    }
}
