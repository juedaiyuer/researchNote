package com.lietu.svmLight;

public class ModelLearn {
	public int    sv_num;	
	public long    at_upper_bound;
	public double  b;
	public Doc[]     supvec;
	public double[]  alpha;
	public int[]    index;       /* index from docnum to position in model */
	public int    totwords;     /* number of features */
	public long    totdoc;       /* number of training documents */
	public KernelParm kernel_parm; /* kernel */
	
	/* the following values are not written to file */
	public double  loo_error,loo_recall,loo_precision; /* leave-one-out estimates */
	public DoubleValue  xa_error = new DoubleValue();
	public DoubleValue xa_recall = new DoubleValue();
	public DoubleValue xa_precision = new DoubleValue();    /* xi/alpha estimates */
	public double[]  lin_weights;  	/* weights for linear case using folding */
	
	static void clear_vector_n(double[] vec,int n)
	{
		int i;
		for(i=0;i<=n;i++) vec[i]=0;
	}

	static void add_vector_ns(double[] vec_n,Word[] vec_s,double faktor)
	{
		Word ai;
		for (int i=0;i<vec_s.length;++i) {
			ai=vec_s[i];
			vec_n[(int)ai.wnum]+=(faktor*ai.weight);
		}
	}

	/* compute weight vector in linear case and add to model*/
	void add_weight_vector_to_linear_model()
	{
		int i;
		
		clear_vector_n(lin_weights,totwords);
		for(i=1;i<sv_num;i++) {
			add_vector_ns(lin_weights,(supvec[i]).words,
				alpha[i]);
		}
	}
	
	//	write model to text file.
	/*void write(String modelfile)
	{
		BufferedWriter  modelfl = new BufferedWriter(new FileWriter(modelfile));
		long j,i;
				
		{
			fprintf(modelfl,"SVM-light Version %s\n",VERSION);
			fprintf(modelfl,"%ld # kernel type\n",
				kernel_parm.kernel_type);
			fprintf(modelfl,"%ld # kernel parameter -d \n",
				kernel_parm.poly_degree);
			fprintf(modelfl,"%.8g # kernel parameter -g \n",
				kernel_parm.rbf_gamma);
			fprintf(modelfl,"%.8g # kernel parameter -s \n",
				kernel_parm.coef_lin);
			fprintf(modelfl,"%.8g # kernel parameter -r \n",
				kernel_parm.coef_const);
			//fprintf(modelfl,"%s # kernel parameter -u \n",model->kernel_parm.custom);
			fprintf(modelfl,"EmptyParam # kernel parameter -u \n");
			fprintf(modelfl,"%ld # highest feature index \n",totwords);
			fprintf(modelfl,"%ld # number of training documents \n",totdoc);
			
			fprintf(modelfl,"%ld # number of support vectors plus 1 \n",sv_num);
			fprintf(modelfl,"%.8g # threshold b \n",b);
			
			for(i=1;i<sv_num;i++) 
			{
				fprintf(modelfl,"%.32g ",alpha[i]);
				for (j=0; ((supvec[i])->words[j]).wnum; j++) 
				{
					fprintf(modelfl,"%ld:%.8g ",
						(long)((supvec[i])->words[j]).wnum,
						(double)(((supvec[i])->words[j]).weight));
				}
				fprintf(modelfl,"\n");
			}
			fclose(modelfl);
		}
	}*/
}
