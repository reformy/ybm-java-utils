package com.kwazylabs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StdinUtils
{
	private static BufferedReader reader = new BufferedReader(
	    new InputStreamReader(System.in));
	
	public static String readLine() throws IOException
	{
		return reader.readLine();
	}
	
	public static int readInt() throws IOException
	{
		String line = reader.readLine();
		if (line.length() == 0)
			return Integer.MAX_VALUE;
		return Integer.parseInt(line);
	}
}
