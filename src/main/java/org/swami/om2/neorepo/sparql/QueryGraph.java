package org.swami.om2.neorepo.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.model.GroupConstraint;
import name.levering.ryan.sparql.model.OptionalConstraint;
import name.levering.ryan.sparql.model.TripleConstraint;
import name.levering.ryan.sparql.model.logic.ExpressionLogic;
import name.levering.ryan.sparql.parser.model.ASTLiteral;
import name.levering.ryan.sparql.parser.model.ASTVar;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.rdf.model.Literal;
import org.neo4j.rdf.model.Statement;
import org.neo4j.rdf.model.Uri;
import org.neo4j.rdf.model.Value;
import org.neo4j.rdf.model.Wildcard;
import org.neo4j.rdf.model.WildcardEnabledStatement;
import org.neo4j.rdf.store.representation.AbstractNode;
import org.neo4j.rdf.store.representation.AbstractRepresentation;
import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.neo4j.util.matching.PatternNode;

public class QueryGraph
{
	private List<NeoVariable> variableList;
	private Map<String, ASTVar> astVariables = new HashMap<String, ASTVar>();
	private Map<AbstractNode, PatternNode> graph =
		new HashMap<AbstractNode, PatternNode>();
	private List<Map<AbstractNode, PatternNode>> optionalGraphs =
		new LinkedList<Map<AbstractNode, PatternNode>>();
	private MetaModelProxy metaModel;
	private RepresentationStrategy representationStrategy;
	private PatternGraphBuilder graphBuilder;

	QueryGraph( RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel, List<NeoVariable> variableList )
	{
		this.variableList = variableList;
		this.metaModel = metaModel;
		this.representationStrategy = representationStrategy;
		this.graphBuilder = new PatternGraphBuilder( representationStrategy );
	}

	PatternNodeAndNodePair getStartNode()
	{
		int lowestCount = Integer.MAX_VALUE;
		PatternNode startNode = null;
		Node node = null;
		
		for ( AbstractNode abstractNode : this.graph.keySet() )
		{
			int count = this.metaModel.getCount( abstractNode );
			
			if ( count < lowestCount )	
			{
				lowestCount = count;
				startNode = this.graph.get( abstractNode );
				node = this.representationStrategy.getExecutor().lookupNode(
					abstractNode );
			}
		}
		
		return new PatternNodeAndNodePair( startNode, node );
	}

	Collection<PatternNode> getOptionalGraphs()
	{
		Collection<PatternNode> optionalStartNodes =
			new ArrayList<PatternNode>();
		
		for ( Map<AbstractNode, PatternNode> optionalGraph :
			this.optionalGraphs )
		{
			optionalStartNodes.add(
				this.getOverLappingNode( this.graph, optionalGraph ) );
		}
		return optionalStartNodes;
	}

	private PatternNode getOverLappingNode(
		Map<AbstractNode, PatternNode> firstGraph,
		Map<AbstractNode, PatternNode> secondGraph )
	{
		for ( PatternNode node : firstGraph.values() )
		{
			for ( PatternNode mainNode : secondGraph.values() )
			{
				if ( node.getLabel().equals( mainNode.getLabel() ) )
				{
					return mainNode;
				}
			}
		}
		
		throw new QueryException(
			"Optional graphs must be connected to the main statements" );
	}

	void build( GroupConstraint groupConstraint )
	{
		this.build( groupConstraint, false );
	}
	
	void build( GroupConstraint groupConstraint, boolean optional )
	{
		ArrayList<Statement> statements = new ArrayList<Statement>();
		for ( Object constraint : groupConstraint.getConstraints() )
		{
			if ( constraint instanceof TripleConstraint )
			{
				Statement statement =
					this.constructStatement( ( TripleConstraint ) constraint );
				statements.add( statement );
			}
			else if ( constraint instanceof OptionalConstraint )
			{
				QueryGraph optionalGraph =
					new QueryGraph( this.representationStrategy,
						this.metaModel, this.variableList );
				optionalGraph.build( ( ( OptionalConstraint )
					constraint ).getConstraint(), true );
				this.optionalGraphs.add( optionalGraph.getGraph() );
			}
			else
			{
				throw new QueryException(
					"Operation not supported with NeoRdfSource." );
			}
		}
		AbstractRepresentation representation =
			this.representationStrategy.getAbstractRepresentation(
				statements.toArray( new Statement[ statements.size() ] ) );
		this.graph = this.graphBuilder.buildPatternGraph(
			representation, this.variableList, optional );
	}
	
	private Map<AbstractNode, PatternNode> getGraph()
	{
		return this.graph;
	}
	
	private Statement constructStatement( TripleConstraint triple )
	{
		this.collectVariables( triple.getSubjectExpression(),
			triple.getObjectExpression() );
		Value subject = this.createUriOrWildcard(
				triple.getSubjectExpression() );
		Value predicate =
			new Uri( triple.getPredicateExpression().toString() );

		Value object = null;
		if ( triple.getObjectExpression() instanceof ASTLiteral )
		{
			object = new Literal( triple.getObjectExpression().toString() );
		}
		else
		{
			object = this.createUriOrWildcard( triple.getObjectExpression() );
		}
		
		return new WildcardEnabledStatement( subject, predicate, object );
	}
	
	private Value createUriOrWildcard( ExpressionLogic expression )
	{
		Value value = null;
		if ( expression instanceof ASTVar )
		{
			value = new Wildcard( ( ( ASTVar ) expression ).getName() );
		}
		else
		{
			value = new Uri( expression.toString() );
		}
		return value;
	}

	private void collectVariables( ExpressionLogic... expressions )
	{
		for ( ExpressionLogic expression : expressions )
		{
			if ( expression instanceof ASTVar )
			{
				this.astVariables.put(
					expression.toString(), ( ASTVar ) expression );
			}
		}
	}
}

class ARelationshipType implements RelationshipType
{
	private String name;
	ARelationshipType( String name )
	{
		this.name = name;
	}
	
	public String name()
	{
		return this.name;
	}
	
}