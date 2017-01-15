package com.lietu.svmLight;

public class KernelParm {
	public int kernel_type;   
	public long poly_degree;
	public double rbf_gamma;
	public double coef_lin;
	public double coef_const;
	public String custom; /* for user supplied kernel */

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("kernel_type:");
		sb.append(kernel_type);
		sb.append('\n');
		
		sb.append("poly_degree:");
		sb.append(poly_degree);
		sb.append('\n');

		sb.append("rbf_gamma:");
		sb.append(rbf_gamma);
		sb.append('\n');

		sb.append("coef_lin:");
		sb.append(coef_lin);
		sb.append('\n');

		sb.append("coef_const:");
		sb.append(coef_const);
		sb.append('\n');
		
		return sb.toString();
	}
}
