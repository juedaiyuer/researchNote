// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Somik Raha
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/util/NodeTreeWalker.java $
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

import org.htmlparser.Node;

/**
 * A class for walking a tree of {@link Node} objects, in either a depth-first or breadth-first manner.
 * The following two diagrams show the represent tree traversal with the two different methods.
 * <table>
 *  <tr>
 *   <th>Depth-first traversal</th>
 *   <th>Breadth-first traversal</th>
 *  </tr>
 *  <tr>
 *   <img src="http://htmlparser.sourceforge.net/tree-traversal-depth-first.gif" alt="Diagram showing depth-first tree traversal" width="300" height="300" />
 *  </tr>
 *  <tr>
 *   <img src="http://htmlparser.sourceforge.net/tree-traversal-breadth-first.gif" alt="Diagram showing breadth-first tree traversal" width="300" height="300" />
 *  </tr>
 * </table>
 * @author  ian_macfarlane
 */
public class NodeTreeWalker implements NodeIterator
{

    /**
     * The root Node element which defines the scope of the current tree to walk.
     */
    protected Node mRootNode;
    
    /**
     * The current Node element, which will be a child of the root Node, or null.
     */
    protected Node mCurrentNode;
    
    /**
     * The next Node element after the current Node element.
     * Stored for internal use only.
     */
    protected Node mNextNode;
    
    /**
     * The maximum depth (child-parent links) from which this NodeTreeWalker may be removed from the root Node.
     * A value of -1 indicates that there is no depth restriction.
     */
    protected int mMaxDepth;
    
    /**
     * Whether the tree traversal method used is depth-first (default) or breadth-first.
     */
    protected boolean mDepthFirst;
    
    /**
     * Creates a new instance of NodeTreeWalker using depth-first tree traversal, without limits on how deep it may traverse.
     * @param rootNode Node The Node to set as the root of the tree.
     * @throws NullPointerException if root Node is null.
     */
    public NodeTreeWalker(Node rootNode)
    {
        this(rootNode, true, -1);
    }
    
    /**
     * Creates a new instance of NodeTreeWalker using the specified type of tree traversal, without limits on how deep it may traverse.
     * @param rootNode The Node to set as the root of the tree.
     * @param depthFirst Whether to use depth-first (true) or breadth-first (false) tree traversal.
     * @throws NullPointerException if rootNode is null.
     */
    public NodeTreeWalker(Node rootNode, boolean depthFirst)
    {
        this(rootNode, depthFirst, -1);
    }
    
    /**
     * Creates a new instance of NodeTreeWalker using the specified type of tree traversal and maximum depth from the root Node to traverse.
     * @param rootNode The Node to set as the root of the tree.
     * @param depthFirst Whether to use depth-first (true) or breadth-first (false) tree traversal.
     * @param maxDepth The maximum depth from the root Node that this NodeTreeWalker may traverse. This must be > 0 or equal to -1.
     * @throws NullPointerException if rootNode is null.
     * @throws IllegalArgumentException maxDepth is not > 0 or equal to -1.
     */
    public NodeTreeWalker(Node rootNode, boolean depthFirst, int maxDepth)
    {
        //check maxDepth is valid
        if ( ! ((maxDepth >= 1) || (maxDepth == -1)))//if not one of these valid possibilities
            throw new IllegalArgumentException("Paramater maxDepth must be > 0 or equal to -1.");
        initRootNode(rootNode);//this method also checks if rootNode is valid
        this.mDepthFirst = depthFirst;
        this.mMaxDepth = maxDepth;
    }
    
    /**
     * Whether the NodeTreeWalker is currently set to use depth-first or breadth-first tree traversal.
     * @return True if depth-first tree-traversal is used, or false if breadth-first tree-traversal is being used.
     */
    public boolean isDepthFirst()
    {
        return (this.mDepthFirst);
    }
    
    /**
     * Sets whether the NodeTreeWalker should use depth-first or breadth-first tree traversal.
     * @param depthFirst Whether to use depth-first (true) or breadth-first (false) tree traversal.
     */
    public void setDepthFirst(boolean depthFirst)
    {
        if (this.mDepthFirst != depthFirst)//if we are changing search pattern
            this.mNextNode = null;
        this.mDepthFirst = depthFirst;
    }
    
    /**
     * The maximum depth (number of child-parent links) below the root Node that this NodeTreeWalker may traverse.
     * @return The maximum depth that this NodeTreeWalker can traverse to.
     */
    public int getMaxDepth()
    {
        return (this.mMaxDepth);
    }
    
