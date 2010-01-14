package org.neo4j.rdf.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.levering.ryan.sparql.common.LenientStatement;
import name.levering.ryan.sparql.common.RdfBindingRow;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.Variable;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rdf.store.representation.AbstractNode;
import org.neo4j.rdf.store.representation.RepresentationStrategy;

public abstract class SparqlTestCase extends NeoWithIndexTestCase
{
	final static String RDF_NAMESPACE =
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	final static String LADOK_NAMESPACE =
		"http://www.swami.se/om2/ladok-1.owl#";
	final static String PRIM_NAMESPACE =
		"http://www.openmetadir.org/om2/prim-1.owl#";
	final static String FOAF_NAMESPACE =
		"http://xmlns.com/foaf/1.0/";
	
	private RepresentationStrategy representationStrategy;
	protected NeoSparqlEngine sparqlEngine;
	protected MetaModelMockUp metaModel;
	private Set<Node> createdNodes = new HashSet<Node>();
	
	@Override
	public void tearDown() throws Exception
	{
		this.deleteAllNodes();
		super.tearDown();
	}
	
	protected MetaModelMockUp metaModelProxy()
	{
	    if ( this.metaModel == null )
	    {
	        this.metaModel = instantiateMetaModelProxy();
	    }
	    return this.metaModel;
	}
	
	protected abstract MetaModelMockUp instantiateMetaModelProxy();
	
	protected NeoSparqlEngine sparqlEngine()
	{
	    if ( this.sparqlEngine == null )
	    {
	        this.sparqlEngine = instantiateSparqlEngine();
	    }
	    return this.sparqlEngine;
	}
	
	protected abstract NeoSparqlEngine instantiateSparqlEngine();
	
	protected RepresentationStrategy representationStrategy()
	{
	    if ( this.representationStrategy == null )
	    {
	        this.representationStrategy = instantiateRepresentationStrategy();
	    }
	    return this.representationStrategy;
	}
	
	protected abstract RepresentationStrategy
	    instantiateRepresentationStrategy();
	
	protected Node createNode( String name )
	{
		return this.createNode( name, null );
	}
	
	protected Node createNode( String name, Node referenceNode )
	{
		Node node = neo().createNode();
		node.setProperty( representationStrategy().getExecutor().
		    getNodeUriPropertyKey( new AbstractNode( null ) ), name );
		if ( referenceNode != null )
		{
			node.createRelationshipTo( referenceNode, new RelationshipType()
			{
                public String name()
                {
                    return RDF_NAMESPACE + "type";
                }
			} );
		}
		this.createdNodes.add( node );
		return node;
	}
	
	protected Node createReferenceNode( String name, String uri )
	{
		Node referenceNode = this.createNode( name );
		referenceNode.setProperty( representationStrategy().getExecutor().
		    getNodeUriPropertyKey( new AbstractNode( null ) ), uri );
		metaModelProxy().addClassNode( uri, referenceNode );
		return referenceNode;
	}
	
	protected void assertResult( NeoRdfGraph result,
	    List<String> expectedResult )
	{
		Iterator<LenientStatement> statements = result.iterator();
		
		assertTrue( ( expectedResult != null && statements.hasNext() ) ||
			( expectedResult == null && !statements.hasNext() ) );
		
		int matchesFound = 0;
		for ( LenientStatement statement : result )
		{
		    boolean match = false;
		    for ( String oneExpectedResult : expectedResult )
		    {
		        if ( statement.toString().equals( oneExpectedResult ) )
		        {
		            match = true;
		            break;
		        }
		    }
            assertTrue( "Expected result not found " + statement, match );
			matchesFound++;
		}

		if ( expectedResult != null )
		{
			assertTrue( "Wrong number of matches were found.",
				matchesFound == expectedResult.size() );
		}
	}
	
	protected void assertResult( RdfBindingSet result,
		Map<String, Integer> variables, List<List<String>> expectedResult )
	{
		Iterator<RdfBindingRow> iterator = result.iterator();
		
		assertTrue( ( expectedResult != null && iterator.hasNext() ) ||
			( expectedResult == null && !iterator.hasNext() ) );
		
		int matchesFound = 0;
		while ( iterator.hasNext() )
		{
			RdfBindingRow row = iterator.next();
            boolean match = false;
			for ( List<String> oneExpectedResult : expectedResult )
			{
			    match = true;
                int numberOfBoundVariables = 0;
    			for ( Variable variable :
    			    ( List<Variable> ) row.getVariables() )
    			{
    				int column = variables.get( variable.getName() );
    				Object value = row.getValue( variable ).toString();
    				match = oneExpectedResult.get( column ).toString().equals(
    				    value );
    				if ( !match )
    				{
    				    break;
    				}
    				numberOfBoundVariables++;
    			}
    			if ( numberOfBoundVariables != oneExpectedResult.size() )
    			{
    			    match = false;
    			}
    			if ( match )
    			{
    			    break;
    			}
			}

			assertTrue( "Expected result for " + row + " not found", match );
			matchesFound++;
		}

		if ( expectedResult != null )
		{
			assertTrue( "Wrong number of matches were found.",
				matchesFound == expectedResult.size() );
		}
	}
	
	protected Map<String, Integer> createVariableMap( String... variableNames )
	{
		Map<String, Integer> result = new HashMap<String, Integer>();
		for ( int i = 0; i < variableNames.length; i++ )
		{
			result.put( variableNames[ i ], i );
		}
		return result;
	}
	
	private void deleteNode( Node node )
	{
		for ( Relationship relationship : node.getRelationships() )
		{
			relationship.delete();
		}
		node.delete();
	}
	
	protected void deleteAllNodes()
	{
		Iterator<Node> iterator = this.createdNodes.iterator();
		while ( iterator.hasNext() )
		{
			this.deleteNode( iterator.next() );
		}
	}
	
	protected void printNeoBindingSet( RdfBindingSet bindingSet )
	{
		NeoRdfBindingSet neoBindingSet = ( NeoRdfBindingSet ) bindingSet;
		
		Iterator<NeoBindingRow> matches = neoBindingSet.iterator();
		System.out.println( "printing matches");
		while ( matches.hasNext() )
		{
			NeoBindingRow match = matches.next();
			this.printMatch( match );
		}
	}
	
	protected void printRdfGraph( NeoRdfGraph graph )
	{
		System.out.println( "Graph: " );
		for ( LenientStatement statement : graph )
		{
			System.out.println( statement.getSubject() + " " + 
				statement.getPredicate() + " " + statement.getObject() );
		}
		System.out.println( "end of graph" );
	}

	protected void printMatch( NeoBindingRow match )
	{
		System.out.println( "Match:" );
		for ( Variable variable : match.getVariables() )
		{
			System.out.println( "\nPN: " + variable.getName() + ", N: " +
				match.getValue( variable ) );
		}
	}
}
