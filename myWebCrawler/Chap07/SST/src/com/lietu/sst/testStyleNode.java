package com.lietu.sst;

import org.w3c.dom.Node;

import junit.framework.TestCase;

public class testStyleNode extends TestCase {
        private ElementNode styleNode;
        private final static String ahrefString = "<a href=\"www.daum.net\">abc</a>";
        private final static String textString = "abc";
        protected void setUp() {
        }
        
        protected void tearDown() {
        }
        
        public void testRootToString() {
                this.styleNode = ElementNode.getInstanceOf();
                //assertEquals(StyleNode.nodeToString(this.styleNode.getNode()), "root");
        }
        
        public void testAhrefToString() {
                this.styleNode = ElementNode.getInstanceOf((Node)StyleTree.parseBytes(ahrefString.getBytes()));
                //assert(ahrefString.equalsIgnoreCase(StyleNode.nodeToString(styleNode.getNode())));
        }
        
        public void testTextToString() {
                this.styleNode = ElementNode.getInstanceOf((Node)StyleTree.parseBytes(textString.getBytes()));
                //assert(textString.equalsIgnoreCase(StyleNode.nodeToString(styleNode.getNode())));
        }
        
        public void testAddSameStyleSet() {
                Node node = null;
                this.styleNode = ElementNode.getInstanceOf();
                
                node = (Node)StyleTree.parseBytes(ahrefString.getBytes());;
                this.styleNode.trainNode(node);
                assertEquals(1, this.styleNode.getChildren().size());
                
                node = (Node)StyleTree.parseBytes(ahrefString.getBytes());
                this.styleNode.trainNode(node);
                assertEquals(1, this.styleNode.getChildren().size());
        }
        
        public void testAddDifferentStyleSet() {
                Node node = null;
                this.styleNode = ElementNode.getInstanceOf();
                
                node = (Node)StyleTree.parseBytes(ahrefString.getBytes());;
                this.styleNode.trainNode(node);
                assertEquals(1, this.styleNode.getChildren().size());
                
                node = (Node)StyleTree.parseBytes(textString.getBytes());
                this.styleNode.trainNode(node);
                assertEquals(2, this.styleNode.getChildren().size());
        }
}
