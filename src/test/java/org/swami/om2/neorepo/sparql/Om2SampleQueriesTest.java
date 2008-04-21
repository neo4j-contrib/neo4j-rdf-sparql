package org.swami.om2.neorepo.sparql;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.logic.SPARQLQueryLogic;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.model.SelectQuery;
import name.levering.ryan.sparql.parser.SPARQLParser;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.swami.om2.neorepo.sparql.MetaModelProxy.OwlPropertyType;

public class Om2SampleQueriesTest extends SparqlTestCase
{
	public static enum Om2RelationshipType implements RelationshipType
	{
		INSTANCE_OF,
		ONE,
		OTHER
	}
	
	private static Map<String, OwlPropertyType> types =
		new HashMap<String, OwlPropertyType>();
	private static Map<String, Object> values = new HashMap<String, Object>();
	private static Map<String, Integer> counts =
		new HashMap<String, Integer>();
	static
	{
		types.put( RDF_NAMESPACE + "about", OwlPropertyType.DATATYPE_TYPE );
		types.put( PRIM_NAMESPACE + "one", OwlPropertyType.OBJECT_TYPE );
		types.put( PRIM_NAMESPACE + "other", OwlPropertyType.OBJECT_TYPE );
		types.put( PRIM_NAMESPACE + "name", OwlPropertyType.DATATYPE_TYPE );
		types.put( LADOK_NAMESPACE + "state", OwlPropertyType.DATATYPE_TYPE );
		types.put( LADOK_NAMESPACE + "courseId",
			OwlPropertyType.DATATYPE_TYPE );
		
		values.put( RDF_NAMESPACE + "type", Om2RelationshipType.INSTANCE_OF );
		values.put( RDF_NAMESPACE + "about", "about" );
		values.put( PRIM_NAMESPACE + "one", Om2RelationshipType.ONE );
		values.put( PRIM_NAMESPACE + "other", Om2RelationshipType.OTHER );
		values.put( PRIM_NAMESPACE + "name", "name" );
		values.put( LADOK_NAMESPACE + "state", "state" );
		values.put( LADOK_NAMESPACE + "courseId", "courseId" );
		
		counts.put( PRIM_NAMESPACE + "Person", new Integer( 10 ) );
		counts.put( PRIM_NAMESPACE + "Department", new Integer( 2 ) );
		counts.put( PRIM_NAMESPACE + "Responsible", new Integer( 3 ) );
		counts.put( LADOK_NAMESPACE + "Student", new Integer( 10 ) );
		counts.put( LADOK_NAMESPACE + "CourseInstance", new Integer( 5 ) );
	}
	
	public Om2SampleQueriesTest( String name )
	{
		super( name, new MetaModelMockUp(
			types, values, counts, Om2RelationshipType.INSTANCE_OF ) );
	}
	
	Node studentReferenceNode;
	Node studentA; // other and takes courseA/courseE and is registered
	Node studentB; // no other
	Node studentC; // other and takes courseA and is registered
	Node studentD; // other and takes courseB and is registered
	Node studentE; // other and takes courseA and is accepted
	Node studentF; // other and takes CourseB and is accepted
	Node personReferenceNode;
	Node personA;
	Node personB;
	Node personC;
	Node personD;
	Node personE;
	Node personF;
	Node departmentReferenceNode;
	Node departmentA; // responsible for courseA and courseD
	Node departmentB; // responsible for courseB and courseC
	Node responsibleReferenceNode;
	Node responsibleA;
	Node responsibleB;
	Node courseReferenceNode;
	Node courseA; // about:"28040ht06", run by the "Psykologi" department
	Node courseB; // courseId: "KOSB15"
	Node courseC;
	Node courseD; // courseId: "TMHB21", run by the "Psykologi" department
	Node courseE;
	
