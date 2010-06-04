package org.neo4j.rdf.sparql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfGraph;
import name.levering.ryan.sparql.model.ConstructQuery;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.model.SelectQuery;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.meta.model.ClassRange;
import org.neo4j.meta.model.DatatypeClassRange;
import org.neo4j.meta.model.MetaModel;
import org.neo4j.meta.model.MetaModelClass;
import org.neo4j.meta.model.MetaModelImpl;
import org.neo4j.meta.model.MetaModelNamespace;
import org.neo4j.meta.model.MetaModelProperty;
import org.neo4j.meta.model.MetaModelRelationship;
import org.neo4j.rdf.model.CompleteStatement;
import org.neo4j.rdf.model.Context;
import org.neo4j.rdf.model.Literal;
import org.neo4j.rdf.model.Resource;
import org.neo4j.rdf.model.Uri;
import org.neo4j.rdf.store.RdfStore;
import org.neo4j.rdf.store.VerboseQuadStore;
import org.neo4j.rdf.store.representation.RepresentationExecutor;
import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.neo4j.rdf.store.representation.standard.VerboseQuadExecutor;
import org.neo4j.rdf.store.representation.standard.VerboseQuadStrategy;

public class Om2SampleQueriesTest extends SparqlTestCase
{
	interface Predicates
	{
		String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String COURSE_ID = "http://www.swami.se/om2/ladok-1.owl#courseId";
		String NAME = "http://www.openmetadir.org/om2/prim-1.owl#name";
        String NICK = "http://www.openmetadir.org/om2/prim-1.owl#nick";
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

	private static Map<String, Integer> counts = new HashMap<String, Integer>();
	private static RdfStore rdfStore;
	private static MetaModel metaModel;
	static
	{
		counts.put( PRIM_NAMESPACE + "Person", new Integer( 10 ) );
		counts.put( PRIM_NAMESPACE + "Department", new Integer( 2 ) );
		counts.put( PRIM_NAMESPACE + "Responsible", new Integer( 3 ) );
		counts.put( LADOK_NAMESPACE + "Student", new Integer( 10 ) );
		counts.put( LADOK_NAMESPACE + "CourseInstance", new Integer( 5 ) );
	}

	@Override
	public RepresentationStrategy instantiateRepresentationStrategy()
	{
		RepresentationExecutor executor = new VerboseQuadExecutor( graphDb(),
			index(), metaModel(), null );
		return new VerboseQuadStrategy( executor, metaModel() );
	}

    @Override
    public MetaModelMockUp instantiateMetaModelProxy()
    {
        return new MetaModelMockUp( metaModel(), counts );
    }

    @Override
    public Neo4jSparqlEngine instantiateSparqlEngine()
    {
        return new Neo4jSparqlEngine( representationStrategy(),
            metaModelProxy() );
    }
    
	private static MetaModel metaModel()
	{
		if ( metaModel == null )
		{
			metaModel = new MetaModelImpl( graphDb(), index() );
			MetaModelNamespace namespace =
				metaModel.getGlobalNamespace();
			MetaModelClass studentClass = namespace.getMetaClass(
				Types.STUDENT.getUriAsString(), true );
			MetaModelClass personClass = namespace.getMetaClass(
				Types.PERSON.getUriAsString(), true );
			MetaModelClass courseClass = namespace.getMetaClass(
				Types.COURSE.getUriAsString(), true );
			MetaModelClass departmentClass = namespace.getMetaClass(
				Types.DEPARTMENT.getUriAsString(), true );
			MetaModelClass responsibleClass = namespace.getMetaClass(
				Types.RESPONSIBLE.getUriAsString(), true );
			
			MetaModelProperty courseId = namespace.getMetaProperty(
				Predicates.COURSE_ID, true );
			courseId.setRange( new DatatypeClassRange( String.class ) );
			MetaModelProperty name = namespace.getMetaProperty(
				Predicates.NAME, true );
			name.setRange( new DatatypeClassRange( String.class ) );
			MetaModelProperty nick = namespace.getMetaProperty(
			    Predicates.NICK, true );
			nick.setRange( new DatatypeClassRange( String.class ) );
			MetaModelRelationship one = namespace.getMetaRelationship(
				Predicates.ONE, true );
			one.setRange( new ClassRange( personClass ) );
			MetaModelRelationship other = namespace.getMetaRelationship(
				Predicates.OTHER, true );
			other.setRange( new ClassRange( courseClass ) );
			MetaModelProperty state = namespace.getMetaProperty(
				Predicates.STATE, true );
			state.setRange( new DatatypeClassRange( String.class ) );

		}
		return metaModel;
	}

