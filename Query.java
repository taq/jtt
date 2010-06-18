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

   private String createQuery(){
      String sql = "select count(*) as jtabletestcount, a.* from "+this.table+" a ";
      if(where!=null)
         sql = sql + " where "+where;
      if(order!=null)
         sql = sql + " order by "+order;
      if(group!=null)
         sql = sql + " group by "+group;
      return sql;
   }

   private boolean execute(){
      try {
         Connection con = this.test.getConnection();
         String sql     = createQuery();
			String cname	= null;
         stmt				= con.createStatement();

         rst	= stmt.executeQuery(sql);
			meta	= rst.getMetaData();
			count	= meta.getColumnCount();

         if(!rst.next()){
				stmt.close();
				rst.close();
				return false;
			}
			data = new HashMap<String,Object>();

         count = rst.getInt("jtabletestcount");
			for(int i=0; i<count; i++){
				cname = meta.getColumnName(i+1);
				data.put(cname,rst.getObject(cname));
			}
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
