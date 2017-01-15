package com.test;

public class TestMain {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		String stt = "f:\\data.txt";
		long[] iArray = { 10, 5, 2, 3, 321, 76, 3221, 98, 86, 39 };
		for (int j = 0; j < iArray.length - 1; j++) {
			for (int i = 0; i < iArray.length - 1; i++) {
				if (iArray[i] > iArray[i + 1]) {
					long k = iArray[i];
					iArray[i] = iArray[i + 1];
					iArray[i + 1] = k;
				}

			}
		}

		for (int i = 0; i < iArray.length; i++) {
			System.out.println(iArray[i]);
		}

		DetaCompress.write(iArray, stt);

		System.out.println("read");

		long cc[] = DetaCompress.read(iArray.length, stt);
		for (int i = 0; i < cc.length; i++) {
			System.out.println(cc[i]);

		}

	}

}