	@Override
	public void setUp()
	{
		super.setUp( Om2RelationshipType.class );
		
		Transaction tx = Transaction.begin();
		try
		{
			studentReferenceNode = this.createReferenceNode(
				"studentReferenceNode", LADOK_NAMESPACE + "Student" );
			
			studentA = this.createNode( "studentA", studentReferenceNode );
			studentB = this.createNode( "studentB", studentReferenceNode );
			studentC = this.createNode( "studentC", studentReferenceNode );
			studentD = this.createNode( "studentD", studentReferenceNode );
			studentE = this.createNode( "studentE", studentReferenceNode );
			studentF = this.createNode( "studentF", studentReferenceNode );
			
			personReferenceNode = this.createReferenceNode(
				"personReferenceNode", PRIM_NAMESPACE + "Person" );
			personA = this.createNode( "personA", personReferenceNode );
			personB = this.createNode( "personB", personReferenceNode );
			personC = this.createNode( "personC", personReferenceNode );
			personD = this.createNode( "personD", personReferenceNode );
			personE = this.createNode( "personE", personReferenceNode );
			personF = this.createNode( "personF", personReferenceNode );
			
			courseReferenceNode = this.createReferenceNode(
				"courseReferenceNode", LADOK_NAMESPACE + "CourseInstance" );
			courseA = this.createNode( "28040ht06", courseReferenceNode );
			courseB = this.createNode( "courseB", courseReferenceNode );
			courseB.setProperty( "courseId", "KOSB15" );
			courseC = this.createNode( "courseC", courseReferenceNode );
			courseD = this.createNode( "courseD", courseReferenceNode );
			courseD.setProperty( "courseId", "TMHB21" );
			courseE = this.createNode( "courseE", courseReferenceNode );
			
			departmentReferenceNode = this.createReferenceNode(
				"departmentReferenceNode", PRIM_NAMESPACE + "Department" );
			departmentA = this.createNode(
				"departmentA", departmentReferenceNode );
			departmentA.setProperty( "name", "Psykologi" );
			departmentB = this.createNode(
				"departmentB", departmentReferenceNode );
			
			responsibleReferenceNode = this.createReferenceNode(
				"responsibleReferenceNode", PRIM_NAMESPACE + "Responsible" );
			responsibleA = this.createNode(
				"responsibleA", responsibleReferenceNode );
			responsibleB = this.createNode(
				"responsibleB", responsibleReferenceNode );
			
			responsibleA.createRelationshipTo(
				departmentA, Om2RelationshipType.ONE );
			responsibleB.createRelationshipTo(
				departmentB, Om2RelationshipType.ONE );
			responsibleA.createRelationshipTo(
				courseA, Om2RelationshipType.OTHER );
			responsibleA.createRelationshipTo(
				courseD, Om2RelationshipType.OTHER );
			responsibleB.createRelationshipTo(
				courseB, Om2RelationshipType.OTHER );
			responsibleB.createRelationshipTo(
				courseC, Om2RelationshipType.OTHER );
			
			
			studentA.createRelationshipTo( personA, Om2RelationshipType.ONE );
			studentB.createRelationshipTo( personB, Om2RelationshipType.ONE );
			studentC.createRelationshipTo( personC, Om2RelationshipType.ONE );
			studentD.createRelationshipTo( personD, Om2RelationshipType.ONE );
			studentE.createRelationshipTo( personE, Om2RelationshipType.ONE );
			studentF.createRelationshipTo( personF, Om2RelationshipType.ONE );
			
			studentA.setProperty( "state", "registered" );
			studentC.setProperty( "state", "registered" );
			studentD.setProperty( "state", "registered" );
			studentE.setProperty( "state", "accepted" );
			studentF.setProperty( "state", "accepted" );
			
			studentA.createRelationshipTo(
				courseA, Om2RelationshipType.OTHER );
			studentA.createRelationshipTo(
				courseE, Om2RelationshipType.OTHER );
			studentC.createRelationshipTo(
				courseA, Om2RelationshipType.OTHER );
			studentD.createRelationshipTo(
				courseB, Om2RelationshipType.OTHER );
			studentE.createRelationshipTo(
				courseA, Om2RelationshipType.OTHER );
			studentF.createRelationshipTo(
				courseB, Om2RelationshipType.OTHER );
			
			SPARQLQueryLogic.getInstance().setLogicFactory(
				new NeoLogic( this.metaModel ) );

			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void testQuery1() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Which students are registered on the course 28040ht06?
			Query query = SPARQLParser.parse( new StringReader(
				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
				"PREFIX d: <#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?student ?person " +
				"WHERE { " +
				"?student rdf:type ladok:Student . " +
				"?student prim:one ?person . " +
				"?person rdf:type prim:Person . " +
				"?student ladok:state \"registered\" . " +
				"?student prim:other ?x . " +
				"?x rdf:about \"28040ht06\" ." +
				"?x rdf:type ladok:CourseInstance . }" ) );
//				"?student prim:other d:28040ht06 . " +
//				"d:28040ht06 rdf:type ladok:CourseInstance . }" ) );
				
			RdfBindingSet result =
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );

			Map<String, Integer> variables =
				this.createVariableMap( "student", "person" );
			String[][] expectedResult = new String[][] { 
				{ "studentA", "personA" },
				{ "studentC", "personC" } };
			this.assertResult( result, variables, expectedResult );
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	public void testQuery2() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Which students are accepted to the course KOSB15?
			Query query = SPARQLParser.parse( new StringReader(
				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?student ?person ?course " +
				"WHERE { " +
				"?student rdf:type ladok:Student . " +
				"?student prim:one ?person . " +
				"?person rdf:type prim:Person . " +
				"?student ladok:state \"accepted\" . " +
				"?student prim:other ?course . " +
				"?course rdf:type ladok:CourseInstance . " +
				"?course ladok:courseId \"KOSB15\" . } " ) );
				
			RdfBindingSet result =
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
			
			Map<String, Integer> variables =
				this.createVariableMap( "student", "person", "course" );
			String[][] expectedResult =
				new String[][] { { "studentF", "personF", "courseB" } };
			this.assertResult( result, variables, expectedResult );
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	public void testQuery3() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Which courses are the Psychology department responsible for?
			Query query = SPARQLParser.parse( new StringReader(
				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?responsible ?course ?department " +
				"WHERE { " +
				"?responsible rdf:type prim:Responsible . " +
				"?responsible prim:other ?course . " +
				"?course rdf:type ladok:CourseInstance . " +
				"?responsible prim:one ?department . " +
				"?department rdf:type prim:Department . " +
				"?department prim:name \"Psykologi\" } " ) );			
			RdfBindingSet result =
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
			
			Map<String, Integer> variables = this.createVariableMap(
				"responsible", "course", "department" );
			String[][] expectedResult = new String[][] {
				{ "responsibleA", "28040ht06", "departmentA" },
				{ "responsibleA", "courseD", "departmentA" } };
			this.assertResult( result, variables, expectedResult );

			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	public void testQuery4() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Which department is responsible for the course TMHB21?
			Query query = SPARQLParser.parse( new StringReader(
				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?responsible ?course ?department ?name " +
				"WHERE { " +
				"?responsible rdf:type prim:Responsible . " +
				"?responsible prim:other ?course . " +
				"?course rdf:type ladok:CourseInstance . " +
				"?course ladok:courseId \"TMHB21\" . " +
				"?responsible prim:one ?department . " +
				"?department rdf:type prim:Department . " +
				"?department prim:name ?name }" ) ); 
			RdfBindingSet result =
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
			
			Map<String, Integer> variables = this.createVariableMap(
				"responsible", "course", "department", "name" );
			String[][] expectedResult = new String[][] {
				{ "responsibleA", "courseD", "departmentA", "Psykologi" } };
			this.assertResult( result, variables, expectedResult );

			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void testQuery5() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Which department is responsible for the non-existant course
			// TDDB56?
			Query query = SPARQLParser.parse( new StringReader(
				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?responsible ?course ?department ?name " +
				"WHERE { " +
				"?responsible rdf:type prim:Responsible . " +
				"?responsible prim:other ?course . " +
				"?course rdf:type ladok:CourseInstance . " +
				"?course ladok:courseId \"TDDB56\" . " +
				"?responsible prim:one ?department . " +
				"?department rdf:type prim:Department . " +
			"?department prim:name ?name }" ) ); 
			RdfBindingSet result =
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
			
			this.assertResult( result, null, null );
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void testQuery6() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Get all students
			// Optional: The person the student is connected to
			// Optional: Every course the student takes
			// Optional: The student's registration status
			Query query = SPARQLParser.parse( new StringReader(
				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?student ?person ?course ?state " +
				"WHERE { " +
				"?student rdf:type ladok:Student . " +
				"OPTIONAL { ?student prim:other ?course . " +
				"?course rdf:type ladok:CourseInstance } " +
				"OPTIONAL { ?student prim:one ?person . " +
				"?person rdf:type prim:Person } " +
				"OPTIONAL { ?student ladok:state ?state } " +
				"}" ) );
				
			RdfBindingSet result =
				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
			
			Map<String, Integer> variables =
				this.createVariableMap( "student", "person", "course", "state" );
			String[][] expectedResult = new String[][] {
				{ "studentA", "personA", "28040ht06", "registered" },
				{ "studentA", "personA", "courseE", "registered" },
				{ "studentB", "personB", "", "" },
				{ "studentC", "personC", "28040ht06", "registered" },
				{ "studentD", "personD", "courseB", "registered" },
				{ "studentE", "personE", "28040ht06", "accepted" },
				{ "studentF", "personF", "courseB", "accepted" } };
			this.assertResult( result, variables, expectedResult );
			studentA.setProperty( "state", "registered" );
			studentC.setProperty( "state", "registered" );
			studentD.setProperty( "state", "registered" );
			studentE.setProperty( "state", "accepted" );
			studentF.setProperty( "state", "accepted" );
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
}
