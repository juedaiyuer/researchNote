package com.lietu.simhash;

public class BitUtil {
	/** Returns the number of bits set in the long */
	public static int pop(long x) {
		int _count = _bitsSetArray65536[(int) (x& 0Xffff)]
					+ _bitsSetArray65536[(int) ((x >>> 16 )& 0xffff)]
					+ _bitsSetArray65536[(int) ((x >>> 32) & 0xffff)]
					+ _bitsSetArray65536[(int) ((x >>> 48) & 0xffff)];
		return _count;
	}

	/**
	 * 判断 x 和 y是否差别不超过K位
	 * @param x
	 * @param y
	 * @param k
	 * @return
	 */
	public static boolean diffIn(long x,long y,int k) {
		long lxor = x ^ y;
		int _count = _bitsSetArray65536[(int) (lxor& 0Xffff)];
		if(_count>k)
			return false;

		_count += _bitsSetArray65536[(int) ((lxor >>> 16 )& 0xffff)];
		if(_count>k)
			return false;

		_count +=  _bitsSetArray65536[(int) ((lxor >>> 32) & 0xffff)];
		if(_count>k)
			return false;

		_count += _bitsSetArray65536[(int) ((lxor >>> 48) & 0xffff)];
		if(_count>k)
			return false;

		return true;
	}

	public static int hamming(long l1, long l2) {
		long lxor = l1 ^ l2;
		return BitUtil.pop(lxor);
	}
	
	private static int[] _bitsSetArray65536 = null;

	static {
		_bitsSetArray65536 = new int[65536];
		
		byte[] _bitsSetArray256 = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2,
				3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1,
				2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4,
				5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3,
				4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3,
				4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4,
				5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4,
				5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3,
				4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5,
				6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3,
				4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5,
				6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6,
				7, 7, 8 };

		for (int j = 0; j < 65536; j++) {
			_bitsSetArray65536[j] = _bitsSetArray256[j & 0xff]
					+ _bitsSetArray256[(j>>> 8 )& 0xff];
		}
	}
	

	public static byte[] IntToBytes(int intValue)
	{
		byte[] result = new byte[4];
        result[0] = (byte) ( (intValue & 0xFF000000) >> 24);
		result[1] = (byte) ( (intValue & 0x00FF0000) >> 16);
        result[2] = (byte) ( (intValue & 0x0000FF00) >> 8);
        result[3] = (byte) ( (intValue & 0x000000FF));
        return result;
	}
	public static byte IntToByte(int intValue)
	{
		byte[] result = new byte[4];
        result[0] = (byte) ( (intValue & 0xFF000000) >> 24);
		result[1] = (byte) ( (intValue & 0x00FF0000) >> 16);
        result[2] = (byte) ( (intValue & 0x0000FF00) >> 8);
        result[3] = (byte) ( (intValue & 0x000000FF));
        return result[0];
	}
	public static int byte2int(byte[] res) { 
		int targets = (res[3] & 0xff) | ((res[2] << 8) & 0xff00) 
		| ((res[1] << 24) >>> 8) | (res[0] << 24); 
		return targets; 
	}
	

	
	public static int byte2int(byte b) {
		byte[] res = new byte[4];
		res[0] = 0x00;
		res[1] = 0x00;
		res[2] = 0x00;
		res[3] = b;
		int targets = (res[3] & 0xff) | ((res[2] << 8) & 0xff00) 
		| ((res[1] << 24) >>> 8) | (res[0] << 24); 
		return targets; 
	}
	public static long bytes2Long(byte[] b) {

		int mask = 0xff;
		long temp = 0;
		long res = 0;
		for (int i = 0; i < 8; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static byte[] long2Bytes(long num) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++) {
			b[i] = (byte) (num >>> (56 - i * 8));
		}
		return b;
	}
	public static byte swapBit(byte b, int start, int end)
	{
		byte temp = 0x00;
		
		return temp;
	}
	
	
	public static byte[] swapByte(byte[] bb, int i)
	{
		byte[] b = new byte[bb.length];
		for(int j=0; j<bb.length; j++)
		{
			b[j] = bb[j];
		}
		
		byte temp;
		if(i == 1)
		{
		}else if(i == 2)
		{
			temp = b[2];
			b[2] = b[0];
			b[0] = temp;
			
			temp = b[3];
			b[3] = b[1];
			b[1] = temp;
		}else if(i == 3)
		{
			temp = b[4];
			b[4] = b[0];
			b[0] = temp;
			
			temp = b[5];
			b[5] = b[1];
			b[1] = temp;
		}else if(i == 4)
		{
			temp = b[6];
			b[6] = b[0];
			b[0] = temp;
			
			temp = b[7];
			b[7] = b[1];
			b[1] = temp;
		}
		
		return b;
	}
	
	public static String encodeBytes(byte[] b)
	{
		String str = "";
		for(int i=0; i<b.length; i++)
		{
			byte low = (byte) (b[i] & 0x0f);
			byte high = (byte) (b[i] & 0xf0);
			high = (byte) (high >> 4);
			high = (byte) (high & 0x0f);
			//高向低
			str += byte2Char(high);
			str += byte2Char(low);
		}
		
		return str;
	}
	
	public static String encodeLong(Long l)
	{
		byte[] b = long2Bytes(l);
		return encodeBytes(b);
	}

	public static long decodeLong(String str)
	{
		byte[] b = decodeString(str);
		return bytes2Long(b);
	}
	
	public static byte[] decodeString(String str)
	{
		byte[] b = new byte[8];
		for(int i=0; i<str.length()/2; i++)
		{
			char high = str.charAt(i*2);
			char low = str.charAt(i*2+1);
			byte high_byte = char2halfbyte(high);
			byte low_byte = char2halfbyte(low);
			high_byte = (byte) (high_byte << 4);
			high_byte = (byte) (high_byte & 0xf0);
			b[i] = (byte) (high_byte | low_byte);
		}
		
		return b;
	}
	private static char byte2Char(byte b)
	{
		switch(b)
		{
		case(0x00):
			return '0';
		case(0x01):
			return '1';
		case(0x02):
			return '2';
		case(0x03):
			return '3';
		case(0x04):
			return '4';
		case(0x05):
			return '5';
		case(0x06):
			return '6';
		case(0x07):
			return '7';
		case(0x08):
			return '8';
		case(0x09):
			return '9';
		case(0x0a):
			return 'a';
		case(0x0b):
			return 'b';
		case(0x0c):
			return 'c';
		case(0x0d):
			return 'd';
		case(0x0e):
			return 'e';
		case(0x0f):
			return 'f';
		}
		return 'X';
	}
	private static byte char2halfbyte(char b)
	{
		switch(b)
		{
		case('0'):
			return 0x00;
		case('1'):
			return 0x01;
		case('2'):
			return 0x02;
		case('3'):
			return 0x03;
		case('4'):
			return 0x04;
		case('5'):
			return 0x05;
		case('6'):
			return 0x06;
		case('7'):
			return 0x07;
		case('8'):
			return 0x08;
		case('9'):
			return 0x09;
		case('a'):
			return 0x0a;
		case('b'):
			return 0x0b;
		case('c'):
			return 0x0c;
		case('d'):
			return 0x0d;
		case('e'):
			return 0x0e;
		case('f'):
			return 0x0f;
		}
		return 0x00;
	}
	
	public static int hammingXOR(long l1, long l2) {
		long lxor = l1 ^ l2;  //按位异或
		return BitUtil.pop(lxor); //计算1的个数
	}
}
