package org.swami.om2.neorepo.sparql;

import java.util.LinkedList;
import java.util.List;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.RdfSource;
import name.levering.ryan.sparql.model.data.SelectQueryData;
import name.levering.ryan.sparql.model.logic.SelectQueryLogic;

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
		QueryGraph graph = this.buildGraph( this.data.getConstraint() );

		return new NeoRdfBindingSet( this.getNeoVariables(),
			this.performMatches( graph ) );
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
