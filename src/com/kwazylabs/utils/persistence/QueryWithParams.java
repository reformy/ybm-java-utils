package com.kwazylabs.utils.persistence;

import java.util.HashMap;
import java.util.Map;

public class QueryWithParams
{
	public Map<String, Object> params = new HashMap<String, Object>();
	public String query;
	public Integer firstResult;
	public Integer maxResults;
	
	public QueryWithParams()
	{
	}
	
	public QueryWithParams(String query)
	{
		this.query = query;
	}
}
