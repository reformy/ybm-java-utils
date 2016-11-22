package com.kwazylabs.utils.webserver;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Basic servlet that gets action to perform. The servlet looks for a parameter
 * "a" and looks for a method with this "action" name. The method must receive
 * the request and the response as parameters. It may return void to indicate
 * nothing should happen after it is invoked, or String to indicate a forward
 * url to use.
 * 
 * @author yair
 *
 */
public class BasicManagerServlet extends HttpServlet
{
	public final static Logger logger = Logger
	    .getLogger(BasicManagerServlet.class);
	
	private String defaultAction;
	
	protected void service(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException
	{
		request.setCharacterEncoding("utf-8");
		
		String action = request.getParameter("a");
		if (StringUtils.isEmpty(action))
		{
			action = defaultAction;
			if (StringUtils.isEmpty(action))
				throw new ServletException("No action.");
		}
		
		try
		{
			Method method = this.getClass().getMethod(action,
			    HttpServletRequest.class, HttpServletResponse.class);
			
			Class<?> returnType = method.getReturnType();
			if (returnType != Void.TYPE && returnType != String.class)
			{
				throw new ServletException("Bad return type: " + returnType);
			}
			String forwardUrl = (String) method.invoke(this, request, response);
			if (returnType == Void.TYPE || StringUtils.isEmpty(forwardUrl))
				return;
			
			request.getRequestDispatcher(forwardUrl).forward(request, response);
		}
		catch (NoSuchMethodException e)
		{
			throw new ServletException("Unknown action: " + action);
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}
	
	protected void setDefaultAction(String defaultAction)
	{
		this.defaultAction = defaultAction;
	}
}
