package com.lietu.classify;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Scanner;

import com.lietu.seg.result.CnToken;
import com.lietu.seg.result.Tagger;
import com.lietu.svmLight.Word;

public class DocNode {
	public int m_idxDoc;                //文档标识
	public String m_strDocName;         //文档名称
	public WeightNode[]	m_sWeightSet;    //词权值列表
	public int m_nCataID;             //代表当前文档的所属类别,由于只在分类时使用,所以序列化的时候不操作此属性
	
	private static PorterStemmer theStemmer  = new PorterStemmer();
	
	public DocNode(){
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("m_idxDoc:");
		sb.append(m_idxDoc);
		sb.append('\n');
		
		sb.append("m_strDocName:");
		sb.append(m_strDocName);
		sb.append('\n');
		
		sb.append("m_sWeightSet.len:");
		sb.append(m_sWeightSet.length);
		sb.append('\n');
		
		return sb.toString();
	}
	
	//使用分词的方法形成文档的特征属性
	//用于训练
	int scanChinese(String pPath, WordList wordList,int nCataNum, int idxCata)
	{
		File fin;
		String buffer=new String();
		String pTempStr=pPath;
		pTempStr+="/";
		pTempStr+=m_strDocName;

		try{
			System.out.println("scan file:"+pTempStr);
			fin = new File(pTempStr);
			Scanner scanner = new Scanner(fin,"GBK");
			scanner.useDelimiter("\\z");
			if(scanner.hasNext())
			{
				buffer = scanner.next();
			}
			else
			{
				scanner.close();
				scanner = new Scanner(fin,"UTF-8");
				scanner.useDelimiter("\\z");
				if(scanner.hasNext())
				{
					buffer = scanner.next();
				}
				else
				{
					scanner.close();
					return -1;
				}
			}
		    scanner.close();
		}
		catch (FileNotFoundException e) {
		    return -1;
		}

		int num=scanChineseString(buffer,wordList,nCataNum,m_idxDoc,idxCata);
		return num;
	}
	
	int scanEnglish(String pPath, WordList wordList, int nCataNum, int idxCata, boolean bStem)
	{
		File fin;
		String buffer=new String();
		String pTempStr = pPath;
		pTempStr+="\\";
		pTempStr+=m_strDocName;

		fin = new File(pTempStr);

		try{
			Scanner scanner = new Scanner(fin);
			scanner.useDelimiter("\\z");
			
			buffer = scanner.next();
			scanner.close();
		} catch (FileNotFoundException e) {
		    return -1;
		}

		int num=scanEnglishString(buffer,wordList,nCataNum,m_idxDoc,idxCata,bStem);
		return num;
	}

	int scanChineseString(String pPath,
						WordList wordList,
						int nCataNum,
						long idxDoc,
						int idxCata)
	{
		int j;
		String w;
		int realcnt=0;

		ArrayList<CnToken> pItem = Tagger.getFormatSegResult(pPath);
		
		for(j=0;j<pItem.size();j++)
		{
			CnToken token = pItem.get(j);
			w=token.termText;
			if(! StopSet.getInstance().contains(w) &&
					//w.charAt(0)>0x0E &&
					(token.type.equals("n") ||
							token.type .equals("ng") ||
							token.type .equals("v") ||
							token.type .equals("b") ||
							token.type .equals("ns")||
							(token.type .equals("nx") && token.termText.length()<=15 ) )
					)
			{
				wordList.add(w,idxCata,idxDoc,nCataNum);
				realcnt++;
			}
		}
		return realcnt;
	}

	int scanEnglishString(String pPath,
						WordList wordList,
						int nCataNum,
						long idxDoc,
						int idxCata,
						boolean bStem)
	{
		char[] buffer=pPath.toLowerCase().toCharArray();
		//buffer = buffer.toLowerCase();

		int nFilePos=0;
		int realcnt=0,wordLen=0;
		char c;
		int p=0;
		//p=buffer;
		while(nFilePos<buffer.length)
		{
			c=buffer[nFilePos];
			if(c==' '||c=='\r'||c=='\n'||
				(c>32&&c<=47)||(c>=58&&c<=64)||(c>=91&&c<=96)||(c>=123&&c<=127))
			{
				//buffer[nFilePos]='\0';
				wordLen=nFilePos - p;//buffer+nFilePos-p;
				if(wordLen>2)
				{
					if(bStem)
						theStemmer.stem(buffer,p,wordLen-1);
					wordList.add(String.valueOf(buffer, p, wordLen-1),idxCata,idxDoc,nCataNum);
					realcnt++;
				}
				p=nFilePos+1;
			}
			nFilePos++;
		}
		return realcnt;
	}

