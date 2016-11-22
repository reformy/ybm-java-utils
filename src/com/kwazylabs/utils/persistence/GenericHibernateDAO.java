package com.kwazylabs.utils.persistence;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kwazylabs.utils.ClassUtils;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@SuppressWarnings("unchecked")
public class GenericHibernateDAO<T, PK extends Serializable> implements
    GenericDAO<T, PK>
{
	private Class<T> c;
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}
	
	private SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}
	
	protected Session getSession()
	{
		return getSessionFactory().getCurrentSession();
	}
	
	public GenericHibernateDAO()
	{
		this.c = (Class<T>) ClassUtils.getParameterizedType(getClass())
		    .getActualTypeArguments()[0];
	}
	
	public GenericHibernateDAO(String className) throws ClassNotFoundException
	{
		this.c = (Class<T>) Class.forName(className);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public void create(T newInstance) throws PersistenceException
	{
		try
		{
			getSession().save(newInstance);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed creating.", e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public void delete(T persistentObject) throws PersistenceException
	{
		try
		{
			getSession().delete(persistentObject);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed deleting.", e);
		}
	}
	
	@Override
	public String getPersistenceUnit() throws PersistenceException
	{
		return null;
	}
	
	@Override
	public Class<T> getType()
	{
		return c;
	}
	
	@Override
	public T read(PK id) throws PersistenceException
	{
		try
		{
			return (T) getSession().get(c, id);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed reading.", e);
		}
	}
	
	@Override
	public List<? extends Serializable> runMultipleResultsQuery(
	    QueryWithParams query) throws PersistenceException
	{
		try
		{
			Query hibernateQuery = createHibernateQuery(query);
			
			if (query.firstResult != null)
				hibernateQuery.setFirstResult(query.firstResult);
			if (query.maxResults != null)
				hibernateQuery.setMaxResults(query.maxResults);
			
			return hibernateQuery.list();
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed querying.", e);
		}
	}
	
	@Override
	public Serializable runSingleResultQuery(QueryWithParams query)
	    throws PersistenceException
	{
		try
		{
			Query hibernateQuery = createHibernateQuery(query);
			
			return (Serializable) hibernateQuery.uniqueResult();
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed querying.", e);
		}
	}
	
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public int runUpdateQuery(String query) throws PersistenceException
	{
		QueryWithParams q = new QueryWithParams();
		q.query = query;
		return runUpdateQuery(q);
	}
	
	private Query createHibernateQuery(QueryWithParams query)
	{
		Query q = getSession().createQuery(query.query);
		for (Entry<String, Object> e : query.params.entrySet())
		{
			if (e.getValue() instanceof Collection<?>)
				q.setParameterList(e.getKey(), (Collection<?>) e.getValue());
			else
				q.setParameter(e.getKey(), e.getValue());
		}
		return q;
	}
	
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public int runUpdateQuery(QueryWithParams query) throws PersistenceException
	{
		try
		{
			Query q = createHibernateQuery(query);
			return q.executeUpdate();
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed executing update query.", e);
		}
	}
	
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public void update(T transientObject) throws PersistenceException
	{
		try
		{
			getSession().update(transientObject);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed updating.", e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public void createOrUpdate(T transientObject) throws PersistenceException
	{
		try
		{
			getSession().saveOrUpdate(transientObject);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed creating or updating.", e);
		}
	}
	
	/**
	 * Joins the collection parts to one statement with a separator between them
	 * and surrounds it with a brackets.
	 * 
	 * @param parts
	 *          The collection to be joined.
	 * @param type
	 *          The separator between the joint parts.
	 * @return The joined parts surrounded with brackets. <tt>null</tt> if null
	 *         iterator input.
	 */
	protected String joinWithBrackets(Collection<?> parts,
	    ParametersJoinTypes type)
	{
		String joined = StringUtils.join(parts, type.toPaddedString());
		if (joined == null)
		{
			return null;
		}
		return "(" + joined + ")";
	}
	
	/**
	 * The possible values for joining different parts in a query.
	 * <p>
	 * 
	 * @author tomh
	 */
	protected enum ParametersJoinTypes
	{
		OR("OR"),
		AND("AND"),
		COMA(",");
		
		private String text;
		
		private ParametersJoinTypes(String text)
		{
			this.text = text;
		}
		
		@Override
		public String toString()
		{
			return text;
		}
		
		/**
		 * @return the <code>toString</code> padded with a whitespace in both sides
		 */
		public String toPaddedString()
		{
			return " " + text + " ";
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = PersistenceException.class)
	@Override
	public void create(Serializable object) throws PersistenceException
	{
		try
		{
			getSession().save(object);
		}
		catch (Exception e)
		{
			throw new PersistenceException("Failed creating.", e);
		}
	}
}
