/* 
 * Copyright 2002-2008 Andy Clark
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;

/**
 * This program tests the NekoHTML parser's use of the HTML DOM
 * implementation by printing the class names of all the nodes in
 * the parsed document.
 *
 * @author Andy Clark
 *
 * @version $Id: TestHTMLDOM.java,v 1.3 2004/02/19 20:00:17 andyc Exp $
 */
public class TestHTMLDOM {

    //
    // MAIN
    //

    /** Main. */
    public static void main(String[] argv) throws Exception {
        DOMParser parser = new DOMParser();
        for (int i = 0; i < argv.length; i++) {
            parser.parse(argv[i]);
            print(parser.getDocument(), "");
        }
    } // main(String[])

    //
    // Public static methods
    //

    /** Prints a node's class name. */
    public static void print(Node node, String indent) {
        System.out.println(indent+node.getClass().getName());
        node.getAttributes().getNamedItem("src");
        Node child = node.getFirstChild();
        while (child != null) {
            print(child, indent+" ");
            child = child.getNextSibling();
        }
    } // print(Node)

} // class TestHTMLDOM
