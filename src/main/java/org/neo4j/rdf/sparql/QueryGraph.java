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
import java.util.concurrent.atomic.AtomicInteger;

import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.model.FilterConstraint;
import name.levering.ryan.sparql.model.GroupConstraint;
import name.levering.ryan.sparql.model.OptionalConstraint;
import name.levering.ryan.sparql.model.TripleConstraint;
import name.levering.ryan.sparql.model.logic.ExpressionLogic;
import name.levering.ryan.sparql.parser.model.ASTAndNode;
import name.levering.ryan.sparql.parser.model.ASTEqualsNode;
import name.levering.ryan.sparql.parser.model.ASTGreaterThanEqualsNode;
import name.levering.ryan.sparql.parser.model.ASTGreaterThanNode;
import name.levering.ryan.sparql.parser.model.ASTLessThanEqualsNode;
import name.levering.ryan.sparql.parser.model.ASTLessThanNode;
import name.levering.ryan.sparql.parser.model.ASTLiteral;
import name.levering.ryan.sparql.parser.model.ASTOrNode;
import name.levering.ryan.sparql.parser.model.ASTRegexFuncNode;
import name.levering.ryan.sparql.parser.model.ASTVar;
import name.levering.ryan.sparql.parser.model.BinaryExpressionNode;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.rdf.model.Literal;
import org.neo4j.rdf.model.Statement;
import org.neo4j.rdf.model.Uri;
import org.neo4j.rdf.model.Value;
import org.neo4j.rdf.model.Wildcard;
import org.neo4j.rdf.model.WildcardStatement;
import org.neo4j.rdf.store.RdfStore;
import org.neo4j.rdf.store.representation.AbstractNode;
import org.neo4j.rdf.store.representation.AbstractRelationship;
import org.neo4j.rdf.store.representation.AbstractRepresentation;
import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.neo4j.util.matching.PatternGroup;
import org.neo4j.util.matching.PatternMatcher;
import org.neo4j.util.matching.PatternNode;
import org.neo4j.util.matching.PatternRelationship;
import org.neo4j.util.matching.filter.CompareExpression;
import org.neo4j.util.matching.filter.FilterBinaryNode;
import org.neo4j.util.matching.filter.FilterExpression;
import org.neo4j.util.matching.filter.RegexPattern;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.swami.om2.neorepo.sparql.NeoVariable.VariableType;

/**
 * Builds a pattern with {@link PatternNode} and {@link PatternRelationship}
 * from a SPARQL query, via an {@link AbstractRepresentation} which is retreived
 * from the supplied {@link RepresentationStrategy}. Which in turn belongs
 * to the {@link RdfStore} the query is queried upon.
 * 
 * The resulting pattern can be used to find matches by a
 * {@link PatternMatcher}.
 */
public class QueryGraph
{
    private AtomicInteger blankLabelCounter = new AtomicInteger();
	private List<NeoVariable> variableList;
	private Map<String, ASTVar> astVariables = new HashMap<String, ASTVar>();
	private Map<AbstractNode, PatternNode> graph =
		new HashMap<AbstractNode, PatternNode>();
	private List<Map<AbstractNode, PatternNode>> optionalGraphs =
		new LinkedList<Map<AbstractNode, PatternNode>>();
	private MetaModelProxy metaModel;
	private RepresentationStrategy representationStrategy;
	private PatternGraphBuilder graphBuilder;
    private Set<AbstractNode> possibleStartNodes = new HashSet<AbstractNode>();
    
    QueryGraph( RepresentationStrategy representationStrategy,
        MetaModelProxy metaModel, List<NeoVariable> variableList,
        AtomicInteger blankLabelCounter )
    {
        this( representationStrategy, metaModel, variableList );
        this.blankLabelCounter = blankLabelCounter;
    }

	QueryGraph( RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel, List<NeoVariable> variableList )
	{
		this.variableList = variableList;
		this.metaModel = metaModel;
		this.representationStrategy = representationStrategy;
		this.graphBuilder = new PatternGraphBuilder();
	}

