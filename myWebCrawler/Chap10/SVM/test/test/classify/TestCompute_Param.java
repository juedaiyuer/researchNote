package test.classify;

import com.lietu.svmLight.ComputeParam;

public class TestCompute_Param {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = "D:/lg/work/xiaoxishu/model_java/svmparams.dat";
		ComputeParam com_param = new ComputeParam();
		com_param.GetFromFile(file);
		System.out.println(com_param);
		//m_lstTrainWordList.GetListFromFile(wordFile);
	}

}
