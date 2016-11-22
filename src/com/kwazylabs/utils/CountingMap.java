package com.kwazylabs.utils;

import java.util.HashMap;

public class CountingMap<T> extends HashMap<T, Integer>
{
	public void count(T t)
	{
		Integer i = get(t);
		if (i == null)
			i = 1;
		else
			i = i + 1;
		put(t, i);
	}
	
	public int getCount(T t)
	{
		return get(t) != null ? get(t) : 0;
	}
	
	@Override
	public String toString()
	{
		StringBuffer res = new StringBuffer();
		for (java.util.Map.Entry<T, Integer> e : entrySet())
		{
			res.append(e.getKey() + " : " + e.getValue() + "\n");
		}
		
		return res.toString();
	}
}
