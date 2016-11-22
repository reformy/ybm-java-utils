package com.kwazylabs.utils.filedownload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;

import com.kwazylabs.utils.DaemonThreadFactory;
import com.kwazylabs.utils.TimeUtils;
import com.kwazylabs.utils.UtilsException;

public class FileDownloader implements Runnable
{
	public final static Logger logger = Logger.getLogger(FileDownloader.class);
	
	private File workDir;
	private String servletUrl;
	
	private Map<String, FileInfo> files = new HashMap<String, FileInfo>();
	private ScheduledExecutorService cleanerService;
	
	private Lock lock = new ReentrantLock();
	
	public FileDownloader(File workDir, String servletUrl) throws UtilsException
	{
		this.workDir = workDir;
		this.servletUrl = servletUrl;
		
		if (workDir.exists() && !workDir.isDirectory())
			throw new UtilsException("Work dir is a file.");
		
		workDir.mkdirs();
		
		cleanerService = Executors
		    .newSingleThreadScheduledExecutor(new DaemonThreadFactory(
		        "FileDownloaderCleaner"));
		cleanerService.scheduleWithFixedDelay(this, 1, 1, TimeUnit.HOURS);
		
		logger
		    .info("File Downloader ready, work dir: " + workDir.getAbsolutePath());
	}
	
	/**
	 * Prepare a file output stream to write to. After filling this stream, one
	 * should call the
	 * 
	 * @param fileName
	 * @param time
	 * @param timeUnit
	 * @return
	 * @throws UtilsException
	 * @throws IOException
	 */
	public FileDownloaderOutputStream openStream(String filename, int time,
	    TimeUnit timeUnit) throws UtilsException
	{
		try
		{
			FileDownloaderOutputStream res = new FileDownloaderOutputStream(
			    prepareFileInfo(prepareFile(filename), time, timeUnit));
			
			logger.trace("Opened stream for: " + filename);
			return res;
		}
		catch (FileNotFoundException e)
		{
			throw new UtilsException("Failed opening file stream.", e);
		}
	}
	
	public String closeAndPrepareDownloadLink(FileDownloaderOutputStream fdos)
	    throws UtilsException
	{
		try
		{
			fdos.close();
			logger.trace("Closed stream for: " + fdos.info.file.getName());
		}
		catch (IOException e)
		{
			throw new UtilsException("Failed closing file stream.", e);
		}
		
		return prepareDownloadLink(fdos.info.file);
	}
	
	private File prepareFile(String filename)
	{
		File dest = new File(workDir, filename);
		if (dest.exists())
			dest.delete();
		return dest;
	}
	
	private FileInfo prepareFileInfo(File dest, int time, TimeUnit timeUnit)
	{
		FileInfo info = new FileInfo();
		info.file = dest;
		info.expirationTime = System.currentTimeMillis()
		    + TimeUtils.toMillis(time, timeUnit);
		lock.lock();
		try
		{
			files.put(dest.getName(), info);
		}
		finally
		{
			lock.unlock();
		}
		return info;
	}
	
	private String prepareDownloadLink(File dest)
	{
		String res = servletUrl + '/' + dest.getName();
		logger.trace("Download link: " + res);
		return res;
	}
	
	public String copyAndPrepareDownloadLink(File src, int time, TimeUnit timeUnit)
	    throws UtilsException
	{
		try
		{
			File dest = prepareFile(src.getName());
			try
			{
				FileUtils.copyFile(src, dest);
			}
			catch (IOException e)
			{
				throw new UtilsException("Failed copying file to: "
				    + dest.getCanonicalPath(), e);
			}
			
			prepareFileInfo(dest, time, timeUnit);
			
			logger.trace("Copied file " + src.getAbsolutePath() + ".");
			
			return prepareDownloadLink(dest);
		}
		catch (IOException ioe)
		{
			throw new UtilsException("Failed.", ioe);
		}
	}
	
	private static PrettyTime prettyTime = new PrettyTime();
	
	public File getFile(String filename)
	{
		lock.lock();
		try
		{
			logger.trace("Looking for file: " + filename);
			FileInfo fileInfo = files.get(filename);
			if (fileInfo == null)
			{
				logger.trace("No such file.");
				return null;
			}
			if (fileInfo.expirationTime < System.currentTimeMillis())
			{
				logger.trace("File expired "
				    + prettyTime.format(new Date(fileInfo.expirationTime)) + ".");
				removeFile(fileInfo);
				return null;
			}
			
			return fileInfo.file;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private void removeFile(FileInfo fileInfo)
	{
		files.remove(fileInfo);
		fileInfo.file.delete();
	}
	
	@Override
	public void run()
	{
		logger.trace("FileDownloader cleaner running, map has " + files.size()
		    + " entries.");
		lock.lock();
		try
		{
			Map<String, FileInfo> newMap = new HashMap<String, FileInfo>();
			for (Entry<String, FileInfo> e : files.entrySet())
			{
				if (e.getValue().expirationTime >= System.currentTimeMillis())
					newMap.put(e.getKey(), e.getValue());
				else
					e.getValue().file.delete();
			}
			files = newMap;
			logger.trace("FileDownloader cleaner done, new map has " + files.size()
			    + " entries.");
		}
		catch (Throwable t)
		{
			logger.error("FileDownloader cleaner failed.", t);
		}
		finally
		{
			lock.unlock();
		}
	}
}
