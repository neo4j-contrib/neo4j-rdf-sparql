package org.swami.om2.neorepo.sparql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfGraph;
import name.levering.ryan.sparql.model.ConstructQuery;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.model.SelectQuery;

import org.neo4j.neometa.structure.DatatypeClassRange;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureClassRange;
import org.neo4j.neometa.structure.MetaStructureImpl;
import org.neo4j.neometa.structure.MetaStructureNamespace;
import org.neo4j.neometa.structure.MetaStructureProperty;
import org.neo4j.rdf.model.CompleteStatement;
import org.neo4j.rdf.model.Context;
import org.neo4j.rdf.model.Literal;
import org.neo4j.rdf.model.Resource;
import org.neo4j.rdf.model.Uri;
import org.neo4j.rdf.store.RdfStore;
import org.neo4j.rdf.store.RdfStoreImpl;
import org.neo4j.rdf.store.representation.RepresentationExecutor;
import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.neo4j.rdf.store.representation.standard.DenseRepresentationStrategy;
import org.neo4j.rdf.store.representation.standard.UriBasedExecutor;

public abstract class Om2SampleQueriesTest extends SparqlTestCase
{
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

	private static Map<String, Integer> counts =
		new HashMap<String, Integer>();
	private RdfStore rdfStore;
	private static RepresentationStrategy representationStrategy;
	private static MetaStructure metaStructure;
	static
	{
		counts.put( PRIM_NAMESPACE + "Person", new Integer( 10 ) );
		counts.put( PRIM_NAMESPACE + "Department", new Integer( 2 ) );
		counts.put( PRIM_NAMESPACE + "Responsible", new Integer( 3 ) );
		counts.put( LADOK_NAMESPACE + "Student", new Integer( 10 ) );
		counts.put( LADOK_NAMESPACE + "CourseInstance", new Integer( 5 ) );
	}

	public Om2SampleQueriesTest( String name )
	{
		super( name, new MetaModelMockUp(
			metaStructure(), counts ), representationStrategy() );
	}

	private static RepresentationStrategy representationStrategy()
	{
		if ( representationStrategy == null )
		{
			RepresentationExecutor executor = new UriBasedExecutor( neo(),
				index(), metaStructure() );
			representationStrategy = new DenseRepresentationStrategy(
				executor, metaStructure() );
		}
		return representationStrategy;
	}

	private static MetaStructure metaStructure()
	{
		if ( metaStructure == null )
		{
			metaStructure = new MetaStructureImpl( neo() );
			MetaStructureNamespace namespace =
				metaStructure.getGlobalNamespace();
			MetaStructureClass studentClass = namespace.getMetaClass(
				"http://www.swami.se/om2/ladok-1.owl#Student", true );
			MetaStructureClass personClass = namespace.getMetaClass(
				"http://www.openmetadir.org/om2/prim-1.owl#Person", true );
			MetaStructureClass courseClass = namespace.getMetaClass(
				"http://www.swami.se/om2/ladok-1.owl#CourseInstance", true );
			MetaStructureClass departmentClass = namespace.getMetaClass(
				"http://www.openmetadir.org/om2/prim-1.owl#Department", true );
			MetaStructureClass responsibleClass = namespace.getMetaClass(
				"http://www.openmetadir.org/om2/prim-1.owl#Responsible",
				true );
			MetaStructureProperty courseId = namespace.getMetaProperty(
				"http://www.swami.se/om2/ladok-1.owl#courseId", true );
			courseId.setRange( new DatatypeClassRange( String.class ) );
			MetaStructureProperty name = namespace.getMetaProperty(
				"http://www.openmetadir.org/om2/prim-1.owl#name", true );
			name.setRange( new DatatypeClassRange( String.class ) );
			MetaStructureProperty one = namespace.getMetaProperty(
				"http://www.openmetadir.org/om2/prim-1.owl#one", true );
			one.setRange( new MetaStructureClassRange( personClass ) );
			MetaStructureProperty other = namespace.getMetaProperty(
				"http://www.openmetadir.org/om2/prim-1.owl#other", true );
			other.setRange( new MetaStructureClassRange( courseClass ) );
			MetaStructureProperty state = namespace.getMetaProperty(
				"http://www.swami.se/om2/ladok-1.owl#state", true );
			state.setRange( new DatatypeClassRange( String.class ) );

		}
		return metaStructure;
	}

	@Override
	public void setUp()
	{
		super.setUp();

		this.rdfStore =
			new RdfStoreImpl( neo(), representationStrategy() );

		List<CompleteStatement> statements = new ArrayList<CompleteStatement>();
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

		statements.add( this.createStatement(
			"departmentA", Predicates.TYPE, Types.DEPARTMENT ) );
		statements.add( this.createStatement(
			"departmentA", Predicates.NAME, "Psykologi" ) );
		statements.add( this.createStatement(
			"departmentB", Predicates.TYPE, Types.DEPARTMENT ) );

		statements.add( this.createStatement(
			"responsibleA", Predicates.TYPE, Types.RESPONSIBLE ) );
		statements.add( this.createStatement(
			"responsibleB", Predicates.TYPE, Types.RESPONSIBLE ) );

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

		this.rdfStore.addStatements( statements.toArray(
		    new CompleteStatement[] {} ) );
	}

