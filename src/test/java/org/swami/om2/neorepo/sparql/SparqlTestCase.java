package org.swami.om2.neorepo.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import name.levering.ryan.sparql.common.RdfBindingRow;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.Variable;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;

public abstract class SparqlTestCase extends TestCase
{
	final static String RDF_NAMESPACE =
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	final static String LADOK_NAMESPACE =
		"http://www.swami.se/om2/ladok-1.owl#";
	final static String PRIM_NAMESPACE =
		"http://www.openmetadir.org/om2/prim-1.owl#";
	final static String FOAF_NAMESPACE =
		"http://xmlns.com/foaf/1.0/";
	protected EmbeddedNeo neo;
	protected MetaModelMockUp metaModel;
	private Set<Node> createdNodes;
	
	public SparqlTestCase( String name, MetaModelMockUp metaModel )
	{
		super( name );
		this.createdNodes = new HashSet<Node>();
		this.metaModel = metaModel;
	}
	
	public void setUp(
		Class<? extends RelationshipType> relationshipTypeClass )
	{
		neo = new EmbeddedNeo( "var-unit-tests" );
	}
	
	@Override
	public void tearDown()
	{
		Transaction tx = Transaction.begin();
		try
		{
			this.deleteAllNodes();
			tx.success();
		}
		finally
		{
			tx.finish();
		}
		neo.shutdown();
	}
	
	protected Node createNode( String name )
	{
		return this.createNode( name, null );
	}
	
	protected Node createNode( String name, Node referenceNode )
	{
		Node node = this.neo.createNode();
		node.setProperty( this.metaModel.getAboutKey(), name );
		if ( referenceNode != null )
		{
			node.createRelationshipTo( referenceNode,
				this.metaModel.getTypeRelationship() );
		}
		this.createdNodes.add( node );
		return node;
	}
	
	protected Node createReferenceNode( String name, String uri )
	{
		Node referenceNode = this.createNode( name );
		referenceNode.setProperty( this.metaModel.getNodeTypeNameKey(), uri );
		this.metaModel.addClassNode( uri, referenceNode );
		return referenceNode;
	}
	
	protected void assertResult( RdfBindingSet result,
		Map<String, Integer> variables, String[][] expectedResult )
	{
		Iterator<RdfBindingRow> iterator = result.iterator();
		
		assertTrue( ( expectedResult != null && iterator.hasNext() ) ||
			( expectedResult == null && !iterator.hasNext() ) );
		
		int i = 0;
		int j = 0;
		while ( iterator.hasNext() )
		{
			RdfBindingRow row = iterator.next();

			for ( Variable variable : ( List<Variable> ) row.getVariables() )
			{
				int column = variables.get( variable.getName() );
				Object value = row.getValue( variable );
				assertTrue( "Incorrect result.",
					expectedResult[i][column].equals( value.toString() ) );
				j++;
			}
			
			assertTrue( "Wrong number of variables were bound.",
				j == expectedResult[i].length );
			j = 0;
			i++;
		}

		if ( expectedResult != null )
		{
			assertTrue( "Wrong number of matches were found.",
				i == expectedResult.length );
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
			System.out.println( "found match" );
			NeoBindingRow match = matches.next();
			this.printMatch( match );
		}
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
