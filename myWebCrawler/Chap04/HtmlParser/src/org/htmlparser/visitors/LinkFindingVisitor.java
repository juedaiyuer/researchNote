// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/LinkFindingVisitor.java $
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

import java.util.Locale;
import org.htmlparser.tags.LinkTag;

import org.htmlparser.Tag;

public class LinkFindingVisitor extends NodeVisitor
{
    private String linkTextToFind;
    private int count;
    private Locale locale;

    public LinkFindingVisitor (String linkTextToFind)
    {
        this (linkTextToFind, null);
    }

    public LinkFindingVisitor (String linkTextToFind, Locale locale)
    {
        count = 0;
        this.locale = (null == locale) ? Locale.ENGLISH : locale;
        this.linkTextToFind = linkTextToFind.toUpperCase (this.locale);
    }

    public void visitTag(Tag tag)
    {
        if (tag instanceof LinkTag)
            if (-1 != ((LinkTag)tag).getLinkText ().toUpperCase (locale).indexOf (linkTextToFind))
                count++;
    }

    public boolean linkTextFound()
    {
        return (0 != count);
    }

    public int getCount()
    {
        return (count);
    }

}
