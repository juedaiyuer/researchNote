package com.lietu.simhash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map.Entry;

/**
 * 64位分四块，最多找出有3位差别的simhash
 * 
 * @author lg
 * 
 */
// TODO: 保存排序后的中间状态
public class SimHashSet4 implements Iterable<SimHashData> {
	ArrayList<SimHashData> t1 = new ArrayList<SimHashData>();
	ArrayList<SimHashData> t2 = new ArrayList<SimHashData>();
	ArrayList<SimHashData> t3 = new ArrayList<SimHashData>();
	ArrayList<SimHashData> t4 = new ArrayList<SimHashData>();

	public ArrayList<SimHashData> getT1(){
		return t1;
	}
	static Comparator<SimHashData> comp = new Comparator<SimHashData>() {
		public int compare(SimHashData o1, SimHashData o2) {
			if (o1.q == o2.q)
				return 0;
			return (isLessThanUnsigned(o1.q, o2.q)) ? 1 : -1;
		}
	}; // 比较无符号64位
	static Comparator<Long> compHigh = new Comparator<Long>() {
		public int compare(Long o1, Long o2) {
			o1 |= 0xFFFFFFFFFFFFL;
			o2 |= 0xFFFFFFFFFFFFL;
			// System.out.println(Long.toBinaryString(o1));
			// System.out.println(Long.toBinaryString(o2));
			// System.out.println((o1 == o2));
			if (o1.equals(o2))
				return 0;
			return (isLessThanUnsigned(o1, o2)) ? 1 : -1;
		}
	}; // 比较无符号64位中的高16位

