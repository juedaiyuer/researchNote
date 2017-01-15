package knncluster;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import com.lietu.seg.result.CnToken;
import com.lietu.seg.result.Tagger;

public class FeathuerSelection {
	private String filelist[];
	private double  fmatrix[][];
	//private double feathermatrix[][];
	//private int feathercount=1000;
	private HashMap[] doc;
	private HashMap worddic=new HashMap();
	public String[] getFilelist(){
		return filelist;
	}
	public void NormalizeVector(double [] vector){
		double sum=0.0;
		for(int i=0;i<vector.length;i++){
			sum+=vector[i]*vector[i];
		}
		double div=Math.sqrt(sum);
		if(div!=0.0){
			for(int j=0;j<vector.length;j++){
				vector[j]=vector[j]/div;
				String value=FORMAT.format(vector[j]);
				//System.out.println(value);
				vector[j]=Double.valueOf(value).doubleValue();
				
			}
		}
	}
	public String fileToStr(String filename){
		String result="";
		String dataline;
	      try {
	         InputStream setdata = getClass().getResourceAsStream(filename);
	         BufferedReader in = new BufferedReader(new InputStreamReader(
	               setdata,"UTF-8"));
	         while ((dataline = in.readLine()) != null) {
	            if ((dataline.length() == 0)) {
	               continue;
	            }
	            result+=dataline.intern().trim();
	         }
	         in.close();
	      }
	         catch (Exception e) {
	         System.err.println("Exception loading data file" + filename + " "
	               + e);
	         e.printStackTrace();
	      }	
	      return result;
	}
	//public void setFeatherNum(int num){
	//	feathercount=num;
	//}
	private static DecimalFormat FORMAT = new DecimalFormat("0.00000");
	
	public void getFeather(String path){
		int i,j,index=0;
		StopWordSet wordset=StopWordSet.GetInstance("stopwordu8.txt");
		StopWordSet symboset=StopWordSet.GetInstance("swsymbol.txt");
		//
		String abolutepath="C:/Program Files/eclipse/workspace/DocCluster/knncluster/";
		System.out.println("abolutepath+path:"+abolutepath+path);
		File dir=new File(abolutepath+path);
		//String[]list=dir.list();
		String []dirlist=dir.list();
		ArrayList arraylist=new ArrayList();
		for(i=0;i<dirlist.length;i++){
			System.out.println(dirlist[i]);
			File subdir=new File(abolutepath+path+"\\"+dirlist[i]);
			String []subdirlist=subdir.list();
			System.out.println(subdirlist.length);
			for(j=0;j<subdirlist.length;j++){
				arraylist.add(dirlist[i]+"\\"+subdirlist[j]);
			}
		}
		String []list=new String[arraylist.size()];
		for(i=0;i<list.length;i++){
			list[i]=(String)arraylist.get(i);
		}
		
		filelist=new String[list.length];
		filelist=list;
	//	feathermatrix=new double[list.length][feathercount];
		doc=new HashMap[list.length];
		for(i=0;i<list.length;i++){
			doc[i]=new HashMap();
		}
		
		System.out.println(doc.length);
		
		for( i=0;i<list.length;i++){
			//System.out.println("Segement:"+list[i]);
			String filedata=fileToStr(path+"\\"+list[i]);
			
			//String []wordlist= Tagger.split(filedata);
			ArrayList ret = Tagger.getFormatSegResult(filedata);
			String []wordlist=new String[ret.size()];
			String [] pos=new String[ret.size()];
			for(int k=0;k<ret.size();++k)
	      {
	    	  CnToken t = (CnToken)ret.get(k);
	    	  wordlist[k]=t.termText;
	    	  pos[k]=t.type;
	      }
		      
			//System.out.println("Create doc vector of"+list[i]);
			for(j=0;j<wordlist.length;j++){
				if(wordset.IsStopWord(wordlist[j])||symboset.IsStopWord(wordlist[j].substring(0,1))||wordlist[j].length()<2||(wordlist[j].charAt(0)>='£°'&&wordlist[j].charAt(1)<='£¹')||((wordlist[j].charAt(0)>='0'&&wordlist[j].charAt(1)<='9'))){
					continue;
				}
				
				
			if((pos[j].startsWith("n")/*&&!pos[j].equals("nx")*/)||pos[j].equals("v")){
				//	System.out.println(wordlist[j]);
					if(!worddic.containsKey(wordlist[j])){
						Pair pair1=new Pair(index,1);
						worddic.put(wordlist[j], pair1);
						Pair pair2=new Pair(index,1);
						doc[i].put(wordlist[j], pair2);
						index++;
					}
					else {
						Pair pair2=(Pair)worddic.get(wordlist[j]);
						if(!doc[i].containsKey(wordlist[j])){
							doc[i].put(wordlist[j],new Pair(pair2.id,1));
							((Pair)worddic.get(wordlist[j])).count++;
						}
						else{
							((Pair)doc[i].get(wordlist[j])).count++;
						}	
					}
				}
			}
		}

		/*for(i=0;i<doc.length;i++){
			System.out.println("word_table:"+filelist[i]);
			Iterator dociter=doc[i].keySet().iterator();
			while(dociter.hasNext()){
				String dockey=(String)dociter.next();
				Pair docpair=(Pair)doc[i].get(dockey);
				System.out.println(dockey+"		"+docpair.id+"	"+docpair.count);
			}
		}*/
		
		//System.out.println("word_dic:");
		
		/*Iterator diciter=worddic.keySet().iterator();
		while(diciter.hasNext()){
			String dickey=(String)diciter.next();
			Pair dicpair=(Pair)worddic.get(dickey);
			System.out.println(dickey+"		"+dicpair.id+"	"+dicpair.count);
		}*/
		
		fmatrix=new double [list.length][index];
		for(i=0;i<list.length;i++){
			for(j=0;j<index;j++){
				fmatrix[i][j]=0;
			}
		}
		
		for(i=0;i<doc.length;i++){
			Iterator it=doc[i].keySet().iterator();
			while(it.hasNext()){
				String word=(String)it.next();
				double tf=(double)((Pair)doc[i].get(word)).count;
				int pos=((Pair)doc[i].get(word)).id;
				double idf=(double)((Pair)worddic.get(word)).count;
				//fmatrix[i][pos]=Math.log(tf+1.0)*Math.log((double)list.length/idf);
				fmatrix[i][pos]=tf*Math.log((double)list.length/idf);
			}
			NormalizeVector(fmatrix[i]);
			/*print files that has no feathure*/
			for(j=0;j<index;j++){
				if(fmatrix[i][j]!=0.0)
					break;
			}
			
			/*if(j==index){
				//System.out.println();
				System.out.println("no feathure file is :"+filelist[i]);
			}*/
		}
		//System.out.println("The len of wordvector is :"+(index));
	
	}
	public double [][] GetFeatherMatrix(String path){
		getFeather(path);
		return fmatrix;
	}
	
	public static void main(String[] argc){
		FeathuerSelection fs=new FeathuerSelection();
		double [][]matrix=fs.GetFeatherMatrix("cluster_doc");
		for(int i=0;i<matrix.length;i++){
			for(int j=0;j<matrix[0].length;j++){
				System.out.print(matrix[i][j]+"  ");
			}
			System.out.println();
		}
	}
}
