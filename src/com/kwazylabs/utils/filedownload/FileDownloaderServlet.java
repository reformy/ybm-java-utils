package com.kwazylabs.utils.filedownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class FileDownloaderServlet extends HttpServlet
{
	public final static Logger logger = Logger
	    .getLogger(FileDownloaderServlet.class);
	
	private FileDownloader downloader;
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
		WebApplicationContext ctx = WebApplicationContextUtils
		    .getRequiredWebApplicationContext(getServletContext());
		
		String downloaderBeanName = getInitParameter("downloaderBeanName");
		if (StringUtils.isEmpty(downloaderBeanName))
			downloaderBeanName = "fileDownloader";
		
		downloader = (FileDownloader) ctx.getBean(downloaderBeanName);
		if (downloader == null)
		{
			throw new ServletException(
			    "FileDownloaderServlet must have a bean names \"fileDownloader\" which is a FileDownloader instance. "
			        + "One can also pass a different bean name using the \"downloaderBeanName\" init param of the servlet.");
		}
		
		logger.info("File Download servlet with bean \"" + downloaderBeanName
		    + "\" is ready.");
	}
	
	@Override
	protected void service(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException
	{
		logger.trace("Got request.");
		
		try
		{
			String path = request.getPathInfo();
			if (path == null || path.length() < 2)
			{
				logger.trace("err: No path: " + path);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			String filename = path.substring(1);
			File file = downloader.getFile(filename);
			if (file == null)
			{
				logger.trace("err: No file for: " + filename);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			logger.trace("Returning file: " + filename);
			
			response.setHeader("Content-disposition",
			    "attachment; filename=" + file.getName());
			
			OutputStream out = response.getOutputStream();
			FileInputStream in = new FileInputStream(file);
			try
			{
				byte[] buffer = new byte[4096];
				int length;
				while ((length = in.read(buffer)) > -1)
				{
					out.write(buffer, 0, length);
				}
			}
			finally
			{
				in.close();
			}
		}
		catch (Exception e)
		{
			logger.error("Failed.", e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
