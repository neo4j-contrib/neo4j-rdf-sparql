package org.swami.om2.neorepo.sparql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.model.SelectQuery;
import org.neo4j.api.core.Transaction;
import org.neo4j.rdf.model.CompleteStatement;
import org.neo4j.rdf.model.Literal;
import org.neo4j.rdf.model.Resource;
import org.neo4j.rdf.model.Statement;
import org.neo4j.rdf.model.Uri;
import org.neo4j.rdf.store.RdfStore;
import org.neo4j.rdf.store.RdfStoreImpl;
import org.neo4j.rdf.store.representation.DenseRepresentationStrategy;
import org.swami.om2.neorepo.sparql.MetaModelProxy.OwlPropertyType;

public class Om2SampleQueriesTest extends SparqlTestCase
{
//	public static class Om2RelationshipType implements RelationshipType
//	{
//		final static Om2RelationshipType INSTANCE_OF =
//			new Om2RelationshipType( "META_INSTANCE_OF" );
//		final static Om2RelationshipType ONE = new Om2RelationshipType(
//			"http://www.openmetadir.org/om2/prim-1.owl#one" );
//		final static Om2RelationshipType OTHER = new Om2RelationshipType(
//			"http://www.openmetadir.org/om2/prim-1.owl#other" );
//		
//		private String name;
//		
//		public Om2RelationshipType( String name )
//		{
//			this.name = name;
//		}
//		
//		public String name()
//		{
//			return name;
//		}
//	}
	
	interface Predicates
	{
		String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String COURSE_ID = "http://www.swami.se/om2/ladok-1.owl#courseId";
		String NAME = "http://www.openmetadir.org/om2/prim-1.owl#name";
		String ONE = "http://www.openmetadir.org/om2/prim-1.owl#one";
		String OTHER = "http://www.openmetadir.org/om2/prim-1.owl#other";
		String STATE = "http://www.swami.se/om2/ladok-1.owl#state";
	}
	
	interface Types
	{
		Uri STUDENT = new Uri(
			"http://www.swami.se/om2/ladok-1.owl#Student" );
		Uri PERSON = new Uri(
			"http://www.openmetadir.org/om2/prim-1.owl#Person" );
		Uri COURSE = new Uri(
			"http://www.swami.se/om2/ladok-1.owl#CourseInstance" );
		Uri DEPARTMENT = new Uri(
			"http://www.openmetadir.org/om2/prim-1.owl#Department" );
		Uri RESPONSIBLE = new Uri(
			"http://www.openmetadir.org/om2/prim-1.owl#Responsible" );
	}
	
	private static Map<String, OwlPropertyType> types =
		new HashMap<String, OwlPropertyType>();
	private static Map<String, Object> values = new HashMap<String, Object>();
	private static Map<String, Integer> counts =
		new HashMap<String, Integer>();
	private RdfStore rdfStore;
	static
	{
//		types.put( RDF_NAMESPACE + "about", OwlPropertyType.DATATYPE_TYPE );
//		types.put( PRIM_NAMESPACE + "one", OwlPropertyType.OBJECT_TYPE );
//		types.put( PRIM_NAMESPACE + "other", OwlPropertyType.OBJECT_TYPE );
//		types.put( PRIM_NAMESPACE + "name", OwlPropertyType.DATATYPE_TYPE );
//		types.put( LADOK_NAMESPACE + "state", OwlPropertyType.DATATYPE_TYPE );
//		types.put( LADOK_NAMESPACE + "courseId",
//			OwlPropertyType.DATATYPE_TYPE );
		
//		values.put( RDF_NAMESPACE + "type", Om2RelationshipType.INSTANCE_OF );
//		values.put( RDF_NAMESPACE + "about", "about" );
//		values.put( PRIM_NAMESPACE + "one", Om2RelationshipType.ONE );
//		values.put( PRIM_NAMESPACE + "other", Om2RelationshipType.OTHER );
//		values.put( PRIM_NAMESPACE + "name", "name" );
//		values.put( LADOK_NAMESPACE + "state", "state" );
//		values.put( LADOK_NAMESPACE + "courseId", "courseId" );
		
		counts.put( PRIM_NAMESPACE + "Person", new Integer( 10 ) );
		counts.put( PRIM_NAMESPACE + "Department", new Integer( 2 ) );
		counts.put( PRIM_NAMESPACE + "Responsible", new Integer( 3 ) );
		counts.put( LADOK_NAMESPACE + "Student", new Integer( 10 ) );
		counts.put( LADOK_NAMESPACE + "CourseInstance", new Integer( 5 ) );
	}
	
