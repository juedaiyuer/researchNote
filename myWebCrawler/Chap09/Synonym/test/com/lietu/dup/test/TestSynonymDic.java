package com.lietu.dup.test;
import com.lietu.dup.SynonymDic;

public class TestSynonymDic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SynonymDic dicSynonymous = SynonymDic.getInstance();
		String text = "APOTEX INC ¨C ETOBICOKE";
		int offset = 0;
		SynonymDic.PrefixRet ret = new SynonymDic.PrefixRet(null,null);
		
		dicSynonymous.checkPrefix(text, offset, ret);
		System.out.println(ret);
		
	}

}
