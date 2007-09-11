package org.swami.om2.neorepo.sparql;

import org.swami.om2.neorepo.sparql.MetaModelProxy.OwlPropertyType;

public class OwlProperty
{
	private OwlPropertyType type;
	private Object mappedValue;

	public OwlProperty( OwlPropertyType type, Object value )
	{
		this.type = type;
		this.mappedValue = value;
	}

	public OwlPropertyType getType()
	{
		return this.type;
	}

	public Object getMappedValue()
	{
		return this.mappedValue;
	}
	
	@Override
	public String toString()
	{
		return "[" + this.type + "|" + this.mappedValue + "]";
	}
}
