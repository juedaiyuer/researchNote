

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;

//正文抽取主程序
public class ExtractContext {
	protected static final String lineSign = System
			.getProperty("line.separator");
	protected static final int lineSign_size = lineSign.length();

	/** 定义系统上下文* */
	public static final ApplicationContext context = new ClassPathXmlApplicationContext(
			new String[] { "newwatch/persistence.xml", "newwatch/biz-util.xml",
					"newwatch/biz-dao.xml" });

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExtractContext console = new ExtractContext();
		ChannelLinkDO c = new ChannelLinkDO();
		c.setEncode("gb2312");
		c.setLink("http://www.qiche.com.cn/files/200712/12016.shtml");
		c.setLinktext("test");
		console.makeContext(c);
	}

	/**
	 * 收集HTML页面信息
	 * 
	 * @param url
	 * @param urlEncode
	 */
	public void makeContext(ChannelLinkDO c) {
		String metakeywords = "<META content={0} name=keywords>";
		String metatitle = "<TITLE>{0}</TITLE>";
		String metadesc = "<META content={0} name=description>";
		String netshap = "<p> 正文快照: 时间{0}</p> ";

		String tempLeate = "<LI class=active><A href=\"{0}\" target=_blank>{1}</A></LI>";
		String crop = "<p><A href=\"{0}\" target=_blank>{1}</A></p> ";

		try {
			String siteUrl = getLinkUrl(c.getLink());
			Parser parser = new Parser(c.getLink());
			parser.setEncoding(c.getEncode());
			for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof Html) {
					PageContext context = new PageContext();
					context.setNumber(0);
					context.setTextBuffer(new StringBuffer());
					// 抓取出内容
					extractHtml(node, context, siteUrl);
					StringBuffer testContext = context.getTextBuffer();
					String srcfilePath = "D:/kuaiso/site/templeate/context.vm";
					String destfilePath = "D:/kuaiso/site/test/test.htm";
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(new FileInputStream(
									srcfilePath), "gbk"));
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(
									destfilePath), "gbk"));
					String lineContext = context.getTextBuffer().toString();
					String line;
					while ((line = reader.readLine()) != null) {
						int start = line.indexOf("#context");
						if (start >= 0) {
							String tempCrop = StringUtils.replace(crop, "{0}", c
									.getLink());
							tempCrop = StringUtils.replace(tempCrop, "{1}",
									"      原文链接： " + c.getLink());
							writer.write(tempCrop + lineSign);
							writer.write(netshap + lineSign);
							writer.write(lineContext + lineSign);
							continue;
						}
						int start1 = line.indexOf("#titledesc");
						if (start1 >= 0) {
							String tempLine = StringUtils.replace(tempLeate,
									"{0}", "test.htm");
							tempLine = StringUtils.replace(tempLine, "{1}",
									"标题:  " + c.getLinktext());

							writer.write(tempLine + lineSign);
							continue;
						}
						int start2 = line.indexOf("#metatitle");
						if (start2 >= 0) {
							metatitle = StringUtils.replace(metatitle, "{0}", c
									.getLinktext());
							writer.write(metatitle + lineSign);
							continue;
						}
						int start3 = line.indexOf("#metadesc");
						if (start3 >= 0) {
							metadesc = StringUtils.replace(metadesc, "{0}", c
									.getLinktext());
							writer.write(metadesc + lineSign);
							continue;
						}
						writer.write(line + lineSign);
					}
					writer.flush();
					writer.close();
					reader.close();
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// 从一个字符串中提取出链接
	private String getLinkUrl(String link) {
		String urlDomaiPattern = "(http://[^/]*?" + "/)(.*?)";
		Pattern pattern = Pattern.compile(urlDomaiPattern,
				Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher matcher = pattern.matcher(link);
		String url = "";
		while (matcher.find()) {
			int start = matcher.start(1);
			int end = matcher.end(1);
			url = link.substring(start, end - 1).trim();
		}
		return url;
	}

	/**
	 * 递归钻取正文信息
	 * 
	 * @param nodeP
	 * @return
	 */
	protected List extractHtml(Node nodeP, PageContext context, String siteUrl)
			throws Exception {
		NodeList nodeList = nodeP.getChildren();
		boolean bl = false;
		if ((nodeList == null) || (nodeList.size() == 0)) {
			if (nodeP instanceof ParagraphTag) {
				ArrayList tableList = new ArrayList();
				StringBuffer temp = new StringBuffer();
				temp.append("<p style=\"TEXT-INDENT: 2em\">");
				tableList.add(temp);
				temp = new StringBuffer();
				temp.append("</p>").append(lineSign);
				tableList.add(temp);
				return tableList;
			}
			return null;
		}
		if ((nodeP instanceof TableTag) || (nodeP instanceof Div)) {
			bl = true;
		}
		if (nodeP instanceof ParagraphTag) {
			ArrayList tableList = new ArrayList();
			StringBuffer temp = new StringBuffer();
			temp.append("<p style=\"TEXT-INDENT: 2em\">");
			tableList.add(temp);
			extractParagraph(nodeP, siteUrl, tableList);
			temp = new StringBuffer();
			temp.append("</p>").append(lineSign);
			tableList.add(temp);
			return tableList;
		}
		ArrayList tableList = new ArrayList();
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof LinkTag) {
					tableList.add(node);
					setLinkImg(node, siteUrl);
				} else if (node instanceof ImageTag) {
					ImageTag img = (ImageTag) node;
					if (img.getImageURL().toLowerCase().indexOf("http://") < 0) {
						img.setImageURL(siteUrl + img.getImageURL());
					} else {
						img.setImageURL(img.getImageURL());
					}
					tableList.add(node);
				} else if (node instanceof ScriptTag
						|| node instanceof StyleTag
						|| node instanceof SelectTag) {
				} else if (node instanceof TextNode) {
					if (node.getText().length() > 0) {
						StringBuffer temp = new StringBuffer();
						String text = collapse(node.getText().replaceAll(
								"&nbsp;", "").replaceAll("　", ""));
						temp.append(text.trim());
						tableList.add(temp);
					}
				} else {
					if (node instanceof TableTag || node instanceof Div) {
						TableValid tableValid = new TableValid();
						isValidTable(node, tableValid);
						if (tableValid.getTrnum() > 2) {
							tableList.add(node);
							continue;
						}
					}
					List tempList = extractHtml(node, context, siteUrl);
					if ((tempList != null) && (tempList.size() > 0)) {
						Iterator ti = tempList.iterator();
						while (ti.hasNext()) {
							tableList.add(ti.next());
						}
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		if ((tableList != null) && (tableList.size() > 0)) {
			if (bl) {
				StringBuffer temp = new StringBuffer();
				Iterator ti = tableList.iterator();
				int wordSize = 0;
				StringBuffer node;
				int status = 0;
				StringBuffer lineStart = new StringBuffer(
						"<p style=\"TEXT-INDENT: 2em\">");
				StringBuffer lineEnd = new StringBuffer("</p>" + lineSign);
				while (ti.hasNext()) {
					Object k = ti.next();
					if (k instanceof LinkTag) {
						if (status == 0) {
							temp.append(lineStart);
							status = 1;
						}
						node = new StringBuffer(((LinkTag) k).toHtml());
						temp.append(node);
					} else if (k instanceof ImageTag) {
						if (status == 0) {
							temp.append(lineStart);
							status = 1;
						}
						node = new StringBuffer(((ImageTag) k).toHtml());
						temp.append(node);
					} else if (k instanceof TableTag) {
						if (status == 0) {
							temp.append(lineStart);
							status = 1;
						}

						node = new StringBuffer(((TableTag) k).toHtml());
						temp.append(node);
					} else if (k instanceof Div) {
						if (status == 0) {
							temp.append(lineStart);
							status = 1;
						}
						node = new StringBuffer(((Div) k).toHtml());
						temp.append(node);
					} else {
						node = (StringBuffer) k;
						if (status == 0) {
							if (node.indexOf("<p") < 0) {
								temp.append(lineStart);
								temp.append(node);
								wordSize = wordSize + node.length();
								status = 1;
							} else {
								temp.append(node);
								status = 1;
							}
						} else if (status == 1) {
							if (node.indexOf("</p") < 0) {
								if (node.indexOf("<p") < 0) {
									temp.append(node);
									wordSize = wordSize + node.length();
								} else {
									temp.append(lineEnd);
									temp.append(node);
									status = 1;
								}
							} else {
								temp.append(node);
								status = 0;
							}
						}
					}
				}
				if (status == 1) {
					temp.append(lineEnd);
				}

				if (wordSize > context.getNumber()) {
					context.setNumber(wordSize);
					context.setTextBuffer(temp);
				}
				return null;
			} else {
				return tableList;
			}
		}
		return null;
	}

	/**
	 * 设置图象连接
	 * 
	 * @param nodeP
	 * @param siteUrl
	 */
	private void setLinkImg(Node nodeP, String siteUrl) {
		NodeList nodeList = nodeP.getChildren();
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof ImageTag) {
					ImageTag img = (ImageTag) node;
					if (img.getImageURL().toLowerCase().indexOf("http://") < 0) {
						img.setImageURL(siteUrl + img.getImageURL());
					} else {
						img.setImageURL(img.getImageURL());
					}
				}
			}
		} catch (Exception e) {
			return;
		}
		return;
	}

	/**
	 * 钻取段落中的内容
	 * 
	 * @param nodeP
	 * @param siteUrl
	 * @param tableList
	 * @return
	 */
	private List extractParagraph(Node nodeP, String siteUrl, List tableList) {
		NodeList nodeList = nodeP.getChildren();
		if ((nodeList == null) || (nodeList.size() == 0)) {
			if (nodeP instanceof ParagraphTag) {
				StringBuffer temp = new StringBuffer();
				temp.append("<p style=\"TEXT-INDENT: 2em\">");
				tableList.add(temp);
				temp = new StringBuffer();
				temp.append("</p>").append(lineSign);
				tableList.add(temp);
				return tableList;
			}
			return null;
		}
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof ScriptTag || node instanceof StyleTag
						|| node instanceof SelectTag) {
				} else if (node instanceof LinkTag) {
					tableList.add(node);
					setLinkImg(node, siteUrl);
				} else if (node instanceof ImageTag) {
					ImageTag img = (ImageTag) node;
					if (img.getImageURL().toLowerCase().indexOf("http://") < 0) {
						img.setImageURL(siteUrl + img.getImageURL());
					} else {
						img.setImageURL(img.getImageURL());
					}
					tableList.add(node);
				} else if (node instanceof TextNode) {
					if (node.getText().trim().length() > 0) {
						String text = collapse(node.getText().replaceAll(
								"&nbsp;", "").replaceAll("　", ""));
						StringBuffer temp = new StringBuffer();
						temp.append(text);
						tableList.add(temp);
					}
				} else if (node instanceof Span) {
					StringBuffer spanWord = new StringBuffer();
					getSpanWord(node, spanWord);
					if ((spanWord != null) && (spanWord.length() > 0)) {
						String text = collapse(spanWord.toString().replaceAll(
								"&nbsp;", "").replaceAll("　", ""));
						StringBuffer temp = new StringBuffer();
						temp.append(text);
						tableList.add(temp);
					}
				} else if (node instanceof TagNode) {
					String tag = node.toHtml();
					if (tag.length() <= 10) {
						tag = tag.toLowerCase();
						if ((tag.indexOf("strong") >= 0)
								|| (tag.indexOf("b") >= 0)) {
							StringBuffer temp = new StringBuffer();
							temp.append(tag);
							tableList.add(temp);
						}
					} else {
						if (node instanceof TableTag || node instanceof Div) {
							TableValid tableValid = new TableValid();
							isValidTable(node, tableValid);
							if (tableValid.getTrnum() > 2) {
								tableList.add(node);
								continue;
							}
						}
						extractParagraph(node, siteUrl, tableList);
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return tableList;
	}

	protected void getSpanWord(Node nodeP, StringBuffer spanWord) {
		NodeList nodeList = nodeP.getChildren();
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof ScriptTag || node instanceof StyleTag
						|| node instanceof SelectTag) {
				} else if (node instanceof TextNode) {
					spanWord.append(node.getText());
				} else if (node instanceof Span) {
					getSpanWord(node, spanWord);
				} else if (node instanceof ParagraphTag) {
					getSpanWord(node, spanWord);
				} else if (node instanceof TagNode) {
					String tag = node.toHtml().toLowerCase();
					if (tag.length() <= 10) {
						if ((tag.indexOf("strong") >= 0)
								|| (tag.indexOf("b") >= 0)) {
							spanWord.append(tag);
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return;
	}

	/**
	 * 判断TABLE是否是表单
	 * 
	 * @param nodeP
	 * @return
	 */
	private void isValidTable(Node nodeP, TableValid tableValid) {
		NodeList nodeList = nodeP.getChildren();
		/** 如果该表单没有子节点则返回* */
		if ((nodeList == null) || (nodeList.size() == 0)) {
			return;
		}
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				/** 如果子节点本身也是表单则返回* */
				if (node instanceof TableTag || node instanceof Div) {
					return;
				} else if (node instanceof ScriptTag
						|| node instanceof StyleTag
						|| node instanceof SelectTag) {
					return;
				} else if (node instanceof TableColumn) {
					return;
				} else if (node instanceof TableRow) {
					TableColumnValid tcValid = new TableColumnValid();
					tcValid.setValid(true);
					findTD(node, tcValid);
					if (tcValid.isValid()) {
						if (tcValid.getTdNum() < 2) {
							if (tableValid.getTdnum() > 0) {
								return;
							} else {
								continue;
							}
						} else {
							if (tableValid.getTdnum() == 0) {
								tableValid.setTdnum(tcValid.getTdNum());
								tableValid.setTrnum(tableValid.getTrnum() + 1);
							} else {
								if (tableValid.getTdnum() == tcValid.getTdNum()) {
									tableValid
											.setTrnum(tableValid.getTrnum() + 1);
								} else {
									return;
								}
							}
						}
					}
				} else {
					isValidTable(node, tableValid);
				}
			}
		} catch (Exception e) {
			return;
		}
		return;
	}

	/**
	 * 判断是否有效TR
	 * 
	 * @param nodeP
	 * @param TcValid
	 * @return
	 */
	private void findTD(Node nodeP, TableColumnValid tcValid) {
		NodeList nodeList = nodeP.getChildren();
		/** 如果该表单没有子节点则返回* */
		if ((nodeList == null) || (nodeList.size() == 0)) {
			return;
		}
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				/** 如果有嵌套表单* */
				if (node instanceof TableTag || node instanceof Div
						|| node instanceof TableRow
						|| node instanceof TableHeader) {
					tcValid.setValid(false);
					return;
				} else if (node instanceof ScriptTag
						|| node instanceof StyleTag
						|| node instanceof SelectTag) {
					tcValid.setValid(false);
					return;
				} else if (node instanceof TableColumn) {
					tcValid.setTdNum(tcValid.getTdNum() + 1);
				} else {
					findTD(node, tcValid);
				}
			}
		} catch (Exception e) {
			tcValid.setValid(false);
			return;
		}
		return;
	}

	protected String collapse(String string) {
		int chars;
		int length;
		int state;
		char character;
		StringBuffer buffer = new StringBuffer();
		chars = string.length();
		if (0 != chars) {
			length = buffer.length();
			state = ((0 == length) || (buffer.charAt(length - 1) == ' ') || ((lineSign_size <= length) && buffer
					.substring(length - lineSign_size, length).equals(lineSign))) ? 0
					: 1;
			for (int i = 0; i < chars; i++) {
				character = string.charAt(i);
				switch (character) {
				case '\u0020':
				case '\u0009':
				case '\u000C':
				case '\u200B':
				case '\u00a0':
				case '\r':
				case '\n':
					if (0 != state) {
						state = 1;
					}
					break;
				default:
					if (1 == state) {
						buffer.append(' ');
					}
					state = 2;
					buffer.append(character);
				}
			}
		}
		return buffer.toString();
	}
}
