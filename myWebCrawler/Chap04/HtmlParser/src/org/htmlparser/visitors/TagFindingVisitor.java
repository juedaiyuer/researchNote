// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Joshua Kerievsky
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/TagFindingVisitor.java $
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

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;

public class TagFindingVisitor extends NodeVisitor {
    private String [] tagsToBeFound;
    private int count [];
    private int endTagCount [];
    private NodeList [] tags;
    private NodeList [] endTags;
    private boolean endTagCheck;

    public TagFindingVisitor(String [] tagsToBeFound) {
        this(tagsToBeFound,false);
    }

    public TagFindingVisitor(String [] tagsToBeFound, boolean endTagCheck) {
        this.tagsToBeFound = tagsToBeFound;
        this.tags = new NodeList[tagsToBeFound.length];
        if (endTagCheck) {
            endTags = new NodeList[tagsToBeFound.length];
            endTagCount = new int[tagsToBeFound.length];
        }
        for (int i=0;i<tagsToBeFound.length;i++) {
            tags[i] = new NodeList();
            if (endTagCheck)
                endTags[i] = new NodeList();
        }
        this.count = new int[tagsToBeFound.length];
        this.endTagCheck = endTagCheck;
    }

    public int getTagCount(int index) {
        return count[index];
    }

    public void visitTag(Tag tag)
    {
        for (int i=0;i<tagsToBeFound.length;i++)
            if (tag.getTagName().equalsIgnoreCase(tagsToBeFound[i])) {
                count[i]++;
                tags[i].add(tag);
            }
    }

    public void visitEndTag(Tag tag)
    {
        if (!endTagCheck) return;
        for (int i=0;i<tagsToBeFound.length;i++)
            if (tag.getTagName().equalsIgnoreCase(tagsToBeFound[i]))
            {
                endTagCount[i]++;
                endTags[i].add(tag);
            }
    }

    public Node [] getTags(int index) {
        return tags[index].toNodeArray();
    }

    public int getEndTagCount(int index) {
        return endTagCount[index];
    }

}
