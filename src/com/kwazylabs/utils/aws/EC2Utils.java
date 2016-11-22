package com.kwazylabs.utils.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.kwazylabs.utils.UtilsException;

public class EC2Utils
{
	public final static Logger logger = Logger.getLogger(EC2Utils.class);
	
	private static EC2Utils instance;
	
	private AmazonElasticLoadBalancingClient elb;
	private AmazonEC2Client ec2;
	
	public EC2Utils(AWSCredentials credentials)
	{
		elb = new AmazonElasticLoadBalancingClient(credentials);
		ec2 = new AmazonEC2Client(credentials);
		instance = this;
	}
	
	public static String getMyPublicIp() throws IOException
	{
		URL url = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");
		BufferedReader r = null;
		try
		{
			r = new BufferedReader(new InputStreamReader(url.openConnection()
			    .getInputStream()));
			String res = r.readLine();
			return res;
		}
		finally
		{
			if (r != null)
				r.close();
		}
	}
	
	public static String getMyInstanceId() throws IOException
	{
		URL url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
		BufferedReader r = null;
		try
		{
			r = new BufferedReader(new InputStreamReader(url.openConnection()
			    .getInputStream()));
			String res = r.readLine();
			return res;
		}
		finally
		{
			if (r != null)
				r.close();
		}
	}
	
	public List<String> getLoadBalancerInstancesIps(String elbName,
	    boolean privateIps) throws UtilsException
	{
		DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest()
		    .withLoadBalancerNames(elbName);
		DescribeLoadBalancersResult describeLoadBalancers = instance.elb
		    .describeLoadBalancers(describeLoadBalancersRequest);
		if (describeLoadBalancers.getLoadBalancerDescriptions().size() == 0)
			return null;
		if (describeLoadBalancers.getLoadBalancerDescriptions().size() > 1)
			throw new UtilsException("More than 1 ELB for name \"" + elbName + "\": "
			    + describeLoadBalancers.getLoadBalancerDescriptions());
		LoadBalancerDescription lbd = describeLoadBalancers
		    .getLoadBalancerDescriptions().get(0);
		List<String> insIds = new ArrayList<String>();
		logger.debug("Found ELB: " + lbd);
		List<String> res = new ArrayList<String>();
		if (lbd.getInstances().size() > 0)
		{
			for (Instance i : lbd.getInstances())
			{
				logger.debug("---> instance: " + i.getInstanceId());
				insIds.add(i.getInstanceId());
			}
			DescribeInstancesRequest intReq = new DescribeInstancesRequest()
			    .withInstanceIds(insIds);
			DescribeInstancesResult describeInstances = instance.ec2
			    .describeInstances(intReq);
			for (Reservation r : describeInstances.getReservations())
			{
				for (com.amazonaws.services.ec2.model.Instance i : r.getInstances())
				{
					logger.debug("Describe instance: " + i.getInstanceId());
					res.add(privateIps ? i.getPrivateIpAddress() : i.getPublicIpAddress());
				}
			}
		}
		else
		{
			logger.debug("No instances connected to this ELB.");
		}
		
		return res;
	}
}
