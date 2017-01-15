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
package org.lobobrowser.html.renderer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;

import org.lobobrowser.html.*;
import org.lobobrowser.html.style.CSS2PropertiesImpl;
import org.lobobrowser.html.style.HtmlValues;
import org.lobobrowser.html.style.RenderState;
import org.lobobrowser.html.domimpl.*;
import org.w3c.dom.*;
import java.util.logging.*;

/**
 * A substantial portion of the HTML rendering logic of the package can
 * be found in this class.
 * This class is in charge of laying out the DOM subtree of a node,
 * usually on behalf of an RBlock.
 * It creates a renderer subtree consisting of RLine's or RBlock's. RLine's in
 * turn contain RWord's and so on.
 * This class also happens to be used as an RBlock scrollable viewport.
 * @author J. H. S.
 */
public class RBlockViewport extends BaseRCollection {
	// NOTES:
	// - seqRenderables should be "lines" in positional order
	// to allow binary searches by position.
	
	public static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);
	private static final Logger logger = Logger.getLogger(RBlockViewport.class.getName());
	
	private final ArrayList seqRenderables = new ArrayList();
	//private final ArrayList awtComponents = new ArrayList();
	private final RenderableContainer container;
	private final int listNesting;
	private final UserAgentContext userAgentContext;
	private final HtmlRendererContext rendererContext;
	private final FrameContext frameContext;
	
	private SortedSet positionedRenderables;
//	private Collection exportedRenderables;
	private RLine currentLine;
	private int maxX;
	private int maxY;
	//private int availHeight;
	private int desiredWidth; // includes insets
	private int desiredHeight; // includes insets
	private int availContentHeight; // does not include insets
	private int availContentWidth; // does not include insets
	private int yLimit; 
	private int otherOrdinal;
	private Insets paddingInsets;
	private boolean overrideNoWrap;
	
	//private ModelNode currentRenderableContext;
	private boolean skipParagraphBreakBefore;
	//private boolean skipLineBreakBefore;
	private static final Map elementLayout = new HashMap();
	
	static {
		Map el = elementLayout;
		EmLayout em = new EmLayout();
		el.put("I", em);
		el.put("EM", em);
		el.put("CITE", em);
		el.put("H1", new HLayout(24));
		el.put("H2", new HLayout(18));
		el.put("H3", new HLayout(15));
		el.put("H4", new HLayout(12));
		el.put("H5", new HLayout(10));
		el.put("H6", new HLayout(8));
		StrongLayout strong = new StrongLayout();
		el.put("B", strong);
		el.put("STRONG", strong);
		el.put("TH", strong);
		el.put("U", new ULayout());
		el.put("STRIKE", new StrikeLayout());
		el.put("BR", new BrLayout());
		el.put("P", new PLayout());
		el.put("NOSCRIPT", new NoScriptLayout());
		NopLayout nop = new NopLayout();
		el.put("SCRIPT", nop);
		el.put("HEAD", nop);
		el.put("TITLE", nop);
		el.put("META", nop);
		el.put("STYLE", nop);
		el.put("LINK", nop);
		el.put("IMG", new ImgLayout());
		el.put("TABLE", new TableLayout());
		ChildrenLayout children = new ChildrenLayout();
		el.put("HTML", children);
		AnchorLayout anchor = new AnchorLayout();
		el.put("A", anchor);
		el.put("ANCHOR", anchor);
		el.put("INPUT", new InputLayout2());
		el.put("TEXTAREA", new TextAreaLayout2());
		el.put("SELECT", new SelectLayout());
		ListItemLayout list = new ListItemLayout();
		el.put("UL", list);
		el.put("OL", list);
		el.put("LI", list);
		CommonBlockLayout cbl = new CommonBlockLayout();		
		el.put("PRE", cbl);
		el.put("CENTER", cbl);
		el.put("CAPTION", cbl);
		DivLayout div = new DivLayout();
		el.put("DIV", div);
		el.put("BODY", div);
		el.put("DL", div);
		el.put("DT", div);
		BlockQuoteLayout bq = new BlockQuoteLayout();
		el.put("BLOCKQUOTE", bq);
		el.put("DD", bq);
		el.put("HR", new HrLayout());
		el.put("SPAN", new SpanLayout());
		ObjectLayout ol = new ObjectLayout(false, true);
		el.put("OBJECT", new ObjectLayout(true, true));
		el.put("APPLET", ol);
		el.put("EMBED", ol);
		el.put("IFRAME", new IFrameLayout());
	}
	
	/**
	 * Constructs an HtmlBlockLayout.
	 * @param container This is usually going to be an RBlock.
	 * @param listNesting The nesting level for lists. This is zero except inside a list.
	 * @param pcontext The HTMLParserContext instance.
	 * @param frameContext This is usually going to be HtmlBlock, an object where text selections are contained.
	 * @param parent This is usually going to be the parent of <code>container</code>.
	 */
	public RBlockViewport(ModelNode modelNode, RenderableContainer container, int listNesting, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext, RCollection parent) {
		super(container, modelNode);
		this.parent = parent;
		this.userAgentContext = pcontext;
		this.rendererContext = rcontext;
		this.frameContext = frameContext;
		this.container = container;
		this.listNesting = listNesting;
		// Layout here can always be "invalidated"
		this.layoutUpTreeCanBeInvalidated = true;		
	}
	
	public void invalidateLayoutLocal() {	
		// Workaround for fact that RBlockViewport does not 
		// get validated or invalidated.
		this.layoutUpTreeCanBeInvalidated = true;
	}
	
	public int getAvailContentWidth() {
		return this.availContentWidth;
	}
	
	/**
	 * Builds the layout/renderer tree from scratch.
	 * Note: Returned dimension needs to be actual size needed for rendered content,
	 * not the available container size. This is relied upon by table layout.
	 * @param yLimit If other than -1, <code>layout</code> will throw <code>SizeExceededException</code>
	 * in the event that the layout goes beyond this y-coordinate point.
	 */
	public void layout(int desiredWidth, int desiredHeight, Insets paddingInsets, NodeImpl rootNode, int yLimit, FloatingBounds floatBounds) {
		// Expected in GUI thread. It's possible it may be invoked during pack()
		// outside of the GUI thread.
		if(!EventQueue.isDispatchThread() && logger.isLoggable(Level.INFO)) {
			logger.warning("layout(): Invoked outside GUI dispatch thread.");
		}
		RenderableContainer container = this.container;	
		this.paddingInsets = paddingInsets;
		this.yLimit = yLimit;
		this.desiredHeight = desiredHeight;
		this.desiredWidth = desiredWidth;
		this.floatBounds = floatBounds;

		// maxX and maxY should not be reset by layoutPass.
		this.maxX = paddingInsets.left;
		this.maxY = paddingInsets.top;

		int availw = desiredWidth - paddingInsets.left - paddingInsets.right;
		if(availw < 0) {
			availw = 0;
		}
		int availh = desiredHeight - paddingInsets.top - paddingInsets.bottom;
		if(availh == 0) {
			availh = 0;
		}
		this.availContentHeight = availh;
		this.availContentWidth = availw;
		Dimension desiredSize = new Dimension(desiredWidth, desiredHeight);

		// New floating algorithm.
		this.layoutPass(desiredSize, rootNode);
		Collection delayedPairs = container.getDelayedPairs();
		if(delayedPairs != null && delayedPairs.size() > 0) {
			Collection dpcopy = new LinkedList();
			dpcopy.addAll(delayedPairs);
			// First pass - reposition everything based on floats.
			Iterator i = dpcopy.iterator();
			while(i.hasNext()) {
				DelayedPair pair = (DelayedPair) i.next();
				if(pair.targetParent == container) {
					if(pair.alignment != 0) {
						this.positionFloat(pair);
						//OPTIMIZE: Laying out everything again could
						//be replaced with a repositioning of all 
						//renderables, plus relayout of blocks affected
						//by floats. Note that relayout is necessary
						//because repositioning of one float potentially
						//affects positioning of subsequent floats.
						this.layoutPass(desiredSize, rootNode);
					}
				}				
			}
			// Second pass - add to positioned renderables
			i = dpcopy.iterator();
			while(i.hasNext()) {
				DelayedPair pair = (DelayedPair) i.next();
				if(pair.targetParent == container) {
					if(pair.alignment == 0) {
						this.importNonFloat(pair);
					}
					else {
						this.addFloat(pair);
					}
				}				
			}
		}		

		// Compute bounds...		
		RLine lastLine = this.currentLine;
		Rectangle lastBounds = lastLine.getBounds();
		int lastTopX = lastBounds.x + lastBounds.width;
		if(lastTopX > this.maxX) {
			this.maxX = lastTopX;
		}
		int lastTopY = lastBounds.y + lastBounds.height;
		int maxY = this.maxY;
		if(lastTopY > maxY) {
			this.maxY = maxY = lastTopY;
		}
		this.width = paddingInsets.right + this.maxX;
		this.height = paddingInsets.bottom + maxY;
	}

	private void layoutPass(Dimension desiredSize, NodeImpl rootNode) {
		RenderableContainer container = this.container;	
		container.clearDelayedPairs();
		this.otherOrdinal = 0;

		// Remove sequential renderables...
		ArrayList renderables = this.seqRenderables;
		renderables.clear();

		// Remove other renderables... 
		this.positionedRenderables = null;
		
		// Other initialization...
		Insets paddingInsets = this.paddingInsets;

		// Call addLine after setting margins
		this.currentLine = this.addLine(rootNode, paddingInsets, null);
		this.skipParagraphBreakBefore = true;
		
		// Start laying out...
		// The parent is expected to have set the RenderState already.
		this.layoutChildren(container, desiredSize, paddingInsets, rootNode);		
		
//		if(floatingPairs != null) {
//			Iterator i = floatingPairs.iterator();
//			while(i.hasNext()) {
//				DelayedPair pair = (DelayedPair) i.next();
//				this.addFloat(pair);
//			}
//		}
	}
	
	/**
	 * Applies any horizonal or vertical alignment. It may also adjust
	 * width and height if necessary.
	 * @param canvasWidth
	 * @param canvasHeight
	 * @param paddingInsets
	 */
	public void align(int alignXPercent, int alignYPercent, int canvasWidth, int canvasHeight, Insets paddingInsets) {
		//TODO: Consider left+right margins due to float/align.
		int prevMaxX = this.maxX;
		int prevMaxY = this.maxY;
		// Horizontal alignment
		if(alignXPercent > 0) {
			ArrayList renderables = this.seqRenderables;
			int availLineWidth = canvasWidth - paddingInsets.left - paddingInsets.right;
			int numRenderables = renderables.size();
			for(int i = 0; i < numRenderables; i++) {
				Object r = renderables.get(i);
				if(r instanceof BoundableRenderable) {
					BoundableRenderable line = (BoundableRenderable) r;
					int y = line.getY();
					int leftOffset = this.fetchLeftOffset(y);
					int rightOffset = this.fetchRightOffset(y);
					int actualAvailWidth = availLineWidth - leftOffset - rightOffset;
					int difference = actualAvailWidth - line.getWidth();
					if(difference > 0) {
						int shift = (difference * alignXPercent) / 100;
						int newX = paddingInsets.left + leftOffset + shift;
						line.setX(newX);
						if(newX + line.getWidth() > this.maxX) {
							this.maxX = newX + line.getWidth();
						}
					}
				}
			}
		}
		
		// Vertical alignment
		if(alignYPercent > 0) {
			int availContentHeight = canvasHeight - paddingInsets.top - paddingInsets.bottom;
			int usedHeight = this.maxY - paddingInsets.top;
			int difference = availContentHeight - usedHeight;
			if(difference > 0) {
				int shift = (difference * alignYPercent) / 100;
				// Try sequential renderables first.
				Iterator renderables = this.seqRenderables.iterator();
				while(renderables.hasNext()) {
					Object r = renderables.next();
					if(r instanceof BoundableRenderable) {
						BoundableRenderable line = (BoundableRenderable) r;
						int newY = line.getY() + shift;
						line.setY(newY);
						if(newY + line.getHeight() > this.maxY) {
							this.maxY = newY + line.getHeight();
						}
					}
				}
				
				// Now other renderables, but only those that can be
				// vertically aligned
				Set others = this.positionedRenderables;
				if(others != null) {
					Iterator i2 = others.iterator();
					while(i2.hasNext()) {
						PositionedRenderable pr = (PositionedRenderable) i2.next();
						if(pr.verticalAlignable) {
							BoundableRenderable br = pr.renderable;
							int newY = br.getY() + shift;
							br.setY(newY);
							if(newY + br.getHeight() > this.maxY) {
								this.maxY = newY + br.getHeight();
							}							
						}
					}
				}
			}
		}
		if(prevMaxX != this.maxX) {
			this.width += (this.maxX - prevMaxX);
		}
		if(prevMaxY != this.maxY) {
			this.height += (this.maxY - prevMaxY);
		}
	}

