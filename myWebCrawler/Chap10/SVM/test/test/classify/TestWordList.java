package test.classify;

import com.lietu.classify.WordList;

public class TestWordList {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String wordFile = "D:/lg/work/wq/model/features.txt";
		String wordData = "D:/lg/work/xiaoxishu/model_java/features.dat";
		
		WordList m_lstTrainWordList = new WordList();
		m_lstTrainWordList.getFromFile(wordData);
		//m_lstTrainWordList.GetListFromFile(wordFile);
	}

}
