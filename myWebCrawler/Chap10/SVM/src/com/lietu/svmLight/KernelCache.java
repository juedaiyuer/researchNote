package com.lietu.svmLight;

import java.util.ArrayList;

public class KernelCache {
	public int   index[];  /* cache some kernel evalutations */
	public ArrayList<Double> buffer; /* to improve speed */
	public int   invindex[];
	public int   active2totdoc[];
	public int   totdoc2active[];
	public long   lru[];
	public long   occu[];
	public long   elems;
	public long   max_elems;
	public long   time;
	public int   activenum;
	public long   buffsize;
}
