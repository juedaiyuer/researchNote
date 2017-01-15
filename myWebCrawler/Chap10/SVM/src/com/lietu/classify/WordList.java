package com.lietu.classify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Map;

public class WordList {
	public HashMap<String,WordNode> m_lstWordList =null;
	
	public WordList()
	{
		m_lstWordList = new HashMap<String,WordNode>(1000);
	}

	public void initWordList() // initialize the word list
	{
		m_lstWordList.clear();
	}

	//	特征列表文件中每一行包含特征词和特征词的权重
	//	格式为feature weight，如果没有weight那么认为weight为1
	public boolean getListFromFile(String strFileName)
	{
		m_lstWordList.clear();
		
		try
		{
			InputStream file = new FileInputStream(new File(strFileName));
			BufferedReader fp = new BufferedReader(new InputStreamReader(file,"GBK"));
			
			String feature,line;
			float weight=1.0f;
			int num=0;
			while((line = fp.readLine())!=null)
			{
				StringTokenizer st = new StringTokenizer(line," " );
				
				try
				{
					st.nextToken();
					//no = st.nextToken();
					feature = st.nextToken();
					weight = Float.parseFloat(st.nextToken());
				}
				catch(NoSuchElementException e2)
				{
					//System.out.println("文件的第"+num+1+"行格式错误!");
					fp.close();
					return false;
				}
				
				if(weight<=0) weight=1.0f;
				
				WordNode node= m_lstWordList.get(feature);
				if(node == null)
				{
					//System.out.println("feature:"+feature);
					node = new WordNode();
					node.m_nWordID=num;
					node.m_dWeight=weight;
					m_lstWordList.put(feature, node);
				}
				num++;
			}
			fp.close();
		}
		catch (FileNotFoundException e1) {
			System.out.println("无法打开文件"+strFileName+"!");
			return false;
		}
		catch (IOException e1) {
			System.out.println("无法读文件"+strFileName+"!");
			return false;
		}
		return true;
	}
	
	public void dumpToFile(String strFileName)
	{
		//System.out.println("Will DO: output File:"+strFileName);
		LEDataOutputStream fBinOut = null;
		try {
			fBinOut = new LEDataOutputStream(new FileOutputStream(
					strFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int hashSize = m_lstWordList.size();
		//System.out.println("hashSize:"+hashSize);
		try {
			fBinOut.writeShort(hashSize);

			for(Map.Entry<String, WordNode> e : m_lstWordList.entrySet())
			{
				fBinOut.writeGBKString(e.getKey());
				fBinOut.writeInt(e.getValue().m_nWordID);
				fBinOut.writeDouble(e.getValue().m_dWeight);
				fBinOut.writeInt(e.getValue().m_lDocFreq);
				fBinOut.writeInt(e.getValue().m_lWordFreq);
			}

			fBinOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//
	public void dumpWordList(String strFileName)
	{
		BufferedWriter stream;
		try {
			stream = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(strFileName)),"GBK"));

			for(Map.Entry<String, WordNode> e : m_lstWordList.entrySet())
			{
				String     str = e.getKey();
				WordNode  wordnode = e.getValue();
				stream.write(String.format("%d %s %f\n", wordnode.m_nWordID,str,wordnode.m_dWeight));
			}
			stream.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
	}
	
	// get from mid information files WordList.mid
	public boolean getFromFile(String strFileName)
	{
		//System.out.println("wordList file:"+strFileName);
		m_lstWordList.clear();
		
		LEDataInputStream fIn = null;
		
		try
		{
			fIn = new LEDataInputStream(new FileInputStream(
				strFileName));
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("无法打开文件"+strFileName+"!") ;
			return false;
		}

		try
		{
			int hashSize = fIn.readShort();
			m_lstWordList = new HashMap<String,WordNode>(hashSize);
			//System.out.println("hashSize:"+hashSize);
			
			for(int i=0;i<hashSize;++i)
			{
				String key = fIn.readGBKString();
				//System.out.println("key:"+key);

				WordNode wordNode = new WordNode();
				wordNode.m_nWordID = fIn.readInt();
				//System.out.println(m_nWordID );//m_nWordID
				wordNode.m_dWeight = (float)fIn.readDouble();
				//System.out.println(m_dWeight);//m_dWeight
				wordNode.m_lDocFreq = fIn.readInt();
				//System.out.println(m_lDocFreq);//m_lDocFreq 3
				wordNode.m_lWordFreq = fIn.readInt();
				//System.out.println(wordNode);//m_lWordFreq 3
				m_lstWordList.put(key, wordNode);
			}
			fIn.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("无法读文件"+strFileName+"!") ;
			return false;
		}
		return true;
	}
	
	//如果词表中不存在这个词,则加入词表,并且根据docID决定文档频率是否加1
	public WordNode add(final String str,
						int cataID,
						long docID,
						int cataNum)
	{
	 	WordNode wordNode=m_lstWordList.get(str);
	 	if(wordNode == null)
	 	{
	 		wordNode = new WordNode();
			wordNode.initBuffer(cataNum);
	 		m_lstWordList.put(str, wordNode);
	 	}
		if(wordNode.m_lDocID!=docID)
		{
			wordNode.m_lDocID=docID;
			wordNode.m_pCataDocFreq[cataID]++;
			wordNode.m_lDocFreq++;
		}
		wordNode.m_pCataWordFreq[cataID]++;
		wordNode.m_lWordFreq++;

		return wordNode;
	}
	
	public void put(String str,WordNode node)
	{
		//System.out.println("put feather"+str);
		m_lstWordList.put(str,node);
	}
	
	//获得词表中词的总数
	public int getCount()
	{
		return m_lstWordList.size();
	}
	
	public long getWordNum()
	{
		long n=0;
		for(Map.Entry<String, WordNode> e : m_lstWordList.entrySet())
		{
			n+=e.getValue().getWordNum();
		}
		
		return n;
	}
	
	public WordNode lookup(String str)
	{
		return m_lstWordList.get(str);
	}
	
	public void indexWord()
	{
		int i=0;

		for(Map.Entry<String, WordNode> e : m_lstWordList.entrySet())
		{
			e.getValue().m_nWordID = i;
			++i;
		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, WordNode> e : m_lstWordList.entrySet())
		{
			sb.append(e.getKey());
			sb.append('\n');
			sb.append(e.getValue());
		}
		return sb.toString();
	}
}
