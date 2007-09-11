package org.swami.om2.neorepo.sparql;

import org.neo4j.util.matching.PatternNode;
import name.levering.ryan.sparql.common.Variable;

public class NeoVariable implements Variable
{
	public static enum VariableType { LITERAL, URI };
	
	private VariableType variableType;
	private Variable variable;
	private PatternNode node;
	private String property;
	
	NeoVariable( Variable variable, VariableType variableType,
		PatternNode node, String property )
	{
		this.variableType = variableType;
		this.variable = variable;
		this.node = node;
		this.property = property;
	}
	
	public VariableType getVariableType()
	{
		return this.variableType;
	}
	
	Variable getVariable()
	{
		return this.variable;
	}
	
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
		return this.variable.getName();
	}
}
