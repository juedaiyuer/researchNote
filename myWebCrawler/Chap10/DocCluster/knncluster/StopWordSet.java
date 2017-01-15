package knncluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StopWordSet {
	
	private static StopWordSet worddic=null;
	private HashSet dic;
	private StopWordSet(String filename){
		dic=new HashSet();
		String dataline;
	      try {
	         InputStream setdata = getClass().getResourceAsStream(filename);
	         BufferedReader in = new BufferedReader(new InputStreamReader(
	               setdata,"UTF-8"));
	         
	         while ((dataline = in.readLine()) != null) {
	            if ((dataline.length() == 0)) {
	               continue;
	            }
	            dic.add(dataline.intern());
	            }
	         in.close();
	      } catch (Exception e) {
	         System.err.println("Exception loading data file" + filename + " "
	               + e);
	         e.printStackTrace();
	      }
	}
	public  static StopWordSet GetInstance(String filename){
		if(StopWordSet.worddic==null){
			StopWordSet.worddic= new StopWordSet(filename);
		}
		return StopWordSet.worddic;	
	}
	public boolean IsStopWord(String str){
		return dic.contains(str);
	}
}

