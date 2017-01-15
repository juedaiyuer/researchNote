package com.lietu.svmLight;

/**
 * http://www.abb.com.cn/cawp/seitp202/affb93ed9cc3fe94c1257020002d923a.aspx
java.lang.ArrayIndexOutOfBoundsException: 0
        at com.lietu.svmLight.Model.sprod_ss(Model.java:245)
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.StringTokenizer;
import java.util.ArrayList;

public class Model {
	public int    sv_num;	
	public long    at_upper_bound;
	public double  b;
	public Doc     supvec[];
	public double  alpha[];
	public int    index[];       /* index from docnum to position in model */
	public int    totwords;     /* number of features */
	public long    totdoc;       /* number of training documents */
	public KernelParm kernel_parm = new KernelParm(); /* kernel */
	
	/* the following values are not written to file */
	public double  loo_error,loo_recall,loo_precision; /* leave-one-out estimates */
	public DoubleValue xa_error;
	public DoubleValue xa_recall;
	public DoubleValue xa_precision;    /* xi/alpha estimates */
	public double  lin_weights[];  	/* weights for linear case using folding */
	
	/* compute weight vector in linear case and add to model*/
	public void add_weight_vector_to_linear_model()
	{
		int i;
		
		clear_vector_n(lin_weights,totwords);
		for(i=1;i<sv_num;i++) {
			//for(int k=0;k<(supvec[i]).words.length;++k)
			//{
			//	System.out.println("k:"+k+":"+(supvec[i]).words[k]);
			//}
			add_vector_ns(lin_weights,(supvec[i]).words,
				alpha[i]);
		}
	}
	
	public void nol_ll(String file,
						IntValue nol,
						IntValue wol,
						IntValue ll) throws IOException
	{
		File fl = new File(file);
		int ic;
		char c;
		int current_length;
		int current_wol;
		BufferedReader br = new BufferedReader( new FileReader(fl));
		
		current_length=0;
		current_wol=0;
		(ll.value)=0;
		(nol.value)=1;
		(wol.value)=0;
		while((ic=br.read()) >=0)
		{
			c=(char)ic;
			current_length++;
			if(c == ' ') 
			{
				current_wol++;
			}
			if(c == '\n') 
			{
				(nol.value)++;
				if(current_length>(ll.value)) 
				{
					(ll.value)=current_length;
				}
				if(current_wol>(wol.value)) 
				{
					(wol.value)=current_wol;
					//System.out.println("current_wol:"+current_wol);
				}
				current_length=0;
				current_wol=0;
			}
		}
		
		br.close();
	}
	
	public Model()
	{}
	
	public Model(String modelfile) throws IOException
	{
		//System.out.println("modelfile:"+modelfile);
		IntValue llsv = new IntValue();
		IntValue max_sv = new IntValue();
		IntValue max_words = new IntValue();
		
		// scan size of model file
		nol_ll(modelfile,max_sv,max_words,llsv);
		//System.out.println("max words:"+max_words.value);
		max_words.value+=2;
		llsv.value+=2;
		supvec = new Doc[max_sv.value];
		alpha = new double[max_sv.value];

		BufferedReader modelfl = new BufferedReader(new FileReader(modelfile));
		
		int i;
		String line;
		int wnum;
		float weight;
		String version_buffer;
		
		line = modelfl.readLine();
		StringTokenizer st = new StringTokenizer(line,"SVM-light Version " );
		version_buffer = st.nextToken();
		
		if(version_buffer.equals(SVM.VERSION))
		{
			modelfl.close();
			throw new IOException("model file svm version error. Should be"+SVM.VERSION +
						" but get "+ version_buffer);
		}
		
		//fscanf(modelfl,"%ld # kernel type\n",&(kernel_parm.kernel_type));
		line = modelfl.readLine();
		//System.out.println(line);
		st = new StringTokenizer(line," " );
		String temp = st.nextToken();
		//System.out.println(temp);
		kernel_parm.kernel_type = Integer.parseInt(temp);
		
		//fscanf(modelfl,"%ld # kernel parameter -d \n",&(kernel_parm.poly_degree));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		kernel_parm.poly_degree = Integer.parseInt(st.nextToken());
		
		//fscanf(modelfl,"%lf # kernel parameter -g \n",&(kernel_parm.rbf_gamma));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		kernel_parm.rbf_gamma = Double.parseDouble(st.nextToken());
		
		//fscanf(modelfl,"%lf # kernel parameter -s \n",&(kernel_parm.coef_lin));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		kernel_parm.coef_lin = Double.parseDouble(st.nextToken());
		
		//fscanf(modelfl,"%lf # kernel parameter -r \n",&(kernel_parm.coef_const));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		kernel_parm.coef_const = Double.parseDouble(st.nextToken());
		
		//fscanf(modelfl,"%s # kernel parameter -u \n",&(kernel_parm.custom));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		kernel_parm.custom = st.nextToken();
		
		//fscanf(modelfl,"%ld # highest feature index \n",&(totwords));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		totwords = Integer.parseInt(st.nextToken());
		
		//fscanf(modelfl,"%ld # number of training documents \n",&(totdoc));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		totdoc = Integer.parseInt(st.nextToken());
		
		//fscanf(modelfl,"%ld # number of support vectors plus 1 \n",&(sv_num));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		sv_num = Integer.parseInt(st.nextToken());
		//System.out.println();
		//fscanf(modelfl,"%lf # threshold b \n",&(b));
		line = modelfl.readLine();
		st = new StringTokenizer(line," " );
		b = Double.parseDouble(st.nextToken());
		
		for(i=1;i<sv_num;i++)
		{
			line = modelfl.readLine();
			
			//System.out.println(line);
			st = new StringTokenizer(line," ");
			//sscanf(line,"%lf",&alpha[i]);
			alpha[i] = Double.parseDouble(st.nextToken());
			
			supvec[i] = new Doc();
			ArrayList<Word> words = new ArrayList<Word>();
			while(st.hasMoreTokens()) // && (wpos<max_words.value)
			{
				StringTokenizer st2 = new StringTokenizer(st.nextToken(),":");
				
				//(sscanf(line+pos,"%ld:%lf",&wnum,&weight) != EOF)
				wnum = Integer.parseInt(st2.nextToken());
				weight = Float.parseFloat(st2.nextToken());
				
				words.add(new Word(wnum,weight));
				//System.out.println("words wnum:"+wnum+" weight"+weight);
			}
			(supvec[i]).words = words.toArray(new Word[words.size()]);
			
			(supvec[i]).twonorm_sq = sprod_ss((supvec[i]).words,(supvec[i]).words);
			(supvec[i]).docnum = -1;
		}
		
		modelfl.close();

		//add by luogang
		if(kernel_parm.kernel_type == 0)
			lin_weights=new double[totwords+1];
		
		//if(modelfile.equals("D:/lg/work/wq/model/model22.mdl"))
		//{
		//	for(j=0;j<wpos;++j)
		//	{
		//		System.out.println("words["+j+"]"+ words[j]);
		//	}
		
		//for(j=0;j<100;++j)
		//{
		//	if(((supvec[1]).words[j]).wnum ==0)
		//		break;
		//	System.out.println("supvec["+1+"].words["+j+"]"+ (supvec[1]).words[j]);
		//}
		//}
	}
	
	/* compute the inner product of two sparse vectors */
	public static double sprod_ss(Word[] a,Word[] b)
	{
	    double sum=0;
	    Word ai,bj;
	    int i=0;
	    int j=0;
	    
	    //TODO: can delete a.length==0
	    if(a == null || a.length==0)
	    {
	    	return sum;
	    }
	    ai=a[i];
	    
	    if(b == null || b.length==0)
	    {
	    	return sum;
	    }
	    bj=b[j];
	    
	    while (true) {
			if(ai.wnum > bj.wnum) {
				j++;
				if(j>=b.length)
					break;
				bj=b[j];
			}
			else if (ai.wnum < bj.wnum) {
				i++;
				if(i>=a.length)
					break;
				ai=a[i];
			}
			else {
				sum+=ai.weight * bj.weight;
				i++;
				if(i>=a.length)
					break;
				j++;
				if(j>=b.length)
					break;
			    ai=a[i];
			    bj=b[j];
			}
	    }
	    
	    //System.out.println("sprod_ss:sum:"+sum);
	    return(sum);
	}
	
	public static void clear_vector_n(double[] vec,int n)
	{
		for(int i=0;i<=n;i++)
			vec[i]=0;
	}
	
	public static void add_vector_ns(double[] vec_n,Word[] vec_s,double faktor)
	{
		Word ai;
		
		for (int i=0;i<vec_s.length;++i) {
			ai = vec_s[i];
			vec_n[ai.wnum]+=(faktor*ai.weight);
			//System.out.println("ai.wnum:"+ai.wnum + " vec_n"+vec_n[ai.wnum]);
		}
	}
	
	public void write(String modelfile)
	{
		BufferedWriter modelfl = null;
		int j,i;
		
		//if (com_pro.show_action)
		//{
		//	System.out.println("Writing model file...");
		//}

		try {
			modelfl = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(modelfile)),"GBK"));
		
			modelfl.write(String.format("SVM-light Version %s\n",SVM.VERSION));
			modelfl.write(String.format("%d # kernel type\n",
				kernel_parm.kernel_type));
			modelfl.write(String.format("%d # kernel parameter -d \n",
				kernel_parm.poly_degree));
			modelfl.write(String.format("%.8g # kernel parameter -g \n",
				kernel_parm.rbf_gamma));
			modelfl.write(String.format("%.8g # kernel parameter -s \n",
				kernel_parm.coef_lin));
			modelfl.write(String.format("%.8g # kernel parameter -r \n",
				kernel_parm.coef_const));
			//fprintf(modelfl,"%s # kernel parameter -u \n",model->kernel_parm.custom);
			modelfl.write("EmptyParam # kernel parameter -u \n");
			modelfl.write(String.format("%d # highest feature index \n",totwords));
			modelfl.write(String.format("%d # number of training documents \n",totdoc));
			
			modelfl.write(String.format("%d # number of support vectors plus 1 \n",sv_num));
			modelfl.write(String.format("%.8g # threshold b \n",b));
			
			for(i=1;i<sv_num;i++)
			{
				modelfl.write(String.format("%.22g ",alpha[i]));
				
				for (j=0; ((supvec[i]).words[j]).wnum!=0; j++) 
				{
					modelfl.write(String.format("%d:%.8g ",
						(long)((supvec[i]).words[j]).wnum,
						(double)(((supvec[i]).words[j]).weight)));
				}
				modelfl.write("\n");
			}
			modelfl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
