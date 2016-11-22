package com.kwazylabs.utils.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class AwsEmail
{
	private AmazonSimpleEmailServiceClient emailService;
	
	private AWSCredentials awsCredentials;
	
	public AwsEmail(AWSCredentials awsCredentials, Regions regions)
	{
		emailService = new AmazonSimpleEmailServiceClient(awsCredentials);
		if (regions != null)
			emailService.setRegion(regions);
		
		this.awsCredentials = awsCredentials;
	}
	
	public AWSCredentials getAwsCredentials()
	{
		return awsCredentials;
	}
	
	public void sendEmail(String title, String content, String source,
	    String[] addresses)
	{
		synchronized (emailService)
		{
			Content subject = new Content().withData(title);
			
			Content textBody = new Content().withData(content);
			Body body = new Body().withText(textBody);
			
			// Create a message with the specified subject and body.
			Message message = new Message().withSubject(subject).withBody(body);
			
			// Assemble the email.
			SendEmailRequest request = new SendEmailRequest().withSource(source)
			    .withDestination(new Destination().withToAddresses(addresses))
			    .withMessage(message);
			
			emailService.sendEmail(request);
		}
	}
	
}
