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
