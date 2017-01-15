// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Joshua Kerievsky
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/visitors/StringFindingVisitor.java $
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

import org.htmlparser.Text;

public class StringFindingVisitor extends NodeVisitor
{
    private String stringToFind;
    private int foundCount;
    private boolean multipleSearchesWithinStrings;
    private Locale locale;

    public StringFindingVisitor(String stringToFind)
    {
        this (stringToFind, null);
    }

    public StringFindingVisitor(String stringToFind, Locale locale)
    {
        this.locale = (null == locale) ? Locale.ENGLISH : locale;
        this.stringToFind = stringToFind.toUpperCase (this.locale);
        foundCount = 0;
        multipleSearchesWithinStrings = false;
    }

    public void doMultipleSearchesWithinStrings()
    {
        multipleSearchesWithinStrings = true;
    }

    public void visitStringNode(Text stringNode)
    {
        String stringToBeSearched = stringNode.getText().toUpperCase(locale);
        if (!multipleSearchesWithinStrings &&
            stringToBeSearched.indexOf(stringToFind) != -1) {
            foundCount++;
        } else if (multipleSearchesWithinStrings) {
            int index = -1;
            do {
                index = stringToBeSearched.indexOf(stringToFind, index+1);
                if (index!=-1)
                    foundCount++;
            } while (index != -1);
        }
    }

    public boolean stringWasFound()
    {
        return (0 != stringFoundCount());
    }

    public int stringFoundCount()
    {
        return foundCount;
    }

}
