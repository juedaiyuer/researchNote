package com.lietu.simhash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.lietu.seg.result.CnToken;
import com.lietu.textSim.StopSet;

public class TextSimHash {
	public static HashSet<String> getKeyWord(HashMap<String,Integer> hmcount,int no)
	{
    	Map<String, Integer> sortMap = sortMap(hmcount);
    	HashSet<String> hsKeyWord = new HashSet<String>();
    	int flag = 0;
    	for(Map.Entry<String, Integer> entry : sortMap.entrySet()) {
    		flag++;
    		if(flag > sortMap.size()-no)
    			hsKeyWord.add(entry.getKey());
        }
    	
    	return hsKeyWord;
	}
	
	public static String[] createFeatures(String sentence,int no)
	{
		ArrayList<CnToken> result = com.lietu.seg.result.Tagger.getFormatSegResult(sentence);
    	StopSet ss = StopSet.getInstance();
    	HashMap<String,Integer> hm = new HashMap<String,Integer>();
    	
    	for (int i=0; i<result.size();i++)
    	{
    		CnToken t = result.get(i);
    		if(ss.contains(t.termText()))
    			continue;
    		if(hm.containsKey(t.termText()))
    		{
    			int number = hm.get(t.termText());
    			hm.remove(t.termText());
    			hm.put(t.termText(), number+1);
    		}
    		else
    		{
    			hm.put(t.termText(), 1);
    		}
        }
    	
    	HashSet<String> hsTempKey = getKeyWord(hm,no);
    	
    	HashSet<String> hs = new HashSet<String>();
    	
    	for (int i=0; i<result.size();i++)
    	{
    		CnToken t = result.get(i);
    		if(!hsTempKey.contains(t.termText()))
    			continue;
    		if(t.code == null)
        		hs.add(t.termText());
        	else
        		hs.add(t.code);
        }
    	
    	//String[] features = new String[hs.size()];
    	//for(int i=0;i<features.length;i++)
    	//	features[i] = (String) hs.toArray()[i];
    	String[] features = (String[]) hs.toArray(new String[hs.size()]);
    	return features;
	}
	
    public static <K, V extends Number> Map<String, V> sortMap(Map<String, V> map) {
        class MyMap<M, N> {
            private M key;
            private N value;
            private M getKey() {
                return key;
            }
            private void setKey(M key) {
                this.key = key;
            }
            private N getValue() {
                return value;
            }
            private void setValue(N value) {
                this.value = value;
            }
        }

        List<MyMap<String, V>> list = new ArrayList<MyMap<String, V>>();
        for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
            MyMap<String, V> my = new MyMap<String, V>();
            String key = i.next();
            my.setKey(key);
            my.setValue(map.get(key));
            list.add(my);
        }

        Collections.sort(list, new Comparator<MyMap<String, V>>() {
            public int compare(MyMap<String, V> o1, MyMap<String, V> o2) {
                if(o1.getValue() == o2.getValue()) {
                    return o1.getKey().compareTo(o2.getKey());
                }else{
                    return (int)(o1.getValue().doubleValue() - o2.getValue().doubleValue());
                }
            }
        });

        Map<String, V> sortMap = new LinkedHashMap<String, V>();
        for(int i = 0, k = list.size(); i < k; i++) {
            MyMap<String, V> my = list.get(i);
            sortMap.put(my.getKey(), my.getValue());
        }
        return sortMap;
    }
}
