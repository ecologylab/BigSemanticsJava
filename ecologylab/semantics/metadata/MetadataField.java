package ecologylab.semantics.metadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;

public class MetadataField<T> extends ElementState
{
	// MetaMetadata metaMetadata;
	@xml_map HashMapArrayList<String, MetadataField>	nested;
	@xml_attribute T									value;
	
	public MetadataField(HashMapArrayList<String, MetadataField> nested)
	{
		this.nested = nested;
	}
}
