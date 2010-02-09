package org.neo4j.rdf.sparql;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import name.levering.ryan.sparql.logic.SPARQLQueryLogic;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;

import org.neo4j.rdf.store.representation.RepresentationStrategy;

public class Neo4jSparqlEngine
{
	private static Neo4jLogic NEO4J_LOGIC;
	
	public Neo4jSparqlEngine( RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
//	    if ( NEO4J_LOGIC != null )
//	    {
//	        throw new IllegalStateException( "There's already a SPARQL engine" +
//	        	" running, unfortunately we only support one SPARQL engine " +
//	        	"per JVM at this moment" );
//	    }
	    NEO4J_LOGIC = new Neo4jLogic( representationStrategy, metaModel );
		SPARQLQueryLogic.getInstance().setLogicFactory( NEO4J_LOGIC );
	}
	
	public Query parse( String query ) throws ParseException
	{
	    // Don't tell me we have to .close() a StringReader!?
	    return parse( new StringReader( query ) );
	}
	
	public Query parse( Reader queryReader ) throws ParseException
	{
		return SPARQLParser.parse( queryReader );
	}
	
	public Query parse( InputStream queryStream ) throws ParseException
	{
		return SPARQLParser.parse( queryStream );
	}
}