	@BeforeClass
	public static void setUpGraph() throws Exception
	{
	    Transaction tx = graphDb().beginTx();
	    try
	    {
	        rdfStore = new VerboseQuadStore( graphDb(), index(), metaModel(), null );
	        tx.success();
	    }
	    finally
	    {
	        tx.finish();
	    }
	    
		List<CompleteStatement> statements = new ArrayList<CompleteStatement>();
		statements.add( createStatement(
			"studentA", Predicates.TYPE, Types.STUDENT ) );
		statements.add( createStatement(
			"studentB", Predicates.TYPE, Types.STUDENT ) );
		statements.add( createStatement(
			"studentC", Predicates.TYPE, Types.STUDENT ) );
		statements.add( createStatement(
			"studentD", Predicates.TYPE, Types.STUDENT ) );
		statements.add( createStatement(
			"studentE", Predicates.TYPE, Types.STUDENT ) );
		statements.add( createStatement(
			"studentF", Predicates.TYPE, Types.STUDENT ) );

		statements.add( createStatement(
			"personA", Predicates.TYPE, Types.PERSON ) );
		statements.add( createStatement(
			"personB", Predicates.TYPE, Types.PERSON ) );
		statements.add( createStatement(
			"personC", Predicates.TYPE, Types.PERSON ) );
		statements.add( createStatement(
			"personD", Predicates.TYPE, Types.PERSON ) );
		statements.add( createStatement(
			"personE", Predicates.TYPE, Types.PERSON ) );
		statements.add( createStatement(
			"personF", Predicates.TYPE, Types.PERSON ) );

		statements.add( createStatement(
			"http://28040ht06", Predicates.TYPE, Types.COURSE ) );
		statements.add( createStatement(
			"courseB", Predicates.TYPE, Types.COURSE ) );
		statements.add( createStatement(
			"courseB", Predicates.COURSE_ID, "KOSB15" ) );
		statements.add( createStatement(
			"courseC", Predicates.TYPE, Types.COURSE ) );
		statements.add( createStatement(
			"courseD", Predicates.TYPE, Types.COURSE ) );
		statements.add( createStatement(
			"courseD", Predicates.COURSE_ID, "TMHB21" ) );
		statements.add( createStatement(
			"courseE", Predicates.TYPE, Types.COURSE ) );

		statements.add( createStatement(
			"departmentA", Predicates.TYPE, Types.DEPARTMENT ) );
		statements.add( createStatement(
			"departmentA", Predicates.NAME, "Psykologi" ) );
		statements.add( createStatement(
			"departmentB", Predicates.TYPE, Types.DEPARTMENT ) );

		statements.add( createStatement(
			"responsibleA", Predicates.TYPE, Types.RESPONSIBLE ) );
		statements.add( createStatement(
			"responsibleB", Predicates.TYPE, Types.RESPONSIBLE ) );

		statements.add( createStatement( "responsibleA",
			Predicates.ONE, new Uri( "departmentA" ) ) );
		statements.add( createStatement( "responsibleB",
			Predicates.ONE, new Uri( "departmentB" ) ) );
		statements.add( createStatement( "responsibleA",
			Predicates.OTHER, new Uri( "http://28040ht06" ) ) );
		statements.add( createStatement(
			"responsibleA", Predicates.OTHER, new Uri( "courseD" ) ) );
		statements.add( createStatement(
			"responsibleB", Predicates.OTHER, new Uri( "courseB" ) ) );
		statements.add( createStatement(
			"responsibleB", Predicates.OTHER, new Uri( "courseC" ) ) );

		statements.add( createStatement(
			"studentA", Predicates.ONE, new Uri( "personA" ) ) );
		statements.add( createStatement(
			"studentB", Predicates.ONE, new Uri( "personB" ) ) );
		statements.add( createStatement(
			"studentC", Predicates.ONE, new Uri( "personC" ) ) );
		statements.add( createStatement(
			"studentD", Predicates.ONE, new Uri( "personD" ) ) );
		statements.add( createStatement(
			"studentE", Predicates.ONE, new Uri( "personE" ) ) );
		statements.add( createStatement(
			"studentF", Predicates.ONE, new Uri( "personF" ) ) );

		statements.add( createStatement(
			"studentA", Predicates.STATE, "registered" ) );
		statements.add( createStatement(
			"studentC", Predicates.STATE, "registered" ) );
		statements.add( createStatement(
			"studentD", Predicates.STATE, "registered" ) );
		statements.add( createStatement(
			"studentE", Predicates.STATE, "accepted" ) );
		statements.add( createStatement(
			"studentF", Predicates.STATE, "accepted" ) );

		statements.add( createStatement( "studentA", Predicates.OTHER,
			new Uri( "http://28040ht06" ) ) );
		statements.add( createStatement(
			"studentA", Predicates.OTHER, new Uri( "courseE" ) ) );
		statements.add( createStatement( "studentC", Predicates.OTHER,
			new Uri( "http://28040ht06" ) ) );
		statements.add( createStatement(
			"studentD", Predicates.OTHER, new Uri( "courseB" ) ) );
		statements.add( createStatement( "studentE", Predicates.OTHER,
			new Uri( "http://28040ht06" ) ) );
		statements.add( createStatement(
			"studentF", Predicates.OTHER, new Uri( "courseB" ) ) );

		tx = graphDb().beginTx();
		try
		{
		    rdfStore.addStatements( statements.toArray(
		            new CompleteStatement[] {} ) );
		    tx.success();
		    
		}
		finally
		{
		    tx.finish();
		}
	}

