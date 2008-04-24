package org.swami.om2.neorepo.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.rdf.model.Uri;
import org.neo4j.rdf.model.Wildcard;
import org.neo4j.rdf.store.representation.AbstractNode;
import org.neo4j.rdf.store.representation.AbstractRelationship;
import org.neo4j.rdf.store.representation.AbstractRepresentation;
import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.neo4j.util.matching.PatternNode;
import org.swami.om2.neorepo.sparql.NeoVariable.VariableType;

public class PatternGraphBuilder
{
	private RepresentationStrategy representationStrategy;
	private Map<AbstractNode, PatternNode> graph =
		new HashMap<AbstractNode, PatternNode>();
	private List<NeoVariable> variableMapping = new ArrayList<NeoVariable>();
	
	public PatternGraphBuilder( RepresentationStrategy representationStrategy )
	{
		this.representationStrategy = representationStrategy;
	}
	
	public Map<AbstractNode, PatternNode> buildPatternGraph(
		AbstractRepresentation representation,
		List<NeoVariable> variableMapping )
	{
		return buildPatternGraph( representation, variableMapping, false );
	}
	
	public Map<AbstractNode, PatternNode> buildPatternGraph(
		AbstractRepresentation representation,
		List<NeoVariable> variableMapping, boolean optional )
	{
		this.variableMapping = variableMapping;
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
		
		return this.graph;
	}
	
	public List<NeoVariable> getVariableMapping()
	{
		return this.variableMapping;
	}
	
	private PatternNode createPatternNode( AbstractNode node )
	{
		PatternNode patternNode = null;
		if ( node.isWildcard() )
		{
			Wildcard wildcard = node.getWildcardOrNull();
			patternNode = new PatternNode( wildcard.getVariableName() );
			this.addVariable( wildcard.getVariableName(),
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
					this.addVariable( ( ( Wildcard ) value ).getVariableName(),
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
	
	private void addVariable( String variableName, VariableType type,
		PatternNode patternNode, String property )
	{
		for ( NeoVariable variable : this.variableMapping )
		{
			if ( variableName.equals( variable.getName() ) )
			{
				return;
			}
		}
		this.variableMapping.add(
			new NeoVariable( variableName, type, patternNode, property ) );
	}
}
