package org.swami.om2.neorepo.sparql;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.model.SelectQuery;
import name.levering.ryan.sparql.parser.ParseException;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.swami.om2.neorepo.sparql.MetaModelProxy.OwlPropertyType;


public class SimpleTest extends SparqlTestCase
{
	private Node a, b, c, d, e;
	private Node person;

	private static Map<String, OwlPropertyType> types =
		new HashMap<String, OwlPropertyType>();
	private static Map<String, Object> values = new HashMap<String, Object>();
	private static Map<String, Integer> counts =
		new HashMap<String, Integer>();
	static
	{
		types.put( FOAF_NAMESPACE + "knows", OwlPropertyType.OBJECT_TYPE );
		types.put( FOAF_NAMESPACE + "name", OwlPropertyType.DATATYPE_TYPE );
		values.put( RDF_NAMESPACE + "type", MyRelationshipType.INSTANCE_OF );
		values.put( FOAF_NAMESPACE + "knows", MyRelationshipType.KNOWS );
		values.put( FOAF_NAMESPACE + "name", "name" );
		counts.put( FOAF_NAMESPACE + "Person", new Integer( 10 ) );
	}

	public static enum MyRelationshipType implements RelationshipType
	{
		KNOWS,
		INSTANCE_OF
	}
	
	public SimpleTest( String name )
	{
		super( name, new MetaModelMockUp(
			types, values, counts ) );
	}
	
	@Override
	public void setUp()
	{
		super.setUp();

		Transaction tx = Transaction.begin();
		try
		{
			person = this.createReferenceNode(
				"person", FOAF_NAMESPACE + "Person" );
			a = this.createNode( "a", person );
			b = this.createNode( "b", person );
			c = this.createNode( "c", person );
			d = this.createNode( "d", person );
			e = this.createNode( "e", person );
			
			a.createRelationshipTo( b, MyRelationshipType.KNOWS );
			a.createRelationshipTo( d, MyRelationshipType.KNOWS );
			
			b.createRelationshipTo( c, MyRelationshipType.KNOWS );
			
			c.createRelationshipTo( e, MyRelationshipType.KNOWS );
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void testSimpleSelectQuery() throws ParseException
	{
		Transaction tx = Transaction.begin();
		try
		{
			Set<String> variables = new HashSet<String>();
			variables.add( "person" );
			variables.add( "friendOfFriend" );
			Set<String> personValues = new HashSet<String>();
			personValues.add( "a" );
			personValues.add( "b" );
			Set<String> friendOfFriendValues = new HashSet<String>();
			friendOfFriendValues.add( "c" );
			friendOfFriendValues.add( "e" );
			
			try
			{
				Query query = this.sparqlEngine.parse( new StringReader(
					"PREFIX foaf: <http://xmlns.com/foaf/1.0/> " + 
					"SELECT ?person ?friendOfFriend " +
					"WHERE { ?person foaf:knows ?x . " +
					        "?x foaf:knows ?friendOfFriend }" ) );
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
				fail( "No types specified. Should be rejected." );
			}
			catch ( Exception e )
			{
				// Ok.
			}
			
			Query query = this.sparqlEngine.parse( new StringReader(
				"PREFIX foaf: <http://xmlns.com/foaf/1.0/> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?person ?friendOfFriend " +
				"WHERE { ?person foaf:knows ?x . " +
						"?x rdf:type foaf:Person . " +
				        "?x foaf:knows ?friendOfFriend . " +
				        "?person rdf:type foaf:Person . " +
				        "?friendOfFriend rdf:type foaf:Person . }" ) );
			
			if ( query instanceof SelectQuery )
			{
				RdfBindingSet result = ( ( SelectQuery ) query ).execute(
					new NeoRdfSource() );

//				Iterator iterator = result.iterator();
//				while ( iterator.hasNext() )
//				{
//					RdfBindingRow bindingRow =
//						( RdfBindingRow ) iterator.next();
//					assert( bindingRow.getVariables().size() == 2 );
//					assert( bindingRow.getValues().size() == 2 );
//					for ( Variable variable :
//						( List<Variable> ) bindingRow.getVariables() )
//					{
//						assert( variables.contains( variable.getName() ) );
//						Literal literal =
//							( Literal ) bindingRow.getValue( variable );
//
//						if ( variable.getName().equals( "person" ) )
//						{
//							assert( personValues.contains(
//								literal.getLabel() ) );
//						}
//						else
//						{
//							assert( friendOfFriendValues.contains(
//								literal.getLabel() ) );
//						}
//					}
//				}
			}
			else
			{
				fail( "Query is not an instance of SelectQuery" );
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
}
