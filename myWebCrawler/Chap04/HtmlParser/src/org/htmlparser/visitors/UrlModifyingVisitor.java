// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Joshua Kerievsky
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/UrlModifyingVisitor.java $
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
import org.htmlparser.Remark;
import org.htmlparser.Text;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.Tag;

public class UrlModifyingVisitor extends NodeVisitor {
    private String linkPrefix;
    private StringBuffer modifiedResult;

    public UrlModifyingVisitor(String linkPrefix) {
        super(true,true);
        this.linkPrefix =linkPrefix;
        modifiedResult = new StringBuffer();
    }

    public void visitRemarkNode (Remark remarkNode)
    {
        modifiedResult.append (remarkNode.toHtml());
    }

    public void visitStringNode(Text stringNode)
    {
        modifiedResult.append (stringNode.toHtml());
    }

    public void visitTag(Tag tag)
    {
        if (tag instanceof LinkTag)
            ((LinkTag)tag).setLink(linkPrefix + ((LinkTag)tag).getLink());
        else if (tag instanceof ImageTag)
            ((ImageTag)tag).setImageURL(linkPrefix + ((ImageTag)tag).getImageURL());
        // process only those nodes that won't be processed by an end tag,
        // nodes without parents or parents without an end tag, since
        // the complete processing of all children should happen before
        // we turn this node back into html text
        if (null == tag.getParent ()
            && (!(tag instanceof CompositeTag) || null == ((CompositeTag)tag).getEndTag ()))
            modifiedResult.append(tag.toHtml());
    }

    public void visitEndTag(Tag tag)
    {
        Node parent;
        
        parent = tag.getParent ();
        // process only those nodes not processed by a parent
        if (null == parent)
            // an orphan end tag
            modifiedResult.append(tag.toHtml());
        else
            if (null == parent.getParent ())
                // a top level tag with no parents
                modifiedResult.append(parent.toHtml());
    }

    public String getModifiedResult() {
        return modifiedResult.toString();
    }
}
