package org.swami.om2.neorepo.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.model.GroupConstraint;
import name.levering.ryan.sparql.model.OptionalConstraint;
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
import org.swami.om2.neorepo.sparql.NeoVariable.VariableType;

public class QueryGraph
{
	private Map<ExpressionLogic, PatternNode> graph =
		new HashMap<ExpressionLogic, PatternNode>();
	private List<NeoVariable> variableList;
	private List<QueryGraph> optionalGraphs = new LinkedList<QueryGraph>();
	private Map<String, PatternNode> nodeTypes =
		new HashMap<String, PatternNode>();
	private Set<PatternNode> possibleStartNodes = new HashSet<PatternNode>();
	protected MetaModelProxy metaModel;
	private Map<ExpressionLogic, String> classMapping =
		new HashMap<ExpressionLogic, String>();

	QueryGraph( MetaModelProxy metaModel, List<NeoVariable> variableList )
	{
		this.variableList = variableList;
		this.metaModel = metaModel;
	}

	PatternNode getStartNode()
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
		Set<TripleConstraint> typeConstraints =
			new HashSet<TripleConstraint>();
		Set<TripleConstraint> normalConstraints =
			new HashSet<TripleConstraint>();
	
		for ( Object constraint : groupConstraint.getConstraints() )
		{
			if ( constraint instanceof TripleConstraint )
			{
				if ( this.metaModel.isTypeProperty(
					this.toUri( ( ( TripleConstraint ) constraint ).
						getPredicateExpression() ) ) )
				{
					typeConstraints.add( ( TripleConstraint ) constraint );
				}
				else
				{
					normalConstraints.add( ( TripleConstraint ) constraint );
				}
			}
			else if ( constraint instanceof OptionalConstraint )
			{
				QueryGraph optionalGraph =
					new QueryGraph( this.metaModel, this.variableList );
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
	
		// Must add types before the other constraints.
		this.addTypes( typeConstraints, optional );
		this.addConstraints( normalConstraints, optional );
	}
	
	private void addTypes(
		Set<TripleConstraint> constraints, boolean optional )
	{
		for ( TripleConstraint constraint : constraints )
		{
			this.addTypeToPattern( constraint, optional );
		}
	}

	private void addTypeToPattern(
		TripleConstraint constraint, boolean optional )
	{
		this.assertConstraint( constraint );
		PatternNode subjectNode = this.getOrCreatePatternNode(
			constraint.getSubjectExpression() );
		PatternNode objectNode = this.getOrCreatePatternNode(
			constraint.getObjectExpression(), ON_CREATED_TYPE );
		subjectNode.createRelationshipTo( objectNode,
			this.metaModel.getTypeRelationship(), optional );
		String objectUri = this.toUri( constraint.getObjectExpression() );
		this.classMapping.put( constraint.getSubjectExpression(), objectUri );
		this.nodeTypes.put( objectUri, objectNode );
	}
	
	private void addConstraints( Set<TripleConstraint> constraints,
		boolean optional )
	{
		for ( TripleConstraint constraint : constraints )
		{
			this.addToPattern( constraint, optional );
		}
	}

	protected void addToPattern( TripleConstraint constraint,
		boolean optional )
	{
		this.assertConstraint( constraint );
		this.addOwlProperty( constraint.getSubjectExpression(),
			constraint.getPredicateExpression(),
			constraint.getObjectExpression(), optional );
	}

	private void addOwlProperty( ExpressionLogic subjectExpression,
		ExpressionLogic predicateExpression, ExpressionLogic objectExpression,
		boolean optional )
	{
		PatternNode subjectNode = this.getOrCreatePatternNode(
			subjectExpression );
		
		OwlProperty property = this.getOwlProperty(
			subjectExpression, predicateExpression, objectExpression );
		
		if ( property.getType() == OwlPropertyType.OBJECT_TYPE )
		{
			subjectNode.createRelationshipTo(
				this.getOrCreatePatternNode( objectExpression ),
				( RelationshipType ) property.getMappedValue(), optional );
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
					propertyKey, optional, valueToMatch );
			}
			else if ( objectExpression instanceof ASTVar )
			{
				subjectNode.addPropertyExistConstraint(
					( String ) property.getMappedValue() );
				this.addVariable( ( ASTVar ) objectExpression,
					NeoVariable.VariableType.LITERAL,
					subjectNode, ( String ) property.getMappedValue() );
			}
			else
			{
				throw new QueryException( "Object [" + objectExpression +
				"] should be a literal or a variable." );
			}
		}
	}

	private void addVariable( ASTVar var, VariableType type,
		PatternNode subjectNode, String string )
	{
		for ( NeoVariable variable : this.variableList )
		{
			if ( var.getName().equals( variable.getName() ) )
			{
				return;
			}
		}
		this.variableList.add(
			new NeoVariable( var, type, subjectNode, string ) );
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

	private PatternNode getOrCreatePatternNode( ExpressionLogic expression )
	{
		return getOrCreatePatternNode( expression, null );
	}
	
	private PatternNode getOrCreatePatternNode( ExpressionLogic expression,
		RunOnPatternNode runOnCreation )
	{
		PatternNode node = this.graph.get( expression );
		if ( node == null )
		{
			node = this.createPatternNode( expression );
			if ( runOnCreation != null )
			{
				runOnCreation.onCreated( node );
			}
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
			this.addVariable( ( ASTVar ) expression,
				NeoVariable.VariableType.URI,
				node, this.metaModel.getAboutKey() );
		}
		
		return node;
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

	private String toUri( ExpressionLogic expression )
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

	private interface RunOnPatternNode
	{
		void onCreated( PatternNode node );
	}
	
	private final RunOnPatternNode ON_CREATED_TYPE =
		new RunOnPatternNode()
	{
		public void onCreated( PatternNode node )
		{
			node.addPropertyEqualConstraint( metaModel.getNodeTypeNameKey(),
				metaModel.getSubTypes( node.getLabel(), true ) );
		}
	};

	void assertGraph()
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
		for ( PatternRelationship relationship : node.getAllRelationships() )
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
		for ( PatternRelationship rel : nodeWithMissingType.getAllRelationships() )
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
}
