package com.lietu.svmLight;

public class LearnParm {
	public double svm_c;                /* upper bound C on alphas */
	public double svm_costratio;        /* factor to multiply C for positive examples */
	public double transduction_posratio;/* fraction of unlabeled examples to be */	/* classified as positives */
	public boolean   biased_hyperplane;    /* if nonzero, use hyperplane w*x+b=0 	otherwise w*x=0 */
	public int   svm_maxqpsize;        /* size q of working set */
	public long   svm_newvarsinqp;      /* new variables to enter the working set in each iteration */
	public double epsilon_crit;         /* tolerable error for distances used in stopping criterion */
	public double epsilon_shrink;       /* how much a multiplier should be above  zero for shrinking */
	public long   svm_iter_to_shrink;   /* iterations h after which an example can be removed by shrinking */
	public int  remove_inconsistent;  /* exclude examples with alpha at C and  retrain */
	
	public boolean  skip_final_opt_check; 
								 /* do not check KT-Conditions at the end of optimization for examples removed by  
								 shrinking. WARNING: This might lead to sub-optimal solutions! */
	
	public boolean  compute_loo;          /* if nonzero, computes leave-one-out	 estimates */
	public double rho;                  /* parameter in xi/alpha-estimates and for pruning leave-one-out range [1..2] */
	public int   xa_depth;             /* parameter in xi/alpha-estimates upper  bounding the number of SV the current alpha_t is distributed over */
	public String predfile;          /* file for predicitions on unlabeled examples					 in transduction */
	public String alphafile; 
	public double epsilon_const;        /* tolerable error on eq-constraint */
	public double epsilon_a;            /* tolerable error on alphas at bounds */
	public double opt_precision;        /* precision of solver, set to e.g. 1e-21 	 if you get convergence problems */
		
	/* the following are only for internal use */
	public long   svm_c_steps;          /* do so many steps for finding optimal C */
	public double svm_c_factor;         /* increase C by this factor every step */
	public double svm_costratio_unlab;
	public double svm_unlabbound;
	public double svm_cost[];            /* individual upper bounds for each var */
	public int   totwords;                /* number of features */
}
