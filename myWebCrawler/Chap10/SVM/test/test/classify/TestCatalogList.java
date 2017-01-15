package test.classify;

import java.io.IOException;

import com.lietu.classify.CatalogList;

public class TestCatalogList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String file = "D:/lg/work/xiaoxishu/model/train.dat";
		CatalogList catalogList = new CatalogList(file);
		int docNum = catalogList.getDocNum();
		System.out.println("docnum:"+docNum);
		System.out.println(catalogList.toString());
	}

}
