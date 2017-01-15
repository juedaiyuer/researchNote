package com.lietu.simhash;

import java.io.UnsupportedEncodingException;

/**
 * This is a very fast, non-cryptographic hash suitable for general hash-based
 * lookup. See http://murmurhash.googlepages.com/ for more details.
 * 
 * <p>
 * The C version of MurmurHash 2.0 found at that site was ported to Java by
 * Andrzej Bialecki (ab at getopt org).
 * </p>
 */
public class MurmurHash {
	public static int hash(byte[] data, int seed) {
		// 'm' and 'r' are mixing constants generated offline.
		// They're not really 'magic', they just happen to work well.
		int m = 0x5bd1e995;
		int r = 24;

		// Initialize the hash to a 'random' value
		int len = data.length;
		int h = seed ^ len;

		int i = 0;
		while (len >= 4) {
			int k = data[i + 0] & 0xFF;
			k |= (data[i + 1] & 0xFF) << 8;
			k |= (data[i + 2] & 0xFF) << 16;
			k |= (data[i + 3] & 0xFF) << 24;

			k *= m;
			k ^= k >>> r;
			k *= m;

			h *= m;
			h ^= k;

			i += 4;
			len -= 4;
		}

		switch (len) {
		case 3:
			h ^= (data[i + 2] & 0xFF) << 16;
		case 2:
			h ^= (data[i + 1] & 0xFF) << 8;
		case 1:
			h ^= (data[i + 0] & 0xFF);
			h *= m;
		}

		h ^= h >>> 13;
		h *= m;
		h ^= h >>> 15;

		return h;
	}

	// Port from: MurmurHash2, 64-bit versions, by Austin Appleby
	public static long hash64(byte[] data, int seed) {
		long m = 0xc6a4a7935bd1e995L;
		int r = 47;

		int len = data.length;
		long h = seed ^ (len * m);

		int len_4 = len >> 3;

		for (int i = 0; i < len_4; i++) {
			long k = data[i];

			k *= m;
			k ^= k >>> r;
			k *= m;

			h ^= k;
			h *= m;
		}

		switch (len & 7) {
		case 7: {
			h ^= (long) data[6 + len_4] << 48;
		}
		case 6: {
			h ^= (long) data[5 + len_4] << 40;
		}
		case 5: {
			h ^= (long) data[4 + len_4] << 32;
		}
		case 4: {
			h ^= (long) data[3 + len_4] << 24;
		}
		case 3: {
			h ^= (long) data[2 + len_4] << 16;
		}
		case 2: {
			h ^= (long) data[1 + len_4] << 8;
		}
		case 1: {
			h ^= (long) data[len_4];
		}
			h *= m;
		}

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		return h;
	}
	
	public static long hash64(int data, int seed) {
		long m = 0xc6a4a7935bd1e995L;
		int r = 47;

		int len = 4;
		long h = seed ^ (len * m);

		long k = data;

		k *= m;
		k ^= k >>> r;
		k *= m;

		h ^= k;
		h *= m;

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		return h;
	}

	public static long stringHash64(String str, int initial) {
		/*byte[] bytes = new byte[str.length()];
		for (int charCount = 0; charCount < str.length(); charCount++) {
			bytes[charCount] = (byte) str.codePointAt(charCount);
			// System.out.println(bytes[charCount]);
			// System.out.println(str.codePointAt(charCount));
		}
		// byte[] bytes = str.getBytes("UTF-8");
		 */
		byte[] bytes = str.getBytes();
		//byte[] bytes = null;
		//try {
		//	bytes = str.getBytes("UTF-8");
		//} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		return MurmurHash.hash64(bytes, initial+bytes.length);
	}

	public static int stringHash(String str, int initial)
			throws UnsupportedEncodingException {
		byte[] key = str.getBytes("UTF-8");
		/*
		 * byte[] bytes = new byte[str.length()]; for (int charCount = 0;
		 * charCount < str.length(); charCount++) { bytes[charCount] = (byte)
		 * str.codePointAt(charCount); // System.out.println(bytes[charCount]);
		 * // System.out.println(str.codePointAt(charCount)); }
		 */
		return MurmurHash.hash(key, initial);
	}

}
