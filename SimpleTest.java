package com.eustaquiorangel.jtabletest;

import java.sql.*;
import java.util.*;

public class SimpleTest {

	public SimpleTest() throws Exception {
		JTableTest jt = new JTableTest("data.yml","sqlite");
		Query query;

		jt.clean("customers");
		Map customers = jt.fixture("customers.yml");
		String beatle = "john";
		Map john = (Map) customers.get(beatle);
		System.out.println("Beatle name is "+john.get("name"));

		query = jt.table("customers");
		System.out.println("there are "+query.count()+" Beatles");

		query = jt.table("customers").where("name like '%n'");
		System.out.println("there are "+query.count()+" Beatle(s) where 'n' is the last char on name");
		System.out.println("the first is "+query.column("name"));

		query = jt.table("customers").where("name like '%n'").order("name desc");
		System.out.println("but if we order descending the database the first is "+query.column("name"));

		// loading just some keys from the fixture file
		jt.clean("customers");
		customers = jt.fixture("customers.yml","john","paul");
		query = jt.table("customers");
		System.out.println("there are "+query.count()+" selected Beatles");
	}

	public static void main(String args[]){
		try {
			SimpleTest t = new SimpleTest();
		} catch(Exception e) {
			System.err.println("error:"+e.getMessage());
		}
	}
}
