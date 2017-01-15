package test.classify;

import com.lietu.svmLight.Model;

public class TestModel {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String modelfile = "D:/lg/work/wq/model/model1.mdl";
		Model model = new Model(modelfile);
		
		System.out.println(model.kernel_parm);
	}

}
