package org.cyberneko.html.xercesbridge;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * This class allows to transparently handle Xerces methods that have changed among versions. 
 * @author Marc Guillemot
 */
public abstract class XercesBridge 
{

	static private final XercesBridge instance = makeInstance();

	/**
	 * The access point for the bridge. 
	 * @return the instance corresponding to the Xerces version being currently used.
	 */
	public static XercesBridge getInstance()
	{
		return instance;
	}
	
    private static XercesBridge makeInstance()
    {
        final String[] classNames = {
            "org.cyberneko.html.xercesbridge.XercesBridge_2_3",
            "org.cyberneko.html.xercesbridge.XercesBridge_2_2",
            "org.cyberneko.html.xercesbridge.XercesBridge_2_1",
            "org.cyberneko.html.xercesbridge.XercesBridge_2_0"
        };

        for (int i = 0; i != classNames.length; ++i) {
            final String className = classNames[i];
        	XercesBridge bridge = (XercesBridge) newInstanceOrNull(className);
            if (bridge != null) {
                return bridge;
            }
        }
        throw new IllegalStateException("Failed to create XercesBridge instance");
    }

	private static XercesBridge newInstanceOrNull(final String className) {
		try {
			return (XercesBridge) Class.forName(className).newInstance();
	    } 
		catch (ClassNotFoundException ex) { }
		catch (SecurityException ex) { } 
		catch (LinkageError ex) { } 
		catch (IllegalArgumentException e) { }
		catch (IllegalAccessException e) { }
		catch (InstantiationException e) { }
		
		return null;
	}
	/**
     * Default implementation does nothing
     * @param namespaceContext 
     * @param ns
     * @param avalue
     */
	public void NamespaceContext_declarePrefix(NamespaceContext namespaceContext, String ns, String avalue) {
		// nothing
	}
	
	/**
	 * Gets the Xerces version used
	 * @return the version
	 */
	public abstract String getVersion();

	/**
	 * Calls startDocument on the {@link XMLDocumentHandler}. 
	 */
	public abstract void XMLDocumentHandler_startDocument(XMLDocumentHandler documentHandler, XMLLocator locator,
			String encoding, NamespaceContext nscontext, Augmentations augs);
	
	/**
	 * Calls startPrefixMapping on the {@link XMLDocumentHandler}. 
	 */
	public void XMLDocumentHandler_startPrefixMapping(
			XMLDocumentHandler documentHandler, String prefix, String uri,
			Augmentations augs) {
		// default does nothing
	}

	/**
	 * Calls endPrefixMapping on the {@link XMLDocumentHandler}. 
	 */
	public void XMLDocumentHandler_endPrefixMapping(
			XMLDocumentHandler documentHandler, String prefix,
			Augmentations augs) {
		// default does nothing
	}

	/**
	 * Calls setDocumentSource (if available in the Xerces version used) on the {@link XMLDocumentFilter}.
	 * This implementation does nothing.
	 */
	public void XMLDocumentFilter_setDocumentSource(XMLDocumentFilter filter,
			XMLDocumentSource lastSource)
	{
		// nothing, it didn't exist on old Xerces versions
	}
}