    /**
     * Removes any restrictions in place that prevent this NodeTreeWalker from traversing beyond a certain depth.
     */
    public void removeMaxDepthRestriction()
    {
        this.mMaxDepth = -1;
    }
    
    /**
     * Get the root Node that defines the scope of the tree to traverse.
     * @return The root Node.
     */
    public Node getRootNode()
    {
        return (this.mRootNode);
    }
    
    /**
     * Get the Node in the tree that the NodeTreeWalker is current at.
     * @return The current Node.
     */
    public Node getCurrentNode()
    {
        return (this.mCurrentNode);
    }
    
    /**
     * Sets the current Node as the root Node.
     * Resets the current position in the tree.
     * @throws NullPointerException if the current Node is null (i.e. if the tree traversal has not yet begun).
     */
    public void setCurrentNodeAsRootNode() throws NullPointerException
    {
        if (this.mCurrentNode == null)
            throw new NullPointerException("Current Node is null, cannot set as root Node.");
        initRootNode(this.mCurrentNode);
    }
    
    /**
     * Sets the specified Node as the root Node.
     * Resets the current position in the tree.
     * @param rootNode The Node to set as the root of the tree.
     * @throws NullPointerException if rootNode is null.
     */
    public void setRootNode(Node rootNode) throws NullPointerException
    {
        initRootNode(rootNode);
    }
    
    /**
     * Resets the current position in the tree,
     * such that calling <code>nextNode()</code> will return the first Node again.
     */
    public void reset()
    {
        this.mCurrentNode = null;
        this.mNextNode = null;
    }
    
    /**
     * Traverses to the next Node from the current Node, using either depth-first or breadth-first tree traversal as appropriate.
     * @return The next Node from the current Node.
     */
    public Node nextNode()
    {
        if (this.mNextNode != null)//check if we've already found the next Node by calling hasMoreNodes()
        {
            this.mCurrentNode = this.mNextNode;
            this.mNextNode = null;//reset mNextNode
        }
        else
        {
            //Check if we have started traversing yet. If not, start with first child (for either traversal method).
            if (this.mCurrentNode == null)
                this.mCurrentNode = this.mRootNode.getFirstChild();
            else
            {
                if (this.mDepthFirst)
                    this.mCurrentNode = getNextNodeDepthFirst();
                else
                    this.mCurrentNode = getNextNodeBreadthFirst();
            }
        }
        return (this.mCurrentNode);
    }
    
    /**
     * Get the number of places down that the current Node is from the root Node.
     * Returns 1 if current Node is a child of the root Node.
     * Returns 0 if this NodeTreeWalker has not yet traversed to any Nodes.
     * @return The depth the current Node is from the root Node.
     */
    public int getCurrentNodeDepth()
    {
        int depth = 0;
        if (this.mCurrentNode != null)//if we are not at the root Node.
        {
            Node traverseNode = this.mCurrentNode;
            while (traverseNode != this.mRootNode)
            {
                ++depth;
                traverseNode = traverseNode.getParent();
            }
        }
        return (depth);
    }
    
    /**
     * Returns whether or not there are more nodes available based on the current configuration of this NodeTreeWalker.
     * @return True if there are more Nodes available, based on the current configuration, or false otherwise.
     */
    public boolean hasMoreNodes()
    {
        if (this.mNextNode == null)//if we've already generated mNextNode
        {
            if (this.mCurrentNode == null)
                this.mNextNode = this.mRootNode.getFirstChild();
            else
            {
                if (this.mDepthFirst)
                    this.mNextNode = getNextNodeDepthFirst();
                else
                    this.mNextNode = getNextNodeBreadthFirst();
            }
        }
        return (this.mNextNode != null);
    }
    
    /**
     * Sets the root Node to be the given Node.
     * Resets the current position in the tree.
     * @param rootNode The Node to set as the root of the tree.
     * @throws NullPointerException if rootNode is null.
     */
    protected void initRootNode(Node rootNode) throws NullPointerException
    {
        if (rootNode == null)
            throw new NullPointerException("Root Node cannot be null.");
        this.mRootNode = rootNode;
        this.mCurrentNode = null;
        this.mNextNode = null;
    }
    
