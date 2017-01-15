// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/lexer/src/main/java/org/htmlparser/util/NodeList.java $
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

import java.io.Serializable;
import java.util.NoSuchElementException;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.visitors.NodeVisitor;

public class NodeList implements Serializable
{
    private static final int INITIAL_CAPACITY=10;
    //private static final int CAPACITY_INCREMENT=20;
    private Node nodeData[];
    private int size;
    private int capacity;
    private int capacityIncrement;

    public NodeList ()
    {
        removeAll ();
    }
    
    /**
     * Create a one element node list.
     * @param node The initial node to add.
     */
    public NodeList (Node node)
    {
        this ();
        add (node);
    }
    
    public void add (Node node)
    {
        if (size == capacity)
            adjustVectorCapacity ();
        nodeData[size++] = node;
    }
    
    /**
     * Add another node list to this one.
     * @param list The list to add.
     */
    public void add (NodeList list)
    {
        for (int i = 0; i < list.size; i++)
            add (list.nodeData[i]);
    }
    
    /**
     * Insert the given node at the head of the list.
     * @param node The new first element.
     */
    public void prepend (Node node)
    {
        if (size == capacity)
            adjustVectorCapacity ();
        System.arraycopy (nodeData, 0, nodeData, 1, size);
        size++;
        nodeData[0]=node;
    }
    
    private void adjustVectorCapacity ()
    {
        capacity += capacityIncrement;
        capacityIncrement *= 2;
        Node oldData [] = nodeData;
        nodeData = newNodeArrayFor (capacity);
        System.arraycopy (oldData, 0, nodeData, 0, size);
    }
    
    private Node[] newNodeArrayFor (int capacity)
    {
        return new Node[capacity];
    }
    
    public int size ()
    {
        return size;
    }
    
    public Node elementAt (int i)
    {
        return nodeData[i];
    }
    
    public SimpleNodeIterator elements ()
    {
        return new SimpleNodeIterator ()
        {
            int count = 0;
            
            public boolean hasMoreNodes ()
            {
                return count < size;
            }
            
            public Node nextNode ()
            {
                synchronized (NodeList.this)
                {
                    if (count < size)
                    {
                        return nodeData[count++];
                    }
                }
                throw new NoSuchElementException ("Vector Enumeration");
            }
        };
    }
    
    public Node [] toNodeArray ()
    {
        Node [] nodeArray = newNodeArrayFor (size);
        System.arraycopy (nodeData, 0, nodeArray, 0, size);
        return nodeArray;
    }
    
    public void copyToNodeArray (Node[] array)
    {
        System.arraycopy (nodeData, 0, array, 0, size);
    }
    
    public String asString ()
    {
        StringBuffer buff = new StringBuffer ();
        for (int i=0;i<size;i++)
            buff.append (nodeData[i].toPlainTextString ());
        return buff.toString ();
    }
    
    /**
     * Convert this nodelist into the equivalent HTML.
     * @param verbatim If <code>true</code> return as close to the original
     * page text as possible.
     * @return The contents of the list as HTML text.
     */
    public String toHtml (boolean verbatim)
    {
        StringBuffer ret;
        
        ret = new StringBuffer ();
        for (int i = 0; i < size; i++)
            ret.append (nodeData[i].toHtml (verbatim));

        return (ret.toString ());
    }

    /**
     * Convert this nodelist into the equivalent HTML.
     * @return The contents of the list as HTML text.
     */
    public String toHtml ()
    {
        return (toHtml (false));
    }

    /**
     * Remove the node at index.
     * @param index The index of the node to remove.
     * @return The node that was removed.
     */
    public Node remove (int index)
    {
        Node ret;

        ret = nodeData[index];
        System.arraycopy (nodeData, index+1, nodeData, index, size - index - 1);
        nodeData[size-1] = null;
        size--;

        return (ret);
    }
    
    public void removeAll ()
    {
        size = 0;
        capacity = INITIAL_CAPACITY;
        nodeData = newNodeArrayFor (capacity);
        capacityIncrement = capacity * 2;
    }
    
