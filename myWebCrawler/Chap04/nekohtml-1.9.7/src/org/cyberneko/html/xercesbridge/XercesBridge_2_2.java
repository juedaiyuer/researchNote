package org.cyberneko.html.xercesbridge;

import org.apache.xerces.impl.Version;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * Xerces bridge for use with Xerces 2.2 and higher
 * @author Marc Guillemot
 */
public class XercesBridge_2_2 extends XercesBridge 
{
	/**
	 * Should fail for Xerces version less than 2.2 
	 * @throws InstantiationException if instantiation failed 
	 */
	protected XercesBridge_2_2() throws InstantiationException {
        try {
        	getVersion();
        } 
        catch (final Throwable e) {
            throw new InstantiationException(e.getMessage());
        }
	}

	public String getVersion() {
		return Version.getVersion();
	}
	
	public void XMLDocumentHandler_startPrefixMapping(
			XMLDocumentHandler documentHandler, String prefix, String uri,
			Augmentations augs) {
		// does nothing, not needed
	}

	public void XMLDocumentHandler_startDocument(XMLDocumentHandler documentHandler, XMLLocator locator,
			String encoding, NamespaceContext nscontext, Augmentations augs) {
		documentHandler.startDocument(locator, encoding, nscontext, augs);
     }

	public void XMLDocumentFilter_setDocumentSource(XMLDocumentFilter filter,
			XMLDocumentSource lastSource) {
		filter.setDocumentSource(lastSource);
	}
}
