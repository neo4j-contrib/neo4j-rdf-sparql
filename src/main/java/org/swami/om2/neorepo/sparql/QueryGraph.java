package org.swami.om2.neorepo.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.neo4j.rdf.store.representation.AbstractStatementRepresentation;
import org.neo4j.rdf.store.representation.RdfRepresentationStrategy;
import org.neo4j.util.matching.PatternNode;
import org.neo4j.util.matching.PatternUtil;
import org.swami.om2.neorepo.sparql.NeoVariable.VariableType;

public class QueryGraph
{
//	private Map<ExpressionLogic, PatternNode> graph =
//		new HashMap<ExpressionLogic, PatternNode>();
//	private Map<ExpressionLogic, AbstractNode> abstractGraph =
//		new HashMap<ExpressionLogic, AbstractNode>();
	private List<NeoVariable> variableList;
	private Map<String, ASTVar> astVariables = new HashMap<String, ASTVar>();
	private Map<AbstractNode, PatternNode> graph =
		new HashMap<AbstractNode, PatternNode>();
	private List<QueryGraph> optionalGraphs = new LinkedList<QueryGraph>();
//	private Map<String, PatternNode> nodeTypes =
//		new HashMap<String, PatternNode>();
	private Set<PatternNode> possibleStartNodes = new HashSet<PatternNode>();
	private MetaModelProxy metaModel;
	private RdfRepresentationStrategy representationStrategy;
//	private Map<ExpressionLogic, String> classMapping =
//		new HashMap<ExpressionLogic, String>();

	QueryGraph( RdfRepresentationStrategy representationStrategy,
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
				node = this.representationStrategy.getAsrExecutor().lookupNode(
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
		AbstractStatementRepresentation representation =
			this.representationStrategy.getAbstractRepresentation(
				statements.toArray( new Statement[ statements.size() ] ) );
		this.buildPatternGraph( representation, optional );
	}
	
	private void buildPatternGraph(
		AbstractStatementRepresentation representation, boolean optional )
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
		PatternUtil.printGraph(
			this.getStartNode().getPatternNode(), System.out );
	}
	
	private PatternNode createPatternNode( AbstractNode node )
	{
		String uri = node.getUriOrNull().getUriAsString();
		PatternNode patternNode = new PatternNode( uri );
		
		if ( node.isWildcard() )
		{
			this.addVariable( this.astVariables.get( uri ), VariableType.URI,
				patternNode, this.representationStrategy.getAsrExecutor().
				getNodeUriPropertyKey( node ) );
		}
		else
		{
			patternNode.addPropertyEqualConstraint(
				this.representationStrategy.getAsrExecutor().
				getNodeUriPropertyKey( node ), uri );
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
						entry.getKey(), entry.getValue() );
				}
			}
		}
		return patternNode;
	}
	
