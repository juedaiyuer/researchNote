// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/TextExtractingVisitor.java $
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

package org.htmlparser.visitors;

import org.htmlparser.Text;
import org.htmlparser.Tag;
import org.htmlparser.util.Translate;


/**
 * Extracts text from a web page.
 * Usage:
 * <code>
 * Parser parser = new Parser(...);
 * TextExtractingVisitor visitor = new TextExtractingVisitor();
 * parser.visitAllNodesWith(visitor);
 * String textInPage = visitor.getExtractedText();
 * </code>
 */
public class TextExtractingVisitor extends NodeVisitor {
    private StringBuffer textAccumulator;
    private boolean preTagBeingProcessed;

    public TextExtractingVisitor() {
        textAccumulator = new StringBuffer();
        preTagBeingProcessed = false;
    }

    public String getExtractedText() {
        return textAccumulator.toString();
    }

    public void visitStringNode(Text stringNode) {
        String text = stringNode.getText();
        if (!preTagBeingProcessed) {
            text = Translate.decode(text);
            text = replaceNonBreakingSpaceWithOrdinarySpace(text);
        }
        textAccumulator.append(text);
    }

    private String replaceNonBreakingSpaceWithOrdinarySpace(String text) {
        return text.replace('\u00a0',' ');
    }

    public void visitTag(Tag tag)
    {
        if (isPreTag(tag))
            preTagBeingProcessed = true;
    }

    public void visitEndTag(Tag tag)
    {
        if (isPreTag(tag))
            preTagBeingProcessed = false;
    }

    private boolean isPreTag(Tag tag) {
        return tag.getTagName().equals("PRE");
    }

}
