package com.kwazylabs.utils;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender
{
	private Session session;
	
	public EmailSender(String host, String user, String password)
	{
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", host);
		props.setProperty("mail.smtp.auth", "true");
		
		props.put("mail.smtp.socketFacry.port", "465");
		props
		    .put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.port", "465");
		
		Authenticator auth = new SMTPAuthenticator(user, password);
		session = Session.getDefaultInstance(props, auth);
		// session.setDebug(true);
	}
	
	public void sendEmail(String subject, String content, String from,
	    String addresses, File... attachments) throws UtilsException
	{
		privateSendEmail(session, subject, content, from, addresses, attachments);
	}
	
	static void privateSendEmail(Session session, String subject, String content,
	    String from, String addresses, File... attachments) throws UtilsException
	{
		Transport transport = null;
		try
		{
			transport = session.getTransport();
			MimeMessage message = new MimeMessage(session);
			message.setSubject(subject);
			Multipart multipart = new MimeMultipart();
			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(content, "text/html; charset=utf-8");
			multipart.addBodyPart(bodyPart);
			for (File f : attachments)
			{
				bodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(f);
				bodyPart.setDataHandler(new DataHandler(source));
				bodyPart.setFileName(f.getName());
				multipart.addBodyPart(bodyPart);
			}
			message.setContent(multipart);
			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setFrom(new InternetAddress(from));
			
			transport.connect();
			transport.sendMessage(message,
			    message.getRecipients(Message.RecipientType.TO));
			transport.close();
			
		}
		catch (MessagingException e)
		{
			throw new UtilsException("Failed sending email.", e);
		}
		finally
		{
			if (transport != null)
			{
				try
				{
					transport.close();
				}
				catch (MessagingException e)
				{
					throw new UtilsException("Failed closing transport.", e);
				}
			}
		}
	}
	
	// public static void main(String[] args) throws Exception
	// {
	// EmailSender sender = new EmailSender("mail.kwazi.co.il",
	// "notify@kwazi.co.il", "alibaliba");
	// sender.sendEmail("this is a test mail", "so plz don't delete it.",
	// "notify@kwazi.co.il", "reformy@gmail.com, daled@kwazi.co.il");
	//
	// System.out.println("Done.");
	// }
	
	private class SMTPAuthenticator extends javax.mail.Authenticator
	{
		String username;
		String password;
		
		SMTPAuthenticator(String username, String password)
		{
			this.username = username;
			this.password = password;
		}
		
		@Override
		public PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(username, password);
		}
	}
}
