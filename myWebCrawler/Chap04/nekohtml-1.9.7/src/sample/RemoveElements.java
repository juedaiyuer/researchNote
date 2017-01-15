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

import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.ElementRemover;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * This is a sample that illustrates how to use the 
 * <code>ElementRemover</code> filter.
 *
 * @author Andy Clark
 *
 * @version $Id: RemoveElements.java,v 1.3 2004/02/19 20:00:17 andyc Exp $
 */
public class RemoveElements {

    //
    // MAIN
    //

    /** Main. */
    public static void main(String[] argv) throws Exception {

        // create element remover filter
        ElementRemover remover = new ElementRemover();

        // set which elements to accept
        remover.acceptElement("b", null);
        remover.acceptElement("i", null);
        remover.acceptElement("u", null);
        remover.acceptElement("a", new String[] { "href" });

        // completely remove script elements
        remover.removeElement("script");

        // create writer filter
        org.cyberneko.html.filters.Writer writer =
            new org.cyberneko.html.filters.Writer();

        // setup filter chain
        XMLDocumentFilter[] filters = {
            remover,
            writer,
        };

        // create HTML parser
        XMLParserConfiguration parser = new HTMLConfiguration();
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);

        // parse documents
        for (int i = 0; i < argv.length; i++) {
            String systemId = argv[i];
            XMLInputSource source = new XMLInputSource(null, systemId, null);
            parser.parse(source);
        }

    } // main(String[])

} // class RemoveElements