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

package org.cyberneko.html;

import org.cyberneko.html.filters.DefaultFilter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;

/**
 * This class implements an filter to output "canonical" files for
 * regression testing.
 *
 * @author Andy Clark
 */
public class Writer
    extends DefaultFilter {

    //
    // Data
    //

    /** Writer. */
    protected PrintWriter out = new PrintWriter(System.out);

    // temp vars

    /** String buffer for collecting text content. */
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();

    //
    // Constructors
    //

    /** 
     * Creates a writer to the standard output stream using UTF-8 
     * encoding. 
     */
    public Writer() {
        this(System.out);
    } // <init>()

    /** 
     * Creates a writer with the specified output stream using UTF-8 
     * encoding. 
     */
    public Writer(OutputStream stream) {
        this(stream, "UTF8");
    } // <init>(OutputStream)

    /** Creates a writer with the specified output stream and encoding. */
    public Writer(OutputStream stream, String encoding) {
        try {
            out = new PrintWriter(new OutputStreamWriter(stream, encoding), true);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("JVM must have "+encoding+" decoder");
        }
    } // <init>(OutputStream,String)

    /** Creates a writer with the specified Java Writer. */
    public Writer(java.io.Writer writer) {
        out = new PrintWriter(writer);
    } // <init>(java.io.Writer)

    //
    // XMLDocumentHandler methods
    //

    // since Xerces-J 2.2.0

    /** Start document. */
    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext nscontext, Augmentations augs) throws XNIException {
        fStringBuffer.clear();
    } // startDocument(XMLLocator,String,NamespaceContext,Augmentations)

    // old methods

    /** Start document. */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException {
        startDocument(locator, encoding, null, augs);
    } // startDocument(XMLLocator,String,Augmentations)

    /** XML declaration. */
    public void xmlDecl(String version, String encoding, String standalone,
                        Augmentations augs) throws XNIException {
        if (version!=null) {
            out.print("xversion ");
            out.println(version);
        }
        if (encoding!=null) {
            out.print("xencoding ");
            out.println(encoding);
        }
        if (standalone!=null) {
            out.print("xstandalone ");
            out.println(standalone);
        }
        out.flush();
    } // xmlDecl(String,String,String,Augmentations)

    /** Doctype declaration. */
    public void doctypeDecl(String root, String pubid, String sysid, Augmentations augs) throws XNIException {
        chars();
        out.print('!');
        if (root != null) {
            out.print(root);
        }
        out.println();
        if (pubid != null) {
            out.print('p');
            out.print(pubid);
            out.println();
        }
        if (sysid != null) {
            out.print('s');
            out.print(sysid);
            out.println();
        }
        out.flush();
    } // doctypeDecl(String,String,String,Augmentations)

    /** Processing instruction. */
    public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
        chars();
        out.print('?');
        out.print(target);
        if (data != null && data.length > 0) {
            out.print(' ');
            print(data.toString());
        }
        out.println();
        out.flush();
    } // processingInstruction(String,XMLString,Augmentations)

    /** Comment. */
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        chars();
        out.print('#');
        print(text.toString());
        out.println();
        out.flush();
    } // comment(XMLString,Augmentations)

    /** Start element. */
    public void startElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
        chars();
        out.print('(');
        out.print(element.rawname);
        int acount = attrs != null ? attrs.getLength() : 0;
        if (acount > 0) {
            String[] anames = new String[acount];
            String[] auris = new String[acount];
            sortAttrNames(attrs, anames, auris);
            for (int i = 0; i < acount; i++) {
                String aname = anames[i];
                out.println();
                out.flush();
                out.print('A');
                if (auris[i] != null) {
                    out.print('{');
                    out.print(auris[i]);
                    out.print('}');
                }
                out.print(aname);
                out.print(' ');
                print(attrs.getValue(aname));
            }
        }
        out.println();
        out.flush();
    } // startElement(QName,XMLAttributes,Augmentations)

    /** End element. */
    public void endElement(QName element, Augmentations augs) throws XNIException {
        chars();
        out.print(')');
        out.print(element.rawname);
        out.println();
        out.flush();
    } // endElement(QName,Augmentations)

    /** Empty element. */
    public void emptyElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
        startElement(element, attrs, augs);
        endElement(element, augs);
    } // emptyElement(QName,XMLAttributes,Augmentations)

    /** Characters. */
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        fStringBuffer.append(text);
    } // characters(XMLString,Augmentations)

    /** Ignorable whitespace. */
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
        characters(text, augs);
    } // ignorableWhitespace(XMLString,Augmentations)

    //
    // Protected methods
    //

    /** Prints collected characters. */
    protected void chars() {
        if (fStringBuffer.length == 0) {
            return;
        }
        out.print('"');
        print(fStringBuffer.toString());
        out.println();
        out.flush();
        fStringBuffer.clear();
    } // chars()

    /** Prints the specified string. */
    protected void print(String s) {
        int length = s != null ? s.length() : 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': {
                    out.print("\\n");
                    break;
                }
                case '\r': {
                    out.print("\\r");
                    break;
                }
                case '\t': {
                    out.print("\\t");
                    break;
                }
                case '\\': {
                    out.print("\\\\");
                    break;
                }
                default: {
                    out.print(c);
                }
            }
        }
    } // print(String)

    //
    // Protected static methods
    //

    /** Sorts the attribute names. */
    protected static void sortAttrNames(XMLAttributes attrs, 
                                        String[] anames, String[] auris) {
        for (int i = 0; i < anames.length; i++) {
            anames[i] = attrs.getQName(i);
            auris[i] = attrs.getURI(i);
        }
        // NOTE: This is super inefficient but it doesn't really matter. -Ac
        for (int i = 0; i < anames.length - 1; i++) {
            int index = i;
            for (int j = i + 1; j < anames.length; j++) {
                if (anames[j].compareTo(anames[index]) < 0) {
                    index = j;
                }
            }
            if (index != i) {
                String tn = anames[i];
                anames[i] = anames[index];
                anames[index] = tn;
                String tu = auris[i];
                auris[i] = auris[index];
                auris[index] = tu;
            }
        }
    } // sortAttrNames(XMLAttributes,String[])

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String[] argv) throws Exception {
        org.apache.xerces.xni.parser.XMLDocumentFilter[] filters = {
            new Writer(),
        };
        org.apache.xerces.xni.parser.XMLParserConfiguration parser =
            new org.cyberneko.html.HTMLConfiguration();
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
        for (int i = 0; i < argv.length; i++) {
            org.apache.xerces.xni.parser.XMLInputSource source =
                new org.apache.xerces.xni.parser.XMLInputSource(null, argv[i], null);
            parser.parse(source);
        }
    } // main(String[])

} // class Writer
