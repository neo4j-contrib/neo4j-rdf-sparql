package org.swami.om2.neorepo.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.neo4j.util.matching.PatternMatch;
import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfBindingSetVisitor;

public class NeoRdfBindingSet implements RdfBindingSet
{
	private List<NeoVariable> variables;
	private Iterable<PatternMatch> matches;
	
	NeoRdfBindingSet( List<NeoVariable> variables,
		Iterable<PatternMatch> matches )
	{
		this.variables = variables;
		this.matches = matches;
	}
	
	public void accept( RdfBindingSetVisitor visitor )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean contains( Object object )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public String describeSet()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public List<? extends NeoVariable> getVariables()
	{
		return this.variables;
	}

	public boolean isDistinct()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean isOrdered()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public Iterator<NeoBindingRow> iterator()
	{
		return new NeoRdfBindingSetIterator();
	}

	public int size()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean add( Object arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean addAll( Collection arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public void clear()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean containsAll( Collection arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean isEmpty()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean remove( Object arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean removeAll( Collection arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean retainAll( Collection arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public Object[] toArray()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public Object[] toArray( Object[] arg0 )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}
	
	class NeoRdfBindingSetIterator implements Iterator<NeoBindingRow>
	{
		private Iterator<PatternMatch> iterator = matches.iterator();
		
		public boolean hasNext()
		{
			return this.iterator.hasNext();
		}

		public NeoBindingRow next()
		{
			return new NeoBindingRow( NeoRdfBindingSet.this, iterator.next() );
		}

		public void remove()
		{
			throw new QueryException(
				"Operation not supported with NeoRdfSource." );
		}
	}
}
