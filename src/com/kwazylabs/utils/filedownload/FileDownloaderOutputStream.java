package com.kwazylabs.utils.filedownload;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileDownloaderOutputStream extends FileOutputStream
{
	FileInfo info;
	
	FileDownloaderOutputStream(FileInfo info) throws FileNotFoundException
	{
		super(info.file);
		this.info = info;
	}
}
