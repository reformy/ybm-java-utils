package com.kwazylabs.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class ImageUtils
{
	public static BufferedImage readImage(File file) throws UtilsException
	{
		try
		{
			BufferedImage img = null;
			ImageInputStream iis = new FileImageInputStream(file);
			try
			{
				for (Iterator<ImageReader> i = ImageIO.getImageReaders(iis); img == null
				    && i.hasNext();)
				{
					ImageReader r = i.next();
					try
					{
						r.setInput(iis);
						img = r.read(0);
					}
					catch (IOException e)
					{
						// ignore.
					}
				}
			}
			finally
			{
				iis.close();
			}
			return img;
		}
		catch (Exception e)
		{
			throw new UtilsException("Failed reading image file.", e);
		}
	}
}
