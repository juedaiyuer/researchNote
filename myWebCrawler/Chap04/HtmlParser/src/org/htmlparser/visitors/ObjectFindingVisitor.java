// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Joshua Kerievsky
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/ObjectFindingVisitor.java $
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

public class ObjectFindingVisitor extends NodeVisitor {
    private Class classTypeToFind;
    private NodeList tags;

    public ObjectFindingVisitor(Class classTypeToFind) {
        this(classTypeToFind,true);
    }

    public ObjectFindingVisitor(Class classTypeToFind,boolean recurse) {
        super(recurse, true);
        this.classTypeToFind = classTypeToFind;
        this.tags = new NodeList();
    }

    public int getCount() {
        return (tags.size ());
    }

    public void visitTag(Tag tag) {
        if (tag.getClass().equals(classTypeToFind))
            tags.add(tag);
    }

    public Node[] getTags() {
        return tags.toNodeArray();
    }
}
