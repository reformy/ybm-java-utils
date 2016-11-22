package com.kwazylabs.utils;

public class OSUtils
{
	public static boolean areWeOnWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}
}
