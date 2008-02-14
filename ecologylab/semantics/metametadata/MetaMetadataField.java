package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

public class MetaMetadataField extends ElementState implements Mappable<String>
{
	@xml_attribute private String	name;

	@xml_attribute private ScalarType metadataType;									
	
	@xml_map("meta_metadata_field") private HashMapArrayList<String, MetaMetadataField>	childMetaMetadata;

	public MetaMetadataField()
	{
		
	}
	
	public MetaMetadataField(String name, ScalarType metadataType,HashMapArrayList<String, MetaMetadataField> set)
	{
		this.name = name;
		this.metadataType = metadataType;
		this.childMetaMetadata = set;
	}
	
	public int size()
	{
		return childMetaMetadata == null ? 0 : childMetaMetadata.size();
	}

	public HashMapArrayList<String, MetaMetadataField> getSet()
	{
		return childMetaMetadata;
	}

	public String getName()
	{
		return name;
	}

	public String key()
	{
		return name;
	}

	public ScalarType getMetadataType()
	{
		return metadataType;
	}

	public MetaMetadataField lookupChild(String name)
	{
		return childMetaMetadata.get(name);
	}
}
