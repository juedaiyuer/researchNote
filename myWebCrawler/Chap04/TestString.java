

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestString {
	/** 多次使用的话不需要重新编译正则表达式了，对于频繁调用能提高效率 */
	public static final String patternString1 = "[^\\s]*((<\\s*[aA]\\s+(href\\s*=[^>]+\\s*)>)(.*)</[aA]>).*";
	public static final String patternString2 = ".*(<\\s*[aA]\\s+(href\\s*=[^>]+\\s*)>(.*)</[aA]>).*";
	public static final String patternString3 = ".*href\\s*=\\s*(\"|'|)http://.*";
	public static Pattern pattern1 = Pattern.compile(patternString1,
			Pattern.DOTALL);
	public static Pattern pattern2 = Pattern.compile(patternString2,
			Pattern.DOTALL);
	public static Pattern pattern3 = Pattern.compile(patternString3,
			Pattern.DOTALL);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/** 测试的数据 */
		String ss = "这是测试<a href=http://www.google.cn>www.google.cn</a>真的是测试了";
		/** 保存提取出来的url,用set从某种程度去重，只是字面上，至于语义那就要需要考虑很多了 */
		Set<String> set = new HashSet<String>();
		/** 解析url并保存在set里 */
		parseUrl(set, ss);
		/** 针对解析出来的url做处理 */
		System.out.println(replaceHtml(set, ss));

	}

	/** 给每个url加上target属性 */
	public static String replaceHtml(Set<String> set, String var) {
		String result = null;
		/** 最好不要对参数修改 */
		result = var;
		Iterator<String> ite = set.iterator();
		while (ite.hasNext()) {
			String url = ite.next();
			if (url != null) {
				result = result.replaceAll(url, url + "  target=\"_blank\"");

			}
		}
		return result;
	}

	public static void parseUrl(Set<String> set, String var) {
		Matcher matcher = null;
		String result = null;
		// 假设最短的a标签链接为 <a href=http://www.a.cn></a>则计算他的长度为28
		if (var != null && var.length() > 28) {
			matcher = pattern3.matcher(var);
			// 确定句子里包含有链接
			if (matcher != null && matcher.matches()) {
				matcher = pattern1.matcher(var);
				String aString = null;
				String bString = null;

				while (matcher != null && matcher.find()) {
					if (matcher.groupCount() > 3) {
						bString = matcher.group(matcher.groupCount() - 3);// 这个group包含所有符合正则的字符串
						aString = matcher.group(matcher.groupCount() - 2);// 这个group包含url的html标签
						String url1 = matcher.group(matcher.groupCount() - 1);// 最后一个group就是url
						set.add(url1);// 将找到的url保存起来
						bString = bString.replaceAll(aString, "");// 去掉已经找到的url的html标签
					}

				}
				if (bString != null) {
					parseUrl(set, bString);// 继续循环提取下一个url
				}

			}
		}

	}

}
