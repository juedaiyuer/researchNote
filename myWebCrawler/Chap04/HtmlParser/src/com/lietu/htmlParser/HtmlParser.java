package com.lietu.htmlParser;
import java.net.URL;
import java.net.URLEncoder;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.htmlparser.visitors.NodeVisitor;
import org.htmlparser.visitors.ObjectFindingVisitor;

public class HtmlParser  {  

	//  private static final Logger logger = Logger.getLogger(htmlParser.class);  

	public HtmlParser(String name) {  
		//    super(name);  
	}  
	public static void main(String[] args) {
		//	testImageVisitor();
		//testLinkCSS();
		try
		{
			// 根据参数查询相关的信息（公司名称）
			String searchWord = URLEncoder.encode("website:www HOME DEPOT USA, INC","utf-8");
			String searchURL = "http://www.google.com/search?q=" + searchWord;// url
			
//			testLinkBean(searchURL);
//			testImageVisitor();
			testNodeFilter();
			
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

	/* 
	 * 测试ObjectFindVisitor的用法 
	 */  
	public static void testImageVisitor() {  
		try {  
			ImageTag imgLink;  
			ObjectFindingVisitor visitor = new ObjectFindingVisitor(ImageTag.class);  
			Parser parser = new Parser(); 
			parser.setURL("http://www.google.com");  
			parser.setEncoding(parser.getEncoding());  
			parser.visitAllNodesWith(visitor);  
			Node[] nodes = visitor.getTags();  
			for (int i = 0; i < nodes.length; i++) {  
				imgLink = (ImageTag) nodes[i];  
				System.out.println(nodes[i]);
				System.out.println("testImageVisitor() ImageURL = " + imgLink.getImageURL());
				System.out.println("testImageVisitor() ImageLocation = " + imgLink.extractImageLocn());  
				System.out.println("testImageVisitor() SRC = " + imgLink.getAttribute("SRC"));
			}  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试TagNameFilter用法 
	 */  
	public static  void testNodeFilter() {  
		try {  
			NodeFilter filter = new TagNameFilter("IMG");  
			Parser parser = new Parser();  
			parser.setURL("http://www.google.com");  
			parser.setEncoding(parser.getEncoding());  
			NodeList list = parser.extractAllNodesThatMatch(filter);  
			for (int i = 0; i < list.size(); i++) {  
				System.out.println(list.elementAt(i).toHtml());
			}  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试NodeClassFilter用法 
	 */  
	public static void testLinkTag(String path) {  
		try {  

			NodeFilter filter = new NodeClassFilter(LinkTag.class);  
			Parser parser = new Parser();  
			parser.setURL(path);  
			parser.setEncoding(parser.getEncoding());  
			NodeList list = parser.extractAllNodesThatMatch(filter);  
			for (int i = 0; i < list.size(); i++) {  
				LinkTag node = (LinkTag) list.elementAt(i); 
				System.out.println("testLinkTag() Link is :" + node.extractLink());

			}  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  

	}  

	/* 
	 * 测试<link href="  text=’text/css’ rel=’stylesheet’ />用法 
	 */  
	public static  void testLinkCSS() {  
		try {  

			Parser parser = new Parser();

			parser.setInputHTML("<head><title>Link Test</title>"  
					+ "<link rel=prefetch href='http://www.homedepot.com/'>"  
					+ "</head>"  
					+ "<body>");  
			parser.setEncoding(parser.getEncoding());  

			for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {  
				Node node = e.nextNode();  
				System.out.println("link "+node.getText() + node.getClass());
			}  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试OrFilter的用法 
	 */  
	public void testOrFilter() {  
		NodeFilter inputFilter = new NodeClassFilter(InputTag.class);  
		NodeFilter selectFilter = new NodeClassFilter(SelectTag.class);  

		NodeList nodeList = null;  

		try {  
			Parser parser = new Parser();  
			parser  
			.setInputHTML("<head><title>OrFilter Test</title>"  
					+ "<link href='/test01/css.css' text='text/css' rel='stylesheet' />"  
					+ "<link href='/test02/css.css' text='text/css' rel='stylesheet' />"  
					+ "</head>"  
					+ "<body>"  
					+ "<input type='text' value='text1′ name='text1′/>"  
					+ "<input type='text' value='text2′ name='text2′/>"  
					+ "<select><option id='1′>1</option><option id='2′>2</option><option id='3′></option></select>"  
					+ "<a href='http://www.yeeach.com'>yeeach.com</a>" + "</body>");  

			parser.setEncoding(parser.getEncoding());  
			OrFilter lastFilter = new OrFilter();  
			lastFilter.setPredicates(new NodeFilter[] { selectFilter, inputFilter });  
			nodeList = parser.parse(lastFilter);  
			for (int i = 0; i <= nodeList.size(); i++) {  
				if (nodeList.elementAt(i) instanceof InputTag) {  
					InputTag tag = (InputTag) nodeList.elementAt(i);  
					System.out.println("OrFilter tag name is :" + tag.getTagName() + " ,tag value is:"  
							+ tag.getAttribute("value"));
				}  
				if (nodeList.elementAt(i) instanceof SelectTag) {  
					SelectTag tag = (SelectTag) nodeList.elementAt(i);  
					NodeList list = tag.getChildren();  

					for (int j = 0; j < list.size(); j++) {  
						OptionTag option = (OptionTag) list.elementAt(j);
						System.out.println("OrFilter Option" + option.getOptionText());

					}  

				}  
			}  

		} catch (ParserException e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试对<table><tr><td></td></tr></table>的解析 
	 */  
	public static  void testTable() {  
		Parser myParser;  
		NodeList nodeList = null;  
		myParser = Parser.createParser("<body> " + "<table id='table1′ >"  
				+ "<tr><td>1-11</td><td>1-12</td><td>1-13</td>"  
				+ "<tr><td>1-21</td><td>1-22</td><td>1-23</td>"  
				+ "<tr><td>1-31</td><td>1-32</td><td>1-33</td></table>" + "<table id='table2′ >"  
				+ "<tr><td>2-11</td><td>2-12</td><td>2-13</td>"  
				+ "<tr><td>2-21</td><td>2-22</td><td>2-23</td>"  
				+ "<tr><td>2-31</td><td>2-32</td><td>2-33</td></table>" + "</body>", "GBK");  
		NodeFilter tableFilter = new NodeClassFilter(TableTag.class);  
		OrFilter lastFilter = new OrFilter();  
		lastFilter.setPredicates(new NodeFilter[] { tableFilter });  
		try {  
			nodeList = myParser.parse(lastFilter);  
			for (int i = 0; i <= nodeList.size(); i++) {  
				if (nodeList.elementAt(i) instanceof TableTag) {  
					TableTag tag = (TableTag) nodeList.elementAt(i);  
					TableRow[] rows = tag.getRows();  

					for (int j = 0; j < rows.length; j++) {  
						TableRow tr = (TableRow) rows[j];  
						TableColumn[] td = tr.getColumns();  
						for (int k = 0; k < td.length; k++) {  
							System.out.println("<td>" + td[k].toPlainTextString());

						}  

					}  

				}  
			}  

		} catch (ParserException e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试NodeVisitor的用法，遍历所有节点 
	 */  
	public static void testVisitorAll() {  
		try {  
			Parser parser = new Parser();  
			parser.setURL("http://www.google.com");  
			parser.setEncoding(parser.getEncoding());  
			NodeVisitor visitor = new NodeVisitor() {  
				public void visitTag(Tag tag) {  
					System.out.println("testVisitorAll()  Tag name is :" + tag.getTagName() + " \n Class is :"  
							+ tag.getClass());
				}  

			};  

			parser.visitAllNodesWith(visitor);  
		} catch (ParserException e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试对指定Tag的NodeVisitor的用法 
	 */  
	public static void testTagVisitor() {  
		try {  

			Parser parser = new Parser("<head><title>dddd</title>"  
					+ "<link href='/test01/css.css' text='text/css' rel='stylesheet' />"  
					+ "<link href='/test02/css.css' text='text/css' rel='stylesheet' />" + "</head>"  
					+ "<body>" + "<a href='http://www.yeeach.com'>yeeach.com</a>" + "</body>");  
			NodeVisitor visitor = new NodeVisitor() {  
				public void visitTag(Tag tag) {  
					if (tag instanceof HeadTag) {  
						System.out.println("visitTag() HeadTag : Tag name is :" + tag.getTagName()  
								+ " \n Class is :" + tag.getClass() + "\n Text is :" + tag.getText());

					} else if (tag instanceof TitleTag) { 
						System.out.println("visitTag() TitleTag : Tag name is :" + tag.getTagName()  
								+ " \n Class is :" + tag.getClass() + "\n Text is :" + tag.getText());


					} else if (tag instanceof LinkTag) {  
						System.out.println("visitTag() LinkTag : Tag name is :" + tag.getTagName()  
								+ " \n Class is :" + tag.getClass() + "\n Text is :" + tag.getText()  
								+ " \n getAttribute is :" + tag.getAttribute("href"));

					} else {  
						System.out.println("visitTag() : Tag name is :" + tag.getTagName() + " \n Class is :"  
								+ tag.getClass() + "\n Text is :" + tag.getText());

					}  

				}  

			};  

			parser.visitAllNodesWith(visitor);  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试HtmlPage的用法 
	 */  
	public void testHtmlPage() {  
		String inputHTML = "<html>" + "<head>"  
		+ "<title>Welcome to the HTMLParser website</title>" + "</head>" + "<body>"  
		+ "Welcome to HTMLParser" + "<table id='table1′ >"  
		+ "<tr><td>1-11</td><td>1-12</td><td>1-13</td>"  
		+ "<tr><td>1-21</td><td>1-22</td><td>1-23</td>"  
		+ "<tr><td>1-31</td><td>1-32</td><td>1-33</td></table>" + "<table id='table2′ >"  
		+ "<tr><td>2-11</td><td>2-12</td><td>2-13</td>"  
		+ "<tr><td>2-21</td><td>2-22</td><td>2-23</td>"  
		+ "<tr><td>2-31</td><td>2-32</td><td>2-33</td></table>" + "</body>" + "</html>";  
		Parser parser = new Parser();  
		try {  
			parser.setInputHTML(inputHTML);  
			parser.setEncoding(parser.getURL());  
			HtmlPage page = new HtmlPage(parser);  
			parser.visitAllNodesWith(page);  
			System.out.println("testHtmlPage -title is :" + page.getTitle());

			NodeList list = page.getBody();  

			for (NodeIterator iterator = list.elements(); iterator.hasMoreNodes();) {  
				Node node = iterator.nextNode();  
				System.out.println("testHtmlPage -node  is :" + node.toHtml());

			}  

		} catch (ParserException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}  
	}  

	/* 
	 * 测试LinkBean的用法 
	 */  
	public static void testLinkBean(String path) {  
		Parser parser = new Parser(); 
		LinkBean linkBean = new LinkBean();  
		linkBean.setURL(path);  
		URL[] urls = linkBean.getLinks();  

		for (int i = 0; i < urls.length; i++) {  
			URL url = urls[i];  
			System.out.println("testLinkBean() -url  is :" + url);
		}  

	}  
	

}  
