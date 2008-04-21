package org.swami.om2.neorepo.sparql;

import java.util.LinkedList;
import java.util.List;
import name.levering.ryan.sparql.common.RdfBindingRow;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.Variable;
import org.neo4j.util.matching.PatternElement;
import org.neo4j.util.matching.PatternMatch;
import org.openrdf.model.Value;

public class NeoBindingRow implements RdfBindingRow
{
	private NeoRdfBindingSet bindingSet;
	private PatternMatch match;
	
	NeoBindingRow( NeoRdfBindingSet bindingSet, PatternMatch match )
	{
		this.bindingSet = bindingSet;
		this.match = match;
	}
	
	public RdfBindingSet getParentSet()
	{
		return this.bindingSet;
	}

	public Value getValue( Variable variable )
	{
		NeoVariable neoVariable = ( NeoVariable ) variable;
		for ( PatternElement element : this.match.getElements() )
		{
			if ( element.getPatternNode().getLabel().equals(
				neoVariable.getNode().getLabel() ) )
			{
				if ( element.getNode().hasProperty(
					neoVariable.getProperty() ) )
				{
					return new NeoValue( element.getNode().getProperty(
							neoVariable.getProperty() ) );
				}
				// Value was optional so just break and return ""
				break;
			}
		}
		return new NeoValue( "" );
	}

	public List<Value> getValues()
	{
		List<Value> values = new LinkedList<Value>();
		
		for ( NeoVariable variable :
			( List<NeoVariable> ) this.getVariables() )
		{
			for ( PatternElement element : this.match.getElements() )
			{
				if ( variable.getNode().getLabel().equals(
					element.getPatternNode().getLabel() ) )
				{
					values.add( new NeoValue(
						element.getNode().getProperty(
							variable.getProperty() ) ) );
					break;
				}
			}
		}
		
		return values;
	}

	public List<? extends Variable> getVariables()
	{
		return this.bindingSet.getVariables();
	}
	
	static class NeoValue implements Value
	{
		Object value;
		
		NeoValue( Object value )
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			if ( value == null )
			{
				return "";
			}
			
			return value.toString();
		}
	}
}
