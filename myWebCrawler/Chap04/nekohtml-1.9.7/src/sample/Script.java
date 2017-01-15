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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.DefaultFilter;
import org.cyberneko.html.filters.Identity;
import org.cyberneko.html.filters.Writer;

/**
 * This sample demonstrates how to use of the <code>pushInputSource</code>
 * method of the HTMLConfiguration in order to dynamically insert content
 * into the HTML stream. The typical use for this functionality is to
 * insert the result of an embedded script into the HTML document in place
 * of the script.
 * <p>
 * This particular example defines a new script language called "NekoHTML"
 * script that is a tiny subset of the NSGMLS format. The following table 
 * enumerates the NSGMLS features supported by this script language:
 * <table border='1' cellspacing='0', cellpadding='3'>
 * <tr><th>(<i>name</i><td>A start element with the specified <i>name</i>.
 * <tr><th>"<i>text</i><td>Character content with the specified <i>text</i>.
 * <tr><th>)<i>name</i><td>An end element with the specified <i>name</i>.
 * </table>
 * <p>
 * In this format, every <i>command</i> is specified on a line by itself.
 * For example, the following document:
 * <pre>
 * &lt;script type='NekoHTML'&gt;
 * (h1
 * "Header
 * )h1
 * &lt;/script&gt;
 * </pre>
 * is equivalent to the following HTML document:
 * <pre>
 * &lt;H1&gt;Header&lt;/H1&gt;
 * </pre>
 * as seen by document handler registered with the parser, when processed 
 * by this filter.
 *
 * @author Andy Clark
 *
 * @version $Id: Script.java,v 1.3 2004/02/19 20:00:17 andyc Exp $
 */
public class Script
    extends DefaultFilter {

    //
    // Constants
    //

    /** Augmentations feature identifier. */
    protected static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Filters property identifier. */
    protected static final String FILTERS = "http://cyberneko.org/html/properties/filters";

    /** Script type ("text/x-nekoscript"). */
    protected static final String SCRIPT_TYPE = "text/x-nekoscript";

    //
    // Data
    //

    /** The NekoHTML configuration. */
    protected HTMLConfiguration fConfiguration;

    /** A string buffer to collect the "script". */
    protected StringBuffer fBuffer;
    
    /** The system identifier of the source document. */
    protected String fSystemId;

    /** The script count. */
    protected int fScriptCount;

    //
    // Constructors
    //

    /** Constructs a script object with the specified configuration. */
    public Script(HTMLConfiguration config) {
        fConfiguration = config;
    } // <init>(HTMLConfiguration)

    //
    // XMLDocumentHandler methods
    //

    /** Start document. */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) 
        throws XNIException {
        fBuffer = null;
        fSystemId = locator != null ? locator.getLiteralSystemId() : null;
        fScriptCount = 0;
        super.startDocument(locator, encoding, augs);
    } // startDocument(XMLLocator,String,Augmentations)

    /** Start element. */
    public void startElement(QName element, XMLAttributes attrs, Augmentations augs)
        throws XNIException {
        if (element.rawname.equalsIgnoreCase("script") && attrs != null) {
            String value = attrs.getValue("type");
            if (value != null && value.equalsIgnoreCase(SCRIPT_TYPE)) {
                fBuffer = new StringBuffer();
                return;
            }
        }
        super.startElement(element, attrs, augs);
    } // startElement(QName,XMLAttributes,Augmentations)

    /** Empty element. */
    public void emptyElement(QName element, XMLAttributes attrs, Augmentations augs)
        throws XNIException {
        if (element.rawname.equalsIgnoreCase("script") && attrs != null) {
            String value = attrs.getValue("type");
            if (value != null && value.equalsIgnoreCase(SCRIPT_TYPE)) {
                return;
            }
        }
        super.emptyElement(element, attrs, augs);
    } // emptyElement(QName,XMLAttributes,Augmentations)

    /** Characters. */
    public void characters(XMLString text, Augmentations augs)
        throws XNIException {
        if (fBuffer != null) {
            fBuffer.append(text.ch, text.offset, text.length);
        }
        else {
            super.characters(text, augs);
        }
    } // characters(XMLString,Augmentations)

    /** End element. */
    public void endElement(QName element, Augmentations augs) throws XNIException {
        if (fBuffer != null) {
            try {
                // run "script" and generate HTML output
                BufferedReader in = new BufferedReader(new StringReader(fBuffer.toString()));
                StringWriter sout = new StringWriter();
                PrintWriter out = new PrintWriter(sout);
                String line;
                while ((line = in.readLine()) != null) {
                    line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    switch (line.charAt(0)) {
                        case '(': {
                            out.print('<');
                            out.print(line.substring(1));
                            out.print('>');
                            break;
                        }
                        case '"': {
                            out.print(line.substring(1));
                            break;
                        }
                        case ')': {
                            out.print("</");
                            out.print(line.substring(1));
                            out.print('>');
                            break;
                        }
                    }
                }

                // push new input source
                String systemId = fSystemId != null ? fSystemId+'_' : "";
                fScriptCount++;
                systemId += "script"+fScriptCount;
                XMLInputSource source = new XMLInputSource(null, systemId, null,
                                                           new StringReader(sout.toString()),
                                                           "UTF-8");
                fConfiguration.pushInputSource(source);
            }
            catch (IOException e) {
                // ignore
            }
            finally {
                fBuffer = null;
            }
        }
        else {
            super.endElement(element, augs);
        }
    } // endElement(QName,Augmentations)

    //
    // MAIN
    //

    /** Main. */
    public static void main(String[] argv) throws Exception {
        HTMLConfiguration parser = new HTMLConfiguration();
        parser.setFeature(AUGMENTATIONS, true);
        XMLDocumentFilter[] filters = { new Script(parser), new Identity(), new Writer() };
        parser.setProperty(FILTERS, filters);
        for (int i = 0; i < argv.length; i++) {
            parser.parse(new XMLInputSource(null, argv[i], null));
        }
    } // main(String[])

} // class Script
