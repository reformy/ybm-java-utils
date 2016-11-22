package com.kwazylabs.utils.persistence;

import com.kwazylabs.utils.UtilsException;

public class PersistenceException extends UtilsException
{
	
	public PersistenceException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}
	
	public PersistenceException(String arg0)
	{
		super(arg0);
	}
	
	public PersistenceException(Throwable arg0)
	{
		super(arg0);
	}
}
