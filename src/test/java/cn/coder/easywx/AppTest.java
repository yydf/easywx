package cn.coder.easywx;

import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	public static void main(String[] args) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("ss", 123);
		System.out.println(map.remove("ss"));
		System.out.println(map.size());
	}
}
