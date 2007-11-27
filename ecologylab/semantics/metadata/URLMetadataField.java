package ecologylab.semantics.metadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;

public class URLMetadataField extends MetadataField<ParsedURL>
{
	@xml_attribute ParsedURL value;
	
	public URLMetadataField(HashMapArrayList<String, MetadataField> nested, ParsedURL value)
	{
		super(nested);
		this.value = value;
	}
}
