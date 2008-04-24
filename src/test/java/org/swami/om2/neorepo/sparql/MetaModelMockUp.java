package org.swami.om2.neorepo.sparql;

import java.util.HashMap;
import java.util.Map;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.rdf.store.representation.AbstractNode;

public class MetaModelMockUp implements MetaModelProxy
{
	private Map<String, OwlPropertyType> types;
	private Map<String, Object> values;
	private Map<String, Node> referenceNodes =
		new HashMap<String, Node>();
	private RelationshipType instanceOf;
	private Map<String, Integer> counts;
	private MetaStructure metaStructure;
	
	public MetaModelMockUp(
		MetaStructure metaStructure, Map<String, Integer> counts )
	{
		this.types = types;
		this.values = values;
		this.counts = counts;
		this.metaStructure = metaStructure;
	}
	
	public MetaStructure getMetaStructure()
	{
		return this.metaStructure;
	}

	public RelationshipType getTypeRelationship()
	{
		return this.instanceOf;
	}
	
	public String getAboutKey()
	{
		return "about";
	}
	
	public String getNodeTypeNameKey()
	{
		return "name";
	}
	
	public OwlProperty getOwlProperty(
		String subjectUri, String predicateUri, String objectUri )
	{
		return new OwlProperty(
			types.get( predicateUri ), values.get( predicateUri ) );
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
	
	public Node getClassNode( String uri )
	{
		return this.referenceNodes.get( uri );
	}
	
	public Object convertCriteriaStringValueToRealValue( String propertyKey,
		String value )
	{
		return value;
	}
	
	public String getObjectType( String subjectUri, String predicateUri )
	{
		// TODO?
		return null;
	}
	
	public String[] getSubTypes( String type, boolean includeMyself )
	{
		return new String[] { type };
	}
	
	// Help method for the unit tests.
	void addClassNode( String uri, Node node )
	{
		this.referenceNodes.put( uri, node );
	}
}
