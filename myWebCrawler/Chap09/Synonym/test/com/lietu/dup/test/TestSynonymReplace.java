package com.lietu.dup.test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.lietu.dup.SynonymReplace;

public class TestSynonymReplace {
	private static String path = System.getProperty("user.dir") + "/NorthAmerica_importer.mdb";
	// Test program
	public static void main(String[] args) throws Exception 
	{
		testDatabase();
	}
	
	public static void replace() throws Exception
	{
		String result = null;
		String results = "";

		// long start = System.currentTimeMillis();
		// for(int i =0;i<10000;++i)
		result = SynonymReplace.replace("A.W.I INDUSTRIES(USA) INC ");
		// long end = System.currentTimeMillis();
		// System.out.println("the result:"+ (end - start) );
		results = result.replaceAll(" +", " ");
		System.out.println("the results:" + results);
          
		result = SynonymReplace.replace("A'' STRIKER ENTERPISE CO.,LTD");
		results = result.replaceAll(" +", " ");
		System.out.println("the result :" + results);
	}
	
	/**
	 * 查询数据
	 */
	public static void testDatabase()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try 
		{
			System.out.println(path);
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			conn = DriverManager.getConnection("jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ="+path);
			
			stmt = conn.createStatement();
			//where ID1 < 1000
			rs = stmt.executeQuery("select * from NorthAmerica_importer");
			//String tel = "";
			//String address = "";
			String name = "";
			System.out.println("数据加载中......");
			while (rs.next())
			{
			//long id = rs.getLong("ID");
//				if(id%1000==0)
//					System.out.println(id);

				// name = rs.getString("name");
				// tel = rs.getString("tel");
				// address = rs.getString("address");
				name = rs.getString("importer");
				//tel = rs.getString("电话");
				//address = rs.getString("地址");
				
				System.out.println(name);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
