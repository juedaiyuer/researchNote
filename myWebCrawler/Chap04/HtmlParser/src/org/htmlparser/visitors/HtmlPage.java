// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/HtmlPage.java $
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

import org.htmlparser.Parser;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.Tag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public class HtmlPage extends NodeVisitor {
    private String title;
    private NodeList nodesInBody;
    private NodeList tables;

    public HtmlPage(Parser parser) {
        super(true);
        title = "";
        nodesInBody = new NodeList();
        tables = new NodeList();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void visitTag(Tag tag)
    {
        if (isTable(tag))
            tables.add(tag);
        else if (isBodyTag(tag))
            nodesInBody = tag.getChildren ();
        else if (isTitleTag(tag))
            title = ((TitleTag)tag).getTitle();
    }

    private boolean isTable(Tag tag)
    {
        return (tag instanceof TableTag);
    }

    private boolean isBodyTag(Tag tag)
    {
        return (tag instanceof BodyTag);
    }

    private boolean isTitleTag(Tag tag)
    {
        return (tag instanceof TitleTag);
    }

    public NodeList getBody() {
        return nodesInBody;
    }

    public TableTag [] getTables()
    {
        TableTag [] tableArr = new TableTag[tables.size()];
        tables.copyToNodeArray (tableArr);
        return tableArr;
    }
}
