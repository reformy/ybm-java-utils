package com.kwazylabs.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils
{
	public static <T> T chooseFrom(List<T> list)
	{
		return list.get(ThreadLocalRandom.current().nextInt(list.size()));
	}
}
