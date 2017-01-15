/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The Lobo Project

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Contact info: xamjadmin@users.sourceforge.net
*/
/*
 * Created on Nov 19, 2005
 */
package org.lobobrowser.html.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.io.*;
import javax.swing.*;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.html.parser.*;
import org.lobobrowser.html.renderer.*;
import org.lobobrowser.util.EventDispatch2;
import org.lobobrowser.util.gui.WrapperLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.w3c.dom.html2.*;

/**
 * The <code>HtmlPanel</code> class is a Swing
 * component that can render a HTML DOM. 
 * @author J. H. S.
 */
public class HtmlPanel extends JComponent implements FrameContext {
	private final EventDispatch2 selectionDispatch = new SelectionDispatch();
	private final javax.swing.Timer notificationTimer;
	private final DocumentNotificationListener notificationListener;
	private static final int NOTIF_TIMER_DELAY = 300;

	private volatile boolean isFrameSet = false;
	private volatile NodeRenderer nodeRenderer = null;
	private volatile NodeImpl rootNode;
	private volatile HtmlBlockPanel htmlBlock;
	private volatile FrameSetPanel frameSetPanel;
	private volatile int preferredWidth = -1;
	
	/**
	 * Constructs an <code>HtmlPanel</code>.
	 */
	public HtmlPanel() {
		super();
		this.setLayout(WrapperLayout.getInstance());
		this.setOpaque(false);
		this.notificationTimer = new javax.swing.Timer(NOTIF_TIMER_DELAY, new NotificationTimerAction());
		this.notificationTimer.setRepeats(false);
		this.notificationListener = new LocalDocumentNotificationListener();
	}

	/**
	 * Sets a preferred width that serves as a hint in calculating
	 * the preferred size of the <code>HtmlPanel</code>. Note that
	 * the preferred size can only be calculated when a document is
	 * available, and it will vary during incremental rendering. 
	 * <p>
	 * This method currently does not have any effect when the
	 * document is a FRAMESET.
	 * <p>
	 * Note also that setting the preferred width to a value other
	 * than <code>-1</code> (the default) will have an effect in renderer performance.	 
	 *  
	 * @param width The preferred width, or <code>-1</code> to unset.
	 */
	public void setPreferredWidth(int width) {
		this.preferredWidth = width;
		HtmlBlockPanel htmlBlock = this.htmlBlock;
		if(htmlBlock != null) {
			htmlBlock.setPreferredWidth(width);
		}
	}
	
	/**
	 * Gets the root <code>Renderable</code> of
	 * the HTML block. It returns <code>null</code>
	 * for FRAMESETs.
	 */
	public BoundableRenderable getBlockRenderable() {
		HtmlBlockPanel htmlBlock = this.htmlBlock;
		return htmlBlock == null ? null : htmlBlock.getRootRenderable();
	}
	
	/**
	 * Gets an instance of {@link FrameSetPanel} in case
	 * the currently rendered page is a FRAMESET.
	 * <p>
	 * Note: This method should be invoked in the GUI thread.
	 * @return A <code>FrameSetPanel</code> instance or <code>null</code>
	 * if the document currently rendered is not a FRAMESET.
	 */
	public FrameSetPanel getFrameSetPanel() {
		int componentCount = this.getComponentCount();
		if(componentCount == 0) {
			return null;
		}
		Object c = this.getComponent(0);
		if(c instanceof FrameSetPanel) {
			return (FrameSetPanel) c;
		}
		return null;
	}
	
	private void setUpAsBlock(UserAgentContext ucontext, HtmlRendererContext rcontext) {
		HtmlBlockPanel shp = this.createHtmlBlockPanel(ucontext, rcontext);
		shp.setPreferredWidth(this.preferredWidth);
		this.htmlBlock = shp;
		this.frameSetPanel = null;
		shp.setDefaultPaddingInsets(new Insets(8, 8, 8, 8));
		this.removeAll();
		this.add(shp);
		this.nodeRenderer = shp;
	}

