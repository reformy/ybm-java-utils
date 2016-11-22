package com.kwazylabs.utils.persistence;

import java.io.Serializable;
import java.util.List;

public interface GenericDAO<T, PK extends Serializable>
{
	public String getPersistenceUnit() throws PersistenceException;
	
	/** Persist the newInstance object into database */
	public void create(T newInstance) throws PersistenceException;
	
	/**
	 * Retrieve an object that was previously persisted to the database using the
	 * indicated id as primary key
	 */
	public T read(PK id) throws PersistenceException;
	
	/** Save changes made to a persistent object. */
	public void update(T transientObject) throws PersistenceException;
	
	/** Check if this object is new, and create or update it accordingly. */
	public void createOrUpdate(T transientObject) throws PersistenceException;
	
	/** Remove an object from persistent storage in the database */
	public void delete(T persistentObject) throws PersistenceException;
	
	/**
	 * 
	 * @return the type for this GenericDao
	 */
	public Class<T> getType();
	
	/**
	 * Executes a DML query
	 * 
	 * @param query
	 * @return The number of entities updated or deleted.
	 */
	public int runUpdateQuery(String query) throws PersistenceException;
	
	/**
	 * Executes a DML query
	 * 
	 * @param query
	 * @return The number of entities updated or deleted.
	 */
	public int runUpdateQuery(QueryWithParams query) throws PersistenceException;
	
	/**
	 * Runs an SQL query expected to return a multiple results.
	 * 
	 * @param query
	 * @return
	 */
	public List<? extends Serializable> runMultipleResultsQuery(QueryWithParams query) throws PersistenceException;
	
	public Serializable runSingleResultQuery(QueryWithParams query) throws PersistenceException;
	
	public void create(Serializable object) throws PersistenceException;
}
