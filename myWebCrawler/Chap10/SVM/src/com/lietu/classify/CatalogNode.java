package com.lietu.classify;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class CatalogNode implements Comparable<CatalogNode> {
	public int m_idxCata;
	public String m_strCatalogName;
	public String m_strDirName;
	public int m_lTotalWordNum;
	public List<DocNode> docList = new ArrayList<DocNode>();
	private int m_lCurDocID;

	public CatalogNode() {
		m_idxCata = -1;
		m_lCurDocID = 0;
		m_lTotalWordNum = 0;
		initCatalogNode(0);
	}

	public int compareTo(CatalogNode node) {
		if (this.m_idxCata < node.m_idxCata)
			return -1;
		else if (this.m_idxCata > node.m_idxCata)
			return 1;
		return 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("m_idxCata:");
		sb.append(m_idxCata);
		sb.append('\n');

		sb.append("m_strDirName:");
		sb.append(m_strDirName);
		sb.append('\n');

		sb.append("m_lTotalWordNum:");
		sb.append(m_lTotalWordNum);
		sb.append('\n');

		sb.append("m_strCatalogName:");
		sb.append(m_strCatalogName);
		sb.append('\n');

		sb.append("DocList:\n");
		for(DocNode docNode:docList)
		{
			sb.append(docNode.toString());
		}
		return sb.toString();
	}

	public void initCatalogNode(int nMode) {
		m_lTotalWordNum = 0;

		if (nMode > 0)
			docList.clear();
	}

	public void setStartDocID(int lDocID) {
		m_lCurDocID = lDocID;
	}

	public void addDoc(DocNode docnode) {
		docList.add(docnode);
	}

	public int getDocNum() {
		return docList.size();
	}

	// 扫描目录构建类和文档节点
	public int scanDirectory(String strPath) {
		File dir = new File(strPath);

		File[] files = dir.listFiles();

		if (files == null) // if can't find the dir
		{
			String csTmp = "目录";
			csTmp += strPath;
			csTmp += "不存在!";
			System.out.println(csTmp);
			return -1;
		}
		/*
		 * for (int i = 0; i < files.length; i++) // process the catalog dir; {
		 * File f = files[i];
		 * 
		 * if (!f.isDirectory()) { addFile(f); } }
		 */
		indexDir("", dir);

		return m_lCurDocID;
	}

	private void indexDir(String subPath, File dir) {
		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				if ("".equals(subPath)) {
					indexDir(f.getName(), f); // recurse
				} else {
					indexDir(subPath + "/" + f.getName(), f); // recurse
				}
			} else
			// if (f.getName().endsWith(".txt"))
			{
				addFile(subPath, f);
			}
		}
	}

	public void addFile(String subPath, File f) {
		// System.out.println("f.getName:"+f.getName());
		DocNode docNode = new DocNode();
		if ("".equals(subPath)) {
			docNode.m_strDocName = f.getName();
		} else {
			docNode.m_strDocName = subPath + "/" + f.getName();
		}
		docNode.m_idxDoc = m_lCurDocID++;
		//docNode.m_nAllocLen = 0;
		docNode.m_sWeightSet = null;
		//docNode.m_nClassNum = 0;
		//docNode.m_pResults = null;
		addDoc(docNode);
	}
}
