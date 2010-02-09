package org.neo4j.rdf.sparql;

import java.util.Iterator;
import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.RdfSource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

public class Neo4jRdfSource implements RdfSource
{
    private QueryException newOperationNotSupportedException()
    {
        return new QueryException( "Operation not supported." );
    }
    
	public Iterator getDefaultStatements( Value subj, URI pred, Value obj )
	{
	    throw newOperationNotSupportedException();
	}

	public Iterator getStatements( Value subj, URI pred, Value obj, URI graph )
	{
        throw newOperationNotSupportedException();
	}

	public Iterator getStatements( Value subj, URI pred, Value obj )
	{
        throw newOperationNotSupportedException();
	}

	public ValueFactory getValueFactory()
	{
        throw newOperationNotSupportedException();
	}

	public boolean hasDefaultStatement( Value subj, URI pred, Value obj )
	{
        throw newOperationNotSupportedException();
	}

	public boolean hasStatement( Value subj, URI pred, Value obj )
	{
        throw newOperationNotSupportedException();
	}

	public boolean hasStatement( Value subj, URI pred, Value obj, URI graph )
	{
        throw newOperationNotSupportedException();
	}
}
