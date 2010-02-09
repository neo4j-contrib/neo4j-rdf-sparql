package org.neo4j.rdf.sparql;

import name.levering.ryan.sparql.common.Variable;

import org.neo4j.graphmatching.PatternNode;

public class Neo4jVariable implements Variable
{
	public static enum VariableType { LITERAL, URI };
	
	private VariableType variableType;
	private String variableName;
	private PatternNode node;
	private String property;
	
	Neo4jVariable( String variableName, VariableType variableType,
		PatternNode node, String property )
	{
		this.variableType = variableType;
		this.variableName = variableName;
		this.node = node;
		this.property = property;
	}
	
	public VariableType getVariableType()
	{
		return this.variableType;
	}
	
//	Variable getVariable()
//	{
//		return this.variable;
//	}
	
	String getProperty()
	{
		return this.property;
	}
	
	PatternNode getNode()
	{
		return this.node;
	}

	public String getName()
	{
		return this.variableName;
	}
	
	@Override
	public String toString()
	{
	    return getClass().getSimpleName() + "[" + getName() + "]";
	}
}