	//根据词典wordList计算文档每一维的权重,形成文档的向量,将其保存到数组m_sWeightSet
	//这个方法要求词典wordList中每一个wordnode的m_dWeight的值都赋为此特征的反比文档频率
	int scanChineseWithDict(String pPath,
							WordList wordList,
							WeightNode[] weightNode)
	{
		//System.out.println("enter scan chinese dic");
		File fin;
		String buffer=new String();
		String pTempStr=pPath;
		pTempStr+="/";
		pTempStr+=m_strDocName;

		//System.out.println("file name:"+pTempStr);
		fin = new File(pTempStr);

		try{
			Scanner scanner = new Scanner(fin,"GBK");
			scanner.useDelimiter("\\z");
			if(scanner.hasNext())
			{
				buffer = scanner.next();
			}
			else
			{
				scanner.close();
				scanner = new Scanner(fin,"UTF-8");
				scanner.useDelimiter("\\z");
				if(!scanner.hasNext())
				{
					scanner.close();
					return -1;
				}
				buffer = scanner.next();
			}
		    scanner.close();
		} catch (FileNotFoundException e) {
		    return -1;
		}
		
		//System.out.println("file content:"+buffer);
		
		int num=scanChineseStringWithDict(buffer,wordList,weightNode);
		
		return num;
	}

	//用于分类
	public int scanChineseStringWithDict(String buffer,
										WordList wordList,
										WeightNode[] m_pTemp)
	{
		int i=0,j;
		//System.out.println("buffer" + buffer);
		
		//realcnt为文章中去掉停用词后剩下的总共词数
		//nStart为一个句子在buffer中的开始位置
		int realcnt=0;
		//句子的长度
		//int nSentenceLen=0;
		//System.out.println("m_nAllocTempLen:"+m_nAllocTempLen);
		for(int m=0;m<m_pTemp.length;++m)
		{
			m_pTemp[m] = new WeightNode();
		}
		
		ArrayList<CnToken> pItem = Tagger.getFormatSegResult(buffer);
		
		//System.out.println("Token size:"+pItem.size());
		for(j=0;j<pItem.size();j++)
		{
			String w=pItem.get(j).termText;
			
			//System.out.println("word:"+w);
			WordNode wordNode = wordList.lookup(w);
			//System.out.println("end lookup");
			if(wordNode!=null)
			{
				//TODO: still error here
				if (wordNode.m_nWordID<m_pTemp.length)
				{
					m_pTemp[wordNode.m_nWordID].s_idxWord=wordNode.m_nWordID;
					m_pTemp[wordNode.m_nWordID].s_tfi+=1;
					m_pTemp[wordNode.m_nWordID].s_dWeight=wordNode.m_dWeight;

					//System.out.println("WordID:"+wordNode.m_nWordID);
					//System.out.println("m_pTemp[wordNode.m_nWordID]:"+m_pTemp[wordNode.m_nWordID]);
					
					realcnt++;
				}
				else
				{
					System.out.println("WordID:"+wordNode.m_nWordID+" m_pTemp.length:"+m_pTemp.length);
				}
			}
		}
				
		//对文档向量中的每一维进行加权
		if(realcnt>0)
		{
			double sum=0;
			for(i=0;i<m_pTemp.length;i++)
			{
				m_pTemp[i].s_dWeight*=m_pTemp[i].s_tfi;
				sum+=(m_pTemp[i].s_dWeight*m_pTemp[i].s_dWeight);
			}
			sum=Math.sqrt(sum);
			//System.out.println("sum:"+sum);
			for(i=0;i<m_pTemp.length;i++)
				m_pTemp[i].s_dWeight/=sum;
		}
		return realcnt;
	}

	int scanEnglishWithDict(String pPath,
			WordList wordList,
			boolean bStem,
			WeightNode[] m_pTemp)
	{
		File fin;
		String buffer=new String();
		String pTempStr=pPath;
		pTempStr+="\\";
		pTempStr+=m_strDocName;

		fin = new File(pTempStr);

		try{
			Scanner scanner = new Scanner(fin);
			scanner.useDelimiter("\\z");
			
			buffer = scanner.next();
		    scanner.close();
		} catch (FileNotFoundException e) {
		    return -1;
		}
		
		int num=scanEnglishStringWithDict(buffer,wordList,bStem,m_pTemp);
		
		return num;
	}

