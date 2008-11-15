package org.neo4j.rdf.sparql;

import java.util.Collection;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfSource;
import name.levering.ryan.sparql.model.logic.ConstraintLogic;

public class NeoConstraintLogic implements ConstraintLogic
{
	public RdfBindingSet constrain( RdfBindingSet bindings, RdfSource source,
		Collection defaultDatasets, Collection namedDatasets )
	{
		return null;
	}
}
