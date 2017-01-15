package com.lietu.sst;

import org.w3c.dom.Node;

import junit.framework.TestCase;

public class testStyleNodeSet extends TestCase {
        private static String str1 = "<tr><a href=\"www.daum.net\">中国</a></tr>";
        private static String str2 = "<tr><a href=\"www.daum.net\">中国</a><p>汽车</p></tr>";
        public void testEquals() {
                Node node = null;
                ElementNode root = ElementNode.getInstanceOf();
                
                node = (Node)StyleTree.parseBytes(str1.getBytes());
                root.trainNode(node);
                
                node = (Node)StyleTree.parseBytes(str2.getBytes());
                root.trainNode(node);
                
                root.printInformation();
        }
}
