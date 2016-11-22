package com.kwazylabs.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils
{
	
	public static long toMillis(int time, TimeUnit timeUnit)
	{
		switch (timeUnit)
		{
			case NANOSECONDS:
				return time / 1000000;
			case MICROSECONDS:
				return time / 1000;
			case MILLISECONDS:
				return time;
			case SECONDS:
				return time * 1000;
			case MINUTES:
				return time * 1000 * 60;
			case HOURS:
				return time * 1000 * 60 * 60;
			case DAYS:
				return time * 1000 * 60 * 60 * 24;
				
			default:
				throw new RuntimeException("Unknown time unit: " + timeUnit);
		}
	}
}
