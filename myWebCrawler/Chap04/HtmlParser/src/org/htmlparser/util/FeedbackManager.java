// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Claude Duguay
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/util/FeedbackManager.java $
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

package org.htmlparser.util;

/**
 * Implementaiton of static methods that allow the parser to
 * route various messages to any implementation of the
 * HTMLParserFeedback interface. End users can use the default
 * DefaultHTMLParserFeedback or may provide their own by calling
 * the setParserFeedback method.
 *
 * @see ParserFeedback
 * @see DefaultParserFeedback
**/

public class FeedbackManager
{
  protected static ParserFeedback callback =
    new DefaultParserFeedback();

  public static void setParserFeedback(ParserFeedback feedback)
  {
    callback = feedback;
  }

  public static void info(String message)
  {
    callback.info(message);
  }

  public static void warning(String message)
  {
    callback.warning(message);
  }

  public static void error(String message, ParserException e)
  {
    callback.error(message, e);
  }
}
