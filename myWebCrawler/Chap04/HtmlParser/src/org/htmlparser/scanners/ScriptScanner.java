// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/scanners/ScriptScanner.java $
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
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Cursor;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.scanners.ScriptDecoder;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * The ScriptScanner handles script CDATA.
 */
public class ScriptScanner
    extends
        CompositeTagScanner
{
    /**
     * Strict parsing of CDATA flag.
     * If this flag is set true, the parsing of script is performed without
     * regard to quotes. This means that erroneous script such as:
     * <pre>
     * document.write("&lt;/script&gt");
     * </pre>
     * will be parsed in strict accordance with appendix
     * <a href="http://www.w3.org/TR/html4/appendix/notes.html#notes-specifying-data">
     * B.3.2 Specifying non-HTML data</a> of the
     * <a href="http://www.w3.org/TR/html4/">HTML 4.01 Specification</a> and
     * hence will be split into two or more nodes. Correct javascript would
     * escape the ETAGO:
     * <pre>
     * document.write("&lt;\/script&gt");
     * </pre>
     * If true, CDATA parsing will stop at the first ETAGO ("&lt;/") no matter
     * whether it is quoted or not. If false, balanced quotes (either single or
     * double) will shield an ETAGO. Beacuse of the possibility of quotes within
     * single or multiline comments, these are also parsed. In most cases,
     * users prefer non-strict handling since there is so much broken script
     * out in the wild.
     */
    public static boolean STRICT = true;

    /**
     * Create a script scanner.
     */
    public ScriptScanner()
    {
    }

    /**
     * Scan for script.
     * Accumulates text from the page, until &lt;/[a-zA-Z] is encountered.
     * @param tag The tag this scanner is responsible for.
     * @param lexer The source of CDATA.
     * @param stack The parse stack, <em>not used</em>.
     */
    public Tag scan (Tag tag, Lexer lexer, NodeList stack)
        throws ParserException
    {
        String language;
        String code;
        Node content;
        int position;
        Node node;
        Attribute attribute;
        Vector vector;

        if (tag instanceof ScriptTag)
        {
            language = ((ScriptTag)tag).getLanguage ();
            if ((null != language) &&
                (language.equalsIgnoreCase ("JScript.Encode") ||
                 language.equalsIgnoreCase ("VBScript.Encode")))
            {
                code = ScriptDecoder.Decode (lexer.getPage (), lexer.getCursor ());
                ((ScriptTag)tag).setScriptCode (code);
            }
        }
        content = lexer.parseCDATA (!STRICT);
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
            attribute = new Attribute ("/script", null);
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
