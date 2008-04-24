package org.swami.om2.neorepo.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.neo4j.rdf.store.representation.AbstractRelationship;
import org.neo4j.rdf.store.representation.AbstractRepresentation;
import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.neo4j.util.matching.PatternNode;
import org.neo4j.util.matching.PatternUtil;
import org.swami.om2.neorepo.sparql.NeoVariable.VariableType;

public class QueryGraph
{
	private List<NeoVariable> variableList;
	private Map<String, ASTVar> astVariables = new HashMap<String, ASTVar>();
	private Map<AbstractNode, PatternNode> graph =
		new HashMap<AbstractNode, PatternNode>();
	private List<QueryGraph> optionalGraphs = new LinkedList<QueryGraph>();
	private MetaModelProxy metaModel;
	private RepresentationStrategy representationStrategy;

	QueryGraph( RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel, List<NeoVariable> variableList )
	{
		this.variableList = variableList;
		this.metaModel = metaModel;
		this.representationStrategy = representationStrategy;
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
		
		for ( QueryGraph optionalGraph : this.optionalGraphs )
		{
			optionalStartNodes.add( this.getOverLappingNode( optionalGraph ) );
		}
		return optionalStartNodes;
	}

	private PatternNode getOverLappingNode( QueryGraph optionalGraph )
	{
		for ( PatternNode node : optionalGraph.graph.values() )
		{
			for ( PatternNode mainNode : this.graph.values() )
			{
				if ( node.getLabel().equals( mainNode.getLabel() ) )
				{
					return node;
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
				this.optionalGraphs.add( optionalGraph );
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
		this.buildPatternGraph( representation, optional );
	}
	
	private void buildPatternGraph(
		AbstractRepresentation representation, boolean optional )
	{
		for ( AbstractNode node : representation.nodes() )
		{
			this.graph.put( node, this.createPatternNode( node ) );
		}
		
		for ( AbstractRelationship relationship :
			representation.relationships() )
		{
			AbstractNode startNode = relationship.getStartNode();
			AbstractNode endNode = relationship.getEndNode();
			final String name = relationship.getRelationshipTypeName();
			this.graph.get( startNode ).createRelationshipTo(
				this.graph.get( endNode ), new ARelationshipType( name ),
				optional );
		}
	}
	
	private PatternNode createPatternNode( AbstractNode node )
	{
		PatternNode patternNode = null;
		if ( node.isWildcard() )
		{
			Wildcard wildcard = node.getWildcardOrNull();
			patternNode = new PatternNode( wildcard.getVariableName() );
			this.addVariable(
					this.astVariables.get( wildcard.getVariableName() ),
					VariableType.URI, patternNode,
					this.representationStrategy.getExecutor().
					getNodeUriPropertyKey( node ) );
		}
		else
		{
			Uri uri = node.getUriOrNull();
			patternNode = new PatternNode(
				uri == null ? "" : uri.getUriAsString() );
			if ( uri != null )
			{
				patternNode.addPropertyEqualConstraint(
					this.representationStrategy.getExecutor().
					getNodeUriPropertyKey( node ), uri.getUriAsString() );
			}
		}
		
		for ( Entry<String, Collection<Object>> entry :
			node.properties().entrySet() )
		{
			for ( Object value : entry.getValue() )
			{
				if ( value instanceof Wildcard )
				{
					this.addVariable( this.astVariables.get(
						( ( Wildcard ) value ).getVariableName() ),
						VariableType.LITERAL, patternNode, entry.getKey() );
				}
				else
				{
					patternNode.addPropertyEqualConstraint(
						entry.getKey(), value );
				}
			}
		}
		return patternNode;
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
			value = new Wildcard( expression.toString() );
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
	
	private void addVariable( ASTVar var, VariableType type,
		PatternNode subjectNode, String property )
	{
		for ( NeoVariable variable : this.variableList )
		{
			if ( var.getName().equals( variable.getName() ) )
			{
				return;
			}
		}
		this.variableList.add(
			new NeoVariable( var, type, subjectNode, property ) );
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