    /**
     * Check to see if the NodeList contains the supplied Node.
     * @param node The node to look for.
     * @return True is the Node is in this NodeList.
     */
    public boolean contains (Node node)
    {
        return (-1 != indexOf (node));
    }
    
    /**
     * Finds the index of the supplied Node.
     * @param node The node to look for.
     * @return The index of the node in the list or -1 if it isn't found.
     */
    public int indexOf (Node node)
    {
        int ret;

        ret = -1;
        for (int i = 0; (i < size) && (-1 == ret); i++)
            if (nodeData[i].equals (node))
                ret = i;

        return (ret);
    }
    
    /**
     * Remove the supplied Node from the list.
     * @param node The node to remove.
     * @return True if the node was found and removed from the list.
     */
    public boolean remove (Node node)
    {
        int index;
        boolean ret;

        ret = false;
        if (-1 != (index = indexOf (node)))
        {
            remove (index);
            ret = true;
        }

        return (ret);
    }

    /**
     * Return the contents of the list as a string.
     * Suitable for debugging.
     * @return A string representation of the list. 
     */
    public String toString()
    {
        StringBuffer ret;
        
        ret = new StringBuffer ();
        for (int i = 0; i < size; i++)
            ret.append (nodeData[i]);

        return (ret.toString ());
    }

    /**
     * Filter the list with the given filter non-recursively.
     * @param filter The filter to use.
     * @return A new node array containing the nodes accepted by the filter.
     * This is a linear list and preserves the nested structure of the returned
     * nodes only.
     */
    public NodeList extractAllNodesThatMatch (NodeFilter filter)
    {
        return (extractAllNodesThatMatch (filter, false));
    }

    /**
     * Filter the list with the given filter.
     * @param filter The filter to use.
     * @param recursive If <code>true<code> digs into the children recursively.
     * @return A new node array containing the nodes accepted by the filter.
     * This is a linear list and preserves the nested structure of the returned
     * nodes only.
     */
    public NodeList extractAllNodesThatMatch (NodeFilter filter, boolean recursive)
    {
        Node node;
        NodeList children;
        NodeList ret;

        ret = new NodeList ();
        for (int i = 0; i < size; i++)
        {
            node = nodeData[i];
            if (filter.accept (node))
                ret.add (node);
            if (recursive)
            {
                children = node.getChildren ();
                if (null != children)
                    ret.add (children.extractAllNodesThatMatch (filter, recursive));
            }
        }

        return (ret);
    }

    /**
     * Remove nodes not matching the given filter non-recursively.
     * @param filter The filter to use.
     */
    public void keepAllNodesThatMatch (NodeFilter filter)
    {
        keepAllNodesThatMatch (filter, false);
    }

    /**
     * Remove nodes not matching the given filter.
     * @param filter The filter to use.
     * @param recursive If <code>true<code> digs into the children recursively.
     */
    public void keepAllNodesThatMatch (NodeFilter filter, boolean recursive)
    {
        Node node;
        NodeList children;

        for (int i = 0; i < size; )
        {
            node = nodeData[i];
            if (!filter.accept (node))
                remove (i);
            else
            {
                if (recursive)
                {
                    children = node.getChildren ();
                    if (null != children)
                        children.keepAllNodesThatMatch (filter, recursive);
                }
                i++;
            }
        }
    }

    /**
     * Utility to apply a visitor to a node list.
     * Provides for a visitor to modify the contents of a page and get the
     * modified HTML as a string with code like this:
     * <pre>
     * Parser parser = new Parser ("http://whatever");
     * NodeList list = parser.parse (null); // no filter
     * list.visitAllNodesWith (visitor);
     * System.out.println (list.toHtml ());
     * </pre>
     */
    public void visitAllNodesWith (NodeVisitor visitor)
        throws
            ParserException
    {
        Node node;

        visitor.beginParsing ();
        for (int i = 0; i < size; i++)
            nodeData[i].accept (visitor);
        visitor.finishedParsing ();
    }
}
