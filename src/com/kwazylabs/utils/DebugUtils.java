package com.kwazylabs.utils;

import java.util.Map;

public class DebugUtils
{
	public static <A, B> void printMap(String name, Map<A, B> map)
	{
		System.out.println("-------------- Start: " + name + " -------------");
		for (A a : map.keySet())
		{
			System.out.println("\t'" + a + "' --> '" + map.get(a) + "'");
		}
		System.out.println("-------------- End -------------");
		
	}
}
