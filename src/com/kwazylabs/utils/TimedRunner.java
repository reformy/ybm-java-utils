package com.kwazylabs.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class TimedRunner
{
	public static AtomicInteger counter = new AtomicInteger();
	
	public static <V> V submit(Callable<V> c, long time, TimeUnit timeUnit)
	    throws InterruptedException, ExecutionException, TimeoutException
	{
		ExecutorService executor = Executors
		    .newSingleThreadExecutor(new DaemonThreadFactory("TimerRunner"
		        + counter.incrementAndGet()));
		Future<V> fv = executor.submit(c);
		executor.shutdown();
		V v = fv.get(time, timeUnit);
		return v;
	}
}
