package org.swami.om2.neorepo.sparql;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;

public interface MetaModelProxy
{
	public enum OwlPropertyType
	{
		OBJECT_TYPE,
		DATATYPE_TYPE
	}
	
	public RelationshipType getTypeRelationship();
	
	public String getAboutKey();
	
	/**
	 * @param subjectUri
	 * @param predicateUri
	 * @param objectUri is null for datatype types.
	 * @return
	 */
	public OwlProperty getOwlProperty(
		String subjectUri, String predicateUri, String objectUri );
	
	public boolean isTypeProperty( String uri );
	
	public int getCount( String uri );
	
	public Node getClassNode( String uri );
	
	public Object convertCriteriaStringValueToRealValue( String propertyKey,
		String value );
	
	public String getObjectType( String subjectUri, String predicateUri );
}
