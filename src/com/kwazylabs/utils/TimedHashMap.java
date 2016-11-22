package com.kwazylabs.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedHashMap<K, V> implements Map<K, V>, Runnable
{
	private static final long CLEANUP_DELAY = 1000 * 60 * 60;
	private static ScheduledExecutorService scheduler = Executors
	    .newSingleThreadScheduledExecutor(new DaemonThreadFactory("timedHashMap"));
	
	private Map<K, TimedObject> inner = Collections
	    .synchronizedMap(new HashMap<K, TimedObject>());
	private final long lifetime;
	
	public TimedHashMap(long lifetime)
	{
		this.lifetime = lifetime;
		scheduler.scheduleWithFixedDelay(this, CLEANUP_DELAY, CLEANUP_DELAY,
		    TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void run()
	{
		synchronized (inner)
		{
			validateAll();
		}
	}
	
	private void validateAll()
	{
		for (K k : inner.keySet())
		{
			validate(k);
		}
	}
	
	private class TimedObject
	{
		V o;
		Date insertionDate;
		
		TimedObject(V o)
		{
			this(o, new Date());
		}
		
		TimedObject(V o, Date insertionDate)
		{
			this.o = o;
			this.insertionDate = insertionDate;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object arg0)
		{
			if (arg0 instanceof TimedHashMap.TimedObject)
			{
				TimedObject other = (TimedObject) arg0;
				return o.equals(other.o);
			}
			return false;
		}
	}
	
	@Override
	public void clear()
	{
		inner.clear();
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		synchronized (inner)
		{
			validate(key);
			return inner.containsKey(key);
		}
	}
	
	private void validate(Object key)
	{
		TimedObject timedObject = inner.get(key);
		if (timedObject != null)
		{
			if (isExpired(timedObject))
			{
				inner.remove(key);
			}
		}
	}
	
	private boolean isExpired(TimedObject timedObject)
	{
		return System.currentTimeMillis() - timedObject.insertionDate.getTime() > lifetime;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public V get(Object key)
	{
		synchronized (inner)
		{
			TimedObject timedObject = inner.get(key);
			if (timedObject != null)
			{
				if (isExpired(timedObject))
				{
					inner.remove(key);
					return null;
				}
				return timedObject.o;
			}
			return null;
		}
	}
	
	@Override
	public boolean isEmpty()
	{
		synchronized (inner)
		{
			validateAll();
			return inner.isEmpty();
		}
	}
	
	@Override
	public Set<K> keySet()
	{
		synchronized (inner)
		{
			validateAll();
			return inner.keySet();
		}
	}
	
	@Override
	public V put(K key, V value)
	{
		TimedObject oldObject = inner.put(key, new TimedObject(value));
		if (oldObject != null && !isExpired(oldObject))
		{
			return oldObject.o;
		}
		return null;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public V remove(Object key)
	{
		TimedObject oldObject = inner.remove(key);
		if (oldObject != null && !isExpired(oldObject))
		{
			return oldObject.o;
		}
		return null;
	}
	
	@Override
	public int size()
	{
		synchronized (inner)
		{
			validateAll();
			return inner.size();
		}
	}
	
	@Override
	public Collection<V> values()
	{
		throw new UnsupportedOperationException();
	}
}
