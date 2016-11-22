package com.kwazylabs.utils.api.facebook;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kwazylabs.utils.UtilsException;

public class FacebookGraphClient
{
	public final static Logger logger = Logger
	    .getLogger(FacebookGraphClient.class);
	
	private static final String BASE_URL = "https://graph.facebook.com/";
	
	private HttpClient client;
	private String facebookAppId;
	
	public FacebookGraphClient(String facebookAppId)
	{
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(30000);
		requestBuilder = requestBuilder.setConnectionRequestTimeout(30000);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestBuilder.build());
		client = builder.build();
		
		this.facebookAppId = facebookAppId;
	}
	
	public Map<String, Object> sendUserRequest(String accessToken,
	    String... fields) throws UtilsException
	{
		checkFacebookAppId(accessToken);
		return sendRequest("me", accessToken, fields);
	}
	
	private void checkFacebookAppId(String accessToken) throws UtilsException
	{
		if (facebookAppId != null)
		{
			Map<String, Object> map = sendAppRequest(accessToken, "id");
			String requestAppId = (String) map.get("id");
			if (!requestAppId.equals(facebookAppId))
			{
				throw new UtilsException("Invalid token: bad app id: " + requestAppId);
			}
		}
	}
	
	private Map<String, Object> sendAppRequest(String token, String... fields)
	    throws UtilsException
	{
		return sendRequest("app", token, fields);
	}
	
	private Map<String, Object> sendRequest(String type, String token,
	    String... fields) throws UtilsException
	{
		String fieldsParam = "";
		if (fields.length > 0)
		{
			fieldsParam = "&fields=" + fields[0];
			for (int i = 1; i < fields.length; i++)
			{
				fieldsParam += ',' + fields[i];
			}
		}
		
		logger.trace("Sending FB request: " + type);
		
		HttpGet get = new HttpGet(BASE_URL + type + "/?access_token=" + token
		    + fieldsParam);
		
		CloseableHttpResponse response = null;
		try
		{
			response = (CloseableHttpResponse) client.execute(get);
			int rc = response.getStatusLine().getStatusCode();
			if (rc != 200)
				throw new UtilsException("Facebsook graph returned: " + rc);
			
			String res = EntityUtils.toString(response.getEntity()).trim();
			JSONObject jo = new JSONObject(res);
			
			Map<String, Object> javized = javize(jo);
			
			logger.trace("Got: " + javized);
			
			return javized;
		}
		catch (IOException ioe)
		{
			throw new UtilsException("Facebook graph failed.", ioe);
		}
		finally
		{
			if (response != null)
				try
				{
					response.close();
				}
				catch (IOException e)
				{
					logger.error(e);
				}
		}
	}
	
	private Map<String, Object> javize(JSONObject jo)
	{
		Map<String, Object> res = new HashMap<String, Object>();
		for (String key : jo.keySet())
		{
			Object value = jo.get(key);
			if (value instanceof JSONObject)
			{
				res.put(key, javize((JSONObject) value));
			}
			else
			{
				res.put(key, value);
			}
		}
		return res;
	}
}
