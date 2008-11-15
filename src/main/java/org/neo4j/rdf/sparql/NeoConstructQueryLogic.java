package org.neo4j.rdf.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import name.levering.ryan.sparql.common.LenientStatement;
import name.levering.ryan.sparql.common.RdfGraph;
import name.levering.ryan.sparql.common.RdfSource;
import name.levering.ryan.sparql.common.impl.StatementImpl;
import name.levering.ryan.sparql.model.data.ConstructQueryData;
import name.levering.ryan.sparql.model.data.UnboundStatement;
import name.levering.ryan.sparql.model.logic.ConstructQueryLogic;
import name.levering.ryan.sparql.model.logic.ExpressionLogic;

import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.openrdf.model.URI;

public class NeoConstructQueryLogic extends AbstractNeoQueryLogic implements
	ConstructQueryLogic
{
	private ConstructQueryData data;
	
	public NeoConstructQueryLogic( ConstructQueryData data,
		RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
		super( representationStrategy, metaModel );
		this.data = data;
	}
	
	public RdfGraph execute( RdfSource source )
	{
		QueryGraph graph = this.buildGraph( this.data.getConstraint() );
		
		NeoRdfBindingSet bindingSet = new NeoRdfBindingSet(
			this.getNeoVariables(), this.performMatches( graph ) );
		return new NeoRdfGraph( this.data.getTriples(), bindingSet );
	}
	
	@Override
	protected List<NeoVariable> getNeoVariables()
	{
		List<NeoVariable> neoVariables = new LinkedList<NeoVariable>();
		for ( NeoVariable neoVariable : super.getNeoVariables() )
		{
			if ( this.variableExists(
				this.data.getVariables(), neoVariable.getName() ) )
			{
				neoVariables.add( neoVariable );
			}
		}
		
		return neoVariables;
	}
}

class NeoRdfGraph implements RdfGraph, Iterable<LenientStatement>
{
	private Collection<UnboundStatement> triples;
	private NeoRdfBindingSet bindingSet;
	RdfGraphIterator iterator;
	
	public NeoRdfGraph( Collection<UnboundStatement> triples,
		NeoRdfBindingSet bindingSet )
	{
		this.triples = triples;
		this.bindingSet = bindingSet;
		this.iterator = new RdfGraphIterator();
	}
	
	public Iterator<LenientStatement> iterator()
	{
		return this.iterator;
	}

	class RdfGraphIterator implements Iterator<LenientStatement>
	{
		private Iterator<NeoBindingRow> bindingRows;
		private Iterator<LenientStatement> currentTriples;
		
		public RdfGraphIterator()
		{
			this.bindingRows = bindingSet.iterator();
		}
	
		public boolean hasNext()
		{
			if ( this.currentTriples == null )
			{
				this.currentTriples = this.getNextTriples();
			}
			
			return this.currentTriples != null;
		}
	
		public LenientStatement next()
		{
			if ( this.currentTriples == null ||
				!this.currentTriples.hasNext() )
			{
				this.currentTriples = this.getNextTriples();
				if ( this.currentTriples == null )
				{
					throw new NoSuchElementException();
				}
			}
			
			LenientStatement statementToReturn = this.currentTriples.next();
			if ( !this.currentTriples.hasNext() )
			{
				this.currentTriples = null;
			}
			
			return statementToReturn;
		}
	
		private Iterator<LenientStatement> getNextTriples()
		{
			if ( !this.bindingRows.hasNext() )
			{
				return null;
			}
			
			List<LenientStatement> boundStatements =
				new ArrayList<LenientStatement>();
			NeoBindingRow bindingRow = this.bindingRows.next();
			for ( UnboundStatement unboundStatement : triples )
			{
				LenientStatement statement = new StatementImpl(
					unboundStatement.getSubjectExpression().evaluate( bindingRow ),
					this.toUri( unboundStatement.getPredicateExpression() ),
					unboundStatement.getObjectExpression().evaluate(
						bindingRow ) );
				boundStatements.add( statement );
			}
			
			return boundStatements.iterator();
		}
		
		private URI toUri( ExpressionLogic predicateExpression )
		{
			return ( URI ) predicateExpression;
		}
	
		public void remove()
		{
			throw new UnsupportedOperationException( "Remove not supported." );
		}
	}
}