//	/**
//	 * Gets the parent RBlockViewport.
//	 * Does not cross tables.
//	 */
//	private RBlockViewport getContainingViewport() {
//		Object parent = this.parent;
//		if(parent instanceof RBlock) {
//			parent = ((RBlock) parent).parent;
//			if(parent instanceof RBlockViewport) {
//				return (RBlockViewport) parent;
//			}
//		}
//		return null;
//	}
	
	private RLine addLine(ModelNode startNode, Insets insets, RLine prevLine) {
		int newLineY = prevLine == null ? insets.top : prevLine.y + prevLine.height;
		this.checkY(newLineY);
		int leftOffset = this.fetchLeftOffset(newLineY);
		int insetsl = insets.left;
		int newX = insetsl + leftOffset;
		int newMaxWidth = this.availContentWidth - this.fetchRightOffset(newLineY) - leftOffset;
		RLine rline;
		if(prevLine == null) {
			// Note: Assumes that prevLine == null means it's the first line.
			RenderState rs = this.modelNode.getRenderState();
			int textIndent = rs == null ? 0 : rs.getTextIndent(this.availContentWidth);
			if(textIndent != 0) {
				newX += textIndent;
			}
			rline = new RLine(startNode, this.container, newX, newLineY, newMaxWidth, 0);			
		}
		else {
			if(prevLine.x + prevLine.width > this.maxX) {
				this.maxX = prevLine.x + prevLine.width;
			}
			rline = new RLine(startNode, this.container, newX, newLineY, newMaxWidth, 0);
		}
		rline.setParent(this);
		this.seqRenderables.add(rline);
		this.currentLine = rline;
		return rline;
	}
	
	private void layoutMarkup(RenderableContainer container, Dimension containerSize, Insets insets, NodeImpl node) {
		// This is the "inline" layout of an element.
		// The difference with layoutChildren is that this
		// method checks for padding and margin insets.
		RenderState rs = node.getRenderState();
		Insets marginInsets = null;
		Insets paddingInsets = null;
		if(rs != null) {
			marginInsets = rs.getMarginInsets();
			paddingInsets = rs.getPaddingInsets();
		}
		int leftSpacing = 0;
		int rightSpacing = 0;
		if(marginInsets != null) {
			leftSpacing += marginInsets.left;
			rightSpacing += marginInsets.right;
		}
		if(paddingInsets != null) {
			leftSpacing += paddingInsets.left;
			rightSpacing += paddingInsets.right;
		}
		if(leftSpacing > 0) {
			RLine line = this.currentLine;
			line.addSpacing(new RSpacing(node, this.container, leftSpacing, line.height));
		}
		this.layoutChildren(container, containerSize, insets, node);
		if(rightSpacing > 0) {
			RLine line = this.currentLine;
			line.addSpacing(new RSpacing(node, this.container, rightSpacing, line.height));
		}
	}

	private void layoutChildren(RenderableContainer container, Dimension containerSize, Insets insets, NodeImpl node) {
		NodeImpl[] childrenArray = node.getChildrenArray();
		if(childrenArray != null) {
			int length = childrenArray.length;
			for(int i = 0; i < length; i++) {
				NodeImpl child = childrenArray[i];
				short nodeType = child.getNodeType();
				if(nodeType == Node.TEXT_NODE) {
					this.layoutText(container, containerSize, insets, child);					
				}
				else if(nodeType == Node.ELEMENT_NODE) {
					String nodeName = child.getNodeName().toUpperCase();
					this.currentLine.addStyleChanger(new RStyleChanger(child));
					MarkupLayout ml = (MarkupLayout) elementLayout.get(nodeName);
					if(ml != null) {
						ml.layoutMarkup(this, container, containerSize, insets, (HTMLElementImpl) child);
					}
					else {
						this.layoutMarkup(container, containerSize, insets, child);
					}
					this.currentLine.addStyleChanger(new RStyleChanger(node));
				}
				else if(nodeType == Node.COMMENT_NODE) {
					// ignore
				}
				else {
					throw new IllegalStateException("Unknown node: " + child);
				}
			}
		}
	}

	private final void positionRBlock(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement, RBlock renderable) {
		// BlockRenderState gets text background color in a different way.
		if(!this.addElsewhereIfPositioned(container, containerSize, insets, renderable, markupElement, false, true, false)) {
			// Block can also define text style.
			RLine line = this.currentLine;
			int newY = line.getY() + line.getHeight(); 
			// Set tentative block origin, so that float bounds work correctly during layout.			
			Insets paddingInsets = this.paddingInsets;
			int newX = paddingInsets == null ? 0 : paddingInsets.left;
			renderable.setOrigin(newX, newY);
			FloatingBounds currentBounds = this.floatBounds;
			RBlockViewport targetViewport = renderable.getRBlockViewport();
			FloatingBounds childBounds = currentBounds == null ? null : new OffsetFloatingBounds(currentBounds, this, this.paddingInsets, targetViewport, targetViewport.paddingInsets);
			renderable.layout(this.availContentWidth, this.availContentHeight, true, false, childBounds, newY);
			this.addAsSeqBlock(containerSize, insets, renderable, false, false, 0);
		}
	}
	
	private final void positionRElement(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement, RElement renderable, boolean usesAlignAttribute) {
		if(!this.addElsewhereIfPositioned(container, containerSize, insets, renderable, markupElement, usesAlignAttribute, true, false)) {
			renderable.layout(this.availContentWidth, this.availContentHeight, false, false);
			this.addAsSeqBlock(containerSize, insets, renderable, false, false, 0);
		}
	}

	private final void layoutRBlock(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement, Insets defaultMarginInsets) {
		RBlock renderable = (RBlock) markupElement.getUINode();
		if(renderable == null) {
			renderable = new RBlock(markupElement, this.listNesting, this.userAgentContext, this.rendererContext, this.frameContext, this.container, RBlock.OVERFLOW_NONE);
			markupElement.setUINode(renderable);
		}
		renderable.setOriginalParent(this);
		renderable.setDefaultMarginInsets(defaultMarginInsets);
		this.positionRBlock(container, containerSize, insets, markupElement, renderable);
	}

	private final void layoutRTable(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
		RElement renderable = (RElement) markupElement.getUINode();
		if(renderable == null) {
			renderable = new RTable(markupElement, this.userAgentContext, this.rendererContext, this.frameContext, container);
			markupElement.setUINode((UINode) renderable);
		}
		renderable.setOriginalParent(this);
		renderable.layout(this.availContentWidth, this.availContentHeight, false, false);
		this.positionRElement(container, containerSize, insets, markupElement, renderable, markupElement instanceof HTMLTableElementImpl);
	}

	private final void layoutListItem(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
		RListItem renderable = (RListItem) markupElement.getUINode();
		if(renderable == null) {
			renderable = new RListItem(markupElement, this.listNesting, this.userAgentContext, this.rendererContext, this.frameContext, this.container, null);
			markupElement.setUINode(renderable);
		}
		renderable.setOriginalParent(this);
		this.positionRBlock(container, containerSize, insets, markupElement, renderable);		
	}

	private final void layoutList(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
		RList renderable = (RList) markupElement.getUINode();
		if(renderable == null) {
			renderable = new RList(markupElement, this.listNesting, this.userAgentContext, this.rendererContext, this.frameContext, this.container, null);
			markupElement.setUINode(renderable);
		}
		renderable.setOriginalParent(this);
		this.positionRBlock(container, containerSize, insets, markupElement, renderable);		
	}

