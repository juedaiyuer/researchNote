// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/util/sort/Ordered.java $
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

package org.htmlparser.util.sort;

/**
 * Describes an object that knows about ordering.
 * Implementors must have a comparison function,
 * which imposes a partial ordering on some
 * collection of objects. Ordered objects can be passed to a
 * sort method (such as org.htmlparser.util.sort.Sort) to allow precise control
 * over the sort order.
 * <p>
 * An set of elements S is partially ordered
 * if and only if <code>e1.compare(e2)==0</code> implies that
 * <code>e1.equals(e2)</code> for every e1 and e2 in S.
 * <p>
 * This all goes away in JDK 1.2.
 * <p>
 * For use with java.lang.Comparable from JDK 1.2:
 * <pre>
 * public int compare (Object o1, Object o2)
 * {
 *     return (((Ordered)o1).compare (o2));
 * }
 * </pre>
 * @see Sort
 */
public interface Ordered
{
    /**
     * Compares this object with another for order.
     * Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater
     * than the second.
     * <p>
     * The implementor must ensure that
     * <code>sgn(x.compare(y)) == -sgn(y.compare(x))</code>
     * for all x and y. (This implies that <code>x.compare(y)</code>
     * must throw an exception if and only if <code>y.compare(x)</code>
     * throws an exception.)
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * <code>((x.compare(y)>0) && (y.compare(z)>0))</code>
     * implies <code>x.compare(z)>0</code>.
     * <p>
     * Finally, the implementer must ensure that
     * <code>x.compare(y)==0</code> implies that
     * <code>sgn(x.compare(z))==sgn(y.compare(z))</code>
     * for all z.
     * @param that The object to compare this object against.
     * @return A negative integer, zero, or a positive
     * integer as this object is less than, equal to,
     * or greater than the second.
     * @exception ClassCastException The arguments type prevents it
     * from being compared by this Ordered.
     */
    public int compare (Object that);
}
