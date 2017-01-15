package com.lietu.classify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class CatalogList {
	private static int m_nSaveMode; // 0 保存文档向量, 1 不保存文档向量
	public CatalogNode[] catalogList = null;

	public CatalogList() {
		catalogList = null;
	}

	public CatalogList(String strFileName) throws IOException {
		// System.out.println("CatalogList:"+strFileName);
		catalogList = null;

		LEDataInputStream fIn = new LEDataInputStream(new FileInputStream(
				strFileName));

		m_nSaveMode = fIn.readInt();
		int hashSize = fIn.readShort();
		catalogList = new CatalogNode[hashSize];
		// System.out.println("hashSize:"+hashSize);

		for (int i = 0; i < hashSize; ++i) {
			CatalogNode catalogNode = new CatalogNode();

			catalogNode.m_idxCata = fIn.readShort();
			catalogNode.m_strDirName = fIn.readGBKString();
			catalogNode.m_lTotalWordNum = fIn.readInt();
			catalogNode.m_strCatalogName = fIn.readGBKString();

			int size = fIn.readShort();
			for (int j = 0; j < size; ++j) {
				DocNode docNode = new DocNode();
				docNode.m_idxDoc = fIn.readInt();
				docNode.m_strDocName = fIn.readGBKString();
				//docNode.m_nAllocLen = 0;
				docNode.m_sWeightSet = null;
				// System.out.println(docNode);

				//docNode.m_nClassNum = 0;
				//docNode.m_pResults = null;
				catalogNode.addDoc(docNode);
			}
			// System.out.println(catalogNode);
			catalogList[i] = catalogNode;
		}
		fIn.close();

		java.util.Arrays.sort(catalogList);
	}

	public void dumpToFile(String strFileName, int type) {
		// System.out.println("dump file:"+strFileName);
		LEDataOutputStream fBinOut = null;
		try {
			fBinOut = new LEDataOutputStream(new FileOutputStream(strFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			fBinOut.writeInt(m_nSaveMode);
			fBinOut.writeShort(catalogList.length);

			for (int i = 0; i < catalogList.length; ++i) {
				CatalogNode catalogNode = catalogList[i];
				fBinOut.writeShort(catalogNode.m_idxCata);
				fBinOut.writeGBKString(catalogNode.m_strDirName);
				fBinOut.writeInt(catalogNode.m_lTotalWordNum);
				fBinOut.writeGBKString(catalogNode.m_strCatalogName);

				fBinOut.writeShort(catalogNode.docList.size());

				for (DocNode docNode : catalogNode.docList) {
					fBinOut.writeInt(docNode.m_idxDoc);
					fBinOut.writeGBKString(docNode.m_strDocName);
				}
			}

			fBinOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpDocList(String strFileName) {
		try {
			BufferedWriter stream = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(strFileName)), "GBK"));

			for (int k = 0; k < catalogList.length; ++k) {
				CatalogNode cataNode = catalogList[k];
				for (DocNode docNode : cataNode.docList) {
					if (docNode.m_sWeightSet!=null) {
						stream.write(String.valueOf(cataNode.m_idxCata + 1));
						for (int i = 0; i < docNode.m_sWeightSet.length; i++) {
							if (Math.abs(docNode.m_sWeightSet[i].s_dWeight) > Double.MIN_NORMAL)
								stream.write(String.format(" %d:%f",
										docNode.m_sWeightSet[i].s_idxWord + 1,
										docNode.m_sWeightSet[i].s_dWeight));
						}
						stream.write("\n");
					}
				}
			}

			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void initCatalogList2() {
		for (CatalogNode cataNode : catalogList) {
			cataNode.docList.clear();
		}
	}

	int getCataNum() {
		return catalogList.length;
	}

	public int getDocNum() {
		int i = 0;

		for (CatalogNode catanode : catalogList) {
			i += catanode.getDocNum();
		}
		// System.out.println("DocNum:"+i);
		return i;
	}

	String getCataName(int idxCata) {
		// System.out.println("catalog id:"+idxCata);

		// binary search
		int low = 0;
		int high = catalogList.length - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			CatalogNode midVal = catalogList[mid];
			int cmp = midVal.m_idxCata - idxCata;

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return midVal.m_strCatalogName; // key found
		}
		return null; // key not found.
	}

	// 扫描目录构建类和文档节点
	long scanDirectory(String strPath) {
		catalogList = null;
		// System.out.println("path:"+strPath);
		File dir = new File(strPath);

		File[] files = dir.listFiles();

		if (files == null) // if can't find the dir
		{
			System.out.println("目录" + strPath + "不存在!");
			return -1;
		}
		int docNum = 0;
		short idxCurCata = 0;

		ArrayList<CatalogNode> catList = new ArrayList<CatalogNode>();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				CatalogNode catalogNode = new CatalogNode();
				catalogNode.m_strCatalogName = f.getName();
				catalogNode.m_strDirName = strPath + "/"
						+ catalogNode.m_strCatalogName;
				System.out.println("path:" + catalogNode.m_strDirName);
				catalogNode.m_idxCata = idxCurCata++;

				catList.add(catalogNode);

				catalogNode.setStartDocID(docNum);
				docNum = catalogNode.scanDirectory(catalogNode.m_strDirName);
			}
		}
		catalogList = catList.toArray(new CatalogNode[catList.size()]);

		return docNum;
	}

	/**
	 * 处理训练库中的文档
	 * @param strDirName 模型库路径
	 * @return
	 */
	long buildLib(String strDirName) {
		return scanDirectory(strDirName);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(CatalogNode catNode :catalogList)
		{
			sb.append(catNode.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