	PatternNodeAndNodePair getStartNode()
	{
		int lowestCount = Integer.MAX_VALUE;
		PatternNode startNode = null;
		Node node = null;
		
		for ( AbstractNode abstractNode : this.possibleStartNodes )
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

	/**
	 * @param firstGraph the first graph.
	 * @param secondGraph the second graph.
	 * @return the node which is the "link" between two graphs, f.ex.
	 * the regular graph and and optional graph.
	 */
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
	    PatternGroup group = new PatternGroup();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		Collection<FilterConstraint> filters =
		    new ArrayList<FilterConstraint>();
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
					new QueryGraph( this.representationStrategy, this.metaModel,
					    this.variableList, this.blankLabelCounter );
				optionalGraph.build( ( ( OptionalConstraint )
					constraint ).getConstraint(), true );
				this.optionalGraphs.add( optionalGraph.getGraph() );
			}
			else if ( constraint instanceof FilterConstraint )
			{
			    filters.add( ( FilterConstraint ) constraint );
			}
			else
			{
				throw new QueryException(
					"Operation not supported with NeoRdfSource." );
			}
		}
		
		AbstractRepresentation representation = new AbstractRepresentation();
		for ( Statement statement : statements )
		{
		    representation = this.representationStrategy.
		        getAbstractRepresentation( statement, representation );
		}
		
		this.graph = this.graphBuilder.buildPatternGraph(
			representation, group, this.variableList, optional );
		
		// Apply filters
		for ( FilterConstraint filter : filters )
		{
		    addFilter( filter, group, optional );
		}
	}
	
	private void addFilter( FilterConstraint constraint, PatternGroup group,
        boolean optional )
    {
	    group.addFilter( toFilterExpression( constraint.getExpression() ) );
    }

    private FilterExpression toFilterExpression( ExpressionLogic expression )
    {
        FilterExpression result = null;
        if ( expression instanceof ASTAndNode ||
            expression instanceof ASTOrNode )
        {
            BinaryExpressionNode binaryNode = ( BinaryExpressionNode )
                expression;
            boolean operatorAnd = expression instanceof ASTAndNode;
            result = new FilterBinaryNode(
                toFilterExpression( binaryNode.getLeftExpression() ),
                operatorAnd,
                toFilterExpression( binaryNode.getRightExpression() ) );
        }
        else
        {
            if ( expression instanceof ASTGreaterThanEqualsNode ||
                expression instanceof ASTEqualsNode ||
                expression instanceof ASTGreaterThanNode ||
                expression instanceof ASTLessThanEqualsNode ||
                expression instanceof ASTLessThanNode )
            {
                result = formCompareExpression( expression );
            }
            else if ( expression instanceof ASTRegexFuncNode )
            {
                result = formRegexPattern( expression );
            }
            else
            {
                throw new RuntimeException( expression +
                    " not supported" );
            }
        }
        return result;
    }

    private FilterExpression formCompareExpression(
        ExpressionLogic expressionLogic )
    {
        BinaryExpressionNode binaryNode =
            ( BinaryExpressionNode ) expressionLogic;
        String operator = binaryNode.getOperator();
        ASTVar var = ( ASTVar ) binaryNode.getLeftExpression();
        ASTLiteral value = ( ASTLiteral ) binaryNode.getRightExpression();
        URI datatype = value.getDatatype();
        Object realValue = null;
        String stringValue = value.toString();
        if ( XMLDatatypeUtil.isDecimalDatatype( datatype ) ||
            XMLDatatypeUtil.isFloatingPointDatatype( datatype ) )
        {
            realValue = new Double( stringValue );
        }
        else if ( XMLDatatypeUtil.isIntegerDatatype( datatype ) )
        {
            realValue = new Integer( stringValue );
        }
        else
        {
            realValue = value.getLabel();
        }

        NeoVariable variable = getVariable( var );
        return new CompareExpression( var.getName(), variable.getProperty(),
            operator, realValue );
    }

    private FilterExpression formRegexPattern( ExpressionLogic expressionLogic )
    {
        ASTRegexFuncNode regexNode = ( ASTRegexFuncNode ) expressionLogic;
        List<?> arguments = regexNode.getArguments();
        ASTVar variable = ( ASTVar ) arguments.get( 0 );
        ASTLiteral regexValue = ( ASTLiteral ) arguments.get( 1 );
        ASTLiteral regexOptions = arguments.size() > 2 ?
            ( ASTLiteral ) arguments.get( 2 ) : null;
        NeoVariable neoVariable = getVariable( variable );
        return new RegexPattern( variable.getName(),
            neoVariable.getProperty(), regexValue.getLabel(),
            regexOptions == null ? "" : regexOptions.getLabel() );
    }

    private NeoVariable getVariableOrNull( ASTVar var )
    {
        for ( NeoVariable variable : this.variableList )
        {
            if ( var.getName().equals( variable.getName() ) )
            {
                return variable;
            }
        }
        return null;
    }
    
    private NeoVariable getVariable( ASTVar var )
    {
        NeoVariable variable = getVariableOrNull( var );
        if ( variable == null )
        {
            throw new RuntimeException( "Undefined variable for " + var );
        }
        return variable;
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
		
		return new WildcardStatement( subject, predicate, object,
			new Wildcard( "context" ) );
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

	private class PatternGraphBuilder
	{
	    public Map<AbstractNode, PatternNode> buildPatternGraph(
	        AbstractRepresentation representation, PatternGroup group,
	        List<NeoVariable> variableMapping )
	    {
	        return buildPatternGraph( representation, group,
	            variableMapping, false );
	    }
	    
	    public Map<AbstractNode, PatternNode> buildPatternGraph(
	        AbstractRepresentation representation, PatternGroup group,
	        List<NeoVariable> variableMapping, boolean optional )
	    {
	        for ( AbstractNode node : representation.nodes() )
	        {
	            PatternNode patternNode = this.createPatternNode( node, group );
	            graph.put( node, patternNode );
	            
	            // If this is an rdf:type node then it's a possible start node
	            if ( node.getUriOrNull() != null )
//	            if ( node.getUriOrNull() != null && metaModel.isTypeProperty(
//	                node.getUriOrNull().getUriAsString() ) )
	            {
	                possibleStartNodes.add( node );
	            }
	        }
	        
	        for ( AbstractRelationship relationship :
	            representation.relationships() )
	        {
	            AbstractNode startNode = relationship.getStartNode();
	            AbstractNode endNode = relationship.getEndNode();
	            final String name = relationship.getRelationshipTypeName();
	            
	            RelationshipType relType = new RelationshipType()
	            {
	                public String name()
	                {
	                    return name;
	                }
	            };
	            
	            graph.get( startNode ).createRelationshipTo(
	                graph.get( endNode ), relType, optional );
	        }
	        
	        return graph;
	    }
	    
	    private PatternNode createPatternNode( AbstractNode node,
	        PatternGroup group )
	    {
	        PatternNode patternNode = null;
	        if ( node.isWildcard() )
	        {
	            Wildcard wildcard = node.getWildcardOrNull();
	            patternNode =
	                new PatternNode( group, wildcard.getVariableName() );
	            this.addVariable( wildcard.getVariableName(), VariableType.URI,
	                patternNode, representationStrategy.getExecutor().
	                    getNodeUriPropertyKey( node ) );
	        }
	        else
	        {
	            Uri uri = node.getUriOrNull();
	            patternNode = new PatternNode( group, uri == null ?
	                ( "_blank_" + blankLabelCounter.incrementAndGet() ) :
	                uri.getUriAsString() );
	            if ( uri != null )
	            {
	                patternNode.addPropertyEqualConstraint(
	                    representationStrategy.getExecutor().
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
	                    this.addVariable( ( ( Wildcard ) value ).
	                        getVariableName(), VariableType.LITERAL,
	                        patternNode, entry.getKey() );
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
	        for ( NeoVariable variable : variableList )
	        {
	            if ( variableName.equals( variable.getName() ) )
	            {
	                return;
	            }
	        }
	        variableList.add(
	            new NeoVariable( variableName, type, patternNode, property ) );
	    }
	}
}
