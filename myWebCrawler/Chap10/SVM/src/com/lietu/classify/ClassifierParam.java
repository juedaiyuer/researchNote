package com.lietu.classify;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClassifierParam {
	//训练时需要使用的参数
	public	String m_txtTrainDir;
	public	String m_txtResultDir;
	public	int		m_nFSMode;        //特征评估函数
	public	int     m_nWordSize;      //特征数目
	public	int     m_nSelMode;       //全局打分还是按类别打分计算特征的区分度
	public	int     m_nOpMode;        //计算概率的方式
	public	int     m_nLanguageType;  //文档的语种
	public	boolean    m_bStem;          //是否进行词干抽取
	public	int     m_nWeightMode;    //特征加权的方法
	
	//分类时需要使用的参数
	public  int m_nClassifyType;              //0  单类分类; 1 多类分类
	public  boolean m_bEvaluation;               //是否需要对测试结果进行评价
	public  boolean m_bCopyFiles;                //是否将分好类的文件拷贝到结果目录下
	public  String m_strTestDir;             //测试文档或其所在的目录
	public  String m_strResultDir;           //测试结果所在的目录
	public  String m_strModelFile;
	public  int m_nKNN;                       //KNN算法的k值
	public  int m_nDocFormat;                 //测试文档的格式
	public  double m_dThreshold;              //多类分类时使用的阈值
	public  int m_nClassifierType;            //分类器的类型: -1 未知, 0 代表KNN, 1 代表SVM
	
	public String toString()
	{
		StringBuilder strParam = new StringBuilder();
		
		strParam.setLength(0);
		strParam.append("训练文档目录:\t\t");
		strParam.append(m_txtTrainDir);
		strParam.append("\r\n");
		
		strParam.append("训练结果目录:\t\t");
		strParam.append(m_txtResultDir);
		strParam.append("\r\n");
		
		strParam.append("概率估算方法:\t\t");
		if(m_nOpMode==ClassifierParam.nOpDocMode)
			strParam.append("基于文档统计\r\n");
		else if(m_nOpMode==ClassifierParam.nOpWordMode)
			strParam.append("基于词频统计\r\n");
		else
			strParam.append("未知\r\n");
		
		strParam.append("特征选择方法:\t\t");
		if(m_nFSMode==ClassifierParam.nFS_IGMode)
			strParam.append("信息增益\r\n");
		else if(m_nFSMode==ClassifierParam.nFS_MIMode)
			strParam.append("互信息\r\n");
		else if(m_nFSMode==ClassifierParam.nFS_CEMode)
			strParam.append("期望交叉熵\r\n");
		else if(m_nFSMode==ClassifierParam.nFS_X2Mode)
			strParam.append("X^2统计\r\n");
		else if(m_nFSMode==ClassifierParam.nFS_WEMode)
			strParam.append("文本证据权重\r\n");
		else if(m_nFSMode==ClassifierParam.nFS_XXMode)
			strParam.append("右半信息增益\r\n");
		else
			strParam.append("未知\r\n");

		strParam.append("特征选择方式:\t\t");
		if(m_nSelMode==ClassifierParam.nFSM_GolbalMode)
			strParam.append("全局选取\r\n");
		else if(m_nSelMode==ClassifierParam.nFSM_IndividualModel)
			strParam.append("按类别单独选取\r\n");
		else
			strParam.append("未知\r\n");

		strParam.append("文档语言种类:\t\t");
		if(m_nLanguageType==ClassifierParam.nLT_Chinese)
			strParam.append("中文\r\n");
		else if(m_nLanguageType==ClassifierParam.nLT_English)
		{
			strParam.append("英文\r\n");
			if(m_bStem)
				strParam.append("是否词干抽取:\t\t是\r\n");
			else
				strParam.append("是否词干抽取:\t\t否\r\n");
		}
		else
			strParam.append("未知\r\n");

		strParam.append("特征加权算法:\t\t");
		if(m_nWeightMode==ClassifierParam.nWM_TF_IDF)
			strParam.append("TF*IDF\r\n");
		else if(m_nWeightMode==ClassifierParam.nWM_TF_DIFF)
			strParam.append("TF*特征评估函数值\r\n");
		else if(m_nWeightMode==ClassifierParam.nWM_TF_IDF_DIFF)
			strParam.append("TF*IDF*特征评估函数值\r\n");
		else
			strParam.append("未知\r\n");

		String strWordSize;
		strWordSize=String.format("特征空间维数:\t\t%d\r\n",m_nWordSize);
		strParam.append(strWordSize);

		//CString nstrWordSize;
		//int			nDistinctWordNum = theClassifier.m_lstWordList.GetCount();
		//nstrWordSize.Format("训练文档集中不重复的单词总数:\t\t%d\r\n",nDistinctWordNum);
		//strParam+=nstrWordSize;

		if(m_nClassifierType==ClassifierParam.nCT_KNN)
			strParam.append("分类器类型: \t\tKNN\r\n");
		else if(m_nClassifierType==ClassifierParam.nCT_SVM)
			strParam.append("分类器类型: \t\tSVM\r\n");
		else
			strParam.append("请先打开一个分类模型文件!");
		
		return strParam.toString();
	}
	
	// calculation model
	public static final int nOpDocMode = 0;      // based on document number model
	public static final int nOpWordMode = 1;     // based on word number model
	
	// feature evaluation fuction
	public static final int nFS_IGMode  = 0;      // Information gain feature selection
	public static final int nFS_MIMode  = 1;      // Mutual Informaiton feature selection
	public static final int nFS_CEMode  = 2;      // Cross Entropy for text feature selection
	public static final int nFS_X2Mode  = 3;      // X^2 Statistics feature selection
	public static final int nFS_WEMode  = 4;      // Weight of Evielence for text feature selection
	public static final int nFS_XXMode  = 5;      // Right half of IG
	
	// how to select features
	public static final int nFSM_GolbalMode=0;  // 全局选
	public static final int nFSM_IndividualModel=1; // 单独选

	// classifier type
	public static final int nCT_Unknown=-1;     // Unknown
	public static final int nCT_KNN=0;         // KNN
	public static final int nCT_SVM=1;         // SVM

	// document language type
	public static final int nLT_Chinese=0;     // Chinese
	public static final int nLT_English=1;     // English

	// document format
	public static final int nDF_Directory=0;   // Directory
	public static final int nDF_Smart=1;       // Smart

	// classify type
	public static final int nFT_Single=0;      // Single Classification
	public static final int nFT_Multi=1;       // Multiple Classification

	// weight mode
	public static final int nWM_TF_IDF=0;      // TF*IDF
	public static final int nWM_TF_DIFF=1;     // TF*DIFF
	public static final int nWM_TF_IDF_DIFF=2; // TF*IDF*DIFF
	
	public ClassifierParam(){
		//训练时需要使用的参数
		m_txtTrainDir = "";
		m_txtResultDir = "";
		m_nFSMode = ClassifierParam.nFS_X2Mode;//ClassifierParam.nFS_IGMode;
		m_nWordSize = 5000;
		m_nSelMode= ClassifierParam.nFSM_GolbalMode;
		m_nOpMode= ClassifierParam.nOpWordMode;//ClassifierParam.nOpDocMode;
		m_nLanguageType= ClassifierParam.nLT_Chinese;
		m_bStem=false;
		m_nWeightMode=0;
		//分类时需要使用的参数
		m_nClassifyType=0;//单类分类
		m_bEvaluation=true;
		m_bCopyFiles=false;
		m_strTestDir="";
		m_strResultDir="";
		m_strModelFile="model";
		m_nDocFormat=ClassifierParam.nDF_Directory;
		m_nKNN=35;
		m_dThreshold=60;
		m_nClassifierType=ClassifierParam.nCT_SVM;
	}
	
	public void dumpToFile(String strFileName){
		LEDataOutputStream fBinOut = null;
		try {
			fBinOut = new LEDataOutputStream(new FileOutputStream(
					strFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			fBinOut.writeGBKString(m_txtTrainDir);
			fBinOut.writeGBKString(m_txtResultDir);
			fBinOut.writeInt(m_nFSMode);
			fBinOut.writeInt(m_nWordSize);
			fBinOut.writeInt(m_nSelMode);
			fBinOut.writeInt(m_nOpMode);
			fBinOut.writeInt(m_nLanguageType);
			fBinOut.writeBoolean(m_bStem);
			fBinOut.writeInt(m_nWeightMode);
			fBinOut.writeInt(m_nClassifyType);
			fBinOut.writeBoolean(m_bEvaluation);
			fBinOut.writeBoolean(m_bCopyFiles);
			fBinOut.writeGBKString(m_strTestDir);
			fBinOut.writeGBKString(m_strResultDir);
			fBinOut.writeGBKString(m_strModelFile);
			fBinOut.writeInt(m_nDocFormat);
			fBinOut.writeInt(m_nKNN);
			fBinOut.writeDouble(m_dThreshold);
			fBinOut.writeInt(m_nClassifierType);
			
			fBinOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean getFromFile(String strFileName)
	{
		//System.out.println("ClassifierParam.file:"+strFileName);
		
		LEDataInputStream fBinIn = null;
		
		try
		{
			fBinIn = new LEDataInputStream(new FileInputStream(
				strFileName));
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("无法打开文件"+strFileName+"!") ;
			return false;
		}

		try
		{
			//训练时需要使用的参数
			m_txtTrainDir= fBinIn.readGBKString();			
			m_txtResultDir= fBinIn.readGBKString();			
			m_nFSMode = fBinIn.readInt();
			m_nWordSize = fBinIn.readInt();
			m_nSelMode = fBinIn.readInt();
			m_nOpMode = fBinIn.readInt();
			m_nLanguageType = fBinIn.readInt();
			m_bStem = fBinIn.readBoolean();
			m_nWeightMode = fBinIn.readInt();
			//分类时需要使用的参数
			m_nClassifyType = fBinIn.readInt();
			m_bEvaluation = fBinIn.readBoolean();
			m_bCopyFiles = fBinIn.readBoolean();
			m_strTestDir= fBinIn.readGBKString();
			m_strResultDir= fBinIn.readGBKString();

			m_strModelFile= fBinIn.readGBKString();
			m_nDocFormat = fBinIn.readInt();
			m_nKNN = fBinIn.readInt();
			m_dThreshold= fBinIn.readDouble();
			m_nClassifierType = fBinIn.readInt();
			
			fBinIn.close();
		}
		catch(Exception e)
		{
			System.out.println("无法读文件"+strFileName+"!") ;
			return false;
		}
		return true;
	}
}
