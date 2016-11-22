package com.kwazylabs.utils;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class MobileStoreUtils
{
	public static String getAppStoreUrlFromBundleId(String bundleId)
	    throws UtilsException
	{
		try
		{
			URL url = new URL("https://itunes.apple.com/lookup?bundleId=" + bundleId);
			String res = IOUtils.toString(url.openConnection().getInputStream());
			int i = res.indexOf("\"trackId\"");
			if (i < 0)
				throw new UtilsException("Unknown bundleId: " + bundleId);
			int iEnd = res.indexOf(',', i);
			String trackId = res.substring(i + 10, iEnd);
			return "https://itunes.apple.com/us/app/id" + trackId;
		}
		catch (IOException e)
		{
			throw new UtilsException(e);
		}
	}
}
