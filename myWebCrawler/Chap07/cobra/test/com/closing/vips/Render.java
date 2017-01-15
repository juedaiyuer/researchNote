package com.closing.vips;

import java.io.*;
import org.lobobrowser.html.*;
import org.lobobrowser.html.gui.*;
import org.lobobrowser.html.parser.*;
import org.lobobrowser.html.test.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.lobobrowser.html.domimpl.HTMLElementImpl;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Render - This object is a wrapper for the Cobra Toolkit, which is part of the
 * Lobo Project (http://html.xamjwg.org/index.jsp). Cobra is a
 * "pure Java HTML renderer and DOM parser."
 * <p>
 * Render opens a URL, uses Cobra to render that HTML and apply JavaScript. It
 * then does a simple tree traversal of the DOM to print beginning and end tag
 * names.
 * <p>
 * Subclass this class and override the <i>doElement(org.w3c.dom.Element
 * element)</i> and <i>doTagEnd(org.w3c.dom.Element element)</i> methods to do
 * some real work. In the base class, doElement() prints the tag name and
 * doTagEnd() prints a closing version of the tag.
 * <p>
 * This class is a rewrite of org.benjysbrain.htmlgrab.Render that uses JRex.
 * <p>
 * Copyright (c) 2008 by Ben E. Cline. This code is presented as a teaching aid.
 * No warranty is expressed or implied.
 * <p>
 * http://www.benjysbrain.com/
 * 
 * @author Benjy Cline
 * @version 1/2008
 */

public class Render {
	String url; // The page to be processed.

	// These variables can be used in subclasses and are created from
	// url. baseURL can be used to construct the absolute URL of the
	// relative URL's in the page. hostBase is just the http://host.com/
	// part of the URL and can be used to construct the full URL of
	// URLs in the page that are site relative, e.g., "/xyzzy.jpg".
	// Variable host is set to the host part of url, e.g., host.com.

	String baseURL;
	String hostBase;
	String host;

	/**
	 * Create a Render object with a target URL.
	 */

	public Render(String url) {
		this.url = url;
	}

	/**
	 * Load the given URL using Cobra. When the page is loaded, recurse on the
	 * DOM and call doElement()/doTagEnd() for each Element node. Return false
	 * on error.
	 * @throws Exception 
	 */

	public boolean parsePage() throws Exception  {
		// From Lobo forum. Disable all logging.

		Logger.getLogger("").setLevel(Level.OFF);

		// Parse the URL and build baseURL and hostURL for use by doElement()
		// and doTagEnd() ;

		URI uri = new URI(url);
		
		String path = uri.getPath();

		host = uri.getHost();
		String port = "";
		if (uri.getPort() != -1)
			port = Integer.toString(uri.getPort());
		if (!port.equals(""))
			port = ":" + port;

		baseURL = "http://" + uri.getHost() + port + path;
		hostBase = "http://" + uri.getHost() + port;

		// Open a connection to the HTML page and use Cobra to parse it.
		// Cobra does not return until page is loaded.
		try {
			URL urlObj = new URL(url);			
			URLConnection connection = urlObj.openConnection();
			InputStream in = connection.getInputStream();

			UserAgentContext context = new SimpleUserAgentContext();
			DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
			Document document = dbi.parse(new InputSourceImpl(in, url,
					"UTF-8"));

			Element ex = document.getDocumentElement();
			doTree(ex);//从根节点递归遍历DOM树
		} catch (Exception e) {
			System.out.println("parsePage(" + url + "):  " + e);
			return false;
		}

		return true;
	}

	/**
	 * Recurse the DOM starting with Node node. For each Node of type Element,
	 * call doElement() with it and recurse over its children. The Elements
	 * refer to the HTML tags, and the children are tags contained inside the
	 * parent tag.
	 */

	public void doTree(Node node) {
		if (node instanceof Element) {//标签
			Element element = (Element) node;

			//访问标签
			doElement(element);

			//访问这个标签下所有的孩子，也就是这个标签所包括的标签 
			NodeList nl = element.getChildNodes();
			if (nl == null)
				return;
			int num = nl.getLength();
			for (int i = 0; i < num; i++)
				doTree(nl.item(i));

			//标签结束
			doTagEnd(element);
		}
		else //非标签节点，例如文本
		{
			System.out.println(node.getNodeName()+":"+node.getNodeValue());
		}
	}

	/**
	 * Simple doElement to print the tag name of the Element. Override to do
	 * something real.
	 */

	public void doElement(Element element) {
		System.out.println("<" + element.getTagName() + ">"+element.toString());
	}

	/**
	 * Simple doTagEnd() to print the closing tag of the Element. Override to do
	 * something real.
	 */

	public void doTagEnd(Element element) {
		System.out.println("</" + element.getTagName() + ">");
	}

	/**
	 * Main: java com.benjysbrain.cobra.Render [url]. Open Render on www.cnn.com
	 * by default. Parse the page and print the beginning and end tags.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String url = "http://www.lietu.com/";
		if (args.length == 1)
			url = args[0];
		Render p = new Render(url);
		p.parsePage();
		System.exit(0);
	}
}