	private void setUpFrameSet(NodeImpl fsrn) {
		this.isFrameSet = true;
		this.htmlBlock = null;
		FrameSetPanel fsp = this.createFrameSetPanel();
		this.frameSetPanel = fsp;
		this.nodeRenderer = fsp;
		this.removeAll();
		this.add(fsp);
		fsp.setRootNode(fsrn);
	}
	
	/**
	 * Method invoked internally to create a {@link HtmlBlockPanel}.
	 * It is made available so it can be overridden.
	 */
	protected HtmlBlockPanel createHtmlBlockPanel(UserAgentContext ucontext, HtmlRendererContext rcontext) {
		return new HtmlBlockPanel(0, java.awt.Color.WHITE, true, ucontext, rcontext, this);		
	}

	/**
	 * Method invoked internally to create a {@link FrameSetPanel}.
	 * It is made available so it can be overridden.
	 */
	protected FrameSetPanel createFrameSetPanel() {
		return new FrameSetPanel();
	}
	
	/**
	 * Scrolls the document such that x and y coordinates
	 * are placed in the upper-left corner of the panel.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public void scroll(int x, int y) {
		//TODO
	}

	/**
	 * Clears the current document if any.
	 * If called outside the GUI thread, the operation
	 * will be scheduled to be performed in the GUI
	 * thread.
	 */
	public void clearDocument() {
		if(java.awt.EventQueue.isDispatchThread()) {
			this.clearDocumentImpl();
		}
		else {
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					HtmlPanel.this.clearDocumentImpl();
				}
			});
		}		
	}
	
	private void clearDocumentImpl() {
		HTMLDocumentImpl prevDocument = (HTMLDocumentImpl) this.rootNode;
		if(prevDocument != null) {
			prevDocument.removeDocumentNotificationListener(this.notificationListener);
		}
		NodeRenderer nr = this.nodeRenderer;
		if(nr != null) {
			nr.setRootNode(null);
		}
		this.rootNode = null;
		this.htmlBlock = null;
		this.nodeRenderer = null;
		this.isFrameSet = false;
		this.removeAll();
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * Sets an HTML DOM node and invalidates the component so it is
	 * rendered immediately in the GUI thread.
	 * @param node This should
	 * normally be a Document instance obtained with
	 * {@link org.lobobrowser.html.parser.DocumentBuilderImpl}.
	 * <p>
	 * Note: It is safe to call this method outside of the GUI thread.
	 * @param rcontext A renderer context.
	 * @param pcontext A parser context.
	 * @deprecated HtmlParserContext is no longer used here.
	 */
	public void setDocument(final Document node, final HtmlRendererContext rcontext, final HtmlParserContext pcontext) {
		this.setDocument(node, rcontext);
	}
	
	/**
	 * Sets an HTML DOM node and invalidates the component so it is
	 * rendered as soon as possible in the GUI thread. 
	 * <p>
	 * If this method is called from a thread that is not the GUI
	 * dispatch thread, the document is scheduled to be set later.
	 * Note that {@link #setPreferredWidth(int) preferred size}
	 * calculations should be done in the GUI dispatch thread for
	 * this reason.
	 * @param node This should
	 * normally be a Document instance obtained with
	 * {@link org.lobobrowser.html.parser.DocumentBuilderImpl}.
	 * <p>
	 * @param rcontext A renderer context.
	 */
	public void setDocument(final Document node, final HtmlRendererContext rcontext) {
		if(java.awt.EventQueue.isDispatchThread()) {
			this.setDocumentImpl(node, rcontext);
		}
		else {
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					HtmlPanel.this.setDocumentImpl(node, rcontext);					
				}
			});
		}
	}

	private void setDocumentImpl(Document node, HtmlRendererContext rcontext) {
		// Expected to be called in the GUI thread.
		if(!(node instanceof HTMLDocumentImpl)) {
			throw new IllegalArgumentException("Only nodes of type HTMLDocumentImpl are currently supported. Use DocumentBuilderImpl.");
		}		
		HTMLDocumentImpl prevDocument = (HTMLDocumentImpl) this.rootNode;
		if(prevDocument != null) {
			prevDocument.removeDocumentNotificationListener(this.notificationListener);
		}
		HTMLDocumentImpl nodeImpl = (HTMLDocumentImpl) node;
		nodeImpl.addDocumentNotificationListener(this.notificationListener);
		this.rootNode = nodeImpl;
		NodeImpl fsrn = this.getFrameSetRootNode(nodeImpl);
		boolean newIfs = fsrn != null;
		if(newIfs != this.isFrameSet || this.getComponentCount() == 0) {
			this.isFrameSet = newIfs;
			if(newIfs) {
				this.setUpFrameSet(fsrn);
			}
			else {
				this.setUpAsBlock(rcontext.getUserAgentContext(), rcontext);
			}		
		}		
		NodeRenderer nr = this.nodeRenderer;
		if(nr != null) {
			// These subcomponents should take care
			// of revalidation.
			if(newIfs) {
				nr.setRootNode(fsrn);
			}
			else {
				nr.setRootNode(nodeImpl);
			}
		}
		else {
			this.invalidate();
			this.validate();
			this.repaint();
		}
	}

	/**
	 * Renders HTML given as a string.
	 */
	public void setHtml(String htmlSource, String uri, HtmlRendererContext rcontext) {
		try {
			DocumentBuilderImpl builder = new DocumentBuilderImpl(rcontext.getUserAgentContext(), rcontext);
			Reader reader = new StringReader(htmlSource);
			try {
				InputSourceImpl is = new InputSourceImpl(reader, uri);
				Document document = builder.parse(is);
				this.setDocument(document, rcontext);
			} finally {
				reader.close();
			}
		} catch(java.io.IOException ioe) {
			throw new IllegalStateException("Unexpected condition.", ioe);
		} catch(org.xml.sax.SAXException se) {
			throw new IllegalStateException("Unexpected condition.", se);			
		}
	}
	
	/**
	 * Gets the HTML DOM node currently rendered if any.
	 */
	public NodeImpl getRootNode() {
		return this.rootNode;
	}

	private boolean resetIfFrameSet() {
		NodeImpl nodeImpl = this.rootNode;
		NodeImpl fsrn = this.getFrameSetRootNode(nodeImpl);
		boolean newIfs = fsrn != null;
		if(newIfs != this.isFrameSet || this.getComponentCount() == 0) {
			this.isFrameSet = newIfs;
			if(newIfs) {
				this.setUpFrameSet(fsrn);
				NodeRenderer nr = this.nodeRenderer;
				nr.setRootNode(fsrn);
				// Set proper bounds and repaint.
				this.validate();
				this.repaint();
				return true;
			}
		}		
		return false;
	}
	
	private NodeImpl getFrameSetRootNode(NodeImpl node) {
		if(node instanceof Document) {
			ElementImpl element = (ElementImpl) ((Document) node).getDocumentElement();
			if(element != null && "HTML".equalsIgnoreCase(element.getTagName())) {
				return this.getFrameSet(element);
			}
			else {
				return this.getFrameSet(node);
			}
		}
		else {
			return null;
		}
	}
	
	private NodeImpl getFrameSet(NodeImpl node) {
		NodeImpl[] children = node.getChildrenArray();
		if(children == null) {
			return null;
		}
		int length = children.length;
		NodeImpl frameSet = null;
		for(int i = 0; i < length; i++) {
			NodeImpl child = children[i];
			if(child instanceof Text) {
				// Ignore
			}
			else if(child instanceof ElementImpl) {
				String tagName = child.getNodeName();
				if("HEAD".equalsIgnoreCase(tagName) ||
				   "NOFRAMES".equalsIgnoreCase(tagName) ||
				   "TITLE".equalsIgnoreCase(tagName) || 
				   "META".equalsIgnoreCase(tagName) ||
				   "SCRIPT".equalsIgnoreCase(tagName) ||
				   "NOSCRIPT".equalsIgnoreCase(tagName)) {
					// ignore it
				}
				else if("FRAMESET".equalsIgnoreCase(tagName)) {
					frameSet = child;
					break;
				}
				else {
					if(this.hasSomeHtml((ElementImpl) child)) {
						return null;
					}
				}
			}
		}
		return frameSet;
	}
	
	private boolean hasSomeHtml(ElementImpl element) {
		String tagName = element.getTagName();
		if("HEAD".equalsIgnoreCase(tagName) || "TITLE".equalsIgnoreCase(tagName) || "META".equalsIgnoreCase(tagName)) {
			return false;
		}
		NodeImpl[] children = element.getChildrenArray();
		if(children != null) {
			int length = children.length;
			for(int i = 0; i < length; i++) {
				NodeImpl child = children[i];
				if(child instanceof Text) {
					String textContent = ((Text) child).getTextContent();
					if(textContent != null && !"".equals(textContent.trim())) {
						return false;
					}
				}
				else if(child instanceof ElementImpl) {
					if(this.hasSomeHtml((ElementImpl) child)) {
						return false;
					}
				}
			}
		}
		return true;		
	}

	/**
	 * Internal method used to expand the selection to the given point.
	 * <p>
	 * Note: This method should be invoked in the GUI thread.
	 */
	public void expandSelection(RenderableSpot rpoint) {
		HtmlBlockPanel block = this.htmlBlock;
		if(block != null) {
			block.setSelectionEnd(rpoint);
			block.repaint();
			this.selectionDispatch.fireEvent(new SelectionChangeEvent(this, block.isSelectionAvailable()));
		}
	}

	/**
	 * Internal method used to reset the selection so that
	 * it is empty at the given point. This is what is called
	 * when the user clicks on a point in the document.
	 * <p>
	 * Note: This method should be invoked in the GUI thread.
	 */
	public void resetSelection(RenderableSpot rpoint) {
		HtmlBlockPanel block = this.htmlBlock;
		if(block != null) {
			block.setSelectionStart(rpoint);
			block.setSelectionEnd(rpoint);
			block.repaint();
		}
		this.selectionDispatch.fireEvent(new SelectionChangeEvent(this, false));
	}
	
	/**
	 * Gets the selection text. 
	 * <p>
	 * Note: This method should be invoked in the GUI thread.
	 */
	public String getSelectionText() {
		HtmlBlockPanel block = this.htmlBlock;
		if(block == null) {
			return null;
		}
		else {
			return block.getSelectionText();
		}
	}
	
	/**
	 * Gets a DOM node enclosing the selection. The node returned should
	 * be the inner-most node that encloses both selection start and end
	 * points. Note that the selection end point may be just outside of
	 * the selection.
	 * <p>
	 * Note: This method should be invoked in the GUI thread.
	 */
	public org.w3c.dom.Node getSelectionNode() {
		HtmlBlockPanel block = this.htmlBlock;
		if(block == null) {
			return null;
		}
		else {
			return block.getSelectionNode();
		}		
	}

	/**
	 * Returns true only if the current block has a selection.
	 * This method has no effect in FRAMESETs at the moment.
	 */
	public boolean hasSelection() {
		HtmlBlockPanel block = this.htmlBlock;
		if(block == null) {
			return false;
		}
		else {
			return block.hasSelection();
		}				
	}

	/**
	 * Copies the current selection, if any, into the clipboard.
	 * This method has no effect in FRAMESETs at the moment.
	 */
	public boolean copy() {
		HtmlBlockPanel block = this.htmlBlock;
		if(block != null) {
			return block.copy();
		}			
		else {
			return false;
		}
	}

	/**
	 * Adds listener of selection changes. Note that it does 
	 * not have any effect on FRAMESETs.
	 * @param listener An instance of {@link SelectionChangeListener}.
	 */
	public void addSelectionChangeListener(SelectionChangeListener listener) {
		this.selectionDispatch.addListener(listener);
	}

	/**
	 * Removes a listener of selection changes that was
	 * previously added.
	 */
	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		this.selectionDispatch.removeListener(listener);
	}
	
	private ArrayList notifications = new ArrayList(1);
	
	private void addNotification(DocumentNotification notification) {
		// This can be called in a random thread.
		ArrayList notifs = this.notifications;
		synchronized(notifs) {
			notifs.add(notification);
		}
		if(EventQueue.isDispatchThread()) {
			// Process imediately
			this.processNotifications();
		}
		else {
			this.notificationTimer.restart();
		}
	}
	
	/**
	 * Invalidates the layout of the given node and schedules it
	 * to be layed out later. Multiple invalidations may be 
	 * processed in a single document layout.
	 */
	public void delayedRelayout(NodeImpl node) {
		ArrayList notifs = this.notifications;
		synchronized(notifs) {
			notifs.add(new DocumentNotification(DocumentNotification.SIZE, node));
		}
		this.notificationTimer.restart();
	}

	private void processNotifications() {
		// This is called in the GUI thread.
		ArrayList notifs = this.notifications;
		DocumentNotification[] notifsArray;
		synchronized(notifs) {
			int size = notifs.size();
			if(size == 0) {
				return;
			}
			notifsArray = new DocumentNotification[size];
			notifsArray = (DocumentNotification[]) notifs.toArray(notifsArray);
			notifs.clear();
		}
		int length = notifsArray.length;
		for(int i = 0; i < length; i++) {
			DocumentNotification dn = notifsArray[i];
			if(dn.node instanceof HTMLFrameSetElement && this.htmlBlock != null) {
				if(this.resetIfFrameSet()) {
					// Revalidation already taken care of.
					return;
				}
			}
		}
		HtmlBlockPanel blockPanel = this.htmlBlock;
		if(blockPanel != null) {
			blockPanel.processDocumentNotifications(notifsArray);
		}
		FrameSetPanel frameSetPanel = this.frameSetPanel;
		if(frameSetPanel != null) {
			frameSetPanel.processDocumentNotifications(notifsArray);
		}
	}
	
	private class SelectionDispatch extends EventDispatch2 {
		/* (non-Javadoc)
		 * @see org.xamjwg.util.EventDispatch2#dispatchEvent(java.util.EventListener, java.util.EventObject)
		 */
		protected void dispatchEvent(EventListener listener, EventObject event) {
			((SelectionChangeListener) listener).selectionChanged((SelectionChangeEvent) event);
		}
	}
	
	private class LocalDocumentNotificationListener implements DocumentNotificationListener {
		public void allInvalidated() {
			HtmlPanel.this.addNotification(new DocumentNotification(DocumentNotification.GENERIC, null));
		}

		public void invalidated(NodeImpl node) {
			HtmlPanel.this.addNotification(new DocumentNotification(DocumentNotification.GENERIC, node));
		}

		public void lookInvalidated(NodeImpl node) {
			HtmlPanel.this.addNotification(new DocumentNotification(DocumentNotification.LOOK, node));
		}

		public void positionInvalidated(NodeImpl node) {
			HtmlPanel.this.addNotification(new DocumentNotification(DocumentNotification.POSITION, node));
		}

		public void sizeInvalidated(NodeImpl node) {
			HtmlPanel.this.addNotification(new DocumentNotification(DocumentNotification.SIZE, node));
		}

		public void externalScriptLoading(NodeImpl node) {
			// Ignorable here.
		}

		public void nodeLoaded(NodeImpl node) {
			HtmlPanel.this.addNotification(new DocumentNotification(DocumentNotification.GENERIC, node));
		}
	}
	
	private class NotificationTimerAction implements java.awt.event.ActionListener {
		public void actionPerformed(ActionEvent e) {
			HtmlPanel.this.processNotifications();
		}	
	}
}
