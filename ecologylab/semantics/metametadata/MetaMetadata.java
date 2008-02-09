package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

public class MetaMetadata extends ElementState implements Mappable<String>
{
	@xml_attribute private String	name;

	@xml_attribute private ScalarType metadataType;									
	
	@xml_map("meta_metadata_field") private HashMapArrayList<String, MetaMetadata>	childMetaMetadata;

	public MetaMetadata(String name, ScalarType metadataType,HashMapArrayList<String, MetaMetadata> set)
	{
		this.name = name;
		this.metadataType = metadataType;
		this.childMetaMetadata = set;
	}
	
	public int size()
	{
		return childMetaMetadata == null ? 0 : childMetaMetadata.size();
	}

	public HashMapArrayList<String, MetaMetadata> getSet()
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

	public MetaMetadata lookupChild(String name)
	{
		return childMetaMetadata.get(name);
	}
}
