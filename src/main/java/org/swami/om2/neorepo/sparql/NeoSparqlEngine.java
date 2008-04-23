package org.swami.om2.neorepo.sparql;

import java.io.InputStream;
import java.io.Reader;
import name.levering.ryan.sparql.logic.SPARQLQueryLogic;
import name.levering.ryan.sparql.model.Query;
import name.levering.ryan.sparql.parser.ParseException;
import name.levering.ryan.sparql.parser.SPARQLParser;
import org.neo4j.rdf.store.representation.RdfRepresentationStrategy;

public class NeoSparqlEngine
{
	public NeoSparqlEngine(
		RdfRepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
	    if ( SPARQLQueryLogic.getInstance() != null )
	    {
	        throw new IllegalStateException( "There's already a SPARQL engine" +
	        	" running, unfortunately we only support one SPARQL engine " +
	        	"per JVM at this moment" );
	    }
		SPARQLQueryLogic.getInstance().setLogicFactory(
			new NeoLogic( representationStrategy, metaModel ) );
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
