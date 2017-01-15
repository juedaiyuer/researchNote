package com.lietu.classify;

import java.io.FileInputStream;

import com.lietu.svmLight.Model;
import com.lietu.svmLight.SVM;

//分类时需要用到的私有成员变量
public class Classifier {
	
	// 训练文档的类别数
	private int m_nClassNum;

	public ClassifierParam m_paramClassifier = new ClassifierParam();
	public WordList m_lstTrainWordList = new WordList();
	// 训练时需要用到的,用来保存在没有进行特征选择之前训练集中所有的特征
	public WordList m_lstWordList = new WordList();
	public CatalogList m_lstTrainCatalogList = new CatalogList();
	public CatalogList m_lstTestCatalogList;
	public SVM docSVM = new SVM();
	Model[] models;

	// 分类模型文件头标识符
	private static int dwModelFileID = 0xFFEFFFFF;

	public int SVMCategory(String content) {
		/*
		 * java.util.Calendar now = java.util.Calendar.getInstance(); if(
		 * (now.get(java.util.Calendar.YEAR) >2007) ) {
		 * System.err.print("对不起，试用期已到"); System.exit(-1); }
		 */
		com.lietu.seg.result.Tagger.segSZ = false;

		WeightNode[] weightNode=new WeightNode[m_lstTrainWordList.getCount()];
		
		int nCataID = -1;
		double[] m_pResults = SVMClassify(content, weightNode);
		if (m_pResults!=null) {
			nCataID = singleCategory(m_pResults);
		}
		return nCataID;
	}

	public Classifier(String strFileName) {
		try {
			LEDataInputStream fIn = new LEDataInputStream(new FileInputStream(
					strFileName));
			(new java.io.File(strFileName)).isFile();
			// long startTime=System.currentTimeMillis();
			// System.out.println("正在打开分类模型文件，请稍候...");
			String str = null;
			String strPath;
			int dwFileID;

			// 读入文件格式标识符
			strPath = strFileName.substring(0, strFileName.lastIndexOf('/'));
			dwFileID = fIn.readInt();

			if (dwFileID != dwModelFileID) {
				fIn.close();
				// System.out.println(dwModelFileID);
				System.out.println("分类模型文件的格式不正确!");
			}

			str = fIn.readString();
			if (!m_paramClassifier.getFromFile(strPath + "/" + str)) {
				System.out.println("无法打开训练参数文件" + str + "!");
			}
			// System.out.println(m_paramClassifier);
			m_paramClassifier.m_txtResultDir = strPath;

			if (m_paramClassifier.m_nClassifierType == ClassifierParam.nCT_KNN) {
				str = fIn.readString();
				System.out.println(str);
				m_lstTrainWordList.initWordList();
				if (!m_lstTrainWordList.getFromFile(strPath + "/" + str)) {
					System.out.println("无法打开特征类表文件" + str + "!");
				}

				str = fIn.readString();
				System.out.println(str);
				
				try
				{
					m_lstTrainCatalogList = new CatalogList(strPath + "/" + str);
				}
				catch (Exception e)
				{
					System.out.println("无法打开训练文档列表文件" + str + "!");
				}
			} else {
				str = fIn.readString();
				// System.out.println(str);
				m_lstTrainWordList.initWordList();
				if (!m_lstTrainWordList.getFromFile(strPath + "/" + str)) {
					System.out.println("无法打开特征类表文件" + str + "!");
					// return false;
				}
				// 对于SVM分类起来说m_lstTrainCatalogList其实没用
				// 此处读入它只是为了在CLeftViw中显示某些统计信息时使用
				str = fIn.readString();
				// System.out.println(str);

				try
				{
					m_lstTrainCatalogList = new CatalogList(strPath + "/" + str);
				}
				catch (Exception e)
				{
					System.out.println("无法打开训练文档列表文件" + str + "!");
				}

				str = fIn.readString();
				// System.out.println(str);
				if (!docSVM.param.GetFromFile(strPath + "/" + str)) {
					System.out.println("无法打开SVM训练参数文件" + str + "!");
					// return false;
				}
				docSVM.param.trainfile = strPath + "/train.txt";
				docSVM.param.resultpath = strPath;
			}
			fIn.close();
			
			m_nClassNum = m_lstTrainCatalogList.getCataNum();
			
			// 训练文档的个数		
			int m_lDocNum = m_lstTrainCatalogList.getDocNum();
			if(m_lDocNum<=0)
				throw new Exception("train doc num cannot be "+m_lDocNum);
			
			if (m_paramClassifier.m_nKNN > m_lDocNum)
				m_paramClassifier.m_nKNN = m_lDocNum;
			
			if (m_paramClassifier.m_nClassifierType == ClassifierParam.nCT_SVM) {
				models = new Model[m_nClassNum];

				for (int i = 0; i < m_nClassNum; i++) {
					// System.out.println("m_paramClassifier.m_strModelFile:"+
					// m_paramClassifier.m_strModelFile);
					String modelfile = m_paramClassifier.m_txtResultDir + "/"
							+ m_paramClassifier.m_strModelFile + (i + 1) + ".mdl";
					docSVM.param.modelfile = modelfile;

					models[i] = new Model(modelfile);
				}
			}
			// long totalTime=System.currentTimeMillis()-startTime;
			// System.out.println(("分类模型文件已经打开，耗时")+totalTime/1000+"秒\r\n");
			// System.out.println(m_paramClassifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 得到文档的所属类别nMaxCatID
	private int singleCategory(double[] simRatio) {
		// System.out.println("enter SingleCategory");
		
		int nCataID = 0;
		double dMaxNum = simRatio[nCataID];
		// System.out.println("SimRatio"+0+":"+pSimRatio[0]+"\t"+
		// m_lstTrainCatalogList.GetCataName(0));
		// System.out.println("ClassNum:"+m_nClassNum);
		for (int i = 1; i < m_nClassNum; i++) {
			// System.out.println("SimRatio"+i+":"+pSimRatio[i]+"\t"+
			// m_lstTrainCatalogList.GetCataName(i));
			if (simRatio[i] > dMaxNum) {
				dMaxNum = simRatio[i];
				nCataID = i;
			}
		}
		//docNode.m_nCataID = nCataID;
		return nCataID;
	}

	public String getCategoryName(String content) {
		int id = SVMCategory(content);

		String className = null;
		if (id >= 0) {
			className = m_lstTrainCatalogList.getCataName(id);
		}
		return className;
	}

	public double[] SVMClassify(String content,
			WeightNode[] weightNode) {
		DocNode docNode = new DocNode();
		
		// System.out.println("enter SVMClassify");
		int nCount = 0;
		if (m_paramClassifier.m_nLanguageType == ClassifierParam.nLT_Chinese)
			nCount = docNode.scanChineseStringWithDict(content,
					m_lstTrainWordList,weightNode);
		else
			nCount = docNode.scanEnglishStringWithDict(content,
					m_lstTrainWordList, m_paramClassifier.m_bStem,weightNode);

		if (nCount > 0) {
			com.lietu.svmLight.Doc doc = new com.lietu.svmLight.Doc();
			docNode.genDocVector(doc,weightNode);
			
			//文档与每个类别的相似度
			double[] simRatio = new double[m_nClassNum];

			for (int i = 0; i < m_nClassNum; i++) {
				if (models == null) {
					System.out.println("models[i] is null");
					return null;
				}

				simRatio[i] = docSVM.svm_classify(doc, models[i]);
			}
			return simRatio;
		}

		return null;
	}
}
