package com.kwazylabs.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UserAgentUtils
{
	public enum MobileType
	{
		ANDROID,
		IPHONE,
		UNKNOWN;
	}
	
	public static MobileType getMobileTypeFor(String ua)
	{
		if (ua.contains("(Linux;") && ua.contains("Android")
		    || ua.contains("(Android"))
			return MobileType.ANDROID;
		if (ua.contains("CPU iPhone") || ua.contains("(iPhone;")
		    || ua.contains("(iPod;"))
			return MobileType.IPHONE;
		if (ua.contains("%2F"))
			try
			{
				return getMobileTypeFor(URLDecoder.decode(ua, "UTF8"));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(e);
			}
		return MobileType.UNKNOWN;
	}
}
