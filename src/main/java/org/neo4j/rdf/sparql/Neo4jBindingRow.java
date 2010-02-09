package org.neo4j.rdf.sparql;

import java.util.LinkedList;
import java.util.List;

import name.levering.ryan.sparql.common.RdfBindingRow;
import name.levering.ryan.sparql.common.RdfBindingSet;
import name.levering.ryan.sparql.common.Variable;

import org.neo4j.graphmatching.PatternElement;
import org.neo4j.graphmatching.PatternMatch;
import org.openrdf.model.Value;

public class Neo4jBindingRow implements RdfBindingRow
{
	private Neo4jRdfBindingSet bindingSet;
	private PatternMatch match;
	
	Neo4jBindingRow( Neo4jRdfBindingSet bindingSet, PatternMatch match )
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
		Neo4jVariable neo4jVariable = this.getNeo4jVariable( variable );
		for ( PatternElement element : this.match.getElements() )
		{
			if ( element.getPatternNode().getLabel().equals(
				neo4jVariable.getNode().getLabel() ) )
			{
				if ( element.getNode().hasProperty(
					neo4jVariable.getProperty() ) )
				{
				    return new Neo4jValue( element.getNode().getProperty(
						neo4jVariable.getProperty() ) );
				}
				// Value was optional so just break and return ""
				break;
			}
		}
		return new Neo4jValue( "" );
	}

	private Neo4jVariable getNeo4jVariable( Variable variable )
	{
		if ( variable instanceof Neo4jVariable )
		{
			return ( Neo4jVariable ) variable;
		}
		
		for ( Neo4jVariable neo4jVariable : this.bindingSet.getVariables() )
		{
			if ( neo4jVariable.getName().equals( variable.getName() ) )
			{
				return neo4jVariable;
			}
		}
		
		throw new RuntimeException( "variable not found." );
	}

	public List<Value> getValues()
	{
		List<Value> values = new LinkedList<Value>();
		
		for ( Neo4jVariable variable :
			( List<Neo4jVariable> ) this.getVariables() )
		{
			for ( PatternElement element : this.match.getElements() )
			{
				if ( variable.getNode().getLabel().equals(
					element.getPatternNode().getLabel() ) )
				{
					values.add( new Neo4jValue(
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
	
	static class Neo4jValue implements Value
	{
		Object value;
		
		Neo4jValue( Object value )
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
