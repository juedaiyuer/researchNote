package com.lietu.dup;



public class SynonymReplace {


	/** The word list with score. */
	public static SynonymDic synonymDic = SynonymDic.getInstance();
	
	public static String replace(String content) throws Exception
	{
		if (content == null){
			return null;
		}
		int len = content.length();
		StringBuilder ret = new StringBuilder(len);
		SynonymDic.PrefixRet matchRet = new SynonymDic.PrefixRet(null,null);
		
		for(int i=0;i<len;)
		{
			//检查是否存在从当前位置开始的同义词
			synonymDic.checkPrefix(content,i,matchRet);
			if(matchRet.value == SynonymDic.Prefix.Match)
			{
				ret.append(matchRet.data);
				i=matchRet.next;//下一个匹配位置
			}
			else //从下一个字符开始匹配
			{
				ret.append(content.charAt(i));
				++i;
			}
		}
		
		return ret.toString();
	}
}
