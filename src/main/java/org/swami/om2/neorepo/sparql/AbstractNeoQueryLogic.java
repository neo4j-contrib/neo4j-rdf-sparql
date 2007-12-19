package org.swami.om2.neorepo.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.model.TripleConstraint;
import name.levering.ryan.sparql.model.logic.ExpressionLogic;
import name.levering.ryan.sparql.parser.model.ASTLiteral;
import name.levering.ryan.sparql.parser.model.ASTQName;
import name.levering.ryan.sparql.parser.model.ASTVar;
import name.levering.ryan.sparql.parser.model.URINode;

import org.neo4j.api.core.RelationshipType;
import org.neo4j.util.matching.PatternNode;
import org.neo4j.util.matching.PatternRelationship;
import org.swami.om2.neorepo.sparql.MetaModelProxy.OwlPropertyType;

public abstract class AbstractNeoQueryLogic
{
	private Map<ExpressionLogic, PatternNode> graph;
	private Map<String, PatternNode> nodeTypes;
	private Set<PatternNode> possibleStartNodes;
	private List<NeoVariable> variableList;
	private Map<ExpressionLogic, String> classMapping;
	protected MetaModelProxy metaModel;

	AbstractNeoQueryLogic( MetaModelProxy metaModel )
	{
		this.graph = new HashMap<ExpressionLogic, PatternNode>();
		this.nodeTypes = new HashMap<String, PatternNode>();
		this.possibleStartNodes = new HashSet<PatternNode>();
		this.variableList = new LinkedList<NeoVariable>();
		this.classMapping = new HashMap<ExpressionLogic, String>();
		this.metaModel = metaModel;
	}
	
	protected PatternNode getStartNode()
	{
		int lowestCount = Integer.MAX_VALUE;
		PatternNode startNode = null;
		
		for ( PatternNode node : this.possibleStartNodes )
		{
			int count = this.metaModel.getCount( node.getLabel() );
			
			if ( count < lowestCount )	
			{
				lowestCount = count;
				startNode = node;
			}
		}
		
		return startNode;
	}
	
	protected List<NeoVariable> getNeoVariables()
	{
		return this.variableList;
	}

	protected void addTypeToPattern( TripleConstraint constraint )
	{
		this.assertConstraint( constraint );
		PatternNode subjectNode = this.getOrCreatePatternNode(
			constraint.getSubjectExpression() );
		PatternNode objectNode = this.getOrCreatePatternNode(
			constraint.getObjectExpression() );
		subjectNode.createRelationshipTo( objectNode,
			this.metaModel.getTypeRelationship() );
		String objectUri = this.toUri( constraint.getObjectExpression() );
		this.classMapping.put( constraint.getSubjectExpression(), objectUri );
		this.nodeTypes.put( objectUri, objectNode );
	}
	
	protected void addToPattern( TripleConstraint constraint )
	{
		this.assertConstraint( constraint );
		this.addOwlProperty( constraint.getSubjectExpression(),
			constraint.getPredicateExpression(),
			constraint.getObjectExpression() );
	}

	private void addOwlProperty( ExpressionLogic subjectExpression,
		ExpressionLogic predicateExpression, ExpressionLogic objectExpression )
	{
		PatternNode subjectNode = this.getOrCreatePatternNode(
			subjectExpression );
		
		OwlProperty property = this.getOwlProperty(
			subjectExpression, predicateExpression, objectExpression );
		
		if ( property.getType() == OwlPropertyType.OBJECT_TYPE )
		{
			subjectNode.createRelationshipTo(
				this.getOrCreatePatternNode( objectExpression ),
				( RelationshipType ) property.getMappedValue() );
		}
		else // It's an OwlProperty.DATATYPE_TYPE
		{
			if ( objectExpression instanceof ASTLiteral )
			{
				String propertyKey = ( String ) property.getMappedValue();
				Object valueToMatch =
					metaModel.convertCriteriaStringValueToRealValue(
						propertyKey, ( ( ASTLiteral )
							objectExpression ).getLabel() );
				subjectNode.addPropertyEqualConstraint(
					propertyKey, valueToMatch );
			}
			else if ( objectExpression instanceof ASTVar )
			{
				subjectNode.addPropertyExistConstraint(
					( String ) property.getMappedValue() );
				this.variableList.add( new NeoVariable(
					( ASTVar ) objectExpression,
					NeoVariable.VariableType.LITERAL,
					subjectNode, ( String ) property.getMappedValue() ) );
			}
			else
			{
				throw new QueryException( "Object [" + objectExpression +
				"] should be a literal or a variable." );
			}
		}
	}

