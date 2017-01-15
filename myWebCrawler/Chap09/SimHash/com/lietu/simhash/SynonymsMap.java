package com.lietu.simhash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

public class SynonymsMap {

	public static String getDir()
	{
		String dir = System.getProperty("dic.dir");
		if (dir == null)
			dir = "/dic/";
		else if( !dir.endsWith("/"))
			dir += "/";
		return dir;
	}
	
	private SynonymsMap()
	{
		hm = new HashMap<String, HashSet<String>>();
		String line;
		
		try 
		{
			//BufferedReader reader = new BufferedReader(new FileReader("D:\\sl\\synonyms.txt"));
			String dic = "/synonyms.txt";
			InputStream file = null;
			if (System.getProperty("dic.dir") == null)
				file = getClass().getResourceAsStream(getDir()+dic);
			else
				file = new FileInputStream(new File(getDir()+dic));
			
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(file,"GBK"));
			
			while ((line = in.readLine()) != null) 
			{
				StringTokenizer token = new StringTokenizer(line,"=");
				token.nextToken();
				String strCombine = token.nextToken();
				StringTokenizer strWord = new StringTokenizer(strCombine," ");
				
				HashSet<String> hs = new HashSet<String>();
				
				while(strWord.hasMoreTokens())
				{
					String strSW  = strWord.nextToken();
					hs.add(strSW);
				}
				
				strWord = new StringTokenizer(strCombine," ");
				while(strWord.hasMoreTokens())
				{
					String strSW  = strWord.nextToken();
					hm.put(strSW, hs);
				}

			}
			
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getSynonyms(String a)
	{
		if (!hm.containsKey(a))
			return null;
		
		HashSet<String> hsa = hm.get(a);
		
		Iterator<String> ival = hsa.iterator();
		if (ival.hasNext()) 
		{
		   return (ival.next());
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		//SynonymsMap sm = new SynonymsMap();
		//System.out.println(sm.getSynonyms("πÛ÷› °"));
	}

	/**
	 * 
	 * @return the singleton of synonyms map
	 */
	public static SynonymsMap getInstance()
	{
		return sMap;
	}

	private static SynonymsMap sMap = new SynonymsMap();
	
	private static HashMap<String, HashSet<String>> hm;
}