//	final void importRenderable(ExportedRenderable er) {
//		switch(er.alignment) {
//		case +1: {
//			Point point = this.translateDescendentPoint(er.originalTarget, er.x, er.y);
//			int rightOffset = this.fetchRightOffset(point.y);
//			this.addToRightMargin(er.renderable, point.x, point.y, rightOffset, false);
//			break;
//		}
//		case -1: {
//			Point point = this.translateDescendentPoint(er.originalTarget, er.x, er.y);
//			int leftOffset = this.fetchLeftOffset(point.y);
//			this.addToLeftMargin(er.renderable, point.x, point.y, leftOffset, false);
//			break;
//		}
//		case 0: {
//			// In this case the original x and y points are not 
//			// translated.
//			BoundableRenderable r = er.renderable;
//			r.setOrigin(er.x, er.y);
//			this.addPositionedRenderable(r, false);
//			if(er.x + r.getWidth() > this.maxX) {
//				this.maxX = er.x + r.getWidth();
//			}
//			int topY = er.y + r.getHeight();
//			if(topY > this.maxY) {
//				this.maxY = topY;
//			}
//			break;
//		}
//		}
//	}

	private void addParagraphBreak(RenderableContainer container, Dimension containerSize, Insets insets, ModelNode startNode) {
		this.addLineBreak(container, containerSize, insets, startNode);
		this.addLineBreak(container, containerSize, insets, startNode);
	}
	
	private void addLineBreak(RenderableContainer container, Dimension containerSize, Insets insets, ModelNode startNode) {
		RLine line = this.currentLine;
		if(line.getHeight() == 0) {
			RenderState rs = startNode.getRenderState();
			int fontHeight = rs.getFontMetrics().getHeight();
			line.setHeight(fontHeight);
		}
		this.currentLine = this.addLine(startNode, insets, this.currentLine);
	}
	
	private boolean addElsewhereIfFloat(RenderableContainer container, Dimension containerSize, Insets insets, RElement renderable, HTMLElementImpl element, boolean usesAlignAttribute, CSS2PropertiesImpl style, boolean layout) {
		// "static" handled here
		String align = null;
		if(style != null) {
			align = style.getFloat();
			if(align != null && align.length() == 0) {
				align = null;
			}
		}
		if(align == null && usesAlignAttribute) {
			align = element.getAttribute("align");
		}
		if(align != null) {
			if("left".equalsIgnoreCase(align)) {
				this.addToLeftMargin(renderable, layout);
				return true;
			}
			else if("right".equalsIgnoreCase(align)) {
				this.addToRightMargin(renderable, layout);				
				return true;
			}
			else {
				// fall through
			}
		}	
		return false;
	}
	
	final RBlockViewport getParentViewport(ExportedRenderable er) {
		if(er.alignment == 0) {
			return this.getParentViewport();
		}
		else {
			return this.getParentViewportForAlign();
		}
	}
	
	final boolean isImportable(ExportedRenderable er) {
		if(er.alignment == 0) {
			return this.positionsAbsolutes();
		}
		else {
			return this.getParentViewportForAlign() == null;
		}
	}
	
	final RBlockViewport getParentViewport() {
		// Use originalParent, which for one, is not going to be null during layout.
		RCollection parent = this.getOriginalOrCurrentParent();
		while(parent != null && !(parent instanceof RBlockViewport)) {
			parent = parent.getOriginalOrCurrentParent();
		}
		return (RBlockViewport) parent;
	}

	final RBlockViewport getParentViewportForAlign() {
		// Use originalParent, which for one, is not going to be null during layout.
		Object parent = this.getOriginalOrCurrentParent();
		if(parent instanceof RBlock) {
			RBlock block = (RBlock) parent;
			if(!block.couldBeScrollable()) {
				parent = ((BaseElementRenderable) parent).getOriginalOrCurrentParent();
				if(parent instanceof RBlockViewport) {
					return (RBlockViewport) parent;
				}
			}	
		}
		return null;
	}

	/**
	 * Returns true iff this viewport can position boxes that
	 * are absolutely positioned.
	 */
	final boolean positionsAbsolutes() {
		ModelNode node = this.getModelNode();
		if(node instanceof HTMLElementImpl) {
			HTMLElementImpl element = (HTMLElementImpl) node;
			CSS2PropertiesImpl style = element.getCurrentStyle();
			if(style != null) {
				String position = style.getPosition();
				return position != null && (position.equalsIgnoreCase("absolute") || position.equalsIgnoreCase("fixed") || position.equalsIgnoreCase("relative"));
			}
			else {
				return false;
			}
		}
		else if(node instanceof HTMLDocumentImpl) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Checks for position and float attributes.
	 * @param container
	 * @param containerSize
	 * @param insets
	 * @param renderable
	 * @param element
	 * @param usesAlignAttribute
	 * @return True if it was added elsewhere.
	 */
	private boolean addElsewhereIfPositioned(RenderableContainer container, Dimension containerSize, Insets insets, RElement renderable, HTMLElementImpl element, boolean usesAlignAttribute, boolean layout, boolean obeysMargins) {
		// At this point block already has bounds.
		CSS2PropertiesImpl style = element.getCurrentStyle();
		String position = null;
		if(style != null) {
			position = style.getPosition();
			if(position != null && position.length() == 0) {
				position = null;
			}
		}
		boolean absolute = position != null && "absolute".equalsIgnoreCase(position);
		boolean relative = position != null && "relative".equalsIgnoreCase(position);
		if(absolute || relative) {
			if(layout) {
				// Presumes the method will return true.
				if(relative) {
					renderable.layout(this.availContentWidth, this.availContentHeight, true, false);
				}
				else {
					renderable.layout(this.availContentWidth, this.availContentHeight, false, false);
				}
			}
			RenderState rs = element.getRenderState();
			String leftText = style.getLeft();
			RLine line = this.currentLine;
			int lineBottomY = line == null ? 0 : line.getY() + line.getHeight();
			int newLeft;
			if(leftText != null) {
				newLeft = HtmlValues.getPixelSize(leftText, rs, 0, this.availContentWidth);
			}
			else {
				String rightText = style.getRight();
				if(rightText != null) {
					int right = HtmlValues.getPixelSize(rightText, rs, 0, this.availContentWidth);
					newLeft = containerSize.width - right - renderable.getWidth();
					// If right==0 and renderable.width is larger than the parent's width, 
					// the expected behavior is for newLeft to be negative.
				}
				else {
					newLeft = 0;
				}
			}
			int newTop = relative ? 0 : lineBottomY;
			String topText = style.getTop();
			if(topText != null) {
				newTop = HtmlValues.getPixelSize(topText, rs, newTop, this.availContentHeight);
			}
			else {
				String bottomText = style.getBottom();
				if(bottomText != null) {
					int bottom = HtmlValues.getPixelSize(bottomText, rs, 0, this.availContentHeight);
					newTop = containerSize.height - bottom - renderable.getHeight();
					if(!relative && newTop < 0) {
						newTop = 0;
					}
				}
			}
			if(relative) {
				// First, try to add normally.
				boolean added = false;
				if(!this.addElsewhereIfFloat(container, containerSize, insets, renderable, element, usesAlignAttribute, style, false)) {
					// This is a fake add as block (should not add to seqRenderables).
					this.addAsSeqBlock(containerSize, insets, renderable, true, obeysMargins, 0);
				}
				else {
					added = true;
				}
				// Second, shift origin.
				renderable.setOrigin(renderable.getX() + newLeft, renderable.getY() + newTop);
				// Now really add to other renderables.
				if(!added) {
					this.addPositionedRenderable(renderable, true);
				}
			}
			else {
				// Schedule as delayed pair. Will be positioned after
				// everything else.
				this.scheduleAbsDelayedPair(renderable, newLeft, newTop, 0);
				// Does not affect bounds of viewport.
				return true;
			}
			int newBottomY = renderable.getY() + renderable.getHeight();
			this.checkY(newBottomY);
			if(newBottomY > this.maxY) {
				this.maxY = newBottomY;
			}
			return true;
		}
		else {
			if(this.addElsewhereIfFloat(container, containerSize, insets, renderable, element, usesAlignAttribute, style, layout)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPositionedElsewhere() {
		ModelNode node = this.modelNode;
		if(node instanceof HTMLElementImpl) {
			CSS2PropertiesImpl style = ((HTMLElementImpl) node).getCurrentStyle();
			if(style != null) {
				String position = style.getPosition();
				if(position != null) {
					if("absolute".equalsIgnoreCase(position) || "relative".equalsIgnoreCase(position)) {
						//TODO: fixed
						return true;
					}
				}
				String floatText = style.getFloat();
				if(floatText != null) {
					if("left".equalsIgnoreCase(floatText) || "right".equalsIgnoreCase(floatText)) {
						return true;
					}
				}
			}
		}
		return false;		
	}
	
	/**
	 * Checks property 'float' and in some cases attribute 'align'.
	 */
	private void addRenderableToLineCheckStyle(RenderableContainer container, Dimension containerSize, Insets insets, RElement renderable, HTMLElementImpl element, boolean usesAlignAttribute) {
		if(this.addElsewhereIfPositioned(container, containerSize, insets, renderable, element, usesAlignAttribute, false, true)) {
			return;
		}
		this.addRenderableToLine(container, insets, renderable);
	}
	
	private void addRenderableToLine(RenderableContainer container, Insets insets, Renderable renderable) {
		//this.skipLineBreakBefore = false;
		RenderState rs = renderable.getModelNode().getRenderState();
		int whiteSpace = this.overrideNoWrap ? RenderState.WS_NOWRAP : rs.getWhiteSpace();
		boolean allowOverflow = whiteSpace == RenderState.WS_NOWRAP || whiteSpace == RenderState.WS_PRE;
		this.skipParagraphBreakBefore = false;
		RLine line = this.currentLine;
		try {
			line.add(renderable, allowOverflow);
		} catch(OverflowException oe) {
			this.addLine(renderable.getModelNode(), insets, line);
			Collection renderables = oe.getRenderables();
			Iterator i = renderables.iterator();
			while(i.hasNext()) {
				Renderable r = (Renderable) i.next();
				this.addRenderableToLine(container, insets, r);
			}
		}
		if(renderable instanceof RUIControl) {
			this.container.add(((RUIControl) renderable).widget.getComponent());
		}
	}
	
	private void addWordToLine(RenderableContainer container, Insets insets, RWord renderable, boolean allowOverflow) {
		//this.skipLineBreakBefore = false;
		this.skipParagraphBreakBefore = false;
		RLine line = this.currentLine;
		try {
			line.addWord(renderable, allowOverflow);
		} catch(OverflowException oe) {
			this.addLine(renderable.getModelNode(), insets, line);
			Collection renderables = oe.getRenderables();
			Iterator i = renderables.iterator();
			while(i.hasNext()) {
				Renderable r = (Renderable) i.next();
				this.addRenderableToLine(container, insets, r);
			}
		}
	}

	private void addAsSeqBlockCheckStyle(Dimension containerSize, Insets insets, RElement block, HTMLElementImpl element, boolean usesAlignAttribute) {
		if(this.addElsewhereIfPositioned(this.container, containerSize, insets, block, element, usesAlignAttribute, false, true)) {
			return;
		}
		this.addAsSeqBlock(containerSize, insets, block, 0);		
	}
	
	private void addAsSeqBlock(Dimension containerSize, Insets insets, BoundableRenderable block, int alignXPercent) {
		this.addAsSeqBlock(containerSize, insets, block, false, true, alignXPercent);
	}
	
	private void addAsSeqBlock(Dimension containerSize, Insets insets, BoundableRenderable block, boolean fakeAdd, boolean obeysMargins, int alignXPercent) {
		int insetsl = insets.left;
		Collection sr = this.seqRenderables;
		RLine prevLine = this.currentLine;
		if(prevLine != null) {
			if(prevLine.x + prevLine.width > this.maxX) {
				this.maxX = prevLine.x + prevLine.width;
			}
			// Check height only with floats.
		}
		int newLineY = prevLine == null ? insets.top : prevLine.y + prevLine.height;
		int blockX;
		if(obeysMargins) {
			int blockOffset = this.fetchLeftOffset(newLineY);
			blockX = blockOffset + insetsl;
			if(alignXPercent > 0) {
				int offset = (this.availContentWidth - blockOffset - block.getWidth()) * alignXPercent / 100;
				if(offset > 0) {
					blockX += offset;
				}
			}
		}
		else {
			//Block does not obey alignment margins
			blockX = insetsl;
			if(alignXPercent > 0) {
				int offset = (this.availContentWidth - block.getWidth()) * alignXPercent / 100;
				if(offset > 0) {
					blockX += offset;
				}
			}			
		}
		block.setOrigin(blockX, newLineY);
		if(!fakeAdd) {
			sr.add(block);
			block.setParent(this);
		}
		if(blockX + block.getWidth() > this.maxX) {
			this.maxX = blockX + block.getWidth();
		}
		newLineY += block.getHeight();
		this.checkY(newLineY);
		int leftOffset = this.fetchLeftOffset(newLineY);
		int newX = insetsl + leftOffset;
		int newMaxWidth = containerSize.width - insetsl - insets.right - this.fetchRightOffset(newLineY) - leftOffset;
		ModelNode lineNode = block.getModelNode().getParentModelNode();
		RLine newLine = new RLine(lineNode, this.container, newX, newLineY, newMaxWidth, 0);
		newLine.setParent(this);
		sr.add(newLine);
		this.currentLine = newLine;
		this.skipParagraphBreakBefore = false;
	}
			
	private void layoutText(RenderableContainer container, Dimension containerSize, Insets insets, NodeImpl textNode) {
		RenderState renderState = textNode.getRenderState();
		if(renderState == null) {
			throw new IllegalStateException("RenderState is null for node " + textNode + " with parent " + textNode.getParentNode());
		}
		FontMetrics fm = renderState.getFontMetrics();
		int descent = fm.getDescent();
		int ascentPlusLeading = fm.getAscent() + fm.getLeading();
		int wordHeight = fm.getHeight();
		int blankWidth = fm.charWidth(' ');
		int whiteSpace = this.overrideNoWrap ? RenderState.WS_NOWRAP : renderState.getWhiteSpace();
		String text = textNode.getNodeValue(); 
		if(whiteSpace != RenderState.WS_PRE) {
			boolean allowOverflow = whiteSpace == RenderState.WS_NOWRAP;
			int length = text.length();
			StringBuffer word = new StringBuffer(12);
			for(int i = 0; i < length; i++) {
				char ch = text.charAt(i);
				if(Character.isWhitespace(ch)) {
					int wlen = word.length();
					if(wlen > 0) {
						RWord rword = new RWord(textNode, word.toString(), container, fm, descent, ascentPlusLeading, wordHeight);
						this.addWordToLine(container, insets, rword, allowOverflow);
						word.delete(0, wlen);
					}
					RLine line = this.currentLine;
					if(line.width > 0) {
						RBlank rblank = new RBlank(textNode, fm, container, ascentPlusLeading, blankWidth, wordHeight);
						line.addBlank(rblank);					
					}
					for(i++; i < length; i++) {
						ch = text.charAt(i);
						if(!Character.isWhitespace(ch)) {
							word.append(ch);
							break;
						}
					}
				}
				else {
					word.append(ch);
				}
			}
			if(word.length() > 0) {
				RWord rword = new RWord(textNode, word.toString(), container, fm, descent, ascentPlusLeading, wordHeight);
				this.addWordToLine(container, insets, rword, allowOverflow);
			}
		}
		else {
			int length = text.length();
			boolean lastCharSlashR = false;
			StringBuffer line = new StringBuffer();
			for(int i = 0; i < length; i++) {
				char ch = text.charAt(i);
				switch(ch) {
				case '\r':
					lastCharSlashR = true;
					break;
				case '\n':
					int llen = line.length();
					if(llen > 0) {
						RWord rword = new RWord(textNode, line.toString(), container, fm, descent, ascentPlusLeading, wordHeight);
						this.addWordToLine(container, insets, rword, true);
						line.delete(0, line.length());
					}
					this.addLine(textNode, insets, this.currentLine);
					break;
				default:
					if(lastCharSlashR) {
						line.append('\r');
						lastCharSlashR = false;
					}
					line.append(ch);
					break;
				}
			}
			if(line.length() > 0) {
				RWord rword = new RWord(textNode, line.toString(), container, fm, descent, ascentPlusLeading, wordHeight);
				this.addWordToLine(container, insets, rword, true);
			}
		}
	}

	/**
	 * 
	 * @param others An ordered collection.
	 * @param seqRenderables
	 * @param destination
	 */
	private void populateZIndexGroups(Collection others, Collection seqRenderables, ArrayList destination) {
		this.populateZIndexGroups(others, seqRenderables.iterator(), destination);
	}

	/**
	 * 
	 * @param others An ordered collection.
	 * @param seqRenderablesIterator
	 * @param destination
	 */
	private void populateZIndexGroups(Collection others, Iterator seqRenderablesIterator, ArrayList destination) {
		// First, others with z-index < 0
		Iterator i1 = others.iterator();
		Renderable pending = null;
		while(i1.hasNext()) {
			PositionedRenderable pr = (PositionedRenderable) i1.next();
			Renderable r = pr.renderable;
			if(r.getZIndex() >= 0) {
				pending = r;
				break;
			}
			destination.add(r);
		}
		// Second, sequential renderables
		Iterator i2 = seqRenderablesIterator;
		while(i2.hasNext()) {
			destination.add(i2.next());
		}
		
		// Third, other renderables with z-index >= 0.
		if(pending != null) {
			destination.add(pending);
			while(i1.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i1.next();
				Renderable r = pr.renderable;
				destination.add(r);			
			}
		}
	}
	
	public Renderable[] getRenderablesArray() {
		SortedSet others = this.positionedRenderables;
		int othersSize = others == null ? 0 : others.size();
		if(othersSize == 0) {
			return (Renderable[]) this.seqRenderables.toArray(Renderable.EMPTY_ARRAY);
		}
		else {
			ArrayList allRenderables = new ArrayList();
			this.populateZIndexGroups(others, this.seqRenderables, allRenderables);
			return (Renderable[]) allRenderables.toArray(Renderable.EMPTY_ARRAY);
		}
	}
	
	public Iterator getRenderables() {
		SortedSet others = this.positionedRenderables;
		if(others == null || others.size() == 0) {
			return this.seqRenderables.iterator();
		}
		else {
			ArrayList allRenderables = new ArrayList();
			this.populateZIndexGroups(others, this.seqRenderables, allRenderables);
			return allRenderables.iterator();
		}
	}

	public Iterator getRenderables(Rectangle clipBounds) {
		if(!EventQueue.isDispatchThread() && logger.isLoggable(Level.INFO)) {
			logger.warning("getRenderables(): Invoked outside GUI dispatch thread.");
		}
		Renderable[] array = (Renderable[]) this.seqRenderables.toArray(Renderable.EMPTY_ARRAY);
		Range range = MarkupUtilities.findRenderables(array, clipBounds, true);
		Iterator baseIterator = org.lobobrowser.util.ArrayUtilities.iterator(array, range.offset, range.length);
		SortedSet others = this.positionedRenderables;
		if(others == null || others.size() == 0) {
			return baseIterator;
		}
		else {
			ArrayList matches = new ArrayList();
			// ArrayList "matches" keeps the order from "others".
			Iterator i = others.iterator();
			while(i.hasNext()) {
				PositionedRenderable pr = (PositionedRenderable) i.next();
				Object r = pr.renderable;
				if(r instanceof BoundableRenderable) {
					BoundableRenderable br = (BoundableRenderable) r;
					Rectangle rbounds = br.getBounds();
					if(clipBounds.intersects(rbounds)) {
						matches.add(pr);
					}
				}
			}
			if(matches.size() == 0) {
				return baseIterator;
			}
			else {
				ArrayList destination = new ArrayList();
				this.populateZIndexGroups(matches, baseIterator, destination);
				return destination.iterator();
			}
		}
	}

	
	public BoundableRenderable getRenderable(int x, int y) {
		Iterator i = this.getRenderables(x, y);
		return i == null ? null : (i.hasNext() ? (BoundableRenderable) i.next() : null);
	}

	public BoundableRenderable getRenderable(java.awt.Point point) {
		return this.getRenderable(point.x, point.y);
	}

	public Iterator getRenderables(java.awt.Point point) {
		return this.getRenderables(point.x, point.y);
	}
	
	public Iterator getRenderables(int pointx, int pointy) {
		if(!EventQueue.isDispatchThread() && logger.isLoggable(Level.INFO)) {
			logger.warning("getRenderable(): Invoked outside GUI dispatch thread.");
		}
		Collection result = null;
		SortedSet others = this.positionedRenderables;
		int size = others == null ? 0 : others.size();
		PositionedRenderable[] otherArray = size == 0 ? null : (PositionedRenderable[]) others.toArray(PositionedRenderable.EMPTY_ARRAY);
		// Try to find in other renderables with z-index >= 0 first.
		int index = 0;
		if(size != 0) {
			int px = pointx;
			int py = pointy;	
			// Must go in reverse order
			for(index = size; --index >= 0;) {
				PositionedRenderable pr = otherArray[index];
				Renderable r = pr.renderable;
				if(r.getZIndex() < 0) {
					break;
				}
				if(r instanceof BoundableRenderable) {
					BoundableRenderable br = (BoundableRenderable) r;
					Rectangle rbounds = br.getBounds();
					if(rbounds.contains(px, py)) {
						if(result == null) {
							result = new LinkedList();
						}
						result.add(br);
					}
				}
			}
		}		
		// Now do a "binary" search on sequential renderables.
		Renderable[] array = (Renderable[]) this.seqRenderables.toArray(Renderable.EMPTY_ARRAY);
		BoundableRenderable found = MarkupUtilities.findRenderable(array, pointx, pointy, true);
		if(found != null) {
			if(result == null) {
				result = new LinkedList();
			}
			result.add(found);
		}
		// Finally, try to find it in renderables with z-index < 0.
		if(size != 0) {
			int px = pointx;
			int py = pointy;	
			// Must go in reverse order
			for(; index >= 0; index--) {
				PositionedRenderable pr = otherArray[index];
				Renderable r = pr.renderable;
				if(r instanceof BoundableRenderable) {
					BoundableRenderable br = (BoundableRenderable) r;
					Rectangle rbounds = br.getBounds();
					if(rbounds.contains(px, py)) {
						if(result == null) {
							result = new LinkedList();
						}
						result.add(br);
					}
				}
			}
		}	
		return result == null ? null : result.iterator();
	}

	private RElement setupNewUIControl(RenderableContainer container, HTMLElementImpl element, UIControl control) {
		RElement renderable = new RUIControl(element, control, container, this.frameContext, this.userAgentContext);
		element.setUINode((UINode) renderable);
		return renderable;
	}
	
	private final void addAlignableAsBlock(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement, BoundableRenderable renderable) {
		// At this point block already has bounds.
		boolean regularAdd = false;
		String align = markupElement.getAttribute("align");
		if(align != null) {
			if("left".equalsIgnoreCase(align)) {
				this.addToLeftMargin(renderable, false);
			}
			else if("right".equalsIgnoreCase(align)) {
				this.addToRightMargin(renderable, false);				
			}
			else {
				regularAdd = true;
			}	
		}
		else {
			regularAdd = true;
		}		
		if(regularAdd) {
			int alignXPercent = 0;
			if(align != null) {
				if("center".equalsIgnoreCase(align)) {
					alignXPercent = 50;
				}
			}
			this.addAsSeqBlock(containerSize, insets, renderable, alignXPercent);
		}
	}
	
	private final void layoutHr(RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
		RElement renderable = (RElement) markupElement.getUINode();
		if(renderable == null) {
			renderable = this.setupNewUIControl(container, markupElement, new HrControl(markupElement));
		}
		renderable.layout(this.availContentWidth, this.availContentHeight, false, false);
		this.addAlignableAsBlock(container, containerSize, insets, markupElement, renderable);
	}

	private final BaseInputControl createInputControl(HTMLBaseInputElement markupElement) {
		String type = markupElement.getAttribute("type");
		if(type == null) {
			return new InputTextControl(markupElement);
		}
		type = type.toLowerCase();
		if("text".equals(type) || type.length() == 0) {
			return new InputTextControl(markupElement);
		}
		else if("hidden".equals(type)) {
			return null;
		}
		else if("submit".equals(type)) {
			return new InputButtonControl(markupElement);
		}
		else if("password".equals(type)) {
			return new InputPasswordControl(markupElement);
		}
		else if("radio".equals(type)) {
			return new InputRadioControl(markupElement);
		}
		else if("checkbox".equals(type)) {
			return new InputCheckboxControl(markupElement);
		}
		else if("image".equals(type)) {
			return new InputImageControl(markupElement);
		}
		else if("reset".equals(type)) {
			return new InputButtonControl(markupElement);
		}
		else if("button".equals(type)) {
			return new InputButtonControl(markupElement);
		}
		else if("file".equals(type)) {
			return new InputFileControl(markupElement);
		}
		else {
			return null;
		}
	}
	
	//private LineMargin leftMargin = null;
	//private LineMargin rightMargin = null;
	
	/**
	 * Gets offset from the left due to floats. It does
	 * not count padding.
	 */
	private final int fetchLeftOffset(int newLineY) {
		FloatingBounds floatBounds = this.floatBounds;
		return floatBounds == null ? 0 : floatBounds.getLeft(newLineY);
	}
	
	private final int fetchRightOffset(int newLineY) {
		FloatingBounds floatBounds = this.floatBounds;
		return floatBounds == null ? 0 : floatBounds.getRight(newLineY);
	}
	
//	private final void checkLineOverflow() {
//		RLine line = this.currentLine;
//		int lineY = line.getY();
//		int leftOffset = this.fetchLeftOffset(lineY);
//		int rightOffset = this.fetchRightOffset(lineY);
//		Insets insets = this.paddingInsets;
//		int newX = insets.left + leftOffset;
//		int newMaxWidth = this.availContentWidth - rightOffset - leftOffset;
//		if(newX != line.getX() || newMaxWidth != line.getWidth()) {
//			try {
//				line.adjustHorizontalBounds(newX, newMaxWidth);
//			} catch(OverflowException oe) {
//				Collection renderables = oe.getRenderables();
//				Iterator i = renderables.iterator();
//				RenderableContainer rc = this.container;
//				boolean first = true;
//				while(i.hasNext()) {
//					Renderable r = (Renderable) i.next();
//					if(first) {
//						first = false;
//						this.addLine(r.getModelNode(), insets, line);
//					}
//					this.addRenderableToLine(rc, insets, r);
//				}
//			}
//		}
//	}

	private static final SizeExceededException SEE = new SizeExceededException();
	
	private final void checkY(int y) {
		if(this.yLimit != -1 && y > this.yLimit) {
			throw SEE;
		}
	}
	
	private final void addToRightMargin(BoundableRenderable brenderable, boolean layout) {
		brenderable.setOriginalParent(this);
		int availWidth = this.availContentWidth;
		int availHeight = this.availContentHeight;
		if(layout) {
			if(brenderable instanceof RElement) {
				((RElement) brenderable).layout(availWidth, availHeight, false, false);
			}
			else {
				throw new IllegalStateException();
			}
		}
		int width = brenderable.getWidth();
		int y = this.currentLine.getBounds().y;
		int rightOffset = 0;
		Insets insets = this.paddingInsets;
		int x = insets.left + this.availContentWidth - rightOffset - width;
		if(x < 0) {
			x = 0;
		}
		this.scheduleFloatDelayedPair(brenderable, x, y, +1);
	}
 
	private final void addToLeftMargin(BoundableRenderable brenderable, boolean layout) {
		brenderable.setOriginalParent(this);
		int availWidth = this.availContentWidth;
		int availHeight = this.availContentHeight;
		if(layout) {
			if(brenderable instanceof RElement) {
				((RElement) brenderable).layout(availWidth, availHeight, false, false);
			}
			else {
				throw new IllegalStateException();
			}
		}
		int y = this.currentLine.getBounds().y;
		int leftOffset = this.fetchLeftOffset(y);
		Insets insets = this.paddingInsets;
		int x = insets.left + leftOffset;
		this.scheduleFloatDelayedPair(brenderable, x, y, -1);
	}

//	private final void addToRightMargin(BoundableRenderable brenderable, int x, int y, int rightOffset, boolean export) {
//		int width = brenderable.getWidth();
//		int height = brenderable.getHeight();
//		// Update maxY and maxY if necessary
//		if(x + width > this.maxX) {
//			this.maxX = x + width;
//		}
//		LineMargin newMargin = new LineMargin(this.rightMargin, y + height, rightOffset + width);
//		this.rightMargin = newMargin;
//		this.checkLineOverflow();
//		if(export && this.scheduleAddInContainingViewport(this, brenderable, x, y, +1)) {
//			// Must try parent viewport. Consider an aligned block
//			// bigger than the current viewport which must also
//			// become a margin for a paragraph in the parent viewport
//			// and sibling paragraphs.
//			return;
//		}
//		int newY = y + height;
//		this.checkY(newY);
//		if(newY > this.maxY) {
//			this.maxY = newY;
//		}
//		//Should only set origin, not size.
//		//brenderable.setBounds(x, y, width, height);
//		brenderable.setOrigin(x, y);
//		this.addPositionedRenderable(brenderable, true);
//	}
//
//	private final void addToLeftMargin(BoundableRenderable brenderable, int x, int y, int leftOffset, boolean export) {
//		int width = brenderable.getWidth();
//		int height = brenderable.getHeight();
//		// Update maxY and maxY if necessary
//		if(x + width > this.maxX) {
//			this.maxX = x + width;
//		}
//		LineMargin newMargin = new LineMargin(this.leftMargin, y + height, leftOffset + width);
//		this.leftMargin = newMargin;
//		this.checkLineOverflow();
//		if(export && this.scheduleAddInContainingViewport(this, brenderable, x, y, -1)) {
//			// Must try parent viewport. Consider an aligned block
//			// bigger than the current viewport which must also
//			// become a margin for a paragraph in the parent viewport
//			// and sibling paragraphs.
//			return;
//		}
//		int newY = y + height;
//		this.checkY(newY);
//		if(newY > this.maxY) {
//			this.maxY = newY;
//		}
//		//Should only set origin, not size.
//		//brenderable.setBounds(x, y, width, height);
//		brenderable.setOrigin(x, y);
//		this.addPositionedRenderable(brenderable, true);
//	}

	private FloatingBounds floatBounds = null;
	
	private final void positionFloat(DelayedPair pair) {		
		BoundableRenderable brenderable = pair.child;
		//OPTIMIZE: Point translation done twice.
		Point localPoint = brenderable == this ? new Point(pair.x, pair.y) : this.translateDescendentPoint(brenderable.getOriginalOrCurrentParent(), pair.x, pair.y);
		int width = brenderable.getWidth();
		int height = brenderable.getHeight();
		int y = localPoint.y;
		int newY = y + height;
		this.checkY(newY);
		if(newY > this.maxY) {
			this.maxY = newY;
		}
		int alignment = pair.alignment;
		FloatingBounds prevFloatBounds = this.floatBounds;
		int x;
		Insets paddingInsets = this.paddingInsets;
		int left = paddingInsets == null ? 0 : paddingInsets.left;
		if(prevFloatBounds != null) {
			x = alignment > 0 ? left + this.availContentWidth - prevFloatBounds.getRight(y) - width : left + prevFloatBounds.getLeft(y);
		}
		else {
			x = alignment > 0 ? left + this.availContentWidth - width : left;
		}
		if(x < left) {
			x = left;
		}
		int effectiveWidth = width;
		if(alignment > 0) {
			if(localPoint.x < x) {
				effectiveWidth += (x - localPoint.x);
				x = localPoint.x;
			}
		}
		else {
			if(localPoint.x > x) {
				effectiveWidth += (localPoint.x - x);
				x = localPoint.x;
			}
		}
		FloatingViewportBounds newFloatBounds = new FloatingViewportBounds(prevFloatBounds, alignment, y, effectiveWidth, height);
		this.floatBounds = newFloatBounds;
		brenderable.setOrigin(x, y);
		if(x + width > this.maxX) {
			this.maxX = x + width;
		}
	}

	private final void addFloat(DelayedPair pair) {		
		BoundableRenderable brenderable = pair.child;
		this.addPositionedRenderable(brenderable, true);
	}
	
	
//	private boolean scheduleAddInContainingViewport(RBlockViewport originalTarget, BoundableRenderable renderable, int x, int y, int alignment) {
//		// Always succeeds. It gets reimported in the local
//		// viewport if it turns out it can't be exported up.
//		this.addExportedRenderable(originalTarget, renderable, x, y, alignment);
//		return true;
//	}

	private void scheduleAbsDelayedPair(BoundableRenderable renderable, int x, int y, int alignment) {
		// It gets reimported in the local
		// viewport if it turns out it can't be exported up.
		RenderableContainer container = this.container;
		for(;;) {
			if(container instanceof Renderable) {
				Object node = ((Renderable) container).getModelNode();
				if(node instanceof HTMLElementImpl) {
					HTMLElementImpl element = (HTMLElementImpl) node;
					CSS2PropertiesImpl style = element.getCurrentStyle();
					if(style != null) {
						String position = style.getPosition();
						if(position != null && (position.equalsIgnoreCase("absolute") || position.equalsIgnoreCase("fixed") || position.equalsIgnoreCase("relative"))) {
							break;
						}
					}
					RenderableContainer newContainer = container.getParentContainer();
					if(newContainer == null) {
						break;
					}
					container = newContainer;
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		DelayedPair pair = new DelayedPair(alignment, container, renderable, x, y);
		this.container.addDelayedPair(pair);
	}

	private void scheduleFloatDelayedPair(BoundableRenderable renderable, int x, int y, int alignment) {
		// It gets reimported in the local
		// viewport if it turns out it can't be exported up.
		RenderableContainer targetContainer = this.container;
		RBlockViewport targetViewport = this;
		for(;;) {
			if(targetViewport.isPositionedElsewhere()) {
				break;
			}
			if(!(targetContainer instanceof RBlock)) {
				break;
			}
			RBlock rblock = (RBlock) targetContainer;
			Object parent = rblock.getOriginalOrCurrentParent();
			if(!(parent instanceof RBlockViewport)) {
				break;
			}
			targetViewport = (RBlockViewport) parent;
			targetContainer = targetViewport.container;
		}
		DelayedPair pair = new DelayedPair(alignment, targetContainer, renderable, x, y);
		this.container.addDelayedPair(pair);
	}

	void importNonFloat(DelayedPair pair) {
		BoundableRenderable r = pair.child;
		r.setOrigin(pair.x, pair.y);
		this.addPositionedRenderable(r, false);
		// Size of block does not change - it's
		// set in stone?
	}

//	boolean addToExportedRenderables(ExportedRenderable er) {
//		// Always succeeds. It gets reimported in the local
//		// viewport if it turns out it can't be exported up.
//		this.addExportedRenderable(er);
//		return true;
//	}

//	private final void addExportedRenderable(RBlockViewport originalTarget, BoundableRenderable renderable, int x, int y, int align) {
//		// Note that exported renderables currently require
//		// the parent to be a block contained in a viewport.
//		Collection er = this.exportedRenderables;
//		if(er == null) {
//			er = new LinkedList();
//			this.exportedRenderables = er;
//		}
//		er.add(new ExportedRenderable(originalTarget, renderable, x, y, align));
//	}

//	private final void addExportedRenderable(ExportedRenderable er) {
//		// Note that exported renderables currently require
//		// the parent to be a block contained in a viewport.
//		Collection ers = this.exportedRenderables;
//		if(ers == null) {
//			ers = new LinkedList();
//			this.exportedRenderables = ers;
//		}
//		ers.add(er);
//	}

//	final Iterator getExportedRenderables() {
//		Collection er = this.exportedRenderables;
//		return er == null ? null : er.iterator();
//	}
	
	private final void addPositionedRenderable(BoundableRenderable renderable, boolean verticalAlignable) {
		// Expected to be called only in GUI thread.
		SortedSet others = this.positionedRenderables;
		if(others == null) {
			others = new TreeSet(new ZIndexComparator());
			this.positionedRenderables = others;
		}
		others.add(new PositionedRenderable(renderable, verticalAlignable, this.otherOrdinal++));
		renderable.setParent(this);
		if(renderable instanceof RUIControl) {
			this.container.add(((RUIControl) renderable).widget.getComponent());
		}
	}
	
	public int getFirstLineHeight() {
		ArrayList renderables = this.seqRenderables;
		int size = renderables.size();
		if(size == 0) {
			return 0;
		}
		for(int i = 0; i < size; i++) {
			BoundableRenderable br = (BoundableRenderable) renderables.get(0);
			int height = br.getHeight();
			if(height != 0) {
				return height;
			}
		}
		// Not found!!
		return 1;
	}

	public int getFirstBaselineOffset() {
		ArrayList renderables = this.seqRenderables;
		Iterator i = renderables.iterator();
		while(i.hasNext()) {
			Object r = i.next();
			if(r instanceof RLine) {
				int blo =((RLine) r).getBaselineOffset();
				if(blo != 0) {
					return blo;
				}
			}
			else if(r instanceof RBlock) {
				RBlock block = (RBlock) r;
				if(block.getHeight() > 0) {
					Insets insets = block.getInsets(false, false);
					Insets paddingInsets = block.getPaddingInsets(block.getModelNode().getRenderState());
					return block.getFirstBaselineOffset() + insets.top + (paddingInsets == null ? 0 : paddingInsets.top);
				}					
			}
		}
		return 0;
	}
	
    //----------------------------------------------------------------

	public RenderableSpot getLowestRenderableSpot(int x, int y) {
	    BoundableRenderable br = this.getRenderable(new Point(x, y));
	    if(br != null) {
	    	return br.getLowestRenderableSpot(x - br.getX(), y - br.getY());
	    }
	    else {
	    	return new RenderableSpot(this, x, y);
	    }
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseClick(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMouseClick(MouseEvent event, int x, int y) {
		Iterator i = this.getRenderables(new Point(x, y));
		if(i != null) {
			while(i.hasNext()) {
			    BoundableRenderable br = (BoundableRenderable) i.next();
			    if(br != null) {
			    	Rectangle bounds = br.getBounds();
			    	if(!br.onMouseClick(event, x - bounds.x, y - bounds.y)) {
			    		return false;
			    	}
			    }
			}
		}
		return true;
	}

	public boolean onDoubleClick(MouseEvent event, int x, int y) {
		Iterator i = this.getRenderables(new Point(x, y));
		if(i != null) {
			while(i.hasNext()) {
			    BoundableRenderable br = (BoundableRenderable) i.next();
			    if(br != null) {
			    	Rectangle bounds = br.getBounds();
			    	if(!br.onDoubleClick(event, x - bounds.x, y - bounds.y)) {
			    		return false;
			    	}
			    }
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseDisarmed(java.awt.event.MouseEvent)
	 */
	public boolean onMouseDisarmed(MouseEvent event) {
		BoundableRenderable br = this.armedRenderable;
		if(br != null) {
			try {
				return br.onMouseDisarmed(event);
			} finally {
				this.armedRenderable = null;
			}
		}
		else {
			return true;
		}
	}

	private BoundableRenderable armedRenderable;
	
	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMousePressed(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMousePressed(MouseEvent event, int x, int y) {
		Iterator i = this.getRenderables(new Point(x, y));
		if(i != null) {
			while(i.hasNext()) {
			    BoundableRenderable br = (BoundableRenderable) i.next();
			    if(br != null) {
			    	Rectangle bounds = br.getBounds();
			    	if(!br.onMousePressed(event, x - bounds.x, y - bounds.y)) {
				    	this.armedRenderable = br;
			    		return false;
			    	}
			    }
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.xamjwg.html.renderer.BoundableRenderable#onMouseReleased(java.awt.event.MouseEvent, int, int)
	 */
	public boolean onMouseReleased(MouseEvent event, int x, int y) {
		Iterator i = this.getRenderables(new Point(x, y));
		if(i != null) {
			while(i.hasNext()) {
			    BoundableRenderable br = (BoundableRenderable) i.next();
			    if(br != null) {
			    	Rectangle bounds = br.getBounds();
			    	if(!br.onMouseReleased(event, x - bounds.x, y - bounds.y)) {
				    	BoundableRenderable oldArmedRenderable = this.armedRenderable;
				    	if(oldArmedRenderable != null && br != oldArmedRenderable) {
				    		oldArmedRenderable.onMouseDisarmed(event);
				    		this.armedRenderable = null;
				    	}
			    		return false;
			    	}
			    }
			}
		}
    	BoundableRenderable oldArmedRenderable = this.armedRenderable;
    	if(oldArmedRenderable != null) {
    		oldArmedRenderable.onMouseDisarmed(event);
    		this.armedRenderable = null;
    	}	    	
    	return true;
	}

	public void paint(Graphics g) {
		Rectangle clipBounds = g.getClipBounds();
		Iterator i = this.getRenderables(clipBounds);
		int renderableCount = 0;
		while(i.hasNext()) {
			renderableCount++;
			Object robj = i.next();
			
			// The expected behavior in HTML is for boxes
			// not to be clipped unless overflow=hidden.
			
//			if(robj instanceof RElement) {
//				// Ensure RElement's are clipped
//				RElement relement = (RElement) robj;
//				Graphics newG = g.create(relement.getX(), relement.getY(), relement.getWidth(), relement.getHeight());
//				try {
//					relement.paint(newG);
//				} finally {
//					newG.dispose();
//				}
//			}
			if(robj instanceof BoundableRenderable) {
				BoundableRenderable renderable = (BoundableRenderable) robj;
				renderable.paintTranslated(g);
				//numRenderables++;
			}
			else {
				((Renderable) robj).paint(g);
			}
		}
	}
	
	//----------------------------------------------------------------

	private static class NopLayout implements MarkupLayout {
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
		}
	}

	private static class NoScriptLayout implements MarkupLayout {
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			UserAgentContext ucontext = bodyLayout.userAgentContext;
			if(!ucontext.isScriptingEnabled()) {
				bodyLayout.layoutMarkup(container, containerSize, insets, markupElement);
			}
			else {
				//NOP
			}
		}
	}

	private static class ChildrenLayout implements MarkupLayout {
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			bodyLayout.layoutChildren(container, containerSize, insets, markupElement);
		}
	}

	private static class HLayout extends CommonLayout {
		
		public HLayout(int fontSize) {
			super(DISPLAY_BLOCK);
		}
		
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			if(!bodyLayout.skipParagraphBreakBefore) {
				RLine currentLine = bodyLayout.currentLine;
				if(currentLine.width == 0) {
					bodyLayout.addLineBreak(container, containerSize, insets, markupElement);
				}
				else {
					bodyLayout.addParagraphBreak(container, containerSize, insets, markupElement);
				}
				bodyLayout.skipParagraphBreakBefore = true;
			}
			super.layoutMarkup(bodyLayout, container, containerSize, insets, markupElement);
			if(markupElement.hasChildNodes()) {
				bodyLayout.addLineBreak(container, containerSize, insets, markupElement);
				bodyLayout.skipParagraphBreakBefore = true;				
			}
		}
	}

	private static class PLayout extends CommonLayout {
		public PLayout() {
			super(DISPLAY_BLOCK);
		}
		
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			if(!bodyLayout.skipParagraphBreakBefore) {
				RLine currentLine = bodyLayout.currentLine;
				if(currentLine.width == 0) {
					bodyLayout.addLineBreak(container, containerSize, insets, markupElement);
				}
				else {
					bodyLayout.addParagraphBreak(container, containerSize, insets, markupElement);
				}
				bodyLayout.skipParagraphBreakBefore = true;
			}
			super.layoutMarkup(bodyLayout, container, containerSize, insets, markupElement);
			if(markupElement.hasChildNodes()) {
				bodyLayout.addLineBreak(container, containerSize, insets, markupElement);
				bodyLayout.skipParagraphBreakBefore = true;				
			}
		}
	}

	private static class ListItemLayout extends CommonLayout {
		public ListItemLayout() {
			super(DISPLAY_LIST_ITEM);
		}
	}

	private static class BrLayout implements MarkupLayout {
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			bodyLayout.addLineBreak(container, containerSize, insets, markupElement);
		}
	}

	private static class HrLayout implements MarkupLayout {
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			bodyLayout.layoutHr(container, containerSize, insets, markupElement);
		}
	}

	private static class TableLayout extends CommonLayout {
		public TableLayout() {
			super(DISPLAY_TABLE);
		}
	}

	private static class CommonBlockLayout extends CommonLayout {
		public CommonBlockLayout() {
			super(DISPLAY_BLOCK);
		}		
	}

	//---------------------------------------------------------------------------
	
	private static class DivLayout extends CommonLayout {
		public DivLayout() {
			super(DISPLAY_BLOCK);
		}
	}
	
	private static class BlockQuoteLayout extends CommonLayout {
		public java.awt.Insets getDefaultMarginInsets() {
			return new Insets(0, 36, 0, 0);
		}

		public BlockQuoteLayout() {
			super(DISPLAY_BLOCK);
		}
	}

	private static class SpanLayout extends CommonLayout {
		public SpanLayout() {
			super(DISPLAY_INLINE);
		}
	}

	private static class EmLayout extends CommonLayout {
		public EmLayout() {
			super(DISPLAY_INLINE);
		}
		
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			super.layoutMarkup(bodyLayout, container, containerSize, insets, markupElement);
		}
	}
	
	private static class ULayout extends CommonLayout {
		public ULayout() {
			super(DISPLAY_INLINE);
		}
		
		/* (non-Javadoc)
		 * @see org.xamjwg.html.renderer.MarkupLayout#layoutMarkup(java.awt.Container, java.awt.Insets, org.xamjwg.html.domimpl.HTMLElementImpl)
		 */
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			super.layoutMarkup(bodyLayout, container, containerSize, insets, markupElement);
		}
	}

	private static class StrikeLayout extends CommonLayout {
		public StrikeLayout() {
			super(DISPLAY_INLINE);
		}
	}

	private static class StrongLayout extends CommonLayout {
		public StrongLayout() {
			super(DISPLAY_INLINE);
		}
	}
	
	private static class AnchorLayout extends CommonLayout {
		public AnchorLayout() {
			super(DISPLAY_INLINE);
		}
	}
	
	private static class ObjectLayout extends CommonWidgetLayout {
		private boolean tryToRenderContent;
		
		/**
		 * @param tryToRenderContent If the object is unknown, content is rendered as HTML.
		 * @param usesAlignAttribute
		 */
		public ObjectLayout(boolean tryToRenderContent, boolean usesAlignAttribute) {
			super(ADD_INLINE, usesAlignAttribute);
			this.tryToRenderContent = tryToRenderContent;
		}
		
		/**
		 * Must use this ThreadLocal because
		 * an ObjectLayout instance is shared
		 * across renderers.
		 */
		private final ThreadLocal htmlObject = new ThreadLocal();

		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			HtmlObject ho = bodyLayout.rendererContext.getHtmlObject(markupElement);
			if(ho == null && this.tryToRenderContent) {
				// Don't know what to do with it - render contents.
				bodyLayout.layoutMarkup(container, containerSize, insets, markupElement);
			}
			else if (ho != null) {
				this.htmlObject.set(ho);
				super.layoutMarkup(bodyLayout, container, containerSize, insets, markupElement);
			}
		}

		protected RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement) {
			HtmlObject ho = (HtmlObject) this.htmlObject.get();
			UIControl uiControl = new UIControlWrapper(ho);
			RUIControl ruiControl = new RUIControl(markupElement, uiControl, bodyLayout.container, bodyLayout.frameContext, bodyLayout.userAgentContext);
			return ruiControl;
		}
	}
	
	private static class ImgLayout extends CommonWidgetLayout {
		public ImgLayout() {
			super(ADD_INLINE, true);
		}

		protected RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement) {
			UIControl control = new ImgControl((HTMLImageElementImpl) markupElement);
			return new RImgControl(markupElement, control, bodyLayout.container, bodyLayout.frameContext, bodyLayout.userAgentContext);
		}
	}
	
	private static class InputLayout2 extends CommonWidgetLayout {
		public InputLayout2() {
			super(ADD_INLINE, true);			
		}

		protected RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement) {			
			HTMLBaseInputElement bie = (HTMLBaseInputElement) markupElement;
			BaseInputControl uiControl = bodyLayout.createInputControl(bie);
			if(uiControl == null) {
				return null;
			}
			bie.setInputContext(uiControl);
			return new RUIControl(markupElement, uiControl, bodyLayout.container, bodyLayout.frameContext, bodyLayout.userAgentContext);
		}
	}

	private static class SelectLayout extends CommonWidgetLayout {
		public SelectLayout() {
			super(ADD_INLINE, true);			
		}
		
		protected RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement) {			
			HTMLBaseInputElement bie = (HTMLBaseInputElement) markupElement;
			BaseInputControl uiControl = new InputSelectControl(bie);
			bie.setInputContext(uiControl);
			return new RUIControl(markupElement, uiControl, bodyLayout.container, bodyLayout.frameContext, bodyLayout.userAgentContext);
		}
	}

	private static class TextAreaLayout2 extends CommonWidgetLayout {
		public TextAreaLayout2() {
			super(ADD_INLINE, true);			
		}
		
		protected RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement) {			
			HTMLBaseInputElement bie = (HTMLBaseInputElement) markupElement;
			BaseInputControl control = new InputTextAreaControl(bie);
			bie.setInputContext(control);
			return new RUIControl(markupElement, control, bodyLayout.container, bodyLayout.frameContext, bodyLayout.userAgentContext);
		}
	}

	private static class IFrameLayout extends CommonWidgetLayout {
		public IFrameLayout() {
			super(ADD_INLINE, true);			
		}
		
		protected RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement) {			
			BrowserFrame frame = bodyLayout.rendererContext.createBrowserFrame();	
			((HTMLIFrameElementImpl) markupElement).setBrowserFrame(frame);
			UIControl control = new BrowserFrameUIControl(markupElement, frame);
			return new RUIControl(markupElement, control, bodyLayout.container, bodyLayout.frameContext, bodyLayout.userAgentContext);
		}
	}

	//------------------------------------------------------------------------
	
	/**
	 * This is layout common to elements that render themselves,
	 * except RBlock, RTable and RList.
	 */
	private static abstract class CommonWidgetLayout implements MarkupLayout {
		protected static final int ADD_INLINE = 0;
		protected static final int ADD_AS_BLOCK = 1;
		private final int method;
		private final boolean useAlignAttribute;
		
		public CommonWidgetLayout(int method, boolean usesAlignAttribute) {
			this.method = method;
			this.useAlignAttribute = usesAlignAttribute;
		}
		
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			CSS2PropertiesImpl style = markupElement.getCurrentStyle();
			if(style != null) {
				String display = style.getDisplay();
				if(display != null && "none".equalsIgnoreCase(display)) {
					return;
				}
			}
			UINode node = markupElement.getUINode();
			RElement renderable = null;
			if(node == null) {
				renderable = this.createRenderable(bodyLayout, markupElement);
				if(renderable == null) {
					if(logger.isLoggable(Level.INFO)) {
						logger.info("layoutMarkup(): Don't know how to render " + markupElement + ".");
					}
					return;
				}
				markupElement.setUINode((UINode) renderable);
			}
			else {
				renderable = (RElement) node;
			}
			int availWidth = bodyLayout.availContentWidth;
			int heightAvailToRenderable = bodyLayout.availContentHeight;
			renderable.layout(availWidth, heightAvailToRenderable, false, false);
			switch(this.method) {
			case ADD_INLINE:
				bodyLayout.addRenderableToLineCheckStyle(container, containerSize, insets, renderable, markupElement, this.useAlignAttribute);
				break;
			case ADD_AS_BLOCK:
				bodyLayout.addAsSeqBlockCheckStyle(containerSize, insets, renderable, markupElement, this.useAlignAttribute);
				break;
			}
		}

		protected abstract RElement createRenderable(RBlockViewport bodyLayout, HTMLElementImpl markupElement);
	}
	
	private static abstract class CommonLayout implements MarkupLayout {
		protected static final int DISPLAY_NONE = 0;
		protected static final int DISPLAY_INLINE = 1;
		protected static final int DISPLAY_BLOCK = 2;
		protected static final int DISPLAY_LIST_ITEM = 3;
		protected static final int DISPLAY_TABLE_ROW = 4;
		protected static final int DISPLAY_TABLE_CELL = 5;		
		protected static final int DISPLAY_TABLE = 6;				
		
		private final int display;
		
		public CommonLayout(int defaultDisplay) {
			this.display = defaultDisplay;
		}

		/**
		 * The default margin insets for a block. Only used
		 * with DISPLAY_BLOCK.
		 */
		public java.awt.Insets getDefaultMarginInsets() {
			return null;
		}
		
		public void layoutMarkup(RBlockViewport bodyLayout, RenderableContainer container, Dimension containerSize, Insets insets, HTMLElementImpl markupElement) {
			RenderState rs = markupElement.getRenderState();
			int display = rs == null ? this.display : rs.getDisplay();
			switch(display) {
				case DISPLAY_NONE:
					// skip it completely.
					UINode node = markupElement.getUINode();
					if(node instanceof BaseBoundableRenderable) {
						// This is necessary so that if the element is made
						// visible again, it can be invalidated.
						((BaseBoundableRenderable) node).markLayoutValid();
					}
					break;
				case DISPLAY_BLOCK:
					bodyLayout.layoutRBlock(container, containerSize, insets, markupElement, this.getDefaultMarginInsets());
					break;
				case DISPLAY_LIST_ITEM:
					String tagName = markupElement.getTagName();
					if("UL".equalsIgnoreCase(tagName) || "OL".equalsIgnoreCase(tagName)) {
						bodyLayout.layoutList(container, containerSize, insets, markupElement);
					}
					else {
						bodyLayout.layoutListItem(container, containerSize, insets, markupElement);
					}
					break;
				case DISPLAY_TABLE:
					bodyLayout.layoutRTable(container, containerSize, insets, markupElement);
					break;
				default:
					// Assume INLINE
					bodyLayout.layoutMarkup(container, containerSize, insets, markupElement);
					break;
				//TODO: tables, lists, etc.
			}
		}		
	}
	
	public boolean isContainedByNode() {
		return false;
	}

	public String toString() {
		return "RBlockViewport[node=" + this.modelNode + "]";
	}
}
