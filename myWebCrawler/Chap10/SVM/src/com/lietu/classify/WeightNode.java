package com.lietu.classify;

//用来记录文档向量中每一维特征的权重
public class WeightNode {
	public int s_idxWord=0;    //特征的ID
	public short s_tfi=0;        //特征在文档中出现的频次
	public float s_dWeight=0;    //特征的权重
	
	public String toString()
	{
		return "tfi:"+s_tfi;
	}
}
