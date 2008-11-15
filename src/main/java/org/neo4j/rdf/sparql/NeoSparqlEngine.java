package org.neo4j.rdf.sparql;

import java.io.InputStream;
import java.io.Reader;

import name.levering.ryan.sparql.logic.SPARQLQueryLogic;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;

import org.neo4j.rdf.store.representation.RepresentationStrategy;

public class NeoSparqlEngine
{
	private static NeoLogic NEO_LOGIC;
	
	public NeoSparqlEngine( RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
//	    if ( NEO_LOGIC != null )
//	    {
//	        throw new IllegalStateException( "There's already a SPARQL engine" +
//	        	" running, unfortunately we only support one SPARQL engine " +
//	        	"per JVM at this moment" );
//	    }
	    NEO_LOGIC = new NeoLogic( representationStrategy, metaModel );
		SPARQLQueryLogic.getInstance().setLogicFactory( NEO_LOGIC );
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