	private CompleteStatement createStatement(
		String subjectString, String predicateString, String objectString )
	{
		Resource subject = new Uri( subjectString );
		Uri predicate = new Uri( predicateString );
		Literal object = new Literal( objectString );

		return new CompleteStatement( subject, predicate, object,
			Context.NULL );
	}

	private CompleteStatement createStatement(
		String subjectString, String predicateString, Uri objectUri )
	{
		Resource subject = new Uri( subjectString );
		Uri predicate = new Uri( predicateString );

		return new CompleteStatement( subject, predicate, objectUri,
			Context.NULL );
	}

	public void testQuery1() throws Exception
	{
		// Which students are registered on the course 28040ht06?
		Query query = this.sparqlEngine.parse( new StringReader(
			"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
			"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"SELECT ?student ?person " +
			"WHERE { " +
			"?student rdf:type ladok:Student . " +
			"?person rdf:type prim:Person . " +
			"?student prim:one ?person . " +
			"?student ladok:state \"registered\" . " +
			"?student prim:other <http://28040ht06> . " +
			"<http://28040ht06> rdf:type ladok:CourseInstance . }" ) );

		RdfBindingSet result =
			( ( SelectQuery ) query ).execute( new NeoRdfSource() );

		Map<String, Integer> variables =
			this.createVariableMap( "student", "person" );
		String[][] expectedResult = new String[][] {
			{ "studentC", "personC" },
			{ "studentA", "personA" } };
		this.assertResult( result, variables, expectedResult );
	}

	public void testQuery2() throws Exception
	{
		// Which students are accepted to the course KOSB15?
		Query query = this.sparqlEngine.parse( new StringReader(
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
	}

	public void testQuery3() throws Exception
	{
		// Which courses are the Psychology department responsible for?
		Query query = this.sparqlEngine.parse( new StringReader(
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
			{ "responsibleA", "courseD", "departmentA" },
			{ "responsibleA", "http://28040ht06", "departmentA" } };
		this.assertResult( result, variables, expectedResult );
	}

	public void testQuery4() throws Exception
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
	}

	public void testQuery5() throws Exception
	{
		// Which department is responsible for the non-existant course
		// TDDB56?
		Query query = this.sparqlEngine.parse( new StringReader(
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
	}

	public void testQuery6() throws Exception
	{
		// Get all students
		// Optional: The person the student is connected to
		// Optional: Every course the student takes
		// Optional: The student's registration status
		Query query = this.sparqlEngine.parse( new StringReader(
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
			{ "studentF", "personF", "courseB", "accepted" },
			{ "studentE", "personE", "http://28040ht06", "accepted" },
			{ "studentD", "personD", "courseB", "registered" },
			{ "studentC", "personC", "http://28040ht06", "registered" },
			{ "studentB", "personB", "", "" },
			{ "studentA", "personA", "courseE", "registered" },
			{ "studentA", "personA", "http://28040ht06", "registered" },
			};

		this.assertResult( result, variables, expectedResult );
	}

	public void testQuery7() throws Exception
	{
		// Construct a new rdf graph from all students/persons
		// ( The new graph doesn't make sense, but hey... ;) )
		Query query = this.sparqlEngine.parse( new StringReader(
			"PREFIX prim: <http://www.openmetadir.org/om2/prim-1.owl#> " +
			"PREFIX ladok: <http://www.swami.se/om2/ladok-1.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"CONSTRUCT { ?student prim:one ?person . " +
			"?person prim:other ?student } " +
			"WHERE { " +
			"?student rdf:type ladok:Student . " +
			"?student prim:one ?person . " +
			"?person rdf:type prim:Person " +
			"}" ) );

		RdfGraph result =
			( ( ConstructQuery ) query ).execute( new NeoRdfSource() );
		String[] expectedResult = new String[] {
		"(studentF, http://www.openmetadir.org/om2/prim-1.owl#one, personF)",
		"(personF, http://www.openmetadir.org/om2/prim-1.owl#other, studentF)",
		"(studentE, http://www.openmetadir.org/om2/prim-1.owl#one, personE)",
		"(personE, http://www.openmetadir.org/om2/prim-1.owl#other, studentE)",
		"(studentD, http://www.openmetadir.org/om2/prim-1.owl#one, personD)",
		"(personD, http://www.openmetadir.org/om2/prim-1.owl#other, studentD)",
		"(studentC, http://www.openmetadir.org/om2/prim-1.owl#one, personC)",
		"(personC, http://www.openmetadir.org/om2/prim-1.owl#other, studentC)",
		"(studentB, http://www.openmetadir.org/om2/prim-1.owl#one, personB)",
		"(personB, http://www.openmetadir.org/om2/prim-1.owl#other, studentB)",
		"(studentA, http://www.openmetadir.org/om2/prim-1.owl#one, personA)",
		"(personA, http://www.openmetadir.org/om2/prim-1.owl#other, studentA)",
		};

		this.assertResult( ( NeoRdfGraph ) result, expectedResult );
	}
}
