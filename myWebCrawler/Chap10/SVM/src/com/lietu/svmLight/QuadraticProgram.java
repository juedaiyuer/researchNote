package com.lietu.svmLight;

public class QuadraticProgram {
	public int   opt_n;            /* number of variables */
	public int   opt_m;            /* number of linear equality constraints */
	public double opt_ce[],opt_ce0[]; /* linear equality constraints */
	public double opt_g[];           /* hessian of objective */
	public double opt_g0[];          /* linear part of objective */
	public double opt_xinit[];       /* initial value for variables */
	public double opt_low[],opt_up[]; /* box constraints */
}
