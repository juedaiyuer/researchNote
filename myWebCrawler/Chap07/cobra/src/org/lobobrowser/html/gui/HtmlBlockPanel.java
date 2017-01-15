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
 * Created on Apr 16, 2005
 */
package org.lobobrowser.html.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

import javax.swing.*;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.*;
import org.lobobrowser.html.renderer.*;
import org.lobobrowser.util.*;
import org.lobobrowser.util.gui.ColorFactory;
import org.w3c.dom.*;
import java.util.logging.*;
import java.util.*;

/**
 * A Swing component that renders a HTML block, given 
 * by a DOM root or an internal element, typically a DIV.
 * This component cannot render FRAMESETs.
 * @see HtmlPanel
 * @see FrameSetPanel
 * @author J. H. S.
 */
public class HtmlBlockPanel extends JComponent implements NodeRenderer, RenderableContainer, ClipboardOwner {
	private static final Logger logger = Logger.getLogger(HtmlBlockPanel.class.getName());
	private static final boolean loggableInfo = logger.isLoggable(Level.INFO);
	protected final FrameContext frameContext;	
	protected final UserAgentContext ucontext;
	protected final HtmlRendererContext rcontext;
	
	protected RenderableSpot startSelection;
	protected RenderableSpot endSelection;
	protected RBlock rblock;
	protected int preferredWidth = -1;

	public HtmlBlockPanel(int listNesting, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext) {
		this(listNesting, ColorFactory.TRANSPARENT, false, pcontext, rcontext, frameContext);
	}
	
