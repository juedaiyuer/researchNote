

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;

import com.newwatch.toolkit.splitwords.SpiderConstant;
import com.newwatch.toolkit.splitwords.SplitManager;



public class TestTabDivSerial {
	/**
	 * A newline.
	 */
	private static final String NEWLINE = System.getProperty("line.separator");
	/**
	 * The length of the NEWLINE.
	 */
	private static final int NEWLINE_SIZE = NEWLINE.length();
	private String url;
	private final String oriEncode = "gb2312,utf-8,gbk,iso-8859-1";
	private ArrayList htmlContext = new ArrayList();
	private String urlEncode;
	private int tableNumber;
	private int channelNumber;
	private int totalNumber;
	// url正则表达
	private String domain;
	private String urlDomaiPattern;
	private String urlPattern;
	private Pattern pattern;
	private Pattern patternPost;

	public void channelParseProcess() {
		/** 提取本站信息的正则表达式* */
		urlDomaiPattern = "(http://[^/]*?" + domain + "/)(.*?)";
		urlPattern = "(http://[^/]*?" + domain
				+ "/[^.]*?).(shtml|html|htm|shtm|php|asp#|asp|cgi|jsp|aspx)";
		pattern = Pattern.compile(urlDomaiPattern, Pattern.CASE_INSENSITIVE
				+ Pattern.DOTALL);
		patternPost = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE
				+ Pattern.DOTALL);
		/** 收集表单集合* */
		SplitManager splitManager = (SplitManager) ExtractLinkConsole.context
				.getBean("splitManager");
		urlEncode = dectedEncode(url);
		if (urlEncode == null) {
			return;
		}
		singContext(url);
		Iterator hi = htmlContext.iterator();
		if (htmlContext.size() == 0) {
			return;
		}
		totalNumber = htmlContext.size();
		// 分析表单集合
		while (hi.hasNext()) {
			TableContext tc = (TableContext) hi.next();
			this.totalNumber = tc.getTableRow();
			if ((tc.getTableRow() == this.channelNumber)
					|| (this.channelNumber == -1)) {
				System.out.println("*********************表单" + tc.getTableRow()
						+ "****************");
				List linkList = tc.getLinkList();
				// 如果没有任何连接
				if ((linkList == null) || (linkList.size() == 0)) {
					continue;
				}
				Iterator hl = linkList.iterator();
				/** 分析单个表单* */
				while (hl.hasNext()) {
					LinkTag lt = (LinkTag) hl.next();
					// **过滤非法link*
					if (isValidLink(lt.getLink()) == SpiderConstant.OUTDOMAINLINKTYPE) {
						continue;
					}
					if (lt.getLinkText().length() < 8) {
						continue;
					}
					/** 过滤无效link* */
					if (splitManager.isChannelLink(lt.getLinkText()) != SpiderConstant.COMMONCHANNEL) {
						continue;
					}
					/** 生成link的hashcode* */
					System.out.println("URL:" + lt.getLinkText() + "   "
							+ lt.getLink());
				}
			}
		}
	}

	/**
	 * 
	 * 判断是否有效连接
	 */
	public int isValidLink(String link) {
		Matcher matcher = pattern.matcher(link);
		while (matcher.find()) {

			int start = matcher.start(2);
			int end = matcher.end(2);
			String postUrl = link.substring(end).trim();
			// 如果是目录型连接
			if ((postUrl.length() == 0) || (postUrl.indexOf(".") < 0)) {
				return SpiderConstant.CHANNELLINKTYPE;
			} else {
				Matcher matcherPost = patternPost.matcher(link);
				if (matcherPost.find()) {
					return SpiderConstant.COMMONLINKTYPE;
				} else {
					return SpiderConstant.OUTDOMAINLINKTYPE;
				}
			}
		}
		return SpiderConstant.OUTDOMAINLINKTYPE;
	}

	/**
	 * 收集HTML页面信息
	 */
	public void singContext(String url) {
		try {
			Parser parser = new Parser(url);
			parser.setEncoding(urlEncode);
			tableNumber = 0;
			for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof Html) {
					extractHtml(node);
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 递归钻取信息
	 */
	public List extractHtml(Node nodeP) {
		NodeList nodeList = nodeP.getChildren();
		boolean bl = false;
		if ((nodeList == null) || (nodeList.size() == 0)) {
			return null;
		}
		if ((nodeP instanceof TableTag) || (nodeP instanceof Div)) {
			bl = true;
		}
		ArrayList tableList = new ArrayList();
		try {
			for (NodeIterator e = nodeList.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof LinkTag) {
					tableList.add(node);
				} else if (node instanceof ScriptTag
						|| node instanceof StyleTag
						|| node instanceof SelectTag) {
				} else if (node instanceof TextNode) {
					if (node.getText().trim().length() > 0) {
						tableList.add(node);
					}
				} else {
					List tempList = extractHtml(node);
					if ((tempList != null) && (tempList.size() > 0)) {
						Iterator ti = tempList.iterator();
						while (ti.hasNext()) {
							tableList.add(ti.next());
						}
					}
				}
			}
		} catch (Exception e) {
		}
		if ((tableList != null) && (tableList.size() > 0)) {
			if (bl) {
				TableContext tc = new TableContext();
				tc.setLinkList(new ArrayList());
				tc.setTextBuffer(new StringBuffer());
				tableNumber++;
				tc.setTableRow(tableNumber);
				Iterator ti = tableList.iterator();
				while (ti.hasNext()) {
					Node node = (Node) ti.next();
					if (node instanceof LinkTag) {
						tc.getLinkList().add(node);
					} else {
						tc.getTextBuffer().append(
								collapse(node.getText().replaceAll(" ", "")));
					}
				}
				htmlContext.add(tc);
				return null;
			} else {
				return tableList;
			}
		}
		return null;
	}

	/**
	 * 去除无效字符
	 */
	protected String collapse(String string) {
		int chars;
		int length;
		int state;
		char character;
		StringBuffer buffer = new StringBuffer();
		chars = string.length();
		if (0 != chars) {
			length = buffer.length();
			state = ((0 == length) || (buffer.charAt(length - 1) == ' ') || ((NEWLINE_SIZE <= length) && buffer
					.substring(length - NEWLINE_SIZE, length).equals(NEWLINE))) ? 0
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

	/**
	 * 检测字符级
	 */
	private String dectedEncode(String url) {
		String[] encodes = oriEncode.split(",");
		for (int i = 0; i < encodes.length; i++) {
			if (dectedCode(url, encodes)) {
				return encodes;
			}
		}
		return null;
	}

	public boolean dectedCode(String url, String encode) {
		try {
			Parser parser = new Parser(url);
			parser.setEncoding(encode);
			for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
				Node node = (Node) e.nextNode();
				if (node instanceof Html) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Pattern getPatternPost() {
		return patternPost;
	}

	public void setPatternPost(Pattern patternPost) {
		this.patternPost = patternPost;
	}

	public String getUrlDomaiPattern() {
		return urlDomaiPattern;
	}

	public void setUrlDomaiPattern(String urlDomaiPattern) {
		this.urlDomaiPattern = urlDomaiPattern;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(int totalNumber) {
		this.totalNumber = totalNumber;
	}

	public String getUrlEncode() {
		return urlEncode;
	}

	public void setUrlEncode(String urlEncode) {
		this.urlEncode = urlEncode;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}