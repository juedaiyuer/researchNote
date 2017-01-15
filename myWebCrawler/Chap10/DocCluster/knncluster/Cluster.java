package knncluster;

public class Cluster {
	
	public double error_rate(){
		return 0.0;
	}
	public void KCluster(String path,int clusternum){
		FeathuerSelection fs=new FeathuerSelection();
		double[][] matrix=fs.GetFeatherMatrix(path);
		int row=matrix.length;
		int col=matrix[0].length;
		int []result=KMeans.kCluster(matrix, clusternum);
		//int []centerid=KMeans.GetInitCluster();
		String filelist[]=fs.getFilelist();
		System.out.println("the init clustering center file is:");
		//for(int j=0;j<centerid.length;j++){
		//	System.out.println(filelist[centerid[j]]);
		//}
		System.out.println();
		for(int i=0;i<result.length;i++){
			System.out.println(filelist[i]+":"+(result[i]+1)) ;
		}
	}
	
	public static void main(String[] path){
		Cluster docCluster=new Cluster();
		docCluster.KCluster("cluster_doc",3);
		//doccluster.KCluster("doc", 3);
	}
}