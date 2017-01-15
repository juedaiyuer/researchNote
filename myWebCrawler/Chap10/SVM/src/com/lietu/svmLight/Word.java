package com.lietu.svmLight;

public class Word {
	public int wnum;
	public float weight;
	
	public Word()
	{}
	
	public Word(int w, float wei)
	{
		wnum = w;
		weight = wei;
	}
	
	public String toString()
	{
		return "wnum:"+wnum+" weight:"+weight;
	}
}
