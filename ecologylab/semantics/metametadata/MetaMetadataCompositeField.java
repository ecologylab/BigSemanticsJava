package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.scalar.ScalarType;

@xml_inherit
public abstract class MetaMetadataCompositeField extends MetaMetadataField
{

	@xml_attribute
	protected boolean						generateClass	= true;

	public MetaMetadataCompositeField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataCompositeField(String name, ScalarType metadataType,
			HashMapArrayList<String, MetaMetadataField> set)
	{
		super(name, metadataType, set);
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataCompositeField(MetaMetadataField copy, String name)
	{
		super(copy, name);
		// TODO Auto-generated constructor stub
	}

	abstract protected String getMetaMetadataTagToInheritFrom();

	/**
	 * Bind field declarations through the extends and type keywords.
	 */
	public void inheritMetaMetadata(MetaMetadataRepository repository)
	{
		if(!inheritMetaMetadataFinished)
		{
			if (kids != null)
			{
				for(MetaMetadataField childField : kids)
				{
					if (childField instanceof MetaMetadataCompositeField)
						((MetaMetadataCompositeField)childField).inheritMetaMetadata(repository);
				}
			}
			String tagName = getMetaMetadataTagToInheritFrom();
			MetaMetadata inheritedMetaMetadata =  repository.getByTagName(tagName);
			if(inheritedMetaMetadata != null)
			{
				inheritedMetaMetadata.inheritMetaMetadata(repository);
				inheritNonDefaultAttributes(inheritedMetaMetadata);
				for(MetaMetadataField inheritedField : inheritedMetaMetadata.getChildMetaMetadata())
					inheritForField(inheritedField);
				inheritSemanticActionsFromMM(inheritedMetaMetadata);
			}
			
			inheritMetaMetadataFinished = true;
		}
	}

	/**
	 * @return the generateClass
	 */
	public boolean isGenerateClass()
	{
		// we r not using getType as by default getType will give meta-metadata name
		if((this instanceof MetaMetadataNestedField) && ((MetaMetadataNestedField) this).type!=null)
		{
			return false;
		}
		return generateClass;
	}
	
	/**
	 * @param generateClass
	 *          the generateClass to set
	 */
	public void setGenerateClass(boolean generateClass)
	{
		this.generateClass = generateClass;
	}
	
}