	public HtmlBlockPanel(int listNesting, Color background, boolean opaque, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext) {
		this.setLayout(null);
		this.setAutoscrolls(true);
		this.frameContext = frameContext;
		this.ucontext = pcontext;
		this.rcontext = rcontext;
		this.setOpaque(opaque);
		this.setBackground(background);
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if("copy".equals(command)) {
					copy();
				}
			}
		};
		this.registerKeyboardAction(actionListener, "copy", KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0), JComponent.WHEN_FOCUSED);		
		this.registerKeyboardAction(actionListener, "copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), JComponent.WHEN_FOCUSED);		
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				onMouseClick(e);
			}
			public void mouseEntered(MouseEvent e) { 
			}
			public void mouseExited(MouseEvent e) {	
				onMouseExited(e);
			}
			public void mousePressed(MouseEvent e) {
				onMousePressed(e);
			}
			public void mouseReleased(MouseEvent e) {
				onMouseReleased(e);
			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
			 */
			public void mouseDragged(MouseEvent e) {
				onMouseDragged(e);
			}

			/* (non-Javadoc)
			 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
			 */
			public void mouseMoved(MouseEvent arg0) {
				onMouseMoved(arg0);
			}
		});
		this.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				onMouseWheelMoved(e);
			}
		});
	}

	public BoundableRenderable getRootRenderable() {
		return this.rblock;
	}
	
	/**
	 * Allows {@link #getPreferredSize()} to render the HTML block
	 * in order to determine the preferred size of this component.
	 * Note that <code>getPreferredSize()<code> is a potentially time-consuming
	 * operation if the preferred width is set. 
	 * @param width The preferred blocked width. Use <code>-1</code> to unset.
	 */
	public void setPreferredWidth(int width) {
		this.preferredWidth = width;
	}

	/**
	 * If the preferred size has been set with {@link #setPreferredSize(Dimension)},
	 * then that size is returned. Otherwise a preferred size is calculated by
	 * rendering the HTML DOM, provided one is available and a preferred width other
	 * than <code>-1</code> has been set with {@link #setPreferredWidth(int)}.
	 * An arbitrary preferred size is returned in other scenarios.
	 */
	public Dimension getPreferredSize() {
		// Expected to be invoked in the GUI thread.
		if(this.isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		final int pw = this.preferredWidth;
		if(pw != -1) {
			final RBlock block = this.rblock;
			if(block != null) {
				// Potentially does layout outside of GUI thread.
				// Do not provide default overflow.
				block.layout(pw, 0, false, false, RBlock.OVERFLOW_NONE);
				// Adjust for permanent vertical scrollbar.
				int newPw = Math.max(block.width + block.getVScrollBarWidth(), pw);
				return new Dimension(newPw, block.height);
			}
		}
		return new Dimension(600, 400);
	}
	
	public void finalize() throws Throwable {
		super.finalize();
	}
	
	public boolean copy() {
		String selection = HtmlBlockPanel.this.getSelectionText();
		if(selection != null) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(selection), HtmlBlockPanel.this);
			return true;
		}		
		else {
			return false;
		}
	}
	
	private Insets defaultPaddingInsets = null;

	public void setDefaultPaddingInsets(Insets insets) {
		this.defaultPaddingInsets = insets;
	}
	
	public int getFirstLineHeight() {
		RBlock block = this.rblock;
		return block == null ? 0 : block.getFirstLineHeight();
	}

	public void setSelectionEnd(RenderableSpot rpoint) {
		this.endSelection = rpoint;
	}
	
	public void setSelectionStart(RenderableSpot rpoint) {
		this.startSelection = rpoint;
	}	
	
	public boolean isSelectionAvailable() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		return start != null && end != null && !start.equals(end);
	}
	
	public org.w3c.dom.Node getSelectionNode() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		if(start != null && end != null) {
			return Nodes.getCommonAncestor((Node) start.renderable.getModelNode(), (Node) end.renderable.getModelNode());
		}
		else {
			return null;
		}
	}
	
	/**
	 * Sets the root node to render. This method should
	 * be invoked in the GUI dispatch thread.
	 */
	public void setRootNode(NodeImpl node) {
		if(node != null) {
			RBlock block = new RBlock(node, 0, this.ucontext, this.rcontext, this.frameContext, this, RBlock.OVERFLOW_VERTICAL);
			block.setDefaultPaddingInsets(this.defaultPaddingInsets);
			node.setUINode(block);
			this.rblock = block;
		}
		else {
			this.rblock = null;
		}
		this.invalidate();
		this.validateAll();
		this.repaint();
	}
	
	protected void validateAll() {
		Component toValidate = this;
		for(;;) {
			Container parent = toValidate.getParent();
			if(parent == null || parent.isValid()) {
				break;
			}
			toValidate = parent;
		}
		toValidate.validate();
	}

	/**
	 * @deprecated setContainer is unused.
	 */
	public void setRootNode(NodeImpl node, boolean setContainer) {
		this.setRootNode(node);
	}

	protected void revalidatePanel() {
		// Called in the GUI thread.
		this.invalidate();
		this.validate();
		//TODO: Could be paintImmediately.
		this.repaint();
	}
	
	public NodeImpl getRootNode() {
		RBlock block = this.rblock;
		return block == null ? null : (NodeImpl) block.getModelNode();
	}
	
	private void onMouseClick(MouseEvent event) {
		// Rely on AWT mouse-click only for double-clicks
		RBlock block = this.rblock;
		if(block != null) {
			int button = event.getButton();
			int clickCount = event.getClickCount();
			if(button == MouseEvent.BUTTON1 && clickCount > 1) {
				//TODO: Double-click must be revised. It generates
				//a single click via mouse release.
				Point point = event.getPoint();
				block.onDoubleClick(event, point.x, point.y);
			}
			else if(button == MouseEvent.BUTTON3 && clickCount == 1) {
				block.onRightClick(event, event.getX(), event.getY());
			}
		}
	}
	
	private BoundableRenderable mousePressTarget;
	
	private void onMousePressed(MouseEvent event) {
		this.requestFocus();
		RBlock block = this.rblock;
		if(block != null) {
			Point point = event.getPoint();
			this.mousePressTarget = block;
			int rx = point.x;
			int ry = point.y;
			block.onMousePressed(event, point.x, point.y);
			RenderableSpot rp = block.getLowestRenderableSpot(rx, ry);
			if(rp != null) {
				this.frameContext.resetSelection(rp);
			}
			else {
				this.frameContext.resetSelection(null);
			}
			
//			BoundableRenderable r = block.getRenderable(point);
//			if(r != null) {
//				this.mousePressTarget = r;
//				Point rpoint = r.getRenderablePoint(point.x, point.y);
//				int rx = rpoint.x;
//				int ry = rpoint.y;
//				r.onMousePressed(event, rx, ry);
//				RenderableSpot rp = r.getLowestRenderableSpot(rx, ry);
//				if(rp != null) {
//					this.frameContext.resetSelection(rp);
//				}
//				else {
//					this.frameContext.resetSelection(null);
//				}
//			}
//			else {
//				this.frameContext.resetSelection(null);
//			}
		}
	}
	
	private void onMouseReleased(MouseEvent event) {
		RBlock block = this.rblock;
		if(block != null) {
			Point point = event.getPoint();
			int rx = point.x;
			int ry = point.y;
			if(event.getButton() == MouseEvent.BUTTON1) {
				// TODO: This will be raised twice on a double-click.
				block.onMouseClick(event, rx, ry);
			}
			block.onMouseReleased(event, rx, ry);
			
//			BoundableRenderable r = block.getRenderable(point);
//			if(r != null) {
//				Point rpoint = r.getRenderablePoint(point.x, point.y);
//				int rx = rpoint.x;
//				int ry = rpoint.y;
//				// First, generate a mouse-click event if it's BUTTON1.
//				if(event.getButton() == MouseEvent.BUTTON1) {
//					// TODO: This will be raised twice on a double-click.
//					r.onMouseClick(event, rx, ry);
//				}
//				// Now generate mouse-released event.
//				r.onMouseReleased(event, rx, ry);
//			}
			
			BoundableRenderable oldTarget = this.mousePressTarget;
			if(oldTarget != null) {
				this.mousePressTarget = null;
				if(oldTarget != block) {
					oldTarget.onMouseDisarmed(event);
				}
			}
		}
		else {
			this.mousePressTarget = null;
		}
	}
	
	private void onMouseExited(MouseEvent event) {
		BoundableRenderable oldTarget = this.mousePressTarget;
		if(oldTarget != null) {
			this.mousePressTarget = null;
			oldTarget.onMouseDisarmed(event);
		}
	}	
	
	private void onMouseWheelMoved(MouseWheelEvent mwe) {
		RBlock block = this.rblock;
		if(block != null) {
			switch(mwe.getScrollType()) {
			case MouseWheelEvent.WHEEL_UNIT_SCROLL:
				int units = mwe.getWheelRotation() * mwe.getScrollAmount();
				block.scrollByUnits(JScrollBar.VERTICAL, units);
				break;
			}
		}
	}
 
	private void onMouseDragged(MouseEvent event) {
		RBlock block = this.rblock;
		if(block != null) {
			Point point = event.getPoint();
			RenderableSpot rp = block.getLowestRenderableSpot(point.x, point.y);
			if(rp != null) {
				this.frameContext.expandSelection(rp);
			}
			block.ensureVisible(point);
		}
	}

	private void onMouseMoved(MouseEvent event) {
		RBlock block = this.rblock;
		if(block != null) {
			Point point = event.getPoint();
			block.onMouseMoved(event, point.x, point.y, false, null);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	//protected void paintComponent(Graphics g) {
	public void paint(Graphics g) {
		// We go against Sun's advice and override
		// paint() instead of paintComponent(). Scrollbars
		// do not repaint correctly if we use
		// paintComponent.
		if(this.isOpaque()) {
			// Background not painted by default in JComponent.
			Rectangle clipBounds = g.getClipBounds();
			g.setColor(this.getBackground());
			g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
		}
		if(g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		RBlock block = this.rblock;
		if(block != null) {
			boolean liflag = loggableInfo;
			long time1 = liflag ? System.currentTimeMillis() : 0;
			block.paint(g);
			if(liflag) {
				long time2 = System.currentTimeMillis();
				Node rootNode = this.getRootNode();
				String uri = rootNode instanceof Document ? ((Document) rootNode).getDocumentURI() : "";
				logger.info("paintComponent(): URI=[" + uri + "]. Block paint elapsed: " + (time2 - time1) + " ms.");
			}

			// Paint FrameContext selection

			RenderableSpot start = this.startSelection;
			RenderableSpot end = this.endSelection;
			if(start != null && end != null && !start.equals(end)) {
				block.paintSelection(g, false, start, end);
			}
		}
		
	}
		
	
	public void doLayout() {
		if(EventQueue.isDispatchThread()) {
			Dimension size = this.getSize();
			boolean liflag = loggableInfo;
			long time1 = 0;
			if(liflag) {
				time1 = System.currentTimeMillis();
			}
			this.removeAll();
			RBlock block = this.rblock;
			if(block != null) {
				ModelNode rootNode = block.getModelNode();
				block.layout(size.width, size.height, true, true);
				//Only set origin
				//this.rblock.setBounds(0, 0, size.width, size.height);
				block.setOrigin(0, 0);
				block.updateWidgetBounds(0, 0);
				if(liflag) {
					long time2 = System.currentTimeMillis();
					String uri = rootNode instanceof Document ? ((Document) rootNode).getDocumentURI() : "";
					logger.info("doLayout(): URI=[" + uri + "]. Block layout elapsed: " + (time2 - time1) + " ms. Component count: " + this.getComponentCount() + ".");
				}
			}
		}
		else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					HtmlBlockPanel.this.doLayout();
				}
			});
		}
	}
	
	/**
	 * Implementation of UINode.repaint().
	 */
	public void repaint(ModelNode modelNode) {
		//this.rblock.invalidateRenderStyle();
		this.repaint();
	}

	public String getSelectionText() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		if(start != null && end != null) {
			StringBuffer buffer = new StringBuffer();
			this.rblock.extractSelectionText(buffer, false, start, end);
			return buffer.toString();
		}
		else {
			return null;
		}
	}

	public boolean hasSelection() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		if(start != null && end != null && !start.equals(end)) {
			return true;
		}
		else {
			return false;
		}		
	}
	
	protected void paintChildren(Graphics g) {
		// Overridding with NOP. For various reasons,
		// the regular mechanism for painting children
		// needs to be handled by Cobra.
	}
	
	public Color getPaintedBackgroundColor() {
		return this.isOpaque() ? this.getBackground() : null;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
	}

	public void relayout() {
		// Expected to be called in the GUI thread.
		// Renderable branch should be invalidated at this
		// point, but this GUI component not necessarily.
		this.revalidatePanel();
	}
	
	public void invalidateLayoutUpTree() {
		// Called when renderable branch is invalidated.
		// We shouldn't do anything here. Changes in renderer
		// tree do not have any bearing on validity of GUI
		// component.
	}

	public void updateAllWidgetBounds() {
		this.rblock.updateWidgetBounds(0, 0);
	}

	public Point getGUIPoint(int clientX, int clientY) {
		// This is the GUI!
		return new Point(clientX, clientY);
	}	
	
	public void focus() {
		this.grabFocus();
	}
	
	private boolean processingDocumentNotification = false;

	void processDocumentNotifications(DocumentNotification[] notifications) {
		// Called in the GUI thread.
		if(this.processingDocumentNotification) {
			// This should not be possible. Even if
			// Javascript modifies the DOM during
			// parsing, this should be executed in
			// the GUI thread, not the parser thread.
			throw new IllegalStateException("Recursive");
		}
		this.processingDocumentNotification = true;
		try {
			//Note: It may be assumed that usually only generic 
			//notifications come in batches. Other types
			//of noitifications probably come one by one.
			boolean topLayout = false;
			java.util.ArrayList repainters = null;
			int length = notifications.length;
			for(int i = 0; i < length; i++) {
				DocumentNotification dn = notifications[i];
				int type = dn.type;
				switch(type) {
				case DocumentNotification.GENERIC: 				
				case DocumentNotification.SIZE: {
					NodeImpl node = dn.node;
					if(node == null) {
						// This is all-invalidate (new style sheet)
						if(loggableInfo) {
							logger.info("processDocumentNotifications(): Calling invalidateLayoutDeep().");
						}
						this.rblock.invalidateLayoutDeep();
						//this.rblock.invalidateRenderStyle();
					}
					else {
						UINode uiNode = node.findUINode();
						if(uiNode != null) {
							RElement relement = (RElement) uiNode;
							relement.invalidateLayoutUpTree();
//							if(type == DocumentNotification.GENERIC) {
//								relement.invalidateRenderStyle();
//							}
						}
						else {
							if(loggableInfo) {
								logger.info("processDocumentNotifications(): Unable to find UINode for " + node);
							}
						}
					}
					topLayout = true;
					break;
				}
				case DocumentNotification.POSITION: {
					//TODO: Could be more efficient.
					NodeImpl node = dn.node;
					NodeImpl parent = (NodeImpl) node.getParentNode();
					if(parent != null) {
						UINode uiNode = parent.findUINode();
						if(uiNode != null) {
							RElement relement = (RElement) uiNode;
							relement.invalidateLayoutUpTree();
						}
					}
					topLayout = true;
					break;
				}
				case DocumentNotification.LOOK: {
					NodeImpl node = dn.node;
					UINode uiNode = node.findUINode();
					if(uiNode != null) {
						if(repainters == null) {
							repainters = new ArrayList(1);
						}
						RElement relement = (RElement) uiNode;
						//relement.invalidateRenderStyle();
						repainters.add(relement);
					}
					break;
				}
				default: 
					break;
				}	
			}
			if(topLayout) {
				this.revalidatePanel();
			}
			else {
				if(repainters != null) {
					Iterator i = repainters.iterator();
					while(i.hasNext()) {
						RElement element = (RElement) i.next();
						element.repaint();
					}
				}
			}
		} finally {
			this.processingDocumentNotification = false;
		}
	}

	public void addDelayedPair(DelayedPair pair) {
		// NOP
	}

	public RenderableContainer getParentContainer() {
		return null;
	}

	public Collection getDelayedPairs() {
		return null;
	}

	public void clearDelayedPairs() {
	}
}