	public Om2SampleQueriesTest( String name )
	{
		super( name, new MetaModelMockUp( types, values, counts ) );
	}
	
//	Node studentReferenceNode;
//	Node studentA; // other and takes courseA/courseE and is registered
//	Node studentB; // no other
//	Node studentC; // other and takes courseA and is registered
//	Node studentD; // other and takes courseB and is registered
//	Node studentE; // other and takes courseA and is accepted
//	Node studentF; // other and takes CourseB and is accepted
//	Node personReferenceNode;
//	Node personA;
//	Node personB;
//	Node personC;
//	Node personD;
//	Node personE;
//	Node personF;
//	Node departmentReferenceNode;
//	Node departmentA; // responsible for courseA and courseD
//	Node departmentB; // responsible for courseB and courseC
//	Node responsibleReferenceNode;
//	Node responsibleA;
//	Node responsibleB;
//	Node courseReferenceNode;
//	Node courseA; // about:"28040ht06", run by the "Psykologi" department
//	Node courseB; // courseId: "KOSB15"
//	Node courseC;
//	Node courseD; // courseId: "TMHB21", run by the "Psykologi" department
//	Node courseE;
	
	@Override
	public void setUp()
	{
		super.setUp();
		
		Transaction tx = Transaction.begin();
		try
		{
			this.rdfStore = new RdfStoreImpl(
				this.neo(), new DenseRepresentationStrategy( this.neo() ) );
			
			List<Statement> statements = new ArrayList<Statement>();
			statements.add( this.createStatement(
				"studentA", Predicates.TYPE, Types.STUDENT ) );
			statements.add( this.createStatement(
				"studentB", Predicates.TYPE, Types.STUDENT ) );
			statements.add( this.createStatement(
				"studentC", Predicates.TYPE, Types.STUDENT ) );
			statements.add( this.createStatement(
				"studentD", Predicates.TYPE, Types.STUDENT ) );
			statements.add( this.createStatement(
				"studentE", Predicates.TYPE, Types.STUDENT ) );
			statements.add( this.createStatement(
				"studentF", Predicates.TYPE, Types.STUDENT ) );
			
//			studentReferenceNode = this.createReferenceNode(
//				"studentReferenceNode", LADOK_NAMESPACE + "Student" );
//			
//			studentA = this.createNode( "studentA", studentReferenceNode );
//			studentB = this.createNode( "studentB", studentReferenceNode );
//			studentC = this.createNode( "studentC", studentReferenceNode );
//			studentD = this.createNode( "studentD", studentReferenceNode );
//			studentE = this.createNode( "studentE", studentReferenceNode );
//			studentF = this.createNode( "studentF", studentReferenceNode );
			
			statements.add( this.createStatement(
				"personA", Predicates.TYPE, Types.PERSON ) );
			statements.add( this.createStatement(
				"personB", Predicates.TYPE, Types.PERSON ) );
			statements.add( this.createStatement(
				"personC", Predicates.TYPE, Types.PERSON ) );
			statements.add( this.createStatement(
				"personD", Predicates.TYPE, Types.PERSON ) );
			statements.add( this.createStatement(
				"personE", Predicates.TYPE, Types.PERSON ) );
			statements.add( this.createStatement(
				"personF", Predicates.TYPE, Types.PERSON ) );

//			personReferenceNode = this.createReferenceNode(
//				"personReferenceNode", PRIM_NAMESPACE + "Person" );
//			personA = this.createNode( "personA", personReferenceNode );
//			personB = this.createNode( "personB", personReferenceNode );
//			personC = this.createNode( "personC", personReferenceNode );
//			personD = this.createNode( "personD", personReferenceNode );
//			personE = this.createNode( "personE", personReferenceNode );
//			personF = this.createNode( "personF", personReferenceNode );
			
			statements.add( this.createStatement(
				"http://28040ht06", Predicates.TYPE, Types.COURSE ) );
			statements.add( this.createStatement(
				"courseB", Predicates.TYPE, Types.COURSE ) );
			statements.add( this.createStatement(
				"courseB", Predicates.COURSE_ID, "KOSB15" ) );
			statements.add( this.createStatement(
				"courseC", Predicates.TYPE, Types.COURSE ) );
			statements.add( this.createStatement(
				"courseD", Predicates.TYPE, Types.COURSE ) );
			statements.add( this.createStatement(
				"courseD", Predicates.COURSE_ID, "TMHB21" ) );
			statements.add( this.createStatement(
				"courseE", Predicates.TYPE, Types.COURSE ) );
//			courseReferenceNode = this.createReferenceNode(
//				"courseReferenceNode", LADOK_NAMESPACE + "CourseInstance" );
//			courseA = this.createNode( "28040ht06", courseReferenceNode );
//			courseB = this.createNode( "courseB", courseReferenceNode );
//			courseB.setProperty( "courseId", "KOSB15" );
//			courseC = this.createNode( "courseC", courseReferenceNode );
//			courseD = this.createNode( "courseD", courseReferenceNode );
//			courseD.setProperty( "courseId", "TMHB21" );
//			courseE = this.createNode( "courseE", courseReferenceNode );
			
			statements.add( this.createStatement(
				"departmentA", Predicates.TYPE, Types.DEPARTMENT ) );
			statements.add( this.createStatement(
				"departmentA", Predicates.NAME, "Psykologi" ) );
			statements.add( this.createStatement(
				"departmentB", Predicates.TYPE, Types.DEPARTMENT ) );
			
//			departmentReferenceNode = this.createReferenceNode(
//				"departmentReferenceNode", PRIM_NAMESPACE + "Department" );
//			departmentA = this.createNode(
//				"departmentA", departmentReferenceNode );
//			departmentA.setProperty( "name", "Psykologi" );
//			departmentB = this.createNode(
//				"departmentB", departmentReferenceNode );
			
			statements.add( this.createStatement(
				"responsibleA", Predicates.TYPE, Types.RESPONSIBLE ) );
			statements.add( this.createStatement(
				"responsibleB", Predicates.TYPE, Types.RESPONSIBLE ) );

//			responsibleReferenceNode = this.createReferenceNode(
//				"responsibleReferenceNode", PRIM_NAMESPACE + "Responsible" );
//			responsibleA = this.createNode(
//				"responsibleA", responsibleReferenceNode );
//			responsibleB = this.createNode(
//				"responsibleB", responsibleReferenceNode );

			statements.add( this.createStatement( "responsibleA",
				Predicates.ONE, new Uri( "departmentA" ) ) );
			statements.add( this.createStatement( "responsibleB",
				Predicates.ONE, new Uri( "departmentB" ) ) );
			statements.add( this.createStatement( "responsibleA",
				Predicates.OTHER, new Uri( "http://28040ht06" ) ) );
			statements.add( this.createStatement(
				"responsibleA", Predicates.OTHER, new Uri( "courseD" ) ) );
			statements.add( this.createStatement(
				"responsibleB", Predicates.OTHER, new Uri( "courseB" ) ) );
			statements.add( this.createStatement(
				"responsibleB", Predicates.OTHER, new Uri( "courseC" ) ) );
//			responsibleA.createRelationshipTo(
//				departmentA, Om2RelationshipType.ONE );
//			responsibleB.createRelationshipTo(
//				departmentB, Om2RelationshipType.ONE );
//			responsibleA.createRelationshipTo(
//				courseA, Om2RelationshipType.OTHER );
//			responsibleA.createRelationshipTo(
//				courseD, Om2RelationshipType.OTHER );
//			responsibleB.createRelationshipTo(
//				courseB, Om2RelationshipType.OTHER );
//			responsibleB.createRelationshipTo(
//				courseC, Om2RelationshipType.OTHER );
			
			statements.add( this.createStatement(
				"studentA", Predicates.ONE, new Uri( "personA" ) ) );
			statements.add( this.createStatement(
				"studentB", Predicates.ONE, new Uri( "personB" ) ) );
			statements.add( this.createStatement(
				"studentC", Predicates.ONE, new Uri( "personC" ) ) );
			statements.add( this.createStatement(
				"studentD", Predicates.ONE, new Uri( "personD" ) ) );
			statements.add( this.createStatement(
				"studentE", Predicates.ONE, new Uri( "personE" ) ) );
			statements.add( this.createStatement(
				"studentF", Predicates.ONE, new Uri( "personF" ) ) );
			
//			studentA.createRelationshipTo( personA, Om2RelationshipType.ONE );
//			studentB.createRelationshipTo( personB, Om2RelationshipType.ONE );
//			studentC.createRelationshipTo( personC, Om2RelationshipType.ONE );
//			studentD.createRelationshipTo( personD, Om2RelationshipType.ONE );
//			studentE.createRelationshipTo( personE, Om2RelationshipType.ONE );
//			studentF.createRelationshipTo( personF, Om2RelationshipType.ONE );

			statements.add( this.createStatement(
				"studentA", Predicates.STATE, "registered" ) );
			statements.add( this.createStatement(
				"studentC", Predicates.STATE, "registered" ) );
			statements.add( this.createStatement(
				"studentD", Predicates.STATE, "registered" ) );
			statements.add( this.createStatement(
				"studentE", Predicates.STATE, "accepted" ) );
			statements.add( this.createStatement(
				"studentF", Predicates.STATE, "accepted" ) );

//			studentA.setProperty( "state", "registered" );
//			studentC.setProperty( "state", "registered" );
//			studentD.setProperty( "state", "registered" );
//			studentE.setProperty( "state", "accepted" );
//			studentF.setProperty( "state", "accepted" );
			
			statements.add( this.createStatement( "studentA", Predicates.OTHER,
				new Uri( "http://28040ht06" ) ) );
			statements.add( this.createStatement(
				"studentA", Predicates.OTHER, new Uri( "courseE" ) ) );
			statements.add( this.createStatement( "studentC", Predicates.OTHER,
				new Uri( "http://28040ht06" ) ) );
			statements.add( this.createStatement(
				"studentD", Predicates.OTHER, new Uri( "courseB" ) ) );
			statements.add( this.createStatement( "studentE", Predicates.OTHER,
				new Uri( "http://28040ht06" ) ) );
			statements.add( this.createStatement(
				"studentF", Predicates.OTHER, new Uri( "courseB" ) ) );

//			studentA.createRelationshipTo(
//				courseA, Om2RelationshipType.OTHER );
//			studentA.createRelationshipTo(
//				courseE, Om2RelationshipType.OTHER );
//			studentC.createRelationshipTo(
//				courseA, Om2RelationshipType.OTHER );
//			studentD.createRelationshipTo(
//				courseB, Om2RelationshipType.OTHER );
//			studentE.createRelationshipTo(
//				courseA, Om2RelationshipType.OTHER );
//			studentF.createRelationshipTo(
//				courseB, Om2RelationshipType.OTHER );
			
			for ( Statement statement : statements )
			{
				this.rdfStore.addStatement( statement );
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private Statement createStatement(
		String subjectString, String predicateString, String objectString )
	{
		Resource subject = new Uri( subjectString );
		Uri predicate = new Uri( predicateString );
		Literal object = new Literal( objectString );
		
		return new CompleteStatement( subject, predicate, object );
	}

	private Statement createStatement(
		String subjectString, String predicateString, Uri objectUri )
	{
		Resource subject = new Uri( subjectString );
		Uri predicate = new Uri( predicateString );
		
		return new CompleteStatement( subject, predicate, objectUri );
	}

//	public void testQuery1() throws Exception
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			// Which students are registered on the course 28040ht06?
//			Query query = this.sparqlEngine.parse( new StringReader(
//				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
//				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"SELECT ?student ?person " +
//				"WHERE { " +
//				"?student rdf:type ladok:Student . " +
//				"?student prim:one ?person . " +
//				"?person rdf:type prim:Person . " +
//				"?student ladok:state \"registered\" . " +
//				"?student prim:other <http://28040ht06> . " +
//				"<http://28040ht06> rdf:type ladok:CourseInstance . }" ) );
////				"?student prim:other d:28040ht06 . " +
////				"d:28040ht06 rdf:type ladok:CourseInstance . }" ) );
//				
//			RdfBindingSet result =
//				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
//
//			Map<String, Integer> variables =
//				this.createVariableMap( "student", "person" );
//			String[][] expectedResult = new String[][] { 
//				{ "studentC", "personC" },
//				{ "studentA", "personA" } };
//			this.assertResult( result, variables, expectedResult );
//			
//			tx.success();
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
//
//	public void testQuery2() throws Exception
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			// Which students are accepted to the course KOSB15?
//			Query query = this.sparqlEngine.parse( new StringReader(
//				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
//				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"SELECT ?student ?person ?course " +
//				"WHERE { " +
//				"?student rdf:type ladok:Student . " +
//				"?student prim:one ?person . " +
//				"?person rdf:type prim:Person . " +
//				"?student ladok:state \"accepted\" . " +
//				"?student prim:other ?course . " +
//				"?course rdf:type ladok:CourseInstance . " +
//				"?course ladok:courseId \"KOSB15\" . } " ) );
//				
//			RdfBindingSet result =
//				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
//			
//			Map<String, Integer> variables =
//				this.createVariableMap( "student", "person", "course" );
//			String[][] expectedResult =
//				new String[][] { { "studentF", "personF", "courseB" } };
//			this.assertResult( result, variables, expectedResult );
//			
//			tx.success();
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
//
//	public void testQuery3() throws Exception
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			// Which courses are the Psychology department responsible for?
//			Query query = this.sparqlEngine.parse( new StringReader(
//				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
//				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"SELECT ?responsible ?course ?department " +
//				"WHERE { " +
//				"?responsible rdf:type prim:Responsible . " +
//				"?responsible prim:other ?course . " +
//				"?course rdf:type ladok:CourseInstance . " +
//				"?responsible prim:one ?department . " +
//				"?department rdf:type prim:Department . " +
//				"?department prim:name \"Psykologi\" } " ) );			
//			RdfBindingSet result =
//				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
//			
//			Map<String, Integer> variables = this.createVariableMap(
//				"responsible", "course", "department" );
//			String[][] expectedResult = new String[][] {
//				{ "responsibleA", "http://28040ht06", "departmentA" },
//				{ "responsibleA", "courseD", "departmentA" } };
//			this.assertResult( result, variables, expectedResult );
//
//			tx.success();
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}

	public void testQuery4() throws Exception
	{
		Transaction tx = Transaction.begin();
		try
		{
			// Which department is responsible for the course TMHB21?
			Query query = this.sparqlEngine.parse( new StringReader(
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
	
//	public void testQuery5() throws Exception
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			// Which department is responsible for the non-existant course
//			// TDDB56?
//			Query query = this.sparqlEngine.parse( new StringReader(
//				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
//				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"SELECT ?responsible ?course ?department ?name " +
//				"WHERE { " +
//				"?responsible rdf:type prim:Responsible . " +
//				"?responsible prim:other ?course . " +
//				"?course rdf:type ladok:CourseInstance . " +
//				"?course ladok:courseId \"TDDB56\" . " +
//				"?responsible prim:one ?department . " +
//				"?department rdf:type prim:Department . " +
//			"?department prim:name ?name }" ) ); 
//			RdfBindingSet result =
//				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
//			
//			this.assertResult( result, null, null );
//			
//			tx.success();
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
//	
//	public void testQuery6() throws Exception
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			// Get all students
//			// Optional: The person the student is connected to
//			// Optional: Every course the student takes
//			// Optional: The student's registration status
//			Query query = this.sparqlEngine.parse( new StringReader(
//				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
//				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"SELECT ?student ?person ?course ?state " +
//				"WHERE { " +
//				"?student rdf:type ladok:Student . " +
//				"OPTIONAL { ?student prim:other ?course . " +
//				"?course rdf:type ladok:CourseInstance } " +
//				"OPTIONAL { ?student prim:one ?person . " +
//				"?person rdf:type prim:Person } " +
//				"OPTIONAL { ?student ladok:state ?state } " +
//				"}" ) );
//				
//			RdfBindingSet result =
//				( ( SelectQuery ) query ).execute( new NeoRdfSource() );
//			
//			Map<String, Integer> variables =
//				this.createVariableMap( "student", "person", "course", "state" );
//			String[][] expectedResult = new String[][] {
//				{ "studentA", "personA", "28040ht06", "registered" },
//				{ "studentA", "personA", "courseE", "registered" },
//				{ "studentB", "personB", "", "" },
//				{ "studentC", "personC", "28040ht06", "registered" },
//				{ "studentD", "personD", "courseB", "registered" },
//				{ "studentE", "personE", "28040ht06", "accepted" },
//				{ "studentF", "personF", "courseB", "accepted" } };
//			this.assertResult( result, variables, expectedResult );
//			studentA.setProperty( "state", "registered" );
//			studentC.setProperty( "state", "registered" );
//			studentD.setProperty( "state", "registered" );
//			studentE.setProperty( "state", "accepted" );
//			studentF.setProperty( "state", "accepted" );
//			
//			tx.success();
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
//
//	public void testQuery7() throws Exception
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			// Construct a new rdf graph from all students/persons
//			// ( The new graph doesn't make sense, but hey... ;) )
//			Query query = this.sparqlEngine.parse( new StringReader(
//				"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
//				"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//				"CONSTRUCT { ?student prim:one ?person . " + 
//				"?person prim:other ?student } " +
//				"WHERE { " +
//				"?student rdf:type ladok:Student . " +
//				"?student prim:one ?person . " +
//				"?person rdf:type prim:Person " +
//				"}" ) );
//				
//			RdfGraph result =
//				( ( ConstructQuery ) query ).execute( new NeoRdfSource() );
//			String[] expectedResult = new String[] {
//			"(studentA, http://www.openmetadir.org/om2/prim-1.owl#one, personA)",
//			"(personA, http://www.openmetadir.org/om2/prim-1.owl#other, studentA)",
//			"(studentB, http://www.openmetadir.org/om2/prim-1.owl#one, personB)",
//			"(personB, http://www.openmetadir.org/om2/prim-1.owl#other, studentB)",
//			"(studentC, http://www.openmetadir.org/om2/prim-1.owl#one, personC)",
//			"(personC, http://www.openmetadir.org/om2/prim-1.owl#other, studentC)",
//			"(studentD, http://www.openmetadir.org/om2/prim-1.owl#one, personD)",
//			"(personD, http://www.openmetadir.org/om2/prim-1.owl#other, studentD)",
//			"(studentE, http://www.openmetadir.org/om2/prim-1.owl#one, personE)",
//			"(personE, http://www.openmetadir.org/om2/prim-1.owl#other, studentE)",
//			"(studentF, http://www.openmetadir.org/om2/prim-1.owl#one, personF)",
//			"(personF, http://www.openmetadir.org/om2/prim-1.owl#other, studentF)"
//			};
//			
//			this.assertResult( ( NeoRdfGraph ) result, expectedResult );
//
//			tx.success();
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
}
