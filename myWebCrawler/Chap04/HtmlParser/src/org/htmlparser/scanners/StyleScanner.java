// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/scanners/StyleScanner.java $
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

package org.htmlparser.scanners;

import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFactory;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Remark;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * The StyleScanner handles style elements.
 * It gathers all interior nodes into one undifferentiated string node.
 */
public class StyleScanner extends CompositeTagScanner
{
    /**
     * Create a style scanner.
     */
    public StyleScanner ()
    {
    }

    /**
     * Scan for style definitions.
     * Accumulates text from the page, until &lt;/[a-zA-Z] is encountered.
     * @param tag The tag this scanner is responsible for.
     * @param lexer The source of CDATA.
     * @param stack The parse stack, <em>not used</em>.
     */
    public Tag scan (Tag tag, Lexer lexer, NodeList stack)
        throws ParserException
    {
        Node content;
        int position;
        Node node;
        Attribute attribute;
        Vector vector;

        content = lexer.parseCDATA ();
        position = lexer.getPosition ();
        node = lexer.nextNode (false);
        if (null != node)
            if (!(node instanceof Tag) || !(   ((Tag)node).isEndTag ()
                && ((Tag)node).getTagName ().equals (tag.getIds ()[0])))
            {
                lexer.setPosition (position);
                node = null;
            }

        // build new end tag if required
        if (null == node)
        {
            attribute = new Attribute ("/style", null);
            vector = new Vector ();
            vector.addElement (attribute);
            node = lexer.getNodeFactory ().createTagNode (
                lexer.getPage (), position, position, vector);
        }
        tag.setEndTag ((Tag)node);
        if (null != content)
        {
            tag.setChildren (new NodeList (content));
            content.setParent (tag);
        }
        node.setParent (tag);
        tag.doSemanticAction ();

        return (tag);
    }
}
