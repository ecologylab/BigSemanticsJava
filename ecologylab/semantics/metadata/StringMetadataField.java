package ecologylab.semantics.metadata;

import ecologylab.generic.HashMapArrayList;

public class StringMetadataField extends MetadataField<String>
{

	@xml_attribute String value;
	
	public StringMetadataField(HashMapArrayList<String, MetadataField> nested, String value)
	{
		super(nested);
		this.value = value;
	}
}
