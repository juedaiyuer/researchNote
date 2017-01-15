package org.cyberneko.html;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.html.filters.DefaultFilter;

/**
 * Unit tests for {@link HTMLScanner}.
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @version $Id: HTMLScanner.java,v 1.19 2005/06/14 05:52:37 andyc Exp $
 */
public class HTMLScannerTest extends TestCase {

	public void testisEncodingCompatible() throws Exception {
		final HTMLScanner scanner = new HTMLScanner();
		assertTrue(scanner.isEncodingCompatible("ISO-8859-1","ISO-8859-1"));
		assertTrue(scanner.isEncodingCompatible("UTF-8","UTF-8"));
		assertTrue(scanner.isEncodingCompatible("UTF-16","UTF-16"));
		assertTrue(scanner.isEncodingCompatible("US-ASCII","ISO-8859-1"));
		assertTrue(scanner.isEncodingCompatible("UTF-8","ISO-8859-1"));

		assertFalse(scanner.isEncodingCompatible("UTF-8","UTF-16"));
		assertFalse(scanner.isEncodingCompatible("ISO-8859-1","UTF-16"));
		assertFalse(scanner.isEncodingCompatible("UTF-16","Cp1252"));
	}

	public void testEvaluateInputSource() throws Exception {
	    String string = "<html><head><title>foo</title></head>"
	        + "<body>"
	        + "<script id='myscript'>"
	        + "  document.write('<style type=\"text/css\" id=\"myStyle\">');"
            + "  document.write('  .nwr {white-space: nowrap;}');"
            + "  document.write('</style>');"
            + "  document.write('<div id=\"myDiv\">');"
            + "  document.write('</div>');"
            + "</script>"
            + "<div><a/></div>"
            + "</body></html>";
        HTMLConfiguration parser = new HTMLConfiguration();
        EvaluateInputSourceFilter filter = new EvaluateInputSourceFilter(parser);
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {filter});
        XMLInputSource source = new XMLInputSource(null, "myTest", null, new StringReader(string), "UTF-8");
        parser.parse(source);
        
        String[] expectedString = {"(HTML", "(HEAD", "(TITLE", ")TITLE", ")HEAD", "(BODY", "(SCRIPT",
            ")SCRIPT", "~inserting", "(STYLE", "~inserting", "~inserting", ")STYLE", "~inserting",
            "(DIV", "~inserting", ")DIV", "(DIV", "(A", ")A", ")DIV", ")BODY", ")HTML"};
        assertEquals(Arrays.asList(expectedString), filter.collectedStrings);
    }

   private static class EvaluateInputSourceFilter extends DefaultFilter {

       private List collectedStrings = new ArrayList();
       private static int counter = 1;
       protected HTMLConfiguration fConfiguration;

       public EvaluateInputSourceFilter(HTMLConfiguration config) {
           fConfiguration = config;
       }

       public void startElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
           collectedStrings.add("(" + element.rawname);
       }

       public void endElement(QName element, Augmentations augs) throws XNIException {
           collectedStrings.add(")" + element.rawname);
           if (element.localpart.equals("SCRIPT")) {
        	   // act as if evaluation of document.write would insert the content
               insert("<style type=\"text/css\" id=\"myStyle\">");
               insert("  .nwr {white-space: nowrap;}");
               insert("</style>");
               insert("<div id=\"myDiv\">");
               insert("</div>");
           }
       }

        private void insert(final String string) {
            collectedStrings.add("~inserting");
            XMLInputSource source = new XMLInputSource(null, "myTest" + counter++, null,
                                                      new StringReader(string), "UTF-8");
            fConfiguration.evaluateInputSource(source);
       }

   }
}
