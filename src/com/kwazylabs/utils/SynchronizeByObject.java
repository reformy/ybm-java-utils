package com.kwazylabs.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizeByObject
{
	private ConcurrentHashMap<Object, Lock> locks = new ConcurrentHashMap<Object, Lock>();
	
	public Lock getLock(Object key)
	{
		Lock res = locks.get(key);
		if (res != null)
			return res;
		
		Lock newLock = new ReentrantLock();
		Lock oldLock = locks.putIfAbsent(key, new ReentrantLock());
		
		return oldLock != null ? oldLock : newLock;
	}
	
	public void lock(Object key)
	{
		getLock(key).lock();
	}
	
	public void unlock(Object key)
	{
		getLock(key).unlock();
	}
}
