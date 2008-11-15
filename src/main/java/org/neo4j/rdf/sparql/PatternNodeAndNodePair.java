package org.neo4j.rdf.sparql;

import org.neo4j.api.core.Node;
import org.neo4j.util.matching.PatternNode;

public class PatternNodeAndNodePair
{
	private PatternNode patternNode;
	private Node node;
	
	PatternNodeAndNodePair( PatternNode patternNode, Node node )
	{
		this.patternNode = patternNode;
		this.node = node;
	}
	
	public PatternNode getPatternNode()
	{
		return this.patternNode;
	}
	
	public Node getNode()
	{
		return this.node;
	}
	
	@Override
	public String toString()
	{
		return "[" + this.patternNode + "|" + this.node + "]";
	}
}
