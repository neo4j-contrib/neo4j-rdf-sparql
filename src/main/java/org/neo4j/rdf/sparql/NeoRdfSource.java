package org.swami.om2.neorepo.sparql;

import java.util.Iterator;
import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.RdfSource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

public class NeoRdfSource implements RdfSource
{
	public Iterator getDefaultStatements( Value subj, URI pred, Value obj )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public Iterator getStatements( Value subj, URI pred, Value obj, URI graph )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public Iterator getStatements( Value subj, URI pred, Value obj )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public ValueFactory getValueFactory()
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean hasDefaultStatement( Value subj, URI pred, Value obj )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean hasStatement( Value subj, URI pred, Value obj )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public boolean hasStatement( Value subj, URI pred, Value obj, URI graph )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}
}
