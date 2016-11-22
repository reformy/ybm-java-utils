package com.kwazylabs.utils;

import java.lang.reflect.ParameterizedType;

public class ClassUtils
{
	/**
	 * Get the real classes of a generic class.
	 * 
	 * @param clazz
	 * @return
	 */
	public static ParameterizedType getParameterizedType(Class<?> clazz)
	{
		if (clazz.getGenericSuperclass() instanceof ParameterizedType)
		{
			return (ParameterizedType) clazz.getGenericSuperclass();
		}
		return getParameterizedType(clazz.getSuperclass());
	}
	
	public static String humanizeClassName(Class<?> clazz)
	{
		return clazz.getSimpleName().replaceAll(
		    String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
		        "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}
}
