package org.neo4j.rdf.sparql;

import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.impl.SPARQLValueFactory;
import name.levering.ryan.sparql.logic.BaseLogic;
import name.levering.ryan.sparql.logic.DefaultEffectiveBooleanLogic;
import name.levering.ryan.sparql.logic.DefaultValueConversionLogic;
import name.levering.ryan.sparql.logic.function.ExternalFunctionFactory;
import name.levering.ryan.sparql.logic.naive.DefaultFilterConstraintLogic;
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
import name.levering.ryan.sparql.model.logic.ValueConversionLogic;

import org.neo4j.rdf.store.representation.RepresentationStrategy;
import org.openrdf.model.URI;

public class Neo4jLogic extends BaseLogic
{
	private RepresentationStrategy representationStrategy;
	private MetaModelProxy metaModel;
	
	public Neo4jLogic( RepresentationStrategy representationStrategy,
		MetaModelProxy metaModel )
	{
		this.representationStrategy = representationStrategy;
		this.metaModel = metaModel;
	}
	
	public AskQueryLogic getAskQueryLogic( AskQueryData data )
	{
        throw new QueryException( "Operation not supported." );
	}

	public ConstructQueryLogic getConstructQueryLogic( ConstructQueryData data,
		SPARQLValueFactory valueFactory )
	{
		return new Neo4jConstructQueryLogic( data, this.representationStrategy,
			this.metaModel );
	}

	public DescribeQueryLogic getDescribeQueryLogic( DescribeQueryData data )
	{
        throw new QueryException( "Operation not supported." );
	}

	public ExpressionLogic getExternalFunctionLogic( CallExpressionData data,
		SPARQLValueFactory valueFactory )
	{
        throw new QueryException( "Operation not supported." );
	}

	public ConstraintLogic getFilterConstraintLogic( FilterConstraintData data,
		SPARQLValueFactory valueFactory )
	{
        ValueConversionLogic conversionLogic = new DefaultValueConversionLogic(
            valueFactory );
        return new DefaultFilterConstraintLogic( data,
            new DefaultEffectiveBooleanLogic( conversionLogic ),
                conversionLogic );
	}

	public ConstraintLogic getGraphConstraintLogic( GraphConstraintData data )
	{
        throw new QueryException( "Operation not supported." );
	}

	public ConstraintLogic getGroupConstraintLogic( GroupConstraintData data )
	{
		return new Neo4jConstraintLogic();
	}

	public ConstraintLogic getOptionalConstraintLogic(
		OptionalConstraintData data, SPARQLValueFactory valueFactory )
	{
		return new Neo4jOptionalConstraintLogic( data, valueFactory );
	}

	public OrderExpressionLogic getOrderExpressionLogic(
		OrderExpressionData data )
	{
        throw new QueryException( "Operation not supported." );
	}

	public SelectQueryLogic getSelectQueryLogic( SelectQueryData data )
	{
		return new Neo4jSelectQueryLogic( data, this.representationStrategy,
			this.metaModel );
	}

	public ConstraintLogic getTripleConstraintLogic(
		TripleConstraintData data )
	{
		return new Neo4jConstraintLogic();
	}

	public ConstraintLogic getUnionConstraintLogic( UnionConstraintData data )
	{
        throw new QueryException( "Operation not supported." );
	}

	public void registerExternalFunction( URI functionIRI,
		ExternalFunctionFactory functionFactory )
	{
        throw new QueryException( "Operation not supported." );
	}
}
