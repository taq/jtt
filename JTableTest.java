package com.eustaquiorangel.jtabletest;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.reflect.Method;
import org.ho.yaml.*;

public class JTableTest {
   private Connection con = null;
   private String cfg;
   private String id;
   private Map con_info;
   private Map data;
   private ArrayList<String> fieldList;
	private Object fixture_filter_obj	= null;
	private String fixture_filter_meth	= null;

   public JTableTest(String cfg, String id) throws Exception {
      con = open(cfg,id);
   }

   public Connection open(String cfg, String id) throws Exception {
      if(con!=null)
         return con;
		if(cfg==null || id==null)
			return null;
      this.cfg = cfg;
      this.id  = id;

      File file = new File(cfg);
      if(!file.exists())
         throw new Exception("File "+cfg+" does not exists");

      data     = (Map) Yaml.load(file);
      con_info = (Map) data.get(id);
      if(con_info==null)
         throw new Exception("Id "+id+" not found.");

      String userName = (String) con_info.get("username");
      String password = (String) con_info.get("password");
      String url      = (String) con_info.get("url");
      Class.forName((String) con_info.get("driver")).newInstance ();
      Connection c = DriverManager.getConnection (url, userName, password);
      con = c;
      return c;
   }

   public void clean(String ... tables) throws Exception {
      if(con==null)
         return;
      Statement stmt = con.createStatement();
      String table = null;
      for(int i=0; i<tables.length; i++){
         table = tables[i];
         stmt.executeUpdate("delete from "+table+" where 1=1");
      }
      stmt.close();
   }

   protected Connection getConnection(){
      return this.con;
   }

   public Query table(String table){
      return new Query(this,table);
   }

   public void close() throws Exception {
      if(con==null)
         return;
      con.close();
   }

   private Object[] createInsertSQL(String table, Map row_val) {
      Object[] rtn   = {null,null};
      String sql     = "insert into "+table+" (";
      try {
         Iterator field_it = row_val.entrySet().iterator();
			fieldList = new ArrayList<String>();

         while(field_it.hasNext()){
            Map.Entry field_entry = (Map.Entry) field_it.next();
            sql = sql + ((String) field_entry.getKey()).toUpperCase() + ",";
            fieldList.add((String) field_entry.getKey());
         }
         sql = sql.substring(0,sql.length()-1);
         sql = sql + ") ";

         sql = sql + "values (";
         for(int i=0; i<row_val.size();i++){
            sql = sql + "?,";
         }
         sql = sql.substring(0,sql.length()-1);
         sql = sql + ")";

         rtn[0] = sql;
         rtn[1] = fieldList;
      } catch(Exception e) {
         System.err.println("createInsertSQL: "+e.getMessage());
      }
      return rtn;
   }

	public void fixture_filter(Object o, String m){
		fixture_filter_obj	= o;
		fixture_filter_meth	= m;
	}

	public Map fixture(String file) throws Exception {
		String[] ids = {};
		return fixture(file,ids);
	}

	@SuppressWarnings("unchecked") // damned generics ... sometimes do more harm than good
   public Map fixture(String file, String ... ids) throws Exception {
      PreparedStatement stmt = null;
      String sql = null, table = null;

      Set data_set;
      Iterator data_it, rows_it;
      String row_key, data_key;

      Map rows=null;
		Map row_val;
      Map.Entry row_entry;
      Object data_val;
      ArrayList fieldList = new ArrayList();

      String[] tokens;
      boolean first = true;
      int count, row_count;

		Method filter = null;

		Arrays.sort(ids);

		if(fixture_filter_obj!=null && fixture_filter_meth!=null)
			filter = fixture_filter_obj.getClass().getMethod(
					   fixture_filter_meth, new Class[] {Object.class});

      try {
         File f = new File(file);
         if(!f.exists()){
            file = "fixtures/"+file;
            f = new File(file);
            if(!f.exists())
               throw new Exception("Fixture file "+file+" does not exists");
         }

         // table name
         tokens   = file.split("\\/"); 
         tokens   = tokens[tokens.length-1].split("\\.yml");
         table    = tokens[0];

         // all rows
         rows     = (Map) Yaml.load(f); 
         rows_it  = rows.entrySet().iterator();

         while(rows_it.hasNext()){
            row_entry   = (Map.Entry) rows_it.next();
            row_key     = (String) row_entry.getKey();
            row_val     = (Map) row_entry.getValue();

            // first data row create the insert statement
            if(first){
               first = false;
               Object[] create   = createInsertSQL(table,row_val);
               sql               = (String) create[0];
               fieldList         = (ArrayList) create[1];
               if(sql==null)
                  throw new Exception("could not create SQL insert for "+file);
               stmt = con.prepareStatement(sql);
            }
				
				if(ids.length>0 && Arrays.binarySearch(ids,row_key)<0)
					continue;

            count = 1;
            data_it = fieldList.iterator();
            while(data_it.hasNext()){
               String field   = (String) data_it.next();
               data_val       = (Object) row_val.get(field);

					// if there is a filter ...
					if(filter!=null){
						data_val = (Object) filter.invoke(fixture_filter_obj,data_val);
						row_val.put(field,data_val);
					}

               stmt.setObject(count,data_val);
               count++;
            }
            row_count = stmt.executeUpdate();
            if(row_count<1)
               throw new Exception("erro inserindo valores para "+row_key+" em "+file);
         }
         stmt.close();
      }catch(Exception e){
         System.err.println("error loading fixture "+file+": "+e.getMessage()+" "+sql);
         e.printStackTrace();
      }
      return rows;
   }
}
