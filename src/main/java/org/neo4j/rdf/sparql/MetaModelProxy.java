package org.neo4j.rdf.sparql;

import org.neo4j.rdf.store.representation.AbstractNode;

public interface MetaModelProxy
{
//	public enum OwlPropertyType
//	{
//		OBJECT_TYPE,
//		DATATYPE_TYPE
//	}
	
//	public RelationshipType getTypeRelationship();
	
//	public String getAboutKey();
	
//	public String getNodeTypeNameKey();
	
//	public OwlProperty getOwlProperty(
//		String subjectUri, String predicateUri, String objectUri );
	
//	public boolean isTypeProperty( String uri );
	
	public int getCount( AbstractNode abstractNode );
	
//	public Node getClassNode( String uri );
	
//	public Object convertCriteriaStringValueToRealValue( String propertyKey,
//		String value );
	
//	public String getObjectType( String subjectUri, String predicateUri );
	
//	public String[] getSubTypes( String type, boolean includeMyself );
}
