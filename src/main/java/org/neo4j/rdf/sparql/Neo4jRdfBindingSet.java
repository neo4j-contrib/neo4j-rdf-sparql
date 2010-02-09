package org.neo4j.rdf.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfBindingSetVisitor;

import org.neo4j.graphmatching.PatternMatch;

public class Neo4jRdfBindingSet implements RdfBindingSet
{
	private List<Neo4jVariable> variables;
	private Iterable<PatternMatch> matches;
	
	Neo4jRdfBindingSet( List<Neo4jVariable> variables,
		Iterable<PatternMatch> matches )
	{
		this.variables = variables;
		this.matches = matches;
	}
	
	public void accept( RdfBindingSetVisitor visitor )
	{
		throw new QueryException( "Operation not supported." );
	}

	public boolean contains( Object object )
	{
        throw new QueryException( "Operation not supported." );
	}

	public String describeSet()
	{
        throw new QueryException( "Operation not supported." );
	}

	public List<? extends Neo4jVariable> getVariables()
	{
		return this.variables;
	}

	public boolean isDistinct()
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean isOrdered()
	{
        throw new QueryException( "Operation not supported." );
	}

	public Iterator<Neo4jBindingRow> iterator()
	{
		return new BindingSetIterator();
	}

	public int size()
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean add( Object item )
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean addAll( Collection items )
	{
        throw new QueryException( "Operation not supported." );
	}

	public void clear()
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean containsAll( Collection items )
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean isEmpty()
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean remove( Object item )
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean removeAll( Collection items )
	{
        throw new QueryException( "Operation not supported." );
	}

	public boolean retainAll( Collection items )
	{
        throw new QueryException( "Operation not supported." );
	}

	public Object[] toArray()
	{
        throw new QueryException( "Operation not supported." );
	}

	public Object[] toArray( Object[] array )
	{
        throw new QueryException( "Operation not supported." );
	}
	
	class BindingSetIterator implements Iterator<Neo4jBindingRow>
	{
		private Iterator<PatternMatch> iterator = matches.iterator();
		
		public boolean hasNext()
		{
			return this.iterator.hasNext();
		}

		public Neo4jBindingRow next()
		{
			return new Neo4jBindingRow( Neo4jRdfBindingSet.this, iterator.next() );
		}

		public void remove()
		{
	        throw new QueryException( "Operation not supported." );
		}
	}
}
