package com.kwazylabs.utils;

public class UtilsException extends Exception
{
	public UtilsException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public UtilsException(String message)
	{
		super(message);
	}
	
	public UtilsException(Throwable cause)
	{
		super(cause);
	}
}
