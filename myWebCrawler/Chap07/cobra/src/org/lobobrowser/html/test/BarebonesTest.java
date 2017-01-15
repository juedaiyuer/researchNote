package org.lobobrowser.html.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.lobobrowser.html.*;
import org.lobobrowser.html.domimpl.HTMLBaseInputElement;
import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.lobobrowser.html.domimpl.HTMLHtmlElementImpl;
import org.lobobrowser.html.domimpl.UINode;
import org.lobobrowser.html.gui.*;
import org.lobobrowser.html.parser.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLElement;
import org.xml.sax.InputSource;

import javax.swing.*;
import java.awt.*;

/**
 * Minimal rendering example: google.com.
 */
public class BarebonesTest {
	public static void main(String[] args) throws Exception {
		String uri = //"http://www.google.com";
			//"http://blogs.conchango.com/julianrharris/archive/2007/09/11/How-to-access-InnerHtml-and-InnerText-properties-of-an-html-_3C00_select_3E003C00_option_3E00_-tag-when-using-the-HtmlAgilityKit.aspx";
		//"http://www.lietu.com";
			"http://search.zhaopin.com/jobseeker/job_results.asp?SchJobLocationSv=&CurPage=5&SchSortType=&SchJobTypeAdv=&SchCityAdv=&EmplType=&SchWorkingTimeAdv=&SchSalaryFromAdv=&SchSalaryToAdv=&SchIncMianYiAdv=&SchEduLevelAdv=&SchCompIndAdv=&SchCompInd=&SchKeyWordNav=Web+%E5%B7%A5%E7%A8%8B%E5%B8%88&SchCompType=&SchAdv=&switchMore=&PublishDate=&industry=&JobLocation=&sortby=&SchJobType=&subJobType=&totalpage=70&vip_type=&sButton=P%3A6&ql=&key_id=&CompID=&suuid=4009_38971.18&KeyWord=Web+%E5%B7%A5%E7%A8%8B%E5%B8%88";
			//String uri = "file:C:\\opt\\XAMJ_Project\\HTML_Renderer\\testing\\table2.html";
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
		DocumentBuilderImpl builder = new DocumentBuilderImpl(rendererContext.getUserAgentContext(), rendererContext);
		final Document document = builder.parse(is);
		//in.close();
		//System.out.println(document.getChildNodes().item(0).getClass());
		//final Node e = document.getChildNodes().item(0);
		//System.out.println(e.getClass().newInstance().getNodeName());;
		
		//System.out.println(o.getOffsetHeight()+" "+o.getOffsetWidth());
		
		// Set the document in the HtmlPanel. This
		// is what lets the document render.
		htmlPanel.setDocument(document, rendererContext);

		// Create a JFrame and add the HtmlPanel to it.
		final JFrame frame = new JFrame();
		frame.getContentPane().add(htmlPanel);
		
		// We pack the JFrame to demonstrate the
		// validity of HtmlPanel's preferred size.
		// Normally you would want to set a specific
		// JFrame size instead.
		
		// This should be done in the GUI dispatch
		// thread since the document is scheduled to
		// be rendered in that thread.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				frame.pack();
				//frame.setVisible(true);
				//FrameSetPanel fsp = ((HtmlPanel)frame.getContentPane().getComponent(0)).getFrameSetPanel();
				//fsp.doLayout();
				print(document," ");
			}
		});
	}

	private static class LocalHtmlRendererContext extends SimpleHtmlRendererContext {
		// Override methods here to implement browser functionality
		public LocalHtmlRendererContext(HtmlPanel contextComponent) {
			super(contextComponent);
		}
	}
	
    public static void print(Node node, String indent)
    {
    	//System.out.println("打印该节点");
        if(
            		node  instanceof HTMLElementImpl )
            {
	            System.out.print(indent);
	            /*System.out.println(node.getNodeName()+ " h:"+
	            		((HTMLElementImpl)node).getOffsetHeight()
	            		+" w:"+
	            		((HTMLElementImpl)node).getOffsetWidth() +
	            		" t:"+ 
	            		((HTMLElementImpl)node).getOffsetTop()+
	            		" l:"+ 
	            		((HTMLElementImpl)node).getOffsetLeft() +
	            		" x:"+((HTMLElementImpl)node).getAlignmentX()+
	            		" y:"+((HTMLElementImpl)node).getAlignmentY());
*/
	            UINode uiNode =((HTMLElementImpl)node).getUINode();
	            if(uiNode!=null)
	            {
	            System.out.println(node.getNodeName()+ " h:"+
	            		uiNode.getBounds()
	            		);
	            }
        	}
            else
            {
            	System.out.println(node.getClass().toString());
            }
        
        //System.out.println("取得第一个孩子节点");
        Node child = node.getFirstChild();
        
        while (child != null)
        {
            print(child, indent+" ");
            child = child.getNextSibling();
            //System.out.println("取得该节点的兄弟节点");
        }
        //System.out.println("打印该节点结束");

    }
}
