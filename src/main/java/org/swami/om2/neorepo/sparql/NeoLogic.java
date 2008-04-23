package org.swami.om2.neorepo.sparql;

import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.impl.SPARQLValueFactory;
import name.levering.ryan.sparql.logic.BaseLogic;
import name.levering.ryan.sparql.logic.function.ExternalFunctionFactory;
import name.levering.ryan.sparql.model.data.AskQueryData;
import name.levering.ryan.sparql.model.data.CallExpressionData;
import name.levering.ryan.sparql.model.data.ConstructQueryData;
import name.levering.ryan.sparql.model.data.DescribeQueryData;
import name.levering.ryan.sparql.model.data.FilterConstraintData;
import name.levering.ryan.sparql.model.data.GraphConstraintData;
import name.levering.ryan.sparql.model.data.GroupConstraintData;
import name.levering.ryan.sparql.model.data.OptionalConstraintData;
import name.levering.ryan.sparql.model.data.OrderExpressionData;
import name.levering.ryan.sparql.model.data.SelectQueryData;
import name.levering.ryan.sparql.model.data.TripleConstraintData;
import name.levering.ryan.sparql.model.data.UnionConstraintData;
import name.levering.ryan.sparql.model.logic.AskQueryLogic;
import name.levering.ryan.sparql.model.logic.ConstraintLogic;
import name.levering.ryan.sparql.model.logic.ConstructQueryLogic;
import name.levering.ryan.sparql.model.logic.DescribeQueryLogic;
import name.levering.ryan.sparql.model.logic.ExpressionLogic;
import name.levering.ryan.sparql.model.logic.OrderExpressionLogic;
import name.levering.ryan.sparql.model.logic.SelectQueryLogic;
import org.neo4j.rdf.store.representation.RdfRepresentationStrategy;
import org.openrdf.model.URI;

public class NeoLogic extends BaseLogic
{
	private RdfRepresentationStrategy representationStrategy;
	private MetaModelProxy metaModel;
	
	public NeoLogic( RdfRepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
		this.representationStrategy = representationStrategy;
		this.metaModel = metaModel;
	}
	
	public AskQueryLogic getAskQueryLogic( AskQueryData data )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public ConstructQueryLogic getConstructQueryLogic( ConstructQueryData data,
		SPARQLValueFactory valueFactory )
	{
		return new NeoConstructQueryLogic( data, this.representationStrategy,
			this.metaModel );
	}

	public DescribeQueryLogic getDescribeQueryLogic( DescribeQueryData data )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public ExpressionLogic getExternalFunctionLogic( CallExpressionData data,
		SPARQLValueFactory valueFactory )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public ConstraintLogic getFilterConstraintLogic( FilterConstraintData data,
		SPARQLValueFactory valueFactory )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public ConstraintLogic getGraphConstraintLogic( GraphConstraintData data )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public ConstraintLogic getGroupConstraintLogic( GroupConstraintData data )
	{
		return new NeoConstraintLogic();
	}

	public ConstraintLogic getOptionalConstraintLogic(
		OptionalConstraintData data, SPARQLValueFactory valueFactory )
	{
		return new NeoOptionalConstraintLogic( data, valueFactory );
	}

	public OrderExpressionLogic getOrderExpressionLogic(
		OrderExpressionData data )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public SelectQueryLogic getSelectQueryLogic( SelectQueryData data )
	{
		return new NeoSelectQueryLogic( data, this.representationStrategy,
			this.metaModel );
	}

	public ConstraintLogic getTripleConstraintLogic(
		TripleConstraintData data )
	{
		return new NeoConstraintLogic();
	}

	public ConstraintLogic getUnionConstraintLogic( UnionConstraintData data )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}

	public void registerExternalFunction( URI functionIRI,
		ExternalFunctionFactory functionFactory )
	{
		throw new QueryException(
			"Operation not supported with NeoRdfSource." );
	}
}
