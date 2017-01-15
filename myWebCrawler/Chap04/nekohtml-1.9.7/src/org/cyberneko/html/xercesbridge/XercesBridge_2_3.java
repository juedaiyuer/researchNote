package org.cyberneko.html.xercesbridge;

import org.apache.xerces.xni.NamespaceContext;

/**
 * Xerces bridge for use with Xerces 2.3 and higher
 * @author Marc Guillemot
 */
public class XercesBridge_2_3 extends XercesBridge_2_2
{
	/**
	 * Should fail for Xerces version less than 2.3 
	 * @throws InstantiationException if instantiation failed 
	 */
	public XercesBridge_2_3() throws InstantiationException {
        try {
        	final Class[] args = {String.class, String.class};
        	NamespaceContext.class.getMethod("declarePrefix", args);
        }
        catch (final NoSuchMethodException e) {
            // means that we're not using Xerces 2.3 or higher
            throw new InstantiationException(e.getMessage());
        }
	}

	public void NamespaceContext_declarePrefix(final NamespaceContext namespaceContext, 
			final String ns, String avalue) {
        namespaceContext.declarePrefix(ns, avalue);
	}
}