	private static CompleteStatement createStatement(
		String subjectString, String predicateString, String objectString )
	{
		Resource subject = new Uri( subjectString );
		Uri predicate = new Uri( predicateString );
		Literal object = new Literal( objectString );

		return new CompleteStatement( subject, predicate, object,
			Context.NULL );
	}

	private static CompleteStatement createStatement(
		String subjectString, String predicateString, Uri objectUri )
	{
		Resource subject = new Uri( subjectString );
		Uri predicate = new Uri( predicateString );

		return new CompleteStatement( subject, predicate, objectUri,
			Context.NULL );
	}

	@Test
	public void testQuery1() throws Exception
	{
		// Which students are registered on the course 28040ht06?
		Query query = sparqlEngine().parse( new StringReader(
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
			( ( SelectQuery ) query ).execute( new Neo4jRdfSource() );

		Map<String, Integer> variables =
			createVariableMap( "student", "person" );
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		expectedResult.add( Arrays.asList( "studentC", "personC" ) );
		expectedResult.add( Arrays.asList( "studentA", "personA" ) );
		assertResult( result, variables, expectedResult );
	}

	@Test
    public void testQuery2() throws Exception
	{
		// Which students are accepted to the course KOSB15?
		Query query = sparqlEngine().parse( new StringReader(
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
			"?course ladok:courseId ?course_id ." +
			"FILTER( regex( ?course_id, \"B15$\" ) )" +
			"} " ) );

		RdfBindingSet result =
			( ( SelectQuery ) query ).execute( new Neo4jRdfSource() );

		Map<String, Integer> variables =
			createVariableMap( "student", "person", "course" );
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		expectedResult.add( Arrays.asList( "studentF", "personF", "courseB" ) );
		assertResult( result, variables, expectedResult );
	}

	@Test
    public void testQuery3() throws Exception
	{
		// Which courses are the Psychology department responsible for?
		Query query = sparqlEngine().parse( new StringReader(
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
			( ( SelectQuery ) query ).execute( new Neo4jRdfSource() );

		Map<String, Integer> variables = createVariableMap(
			"responsible", "course", "department" );
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		expectedResult.add( Arrays.asList(
		    "responsibleA", "courseD", "departmentA" ) );
		expectedResult.add( Arrays.asList(
			"responsibleA", "http://28040ht06", "departmentA" ) );
		assertResult( result, variables, expectedResult );
	}

	@Test
    public void testQuery4() throws Exception
	{
		// Which department is responsible for the course TMHB21?
		Query query = sparqlEngine().parse( new StringReader(
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
			( ( SelectQuery ) query ).execute( new Neo4jRdfSource() );

		Map<String, Integer> variables = createVariableMap(
			"responsible", "course", "department", "name" );
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		expectedResult.add( Arrays.asList(
		    "responsibleA", "courseD", "departmentA", "Psykologi" ) );
		assertResult( result, variables, expectedResult );
	}

	@Test
    public void testQuery5() throws Exception
	{
		// Which department is responsible for the non-existing course
		// TDDB56?
		Query query = sparqlEngine().parse( new StringReader(
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
			( ( SelectQuery ) query ).execute( new Neo4jRdfSource() );

		assertResult( result, null, null );
	}

	@Test
    public void testQuery6() throws Exception
	{
		// Get all students
		// Optional: The person the student is connected to
		// Optional: Every course the student takes
		// Optional: The student's registration status
		Query query = sparqlEngine().parse( new StringReader(
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
			( ( SelectQuery ) query ).execute( new Neo4jRdfSource() );

		Map<String, Integer> variables =
			createVariableMap( "student", "person", "course", "state" );
		List<List<String>> expectedResult = new ArrayList<List<String>>();
		expectedResult.add( Arrays.asList(
		    "studentF", "personF", "courseB", "accepted" ) );
		expectedResult.add( Arrays.asList(
		    "studentE", "personE", "http://28040ht06", "accepted" ) );
        expectedResult.add( Arrays.asList(
			"studentD", "personD", "courseB", "registered" ) );
	    expectedResult.add( Arrays.asList(
			"studentC", "personC", "http://28040ht06", "registered" ) );
	    expectedResult.add( Arrays.asList(
			"studentB", "personB", "", "" ) );
	    expectedResult.add( Arrays.asList(
	        "studentA", "personA", "courseE", "registered" ) );
	    expectedResult.add( Arrays.asList(
			"studentA", "personA", "http://28040ht06", "registered" ) );

		assertResult( result, variables, expectedResult );
	}

	@Test
    public void testQuery7() throws Exception
	{
		// Construct a new rdf graph from all students/persons
		// ( The new graph doesn't make sense, but hey... ;) )
		Query query = sparqlEngine().parse( new StringReader(
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
			( ( ConstructQuery ) query ).execute( new Neo4jRdfSource() );
		List<String> expectedResult = new ArrayList<String>();
		expectedResult.add(
	"(studentF, http://www.openmetadir.org/om2/prim-1.owl#one, personF)" );
        expectedResult.add(
	"(personF, http://www.openmetadir.org/om2/prim-1.owl#other, studentF)" );
        expectedResult.add(
	"(studentE, http://www.openmetadir.org/om2/prim-1.owl#one, personE)" );
        expectedResult.add(
	"(personE, http://www.openmetadir.org/om2/prim-1.owl#other, studentE)" );
        expectedResult.add(
	"(studentD, http://www.openmetadir.org/om2/prim-1.owl#one, personD)" );
        expectedResult.add(
	"(personD, http://www.openmetadir.org/om2/prim-1.owl#other, studentD)" );
        expectedResult.add(
	"(studentC, http://www.openmetadir.org/om2/prim-1.owl#one, personC)" );
        expectedResult.add(
	"(personC, http://www.openmetadir.org/om2/prim-1.owl#other, studentC)" );
        expectedResult.add(
	"(studentB, http://www.openmetadir.org/om2/prim-1.owl#one, personB)" );
        expectedResult.add(
	"(personB, http://www.openmetadir.org/om2/prim-1.owl#other, studentB)" );
        expectedResult.add(
	"(studentA, http://www.openmetadir.org/om2/prim-1.owl#one, personA)" );
        expectedResult.add(
	"(personA, http://www.openmetadir.org/om2/prim-1.owl#other, studentA)" );

		assertResult( ( Neo4jRdfGraph ) result, expectedResult );
	}
	
//	@Test
//	public void blabla() throws Exception
//	{
//	        String queryString =
//	        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
//	        "PREFIX data: <http://example.org/foaf/>" +
//
//	        "SELECT ?nick" +
//	        "WHERE" +
//	        "  {" +
//	        "     GRAPH data:bobFoaf {" +
//	        "         ?x foaf:mbox <mailto:bob@work.example> ." +
//	        "         ?x foaf:nick ?nick }" +
//	        "  }";
//	        
//	        Query query = sparqlEngine().parse( new StringReader( queryString ) );
//	}
}
