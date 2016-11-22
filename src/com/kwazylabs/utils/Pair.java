package com.kwazylabs.utils;

import java.io.Serializable;

public class Pair<C1 extends Serializable, C2 extends Serializable> implements
    Serializable
{
	public C1 m1;
	public C2 m2;
	
	public Pair(C1 m1, C2 m2)
	{
		this.m1 = m1;
		this.m2 = m2;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Pair))
			return false;
		
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (m1 == null && other.m1 == null || m1 != null && m1.equals(other.m1))
		{
			// Passed m1 check.
			if (m2 == null && other.m2 == null || m2 != null && m2.equals(other.m2))
			{
				// Passed m2 check.
				return true;
			}
		}
		
		return false;
	}
}
