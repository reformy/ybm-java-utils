package com.kwazylabs.utils;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class EmailSSLSender
{
	private Session session;
	private String defaultFromAddress;
	
	public EmailSSLSender(String host, final String user, final String password)
	{
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtp.socketFactory.class",
		    "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", "465");
		session = Session.getDefaultInstance(props, new javax.mail.Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(user, password);
			}
		});
	}
	
	public void setDefaultFromAddress(String defaultFromAddress)
	{
		this.defaultFromAddress = defaultFromAddress;
	}
	
	public void sendEmail(String subject, String content, String from,
	    String addresses) throws UtilsException
	{
		EmailSender.privateSendEmail(session, subject, content, from, addresses);
	}
	
	public void sendEmail(String subject, String content, String addresses)
	    throws UtilsException
	{
		if (defaultFromAddress == null)
			throw new UtilsException("No default address defined! "
			    + "Please call setDefaultFromAddress() or use other sendMail method.");
		EmailSender.privateSendEmail(session, subject, content, defaultFromAddress,
		    addresses);
	}
}
