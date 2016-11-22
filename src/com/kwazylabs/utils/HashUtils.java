package com.kwazylabs.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class HashUtils
{
	public static String md5(String src) throws UtilsException
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(src.getBytes());
			String res = Hex.encodeHexString(digest.digest());
			return res;
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new UtilsException(e);
		}
	}
}
