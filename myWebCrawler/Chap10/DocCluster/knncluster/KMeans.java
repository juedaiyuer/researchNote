package knncluster;

import java.lang.Math;

public class KMeans {
	//private static int[] CenterId;
	//to computer the EuclideanDistance
	private static double euDistance(double array1[], double array2[]) {
		double Dist = 0.0;
		if (array1.length != array2.length) {
			System.out.println("the number of the arrary is ineql");
		}
		
		for (int i = 0; i < array2.length; i++) {
			Dist += (array1[i] - array2[i]) * (array1[i] - array2[i]);
		}
		return Math.sqrt(Dist);
	}
	
	//to print the int Array
	private static void printArray(int array[]) {
		System.out.print('[');
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if ((i + 1) < array.length) {
				System.out.print(", ");
			}
		}
		System.out.println(']');
	}
	
	//返回一个M个元素组成的随机整数数组，其中每个元素的取值范围从0到n-1
	public static int[] randperm(int N,int M){
		double[]  permF=new double[N];
		int[]     permI=new int[N];
		int[]     sampleResult=new int[M];
		double tempF;
		int    tempI;
		for(int i=0; i<N; i++){
			permF[i]=Math.random();
			permI[i]=i;			
		}
		//通过排序把数组permI随机化
		for(int i=0; i<N-1; i++){
			for(int j=i+1; j<N; j++){
				if(permF[i]<permF[j]){
					tempF=permF[i];
					tempI=permI[i];
					permF[i]=permF[j];
					permI[i]=permI[j];
					permF[j]=tempF;
					permI[j]=tempI;					
				}
			}
		}
		//取前m个值返回
		for(int i=0; i<M; i++){
			sampleResult[i]=permI[i];
		}
		return sampleResult;
	}
	
	//the judge the equal two Array
	private static boolean isEqual(int Array1[],int Array2[]){
		for(int i=0; i<Array1.length; i++){
			if(Array1[i]!=Array2[i]){
				return false;
			}
		}
		return true;		
	}
	
	//get the location of min element from the Array
	private static int minLocation(double Array[]){
		int Location;
		double Min;
		//initial
		Min=Array[0];
		Location=0;
		//Iteration
		for(int i=1; i<Array.length; i++){
			if(Array[i]<Min){
				Location=i;
				Min=Array[i];			  
			}
		}
		return Location;
	}
	
	private static boolean isInArray(int[] Array,int elem){
		for(int i=0;i<Array.length;i++){
			if(Array[i]==elem){
				return true;
			}
		}
		return false;
	}
	
	//public static int[] GetInitCluster(){
	//	return CenterId;
	//}
	
	public static int[] minMaxInitCluster(double matrix[][],int row,int col,int clusterNum){
		int i,j,r,c;
		r=c=0; //所有对象中相距最远的两个对象编号
		int [] result=new int[clusterNum];//聚点结果数组
		for(i=0;i<clusterNum;i++){
			result[i]=-2;
		}
		double[][]  distMatrix=new double[row][row];//距离矩阵
		double max=0.0;
		
		//发现所有对象中相距最远的两个对象
		for(i=0; i<row; i++){
			for( j=0; j<row; j++){
				//发现第i个和第j个对象之间的距离
				distMatrix[i][j]=euDistance(matrix[i],matrix[j]);
				distMatrix[j][i]=distMatrix[i][j];
				if(i>j){
					if(distMatrix[i][j]>max){
						max=distMatrix[i][j];
						r=i;
						c=j;
					}
				}
			}
		}
		
		int num=2;
		result[0]=r;
		result[1]=c;
		int next=-1; //记录下一个聚点编号
		while(num<clusterNum){
			max=0.0;
			for(i=0;i<row;i++){
				if( !isInArray(result,i)){
					//找出和所有已知聚点最近的聚点中最远的点
					double min=Double.MAX_VALUE;
					for(j=0;j<num;j++){
						if(distMatrix[i][result[j]]<min){
							min=distMatrix[i][result[j]];
						}
					}
					if(min>max){
						max=min;
						next=i;
					}
				}
			}
			result[num]=next;
			num++;
		}
		return result;
	}
	
	//to clustering the data Matrix
	public static int[] kCluster(double matrix[][], int clusterNum){
		int row = matrix.length;//行数，也就是点的个数
		int col = matrix[0].length;//列数，也就是特征个数
		int[] centerId=new int[clusterNum];//初始聚点编号
    	int[]  cId=new int[row];//聚类结果
    	
    	int[]  oldCid=new int[row];//上次聚类结果
    	int[]  numOfEveryCluster=new int[clusterNum];
    	double[][]  clusterCenter=new double[clusterNum][col];//聚点
    	double[]  centerDist=new double[clusterNum];
    	//初始化聚类中心
    	centerId= minMaxInitCluster(matrix, row, col,clusterNum);
    	/*	System.out.println("Init cluster center is :");
    	for(int k=0;k<CenterId.length;k++){
    		System.out.print(CenterId[k]+" 	");
    	}*/
    	System.out.println();
    	for(int i=0; i<clusterNum; i++){
    		for(int j=0; j<col; j++){
    			 clusterCenter[i][j]=matrix[ centerId[i] ][j];    		
    		}
    	}
    	
    	int maxIter=100;//最大叠代次数
    	int iter=1;
    	
    	while( !isEqual(cId,oldCid) && iter < maxIter){
    		System.arraycopy(cId, 0, oldCid, 0, cId.length);
    		
    		//遍历每个点，发现它到每一个聚点的距离
    		for(int i=0;i<row;i++){
    			for(int j=0; j<clusterNum;j++){
    				centerDist[j]=euDistance(matrix[i], clusterCenter[j] );
    			}
    			//将点归类给距离最近的簇
    			cId[i]=minLocation(centerDist);    			
    		}
    		
    		//得到每个簇拥有的点数
    		for(int j=0; j<clusterNum; j++){
    			numOfEveryCluster[j]=0;
    			for(int i=0; i<row; i++){
    				if(cId[i]==j){
    					numOfEveryCluster[j]++;    					
    				}    			
    		    }
    		}
    		
    		//计算新的聚点
    		//求和
    		for(int j=0; j<clusterNum; j++){
    			for(int k=0; k<col; k++){
    				clusterCenter[j][k]=0.0;
    				for(int i=0; i<row; i++){
    					if(cId[i]==j){
    						clusterCenter[j][k]+=matrix[i][k];
    					}
    				}
    			}
    	    }
       	    //求平均值
       	    for(int j=0; j<clusterNum; j++){
       	    	for(int k=0; k<col; k++){
       	    		clusterCenter[j][k]=clusterCenter[j][k]/(double)numOfEveryCluster[j];
       	    	}
       	    }
       		++iter;//叠代次数
    	}
    	
    	return cId;
    }
    	
    //main to test the KMeans
  public static void main(String[] args) {
    	int Matrix_row;
    	int Matrix_col;
    	int ClusterNum;
    	Matrix_col=1000;
    	Matrix_row=1000;
    	ClusterNum=15;
		double[][]  Matrix = new double[Matrix_row][Matrix_col];
		int[]  List=new int[Matrix_row];
	
		for(int i=0; i<Matrix_row; i++){
			for(int j=0; j<Matrix_col; j++){
				Matrix[i][j]=10*Math.random();
			}
		}
		
	    List=kCluster(Matrix, ClusterNum);
	    System.out.println("The result of clustering, value of No.i means the ith belong to the No.value cluster");
	    printArray(List);
	}
}