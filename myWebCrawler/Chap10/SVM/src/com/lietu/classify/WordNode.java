package com.lietu.classify;

public class WordNode {
	static final double dZero = 1.0E-10;
	
	//	在特征选择的时候用来保存特征的类别区分度
	//在特征选择完成之后，它用来保存每个特征的"log(训练文档总数/特征的文档频率)"值
	public float m_dWeight;
	public int m_nAllocLen;
	public int m_nWordID;           //特征的ID
	public double[] catWeight = null;   //特征对于每个类别的区分度
	//特征在整个文档集中的文档频率,实际就是函数GetDocNum()返回的值
	//如果特征不是从训练文档集中选择得到的，就无法使用GetDocNum()得到特征的文档频率
	//所以，此处使用m_lDocFreq来记录特征的文档频率
	public int m_lDocFreq;         //特征在整个文档集中的文档频率
	//特征在整个文档集中的词频,实际就是函数GetWordNum()返回的值
	//如果特征不是从训练文档集中选择得到的，就无法使用GetWordNum()得到特征的词频
	//所以，此处使用m_lWordFreq来记录特征的词频
	public int m_lWordFreq;        //特征在整个文档集中的词频
	public long[] m_pCataDocFreq = null;    //特征在每一个类别中的文档频率
	public long[] m_pCataWordFreq = null;   //特征在每一个类别中的词频
	public long m_lDocID;           //得到特征的文档频率的时候用到
	
	public WordNode()
	{
		m_dWeight=0.0f;
		m_nAllocLen=0;
		catWeight=null;
		m_pCataDocFreq=null;
		m_pCataWordFreq=null;
		m_lDocFreq=0;
		m_lWordFreq=0;
		m_nWordID=-1;
		m_lDocID=-1;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("m_nWordID:");
		sb.append(m_nWordID);
		sb.append('\n');
		
		sb.append("m_dWeight:");
		sb.append(m_dWeight);
		sb.append('\n');

		sb.append("m_lDocFreq:");
		sb.append(m_lDocFreq);
		sb.append('\n');

		sb.append("m_lWordFreq:");
		sb.append(m_lWordFreq);
		sb.append('\n');

		sb.append("catWeight:");
		for(double cw:catWeight)
		{
			sb.append(cw);
			sb.append(" ");
		}
		sb.append('\n');
		
		sb.append("m_pCataDocFreq:");
		for(long cw:m_pCataDocFreq)
		{
			sb.append(cw);
			sb.append(" ");
		}
		sb.append('\n');

		sb.append("m_pCataWordFreq:");
		for(long cw:m_pCataWordFreq)
		{
			sb.append(cw);
			sb.append(" ");
		}
		sb.append('\n');
		
		return sb.toString();
	}
	
	public void initBuffer(int nLen)
	{
		if(nLen<=0) return;
		if(m_nAllocLen<=0&&catWeight==null&&
			m_pCataDocFreq==null&&m_pCataWordFreq==null)
		{
			m_nAllocLen=nLen;
			catWeight=new double[m_nAllocLen];
			//for(int i=0;i<m_nAllocLen;++i)
			//{
			//	catWeight[i]=0;
			//}
			m_pCataDocFreq=new long[m_nAllocLen];

			//for(int i=0;i<m_nAllocLen;++i)
			//{
			//	m_pCataDocFreq[i]=0;
			//}
			m_pCataWordFreq=new long[m_nAllocLen];

			//for(int i=0;i<m_nAllocLen;++i)
			//{
			//	m_pCataWordFreq[i]=0;
			//}
		}
	}

	//	此函数暂且只在层次分类中用到,函数名称和其实现的功能看起来有点不同
	public void copy(WordNode wordNode)
	{
		m_dWeight=wordNode.m_dWeight;
		m_nAllocLen=0;
		m_nWordID=wordNode.m_nWordID;
		m_lDocFreq=wordNode.m_lDocFreq;
		catWeight=null;
		m_pCataDocFreq=null;
		m_pCataWordFreq=null;
		m_lDocID=0;
	}
	
	public long getWordNum()
	{
		long sum=0;
		if(m_nAllocLen>0)
		{
			for(int i=0;i<m_nAllocLen;i++)
				sum+=m_pCataWordFreq[i];
		}
		else sum=m_lWordFreq;
		return sum;
	}
	
	public long getDocNum()
	{
		long sum=0;
		if(m_nAllocLen>0)
		{
			for(int i=0;i<m_nAllocLen;i++)
				sum+=m_pCataDocFreq[i];
		}
		else sum=m_lDocFreq;
		return sum;
	}
	
	public long getCataWordNum(int cataID)
	{
		return m_pCataWordFreq[cataID];	
	}
	
	public long getCataDocNum(int cataID)
	{
		long sum=0;
		if(m_nAllocLen>0)
		{
			for(int i=0;i<m_nAllocLen;i++)
				sum+=m_pCataDocFreq[i];
		}
		else sum=m_lDocFreq;
		return sum;
	}
	
	//	用于计算特征的权重，参数sum代表文档集中的文档总数
	//	如果bMult=true且m_dWeight大于0, 则将特征的反比文档频率乘上m_dWeight原来的值, 再保存到成员变量m_dWeight中
	//	否则, 将特征的反比文档频率值保存到成员变量m_dWeight中
	public void computeWeight(long sum, boolean bMult)
	{
		long docFreq=getDocNum();
		if(docFreq<=0&&sum<=0)
		{
			m_dWeight=0.0f;
			return;
		}
		float weight=(float)Math.log((double)sum/(double)docFreq);
		if(bMult&&m_dWeight>dZero)
			m_dWeight*=weight;
		else
			m_dWeight=weight;
	}
	
	public void computeWeight(long sum)
	{
		computeWeight(sum, false);
	}
}
