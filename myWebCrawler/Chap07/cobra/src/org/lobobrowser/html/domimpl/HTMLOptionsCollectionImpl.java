package org.lobobrowser.html.domimpl;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.html2.HTMLOptionsCollection;

public class HTMLOptionsCollectionImpl extends DescendentHTMLCollection implements HTMLOptionsCollection {
	private static final NodeFilter OPTION_FILTER = new OptionFilter();
	
	public HTMLOptionsCollectionImpl(HTMLElementImpl selectElement) {
		super(selectElement, OPTION_FILTER);
	}
	
	public void setLength(int length) throws DOMException {
		//TODO: ???
		throw new UnsupportedOperationException();
	}

	private static class OptionFilter implements NodeFilter {
		public boolean accept(Node node) {
			return "option".equalsIgnoreCase(node.getNodeName());
		}
	}
}
