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

public class Neo4jConstructQueryLogic extends AbstractNeo4jQueryLogic implements
	ConstructQueryLogic
{
	private ConstructQueryData data;
	
	public Neo4jConstructQueryLogic( ConstructQueryData data,
		RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
		super( representationStrategy, metaModel );
		this.data = data;
	}
	
	public RdfGraph execute( RdfSource source )
	{
		QueryGraph graph = this.buildGraph( this.data.getConstraint() );
		
		Neo4jRdfBindingSet bindingSet = new Neo4jRdfBindingSet(
			this.getNeo4jVariables(), this.performMatches( graph ) );
		return new Neo4jRdfGraph( this.data.getTriples(), bindingSet );
	}
	
	@Override
	protected List<Neo4jVariable> getNeo4jVariables()
	{
		List<Neo4jVariable> variables = new LinkedList<Neo4jVariable>();
		for ( Neo4jVariable variable : super.getNeo4jVariables() )
		{
			if ( this.variableExists(
				this.data.getVariables(), variable.getName() ) )
			{
				variables.add( variable );
			}
		}
		
		return variables;
	}
}

class Neo4jRdfGraph implements RdfGraph, Iterable<LenientStatement>
{
	private Collection<UnboundStatement> triples;
	private Neo4jRdfBindingSet bindingSet;
	RdfGraphIterator iterator;
	
	public Neo4jRdfGraph( Collection<UnboundStatement> triples,
		Neo4jRdfBindingSet bindingSet )
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
		private Iterator<Neo4jBindingRow> bindingRows;
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
			Neo4jBindingRow bindingRow = this.bindingRows.next();
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