package org.neo4j.rdf.sparql;

import java.util.LinkedList;
import java.util.List;

import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfSource;
import name.levering.ryan.sparql.model.data.SelectQueryData;
import name.levering.ryan.sparql.model.logic.SelectQueryLogic;

import org.neo4j.rdf.store.representation.RepresentationStrategy;

public class Neo4jSelectQueryLogic extends AbstractNeo4jQueryLogic
	implements SelectQueryLogic
{
	private SelectQueryData data;
	
	public Neo4jSelectQueryLogic( SelectQueryData data,
		RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
		super( representationStrategy, metaModel );
		this.data = data;
	}
	
	public RdfBindingSet execute( RdfSource source )
	{
		QueryGraph graph = this.buildGraph( this.data.getConstraint() );

		return new Neo4jRdfBindingSet( this.getNeo4jVariables(),
			this.performMatches( graph ) );
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
