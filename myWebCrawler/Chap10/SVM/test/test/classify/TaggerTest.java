/*
 * Created on 2005-2-9
 *
 */
package test.classify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

import com.lietu.seg.result.CnToken;
import com.lietu.seg.result.WordIndex;

/**
 * @author luogang
 *
 */
public class TaggerTest {

	public static void main(String[] args) throws Exception
	{
		//testNormalSeg();
		//testOrg();
		//testPlace();
		//testPOS();
		//testBrand();
		testFormatSeg();
		//testSplit();
		//testSplitWords();
	}
	
    public static void testFormatSeg() throws Exception{
    	long startTime;
    	long endTime;
    	
    	com.lietu.seg.result.Tagger.makeTag= true;//false;
    	com.lietu.seg.result.Tagger.segSZ = false;
    	String sentence =//"迈向充满希望的新世纪";
    		//"有关刘晓庆偷税案";
    		"在广州分别设有UPS不间断电源和免维护蓄电池生产基地。";
    		//"上海雷天软件科技有限... 公司，  ，上海雷天软件科技有限公司是一家从事通信软件制作的公司，为适应市场的高速发展，组织有更具潜力的团队，现诚聘请有资深经验，渴望成就与新挑战并能承受快速发展压力的年轻才俊加盟。户籍不限，唯才是用。 联系方式： 电 话：021-36030126 E-mail：zhujc@linghui.com 职位1： 软件开发工程师 职位描述： JAVA软件工程师（3-4人）责任：负责java软件程序编码工作，";//"其中包括兴安至全州、桂林至兴安、全州至黄沙河、阳朔至平乐、桂林至阳朔、桂林市国道过境线灵川至三塘段、平乐至钟山、桂林至三江高速公路。";
    	//"许可和权利";
    		//"许可制度";
    	//seg.result.Tagger tagger = new seg.result.Tagger();
    	ArrayList result = com.lietu.seg.result.Tagger.getFormatSegResult(sentence);
    	
    	startTime = System.currentTimeMillis();
    	for (int i=0; i<result.size();i++)
    	{
    		CnToken t = (CnToken)result.get(i);
            System.out.println(t.termText() + " " + t.startOffset() + " "
                               + t.endOffset() + " "+t.type());
        }
    	endTime = System.currentTimeMillis();
    	System.out.println("first seg time cost:" + ( endTime - startTime));
    }

    /*public static void testSplitWords() throws Exception{
    	long startTime;
    	long endTime;
    	
    	StringBuffer sentence =//"迈向充满希望的新世纪";
    		//"有关刘晓庆偷税案";
    		new StringBuffer("上海雷天软件科技有限... 公司，  ，上海雷天软件科技有限公司是一家从事通信软件制作的公司，为适应市场的高速发展，组织有更具潜力的团队，现诚聘请有资深经验，渴望成就与新挑战并能承受快速发展压力的年轻才俊加盟。户籍不限，唯才是用。 联系方式： 电 话：021-36030126 E-mail：zhujc@linghui.com 职位1： 软件开发工程师 职位描述： JAVA软件工程师（3-4人）责任：负责java软件程序编码工作，");//"其中包括兴安至全州、桂林至兴安、全州至黄沙河、阳朔至平乐、桂林至阳朔、桂林市国道过境线灵川至三塘段、平乐至钟山、桂林至三江高速公路。";
    	//"许可和权利";
    		//"许可制度";
    	int offset = 0;
    	SortedSet<WordIndex> result = seg.result.Tagger.splitWords(sentence,offset,sentence.length() - offset);
    	
    	startTime = System.currentTimeMillis();
    	
    	Iterator<WordIndex> iterator = result.iterator();
        while (iterator.hasNext())
        {
        	WordIndex item = iterator.next();
            System.out.print( item .word() + " " + item.index() + "\n" );
        }

    	//for (int i=0; i<result.size();i++)
    	//{
        //    System.out.println(result.);
        //}
    	endTime = System.currentTimeMillis();
    	System.out.println("first seg time cost:" + ( endTime - startTime));
    }*/
    
	public static void testNormalSeg() throws Exception
	{
		//seg.result.Tagger tagger = new seg.result.Tagger();
		
		//seg.result.Tagger.makeTag = false;
		com.lietu.seg.result.Tagger.segName = true;
		
		String sSentence="全亚洲仅有的两名提前录取生之一、每年4.5万美元的全额奖学金资助---复旦附中高三女生汤玫捷获得了哈佛大学最优厚的入学待遇。";
		String sSentenceResult;
		
		long startTime = System.currentTimeMillis();
		sSentenceResult= com.lietu.seg.result.Tagger.getNormalSegResult(sSentence);
		System.out.println("seg time cost:" + (System.currentTimeMillis() - startTime)); 
		
		System.out.println(sSentenceResult);
		
		sSentence="标准制定的家电厂商，";//"从美国返台的俞扬和，相当低调";
		sSentenceResult= com.lietu.seg.result.Tagger.getNormalSegResult(sSentence);
		System.out.println(sSentenceResult);
		
		sSentence="新华社记者黄智敏";
		sSentenceResult= com.lietu.seg.result.Tagger.getNormalSegResult(sSentence);
		System.out.println(sSentenceResult);
		
		sSentence="陈忠和不假迟疑地答";//"夏衍、刘白羽、姚雪垠、王蒙、李德伦、张光年、管桦、魏巍、谌容、刘长瑜、董学文等文艺界知名人士先后发言。";
		sSentenceResult= com.lietu.seg.result.Tagger.getNormalSegResult(sSentence);
		System.out.println(sSentenceResult);
	}
	
	
	public static void testPlace() throws Exception
	{
		//seg.result.Tagger tagger = new seg.result.Tagger();

		//seg.result.Tagger.makeTag = true;
		com.lietu.seg.result.Tagger.segSZ = false;
		
		String sSentence="中华人民共和国普通高等学校联合招收华侨、港澳、台湾省学生办公室（简称中国联合招办）附设在广东省招生办内，";
		String sSentenceResult;

		long startTime = System.currentTimeMillis();
		sSentenceResult = com.lietu.seg.result.Tagger.getNormalSegResult(sSentence);
		//141
		System.out.println("seg time cost:" + (System.currentTimeMillis() - startTime)); 
		System.out.println(sSentenceResult);
	}

	public static void testOrg() throws Exception
	{
		//seg.result.Tagger tagger = new seg.result.Tagger();

		//seg.result.Tagger.makeTag = true;
		com.lietu.seg.result.Tagger.segSZ = false;
		
		String sSentence="醴陵市城区,。国星陶瓷销售处。。";
		String sSentenceResult;

		long startTime = System.currentTimeMillis();
		sSentenceResult = com.lietu.seg.result.Tagger.getNormalSegResult(sSentence);
		com.lietu.seg.result.Tagger.reLoad();
		com.lietu.seg.result.Tagger.getFormatSegResult(sSentence);
		//141
		System.out.println("seg time cost:" + (System.currentTimeMillis() - startTime)); 
		System.out.println(sSentenceResult);
	}
}