	int scanEnglishStringWithDict(String pPath,
								WordList wordList,
								boolean bStem,
								WeightNode[] m_pTemp)
	{
		String buffer=pPath.toLowerCase();

		int nFilePos=0;
		int realcnt=0,wordLen=0;
		char c;
		int p=0;
		
		for(int i=0;i<m_pTemp.length;++i)
		{
			m_pTemp[i] = new WeightNode();
		}
		WordNode wordNode=null;
		for(;nFilePos<buffer.length();nFilePos++)
		{
			c=buffer.charAt(nFilePos);
			if(c==' '||c=='\r'||c=='\n'||
				(c>32&&c<=47)||(c>=58&&c<=64)||(c>=91&&c<=96)||(c>=123&&c<=127))
			{
				wordLen=nFilePos-p;
				if(wordLen>2)
				{
					String word = buffer.substring(p,wordLen+p);
					if(bStem)
						theStemmer.stem(word);
					wordNode = wordList.lookup(word);
					if(wordNode!=null)
					{
						m_pTemp[wordNode.m_nWordID].s_idxWord=wordNode.m_nWordID;
						m_pTemp[wordNode.m_nWordID].s_tfi+=1;
						m_pTemp[wordNode.m_nWordID].s_dWeight=wordNode.m_dWeight;
						realcnt++;
					}
				}
				p=nFilePos+1;
			}
		}

		//对文档向量中的每一维进行加权
		int i;
		if(realcnt>0)
		{
			double sum=0;
			for(i=0;i<m_pTemp.length;i++)
			{
				m_pTemp[i].s_dWeight*=m_pTemp[i].s_tfi;
				sum+=(m_pTemp[i].s_dWeight*m_pTemp[i].s_dWeight);
			}
			sum=Math.sqrt(sum);
			for(i=0;i<m_pTemp.length;i++)
				m_pTemp[i].s_dWeight/=sum;
		}
		return realcnt;
	}
	
	void allocBuffer(int nLen)
	{
		if(nLen<=0) return;
		
		m_sWeightSet=new WeightNode[nLen];
		for(int i=0;i<nLen;++i)
		{
			m_sWeightSet[i] = new WeightNode();
		}
	}
	
	int genDocVector(WeightNode[] m_pTemp)
	{
		int i,nSum=0;
		for(i=0;i<m_pTemp.length;i++)
			if(m_pTemp[i].s_dWeight>Double.MIN_NORMAL) nSum++;	
		allocBuffer(nSum);
		nSum=0;
		for(i=0;i<m_pTemp.length;i++)
		{
			if(m_pTemp[i].s_dWeight>Double.MIN_NORMAL)
			{
				if(m_sWeightSet == null)
				{
					System.out.println("is null m_sWeightSet all");
				}
				if(m_sWeightSet[nSum] == null)
				{
					System.out.println("is null m_sWeightSet"+nSum);
				}
				m_sWeightSet[nSum].s_idxWord=m_pTemp[i].s_idxWord;
				m_sWeightSet[nSum].s_tfi=m_pTemp[i].s_tfi;
				m_sWeightSet[nSum].s_dWeight=m_pTemp[i].s_dWeight;
				nSum++;
			}
		}
		return nSum;
	}

	int genDocVector(com.lietu.svmLight.Doc doc,
					WeightNode[] weightNode)
	{
		if(weightNode==null||weightNode.length<=0) return -1;
		
		ArrayList<com.lietu.svmLight.Word> words=new ArrayList<com.lietu.svmLight.Word>();
		
		int nSum=0;
		for(int i=0;i<weightNode.length;i++)
		{
			if(weightNode[i].s_dWeight>0)
			{
				//DOC的特征ID从1开始
				words.add( new com.lietu.svmLight.Word(i+1,weightNode[i].s_dWeight));
				nSum++;
			}
		}
		doc.words = words.toArray(new Word[words.size()]);
		doc.docnum=-1;
		return nSum;
	}

	//与存放在m_pTemp中的向量进行相似度的计算
	double computeSimilarityRatio(WeightNode[] m_pTemp)
	{
		double sum=0.0;
		for(int i=0;i<m_sWeightSet.length;i++)
			sum+=m_sWeightSet[i].s_dWeight*m_pTemp[m_sWeightSet[i].s_idxWord].s_dWeight;
		return sum;
	}
}
