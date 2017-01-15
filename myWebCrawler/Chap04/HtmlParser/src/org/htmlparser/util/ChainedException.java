// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Claude Duguay
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/util/ChainedException.java $
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
 * Support for chained exceptions in code that predates Java 1.4.
 * A chained exception can use a Throwable argument to reference
 * a lower level exception. The chained exception provides a
 * stack trace that includes the message and any throwable
 * exception included as an argument in the chain.
 *
 * For example:
 *
 *   ApplicationException: Application problem encountered;
 *   ProcessException: Unable to process document;
 *   java.io.IOException: Unable to open 'filename.ext'
 *     at ChainedExceptionTest.openFile(ChainedExceptionTest.java:19)
 *     at ChainedExceptionTest.processFile(ChainedExceptionTest.java:27)
 *     at ChainedExceptionTest.application(ChainedExceptionTest.java:40)
 *     at ChainedExceptionTest.main(ChainedExceptionTest.java:52)
 *
 * Represents the output from two nested exceptions. The outside
 * exception is a subclass of ChainedException called
 * ApplicationException, which includes a throwable reference.
 * The throwable reference is also a subclass of ChainedException,
 * called ProcessException, which in turn includes a reference to
 * a standard IOException. In each case, the message is increasingly
 * specific about the nature of the problem. The end user may only
 * see the application exception, but debugging is greatly
 * enhanced by having more details in the stack trace.
 *
 * @author Claude Duguay
 **/

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Vector;

public class ChainedException
  extends Exception
{
  protected Throwable throwable;

  public ChainedException() {}

  public ChainedException(String message)
  {
    super(message);
  }

  public ChainedException(Throwable throwable)
  {
    this.throwable = throwable;
  }

  public ChainedException(String message, Throwable throwable)
  {
    super(message);
    this.throwable = throwable;
  }

  public String[] getMessageChain()
  {
    Vector list = getMessageList();
    String[] chain = new String[list.size()];
    list.copyInto (chain);
    return chain;
  }

  public Vector getMessageList()
  {
    Vector list = new Vector();
    list.addElement(getMessage());
    if (throwable != null)
    {
      if (throwable instanceof ChainedException)
      {
        ChainedException chain = (ChainedException)throwable;
        Vector sublist = chain.getMessageList ();
        for (int i = 0; i < sublist.size (); i++)
            list.addElement (sublist.elementAt (i));
      }
      else
      {
        String message = throwable.getMessage();
        if (message != null && !message.equals(""))
        {
          list.addElement (message);
        }
      }
    }
    return list;
  }

  public Throwable getThrowable()
  {
    return throwable;
  }

  public void printStackTrace()
  {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintStream out)
  {
    synchronized (out)
    {
      if (throwable != null)
      {
        out.println(getClass().getName() +
          ": " + getMessage() + ";");
        throwable.printStackTrace(out);
      }
      else
      {
        super.printStackTrace(out);
      }
    }
  }

  public void printStackTrace(PrintWriter out)
  {
    synchronized (out)
    {
      if (throwable != null)
      {
        out.println(getClass().getName() +
          ": " + getMessage() + ";");
        throwable.printStackTrace(out);
      }
      else
      {
        super.printStackTrace(out);
      }
    }
  }
}

