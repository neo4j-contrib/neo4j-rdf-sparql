package org.neo4j.rdf.sparql;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.api.core.Node;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.rdf.store.representation.AbstractNode;

public class MetaModelMockUp implements MetaModelProxy
{
	private Map<String, Node> referenceNodes =
		new HashMap<String, Node>();
//	private RelationshipType instanceOf;
	private Map<String, Integer> counts;
	private MetaStructure metaStructure;
	
	public MetaModelMockUp(
		MetaStructure metaStructure, Map<String, Integer> counts )
	{
		this.counts = counts;
		this.metaStructure = metaStructure;
	}
	
	public MetaStructure getMetaStructure()
	{
		return this.metaStructure;
	}

	public boolean isTypeProperty( String uri )
	{
		return "http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals( uri );
	}
	
	public int getCount( AbstractNode abstractNode )
	{
		Integer count = null;
		if ( abstractNode.getUriOrNull() != null )
		{
			count = this.counts.get(
				abstractNode.getUriOrNull().getUriAsString() );
		}
		return count == null ? Integer.MAX_VALUE : count;
	}
	
	public void addClassNode( String uri, Node node )
	{
	    this.referenceNodes.put( uri, node );
	}
}
