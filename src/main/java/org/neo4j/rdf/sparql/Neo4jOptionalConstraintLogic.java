package org.neo4j.rdf.sparql;

import java.util.Collection;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfSource;
import name.levering.ryan.sparql.common.impl.SPARQLValueFactory;
import name.levering.ryan.sparql.model.OptionalConstraint;
import name.levering.ryan.sparql.model.data.OptionalConstraintData;
import name.levering.ryan.sparql.model.logic.ConstraintLogic;

public class Neo4jOptionalConstraintLogic implements ConstraintLogic
{
	private OptionalConstraintData data;
	private SPARQLValueFactory valueFactory;

	Neo4jOptionalConstraintLogic( OptionalConstraintData data,
		SPARQLValueFactory valueFactory )
	{
		this.data = data;
		this.valueFactory = valueFactory;
	}
	
	public RdfBindingSet constrain( RdfBindingSet bindings, RdfSource source,
		Collection defaultDataSets, Collection namedDataSets )
	{
		OptionalConstraint constraint =
			( OptionalConstraint ) this.data.getConstraint();
		return bindings;
	}
}
