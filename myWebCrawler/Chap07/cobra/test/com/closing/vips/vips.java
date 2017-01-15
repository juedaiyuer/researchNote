package com.closing.vips;

import java.awt.EventQueue;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Minimal rendering example: google.com.
 */
public class vips {
	public static void main(String[] args) throws Exception {
		String uri = "http://www.google.com/relief/chileearthquake/";
		URL url = new URL(uri);
		URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();

		// A Reader should be created with the correct charset,
		// which may be obtained from the Content-Type header
		// of an HTTP response.
		Reader reader = new InputStreamReader(in);

		// InputSourceImpl constructor with URI recommended
		// so the renderer can resolve page component URLs.
		InputSource is = new InputSourceImpl(reader, uri);
		HtmlPanel htmlPanel = new HtmlPanel();
		HtmlRendererContext rendererContext = new LocalHtmlRendererContext(htmlPanel);
		
		// Set a preferred width for the HtmlPanel,
		// which will allow getPreferredSize() to
		// be calculated according to block content.
		// We do this here to illustrate the 
		// feature, but is generally not
		// recommended for performance reasons.
		htmlPanel.setPreferredWidth(800);
		
		// This example does not perform incremental
		// rendering. 
		HTMLDocumentImpl builder = new HTMLDocumentImpl(rendererContext);
		System.out.println(builder.getAlignmentX() + "\t" + builder.getAlignmentY() + "\t" + builder.getBody().getNodeName());
	}

	private static class LocalHtmlRendererContext extends SimpleHtmlRendererContext {
		// Override methods here to implement browser functionality
		public LocalHtmlRendererContext(HtmlPanel contextComponent) {
			super(contextComponent);
		}
	}
}
