package com.eustaquiorangel.jtabletest;

import java.sql.*;
import java.util.*;

public class Query {
   JTableTest test;

   String table		= null;
   String where		= null;
   String order		= null;
   String group		= null;
   int count			= -1;
	Statement stmt		= null;
   ResultSet rst		= null;
   boolean executed	= false;

	HashMap<String,Object> data = null;
	ResultSetMetaData meta;

   public Query(JTableTest test, String table){
      this.test   = test;
      this.table  = table;
   }

   public Query where(String where){
      this.where = where;
      return this;
   }

   public Query order(String order){
      this.order = order;
      return this;
   }

   private String createQuery(){
      String sql = "select * from "+this.table+" a ";
      if(where!=null)
         sql = sql + " where "+where;
      if(order!=null)
         sql = sql + " order by "+order;
      if(group!=null)
         sql = sql + " group by "+group;
      return sql;
   }

	private String createCount(){
		return createQuery().replace("*","count(*) as count");
	}

   private boolean execute(){
      try {
         Connection con = this.test.getConnection();
         String sql     = createQuery();
			String csql		= createCount();
			String cname	= null;
			int ccnt			= -1;
         stmt				= con.createStatement();

			// get data
         rst	= stmt.executeQuery(sql);
			meta	= rst.getMetaData();
			ccnt	= meta.getColumnCount();

         if(rst.next()){
			   data = new HashMap<String,Object>();

			   for(int i=0; i<ccnt; i++){
				   cname = meta.getColumnName(i+1);
				   data.put(cname,rst.getObject(cname));
			   }
         }
			rst.close();

			// get the count - hey, cheaper and better than iteract on 
			// non-scrollable resultsets!
			rst = stmt.executeQuery(csql);
			if(!rst.next()){
				stmt.close();
				rst.close();
				return false;
			}
			count = rst.getInt("count");

			stmt.close();
			rst.close();
         executed = true; 
      } catch(Exception e) {
         executed = false;
      }
      return executed;
   }

   public int count(){
      if(!executed)
         execute();
      return count;
   }

   public Object column(String column) throws Exception {
      if(!executed)
         execute();
		if(data==null)
			return null;
      return data.get(column);
   }
}
