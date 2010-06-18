package com.eustaquiorangel.jtabletest;

import java.sql.*;
import java.util.*;
import junit.framework.*;

public class SimpleJunitTest extends TestCase {

	public void testBeatles() throws Exception {
		JTableTest jt = new JTableTest("data.yml","sqlite");
		Query query;

		jt.clean("customers");

		// test fixture
		Map customers = jt.fixture("customers.yml");
		String beatle = "john";
		Map john = (Map) customers.get(beatle);
		assertEquals(john.get("name"),"John Lennon");

		// test row count
		query = jt.table("customers");
		assertTrue(query.count()==4);

		// test row count with conditions
		query = jt.table("customers").where("name like '%n'");
		assertTrue(query.count()==2);

		// test row count with conditions and order
		query = jt.table("customers").where("name like '%n'").order("name desc");
		assertEquals(john.get("name"),john.get("name"));
	}
}