//	private boolean isAVariable( AbstractNode node )
//	{
//		if ( node  )
//		{
//			return false;
//		}
//		return node.getUriOrNull().getUriAsString().startsWith( "?" );
//	}

	private Statement constructStatement( TripleConstraint triple )
	{
		this.collectVariables( triple.getSubjectExpression(),
			triple.getObjectExpression() );
		Value subject =
			new Uri( triple.getSubjectExpression().toString() );
		Value predicate =
			new Uri( triple.getPredicateExpression().toString() );
		Value object = null;
		
		boolean wildcardsUsed = false;
		if ( triple.getObjectExpression() instanceof ASTLiteral )
		{
			object = new Literal( triple.getObjectExpression().toString() );
		}
		else if ( triple.getObjectExpression() instanceof ASTVar )
		{
			object = new Wildcard( triple.getObjectExpression().toString() );
			wildcardsUsed = true;
		}
		else
		{
			object = new Uri( triple.getObjectExpression().toString() );
		}
		
		Statement statement = null;
//		if ( wildcardsUsed )
//		{
			statement = new WildcardEnabledStatement(
				subject, predicate, object );
//		}
//		else
//		{
//			statement = new CompleteStatement( ( Uri ) subject, ( Uri ) predicate, ( Uri ) object );
//		}
		
		return statement;
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

//	void build( GroupConstraint groupConstraint, boolean optional )
//	{
//		Set<TripleConstraint> typeConstraints =
//			new HashSet<TripleConstraint>();
//		Set<TripleConstraint> normalConstraints =
//			new HashSet<TripleConstraint>();
//	
//		for ( Object constraint : groupConstraint.getConstraints() )
//		{
//			if ( constraint instanceof TripleConstraint )
//			{
//				if ( this.metaModel.isTypeProperty(
//					this.toUri( ( ( TripleConstraint ) constraint ).
//						getPredicateExpression() ) ) )
//				{
//					typeConstraints.add( ( TripleConstraint ) constraint );
//				}
//				else
//				{
//					normalConstraints.add( ( TripleConstraint ) constraint );
//				}
//			}
//			else if ( constraint instanceof OptionalConstraint )
//			{
//				QueryGraph optionalGraph =
//					new QueryGraph( this.metaModel, this.variableList );
//				optionalGraph.build( ( ( OptionalConstraint )
//					constraint ).getConstraint(), true );
//				this.optionalGraphs.add( optionalGraph );
//			}
//			else
//			{
//				throw new QueryException(
//					"Operation not supported with NeoRdfSource." );
//			}
//		}
//	
//		// Must add types before the other constraints.
//		this.addTypes( typeConstraints, optional );
//		this.addConstraints( normalConstraints, optional );
//	}
//	
//	private void addTypes(
//		Set<TripleConstraint> constraints, boolean optional )
//	{
//		for ( TripleConstraint constraint : constraints )
//		{
//			this.addTypeToPattern( constraint, optional );
//		}
//	}
//
//	private void addTypeToPattern(
//		TripleConstraint constraint, boolean optional )
//	{
//		this.assertConstraint( constraint );
//		PatternNode subjectNode = this.getOrCreatePatternNode(
//			constraint.getSubjectExpression() );
//		PatternNode objectNode = this.getOrCreatePatternNode(
//			constraint.getObjectExpression(), ON_CREATED_TYPE );
//		subjectNode.createRelationshipTo( objectNode,
//			this.metaModel.getTypeRelationship(), optional );
//		String objectUri = this.toUri( constraint.getObjectExpression() );
//		this.classMapping.put( constraint.getSubjectExpression(), objectUri );
//		this.nodeTypes.put( objectUri, objectNode );
//	}
//	
//	private void addConstraints( Set<TripleConstraint> constraints,
//		boolean optional )
//	{
//		for ( TripleConstraint constraint : constraints )
//		{
//			this.addToPattern( constraint, optional );
//		}
//	}
//
//	protected void addToPattern( TripleConstraint constraint,
//		boolean optional )
//	{
//		this.assertConstraint( constraint );
//		this.addOwlProperty( constraint.getSubjectExpression(),
//			constraint.getPredicateExpression(),
//			constraint.getObjectExpression(), optional );
//	}
//
//	private void addOwlProperty( ExpressionLogic subjectExpression,
//		ExpressionLogic predicateExpression, ExpressionLogic objectExpression,
//		boolean optional )
//	{
//		PatternNode subjectNode = this.getOrCreatePatternNode(
//			subjectExpression );
//		
//		OwlProperty property = this.getOwlProperty(
//			subjectExpression, predicateExpression, objectExpression );
//		
//		if ( property.getType() == OwlPropertyType.OBJECT_TYPE )
//		{
//			subjectNode.createRelationshipTo(
//				this.getOrCreatePatternNode( objectExpression ),
//				( RelationshipType ) property.getMappedValue(), optional );
//		}
//		else // It's an OwlProperty.DATATYPE_TYPE
//		{
//			if ( objectExpression instanceof ASTLiteral )
//			{
//				String propertyKey = ( String ) property.getMappedValue();
//				Object valueToMatch =
//					metaModel.convertCriteriaStringValueToRealValue(
//						propertyKey, ( ( ASTLiteral )
//							objectExpression ).getLabel() );
//				subjectNode.addPropertyEqualConstraint(
//					propertyKey, optional, valueToMatch );
//			}
//			else if ( objectExpression instanceof ASTVar )
//			{
//				subjectNode.addPropertyExistConstraint(
//					( String ) property.getMappedValue() );
//				this.addVariable( ( ASTVar ) objectExpression,
//					NeoVariable.VariableType.LITERAL,
//					subjectNode, ( String ) property.getMappedValue() );
//			}
//			else
//			{
//				throw new QueryException( "Object [" + objectExpression +
//				"] should be a literal or a variable." );
//			}
//		}
//	}
//
//
//	private OwlProperty getOwlProperty( ExpressionLogic subjectExpression,
//		ExpressionLogic predicateExpression, ExpressionLogic objectExpression )
//	{
//		if ( objectExpression instanceof ASTVar )
//		{
//			return this.metaModel.getOwlProperty(
//				this.classMapping.get( subjectExpression ),
//				this.toUri( predicateExpression ),
//				this.classMapping.get( objectExpression ) );
//		}
//		else
//		{
//			return this.metaModel.getOwlProperty(
//				this.classMapping.get( subjectExpression ),
//				this.toUri( predicateExpression ), null );
//		}
//	}
//
//	private PatternNode getOrCreatePatternNode( ExpressionLogic expression )
//	{
//		return getOrCreatePatternNode( expression, null );
//	}
//	
//	private PatternNode getOrCreatePatternNode( ExpressionLogic expression,
//		RunOnPatternNode runOnCreation )
//	{
//		PatternNode node = this.graph.get( expression );
//		if ( node == null )
//		{
//			node = this.createPatternNode( expression );
//			if ( runOnCreation != null )
//			{
//				runOnCreation.onCreated( node );
//			}
//		}
//		return node;
//	}
//	
//	private PatternNode getOrCreatePatternNode( String nodeType )
//	{
//		PatternNode node = this.nodeTypes.get( nodeType );
//		if ( node == null )
//		{
//			node = new PatternNode( nodeType );
//			this.nodeTypes.put( nodeType, node );
//			this.possibleStartNodes.add( node );
//		}
//		return node;
//	}
//
//	private PatternNode createPatternNode( ExpressionLogic expression )
//	{
//		PatternNode node =
//			new PatternNode( this.toUri( expression ) );
//		this.graph.put( expression, node );
//		
//		if ( expression instanceof ASTQName )
//		{
//			this.possibleStartNodes.add( node );
//		}
//		
//		if ( expression instanceof ASTVar )
//		{
//			this.addVariable( ( ASTVar ) expression,
//				NeoVariable.VariableType.URI,
//				node, this.metaModel.getAboutKey() );
//		}
//		
//		return node;
//	}
//	
//	private void assertConstraint( TripleConstraint constraint )
//	{
//		if ( !( constraint.getPredicateExpression() instanceof ASTQName ) )
//		{
//			throw new QueryException(
//				"Predicate [" + constraint.getPredicateExpression() +
//				"] is not a fully qualified predicate. Not supported." );
//		}
//	}
//
//	private String toUri( ExpressionLogic expression )
//	{
//		String namespace = "";
//		String localName = "";
//		
//		if ( expression instanceof URINode )
//		{
//			namespace = ( ( URINode ) expression ).getNamespace();
//			localName = ( ( URINode ) expression ).getLocalName();
//		}
//		else if ( expression instanceof ASTVar )
//		{
//			localName = ( ( ASTVar ) expression ).getName();
//		}
//		else if ( expression instanceof ASTLiteral )
//		{
//			localName = ( ( ASTLiteral ) expression ).getLabel();
//		}
//		
//		return namespace + localName;
//	}
//
//	private interface RunOnPatternNode
//	{
//		void onCreated( PatternNode node );
//	}
//	
//	private final RunOnPatternNode ON_CREATED_TYPE =
//		new RunOnPatternNode()
//	{
//		public void onCreated( PatternNode node )
//		{
//			node.addPropertyEqualConstraint( metaModel.getNodeTypeNameKey(),
//				metaModel.getSubTypes( node.getLabel(), true ) );
//		}
//	};
//
//	void assertGraph()
//	{
//		for ( PatternNode node : this.graph.values() )
//		{
//			if ( !this.possibleStartNodes.contains( node ) )
//			{
//				this.assertHasTypeRelationship( node );
//			}
//		}
//	}
//	
//	private PatternRelationship findTypeRelationshipOrNull( PatternNode node )
//	{
//		for ( PatternRelationship relationship : node.getAllRelationships() )
//		{
//			if ( relationship.getType().equals(
//				this.metaModel.getTypeRelationship() ) )
//			{
//				return relationship;
//			}
//		}
//		return null;
//	}
//	
//	private void assertHasTypeRelationship( PatternNode node )
//	{
//		boolean found = findTypeRelationshipOrNull( node ) != null;
//		if ( !found )
//		{
//			found = tryToFindAndAddType( node );
//		}
//		if ( !found )
//		{
//			throw new QueryException( "Type for variable [" + node.getLabel() +
//				"] is not specified. Not supported." );
//		}
//	}
//	
//	private boolean tryToFindAndAddType( PatternNode nodeWithMissingType )
//	{
//		// Try to get the type from proxy...
//		// allright take the first relationship for this node,
//		// go to that other node and get its type relationship to
//		// use you know!
//		PatternRelationship firstRelationshipHack = null;
//		for ( PatternRelationship rel : nodeWithMissingType.getAllRelationships() )
//		{
//			firstRelationshipHack = rel;
//			break;
//		}
//		
//		if ( firstRelationshipHack != null )
//		{
//			PatternNode otherNode =
//				firstRelationshipHack.getOtherNode( nodeWithMissingType );
//			PatternRelationship otherNodesTypeRel =
//				findTypeRelationshipOrNull( otherNode );
//			if ( otherNodesTypeRel != null )
//			{
//				PatternNode otherTypeNode = otherNodesTypeRel.getOtherNode(
//					otherNode );
//				String type = this.metaModel.getObjectType(
//					otherTypeNode.getLabel(),
//					firstRelationshipHack.getType().name() );
//				if ( type != null )
//				{
//					PatternNode typeNode = getOrCreatePatternNode( type );
//					nodeWithMissingType.createRelationshipTo( typeNode,
//						this.metaModel.getTypeRelationship() );
//					return true;
//				}
//			}
//		}
//		return false;
//	}
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