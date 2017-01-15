package com.lietu.svmLight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SVM {
	public static String VERSION = "V3.50";
	public static String VERSION_DATE = "01.11.00";
	public static int PRIMAL_OPTIMAL = 1;
	public static int DUAL_OPTIMAL = 2;
	public static int MAXITER_EXCEEDED = 3;
	public static int NAN_SOLUTION = 4;
	public static int ONLY_ONE_VARIABLE = 5;
	public static int LARGEROUND = 0;
	public static int SMALLROUND = 1;
	public static double DEF_PRECISION = 1E-5;
	public static int DEF_MAX_ITERATIONS = 200;
	public static double DEF_LINDEP_SENSITIVITY = 1E-8;
	public static double EPSILON_HIDEO = 1E-20;
	public static double EPSILON_EQ = 1E-5;
	public static int LINEAR = 0; /* linear kernel type */
	public static int POLY = 1; /* polynoial kernel type */
	public static int RBF = 2; /* rbf kernel type */
	public static int SIGMOID = 3; /* sigmoid kernel type */
	public static int CUSTOM = 4;
	public ComputePrompt com_pro = new ComputePrompt();
	public ComputeParam param = new ComputeParam();
	public ComputeResult com_result = new ComputeResult();
	private double primal[] = null;
	private double dual[];
	private long precision_violations;
	private double opt_precision;
	private long maxiter;
	private double lindep_sensitivity;
	private double buffer[];
	private int nonoptimal[];
	private long smallroundcount;

	public SVM() {
		precision_violations = 0;
		opt_precision = DEF_PRECISION;
		maxiter = DEF_MAX_ITERATIONS;
		lindep_sensitivity = DEF_LINDEP_SENSITIVITY;
		smallroundcount = 0;
	}

	private void clear_vector_n(double[] vec, long n) {
		int i;
		for (i = 0; i <= n; i++)
			vec[i] = 0;
	}

	/* compute weight vector in linear case and add to model */
	private void add_weight_vector_to_linear_model(Model model) {
		int i;

		model.lin_weights = new double[model.totwords + 1];
		clear_vector_n(model.lin_weights, model.totwords);

		for (i = 1; i < model.sv_num; i++) {
			Model.add_vector_ns(model.lin_weights, (model.supvec[i]).words,
					model.alpha[i]);
		}
	}

	/*
	 * classifies example for linear kernel important: the model must have the
	 * linear weight vector computed important: the feature numbers in the
	 * example to classify must not be larger than the weight vector!
	 */
	private double classify_example_linear(Model model, Doc ex) {
		// for(int i=0;i<model.lin_weights.length;++i)
		// {
		// if(model.lin_weights[i]!=0)
		// System.out.println("model.lin_weights["+i+"]:"+model.lin_weights[i]);
		// }

		// for(int i=0;i<ex.words.length;++i)
		// {
		// System.out.println("ex.words:"+ex.words[i].wnum);
		// }

		double ret = sprod_ns(model.lin_weights, ex.words);
		// System.out.println("classify_example_linear.sprod_ns:"+ret);
		ret -= model.b;
		// System.out.println("classify_example_linear.model.b:"+model.b);
		return (ret);
	}

	/******************************** svm_common ****************************/
	private double classify_example(Model model, Doc ex) {
		int i;
		double dist;

		dist = 0;
		for (i = 1; i < model.sv_num; i++) {
			dist += kernel(model.kernel_parm, model.supvec[i], ex)
					* model.alpha[i];
		}
		return (dist - model.b);
	}

	public int svm_classify(double[] weight) {
		Doc doc = new Doc(); /* test example */
		IntValue max_docs = new IntValue();
		IntValue max_words_doc = new IntValue();
		LongValue lld = new LongValue();
		int totdoc = 0;
		LongValue doc_label = new LongValue();
		IntValue wnum = new IntValue();
		int j;
		String line;
		BufferedReader docfl = null;
		String docfile;
		String modelfile;

		docfile = param.classifyfile;
		modelfile = param.modelfile;

		Model model = null;

		try {
			model = new Model(modelfile);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		// linear kernel
		// compute weight vector
		if (model.kernel_parm.kernel_type == 0) {
			model.add_weight_vector_to_linear_model();
		}

		if (com_pro.show_action)
			System.out.println("Classifying test examples..");

		try {
			docfl = new BufferedReader(new FileReader(docfile));
		} catch (Exception e) {
			System.out.println("file not exists:" + docfile);
			return -1;
		}

		// chen 10.9.2001
		nol_ll(docfile, max_docs, max_words_doc, lld); /*
														 * scan size of input
														 * file
														 */
		max_words_doc.value += 2;
		lld.value += 2;
		// line = (char *)my_malloc(sizeof(char)*lld);
		// Word[(int)max_words_doc.value+10];
		ArrayList<Word> words = new ArrayList<Word>();

		try {
			while ((line = docfl.readLine()) != null) {
				if (line.startsWith("#"))
					continue; /* line contains comments */
				parse_document(line, doc, doc_label, wnum, max_words_doc.value);
				if (model.kernel_parm.kernel_type == 0) {
					/* linear kernel */
					for (j = 0; (j < words.size()); j++) { /*
															 * Check if feature
															 * numbers are not
															 * larger than in
															 * model. Remove
															 * feature if
															 * necessary.
															 */
						if ((words.get(j)).wnum > model.totwords)
							(words.get(j)).wnum = 0;
					}

					weight[totdoc] = classify_example_linear(model, doc);
				} else
					/* non-linear kernel */
					weight[totdoc] = classify_example(model, doc);
				totdoc++;
			}
			docfl.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		doc.words = words.toArray(new Word[words.size()]);

		return (0);
	}

	public double svm_classify(Doc doc, Model model) {
		// System.out.println("enter classify");
		int j;
		double sim;

		doc.twonorm_sq = Model.sprod_ss(doc.words, doc.words);

		// for(int k=0;k<(model.supvec[1]).words.length;++k)
		// {
		// System.out.println((model.supvec[1]).words[k]);
		// }
		// System.out.println("doc.twonorm_sq:"+doc.twonorm_sq);

		if (model.kernel_parm.kernel_type == 0) {
			// System.out.println("totwords:"+model.totwords);
			// linear kernel, compute weight vector
			add_weight_vector_to_linear_model(model);
			for (j = 0; j < doc.words.length; j++) {
				// Check if feature numbers are not larger than in
				// model. Remove feature if necessary.
				if ((doc.words[j]).wnum > model.totwords)
					(doc.words[j]).wnum = 0;
			}

			sim = classify_example_linear(model, doc);

			// System.out.println("linear kernel sim:"+sim);
		}
		// non-linear kernel
		else {
			sim = classify_example(model, doc);
		}

		// System.exit(0);
		return sim;
	}

	// ***************************svm_learn_main****************************/
	private void set_learn_parameters(LearnParm learn_parm,
			KernelParm kernel_parm) {
		learn_parm.biased_hyperplane = param.biased_Hyperplane;
		learn_parm.remove_inconsistent = param.remove_inconsitant;
		learn_parm.skip_final_opt_check = param.final_test;
		learn_parm.svm_maxqpsize = param.maximum_size;
		learn_parm.svm_newvarsinqp = param.new_variable;
		learn_parm.svm_iter_to_shrink = param.iteration_time;
		learn_parm.svm_c = param.C;
		learn_parm.transduction_posratio = param.fraction;
		learn_parm.svm_costratio = param.cost_factor;
		learn_parm.svm_costratio_unlab = 1.0;
		learn_parm.svm_unlabbound = 1E-5;
		learn_parm.epsilon_crit = 0.001;
		learn_parm.epsilon_a = 1E-15;
		learn_parm.compute_loo = param.loo;
		learn_parm.rho = param.rho;
		learn_parm.xa_depth = param.search_depth;

		kernel_parm.kernel_type = param.kernel_type;
		kernel_parm.poly_degree = param.poly_degree;
		kernel_parm.rbf_gamma = param.rbf_gamma;
		kernel_parm.coef_lin = param.poly_s;
		kernel_parm.coef_const = param.poly_c;
		// strcpy(kernel_parm->custom,com_param.);
	}

	private void select_top_n(double[] selcrit, int range, int[] select, int n) {
		int i;
		int j;

		for (i = 0; (i < n) && (i < range); i++) { /*
													 * Initialize with the first
													 * n elements
													 */
			for (j = i; j >= 0; j--) {
				if ((j > 0) && (selcrit[select[j - 1]] < selcrit[i])) {
					select[j] = select[j - 1];
				} else {
					select[j] = i;
					j = -1;
				}
			}
		}
		for (i = n; i < range; i++) {
			if (selcrit[i] > selcrit[select[n - 1]]) {
				for (j = n - 1; j >= 0; j--) {
					if ((j > 0) && (selcrit[select[j - 1]] < selcrit[i])) {
						select[j] = select[j - 1];
					} else {
						select[j] = i;
						j = -1;
					}
				}
			}
		}
	}

	private int kernel_cache_malloc(KernelCache kernel_cache) {
		int i;

		if (kernel_cache.elems < kernel_cache.max_elems) {
			for (i = 0; i < kernel_cache.max_elems; i++) {
				if (kernel_cache.occu[i] == 0) {
					kernel_cache.occu[i] = 1;
					kernel_cache.elems++;
					return (i);
				}
			}
		}
		return (-1);
	}

	private long kernel_cache_touch(KernelCache kernel_cache, int docnum) /*
																		 * Update
																		 * lru
																		 * time
																		 * to
																		 * avoid
																		 * removal
																		 * from
																		 * cache
																		 * .
																		 */
	{
		if (kernel_cache != null && kernel_cache.index[docnum] != -1) {
			kernel_cache.lru[kernel_cache.index[docnum]] = kernel_cache.time; /* lru */
			return (1);
		}
		return (0);
	}

	private void kernel_cache_reset_lru(KernelCache kernel_cache) {
		long maxlru = 0;
		int k;

		for (k = 0; k < kernel_cache.max_elems; k++) {
			if (maxlru < kernel_cache.lru[k])
				maxlru = kernel_cache.lru[k];
		}
		for (k = 0; k < kernel_cache.max_elems; k++) {
			kernel_cache.lru[k] -= maxlru;
		}
	}

	private double sprod_ns(double[] vec_n, Word[] vec_s) {
		double sum = 0.0;
		Word ai;

		for (int i = 0; i < vec_s.length; ++i) {
			ai = vec_s[i];
			sum += (vec_n[ai.wnum] * ai.weight);
		}
		return (sum);
	}

	private long select_next_qp_subproblem_grad_cache(long[] label,
			int[] unlabeled, double[] a, double[] lin, /*
														 * Use the feasible
														 * direction approach to
														 * select the
														 */
			int totdoc, long qp_size, /*
									 * next qp-subproblem (see chapter
									 * 'Selecting a
									 */
			LearnParm learn_parm, /* good working set') among the variable with */
			boolean[] inconsistent, int[] active2dnum, int[] working2dnum, /*
																			 * cached
																			 * kernel
																			 */
			double[] selcrit, int[] select, KernelCache kernel_cache,
			int[] key, int[] chosen) {
		int choosenum;
		int i, j, k;
		int activedoc;
		int inum;
		double s;

		for (inum = 0; working2dnum[inum] >= 0; inum++)
			; /* find end of index */
		choosenum = 0;
		activedoc = 0;
		for (i = 0; (j = active2dnum[i]) >= 0; i++) {
			s = -label[j];
			if ((kernel_cache.index[j] >= 0)
					&& (!((a[j] <= (0 + learn_parm.epsilon_a)) && (s < 0)))
					&& (!((a[j] >= (learn_parm.svm_cost[j] - learn_parm.epsilon_a)) && (s > 0)))
					&& (chosen[j] == 0) && (label[j] != 0)
					&& (!inconsistent[j])) {
				selcrit[activedoc] = (double) label[j]
						* (-1.0 + (double) label[j] * lin[j]);
				key[activedoc] = j;
				activedoc++;
			}
		}
		select_top_n(selcrit, activedoc, select, (int) (qp_size / 2));
		for (k = 0; (choosenum < (qp_size / 2)) && (k < (qp_size / 2))
				&& (k < activedoc); k++) {
			i = key[select[k]];
			chosen[i] = 1;
			working2dnum[inum + choosenum] = i;
			choosenum += 1;
			kernel_cache_touch(kernel_cache, i); /*
												 * make sure it does not get
												 * kicked
												 */
			/* out of cache */
		}

		activedoc = 0;
		for (i = 0; (j = active2dnum[i]) >= 0; i++) {
			s = label[j];
			if ((kernel_cache.index[j] >= 0)
					&& (!((a[j] <= (0 + learn_parm.epsilon_a)) && (s < 0)))
					&& (!((a[j] >= (learn_parm.svm_cost[j] - learn_parm.epsilon_a)) && (s > 0)))
					&& (chosen[j] == 0) && (label[j] != 0)
					&& (!inconsistent[j])) {
				selcrit[activedoc] = -(double) (label[j] * (-1.0 + (double) label[j]
						* lin[j]));
				key[activedoc] = j;
				activedoc++;
			}
		}
		select_top_n(selcrit, activedoc, select, (int) (qp_size / 2));
		for (k = 0; (choosenum < qp_size) && (k < (qp_size / 2))
				&& (k < activedoc); k++) {
			i = key[select[k]];
			chosen[i] = 1;
			working2dnum[inum + choosenum] = i;
			choosenum += 1;
			kernel_cache_touch(kernel_cache, i); /*
												 * make sure it does not get
												 * kicked
												 */
			/* out of cache */
		}
		working2dnum[inum + choosenum] = -1; /* complete index */

		return (choosenum);
	}

	private boolean kernel_cache_check(KernelCache kernel_cache, int docnum) /*
																			 * Is
																			 * that
																			 * row
																			 * cached
																			 * ?
																			 */
	{
		return (kernel_cache.index[docnum] != -1);
	}

	private void kernel_cache_free(KernelCache kernel_cache, int i) {
		kernel_cache.occu[i] = 0;
		kernel_cache.elems--;
	}

	private boolean kernel_cache_free_lru(KernelCache kernel_cache) /*
																	 * remove
																	 * least
																	 * recently
																	 * used
																	 * cache
																	 */
	/* element */
	{
		int k;
		int least_elem = -1;
		long least_time;

		least_time = kernel_cache.time + 1;
		for (k = 0; k < kernel_cache.max_elems; k++) {
			if (kernel_cache.invindex[k] != -1) {
				if (kernel_cache.lru[k] < least_time) {
					least_time = kernel_cache.lru[k];
					least_elem = k;
				}
			}
		}
		if (least_elem != -1) {
			kernel_cache_free(kernel_cache, least_elem);
			kernel_cache.index[kernel_cache.invindex[least_elem]] = -1;
			kernel_cache.invindex[least_elem] = -1;
			return true;
		}
		return false;
	}

	private List<Double> kernel_cache_clean_and_malloc(
			KernelCache kernel_cache, int docnum) /*
												 * Get a free cache entry. In
												 * case cache is full, the lru
												 */
	{
		/* element is removed. */
		int result;
		if ((result = kernel_cache_malloc(kernel_cache)) == -1) {
			if (kernel_cache_free_lru(kernel_cache)) {
				result = kernel_cache_malloc(kernel_cache);
			}
		}
		kernel_cache.index[docnum] = result;
		if (result == -1) {
			return null;
		}
		kernel_cache.invindex[result] = docnum;
		kernel_cache.lru[kernel_cache.index[docnum]] = kernel_cache.time; /* lru */
		return (kernel_cache.buffer.subList(kernel_cache.activenum
				* kernel_cache.index[docnum], kernel_cache.buffer.size()
				- kernel_cache.activenum * kernel_cache.index[docnum]));
	}

	private void cache_kernel_row(KernelCache kernel_cache, Doc[] docs, /*
																		 * Fills
																		 * cache
																		 * for
																		 * the
																		 * row m
																		 */
	int m, KernelParm kernel_parm) {
		Doc ex;
		int j;
		int k;
		int l;
		List<Double> cache;

		if (!kernel_cache_check(kernel_cache, m)) {
			/* not cached yet */
			cache = kernel_cache_clean_and_malloc(kernel_cache, m);
			if (cache != null) {
				l = kernel_cache.totdoc2active[m];
				ex = (docs[m]);
				for (j = 0; j < kernel_cache.activenum; j++) { /* fill cache */
					k = kernel_cache.active2totdoc[j];
					if ((kernel_cache.index[k] != -1) && (l != -1) && (k != m)) {
						cache.set(j, kernel_cache.buffer
								.get(kernel_cache.activenum
										* kernel_cache.index[k] + l));
					} else {
						cache.set(j, kernel(kernel_parm, ex, (docs[k])));
					}
				}
			} else {
				System.out
						.println("Error: Kernel cache full! => increase cache size");
			}
		}
	}

	private void cache_multiple_kernel_rows(KernelCache kernel_cache,
			Doc[] docs, /* Fills cache for the rows in key */
			int[] key, long varnum, KernelParm kernel_parm) {
		int i;

		for (i = 0; i < varnum; i++) { /* fill up kernel cache */
			cache_kernel_row(kernel_cache, docs, key[i], kernel_parm);
		}
	}

	public int svm_learn_main(int pos_label) {
		Doc docs[]; /* training examples */
		long label[];
		IntValue max_docs = new IntValue();
		IntValue max_words_doc = new IntValue();
		IntValue totwords = new IntValue();
		IntValue totdoc = new IntValue();
		;
		LongValue ll = new LongValue();
		LearnParm learn_parm = new LearnParm();
		KernelParm kernel_parm = new KernelParm();
		Model model = new Model();
		String docfile;

		if (com_pro.show_action)
			System.out.println("begin to compute");
		if (com_pro.show_action)
			System.out.println("Scanning examples...");
		set_learn_parameters(learn_parm, kernel_parm);
		docfile = param.trainfile;
		// modelfile=com_param.modelfile;
		// kernel_cache_size=com_param.cache_size;

		nol_ll(docfile, max_docs, max_words_doc, ll); /* scan size of input file */
		max_words_doc.value += 2;
		ll.value += 2;
		max_docs.value += 2;
		// System.out.println("max_docs.value:"+max_docs.value);
		docs = new Doc[max_docs.value];
		for (int i = 0; i < docs.length; ++i) {
			docs[i] = new Doc();
		}
		label = new long[max_docs.value];
		read_documents(docfile, docs, label, max_words_doc.value, ll.value,
				totwords, totdoc, pos_label);
		if (kernel_parm.kernel_type == LINEAR) {
			// don't need the cache
			svm_learn(docs, label, totdoc.value, totwords.value, learn_parm,
					kernel_parm, null, model);
		} else {
			// kernel_cache_init(kernel_cache,totdoc,com_param.cache_size);
			// svm_learn(docs,label,totdoc,totwords,learn_parm,kernel_parm,
			// kernel_cache,model);
			// kernel_cache_cleanup(kernel_cache);
		}
		model.write(param.modelfile);
		if (com_pro.show_action)
			System.out.println("Cease to compute");
		return (0);
	}

	/*************************** Working set selection ***************************/
	private long select_next_qp_subproblem_grad(long[] label, int[] unlabeled,
			double[] a, double[] lin, /*
									 * Use the feasible direction approach to
									 * select the
									 */
			int totdoc, long qp_size, /*
									 * next qp-subproblem (see section
									 * 'Selecting a good
									 */
			LearnParm learn_parm, /* working set') */
			boolean[] inconsistent, int[] active2dnum, int[] working2dnum,
			double[] selcrit, int[] select, KernelCache kernel_cache,
			int[] key, int[] chosen) {
		int choosenum;
		int i;
		int j;
		int k;
		int activedoc;
		int inum;
		double s;

		for (inum = 0; working2dnum[inum] >= 0; inum++)
			; /* find end of index */
		choosenum = 0;
		activedoc = 0;
		// System.out.println("active2dnum[0]:"+active2dnum[0]);
		for (i = 0; (j = active2dnum[i]) >= 0; i++) {
			s = -label[j];
			// System.out.println("s:"+s);
			if ((!((a[j] <= (0 + learn_parm.epsilon_a)) && (s < 0)))
					&& (!((a[j] >= (learn_parm.svm_cost[j] - learn_parm.epsilon_a)) && (s > 0)))
					&& (!inconsistent[j]) && (label[j] != 0)
					&& (chosen[j] == 0)) {
				selcrit[activedoc] = lin[j] - (double) label[j];
				key[activedoc] = j;
				activedoc++;
			}
		}
		select_top_n(selcrit, activedoc, select, (int) (qp_size / 2));

		// System.out.println("qp_size:"+qp_size);//10
		// System.out.println("activedoc:"+activedoc);//2715
		for (k = 0; (choosenum < (qp_size / 2)) && (k < (qp_size / 2))
				&& (k < activedoc); k++) {
			i = key[select[k]];
			chosen[i] = 1;
			working2dnum[inum + choosenum] = i;
			choosenum += 1;
			kernel_cache_touch(kernel_cache, i); /*
												 * make sure it does not get
												 * kicked
												 */
			/* out of cache */
		}

		activedoc = 0;
		for (i = 0; (j = active2dnum[i]) >= 0; i++) {
			s = label[j];
			if ((!((a[j] <= (0 + learn_parm.epsilon_a)) && (s < 0)))
					&& (!((a[j] >= (learn_parm.svm_cost[j] - learn_parm.epsilon_a)) && (s > 0)))
					&& (!inconsistent[j]) && (label[j] != 0)
					&& (chosen[j] == 0)) {
				selcrit[activedoc] = (double) (label[j]) - lin[j];
				key[activedoc] = j;
				activedoc++;
			}
		}

		select_top_n(selcrit, activedoc, select, (int) (qp_size / 2));
		for (k = 0; (choosenum < qp_size) && (k < (qp_size / 2))
				&& (k < activedoc); k++) {
			i = key[select[k]];
			chosen[i] = 1;
			working2dnum[inum + choosenum] = i;
			choosenum += 1;
			kernel_cache_touch(kernel_cache, i); /*
												 * make sure it does not get
												 * kicked
												 */
			/* out of cache */
		}

		working2dnum[inum + choosenum] = -1; /* complete index */

		// System.out.println("select_next_qp_subproblem_grad.choosenum:"+
		// choosenum);
		return (choosenum);
	}

	private long compute_index(boolean[] binfeature, int range, int[] index)
	/* create an inverted index of binfeature */
	{
		int i, ii;

		ii = 0;
		for (i = 0; i < range; i++) {
			if (binfeature[i]) {
				index[ii] = i;
				ii++;
			}
		}
		for (i = 0; i < 4; i++) {
			index[ii + i] = -1;
		}
		return (ii);
	}

	/* create an inverted index of binfeature */
	private long compute_index(int[] binfeature, int range, int[] index) {
		int i, ii;

		ii = 0;
		// System.out.println("range:"+range);
		for (i = 0; i < range; i++) {
			if (binfeature[i] != 0) {
				// System.out.println("index["+ii+"]:"+i);
				index[ii] = i;
				ii++;
			}
		}
		for (i = 0; i < 4; i++) {
			index[ii + i] = -1;
		}
		return (ii);
	}

	/* initializes and empties index */
	private void clear_index(int[] index) {
		index[0] = -1;
	}

	private void compute_matrices_for_optimization(Doc[] docs, long[] label,
			int[] unlabeled, int[] chosen, int[] active2dnum, int[] key,
			Model model, double[] a, double[] lin, int varnum, long totdoc,
			LearnParm learn_parm, double[] aicache, KernelParm kernel_parm,
			QuadraticProgram qp) {
		//System.out.println("enter compute_matrices_for_optimization :"+varnum)
		// ;
		int ki;
		int kj;
		int i;
		int j;
		double kernel_temp;

		if (com_pro.show_compute_3) {
			System.out.println("Computing qp-matrices (type "
					+ kernel_parm.kernel_type + " kernel [degree "
					+ kernel_parm.poly_degree + ", rbf_gamma "
					+ kernel_parm.rbf_gamma + ", coef_lin "
					+ kernel_parm.coef_lin + ", coef_const "
					+ kernel_parm.coef_const + "])...");
		}

		qp.opt_n = varnum;
		qp.opt_ce0[0] = 0; /* compute the constant for equality constraint */
		for (j = 1; j < model.sv_num; j++) { /* start at 1 */
			if (chosen[(model.supvec[j]).docnum] == 0) {
				qp.opt_ce0[0] += model.alpha[j];
			}
		}
		if (learn_parm.biased_hyperplane)
			qp.opt_m = 1;
		else
			qp.opt_m = 0; /* eq-constraint will be ignored */

		/* init linear part of objective function */
		for (i = 0; i < varnum; i++) {
			qp.opt_g0[i] = lin[key[i]];
		}

		for (i = 0; i < varnum; i++) {
			ki = key[i];

			/* Compute the matrix for equality constraints */
			qp.opt_ce[i] = label[ki];
			qp.opt_low[i] = 0;
			qp.opt_up[i] = learn_parm.svm_cost[ki];

			kernel_temp = kernel(kernel_parm, (docs[ki]), (docs[ki]));
			/* compute linear part of objective function */
			qp.opt_g0[i] -= (kernel_temp * a[ki] * (double) label[ki]);
			/* compute quadratic part of objective function */
			qp.opt_g[varnum * i + i] = kernel_temp;
			for (j = i + 1; j < varnum; j++) {
				kj = key[j];
				kernel_temp = kernel(kernel_parm, (docs[ki]), (docs[kj]));
				/* compute linear part of objective function */
				qp.opt_g0[i] -= (kernel_temp * a[kj] * (double) label[kj]);
				qp.opt_g0[j] -= (kernel_temp * a[ki] * (double) label[ki]);
				/* compute quadratic part of objective function */
				qp.opt_g[varnum * i + j] = (double) label[ki]
						* (double) label[kj] * kernel_temp;
				qp.opt_g[varnum * j + i] = (double) label[ki]
						* (double) label[kj] * kernel_temp;
			}

			// if(i % 20 == 0&&com_pro.show_compute_2)
			// {
			// System.out.println(i+"..");
			// }
		}

		for (i = 0; i < varnum; i++) {
			/* assure starting at feasible point */
			qp.opt_xinit[i] = a[key[i]];
			/* set linear part of objective function */
			qp.opt_g0[i] = -1.0 + qp.opt_g0[i] * (double) label[key[i]];
		}
	}

	/* calculate the kernel function */
	private double kernel(KernelParm kernel_parm, Doc a, Doc b) {
		com_result.kernel_cache_statistic++;
		switch (kernel_parm.kernel_type) {
		case 0: /* linear */
			return (Model.sprod_ss(a.words, b.words));
		case 1: /* polynomial */
			return (Math.pow(
					kernel_parm.coef_lin * Model.sprod_ss(a.words, b.words)
							+ kernel_parm.coef_const,
					(double) kernel_parm.poly_degree));
		case 2: /* radial basis function */
			return (Math
					.exp(-kernel_parm.rbf_gamma
							* (a.twonorm_sq - 2
									* Model.sprod_ss(a.words, b.words) + b.twonorm_sq)));
		case 3: /* sigmoid neural net */
			return (Math
					.tanh(kernel_parm.coef_lin
							* Model.sprod_ss(a.words, b.words)
							+ kernel_parm.coef_const));
		case 4: /* custom-kernel supplied in file kernel.h */
			return (custom_kernel(kernel_parm, a, b));
			// chen .test sum of
			//return((CFLOAT)pow(kernel_parm->coef_lin*sprod_ss(a->words,b->words
			// )+kernel_parm->coef_const,(double)kernel_parm->poly_degree)+exp(-
			// kernel_parm
			// ->rbf_gamma*(a->twonorm_sq-2*sprod_ss(a->words,b->words
			// )+b->twonorm_sq)));
		default:
			System.out.println("Error: Unknown kernel function");
			return (-1);
		}
	}

	private void get_kernel_row(KernelCache kernel_cache, Doc[] docs, /*
																	 * Get's a
																	 * row of
																	 * the
																	 * matrix of
																	 * kernel
																	 * values
																	 */
	int docnum, long totdoc, /* This matrix has the same form as the Hessian, */
	int[] active2dnum, /* just that the elements are not multiplied by */
	double[] buffer, /* y_i y_j a_i a_j */
	KernelParm kernel_parm) /* Takes the values from the cache if available. */
	{
		int i, j;
		// ,start
		Doc ex;

		ex = (docs[docnum]);
		if (kernel_cache.index[docnum] != -1) { /* is cached? */
			kernel_cache.lru[kernel_cache.index[docnum]] = kernel_cache.time; /* lru */
			// start=kernel_cache.activenum*kernel_cache.index[docnum];
			for (i = 0; (j = active2dnum[i]) >= 0; i++) {
				if (kernel_cache.totdoc2active[j] >= 0) {
					// TODO:
					// buffer[j]=kernel_cache.buffer[start+kernel_cache.
					// totdoc2active[j]];
				} else {
					buffer[j] = kernel(kernel_parm, ex, (docs[j]));
				}
			}
		} else {
			for (i = 0; (j = active2dnum[i]) >= 0; i++) {
				buffer[j] = kernel(kernel_parm, ex, (docs[j]));
			}
		}
	}

	private void optimize_svm(Doc[] docs, /* Do optimization on the working set. */
	long[] label, int[] unlabeled, int[] chosen, int[] active2dnum,
			Model model, int totdoc, int[] working2dnum, int varnum,
			double[] a, double[] lin, LearnParm learn_parm, double[] aicache,
			KernelParm kernel_parm, QuadraticProgram qp,
			DoubleValue epsilon_crit_target) {
		int i;
		double[] a_v;

		compute_matrices_for_optimization(docs, label, unlabeled, chosen,
				active2dnum, working2dnum, model, a, lin, varnum, totdoc,
				learn_parm, aicache, kernel_parm, qp);

		if (com_pro.show_compute_3) {
			System.out.println("Running optimizer...");
		}

		/* call the qp-subsolver */
		DoubleValue model_b = new DoubleValue();
		model_b.value = model.b;
		a_v = optimize_qp(qp, epsilon_crit_target, learn_parm.svm_maxqpsize,
				model_b, learn_parm);

		model.b = model_b.value;
		for (i = 0; i < varnum; i++) {
			a[working2dnum[i]] = a_v[i];
		}
	}

	private void lcopy_matrix(double[] matrix, int depth, double[] matrix2) {
		int i;

		for (i = 0; i < (depth) * (depth); i++) {
			matrix2[i] = matrix[i];
		}
	}

	private void lswitchrk_matrix(double[] matrix, int depth, int rk1, int rk2) {
		int i;
		double temp;

		for (i = 0; i < depth; i++) {
			temp = matrix[rk1 * depth + i];
			matrix[rk1 * depth + i] = matrix[rk2 * depth + i];
			matrix[rk2 * depth + i] = temp;
		}
		for (i = 0; i < depth; i++) {
			temp = matrix[i * depth + rk1];
			matrix[i * depth + rk1] = matrix[i * depth + rk2];
			matrix[i * depth + rk2] = temp;
		}
	}

	private void linvert_matrix(double[] matrix, int depth, double[] inverse,
			double lindep_sensitivity, int[] lin_dependent) /*
															 * indicates the
															 * active parts of
															 * matrix on input
															 * and output
															 */
	{
		int i, j, k;
		double factor;

		for (i = 0; i < depth; i++) {
			/* lin_dependent[i]=0; */
			for (j = 0; j < depth; j++) {
				inverse[i * depth + j] = 0.0;
			}
			inverse[i * depth + i] = 1.0;
		}

		for (i = 0; i < depth; i++) {
			if (lin_dependent[i] != 0
					|| (Math.abs(matrix[i * depth + i]) < lindep_sensitivity)) {
				lin_dependent[i] = 1;
			} else {
				for (j = i + 1; j < depth; j++) {
					factor = matrix[j * depth + i] / matrix[i * depth + i];
					for (k = i; k < depth; k++) {
						matrix[j * depth + k] -= (factor * matrix[i * depth + k]);
					}
					for (k = 0; k < depth; k++) {
						inverse[j * depth + k] -= (factor * inverse[i * depth
								+ k]);
					}
				}
			}
		}

		for (i = depth - 1; i >= 0; i--) {
			if (lin_dependent[i] == 0) {
				factor = 1 / matrix[i * depth + i];
				for (k = 0; k < depth; k++) {
					inverse[i * depth + k] *= factor;
				}
				matrix[i * depth + i] = 1;
				for (j = i - 1; j >= 0; j--) {
					factor = matrix[j * depth + i];
					matrix[j * depth + i] = 0;
					for (k = 0; k < depth; k++) {
						inverse[j * depth + k] -= (factor * inverse[i * depth
								+ k]);
					}
				}
			}
		}
	}

	private int solve_dual(
	/* Solves the dual using the method of Hildreth and D'Espo. */
	/* Can only handle problems with zero or exactly one */
	/* equality constraints. */
	int n, /* number of variables */
	int m, /* number of linear equality constraints */
	double precision, /* solve at least to this dual precision */
	double epsilon_crit, /* stop, if KT-Conditions approx fulfilled */
	long maxiter, /* stop after that many iterations */
	double[] g, double[] g0, /* linear part of objective */
	double[] ce, double[] ce0, /* linear equality constraints */
	double[] low, double[] up, /* box constraints */
	double[] primal, /* variables (with initial values) */
	double[] d, double[] d0, double[] ig, double[] dual, double[] dual_old,
			double[] temp, /* buffer */
			long goal) {
		int i;
		int j = 0;
		int k;
		long iter;
		double sum, w, maxviol, viol, temp1, temp2, isnantest;
		double model_b, dist;
		long retrain, maxfaktor, primal_optimal = 0, at_bound, scalemaxiter;
		double epsilon_a = 1E-15, epsilon_hideo;
		double eq;

		if ((m < 0) || (m > 1))
			System.out
					.println("SOLVE DUAL: inappropriate number of eq-constrains!");

		for (i = 0; i < 2 * (n + m); i++) {
			dual[i] = 0;
			dual_old[i] = 0;
		}

		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) { /* dual hessian for box constraints */
				d[i * 2 * (n + m) + j] = ig[i * n + j];
				d[(i + n) * 2 * (n + m) + j] = -ig[i * n + j];
				d[i * 2 * (n + m) + j + n] = -ig[i * n + j];
				d[(i + n) * 2 * (n + m) + j + n] = ig[i * n + j];
			}
			if (m > 0) {
				sum = 0; /* dual hessian for eq constraints */
				for (j = 0; j < n; j++) {
					sum += (ce[j] * ig[i * n + j]);
				}
				d[i * 2 * (n + m) + 2 * n] = sum;
				d[i * 2 * (n + m) + 2 * n + 1] = -sum;
				d[(n + i) * 2 * (n + m) + 2 * n] = -sum;
				d[(n + i) * 2 * (n + m) + 2 * n + 1] = sum;
				d[(n + n) * 2 * (n + m) + i] = sum;
				d[(n + n + 1) * 2 * (n + m) + i] = -sum;
				d[(n + n) * 2 * (n + m) + (n + i)] = -sum;
				d[(n + n + 1) * 2 * (n + m) + (n + i)] = sum;

				sum = 0;
				for (j = 0; j < n; j++) {
					for (k = 0; k < n; k++) {
						sum += (ce[k] * ce[j] * ig[j * n + k]);
					}
				}
				d[(n + n) * 2 * (n + m) + 2 * n] = sum;
				d[(n + n) * 2 * (n + m) + 2 * n + 1] = -sum;
				d[(n + n + 1) * 2 * (n + m) + 2 * n] = -sum;
				d[(n + n + 1) * 2 * (n + m) + 2 * n + 1] = sum;
			}
		}

		for (i = 0; i < n; i++) { /* dual linear component for the box constraints */
			w = 0;
			for (j = 0; j < n; j++) {
				w += (ig[i * n + j] * g0[j]);
			}
			d0[i] = up[i] + w;
			d0[i + n] = -low[i] - w;
		}

		if (m > 0) {
			sum = 0; /* dual linear component for eq constraints */
			for (j = 0; j < n; j++) {
				for (k = 0; k < n; k++) {
					sum += (ce[k] * ig[k * n + j] * g0[j]);
				}
			}
			d0[2 * n] = ce0[0] + sum;
			d0[2 * n + 1] = -ce0[0] - sum;
		}

		maxviol = 999999;
		iter = 0;
		retrain = 1;
		maxfaktor = 1;
		scalemaxiter = maxiter / 5;
		while ((retrain != 0) && (maxviol > 0)
				&& (iter < (scalemaxiter * maxfaktor))) {
			iter++;

			while ((maxviol > precision) && (iter < (scalemaxiter * maxfaktor))) {
				iter++;
				maxviol = 0;
				for (i = 0; i < 2 * (n + m); i++) {
					sum = d0[i];
					for (j = 0; j < 2 * (n + m); j++) {
						sum += d[i * 2 * (n + m) + j] * dual_old[j];
					}
					sum -= d[i * 2 * (n + m) + i] * dual_old[i];
					dual[i] = -sum / d[i * 2 * (n + m) + i];
					if (dual[i] < 0)
						dual[i] = 0;

					viol = Math.abs(dual[i] - dual_old[i]);
					if (viol > maxviol)
						maxviol = viol;
					dual_old[i] = dual[i];
				}
				/*
				 * sprintf(temstr,"%d) maxviol=%20f precision=%f\n",iter,maxviol,
				 * precision);
				 */
			}

			if (m > 0) {
				for (i = 0; i < n; i++) {
					temp[i] = dual[i] - dual[i + n] + ce[i]
							* (dual[n + n] - dual[n + n + 1]) + g0[i];
				}
			} else {
				for (i = 0; i < n; i++) {
					temp[i] = dual[i] - dual[i + n] + g0[i];
				}
			}
			for (i = 0; i < n; i++) {
				primal[i] = 0; /* calc value of primal variables */
				for (j = 0; j < n; j++) {
					primal[i] += ig[i * n + j] * temp[j];
				}
				primal[i] *= -1.0;
				if (primal[i] <= (low[i])) { /* clip conservatively */
					primal[i] = low[i];
				} else if (primal[i] >= (up[i])) {
					primal[i] = up[i];
				}
			}

			if (m > 0)
				model_b = dual[n + n + 1] - dual[n + n];
			else
				model_b = 0;

			isnantest = 0;
			epsilon_hideo = EPSILON_HIDEO;
			for (i = 0; i < n; i++) { /* check precision of alphas */
				// isnantest+=primal[j];
				dist = -model_b * ce[i];
				dist += (g0[i] + 1.0);
				for (j = 0; j < i; j++) {
					dist += (primal[j] * g[j * n + i]);
				}
				for (j = i; j < n; j++) {
					dist += (primal[j] * g[i * n + j]);
				}
				if ((primal[i] < (up[i] - epsilon_hideo))
						&& (dist < (1.0 - epsilon_crit))) {
					epsilon_hideo = (up[i] - primal[i]) * 2.0;
				} else if ((primal[i] > (low[i] + epsilon_hideo))
						&& (dist > (1.0 + epsilon_crit))) {
					epsilon_hideo = (primal[i] - low[i]) * 2.0;
				}
			}
			/* sprintf(temstr,"\nEPSILON_HIDEO=%.30f\n",epsilon_hideo); */

			for (i = 0; i < n; i++) { /* clip alphas to bounds */
				if (primal[i] <= (low[i] + epsilon_hideo)) {
					primal[i] = low[i];
				} else if (primal[i] >= (up[i] - epsilon_hideo)) {
					primal[i] = up[i];
				}
			}

			retrain = 0;
			primal_optimal = 1;
			at_bound = 0;
			for (i = 0; (i < n); i++) { /* check primal KT-Conditions */
				dist = -model_b * ce[i];
				dist += (g0[i] + 1.0);
				for (j = 0; j < i; j++) {
					dist += (primal[j] * g[j * n + i]);
				}
				for (j = i; j < n; j++) {
					dist += (primal[j] * g[i * n + j]);
				}
				if ((primal[i] < (up[i] - epsilon_a))
						&& (dist < (1.0 - epsilon_crit))) {
					retrain = 1;
					primal_optimal = 0;
				} else if ((primal[i] > (low[i] + epsilon_a))
						&& (dist > (1.0 + epsilon_crit))) {
					retrain = 1;
					primal_optimal = 0;
				}
				if ((primal[i] <= (low[i] + epsilon_a))
						|| (primal[i] >= (up[i] - epsilon_a))) {
					at_bound++;
				}
				/*
				 * sprintf(temstr,"HIDEOtemp: a[%ld]=%.30f, dist=%.6f, b=%f, at_bound=%ld\n"
				 * ,i,primal[i],dist,model_b,at_bound);
				 */
			}
			if (m > 0) {
				eq = -ce0[0]; /* check precision of eq-constraint */
				for (i = 0; i < n; i++) {
					eq += (ce[i] * primal[i]);
				}
				if ((EPSILON_EQ < Math.abs(eq))
				/*
				 * && !((goal==PRIMAL_OPTIMAL) && (at_bound==n))
				 */
				) {
					retrain = 1;
					primal_optimal = 0;
				}
				/*
				 * sprintf(temstr,"\n eq=%.30f ce0=%f at-bound=%ld\n",eq,ce0[0],at_bound
				 * );
				 */
			}

			if (retrain != 0) {
				precision /= 10;
				if (((goal == PRIMAL_OPTIMAL) && (maxfaktor < 50000))
						|| (maxfaktor < 5)) {
					maxfaktor++;
				}
			}
		}

		if (primal_optimal == 0) {
			for (i = 0; i < n; i++) {
				primal[i] = 0; /* calc value of primal variables */
				for (j = 0; j < n; j++) {
					primal[i] += ig[i * n + j] * temp[j];
				}
				primal[i] *= -1.0;
				if (primal[i] <= (low[i] + epsilon_a)) { /* clip conservatively */
					primal[i] = low[i];
				} else if (primal[i] >= (up[i] - epsilon_a)) {
					primal[i] = up[i];
				}
			}
		}

		isnantest = 0;
		for (i = 0; i < n; i++) { /* check for isnan */
			isnantest += primal[i];
		}

		if (m > 0) {
			temp1 = dual[n + n + 1]; /* copy the dual variables for the eq */
			temp2 = dual[n + n]; /* constraints to a handier location */
			for (i = n + n + 1; i >= 2; i--) {
				dual[i] = dual[i - 2];
			}
			dual[0] = temp2;
			dual[1] = temp1;
			isnantest += temp1 + temp2;
		}

		// TODO: _isnan
		if ((isnantest == Double.MAX_VALUE)) {
			return ((int) NAN_SOLUTION);
		} else if (primal_optimal != 0) {
			return ((int) PRIMAL_OPTIMAL);
		} else if (maxviol == 0.0) {
			return ((int) DUAL_OPTIMAL);
		} else {
			return ((int) MAXITER_EXCEEDED);
		}
	}

	private int optimize_hildreth_despo(int n, /* number of variables */
	int m, /* number of linear equality constraints [0,1] */
	double precision, /* solve at least to this dual precision */
	double epsilon_crit, /* stop, if KT-Conditions approx fulfilled */
	double epsilon_a, /* precision of alphas at bounds */
	long maxiter, /* stop after this many iterations */
	long goal, /* keep going until goal fulfilled */
	long smallround, /* use only two variables of steepest descent */
	double lindep_sensitivity, /* epsilon for detecting linear dependent ex */
	double[] g, /* hessian of objective */
	double[] g0, /* linear part of objective */
	double[] ce, double[] ce0, /* linear equality constraints */
	double[] low, double[] up, /* box constraints */
	double[] primal, /* primal variables */
	double[] init, /* initial values of primal */
	double[] dual, /* dual variables */
	int[] lin_dependent, double[] buffer) {
		int i;
		int j;
		int k;
		int from, to;
		int n_indep;
		long changed;
		double sum, bmin = 0, bmax = 0;
		double[] d = null;
		double[] d0 = null;
		double[] ig = null;
		double[] dual_old = null;
		double[] temp = null;
		double[] start = null;
		double[] g0_new;
		double[] g_new = null;
		double[] ce_new = null;
		double[] ce0_new = null;
		double[] low_new = null;
		double[] up_new = null;
		double add, t;
		int result;
		// double obj_before,obj_after;
		int b1;
		int b2;

		// System.out.println("n:"+n);

		g0_new = (buffer); /* claim regions of buffer */
		d = Arrays.copyOfRange(buffer, n, buffer.length);
		d0 = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2,
				buffer.length);
		ce_new = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2, buffer.length);
		ce0_new = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2 + n, buffer.length);
		ig = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2 + (n + m)
				* 2 + n + m, buffer.length);
		dual_old = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2 + n + m + n * n, buffer.length);
		low_new = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2 + n + m + n * n + (n + m) * 2, buffer.length);
		up_new = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2 + n + m + n * n + (n + m) * 2 + n, buffer.length);
		start = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2 + n + m + n * n + (n + m) * 2 + n + n,
				buffer.length);
		g_new = Arrays.copyOfRange(buffer, n + (n + m) * 2 * (n + m) * 2
				+ (n + m) * 2 + n + m + n * n + (n + m) * 2 + n + n + n,
				buffer.length);
		temp = Arrays.copyOfRange(buffer,
				n + (n + m) * 2 * (n + m) * 2 + (n + m) * 2 + n + m + n * n
						+ (n + m) * 2 + n + n + n + n * n, buffer.length);

		b1 = -1;
		b2 = -1;
		for (i = 0; i < n; i++) { /* get variables with steepest feasible descent */
			sum = g0[i];
			for (j = 0; j < n; j++)
				sum += init[j] * g[i * n + j];
			sum = sum * ce[i];
			if (((b1 == -1) || (sum < bmin))
					&& (!((init[i] <= (low[i] + epsilon_a)) && (ce[i] < 0.0)))
					&& (!((init[i] >= (up[i] - epsilon_a)) && (ce[i] > 0.0)))) {
				// System.out.println("b1 != -1");
				bmin = sum;
				b1 = i;
			}
			if (((b2 == -1) || (sum > bmax))
					&& (!((init[i] <= (low[i] + epsilon_a)) && (ce[i] > 0.0)))
					&& (!((init[i] >= (up[i] - epsilon_a)) && (ce[i] < 0.0)))) {
				bmax = sum;
				b2 = i;
			}
		}

		/* in case of unbiased hyperplane, the previous projection on */
		/* equality constraint can lead to b1 or b2 being -1. */
		if ((b1 == -1) || (b2 == -1)) {
			b1 = Math.max(b1, b2);
			b2 = Math.max(b1, b2);
		}

		for (i = 0; i < n; i++) {
			start[i] = init[i];
		}

		/* in case both examples are equal */
		add = 0;
		changed = 0;
		// TODO: over flow
		if ((b1 * n + b2) < 0 || (b1 * n + b1) < 0 || (b1 * n + b2) < 0
				|| (b2 * n + b2) < 0) {
			System.out.println("b1*n+b2 " + (b1 * n + b2));
			System.out.println("b1*n+b1 " + (b1 * n + b1));
			System.out.println("b1*n+b2 " + (b1 * n + b2));
			System.out.println("b2*n+b2 " + (b2 * n + b2));
			System.out.println("n:" + n + " b1:" + b1 + " b2:" + b2);
		}
		// END TODO:
		else if ((-g[b1 * n + b2] == g[b1 * n + b1])
				&& (-g[b1 * n + b2] == g[b2 * n + b2])) {
			changed = 1;
			if (ce[b1] == ce[b2]) { /* distribute evenly */
				start[b1] = (init[b1] + init[b2]) / 2.0;
				start[b2] = (init[b1] + init[b2]) / 2.0;
				if (start[b2] > up[b2]) {
					t = start[b2] - up[b2];
					start[b2] = up[b2];
					start[b1] += t;
				}
				if (start[b1] > up[b1]) {
					t = start[b1] - up[b1];
					start[b1] = up[b1];
					start[b2] += t;
				}
			} else { /* set to upper bound */
				t = up[b1] - init[b1];
				if ((up[b2] - init[b2]) < t) {
					t = up[b2] - init[b2];
				}
				start[b1] = init[b1] + t;
				start[b2] = init[b2] + t;
			}
		}

		/* if we have a biased hyperplane, then adding a constant to the */
		/* hessian does not change the solution. So that is done for examples */
		/* with zero diagonal entry, since HIDEO cannot handle them. */
		else if ((Math.abs(g[b1 * n + b1]) < lindep_sensitivity)
				|| (Math.abs(g[b2 * n + b2]) < lindep_sensitivity)) {
			add += 0.093274;
		}
		/* in case both examples are linear dependent */
		else if (Math.abs(g[b1 * n + b1] / g[b1 * n + b2] - g[b1 * n + b2]
				/ g[b2 * n + b2]) < lindep_sensitivity) {
			add += 0.078274;
		}

		/* sprintf(temstr,"b1=%ld,b2=%ld\n",b1,b2); */

		lcopy_matrix(g, n, d);
		if ((m == 1) && (add > 0.0)) {
			for (j = 0; j < n; j++) {
				for (k = 0; k < n; k++) {
					d[j * n + k] += add * ce[j] * ce[k];
				}
			}
		} else {
			add = 0.0;
		}

		if (n > 2) { /* switch, so that variables are better mixed */
			lswitchrk_matrix(d, n, b1, 0);
			if (b2 == 0)
				lswitchrk_matrix(d, n, b1, 1);
			else
				lswitchrk_matrix(d, n, b2, 1);
		}
		if (smallround == SMALLROUND) {
			for (i = 2; i < n; i++) {
				lin_dependent[i] = 1;
			}
			lin_dependent[0] = 0;
			lin_dependent[1] = 0;
		} else {
			for (i = 0; i < n; i++) {
				lin_dependent[i] = 0;
			}
		}
		linvert_matrix(d, n, ig, lindep_sensitivity, lin_dependent);
		if (n > 2) { /* now switch back */
			if (b2 == 0) {
				lswitchrk_matrix(ig, n, b1, 1);
				i = lin_dependent[1];
				lin_dependent[1] = lin_dependent[b1];
				lin_dependent[b1] = i;
			} else {
				lswitchrk_matrix(ig, n, b2, 1);
				i = lin_dependent[1];
				lin_dependent[1] = lin_dependent[b2];
				lin_dependent[b2] = i;
			}
			lswitchrk_matrix(ig, n, b1, 0);
			i = lin_dependent[0];
			lin_dependent[0] = lin_dependent[b1];
			lin_dependent[b1] = i;
		}
		/* lprint_matrix(d,n); */
		/* lprint_matrix(ig,n); */

		lcopy_matrix(g, n, g_new); /* restore g_new matrix */
		if (add > 0)
			for (j = 0; j < n; j++) {
				for (k = 0; k < n; k++) {
					g_new[j * n + k] += add * ce[j] * ce[k];
				}
			}

		for (i = 0; i < n; i++) { /* fix linear dependent vectors */
			g0_new[i] = g0[i] + add * ce0[0] * ce[i];
		}
		if (m > 0)
			ce0_new[0] = -ce0[0];
		for (i = 0; i < n; i++) { /* fix linear dependent vectors */
			if (lin_dependent[i] != 0) {
				for (j = 0; j < n; j++) {
					if (lin_dependent[j] == 0) {
						g0_new[j] += start[i] * g_new[i * n + j];
					}
				}
				if (m > 0)
					ce0_new[0] -= (start[i] * ce[i]);
			}
		}
		from = 0; /* remove linear dependent vectors */
		to = 0;
		n_indep = 0;
		for (i = 0; i < n; i++) {
			if (lin_dependent[i] == 0) {
				g0_new[n_indep] = g0_new[i];
				ce_new[n_indep] = ce[i];
				low_new[n_indep] = low[i];
				up_new[n_indep] = up[i];
				primal[n_indep] = start[i];
				n_indep++;
			}
			for (j = 0; j < n; j++) {
				if ((lin_dependent[i] == 0) && (lin_dependent[j] == 0)) {
					ig[to] = ig[from];
					g_new[to] = g_new[from];
					to++;
				}
				from++;
			}
		}

		/* cannot optimize with only one variable */
		if ((n_indep <= 1) && (m > 0) && (changed == 0)) {
			for (i = n - 1; i >= 0; i--) {
				primal[i] = init[i];
			}
			return ((int) ONLY_ONE_VARIABLE);
		}

		result = solve_dual(n_indep, m, precision, epsilon_crit, maxiter,
				g_new, g0_new, ce_new, ce0_new, low_new, up_new, primal, d, d0,
				ig, dual, dual_old, temp, goal);

		j = n_indep;
		for (i = n - 1; i >= 0; i--) {
			if (lin_dependent[i] == 0) {
				j--;
				primal[i] = primal[j];
			} else if ((m == 0) && (g[i * n + i] == 0)) {
				/*
				 * if we use a biased hyperplane, each example with a zero
				 * diagonal
				 */
				/* entry must have an alpha at the upper bound. Doing this */
				/*
				 * is essential for the HIDEO optimizer, since it cannot handle
				 * zero
				 */
				/*
				 * diagonal entries in the hessian for the unbiased hyperplane
				 * case.
				 */
				primal[i] = up[i];
			} else {
				primal[i] = start[i]; /* leave as is */
			}
			temp[i] = primal[i];
		}

		return (result);
	}

	/******************************** svm_hideo ****************************/
	private double[] optimize_qp(QuadraticProgram qp, DoubleValue epsilon_crit,
			int nx, /* Maximum number of variables in QP */
			DoubleValue threshold, LearnParm learn_parm)
	/* start the optimizer and return the optimal values */
	/* The HIDEO optimizer does not necessarily fully solve the problem. */
	/* Since it requires a strictly positive definite hessian, the solution */
	/* is restricted to a linear independent subset in case the matrix is */
	/* only semi-definite. */
	{
		int i;
		int result;
		double eq;

		// System.out.println("nx:"+nx);
		if (primal == null) { /* allocate memory at first call */
			primal = new double[nx];
			dual = new double[(nx + 1) * 2];
			nonoptimal = new int[nx];
			buffer = new double[((nx + 1) * 2 * (nx + 1) * 2 + nx * nx + 2
					* (nx + 1) * 2 + 2 * nx + 1 + 2 * nx + nx + nx + nx * nx)];
			threshold.value = 0;
		}

		eq = qp.opt_ce0[0];
		for (i = 0; i < qp.opt_n; i++) {
			eq += qp.opt_xinit[i] * qp.opt_ce[i];
		}

		result = optimize_hildreth_despo(qp.opt_n, qp.opt_m, opt_precision,
				(epsilon_crit.value), learn_parm.epsilon_a, maxiter,
				/* (long)PRIMAL_OPTIMAL, */
				(long) 0, (long) 0, lindep_sensitivity, qp.opt_g, qp.opt_g0,
				qp.opt_ce, qp.opt_ce0, qp.opt_low, qp.opt_up, primal,
				qp.opt_xinit, dual, nonoptimal, buffer);

		if (learn_parm.totwords < learn_parm.svm_maxqpsize) {
			/* larger working sets will be linear dependent anyway */
			learn_parm.svm_maxqpsize = Math.max(learn_parm.totwords, 2);
		}

		if (result == NAN_SOLUTION) {
			lindep_sensitivity *= 2; /* throw out linear dependent examples more */
			/* generously */
			if (learn_parm.svm_maxqpsize > 2) {
				learn_parm.svm_maxqpsize--; /* decrease size of qp-subproblems */
			}
			precision_violations++;
		}

		/* take one round of only two variable to get unstuck */
		if (result != PRIMAL_OPTIMAL) {

			smallroundcount++;

			result = optimize_hildreth_despo(qp.opt_n, qp.opt_m, opt_precision,
					(epsilon_crit.value), learn_parm.epsilon_a, (long) maxiter,
					(long) PRIMAL_OPTIMAL, (long) SMALLROUND,
					lindep_sensitivity, qp.opt_g, qp.opt_g0, qp.opt_ce,
					qp.opt_ce0, qp.opt_low, qp.opt_up, primal, qp.opt_xinit,
					dual, nonoptimal, buffer);
		}

		if (result != PRIMAL_OPTIMAL) {
			if (result != ONLY_ONE_VARIABLE)
				precision_violations++;
			if (result == MAXITER_EXCEEDED)
				maxiter += 100;
			if (result == NAN_SOLUTION) {
				lindep_sensitivity *= 2; /*
										 * throw out linear dependent examples
										 * more
										 */
				/* generously */
				/* results not valid, so return inital values */
				for (i = 0; i < qp.opt_n; i++) {
					primal[i] = qp.opt_xinit[i];
				}
			}
		}

		if (precision_violations > 50) {
			precision_violations = 0;
			(epsilon_crit.value) *= 10.0;
		}

		if ((qp.opt_m > 0) && (result != NAN_SOLUTION))
			threshold.value = dual[1] - dual[0];
		else
			threshold.value = 0;

		return (primal);
	}

	private void update_linear_component(Doc[] docs, long[] label,
			int[] active2dnum, /* keep track of the linear component */
			double[] a, double[] a_old, /* lin of the gradient etc. by updating */
			int[] working2dnum, long totdoc, int totwords, /*
															 * based on the
															 * change of the
															 * variables
															 */
			KernelParm kernel_parm, /* in the current working set */
			KernelCache kernel_cache, double[] lin, double[] aicache,
			double[] weights) {
		int i, ii, j, jj;
		double tec;

		if (kernel_parm.kernel_type == 0) { /* special linear case */
			Model.clear_vector_n(weights, totwords);
			for (ii = 0; (i = working2dnum[ii]) >= 0; ii++) {
				if (a[i] != a_old[i]) {
					Model.add_vector_ns(weights, docs[i].words,
							((a[i] - a_old[i]) * (double) label[i]));
				}
			}
			for (jj = 0; (j = active2dnum[jj]) >= 0; jj++) {
				lin[j] += sprod_ns(weights, docs[j].words);
			}
		} else { /* general case */
			for (jj = 0; (i = working2dnum[jj]) >= 0; jj++) {
				if (a[i] != a_old[i]) {
					get_kernel_row(kernel_cache, docs, i, totdoc, active2dnum,
							aicache, kernel_parm);
					for (ii = 0; (j = active2dnum[ii]) >= 0; ii++) {
						tec = aicache[j];
						lin[j] += (((a[i] * tec) - (a_old[i] * tec)) * (double) label[i]);
					}
				}
			}
		}
	}

	private long check_optimality(Model model, /* Check KT-conditions */
	long[] label, int[] unlabeled, double[] a, double[] lin, int totdoc,
			LearnParm learn_parm, DoubleValue maxdiff, double epsilon_crit_org,
			LongValue misclassified, boolean[] inconsistent, int[] active2dnum,
			long[] last_suboptimal_at, long iteration, KernelParm kernel_parm) {
		int i;
		int ii;
		long retrain;
		double dist, ex_c;

		if (kernel_parm.kernel_type == LINEAR) { /* be optimistic */
			learn_parm.epsilon_shrink = -learn_parm.epsilon_crit
					+ epsilon_crit_org;
		} else { /* be conservative */
			learn_parm.epsilon_shrink = learn_parm.epsilon_shrink * 0.7
					+ (maxdiff.value) * 0.3;
		}

		retrain = 0;
		(maxdiff.value) = 0;
		(misclassified.value) = 0;
		for (ii = 0; (i = active2dnum[ii]) >= 0; ii++) {
			if ((!inconsistent[i]) && label[i] != 0) {
				dist = (lin[i] - model.b) * (double) label[i];/*
															 * 'distance' from
															 * hyperplane
															 */
				ex_c = learn_parm.svm_cost[i] - learn_parm.epsilon_a;
				if (dist <= 0) {
					(misclassified.value)++; /*
											 * does not work due to deactivation
											 * of var
											 */
				}
				if ((a[i] > learn_parm.epsilon_a) && (dist > 1)) {
					if ((dist - 1.0) > (maxdiff.value)) /* largest violation */
						(maxdiff.value) = dist - 1.0;
				} else if ((a[i] < ex_c) && (dist < 1)) {
					if ((1.0 - dist) > (maxdiff.value)) /* largest violation */
						(maxdiff.value) = 1.0 - dist;
				}
				/*
				 * Count how long a variable was at lower/upper bound (and
				 * optimal).
				 */
				/* Variables, which were at the bound and optimal for a long */
				/* time are unlikely to become support vectors. In case our */
				/* cache is filled up, those variables are excluded to save */
				/* kernel evaluations. (See chapter 'Shrinking'). */
				if ((a[i] > (learn_parm.epsilon_a)) && (a[i] < ex_c)) {
					last_suboptimal_at[i] = iteration; /* not at bound */
				} else if ((a[i] <= (learn_parm.epsilon_a))
						&& (dist < (1.0 + learn_parm.epsilon_shrink))) {
					last_suboptimal_at[i] = iteration; /* not likely optimal */
				} else if ((a[i] >= ex_c)
						&& (dist > (1.0 - learn_parm.epsilon_shrink))) {
					last_suboptimal_at[i] = iteration; /* not likely optimal */
				}
			}
		}
		/* termination criterion */
		if ((retrain == 0) && ((maxdiff.value) > learn_parm.epsilon_crit)) {
			retrain = 1;
		}
		return (retrain);
	}

	private double compute_objective_function(double[] a, double[] lin,
			long[] label, int[] active2dnum) {
		int i, ii;
		double criterion;
		/* calculate value of objective function */
		criterion = 0;
		for (ii = 0; active2dnum[ii] >= 0; ii++) {
			i = active2dnum[ii];
			criterion = criterion - a[i] + 0.5 * a[i] * label[i] * lin[i];
		}
		return (criterion);
	}

	private long calculate_svm_model(Doc[] docs, /*
												 * Compute decision function
												 * based on current values
												 */
	long[] label, int[] unlabeled, /* of alpha. */
	double[] lin, double[] a, double[] a_old, LearnParm learn_parm,
			int[] working2dnum, Model model) {
		int i, ii;
		int pos;
		boolean b_calculated = false;
		double ex_c;

		// sprintf(temstr,"Calculating model..."); printm(temstr);

		if (!learn_parm.biased_hyperplane) {
			model.b = 0;
			b_calculated = true;
		}

		for (ii = 0; (i = working2dnum[ii]) >= 0; ii++) {
			if ((a_old[i] > 0) && (a[i] == 0)) { /* remove from model */
				pos = model.index[i];
				model.index[i] = -1;
				(model.sv_num)--;
				model.supvec[pos] = model.supvec[model.sv_num];
				model.alpha[pos] = model.alpha[model.sv_num];
				model.index[(model.supvec[pos]).docnum] = pos;
			} else if ((a_old[i] == 0) && (a[i] > 0)) { /* add to model */
				model.supvec[model.sv_num] = (docs[i]);
				model.alpha[model.sv_num] = a[i] * (double) label[i];
				model.index[i] = model.sv_num;
				(model.sv_num)++;
			} else if (a_old[i] == a[i]) { /* nothing to do */
			} else { /* just update alpha */
				model.alpha[model.index[i]] = a[i] * (double) label[i];
			}

			ex_c = learn_parm.svm_cost[i] - learn_parm.epsilon_a;
			if ((a_old[i] >= ex_c) && (a[i] < ex_c)) {
				(model.at_upper_bound)--;
			} else if ((a_old[i] < ex_c) && (a[i] >= ex_c)) {
				(model.at_upper_bound)++;
			}

			if ((!b_calculated) && (a[i] > learn_parm.epsilon_a)
					&& (a[i] < ex_c)) { /* calculate b */
				model.b = (-(double) label[i] + lin[i]);
				b_calculated = true;
			}
		}

		/*
		 * If there is no alpha in the working set not at bounds, then just use
		 * the model->b from the last iteration or the one provided by the core
		 * optimizer
		 */

		return (model.sv_num - 1); /*
									 * have to substract one, since element 0 is
									 * empty
									 */
	}

	private void reactivate_inactive_examples(long[] label, int[] unlabeled, /*
																			 * Make
																			 * all
																			 * variables
																			 * active
																			 * again
																			 */
	double[] a, /* which had been removed by shrinking. */
	ShrinkState shrink_state, double[] lin, int totdoc, int totwords,
			long iteration, /* Computes lin for those */
			LearnParm learn_parm, /* variables from scratch. */
			boolean[] inconsistent, Doc[] docs, KernelParm kernel_parm,
			KernelCache kernel_cache, Model model, double[] aicache,
			double[] weights, DoubleValue maxdiff) {
		int i;
		int j;
		int ii, jj;
		int t;
		int[] changed2dnum;
		int[] inactive2dnum;
		boolean[] changed;
		boolean[] inactive;
		double kernel_val;
		double[] a_old;
		double dist;
		double ex_c;

		changed = new boolean[totdoc];
		changed2dnum = new int[totdoc + 11];
		inactive = new boolean[totdoc];
		inactive2dnum = new int[totdoc + 11];
		for (t = shrink_state.deactnum - 1; (t >= 0)
				&& shrink_state.a_history[t] != null; t--) {
			if (com_pro.show_other) {
				System.out.println(t + "..");
			}

			a_old = shrink_state.a_history[t];
			for (i = 0; i < totdoc; i++) {
				inactive[i] = ((shrink_state.active[i] == 0) && (shrink_state.inactive_since[i] == t));
				changed[i] = (a[i] != a_old[i]);
			}
			compute_index(inactive, totdoc, inactive2dnum);
			compute_index(changed, totdoc, changed2dnum);

			if (kernel_parm.kernel_type == LINEAR) { /* special linear case */
				ModelLearn.clear_vector_n(weights, totwords);
				for (ii = 0; changed2dnum[ii] >= 0; ii++) {
					i = changed2dnum[ii];
					ModelLearn.add_vector_ns(weights, docs[i].words,
							((a[i] - a_old[i]) * (double) label[i]));
				}
				for (jj = 0; (j = inactive2dnum[jj]) >= 0; jj++) {
					lin[j] += sprod_ns(weights, docs[j].words);
				}
			} else {
				for (ii = 0; (i = changed2dnum[ii]) >= 0; ii++) {
					get_kernel_row(kernel_cache, docs, i, totdoc,
							inactive2dnum, aicache, kernel_parm);
					for (jj = 0; (j = inactive2dnum[jj]) >= 0; jj++) {
						kernel_val = aicache[j];
						lin[j] += (((a[i] * kernel_val) - (a_old[i] * kernel_val)) * (double) label[i]);
					}
				}
			}
		}
		(maxdiff.value) = 0;
		for (i = 0; i < totdoc; i++) {
			shrink_state.inactive_since[i] = shrink_state.deactnum - 1;
			if (!inconsistent[i]) {
				dist = (lin[i] - model.b) * (double) label[i];
				ex_c = learn_parm.svm_cost[i] - learn_parm.epsilon_a;
				if ((a[i] > learn_parm.epsilon_a) && (dist > 1)) {
					if ((dist - 1.0) > (maxdiff.value)) /* largest violation */
						(maxdiff.value) = dist - 1.0;
				} else if ((a[i] < ex_c) && (dist < 1)) {
					if ((1.0 - dist) > (maxdiff.value)) /* largest violation */
						(maxdiff.value) = 1.0 - dist;
				}
				if ((a[i] > (0 + learn_parm.epsilon_a)) && (a[i] < ex_c)) {
					shrink_state.active[i] = 1; /* not at bound */
				} else if ((a[i] <= (0 + learn_parm.epsilon_a))
						&& (dist < (1 + learn_parm.epsilon_shrink))) {
					shrink_state.active[i] = 1;
				} else if ((a[i] >= ex_c)
						&& (dist > (1 - learn_parm.epsilon_shrink))) {
					shrink_state.active[i] = 1;
				}
			}
		}
		for (i = 0; i < totdoc; i++) {
			(shrink_state.a_history[shrink_state.deactnum - 1])[i] = a[i];
		}

		for (t = shrink_state.deactnum - 2; (t >= 0)
				&& shrink_state.a_history[t] != null; t--) {
			// free(shrink_state.a_history[t]);
			shrink_state.a_history[t] = null;
		}
	}

	private void write_prediction(String predfile, Model model, double[] lin,
			double[] a, int[] unlabeled, long[] label, long totdoc,
			LearnParm learn_parm) {
		try {
			BufferedWriter predfl = new BufferedWriter(new FileWriter(predfile));
			int i;
			double dist, a_max;

			System.out.println("Writing prediction file...");

			// if ((predfl = fopen (predfile, "w")) == NULL)
			// { printe (predfile); }
			a_max = learn_parm.epsilon_a;
			for (i = 0; i < totdoc; i++) {
				if ((unlabeled[i] != 0) && (a[i] > a_max)) {
					a_max = a[i];
				}
			}
			for (i = 0; i < totdoc; i++) {
				if (unlabeled[i] != 0) {
					if ((a[i] > (learn_parm.epsilon_a))) {
						dist = (double) label[i]
								* (1.0 - learn_parm.epsilon_crit - a[i]
										/ (a_max * 2.0));
					} else {
						dist = (lin[i] - model.b);
					}
					if (dist > 0) {
						predfl.write(dist + ":+1 n");
					} else {
						predfl.write(-dist + "%.8g:-1 \n");
					}
				}
			}
			predfl.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private long incorporate_unlabeled_examples(Model model, long[] label,
			boolean[] inconsistent, int[] unlabeled, double[] a, double[] lin,
			int totdoc, double[] selcrit, int[] select, int[] key,
			long transductcycle, KernelParm kernel_parm, LearnParm learn_parm) {
		int i;
		int j;
		int k, j1, j2, j3;
		int j4;
		long unsupaddnum1 = 0, unsupaddnum2 = 0;
		long pos, neg, upos, uneg, orgpos, orgneg, nolabel, newpos, newneg, allunlab;
		double dist, model_length, posratio, negratio;
		long check_every = 2;
		double loss;
		double switchsens = 0.0;
		// double switchsensorg=0.0;
		double umin, umax, sumalpha;
		int imin = 0, imax = 0;
		long switchnum = 0;

		switchsens /= 1.2;

		/* assumes that lin[] is up to date -> no inactive vars */

		orgpos = 0;
		orgneg = 0;
		newpos = 0;
		newneg = 0;
		nolabel = 0;
		allunlab = 0;
		for (i = 0; i < totdoc; i++) {
			if (unlabeled[i] == 0) {
				if (label[i] > 0) {
					orgpos++;
				} else {
					orgneg++;
				}
			} else {
				allunlab++;
				if (unlabeled[i] != 0) {
					if (label[i] > 0) {
						newpos++;
					} else if (label[i] < 0) {
						newneg++;
					}
				}
			}
			if (label[i] == 0) {
				nolabel++;
			}
		}

		if (learn_parm.transduction_posratio >= 0) {
			posratio = learn_parm.transduction_posratio;
		} else {
			posratio = (double) orgpos / (double) (orgpos + orgneg); /*
																	 * use ratio
																	 * of
																	 * pos/neg
																	 */
		} /* in training data */
		negratio = 1.0 - posratio;

		learn_parm.svm_costratio = 1.0; /* global */
		if (posratio > 0) {
			learn_parm.svm_costratio_unlab = negratio / posratio;
		} else {
			learn_parm.svm_costratio_unlab = 1.0;
		}

		pos = 0;
		neg = 0;
		upos = 0;
		uneg = 0;
		for (i = 0; i < totdoc; i++) {
			dist = (lin[i] - model.b); /* 'distance' from hyperplane */
			if (dist > 0) {
				pos++;
			} else {
				neg++;
			}
			if (unlabeled[i] != 0) {
				if (dist > 0) {
					upos++;
				} else {
					uneg++;
				}
			}
			if ((unlabeled[i] == 0)
					&& (a[i] > (learn_parm.svm_cost[i] - learn_parm.epsilon_a))) {
				/*
				 * printf("Ubounded %ld (class %ld, unlabeled %ld)\n",i,label[i],
				 * unlabeled[i]);
				 */
			}
		}

		System.out.println("POS=" + pos + ", ORGPOS=" + orgpos + ", ORGNEG="
				+ orgneg);
		System.out.println("POS=" + pos + ", NEWPOS=" + newpos + ", NEWNEG="
				+ newneg);
		System.out.println("pos ratio = " + (double) (upos)
				/ (double) (allunlab) + " (" + posratio + ").\n");

		if (transductcycle == 0) {
			j1 = 0;
			j2 = 0;
			j4 = 0;
			for (i = 0; i < totdoc; i++) {
				dist = (lin[i] - model.b); /* 'distance' from hyperplane */
				if ((label[i] == 0) && (unlabeled[i] != 0)) {
					selcrit[j4] = dist;
					key[j4] = i;
					j4++;
				}
			}
			unsupaddnum1 = 0;
			unsupaddnum2 = 0;
			select_top_n(selcrit, j4, select, (int) (allunlab * posratio + 0.5));
			for (k = 0; (k < (long) (allunlab * posratio + 0.5)); k++) {
				i = key[select[k]];
				label[i] = 1;
				unsupaddnum1++;
				j1++;
			}
			for (i = 0; i < totdoc; i++) {
				if ((label[i] == 0) && (unlabeled[i] != 0)) {
					label[i] = -1;
					j2++;
					unsupaddnum2++;
				}
			}
			for (i = 0; i < totdoc; i++) { /* set upper bounds on vars */
				if (unlabeled[i] != 0) {
					if (label[i] == 1) {
						learn_parm.svm_cost[i] = learn_parm.svm_c
								* learn_parm.svm_costratio_unlab
								* learn_parm.svm_unlabbound;
					} else if (label[i] == -1) {
						learn_parm.svm_cost[i] = learn_parm.svm_c
								* learn_parm.svm_unlabbound;
					}
				}
			}

			System.out.println("costratio " + learn_parm.svm_costratio
					+ ", costratio_unlab " + learn_parm.svm_costratio_unlab
					+ ", unlabbound " + learn_parm.svm_unlabbound);
			System.out.println("Classifying unlabeled data as " + unsupaddnum1
					+ " POS / " + unsupaddnum2 + " NEG.");

			System.out.println("Retraining.");

			return ((long) 3);
		}
		if ((transductcycle % check_every) == 0) {
			System.out.println("Retraining.");
			j1 = 0;
			j2 = 0;
			unsupaddnum1 = 0;
			unsupaddnum2 = 0;
			for (i = 0; i < totdoc; i++) {
				if ((unlabeled[i] == 2)) {
					unlabeled[i] = 1;
					label[i] = 1;
					j1++;
					unsupaddnum1++;
				} else if ((unlabeled[i] == 3)) {
					unlabeled[i] = 1;
					label[i] = -1;
					j2++;
					unsupaddnum2++;
				}
			}
			for (i = 0; i < totdoc; i++) { /* set upper bounds on vars */
				if (unlabeled[i] != 0) {
					if (label[i] == 1) {
						learn_parm.svm_cost[i] = learn_parm.svm_c
								* learn_parm.svm_costratio_unlab
								* learn_parm.svm_unlabbound;
					} else if (label[i] == -1) {
						learn_parm.svm_cost[i] = learn_parm.svm_c
								* learn_parm.svm_unlabbound;
					}
				}
			}

			System.out.println("costratio " + learn_parm.svm_costratio
					+ ", costratio_unlab " + learn_parm.svm_costratio_unlab
					+ ", unlabbound " + learn_parm.svm_unlabbound);
			System.out.println(upos + " positive -> Added " + unsupaddnum1
					+ " POS / " + unsupaddnum2 + " NEG unlabeled examples.");

			if (learn_parm.svm_unlabbound == 1) {
				learn_parm.epsilon_crit = 0.001; /* do the last run right */
			} else {
				learn_parm.epsilon_crit = 0.01; /*
												 * otherwise, no need to be so
												 * picky
												 */
			}

			return ((long) 3);
		} else if (((transductcycle % check_every) < check_every)) {
			model_length = 0;
			sumalpha = 0;
			loss = 0;
			for (i = 0; i < totdoc; i++) {
				model_length += a[i] * label[i] * lin[i];
				sumalpha += a[i];
				dist = (lin[i] - model.b); /* 'distance' from hyperplane */
				if ((label[i] * dist) < (1.0 - learn_parm.epsilon_crit)) {
					loss += (1.0 - (label[i] * dist)) * learn_parm.svm_cost[i];
				}
			}
			model_length = Math.sqrt(model_length);

			System.out.println("Model-length = " + model_length + " ("
					+ sumalpha + "), loss = " + loss + ", objective = " + loss
					+ 0.5 * model_length * model_length);
			j1 = 0;
			j2 = 0;
			j3 = 0;
			j4 = 0;
			unsupaddnum1 = 0;
			unsupaddnum2 = 0;
			umin = 99999;
			umax = -99999;
			j4 = 1;
			while (j4 != 0) {
				umin = 99999;
				umax = -99999;
				for (i = 0; (i < totdoc); i++) {
					dist = (lin[i] - model.b);
					if ((label[i] > 0) && (unlabeled[i] != 0)
							&& (!inconsistent[i]) && (dist < umin)) {
						umin = dist;
						imin = i;
					}
					if ((label[i] < 0) && (unlabeled[i] != 0)
							&& (!inconsistent[i]) && (dist > umax)) {
						umax = dist;
						imax = i;
					}
				}
				if ((umin < (umax + switchsens - 1E-4))) {
					j1++;
					j2++;
					unsupaddnum1++;
					unlabeled[imin] = 3;
					inconsistent[imin] = true;
					unsupaddnum2++;
					unlabeled[imax] = 2;
					inconsistent[imax] = true;
				} else
					j4 = 0;
				j4 = 0;
			}
			for (j = 0; (j < totdoc); j++) {
				if (unlabeled[j] != 0 && (!inconsistent[j])) {
					if (label[j] > 0) {
						unlabeled[j] = 2;
					} else if (label[j] < 0) {
						unlabeled[j] = 3;
					}
					/* inconsistent[j]=1; */
					j3++;
				}
			}
			switchnum += unsupaddnum1 + unsupaddnum2;

			/*
			 * stop and print out current margin
			 * sprintf(temstr,"switchnum %ld %ld\n"
			 * ,switchnum,kernel_parm->poly_degree); if(switchnum ==
			 * 2kernel_parm->poly_degree) { learn_parm->svm_unlabbound=1; }
			 */

			if ((unsupaddnum1 == 0) && (unsupaddnum2 == 0)) {
				if ((learn_parm.svm_unlabbound >= 1)
						&& ((newpos + newneg) == allunlab)) {
					for (j = 0; (j < totdoc); j++) {
						inconsistent[j] = false;
						if (unlabeled[j] != 0)
							unlabeled[j] = 1;
					}
					write_prediction(learn_parm.predfile, model, lin, a,
							unlabeled, label, totdoc, learn_parm);

					System.out.println("Number of switches: " + switchnum);
					return ((long) 0);
					// switchsens=switchsensorg;
					// learn_parm.svm_unlabbound*=1.5;
					// if(learn_parm.svm_unlabbound>1)
					// {
					// learn_parm.svm_unlabbound=1;
					// }
					// model.at_upper_bound=0; /* since upper bound increased */

					// System.out.println(
					// "Increasing influence of unlabeled examples to "
					// +learn_parm.svm_unlabbound*100.0+
					// "%% .");

					// learn_parm.epsilon_crit=0.5; /* don't need to be so picky
					// */

					// for(i=0;i<totdoc;i++)
					// { /* set upper bounds on vars */
					// if(unlabeled[i]!=0)
					// {
					// if(label[i] == 1)
					// {
					// learn_parm.svm_cost[i]=learn_parm.svm_c*
					// learn_parm.svm_costratio_unlab*learn_parm.svm_unlabbound;
					// }
					// else if(label[i] == -1)
					// {
					// learn_parm.svm_cost[i]=learn_parm.svm_c*
					// learn_parm.svm_unlabbound;
					// }
					// }
					// return((long)2);
					// }
				}
			}
		}
		return ((long) 0);
	}

	private void kernel_cache_shrink(KernelCache kernel_cache, int totdoc,
			long numshrink, int[] after) /* remove numshrink columns in the cache */
	{
		/* which correspond to examples marked */
		long i;
		int j, jj, from = 0, to = 0;
		long scount; /* 0 in after. */
		long[] keep;

		System.out.println(" Reorganizing cache...");

		keep = new long[totdoc];
		for (j = 0; j < totdoc; j++) {
			keep[j] = 1;
		}
		scount = 0;
		for (jj = 0; (jj < kernel_cache.activenum) && (scount < numshrink); jj++) {
			j = kernel_cache.active2totdoc[jj];
			if (after[j] == 0) {
				scount++;
				keep[j] = 0;
			}
		}

		for (i = 0; i < kernel_cache.max_elems; i++) {
			for (jj = 0; jj < kernel_cache.activenum; jj++) {
				j = kernel_cache.active2totdoc[jj];
				if (keep[j] == 0) {
					from++;
				} else {
					kernel_cache.buffer.set(to, kernel_cache.buffer.get(from));
					to++;
					from++;
				}
			}
		}

		kernel_cache.activenum = 0;
		for (j = 0; j < totdoc; j++) {
			if ((keep[j] != 0) && (kernel_cache.totdoc2active[j] != -1)) {
				kernel_cache.active2totdoc[kernel_cache.activenum] = j;
				kernel_cache.totdoc2active[j] = kernel_cache.activenum;
				kernel_cache.activenum++;
			} else {
				kernel_cache.totdoc2active[j] = -1;
			}
		}

		kernel_cache.max_elems = (long) (kernel_cache.buffsize / kernel_cache.activenum);
		if (kernel_cache.max_elems > totdoc) {
			kernel_cache.max_elems = totdoc;
		}

		if (com_pro.show_action) {
			System.out.println("done.");
		}
		if (com_pro.show_compute_2) {
			System.out.println(" Cache-size in rows = "
					+ kernel_cache.max_elems);
		}
	}

	private long shrink_problem(
	/* shrink some variables away */
	/* do the shrinking only if at least minshrink variables can be removed */
	LearnParm learn_parm, ShrinkState shrink_state, int[] active2dnum,
			long iteration, long[] last_suboptimal_at, int totdoc,
			long minshrink, double[] a, boolean[] inconsistent) {
		int i, ii;
		long change;
		long activenum;
		double[] a_old;

		activenum = 0;
		change = 0;
		for (ii = 0; active2dnum[ii] >= 0; ii++) {
			i = active2dnum[ii];
			activenum++;
			if (((iteration - last_suboptimal_at[i]) > learn_parm.svm_iter_to_shrink)
					|| (inconsistent[i])) {
				change++;
			}
		}
		if (change >= minshrink) { /*
									 * shrink only if sufficiently many
									 * candidates
									 */
			/* Shrink problem by removing those variables which are */
			/* optimal at a bound for a minimum number of iterations */
			if (com_pro.show_other) {
				System.out.println(" Shrinking...");
			}
			a_old = new double[totdoc];
			shrink_state.a_history[shrink_state.deactnum] = a_old;
			for (i = 0; i < totdoc; i++) {
				a_old[i] = a[i];
			}
			change = 0;
			for (ii = 0; active2dnum[ii] >= 0; ii++) {
				i = active2dnum[ii];
				if ((((iteration - last_suboptimal_at[i]) > learn_parm.svm_iter_to_shrink) || (inconsistent[i]))) {
					shrink_state.active[i] = 0;
					shrink_state.inactive_since[i] = shrink_state.deactnum;
					change++;
				}
			}
			activenum = compute_index(shrink_state.active, totdoc, active2dnum);
			shrink_state.deactnum++;
			if (com_pro.show_other) {
				System.out.println(" Number of inactive variables = "
						+ (totdoc - activenum));
			}
		}
		return (activenum);
	}

	private long identify_inconsistent(double[] a, long[] label,
			int[] unlabeled, int totdoc, LearnParm learn_parm,
			LongValue inconsistentnum, boolean[] inconsistent) {
		int i;
		long retrain;

		/* Throw out examples with multipliers at upper bound. This */
		/* corresponds to the -i 1 option. */
		/* ATTENTION: this is just a heuristic for finding a close */
		/* to minimum number of examples to exclude to */
		/* make the problem separable with desired margin */
		retrain = 0;
		for (i = 0; i < totdoc; i++) {
			if ((!inconsistent[i])
					&& (unlabeled[i] == 0)
					&& (a[i] >= (learn_parm.svm_cost[i] - learn_parm.epsilon_a))) {
				inconsistentnum.value++;
				inconsistent[i] = true; /* never choose again */
				retrain = 2; /* start over */

				System.out.println("inconsistent(" + i + ")..");
			}
		}
		return (retrain);
	}

	private long identify_misclassified(double[] lin, long[] label,
			int[] unlabeled, int totdoc, Model model,
			LongValue inconsistentnum, boolean[] inconsistent) {
		int i;
		long retrain;
		double dist;

		/* Throw out misclassified examples. This */
		/* corresponds to the -i 2 option. */
		/* ATTENTION: this is just a heuristic for finding a close */
		/* to minimum number of examples to exclude to */
		/* make the problem separable with desired margin */
		retrain = 0;
		for (i = 0; i < totdoc; i++) {
			dist = (lin[i] - model.b) * (double) label[i]; /*
															 * 'distance' from
															 * hyperplane
															 */
			if ((!inconsistent[i]) && (unlabeled[i] == 0) && (dist <= 0)) {
				(inconsistentnum.value)++;
				inconsistent[i] = true; /* never choose again */
				retrain = 2; /* start over */

				System.out.println("inconsistent(" + i + ")..");
			}
		}
		return (retrain);
	}

	private long identify_one_misclassified(double[] lin, long[] label,
			int[] unlabeled, int totdoc, Model model,
			LongValue inconsistentnum, boolean[] inconsistent) {
		int i;
		long retrain;
		int maxex = -1;
		double dist, maxdist = 0;

		/* Throw out the 'most misclassified' example. This */
		/* corresponds to the -i 3 option. */
		/* ATTENTION: this is just a heuristic for finding a close */
		/* to minimum number of examples to exclude to */
		/* make the problem separable with desired margin */
		retrain = 0;
		for (i = 0; i < totdoc; i++) {
			if ((!inconsistent[i]) && (unlabeled[i] == 0)) {
				dist = (lin[i] - model.b) * (double) label[i];/*
															 * 'distance' from
															 * hyperplane
															 */
				if (dist < maxdist) {
					maxdist = dist;
					maxex = i;
				}
			}
		}
		if (maxex >= 0) {
			(inconsistentnum.value)++;
			inconsistent[maxex] = true; /* never choose again */
			retrain = 2; /* start over */

			System.out.println("inconsistent(" + i + ")..");
		}
		return (retrain);
	}

	private long optimize_to_convergence(Doc[] docs, long[] label, int totdoc,
			int totwords, LearnParm learn_parm, KernelParm kernel_parm,
			KernelCache kernel_cache, ShrinkState shrink_state, Model model,
			boolean[] inconsistent, int[] unlabeled, double[] a, double[] lin,
			TimingProfile timing_profile, DoubleValue maxdiff, long heldout,
			long retrain) {
		int chosen[];
		int key[];
		int i;
		int j;
		int jj;
		long last_suboptimal_at[];
		boolean noshrink;
		LongValue inconsistentnum = new LongValue();
		int choosenum;
		long already_chosen = 0, iteration;
		LongValue misclassified = new LongValue();
		long supvecnum = 0;
		int active2dnum[];
		long inactivenum;
		int[] working2dnum;
		int[] selexam;
		long activenum;
		// double criterion;
		double eq;
		double[] a_old;
		long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0; /* timing */
		long transductcycle;
		boolean transduction;
		DoubleValue epsilon_crit_org = new DoubleValue();

		double[] selcrit; /* buffer for sorting */
		double[] aicache; /* buffer to keep one row of hessian */
		double[] weights; /* buffer for weight vector in linear case */
		QuadraticProgram qp = new QuadraticProgram(); /*
													 * buffer for one quadratic
													 * program
													 */

		// System.out.println("optimize_to_convergence.totdoc:"+totdoc);
		epsilon_crit_org.value = learn_parm.epsilon_crit; /* save org */
		if (kernel_parm.kernel_type == LINEAR) {
			learn_parm.epsilon_crit = 2.0;
			kernel_cache = null; /* caching makes no sense for linear kernel */
		}
		learn_parm.epsilon_shrink = 2;
		maxdiff.value = 1;

		learn_parm.totwords = totwords;

		chosen = new int[totdoc];
		last_suboptimal_at = new long[totdoc];
		key = new int[totdoc + 11];
		selcrit = new double[totdoc];
		selexam = new int[totdoc];
		a_old = new double[totdoc];
		aicache = new double[totdoc];
		working2dnum = new int[totdoc + 11];
		active2dnum = new int[totdoc + 11];

		// System.out.println("maxqpsize:"+learn_parm.svm_maxqpsize);
		qp.opt_ce = new double[learn_parm.svm_maxqpsize];
		qp.opt_ce0 = new double[1];
		qp.opt_g = new double[learn_parm.svm_maxqpsize
				* learn_parm.svm_maxqpsize];
		qp.opt_g0 = new double[learn_parm.svm_maxqpsize];
		qp.opt_xinit = new double[learn_parm.svm_maxqpsize];
		qp.opt_low = new double[learn_parm.svm_maxqpsize];
		qp.opt_up = new double[learn_parm.svm_maxqpsize];

		weights = new double[totwords + 1];

		choosenum = 0;
		inconsistentnum.value = 0;
		transductcycle = 0;
		transduction = false;
		if (retrain == 0)
			retrain = 1;
		iteration = 1;

		if (kernel_cache != null) {
			kernel_cache.time = iteration; /* for lru cache */
			kernel_cache_reset_lru(kernel_cache);
		}

		for (i = 0; i < totdoc; i++) {
			/* various inits */
			chosen[i] = 0;
			a_old[i] = a[i];
			last_suboptimal_at[i] = 1;
			if (inconsistent[i])
				inconsistentnum.value++;
			if (unlabeled[i] != 0) {
				transduction = true;
			}
		}

		activenum = compute_index(shrink_state.active, totdoc, active2dnum);
		inactivenum = totdoc - activenum;
		clear_index(working2dnum);

		/* repeat this loop until we have convergence */
		for (; retrain != 0 && (iteration < 1000000); iteration++) {
			if (kernel_cache != null)
				kernel_cache.time = iteration; /* for lru cache */
			i = 0;
			for (jj = 0; (j = working2dnum[jj]) >= 0; jj++) {
				/* clear working set */
				if ((chosen[j] >= (learn_parm.svm_maxqpsize / Math.min(
						learn_parm.svm_maxqpsize, learn_parm.svm_newvarsinqp)))
						|| (inconsistent[j]) || (j == heldout)) {
					chosen[j] = 0;
					choosenum--;
				} else {
					chosen[j]++;
					working2dnum[i++] = j;
				}
			}
			working2dnum[i] = -1;

			if (retrain == 2) {
				choosenum = 0;
				for (jj = 0; (j = working2dnum[jj]) >= 0; jj++) {
					/* fully clear working set */
					chosen[j] = 0;
				}
				clear_index(working2dnum);
				for (i = 0; i < totdoc; i++) {
					/* set inconsistent examples to zero (-i 1) */
					if ((inconsistent[i] || (heldout == i)) && (a[i] != 0.0)) {
						chosen[i] = 99999;
						choosenum++;
						a[i] = 0;
					}
				}
				if (learn_parm.biased_hyperplane) {
					eq = 0;
					for (i = 0; i < totdoc; i++) { /*
													 * make sure we fulfill
													 * equality constraint
													 */
						eq += a[i] * label[i];
					}
					for (i = 0; (i < totdoc)
							&& (Math.abs(eq) > learn_parm.epsilon_a); i++) {
						if ((eq * label[i] > 0) && (a[i] > 0)) {
							chosen[i] = 88888;
							choosenum++;
							if ((eq * label[i]) > a[i]) {
								eq -= (a[i] * label[i]);
								a[i] = 0;
							} else {
								a[i] -= (eq * label[i]);
								eq = 0;
							}
						}
					}
				}
				compute_index(chosen, totdoc, working2dnum);
			} else {
				/* select working set according to steepest gradient */
				if ((Math.min(learn_parm.svm_newvarsinqp,
						learn_parm.svm_maxqpsize) >= 4)
						&& (kernel_parm.kernel_type != LINEAR)) {
					/* select part of the working set from cache */
					already_chosen = select_next_qp_subproblem_grad_cache(
							label,
							unlabeled,
							a,
							lin,
							totdoc,
							Math
									.min(
											(long) (learn_parm.svm_maxqpsize - choosenum),
											(long) (learn_parm.svm_newvarsinqp / 2)),
							learn_parm, inconsistent, active2dnum,
							working2dnum, selcrit, selexam, kernel_cache, key,
							chosen);
					choosenum += already_chosen;
				}
				choosenum += select_next_qp_subproblem_grad(
						label,
						unlabeled,
						a,
						lin,
						totdoc,
						Math
								.min(
										(long) (learn_parm.svm_maxqpsize - choosenum),
										(long) (learn_parm.svm_newvarsinqp - already_chosen)),
						learn_parm, inconsistent, active2dnum, working2dnum,
						selcrit, selexam, kernel_cache, key, chosen);
			}

			// System.out.println("choosenum:"+choosenum);

			// sprintf(temstr," %ld vectors chosen\n",choosenum);
			// printm(temstr);
			t1 = System.currentTimeMillis();

			if (kernel_cache != null)
				cache_multiple_kernel_rows(kernel_cache, docs, working2dnum,
						choosenum, kernel_parm);

			t2 = System.currentTimeMillis();

			// System.out.println("retrain:"+ retrain);
			if (retrain != 2) {
				optimize_svm(docs, label, unlabeled, chosen, active2dnum,
						model, totdoc, working2dnum, choosenum, a, lin,
						learn_parm, aicache, kernel_parm, qp, epsilon_crit_org);
			}

			t3 = System.currentTimeMillis();
			update_linear_component(docs, label, active2dnum, a, a_old,
					working2dnum, totdoc, totwords, kernel_parm, kernel_cache,
					lin, aicache, weights);

			t4 = System.currentTimeMillis();
			supvecnum = calculate_svm_model(docs, label, unlabeled, lin, a,
					a_old, learn_parm, working2dnum, model);

			t5 = System.currentTimeMillis();

			/* The following computation of the objective function works only */
			/* relative to the active variables */

			// criterion=compute_objective_function(a,lin,label,active2dnum);
			for (jj = 0; (i = working2dnum[jj]) >= 0; jj++) {
				a_old[i] = a[i];
			}

			if (retrain == 2) {
				/* reset inconsistent unlabeled examples */
				for (i = 0; (i < totdoc); i++) {
					if (inconsistent[i] && unlabeled[i] != 0) {
						inconsistent[i] = false;
						label[i] = 0;
					}
				}
			}

			// LongValue misclassified_value = new LongValue();
			// misclassified_value.value = misclassified;

			retrain = check_optimality(model, label, unlabeled, a, lin, totdoc,
					learn_parm, maxdiff, epsilon_crit_org.value, misclassified,
					inconsistent, active2dnum, last_suboptimal_at, iteration,
					kernel_parm);

			// misclassified = misclassified_value.value;

			t6 = System.currentTimeMillis();
			timing_profile.time_select += t1 - t0;
			timing_profile.time_kernel += t2 - t1;
			timing_profile.time_opti += t3 - t2;
			timing_profile.time_update += t4 - t3;
			timing_profile.time_model += t5 - t4;
			timing_profile.time_check += t6 - t5;

			noshrink = false;
			if ((retrain == 0)
					&& (inactivenum > 0)
					&& ((!learn_parm.skip_final_opt_check) || (kernel_parm.kernel_type == LINEAR))) {
				if (com_pro.show_other) {
					System.out
							.println(" Checking optimality of inactive variables...");
				}

				t1 = System.currentTimeMillis();
				reactivate_inactive_examples(label, unlabeled, a, shrink_state,
						lin, totdoc, totwords, iteration, learn_parm,
						inconsistent, docs, kernel_parm, kernel_cache, model,
						aicache, weights, maxdiff);
				/* Update to new active variables. */
				activenum = compute_index(shrink_state.active, totdoc,
						active2dnum);
				inactivenum = totdoc - activenum;
				/* termination criterion */
				noshrink = true;
				retrain = 0;
				if ((maxdiff.value) > learn_parm.epsilon_crit)
					retrain = 1;
				timing_profile.time_shrink += System.currentTimeMillis() - t1;
			}

			if ((retrain == 0) && (learn_parm.epsilon_crit > (maxdiff.value)))
				learn_parm.epsilon_crit = (maxdiff.value);
			if ((retrain == 0)
					&& (learn_parm.epsilon_crit > epsilon_crit_org.value)) {
				learn_parm.epsilon_crit /= 2.0;
				retrain = 1;
				noshrink = true;
			}
			if (learn_parm.epsilon_crit < epsilon_crit_org.value)
				learn_parm.epsilon_crit = epsilon_crit_org.value;
			{
				// / sprintf(temstr,
				// " => (%ld SV (incl. %ld SV at u-bound), max violation=%.5f)\n"
				// ,
				// supvecnum,model->at_upper_bound,(*maxdiff));
				// printm(temstr);
			}

			if ((retrain == 0) && (transduction)) {
				for (i = 0; (i < totdoc); i++) {
					shrink_state.active[i] = 1;
				}
				activenum = compute_index(shrink_state.active, totdoc,
						active2dnum);
				inactivenum = 0;

				retrain = incorporate_unlabeled_examples(model, label,
						inconsistent, unlabeled, a, lin, totdoc, selcrit,
						selexam, key, transductcycle, kernel_parm, learn_parm);
				epsilon_crit_org.value = learn_parm.epsilon_crit;
				if (kernel_parm.kernel_type == LINEAR)
					learn_parm.epsilon_crit = 1;
				transductcycle++;
			} else if (((iteration % 10) == 0) && (!noshrink)) {
				activenum = shrink_problem(learn_parm, shrink_state,
						active2dnum, iteration, last_suboptimal_at, totdoc,
						Math.max((activenum / 10), 100), a, inconsistent);
				inactivenum = totdoc - activenum;
				if ((kernel_cache != null)
						&& (supvecnum > kernel_cache.max_elems)
						&& ((kernel_cache.activenum - activenum) > Math.max(
								(activenum / 10), 500))) {
					kernel_cache_shrink(kernel_cache, totdoc, Math.max(
							(activenum / 10), 500), shrink_state.active);
				}
			}

			if ((retrain == 0) && learn_parm.remove_inconsistent != 0) {
				System.out
						.println(" Moving training errors to inconsistent examples...");

				if (learn_parm.remove_inconsistent == 1) {
					retrain = identify_inconsistent(a, label, unlabeled,
							totdoc, learn_parm, inconsistentnum, inconsistent);
				} else if (learn_parm.remove_inconsistent == 2) {
					retrain = identify_misclassified(lin, label, unlabeled,
							totdoc, model, inconsistentnum, inconsistent);
				} else if (learn_parm.remove_inconsistent == 3) {
					retrain = identify_one_misclassified(lin, label, unlabeled,
							totdoc, model, inconsistentnum, inconsistent);
				}
				if (retrain != 0) {
					if (kernel_parm.kernel_type == LINEAR) { /* reinit shrinking */
						learn_parm.epsilon_crit = 2.0;
					}
				}
				if (retrain != 0) {
					System.out.println(" Now " + inconsistentnum
							+ " inconsistent examples.\n");
				}
			}
			// System.out.println("iteration:"+iteration+" retrain:"+retrain);
		} /* end of loop */
		/* repeat this loop until we have convergence */
		// TODO:
		learn_parm.epsilon_crit = epsilon_crit_org.value; /* restore org */

		return (iteration);
	}

	/******************************** Shrinking *********************************/
	private void init_shrink_state(ShrinkState shrink_state, int totdoc,
			long maxhistory) {
		int i;

		shrink_state.deactnum = 0;
		shrink_state.active = new int[totdoc];
		shrink_state.inactive_since = new int[totdoc];
		shrink_state.a_history = new double[10000][];

		for (i = 0; i < totdoc; i++) {
			shrink_state.active[i] = 1;
			shrink_state.inactive_since[i] = 0;
		}
	}

	private double custom_kernel(KernelParm kernel_parm, Doc a, Doc b) {
		double sum = 0;
		// word[] ai;
		// word[] bj;
		// ai=a.words;
		// bj=b.words;

		/*
		 * while (ai.wnum || bj.wnum) { if(ai->wnum == bj->wnum) {
		 * sum+=(fabs(ai->weight-bj->weight))(fabs(ai->weight-bj->weight));
		 * ai++; bj++; } else if ((ai!=0) &&(ai->wnum<bj->wnum || bj->wnum==0))
		 * { sum+=fabs(ai->weight)fabs(ai->weight); ai++; } else if ((bj!=0)
		 * &&(bj->wnum<ai->wnum|| ai->wnum==0)) {
		 * sum+=fabs(bj->weight)fabs(bj->weight); bj++; } }
		 */

		// case 1: /* polynomial *///
		// return((CFLOAT)pow(kernel_parm->coef_lin*sprod_ss(a->words,b->words)+
		// kernel_parm->coef_const,(double)kernel_parm->poly_degree));
		// case 2: /* radial basis function */
		//return((CFLOAT)exp(-kernel_parm->rbf_gamma*(a->twonorm_sq-2*sprod_ss(a
		// ->words,b->words)+b->twonorm_sq)));
		// case 3: /* sigmoid neural net */
		//return((CFLOAT)tanh(kernel_parm->coef_lin*sprod_ss(a->words,b->words)+
		// kernel_parm->coef_const));
		// case 4: /* custom-kernel supplied in file kernel.h*/
		// return((CFLOAT)custom_kernel(kernel_parm,a,b));
		/*
		 * SVM_WORDai,bj; ai=a->words; bj=b->words; double suma=0.0,sumb=0.0;
		 * 
		 * while (ai->wnum ) { suma+=ai->weight; ai++; } while (bj->wnum ) {
		 * sumb+=bj->weight; bj++; }
		 */
		// double
		// K_rbf=exp(-0.001*(a->twonorm_sq-2*sprod_ss(a->words,b->words)+b
		// ->twonorm_sq));
		double K_Laplace = Math.exp(-0.0001 * sum);
		double K_poly = Math.pow(Model.sprod_ss(a.words, b.words) + 20, 3);
		// double K_linear=sprod_ss(a->words,b->words);
		// double sum;
		// double sum=suma*sumb;
		// sum=K_rbf*K_poly;
		// double sum=fabs(pro*pro+pro-tan(pro));
		return K_Laplace * K_poly;
	}

	private double estimate_r_delta(Doc[] docs, long totdoc,
			KernelParm kernel_parm) {
		int i;
		double maxxlen, xlen;
		Doc nulldoc = new Doc(); /* assumes that the center of the ball is at the */
		Word nullword = new Word(); /* origin of the space. */

		nullword.wnum = 0;
		nulldoc.words = null;
		nulldoc.twonorm_sq = 0;
		nulldoc.docnum = -1;

		maxxlen = 0;
		for (i = 0; i < totdoc; i++) {
			xlen = Math.sqrt(kernel(kernel_parm, (docs[i]), (docs[i])) - 2
					* kernel(kernel_parm, (docs[i]), nulldoc)
					+ kernel(kernel_parm, nulldoc, nulldoc));
			if (xlen > maxxlen) {
				maxxlen = xlen;
			}
		}

		return (maxxlen);
	}

	private double estimate_r_delta_average(Doc[] docs, long totdoc,
			KernelParm kernel_parm) {
		int i;
		double avgxlen;
		Doc nulldoc = new Doc(); /* assumes that the center of the ball is at the */
		Word nullword = new Word(); /* origin of the space. */

		nullword.wnum = 0;
		nulldoc.words = null;
		nulldoc.twonorm_sq = 0;
		nulldoc.docnum = -1;

		avgxlen = 0;
		for (i = 0; i < totdoc; i++) {
			avgxlen += Math.sqrt(kernel(kernel_parm, (docs[i]), (docs[i])) - 2
					* kernel(kernel_parm, (docs[i]), nulldoc)
					+ kernel(kernel_parm, nulldoc, nulldoc));
		}

		return (avgxlen / totdoc);
	}

	private double estimate_sphere(Model model, /*
												 * Approximates the radius of
												 * the ball containing
												 */
	KernelParm kernel_parm) /* the support vectors by bounding it with the */
	{
		/* length of the longest support vector. This is */
		int j; /* pretty good for text categorization, since all */
		double xlen, maxxlen = 0; /*
								 * documents have feature vectors of length 1.
								 * It
								 */
		Doc nulldoc = new Doc(); /* assumes that the center of the ball is at the */

		nulldoc.twonorm_sq = 0;
		nulldoc.docnum = -1;

		for (j = 1; j < model.sv_num; j++) {
			xlen = Math.sqrt(kernel(kernel_parm, model.supvec[j],
					model.supvec[j])
					- 2
					* kernel(kernel_parm, model.supvec[j], nulldoc)
					+ kernel(kernel_parm, nulldoc, nulldoc));
			if (xlen > maxxlen) {
				maxxlen = xlen;
			}
		}

		return (maxxlen);
	}

	private void estimate_transduction_quality(Model model, /*
															 * loo-bound based
															 * on observation
															 */
	long[] label, int[] unlabeled, long totdoc, /* that loo-errors must have an */
	Doc[] docs, double[] lin) {
		int i;
		int j;
		long l = 0, ulab = 0, lab = 0, labpos = 0, labneg = 0, ulabpos = 0, ulabneg = 0, totulab = 0;
		double totlab = 0, totlabpos = 0, totlabneg = 0, labsum = 0, ulabsum = 0;
		double r_delta, r_delta_sq, xi, xisum = 0, asum = 0;

		r_delta = estimate_r_delta(docs, totdoc, (model.kernel_parm));
		r_delta_sq = r_delta * r_delta;

		for (j = 0; j < totdoc; j++) {
			if (unlabeled[j] != 0) {
				totulab++;
			} else {
				totlab++;
				if (label[j] > 0)
					totlabpos++;
				else
					totlabneg++;
			}
		}
		for (j = 1; j < model.sv_num; j++) {
			i = model.supvec[j].docnum;
			xi = 1.0 - ((lin[i] - model.b) * (double) label[i]);
			if (xi < 0)
				xi = 0;

			xisum += xi;
			asum += Math.abs(model.alpha[j]);
			if (unlabeled[i] != 0) {
				ulabsum += (Math.abs(model.alpha[j]) * r_delta_sq + xi);
			} else {
				labsum += (Math.abs(model.alpha[j]) * r_delta_sq + xi);
			}
			if ((Math.abs(model.alpha[j]) * r_delta_sq + xi) >= 1) {
				l++;
				if (unlabeled[model.supvec[j].docnum] != 0) {
					ulab++;
					if (model.alpha[j] > 0)
						ulabpos++;
					else
						ulabneg++;
				} else {
					lab++;
					if (model.alpha[j] > 0)
						labpos++;
					else
						labneg++;
				}
			}
		}
		System.out.println("xacrit>=1: labeledpos=" + (double) labpos
				/ (double) totlab * 100.0 + " labeledneg=" + (double) labneg
				/ (double) totlab * 100.0 + " default=" + (double) totlabpos
				/ (double) (totlab) * 100.0);
		System.out.println("xacrit>=1: unlabelpos=" + (double) ulabpos
				/ (double) totulab * 100.0 + " unlabelneg=" + (double) ulabneg
				/ (double) totulab * 100.0);
		System.out.println("xacrit>=1: labeled=" + (double) lab
				/ (double) totlab * 100.0 + " unlabled=" + (double) ulab
				/ (double) totulab * 100.0 + " all=" + (double) l
				/ (double) (totdoc) * 100.0);
		System.out.println("xacritsum: labeled=" + (double) labsum
				/ (double) totlab * 100.0 + " unlabled=" + (double) ulabsum
				/ (double) totulab * 100.0 + " all="
				+ (double) (labsum + ulabsum) / (double) (totdoc) * 100.0);
		System.out.println("r_delta_sq=" + r_delta_sq + " xisum=" + xisum
				+ " asum=" + asum);
	}

	private double length_of_longest_document_vector(Doc[] docs, long totdoc,
			KernelParm kernel_parm) {
		int i;
		double maxxlen, xlen;
		maxxlen = 0;
		for (i = 0; i < totdoc; i++) {
			xlen = Math.sqrt(kernel(kernel_parm, (docs[i]), (docs[i])));
			if (xlen > maxxlen) {
				maxxlen = xlen;
			}
		}

		return (maxxlen);
	}

	private double distribute_alpha_t_greedily(int[] sv2dnum, int svnum,
			Doc[] docs, double[] a, int docnum, long[] label,
			KernelParm kernel_parm, LearnParm learn_parm, double thresh) {
		int i;
		int j;
		int k;
		int d;
		boolean skip, allskip;
		double best;
		double[] best_val = new double[101];
		double val, init_val_sq, init_val_lin;
		int[] best_ex = new int[101];
		double[] cache;
		double[] trow;

		cache = new double[learn_parm.xa_depth * svnum];
		trow = new double[svnum];

		for (k = 0; k < svnum; k++) {
			trow[k] = kernel(kernel_parm, (docs[docnum]), (docs[sv2dnum[k]]));
		}

		init_val_sq = 0;
		init_val_lin = 0;
		best = 0;

		for (d = 0; d < learn_parm.xa_depth; d++) {
			allskip = true;
			if (d >= 1) {
				init_val_sq += cache[best_ex[d - 1] + svnum * (d - 1)];
				for (k = 0; k < d - 1; k++) {
					init_val_sq += 2.0 * cache[best_ex[k] + svnum * (d - 1)];
				}
				init_val_lin += trow[best_ex[d - 1]];
			}
			for (i = 0; i < svnum; i++) {
				skip = false;
				if (sv2dnum[i] == docnum)
					skip = true;
				for (j = 0; j < d; j++) {
					if (i == best_ex[j])
						skip = true;
				}

				if (!skip) {
					val = init_val_sq;
					val += docs[sv2dnum[i]].twonorm_sq;
					for (j = 0; j < d; j++) {
						val += 2.0 * cache[i + j * svnum];
					}
					val *= (1.0 / (2.0 * (d + 1.0) * (d + 1.0)));
					val -= ((init_val_lin + trow[i]) / (d + 1.0));

					if (allskip || (val < best_val[d])) {
						best_val[d] = val;
						best_ex[d] = i;
					}
					allskip = false;
					if (val < thresh) {
						i = svnum;
						/* sprintf(temstr,"EARLY"); */
					}
				}
			}
			if (!allskip) {
				for (k = 0; k < svnum; k++) {
					cache[d * svnum + k] = kernel(kernel_parm,
							(docs[sv2dnum[best_ex[d]]]), (docs[sv2dnum[k]]));
				}
			}
			if ((!allskip) && ((best_val[d] < best) || (d == 0))) {
				best = best_val[d];
			}
			if (allskip || (best < thresh)) {
				d = learn_parm.xa_depth;
			}
		}

		/* sprintf(temstr,"Distribute[%ld](%ld)=%f, ",docnum,best_depth,best); */
		return (best);
	}

	private void compute_xa_estimates(Model model, /*
													 * xa-estimate of error
													 * rate,
													 */
	long[] label, int[] unlabeled, int totdoc, /* recall, and precision */
	Doc[] docs, double[] lin, double[] a, KernelParm kernel_parm,
			LearnParm learn_parm, DoubleValue error, DoubleValue recall,
			DoubleValue precision) {
		int i;
		long looerror, looposerror, loonegerror;
		long totex, totposex;
		double xi, r_delta, r_delta_sq, sim = 0;
		int[] sv2dnum = null;
		boolean[] sv = null;
		int svnum;

		r_delta = estimate_r_delta(docs, totdoc, kernel_parm);
		r_delta_sq = r_delta * r_delta;

		looerror = 0;
		looposerror = 0;
		loonegerror = 0;
		totex = 0;
		totposex = 0;
		svnum = 0;

		if (learn_parm.xa_depth > 0) {
			sv = new boolean[totdoc + 11];
			for (i = 0; i < totdoc; i++)
				sv[i] = false;
			for (i = 1; i < model.sv_num; i++)
				if (a[model.supvec[i].docnum] < (learn_parm.svm_cost[model.supvec[i].docnum] - learn_parm.epsilon_a)) {
					sv[model.supvec[i].docnum] = true;
					svnum++;
				}
			sv2dnum = new int[totdoc + 11];
			clear_index(sv2dnum);
			compute_index(sv, totdoc, sv2dnum);
		}

		for (i = 0; i < totdoc; i++) {
			if (unlabeled[i] != 0) {
				/* ignore it */
			} else {
				xi = 1.0 - ((lin[i] - model.b) * (double) label[i]);
				if (xi < 0)
					xi = 0;
				if (label[i] > 0) {
					totposex++;
				}
				if ((learn_parm.rho * a[i] * r_delta_sq + xi) >= 1.0) {
					if (learn_parm.xa_depth > 0) { /* makes assumptions */
						sim = distribute_alpha_t_greedily(
								sv2dnum,
								svnum,
								docs,
								a,
								i,
								label,
								kernel_parm,
								learn_parm,
								(double) ((1.0 - xi - a[i] * r_delta_sq) / (2.0 * a[i])));
					}
					if ((learn_parm.xa_depth == 0)
							|| ((a[i] * docs[i].twonorm_sq + a[i] * 2.0 * sim + xi) >= 1.0)) {
						looerror++;
						if (label[i] > 0) {
							looposerror++;
						} else {
							loonegerror++;
						}
					}
				}
				totex++;
			}
		}

		(error.value) = ((double) looerror / (double) totex) * 100.0;
		(recall.value) = (1.0 - (double) looposerror / (double) totposex) * 100.0;
		(precision.value) = (((double) totposex - (double) looposerror) / ((double) totposex
				- (double) looposerror + (double) loonegerror)) * 100.0;
	}

	/* compute length of weight vector */
	private double model_length_s(Model model, KernelParm kernel_parm) {
		int i, j;
		double sum = 0, alphai;
		Doc supveci;

		for (i = 1; i < model.sv_num; i++) {
			alphai = model.alpha[i];
			supveci = model.supvec[i];
			for (j = 1; j < model.sv_num; j++) {
				sum += alphai * model.alpha[j]
						* kernel(kernel_parm, supveci, model.supvec[j]);
			}
		}
		return (Math.sqrt(sum));
	}

	private double estimate_margin_vcdim(Model model, double w, /*
																 * optional:
																 * length of
																 * model vector
																 * in feature
																 * space
																 */
	double R, /* optional: radius of ball containing the data */
	KernelParm kernel_parm) {
		double h;

		/* follows chapter 5.6.4 in [Vapnik/95] */

		if (w < 0) {
			w = model_length_s(model, kernel_parm);
		}
		if (R < 0) {
			R = estimate_sphere(model, kernel_parm);
		}
		h = w * w * R * R + 1;
		return (h);
	}

	/******************************** svm_learn ****************************/
	/*
	 * Learns an SVM model based on the training data in docs/label. The
	 * resulting model is returned in the structure model.
	 */
	public void svm_learn(Doc[] docs, long[] label, int totdoc, int totwords,
			LearnParm learn_parm, KernelParm kernel_parm,
			KernelCache kernel_cache, Model model) {
		boolean[] inconsistent;
		int i;
		long inconsistentnum;
		int misclassified;
		long upsupvecnum;
		double loss, model_length, example_length;
		DoubleValue maxdiff = new DoubleValue();
		double[] lin;
		double[] a;
		// long runtime_start,runtime_end;
		long iterations;
		int[] unlabeled;
		boolean transduction;
		int heldout;
		long loo_count = 0, loo_count_pos = 0, loo_count_neg = 0, trainpos = 0, trainneg = 0;
		long loocomputed = 0, runtime_start_loo = 0, runtime_start_xa = 0;
		double heldout_c = 0, r_delta_sq = 0, r_delta, r_delta_avg;

		double[] xi_fullset; /* buffer for storing xi on full sample in loo */
		double[] a_fullset; /* buffer for storing alpha on full sample in loo */
		TimingProfile timing_profile = new TimingProfile();
		ShrinkState shrink_state = new ShrinkState();

		// runtime_start=System.currentTimeMillis();

		timing_profile.time_kernel = 0;
		timing_profile.time_opti = 0;
		timing_profile.time_shrink = 0;
		timing_profile.time_update = 0;
		timing_profile.time_model = 0;
		timing_profile.time_check = 0;
		timing_profile.time_select = 0;

		com_result.kernel_cache_statistic = 0;

		learn_parm.totwords = totwords;

		/* make sure -n value is reasonable */
		if ((learn_parm.svm_newvarsinqp < 2)
				|| (learn_parm.svm_newvarsinqp > learn_parm.svm_maxqpsize)) {
			learn_parm.svm_newvarsinqp = learn_parm.svm_maxqpsize;
		}

		init_shrink_state(shrink_state, totdoc, 10000);

		inconsistent = new boolean[totdoc];
		unlabeled = new int[totdoc];
		a = new double[totdoc];
		a_fullset = new double[totdoc];
		xi_fullset = new double[totdoc];
		lin = new double[totdoc];
		learn_parm.svm_cost = new double[totdoc];
		model.supvec = new Doc[totdoc + 2];
		model.alpha = new double[totdoc + 2];
		model.index = new int[totdoc + 2];

		model.at_upper_bound = 0;
		model.b = 0;
		model.supvec[0] = null; /* element 0 reserved and empty for now */
		model.alpha[0] = 0;
		model.lin_weights = null;
		model.totwords = totwords;
		model.totdoc = totdoc;
		model.kernel_parm = kernel_parm;
		model.sv_num = 1;
		model.loo_error = -1;
		model.loo_recall = -1;
		model.loo_precision = -1;
		model.xa_error = new DoubleValue();
		model.xa_error.value = -1;
		model.xa_recall = new DoubleValue();
		model.xa_recall.value = -1;
		model.xa_precision = new DoubleValue();
		model.xa_precision.value = -1;
		inconsistentnum = 0;
		transduction = false;

		r_delta = estimate_r_delta(docs, totdoc, kernel_parm);
		r_delta_sq = r_delta * r_delta;

		r_delta_avg = estimate_r_delta_average(docs, totdoc, kernel_parm);
		if (learn_parm.svm_c == 0.0) { /* default value for C */
			learn_parm.svm_c = 1.0 / (r_delta_avg * r_delta_avg);
			if (com_pro.show_compute_1) {
				System.out
						.println("Setting default regularization parameter C="
								+ learn_parm.svm_c);
			}
		}

		for (i = 0; i < totdoc; i++) {
			/* various inits */
			inconsistent[i] = false;
			a[i] = 0;
			lin[i] = 0;
			unlabeled[i] = 0;
			if (label[i] == 0) {
				unlabeled[i] = 1;
				transduction = true;
			}
			if (label[i] > 0) {
				learn_parm.svm_cost[i] = learn_parm.svm_c
						* learn_parm.svm_costratio
						* Math.abs((double) label[i]);
				label[i] = 1;
				trainpos++;
			} else if (label[i] < 0) {
				learn_parm.svm_cost[i] = learn_parm.svm_c
						* Math.abs((double) label[i]);
				label[i] = -1;
				trainneg++;
			} else {
				learn_parm.svm_cost[i] = 0;
			}
		}

		/* caching makes no sense for linear kernel */
		if (kernel_parm.kernel_type == LINEAR) {
			kernel_cache = null;
		}

		if (transduction) {
			learn_parm.svm_iter_to_shrink = 99999999;
			System.out
					.println("\nDeactivating Shrinking due to an incompatibility with the transductive \nlearner in the current version.\n\n");
		}

		if (transduction && learn_parm.compute_loo) {
			learn_parm.compute_loo = false;
			System.out
					.println("\nCannot compute leave-one-out estimates for transductive learner.\n\n");
		}

		if (learn_parm.remove_inconsistent != 0 && learn_parm.compute_loo) {
			learn_parm.compute_loo = false;
			System.out
					.println("\nCannot compute leave-one-out estimates when removing inconsistent examples.\n\n");
		}

		if ((trainpos == 1) || (trainneg == 1)) {
			learn_parm.compute_loo = false;
			System.out
					.println("\nCannot compute leave-one-out with only one example in one class.\n\n");
		}

		// if (com_pro.show_action)
		{
			System.out.println("Optimizing...");
		}

		/* train the svm */
		iterations = optimize_to_convergence(docs, label, totdoc, totwords,
				learn_parm, kernel_parm, kernel_cache, shrink_state, model,
				inconsistent, unlabeled, a, lin, timing_profile, maxdiff,
				(long) -1, (long) 1);
		// if (com_pro.show_action)
		{
			System.out.println("done. (" + iterations + " iterations) ");
		}

		misclassified = 0;
		for (i = 0; (i < totdoc); i++) { /* get final statistic */
			if ((lin[i] - model.b) * (double) label[i] <= 0.0)
				misclassified++;
		}
		if (com_pro.show_action) {
			System.out.println("optimization finished");
		}
		if (com_pro.show_trainresult) {
			System.out.println(" (" + misclassified
					+ " misclassified, maxdiff=" + maxdiff.value + ").");
		}
		com_result.train_misclassify = misclassified;
		com_result.max_difference = maxdiff.value;

		// runtime_end=System.currentTimeMillis();

		if (learn_parm.remove_inconsistent != 0) {
			inconsistentnum = 0;
			for (i = 0; i < totdoc; i++)
				if (inconsistent[i])
					inconsistentnum++;
			System.out
					.println("Number of SV: " + (model.sv_num - 1) + " (plus "
							+ inconsistentnum + " inconsistent examples)\n");
		} else {
			upsupvecnum = 0;
			for (i = 1; i < model.sv_num; i++) {
				if (Math.abs(model.alpha[i]) >= (learn_parm.svm_cost[(model.supvec[i]).docnum] - learn_parm.epsilon_a))
					upsupvecnum++;
			}
			if (com_pro.show_trainresult) {
				System.out.println("Number of SV: " + (model.sv_num - 1)
						+ " (including " + upsupvecnum + " at upper bound)\n");
			}
		}

		if ((!learn_parm.skip_final_opt_check)) {
			loss = 0;
			model_length = 0;
			for (i = 0; i < totdoc; i++) {
				if ((lin[i] - model.b) * (double) label[i] < 1.0 - learn_parm.epsilon_crit)
					loss += 1.0 - (lin[i] - model.b) * (double) label[i];
				model_length += a[i] * label[i] * lin[i];
			}
			model_length = Math.sqrt(model_length);
			System.out.println("L1 loss: loss=" + loss);
			System.out.println("Norm of weight vector: |w|=" + model_length);
			example_length = estimate_sphere(model, kernel_parm);
			System.out.println("Norm of longest example vector: |x|="
					+ length_of_longest_document_vector(docs, totdoc,
							kernel_parm));
			System.out.println("Estimated VCdim of classifier: VCdim<="
					+ estimate_margin_vcdim(model, model_length,
							example_length, kernel_parm));
			if ((learn_parm.remove_inconsistent == 0) && (!transduction)) {
				runtime_start_xa = System.currentTimeMillis();
				System.out.println("Computing XiAlpha-estimates...");
				compute_xa_estimates(model, label, unlabeled, totdoc, docs,
						lin, a, kernel_parm, learn_parm, (model.xa_error),
						(model.xa_recall), (model.xa_precision));

				System.out
						.println("Runtime for XiAlpha-estimates in cpu-seconds: "
								+ (System.currentTimeMillis() - runtime_start_xa)
								/ 100.0);

				System.out.println("XiAlpha-estimate of the error: error<="
						+ model.xa_error + "%% (rho=" + learn_parm.rho
						+ ",depth=" + learn_parm.xa_depth + ")");
				System.out.println("XiAlpha-estimate of the recall: recall=>"
						+ model.xa_recall + "%% (rho=" + learn_parm.rho
						+ ",depth=" + learn_parm.xa_depth + ")");
				System.out
						.println("XiAlpha-estimate of the precision: precision=>"
								+ model.xa_precision
								+ "%% (rho="
								+ learn_parm.rho
								+ ",depth="
								+ learn_parm.xa_depth + ")");
			} else if (learn_parm.remove_inconsistent == 0) {
				estimate_transduction_quality(model, label, unlabeled, totdoc,
						docs, lin);
			}
		}
		if (com_pro.show_trainresult) {
			System.out.println("Number of kernel evaluations: "
					+ com_result.kernel_cache_statistic);
		}

		/* leave-one-out testing starts now */
		if (learn_parm.compute_loo) {
			System.out.println("learn_parm.compute_loo:"
					+ learn_parm.compute_loo);
			/* save results of training on full dataset for leave-one-out */
			runtime_start_loo = System.currentTimeMillis();
			for (i = 0; i < totdoc; i++) {
				xi_fullset[i] = 1.0 - ((lin[i] - model.b) * (double) label[i]);
				a_fullset[i] = a[i];
			}
			System.out.println("Computing leave-one-out");

			/* repeat this loop for every held-out example */
			for (heldout = 0; (heldout < totdoc); heldout++) {
				if (learn_parm.rho * a_fullset[heldout] * r_delta_sq
						+ xi_fullset[heldout] < 1.0) {
					/* guaranteed to not produce a leave-one-out error */
					System.out.println("+");
				} else if (xi_fullset[heldout] > 1.0) {
					/* guaranteed to produce a leave-one-out error */
					loo_count++;
					if (label[heldout] > 0)
						loo_count_pos++;
					else
						loo_count_neg++;
					System.out.print("-");
				} else {
					loocomputed++;
					heldout_c = learn_parm.svm_cost[heldout]; /*
															 * set upper bound
															 * to zero
															 */
					learn_parm.svm_cost[heldout] = 0;
					/* make sure heldout example is not currently */
					/* shrunk away. Assumes that lin is up to date! */
					shrink_state.active[heldout] = 1;

					optimize_to_convergence(docs, label, totdoc, totwords,
							learn_parm, kernel_parm, kernel_cache,
							shrink_state, model, inconsistent, unlabeled, a,
							lin, timing_profile, maxdiff, heldout, (long) 2);

					/*
					 * printf("%f\n",(lin[heldout]-model->b)(double)label[heldout
					 * ]);
					 */

					if (((lin[heldout] - model.b) * (double) label[heldout]) < 0.0) {
						loo_count++; /* there was a loo-error */
						if (label[heldout] > 0)
							loo_count_pos++;
						else
							loo_count_neg++;
					} else {

					}
					/* now we need to restore the original data set */
					learn_parm.svm_cost[heldout] = heldout_c; /*
															 * restore upper
															 * bound
															 */
				}
			} /* end of leave-one-out loop */

			System.out.println("\nRetrain on full problem");
			optimize_to_convergence(docs, label, totdoc, totwords, learn_parm,
					kernel_parm, kernel_cache, shrink_state, model,
					inconsistent, unlabeled, a, lin, timing_profile, maxdiff,
					(long) -1, (long) 1);

			/* after all leave-one-out computed */
			model.loo_error = 100.0 * loo_count / (double) totdoc;
			model.loo_recall = (1.0 - (double) loo_count_pos
					/ (double) trainpos) * 100.0;
			model.loo_precision = (trainpos - loo_count_pos)
					/ (double) (trainpos - loo_count_pos + loo_count_neg)
					* 100.0;
			System.out.println("Leave-one-out estimate of the error: error="
					+ model.loo_error + "%%");
			System.out.println("Leave-one-out estimate of the recall: recall="
					+ model.loo_recall + "%%");
			System.out
					.println("Leave-one-out estimate of the precision: precision="
							+ model.loo_precision + "%%");
			System.out.println("Actual leave-one-outs computed:  "
					+ loocomputed + " (rho=" + learn_parm.rho + ")");
			System.out.println("Runtime for leave-one-out in cpu-seconds: "
					+ (double) (System.currentTimeMillis() - runtime_start_loo)
					/ 100.0);
		}

		// if(learn_parm->alphafile[0])
		// write_alphas(learn_parm->alphafile,a,label,totdoc);

		// TODO: delete this
		// shrink_state_cleanup(shrink_state);
	}

	private int nol_ll(String file, IntValue nol, IntValue wol, LongValue ll) {
		int ic;
		char c;
		long current_length;
		int current_wol;

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));

			current_length = 0;
			current_wol = 0;
			ll.value = 0;
			nol.value = 1;
			wol.value = 0;

			while ((ic = in.read()) != -1) {
				c = (char) ic;
				current_length++;
				if (c == ' ') {
					current_wol++;
				}
				if (c == '\n') {
					nol.value++;
					if (current_length > (ll.value)) {
						(ll.value) = current_length;
					}
					if (current_wol > (wol.value)) {
						(wol.value) = current_wol;
					}
					current_length = 0;
					current_wol = 0;
				}
			}

			in.close();
		} catch (FileNotFoundException e1) {
			// If this file does not exist
			return -1;
		} catch (IOException e1) {
			// If this file does not exist
			return -1;
		}
		return 1;
	}

	/* read the data from text documents */
	private boolean read_documents(String docfile, Doc docs[], long label[],
			int max_words_doc, long ll, IntValue totwords, IntValue totdoc,
			int post_label) {
		String temstr = new String();
		// int n;
		String line;
		Doc doc = new Doc();
		int dnum = 0;
		IntValue wpos = new IntValue();
		int i;
		long dpos = 0;
		long dneg = 0;
		long dunlab = 0;
		LongValue doc_label = new LongValue();
		// File docfl;
		// File file = new File(docfile);
		try {
			BufferedReader in = new BufferedReader(new FileReader(docfile));

			// If this file does not exist

			doc.words = new Word[max_words_doc + 10];
			for (int k = 0; k < doc.words.length; ++k) {
				doc.words[k] = new Word();
			}

			if (com_pro.show_readfile) {
				System.out.println(temstr + "Reading examples into memory...");
				System.out.println(temstr);
			}

			dnum = 0;
			(totwords.value) = 0;
			// while((!feof(docfl)) && fgets(line,(int)ll,docfl))

			while ((line = in.readLine()) != null) {
				if (line.startsWith("#"))
					continue; /* line contains comments */
				if (parse_document(line, doc, doc_label, wpos, max_words_doc) != -1) {
					System.out.println(temstr + "Parsing error in line %ld!"
							+ dnum);
					System.out.println(temstr);
				}

				if (doc_label.value == 0) {
					label[dnum] = 0;
					dunlab++;
				} else if (doc_label.value == post_label) {
					label[dnum] = 1;
					dpos++;
				} else {
					label[dnum] = -1;
					dneg++;
				}

				if ((wpos.value > 1)
						&& ((doc.words[(int) wpos.value - 2]).wnum > (totwords.value)))
					(totwords.value) = (doc.words[(int) wpos.value - 2]).wnum;
				// docs[dnum].words = (word *)my_malloc(sizeof(SVM_word)*wpos);
				docs[dnum].docnum = dnum;

				if (docs[dnum].words == null) {
					docs[dnum].words = new Word[wpos.value];
				}

				for (i = 0; i < wpos.value; i++) {
					docs[dnum].words[i] = new Word(doc.words[i].wnum,
							doc.words[i].weight);
				}
				docs[dnum].twonorm_sq = doc.twonorm_sq;
				dnum++;
				if ((dnum % 100) == 0 && com_pro.show_readfile) {
					System.out.println(temstr + "read %ld.." + dnum);
					System.out.println(temstr);
				}
			}
			in.close();

		} catch (FileNotFoundException e1) {
			System.out.println("File not Found file:" + docfile);
			return false;
		} catch (IOException e1) {
			System.out.println("IOException file:" + docfile);
			// If this file does not exist
			return false;
		}

		if (com_pro.show_action) {
			System.out.println(temstr + "OK. (%ld examples read)" + dnum);
			System.out.println(temstr);
			System.out.println(temstr
					+ "%ld positive, %ld negative, and %ld unlabeled examples."
					+ dpos + dneg + dunlab);
			System.out.println(temstr);
		}

		totdoc.value = dnum;
		// System.out.println("read_documents.totdoc:"+totdoc.value);
		return true;
	}

	private double parse_document(String line, Doc doc, LongValue label,
			IntValue numwords, int max_words_doc) {
		// String temstr = new String();
		// System.out.println("parse_document:"+line);
		// int n;
		int wpos;
		int pos;
		int wnum;
		float weight;
		pos = line.indexOf("#");
		/* cut off comments */

		if (pos >= 0) {
			line = line.substring(0, pos);
		}
		wpos = 0;
		StringTokenizer st = new StringTokenizer(line, " ");

		if ((label.value = Long.parseLong(st.nextToken())) == -1)
			return (0);

		while (st.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ":");
			wnum = Integer.parseInt(st2.nextToken());
			if (wnum < 0) {
				System.out
						.println("Feature numbers must be larger or equal to 1!!!");
				return (0);
			}
			if ((wpos > 0) && ((doc.words[wpos - 1]).wnum >= wnum)) {
				System.out.println("Features must be in increasing order!!!");

				return (0);
			}
			weight = Float.parseFloat(st2.nextToken());

			(doc.words[wpos]).wnum = wnum;
			(doc.words[wpos]).weight = weight;
			wpos++;
		}

		(doc.words[wpos]).wnum = 0;
		(numwords.value) = wpos + 1;
		doc.docnum = -1;
		doc.twonorm_sq = Model.sprod_ss(doc.words, doc.words);

		return (-1);
	}
}
