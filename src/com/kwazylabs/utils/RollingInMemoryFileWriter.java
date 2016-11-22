package com.kwazylabs.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

/**
 * Logger file - keeps line in memory up to some limit, then dumps to file.
 * Rolls the file after another limit.
 * 
 * @author yair
 *
 */
public class RollingInMemoryFileWriter
{
	public final static Logger logger = Logger
	    .getLogger(RollingInMemoryFileWriter.class);
	
	private final long bufferMaxSize;
	private final long fileMaxSize;
	private final boolean addTimestamp;
	private final boolean startWithOne;
	private final int maxFiles;
	
	private File logFile;
	private String logFilepath;
	private FileWriter innerWriter;
	private long currentFileSize;
	private int currentFileIndex = 0;
	
	private StringBuffer buffer;
	private ReadWriteLock bufferLock = new ReentrantReadWriteLock();
	private Lock bufferAppendLock = new ReentrantLock();
	
	public static void main(String[] args) throws Exception
	{
		RollingInMemoryFileWriter w = new RollingInMemoryFileWriter(
		    "/home/yair/yair.csv", false, false, 10, 100, 0);
		for (int j = 1000000; j < 1001000; j += 100)
		{
			for (int i = 0; i < 100; i++)
			{
				w.println("" + (i + j));
			}
			System.out.println(j);
			w.switchFile("/home/yair/yair"+j+".csv");
		}
		System.out.println("done");
	}
	
	private static DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	public RollingInMemoryFileWriter(String logFilepath, boolean addTimestamp,
	    boolean startWithOne, long bufferMaxSize, long fileMaxSize, int maxFiles)
	    throws IOException
	{
		this.addTimestamp = addTimestamp;
		this.startWithOne = startWithOne;
		this.maxFiles = maxFiles;
		
		if (addTimestamp)
		{
			this.logFilepath = addTimestamp(logFilepath);
		}
		else
			this.logFilepath = logFilepath;
		
		buffer = new StringBuffer();
		createNewWriter();
		this.bufferMaxSize = bufferMaxSize;
		this.fileMaxSize = fileMaxSize;
	}
	
	private String addTimestamp(String filepath)
	{
		int iDot = logFilepath.lastIndexOf('.');
		int iSlash = logFilepath.lastIndexOf('/');
		int iTimestamp = logFilepath.length();
		if (iDot > -1)
		{
			if (iSlash < iDot)
				iTimestamp = iDot;
		}
		
		return logFilepath.substring(0, iTimestamp) + "." + df.format(new Date())
		    + logFilepath.substring(iTimestamp);
	}
	
	private void createNewWriter() throws IOException
	{
		int i = ++currentFileIndex;
		if (maxFiles > 0 && i >= maxFiles)
		{
			// Don't start a new one.
			buffer = null;
			return;
		}
		logFile = new File(logFilepath + (startWithOne ? ("." + (i)) : ""));
		innerWriter = new FileWriter(logFile);
		currentFileSize = 0;
		
		logger.trace("New log file: " + logFile.getAbsolutePath());
	}
	
	public void println(String line)
	{
		if (buffer == null)
			// Closed!
			return;
		
		bufferLock.readLock().lock();
		try
		{
			if (buffer == null)
				// Closed!
				return;
			
			bufferAppendLock.lock();
			try
			{
				buffer.append(line).append('\n');
			}
			finally
			{
				bufferAppendLock.unlock();
			}
		}
		finally
		{
			bufferLock.readLock().unlock();
		}
		checkAndRoll();
	}
	
	/**
	 * @param logFilepath
	 * @return old log filepath.
	 * @throws IOException
	 */
	public String switchFile(String logFilepath) throws IOException
	{
		bufferLock.writeLock().lock();
		try
		{
			innerWriter.close();
			String oldLogFilepath = this.logFilepath;
			if (addTimestamp)
			{
				this.logFilepath = addTimestamp(logFilepath);
			}
			else
				this.logFilepath = logFilepath;
			
			logger.debug("Switching file:\nOld: " + oldLogFilepath + "\nnew: "
			    + this.logFilepath);
			
			currentFileIndex = 0;
			
			createNewWriter();
			
			return oldLogFilepath;
		}
		finally
		{
			bufferLock.writeLock().unlock();
		}
	}
	
	private void checkAndRoll()
	{
		if (buffer.length() >= bufferMaxSize)
		{
			bufferLock.writeLock().lock();
			try
			{
				if (buffer.length() >= bufferMaxSize)
				{
					innerWriter.write(buffer.toString());
					innerWriter.flush();
					currentFileSize += buffer.length();
					buffer = new StringBuffer();
					
					if (currentFileSize >= fileMaxSize)
					{
						// Roll file.
						innerWriter.close();
						
						if (!startWithOne)
						{
							// Find next index to roll.
							int index = 0;
							File newFile;
							do
							{
								newFile = new File(logFile.getParentFile(), logFile.getName()
								    + '.' + (++index));
							} while (newFile.exists());
							
							logFile.renameTo(newFile);
						}
						
						createNewWriter();
					}
				}
			}
			catch (IOException ioe)
			{
				logger.error(ioe);
			}
			finally
			{
				bufferLock.writeLock().unlock();
			}
		}
	}
	
	public void close() throws IOException
	{
		bufferLock.writeLock().lock();
		try
		{
			innerWriter.write(buffer.toString());
			buffer = null;
			innerWriter.close();
		}
		finally
		{
			bufferLock.writeLock().unlock();
		}
	}
}