    /**
     * Traverses to the next Node from the current Node using depth-first tree traversal
     * @return The next Node from the current Node using depth-first tree traversal.
     */
    protected Node getNextNodeDepthFirst()
    {
        //loosely based on http://www.myarch.com/treeiter/traditways.jhtml
        int currentDepth = getCurrentNodeDepth();
        Node traverseNode = null;
        if ((this.mMaxDepth == -1) || (currentDepth < this.mMaxDepth))//if it is less than max depth, then getting first child won't be more than max depth
        {
            traverseNode = this.mCurrentNode.getFirstChild();
            if (traverseNode != null)
                return (traverseNode);
        }
        
        traverseNode = this.mCurrentNode;
        
        Node tempNextSibling = null;//keeping a reference to this this saves calling getNextSibling once later
        while ((traverseNode != this.mRootNode) && (tempNextSibling = traverseNode.getNextSibling()) == null)//CANNOT assign traverseNode as root Node
            traverseNode = traverseNode.getParent();// use child-parent link to get to the parent level
        
        return (tempNextSibling);//null if ran out of Node's
    }
    
    /**
     * Traverses to the next Node from the current Node using breadth-first tree traversal
     * @return The next Node from the current Node using breadth-first tree traversal.
     */
    protected Node getNextNodeBreadthFirst()
    {
        Node traverseNode;
        
        //see if the mCurrentNode has a sibling after it
        traverseNode = this.mCurrentNode.getNextSibling();
        if (traverseNode != null)
            return (traverseNode);
        
        int depth = getCurrentNodeDepth();
        
        //try and find the next Node at the same depth that is not a sibling

        NodeList traverseNodeList;
        
        //step up to the parent Node to look through its children
        traverseNode = this.mCurrentNode.getParent();
        int currentDepth = depth - 1;
        
        while(currentDepth > 0)//this is safe as we've tried getNextSibling already
        {
            Node tempNextSibling = null;//keeping a reference to this this saves calling getNextSibling once later
            //go to first parent with nextSibling, then to that sibling
            while(((tempNextSibling = traverseNode.getNextSibling()) == null) && (traverseNode != this.mRootNode))//CAN assign traverseNode as root Node
            {
                traverseNode = traverseNode.getParent();
                --currentDepth;
            }
            
            //if have traversed back to the root Node, skip to next part where it finds the first Node at the next depth down
            if (traverseNode == this.mRootNode)
                break;
            
            traverseNode = tempNextSibling;
            
            if (traverseNode != null)
            {
                //go through children of that sibling
                traverseNodeList = traverseNode.getChildren();
                while((traverseNodeList != null) && (traverseNodeList.size() != 0))
                {
                    traverseNode = traverseNode.getFirstChild();
                    ++currentDepth;
                    if (currentDepth == depth)
                        return (traverseNode);//found the next Node at the current depth
                    else
                        traverseNodeList = traverseNode.getChildren();
                } // while((traverseNodeList != null) && (traverseNodeList.size() != 0))
            } // if (traverseNode != null)
        } // while(currentDepth > 0)
        
        //step to the next depth down
        
        //check first whether we are about to go past max depth
        if (this.mMaxDepth != -1)//if -1, then there is no max depth restriction
        {
            if (depth >= this.mMaxDepth)
                return (null);//can't go past max depth
        }
        
        traverseNode = this.mRootNode.getFirstChild();
        ++depth;//look for next depth
        currentDepth = 1;
        while(currentDepth > 0)
        {
            //go through children of that sibling
            traverseNodeList = traverseNode.getChildren();
            while((traverseNodeList != null) && (traverseNodeList.size() != 0))
            {
                traverseNode = traverseNode.getFirstChild();
                ++currentDepth;
                if (currentDepth == depth)
                    return (traverseNode);//found the next Node at the current depth
                else
                    traverseNodeList = traverseNode.getChildren();
            } // while((traverseNodeList != null) && (traverseNodeList.size() != 0))
            
            //go to first parent with nextSibling, then to that sibling
            while((traverseNode.getNextSibling() == null) && (traverseNode != this.mRootNode))
            {
                traverseNode = traverseNode.getParent();
                --currentDepth;
            }
            traverseNode = traverseNode.getNextSibling();
            if (traverseNode == null)//if null (i.e. reached end of tree), return null
                return (null);
        } // while(currentDepth > 0)
        
        //otherwise, finished searching, return null
        return (null);
    }
    
    
    // todo
    
    // previousNode()
    // getPreviousNodeDepthFirst()
    // getPreviousNodeBreadthFirst()
    // hasPreviousNodes() ?
    // these should be specificed in an interface - suggest something like ReversableNodeIterator (extends NodeIterator)
    // possible optimisations: when doing mNextNode, we should save mCurrentNode as previousNode, and vice versa
}