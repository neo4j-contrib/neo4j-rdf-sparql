package org.swami.om2.neorepo.sparql;

import java.util.HashMap;
import java.util.Map;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

public class MetaModelMockUp implements MetaModelProxy
{
	private Map<String, OwlPropertyType> types;
	private Map<String, Object> values;
	private Map<String, Node> referenceNodes =
		new HashMap<String, Node>();
	private RelationshipType instanceOf;
	private Map<String, Integer> counts;
	
	public MetaModelMockUp( Map<String, OwlPropertyType> types,
		Map<String, Object> values, Map<String, Integer> counts,
		RelationshipType instanceOf )
	{
		this.types = types;
		this.values = values;
		this.counts = counts;
		this.instanceOf = instanceOf;
	}

	public RelationshipType getTypeRelationship()
	{
		return this.instanceOf;
	}
	
	public String getAboutKey()
	{
		return "about";
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
	
	public int getCount( String uri )
	{
		return this.counts.get( uri );
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
	
	// Help method for the unit tests.
	void addClassNode( String uri, Node node )
	{
		this.referenceNodes.put( uri, node );
	}
}
