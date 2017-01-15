package com.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DetaCompress {

	public static byte[] longToBytes(long n) {
		byte[] buf = new byte[8];// 新建一个byte数组
		for (int i = buf.length - 1; i >= 0; i--) {
			buf[i] = (byte) (n & 0x00000000000000ff);// 取低8位的值
			n >>>= 8;// 右移8位
		}
		return buf;
	}

	// 把一个long型的数据进行压缩
	public static void writeVLong(long i, BufferedOutputStream dos)
			throws IOException {
		while ((i & ~0x7F) != 0) {
			dos.write((byte) ((i & 0x7f) | 0x80)); // 写入低位字节
			i >>>= 7; // 右移7位
		}

		dos.write((byte) i);
		// System.out.println((byte)i+"    写入低位字节");

	}

	// 把一个压缩后的long型的数据读取出来
	static long readVLong(DataInputStream dis) throws IOException {
		byte b = dis.readByte(); // 读入一个字节
		int i = b & 0x7F; // 取低7位的值
		// 每个高位的字节多乘个2的7次方，也就是128
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			if (dis.available() != 0) {
				b = dis.readByte();
				i |= (b & 0x7F) << shift; // 当前字节表示的位乘2的shift次方
			}
		}
		return i;// 返回最终结果i
	}

	// 把long型数组simHashSet写入fileName指定的文件中去
	static int write(long[] simHashSet, String fileName) {
		int j = 0;
		try {
			BufferedOutputStream dos = new BufferedOutputStream(
					new FileOutputStream(fileName));
			byte[] b = longToBytes(simHashSet[0]);// 数组的第一个数字一个转换成二进制
			dos.write(b);// 把它写到文件中
			for (int i = 1; i < simHashSet.length; i++) {
				long lo = simHashSet[i] - simHashSet[i - 1];// 用一个变量记录数组中后一个数减前一个数的差
				writeVLong(lo, dos);// 把这个差值写入文件
			}
			dos.close();
			j = simHashSet.length;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return j;
	}

	// 从fileName指定的文件中把long型数组写出来
	static long[] read(int len, String fileName) {
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					new FileInputStream(fileName)));
			long[] simHashSet = new long[len];
			simHashSet[0] = dis.readLong();// 从文件读取第一个long型数字放入数组
			for (int i = 1; i < len; i++) {
				simHashSet[i] = readVLong(dis);// 读取文件剩下的元素
				simHashSet[i] = simHashSet[i] + simHashSet[i - 1];  // 将元素都变成数组后一个数和前一个数字的和
			}
			dis.close();
			
			return simHashSet;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
