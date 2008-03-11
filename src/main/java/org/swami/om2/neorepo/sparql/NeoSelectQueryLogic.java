package org.swami.om2.neorepo.sparql;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import name.levering.ryan.sparql.common.QueryException;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfSource;
import name.levering.ryan.sparql.model.TripleConstraint;
import name.levering.ryan.sparql.model.data.SelectQueryData;
import name.levering.ryan.sparql.model.logic.SelectQueryLogic;
import org.neo4j.util.matching.PatternNode;

public class NeoSelectQueryLogic extends AbstractNeoQueryLogic
	implements SelectQueryLogic
{
	private SelectQueryData data;
	
	public NeoSelectQueryLogic(
		SelectQueryData data, MetaModelProxy metaModel )
	{
		super( metaModel );
		this.data = data;
	}
	
	public RdfBindingSet execute( RdfSource source )
	{
		Set<TripleConstraint> typeConstraints =
			new HashSet<TripleConstraint>();
		Set<TripleConstraint> normalConstraints =
			new HashSet<TripleConstraint>();
		for ( Object constraint : this.data.getConstraint().getConstraints() )
		{
			if ( constraint instanceof TripleConstraint )
			{
				if ( this.metaModel.isTypeProperty(
					this.toUri( ( ( TripleConstraint ) constraint ).
						getPredicateExpression() ) ) )
				{
					typeConstraints.add( ( TripleConstraint ) constraint );
				}
				else
				{
					normalConstraints.add( ( TripleConstraint ) constraint );
				}
			}
			else
			{
				throw new QueryException(
					"Operation not supported with NeoRdfSource." );
			}
		}

		// Must add types before the other constraints.
		this.addTypes( typeConstraints );
		this.addConstraints( normalConstraints );
		
		this.assertGraph();
		PatternNode startNode = this.getStartNode();

		return new NeoRdfBindingSet( this.getNeoVariables(),
			this.performMatches( startNode ) );
	}
	
	private void addTypes( Set<TripleConstraint> constraints )
	{
		for ( TripleConstraint constraint : constraints )
		{
			this.addTypeToPattern( constraint );
		}
	}
	
	private void addConstraints( Set<TripleConstraint> constraints )
	{
		for ( TripleConstraint constraint : constraints )
		{
			this.addToPattern( constraint );
		}
	}
	
	@Override
	protected List<NeoVariable> getNeoVariables()
	{
		List<NeoVariable> neoVariables = new LinkedList<NeoVariable>();
		for ( NeoVariable neoVariable : super.getNeoVariables() )
		{
			if ( this.data.getVariables().contains(
				neoVariable.getVariable() ) )
			{
				neoVariables.add( neoVariable );
			}
		}
		
		return neoVariables;
	}
}
