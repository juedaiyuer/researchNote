package com.lietu.simhash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class SimHashSet {
	ArrayList<Long> simHashs = new ArrayList<Long>();

	static Comparator<Long> comp = new Comparator<Long>(){
		public int compare(Long o1, Long o2){
			if(o1.equals(o2)) return 0;
			return (isLessThanUnsigned(o1,o2)) ? 1: -1;
		}
	}; // Comparator comp
	
	public void load(String fileName) {
		String line = null;
		
		try {
			InputStream is = new FileInputStream(new File (fileName));
			
			BufferedReader br = new BufferedReader (new InputStreamReader(is)); 
			
			while ((line = br.readLine()) != null){
				addSimHash(line.trim());
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isLessThanUnsigned(long n1, long n2) {
		return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
	}

	public void sort()
	{
		Collections.sort(simHashs, comp);
	}

	public boolean contains(long key)
	{
		int low = 0;
		int high = simHashs.size()-1;

		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    Long midVal = simHashs.get(mid);
		    int cmp = comp.compare(midVal, key);

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return true; // key found
		}
		return false;  // key not found
	}

	public boolean containSim(long fingerPrint,int k) {
		if(contains(fingerPrint))
		{
			return true;
		}
		//System.out.println("o:"+Long.toBinaryString(fingerPrint));
		
		int[] indices;
		
		for(int ki=1;ki<=k;++ki)
		{
			//System.out.println("ki:"+ki);
			CombinationGenerator x = new CombinationGenerator(64, ki);
			//int count =0;
			while (x.hasMore()) {
				indices = x.getNext();
				long simFP = fingerPrint;
				for (int i = 0; i < indices.length; i++) {
					simFP = simFP ^ 1L << indices[i];
				}
				//System.out.println(Long.toBinaryString(simFP));
				if(contains(simFP))
				{
					return true;
				}
				//++count;
			}
			//System.out.println("count:"+count);
		}
		
		return false;
	}
	
	public HashSet<Long>  getSim(long fingerPrint,int k) {
		HashSet<Long> retAll = new HashSet<Long>();
		if(contains(fingerPrint))
		{
			retAll.add(fingerPrint);
		}
		//System.out.println("o:"+Long.toBinaryString(fingerPrint));
		
		int[] indices;
		
		for(int ki=1;ki<=k;++ki)
		{
			//System.out.println("ki:"+ki);
			CombinationGenerator x = new CombinationGenerator(64, ki);
			//int count =0;
			while (x.hasMore()) {
				indices = x.getNext();
				long simFP = fingerPrint;
				for (int i = 0; i < indices.length; i++) {
					simFP = simFP ^ 1L << indices[i];
				}
				//System.out.println(Long.toBinaryString(simFP));
				if(contains(simFP))
				{
					retAll.add(simFP);
					//return true;
				}
				//++count;
			}
			//System.out.println("count:"+count);
		}
		
		return retAll;
	}
	
	public void addSimHash(String key)
	{
		//long t = Long.parseLong(key,2);
		long t = BitUtil.decodeLong(key);
		//System.out.println(t);
		simHashs.add(t);
	}

	public void save(String fileName)
	{
		BufferedWriter writer;
		try{
			writer = new BufferedWriter(new FileWriter(fileName));
			for(long simhash :simHashs){
				//System.out.println(word);
				//writer.write(Long.toBinaryString(simhash));
				writer.write(BitUtil.encodeLong(simhash));
			   	writer.write("\r\n");
			}
			writer.flush();
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
