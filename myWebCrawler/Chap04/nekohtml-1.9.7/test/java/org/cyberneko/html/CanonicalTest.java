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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.xercesbridge.XercesBridge;

/**
 * This test generates canonical result using the <code>Writer</code> class
 * and compares it against the expected canonical output. Simple as that.
 *
 * @author Andy Clark
 * @author Marc Guillemot
 * @author Ahmed Ashour
 */
public class CanonicalTest extends TestCase {

    private static final File canonicalDir = new File("data/canonical");
    private static final File outputDir = new File("build/data/output/" + XercesBridge.getInstance().getVersion());
    private File dataFile;
    
    public static Test suite() throws Exception {
    	outputDir.mkdirs();

    	TestSuite suite = new TestSuite();
        File dataDir = new File("data");
        File[] dataFiles = dataDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.startsWith("test") && name.endsWith(".html");
            }
        });

        for (int i=0; i < dataFiles.length; i++) {
            suite.addTest(new CanonicalTest(dataFiles[i]));
        }
        return suite;
    }

    CanonicalTest(final File dataFile) throws Exception {
        super(dataFile.getName() + " [" + XercesBridge.getInstance().getVersion() + "]");
        this.dataFile = dataFile;
    }
    
    
    protected void runTest() throws Exception {
        List/*String*/ dataLines = getResult(dataFile);
        try
        {
        	final File canonicalFile = new File(canonicalDir, dataFile.getName());
        	if (!canonicalFile.exists()) {
        		fail("Canonical file not found: " + canonicalFile.getAbsolutePath());
        	}
            List/*String*/ canonicalLines = getCanonical(canonicalFile);
	    	assertEquals("file length", canonicalLines.size(), dataLines.size());
	
	    	for (int l=0; l < dataLines.size(); l++) {
	        	assertEquals("line " + (l + 1), canonicalLines.get(l), dataLines.get(l));
	        }
        }
        catch (final AssertionFailedError e)
        {
        	final File output = new File(outputDir, dataFile.getName());
        	final PrintWriter pw = new PrintWriter(new FileOutputStream(output));
        	for (final Iterator iter=dataLines.iterator(); iter.hasNext(); )
        	{
        		pw.println(iter.next());
        	}
        	pw.close();
        	
        	throw e;
        }
    }

    private List/*String*/ getCanonical(File infile) throws IOException {
        List/*String*/ lines = new ArrayList/*String*/();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new UTF8BOMSkipper(new FileInputStream(infile)), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }

    private List/*String*/ getResult(final File infile) throws IOException {
        List/*String*/ lines = new ArrayList/*String*/();
        StringWriter out = new StringWriter();
        try {
            // create filters
            XMLDocumentFilter[] filters = { new Writer(out) };
            
            // create parser
            XMLParserConfiguration parser = new HTMLConfiguration();

            // parser settings
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
            String infilename = infile.toString();
            File insettings = new File(infilename+".settings");
            if (insettings.exists()) {
                BufferedReader settings = new BufferedReader(new FileReader(insettings));
                String settingline;
                while ((settingline = settings.readLine()) != null) {
                    StringTokenizer tokenizer = new StringTokenizer(settingline);
                    String type = tokenizer.nextToken();
                    String id = tokenizer.nextToken();
                    String value = tokenizer.nextToken();
                    if (type.equals("feature")) {
                        parser.setFeature(id, value.equals("true"));
                    }
                    else {
                        parser.setProperty(id, value);
                    }
                }
                settings.close();
            }

            // parse
            parser.parse(new XMLInputSource(null, infilename, null));
        }
        finally {
            out.close();
        }
        BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
}
