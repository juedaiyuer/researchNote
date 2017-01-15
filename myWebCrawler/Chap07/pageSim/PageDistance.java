package com.lietu.newsspider.page;

import java.util.ArrayList;

import org.htmlparser.Node;

public class PageDistance {
	// ****************************
	// Get minimum of three values
	// ****************************
	private static int Minimum(int a, int b, int c) {
		int mi;
		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}

	/**
	 * 
	 * @param s 输入源串
	 * @param t 输入目标串
	 * @return 源串和目标串之间的编辑距离
	 */
	public static double LD(ArrayList<Node> s, ArrayList<Node> t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		Node s_i; // ith character of s
		Node t_j; // jth character of t
		int cost; // cost

		// Step 1 初始化
		n = s.size();
		m = t.size();
		if (n == 0) {
			return 1;
		}
		if (m == 0) {
			return 1;
		}
		d = new int[n + 1][m + 1];

		// Step 2 Initialize the first row to 0..n.
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		//Initialize the first column to 0..m.
		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3 Examine each character of s (i from 1 to n).
		for (i = 1; i <= n; i++) {
			s_i = s.get(i - 1);

			// Step 4 Examine each character of t (j from 1 to m).
			for (j = 1; j <= m; j++) {
				t_j = t.get(j - 1);

				// Step 5
				//	If s[i] equals t[j], the cost is 0.
				//  If s[i] doesn't equal t[j], the cost is 1.
				if (s_i.getClass().equals(t_j.getClass())) {
					cost = 0;
				} else {
					//System.out.println("diff"+s_i.toHtml()+" "+t_j.toHtml());
					cost = 1;
				}

				// Step 6
				//Set cell d[i,j] of the matrix equal to the minimum of:
				//a. The cell immediately above plus 1: d[i-1,j] + 1.
				//b. The cell immediately to the left plus 1: d[i,j-1] + 1.
				//c. The cell diagonally above and to the left plus the cost: d[i-1,j-1] + cost.
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
						d[i - 1][j - 1] + cost);
				
				//System.out.println("d["+i+"]["+j+"]:"+d[i][j]);
			}
		}

		// Step 7
		//	After the iteration steps (3, 4, 5, 6) are complete, the distance is found in cell d[n,m].
		double ret = 0.0D;
		if (n > m){
			ret = (double)d[n][m] / (double)n;
		} else {
			ret = (double)d[n][m] / (double)m;
		}
		return ret;
//		return d[n][m];
	}
}
