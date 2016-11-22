package com.kwazylabs.utils;

import java.text.DecimalFormat;

public class FormatUtils
{
	private static ThreadLocal<DecimalFormat> tDecimalFormat = new ThreadLocal<DecimalFormat>()
	{
		@Override
		protected DecimalFormat initialValue()
		{
			return new DecimalFormat("#.00");
		}
	};
	
	public static String formatDoubleWith2Digits(double d)
	{
		return tDecimalFormat.get().format(d);
	}
}
