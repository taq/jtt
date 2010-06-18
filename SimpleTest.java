package com.eustaquiorangel.jtabletest;

public class SimpleTest {
	public SimpleTest() throws Exception {
		JTableTest jt = new JTableTest(null,null);
	}

	public static void main(String args[]){
		try {
			SimpleTest t = new SimpleTest();
		} catch(Exception e) {
			System.err.println("error:"+e.getMessage());
		}
	}
}