	public void load(String fileName) {
		String line = null;

		try {
			InputStream is = new FileInputStream(new File(fileName));

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				addSimHash(line.trim());
			}
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isLessThanUnsigned(long n1, long n2) {
		return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
	}

	public void sort() {
		t2.clear();
		t3.clear();
		t4.clear();
		for (SimHashData simHash : t1) 
		{
			long t = Long.rotateLeft(simHash.q, 16);
			t2.add(new SimHashData(t, simHash.no));

			t = Long.rotateLeft(t, 16);
			t3.add(new SimHashData(t, simHash.no));

			t = Long.rotateLeft(t, 16);
			t4.add(new SimHashData(t, simHash.no));
		}

		Collections.sort(t1, comp);
		Collections.sort(t2, comp);
		Collections.sort(t3, comp);
		Collections.sort(t4, comp);
	}

	public boolean contains(SimHashData key) {
		int low = 0;
		int high = t1.size() - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			SimHashData midVal = t1.get(mid);
			int cmp = comp.compare(midVal, key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return true; // key found
		}
		return false; // key not found
	}

	/**
	 * probe exact match
	 * 
	 * @param t
	 * @return
	 */
	public Span probe(ArrayList<SimHashData> t, long key) {
		// System.out.println("t:"+t.size());
		int low = 0;
		int high = t.size() - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			Long midVal = t.get(mid).q;
			int cmp = compHigh.compare(midVal, key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else {
				// key found
				int matchStart = mid;
				int matchEnd = mid;
				while (matchStart > 0) {
					midVal = t.get(matchStart - 1).q;
					if (compHigh.compare(midVal, key) == 0) {
						--matchStart;
					} else {
						break;
					}
				}

				while (matchEnd < (t.size() - 1)) {
					midVal = t.get(matchEnd + 1).q;
					if (compHigh.compare(midVal, key) == 0) {
						++matchEnd;
					} else {
						break;
					}
				}
				return new Span(matchStart, matchEnd);
			}
		}
		return null; // key not found
	}

	/**
	 * get most 3 bit difference.
	 * 
	 * @param fingerPrint
	 * @param k
	 * @return
	 */
	public HashSet<SimHashData> getSimSet(long fingerPrint, int k) {

		HashSet<SimHashData> retAll = new HashSet<SimHashData>();
		Span s1 = probe(t1, fingerPrint);
		if (s1 != null) {
			// System.out.println("s1:"+s1);
			ArrayList<SimHashData> ret1 = getSim(t1, s1, fingerPrint, k);
			retAll.addAll(ret1);
		}
		long q2 = Long.rotateLeft(fingerPrint, 16);
		Span s2 = probe(t2, q2);
		if (s2 != null) {
			// System.out.println("s2:"+s2);
			ArrayList<SimHashData> ret2 = getSim(t2, s2, q2, k);
			// rotateRight(ret2, 16);
			retAll.addAll(ret2);
		}

		long q3 = Long.rotateLeft(q2, 16);
		Span s3 = probe(t3, q3);
		if (s3 != null) {
			// System.out.println("s3:"+s3);
			ArrayList<SimHashData> ret3 = getSim(t3, s3, q3, k);
			// rotateRight(ret3, 32);
			retAll.addAll(ret3);
		}

		long q4 = Long.rotateLeft(q3, 16);
		Span s4 = probe(t4, q4);
		if (s4 != null) {
		//	System.out.println("s4:" + s4);
			ArrayList<SimHashData> ret4 = getSim(t4, s4, q4, k);
			// rotateRight(ret4, 48);
			retAll.addAll(ret4);
		}
		// System.out.println("o:"+Long.toBinaryString(fingerPrint));
		return retAll;
	}

	/**
	 * 从Span找出部分相等的，取出最多差k位的
	 * 
	 * @param t
	 * @param s
	 * @param fingerPrint
	 * @param k
	 * @return
	 */
	public ArrayList<SimHashData> getSim(ArrayList<SimHashData> t, Span s,
			long fingerPrint, int k) {
		ArrayList<SimHashData> result = new ArrayList<SimHashData>();

		for (int i = s.getStart(); i <= s.getEnd(); ++i) {
			SimHashData data = t.get(i);
			long q = data.q;
			if (BitUtil.diffIn(fingerPrint, q, k)) {
				result.add(data);
			}
		}

		return result;
	}

	public void addSimHash(String line) {
		StringTokenizer st = new StringTokenizer(line, ":");
		String key = st.nextToken();
		long t = BitUtil.decodeLong(key);
		long no = Long.parseLong(st.nextToken());
		// Long.parseLong(key,2);
		// System.out.println(t);
		t1.add(new SimHashData(t, no));
	}
	
	public void addSimHash(SimHashData key) {
		t1.add(key);
	}

	public void addInc(String key) {
		long t = BitUtil.decodeLong(key);
		// Long.parseLong(key,2);
		// System.out.println(t);
		SimHashData element = new SimHashData(t);
		int insertionPoint = findInsertionPoint(t1, element);
		t1.add(insertionPoint, element);

		long q2 = Long.rotateLeft(t, 16);
		element = new SimHashData(q2);
		insertionPoint = findInsertionPoint(t2, element);
		t2.add(insertionPoint, element);

		long q3 = Long.rotateLeft(q2, 16);
		element = new SimHashData(q3);
		insertionPoint = findInsertionPoint(t3, element);
		t3.add(insertionPoint, element);

		long q4 = Long.rotateLeft(q3, 16);
		element = new SimHashData(q4);
		insertionPoint = findInsertionPoint(t4, element);
		t4.add(insertionPoint, element);
	}

	/**
	 * Find the insertion point for the argument in a sorted list.
	 * 
	 * @param element
	 *            find this object's insertion point in the sorted list
	 * @return the index of the insertion point
	 */
	int findInsertionPoint(ArrayList<SimHashData> list, SimHashData element) {
		// Find the new element's insertion point.
		int insertionPoint = Collections.binarySearch(list, element, comp);
		if (insertionPoint < 0) {
			insertionPoint = -(insertionPoint + 1);
		}
		return insertionPoint;
	}

	public Iterator<SimHashData> iterator() {
		return t1.iterator();
	}

	public void save(String fileName) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			for (SimHashData simhash : t1) {
				//String str=BitUtil.encodeLong(simhash.q).substring(8);
				String str=BitUtil.encodeLong(simhash.q);
				writer.write(str);
//				writer.write(simhash.q+"");
				writer.write(":");
				writer.write(String.valueOf(simhash.no));
				writer.write("\r\n");
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save(String fileName, String[] newStr) {
		BufferedWriter writer;
		try {
			OutputStream out = new FileOutputStream(fileName, true);
			OutputStreamWriter outWriter = new OutputStreamWriter(out);
			writer = new BufferedWriter(outWriter);
			for (int i = 0; i < newStr.length; i++) {
				if (newStr[i] != null) {
					writer.append(newStr[i]);
					writer.append("\r\n");
					if (i % 10000 == 0)
						System.out.println(i + ":" + newStr[i]);
				} else {
					break;
				}
			}
			writer.flush();
			writer.close();
			System.out.println("结束!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 将数据读成SimHashData对象型集合
	public ArrayList<SimHashData> readData(String path) {
		ArrayList<SimHashData> list = new ArrayList<SimHashData>();

		try {
			InputStream input = new FileInputStream(new File(path));
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line = "";
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ":");
				long key = BitUtil.decodeLong(st.nextToken());
				long no = Long.parseLong(st.nextToken());
				list.add(new SimHashData(key, no));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}



}