	private OwlProperty getOwlProperty( ExpressionLogic subjectExpression,
		ExpressionLogic predicateExpression, ExpressionLogic objectExpression )
	{
		if ( objectExpression instanceof ASTVar )
		{
			return this.metaModel.getOwlProperty(
				this.classMapping.get( subjectExpression ),
				this.toUri( predicateExpression ),
				this.classMapping.get( objectExpression ) );
		}
		else
		{
			return this.metaModel.getOwlProperty(
				this.classMapping.get( subjectExpression ),
				this.toUri( predicateExpression ), null );
		}
	}

	private PatternNode createPatternNode( ExpressionLogic expression )
	{
		PatternNode node =
			new PatternNode( this.toUri( expression ) );
		this.graph.put( expression, node );
		
		if ( expression instanceof ASTQName )
		{
			this.possibleStartNodes.add( node );
		}
		
		if ( expression instanceof ASTVar )
		{
			this.variableList.add( new NeoVariable( ( ASTVar ) expression,
				NeoVariable.VariableType.URI,
				node, this.metaModel.getAboutKey() ) );
		}
		
		return node;
	}

	private PatternNode getOrCreatePatternNode( ExpressionLogic expression )
	{
		PatternNode node = this.graph.get( expression );
		if ( node == null )
		{
			node = this.createPatternNode( expression );
		}
		return node;
	}
	
	private PatternNode getOrCreatePatternNode( String nodeType )
	{
		PatternNode node = this.nodeTypes.get( nodeType );
		if ( node == null )
		{
			node = new PatternNode( nodeType );
			this.nodeTypes.put( nodeType, node );
			this.possibleStartNodes.add( node );
		}
		return node;
	}

	protected void assertGraph()
	{
		for ( PatternNode node : this.graph.values() )
		{
			if ( !this.possibleStartNodes.contains( node ) )
			{
				this.assertHasTypeRelationship( node );
			}
		}
	}
	
	private PatternRelationship findTypeRelationshipOrNull( PatternNode node )
	{
		for ( PatternRelationship relationship : node.getRelationships() )
		{
			if ( relationship.getType().equals(
				this.metaModel.getTypeRelationship() ) )
			{
				return relationship;
			}
		}
		return null;
	}
	
	private void assertHasTypeRelationship( PatternNode node )
	{
		boolean found = findTypeRelationshipOrNull( node ) != null;
		if ( !found )
		{
			found = tryToFindAndAddType( node );
		}
		if ( !found )
		{
			throw new QueryException( "Type for variable [" + node.getLabel() +
				"] is not specified. Not supported." );
		}
	}
	
	private boolean tryToFindAndAddType( PatternNode nodeWithMissingType )
	{
		// Try to get the type from proxy...
		// allright take the first relationship for this node,
		// go to that other node and get its type relationship to
		// use you know!
		PatternRelationship firstRelationshipHack = null;
		for ( PatternRelationship rel : nodeWithMissingType.getRelationships() )
		{
			firstRelationshipHack = rel;
			break;
		}
		
		if ( firstRelationshipHack != null )
		{
			PatternNode otherNode =
				firstRelationshipHack.getOtherNode( nodeWithMissingType );
			PatternRelationship otherNodesTypeRel =
				findTypeRelationshipOrNull( otherNode );
			if ( otherNodesTypeRel != null )
			{
				PatternNode otherTypeNode = otherNodesTypeRel.getOtherNode(
					otherNode );
				String type = this.metaModel.getObjectType(
					otherTypeNode.getLabel(),
					firstRelationshipHack.getType().name() );
				if ( type != null )
				{
					PatternNode typeNode = getOrCreatePatternNode( type );
					nodeWithMissingType.createRelationshipTo( typeNode,
						this.metaModel.getTypeRelationship() );
					return true;
				}
			}
		}
		return false;
	}

	private void assertConstraint( TripleConstraint constraint )
	{
		if ( !( constraint.getPredicateExpression() instanceof ASTQName ) )
		{
			throw new QueryException(
				"Predicate [" + constraint.getPredicateExpression() +
				"] is not a fully qualified predicate. Not supported." );
		}
	}

	protected String toUri( ExpressionLogic expression )
	{
		String namespace = "";
		String localName = "";
		
		if ( expression instanceof URINode )
		{
			namespace = ( ( URINode ) expression ).getNamespace();
			localName = ( ( URINode ) expression ).getLocalName();
		}
		else if ( expression instanceof ASTVar )
		{
			localName = ( ( ASTVar ) expression ).getName();
		}
		else if ( expression instanceof ASTLiteral )
		{
			localName = ( ( ASTLiteral ) expression ).getLabel();
		}
		
		return namespace + localName;
	}
}
