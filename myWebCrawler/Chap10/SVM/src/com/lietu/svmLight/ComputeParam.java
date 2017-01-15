package com.lietu.svmLight;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lietu.classify.LEDataInputStream;
import com.lietu.classify.LEDataOutputStream;

public class ComputeParam {
	//Learning options
	public	double	C=0.0;
	public	double	cost_factor=1.0;
	public	boolean	biased_Hyperplane = true;
	public	int	remove_inconsitant=0;
	//performance estimations options
	public	boolean	loo=false;
	public	double	rho=1.0;
	public	int	search_depth=0;
	//transduction option
	public	double	fraction=1.0;
	//kernel option
	public	int	kernel_type=0;
	public	long	poly_degree = 1;
	public	double	rbf_gamma = 1.0;
	public	double	poly_s = 1.0;
	public	double	poly_c = 0.0;
	
	//optimization options 
	public	int	maximum_size=10;
	public	long 	new_variable=10;//2..maximun_size
	public	double	cache_size = 40.0;//5..,the larger, the faster
	public	double	epsion = 0.001;
	public	long	iteration_time=100;//default 100
	public	boolean	final_test=true;//default 1, to do final test.

	public	int     classifier_num;     //类别总数
	public	int		classifier_type;    //分类器的类型
	//output options

	public	String	trainfile;
	public	String	modelfile;
	public	String	resultfile;
	public	String	classifyfile;
	public	String matrixfile;
	public	String resultpath;

	public	int	running;  //0--not running  1--generate document vectors  2--train svm
	public	boolean paused;
	//global variables
	public	double	Coff[] = new double[3];
	
	public ComputeParam(){}
	
	public void DumpToFile(String strFileName){
		LEDataOutputStream fBinOut = null;
		try {
			fBinOut = new LEDataOutputStream(new FileOutputStream(
					strFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			fBinOut.writeDouble(C);
			fBinOut.writeDouble(cost_factor);
			fBinOut.writeBoolean(biased_Hyperplane);
			fBinOut.writeInt(remove_inconsitant);
			fBinOut.writeBoolean(loo);
			fBinOut.writeDouble(rho);
			fBinOut.writeInt(search_depth);
			fBinOut.writeDouble(fraction);
			fBinOut.writeInt(kernel_type);
			fBinOut.writeInt((int)poly_degree);
			fBinOut.writeDouble(rbf_gamma);
			fBinOut.writeDouble(poly_s);
			fBinOut.writeDouble(poly_c);
			fBinOut.writeInt(maximum_size);
			fBinOut.writeInt((int)new_variable);
			fBinOut.writeDouble(cache_size);
			fBinOut.writeDouble(epsion);
			fBinOut.writeInt((int)iteration_time);
			fBinOut.writeBoolean(final_test);
			fBinOut.writeInt(classifier_num);
			fBinOut.writeInt(classifier_type);
			
			//System.out.println("trainfile:"+trainfile);
			fBinOut.writeGBKString(trainfile);
			//System.out.println("modelfile:"+modelfile);
			fBinOut.writeGBKString(modelfile);
			//System.out.println("resultfile:"+resultfile);
			fBinOut.writeGBKString(resultfile);
			//System.out.println("classifyfile:"+classifyfile);
			fBinOut.writeGBKString(classifyfile);
			//System.out.println("matrixfile:"+matrixfile);
			fBinOut.writeGBKString(matrixfile);
			//System.out.println("resultpath:"+resultpath);
			fBinOut.writeGBKString(resultpath);
			fBinOut.writeDouble(Coff[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public	boolean GetFromFile(String strFileName){
		//System.out.println("Compute_Param:"+strFileName);

		LEDataInputStream fIn = null;
		
		try
		{
			fIn = new LEDataInputStream(new FileInputStream(
				strFileName));
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("无法打开文件"+strFileName+"!") ;
			return false;
		}

		try
		{
			C = fIn.readDouble();
			cost_factor = fIn.readDouble();
			biased_Hyperplane =fIn.readBoolean();//(fIn.readInt()!=0);
			remove_inconsitant = fIn.readInt();
			loo = fIn.readBoolean();//(fIn.readInt()!=0);
			rho = fIn.readDouble();
			search_depth = fIn.readInt();
			fraction = fIn.readDouble();
			kernel_type = fIn.readInt();
			poly_degree = fIn.readInt();
			rbf_gamma = fIn.readDouble();
			poly_s = fIn.readDouble();
			poly_c = fIn.readDouble();
			maximum_size = fIn.readInt();
			new_variable = fIn.readInt();//2..maximun_size
			cache_size = fIn.readDouble();//5..,the larger, the faster
			epsion = fIn.readDouble();
			iteration_time = fIn.readInt();//default 100
			final_test = fIn.readBoolean();//default 1, to do final test.
			classifier_num = fIn.readInt();
			classifier_type = fIn.readInt();

			trainfile = fIn.readGBKString();
			modelfile = fIn.readGBKString();
			resultfile = fIn.readGBKString();
			classifyfile = fIn.readGBKString();
			matrixfile = fIn.readGBKString();
			resultpath = fIn.readGBKString();
			Coff[2] = fIn.readDouble();
			//Coff[1] = fIn.readDouble();
			fIn.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("无法读文件"+strFileName+"!") ;
			return false;
		}
		
		return true;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("C:");
		sb.append(C);
		sb.append('\n');
		
		sb.append("cost_factor:");
		sb.append(cost_factor);
		sb.append('\n');

		sb.append("biased_Hyperplane:");
		sb.append(biased_Hyperplane);
		sb.append('\n');

		sb.append("remove_inconsitant:");
		sb.append(remove_inconsitant);
		sb.append('\n');

		sb.append("trainfile:");
		sb.append(trainfile);
		sb.append('\n');

		sb.append("modelfile:");
		sb.append(modelfile);
		sb.append('\n');

		sb.append("resultfile:");
		sb.append(resultfile);
		sb.append('\n');

		sb.append("classifyfile:");
		sb.append(classifyfile);
		sb.append('\n');

		sb.append("matrixfile:");
		sb.append(matrixfile);
		sb.append('\n');

		sb.append("resultpath:");
		sb.append(resultpath);
		sb.append('\n');

		sb.append("Coff[2]:");
		sb.append(Coff[2]);
		sb.append('\n');
		
		return sb.toString();
	}
}
