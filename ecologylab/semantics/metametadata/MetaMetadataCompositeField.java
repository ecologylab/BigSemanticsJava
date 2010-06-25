package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.scalar.ScalarType;

@xml_inherit
public abstract class MetaMetadataCompositeField extends MetaMetadataField
{

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
					if (childField instanceof MetaMetadataNestedField)
						((MetaMetadataNestedField)childField).inheritMetaMetadata(repository);
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
			
			sortForDisplay();
		}
	}

	/**
	 * Indicate whether to generate a new class definition for this meta-metadata.
	 */
	@Override
	public boolean isNewClass()
	{
		// if no internal structure, no need to generate a class
		if (kids == null)
			return false;
		
		// if indicated by the author explicitly, do not generate a class
		if (this instanceof MetaMetadata && !((MetaMetadata) this).isGenerateClass())
			return false;
		
		// otherwise, determine if we need to generate a class
		/*
		 * look through its children recursively. if any of them is a data definition, which implies
		 * that this composite field is supposed to define or extend a type inline, we have to generate
		 * a class for it
		 * 
		 * must start from the 1st level children, not the field itself
		 */
		for (MetaMetadataField child: getChildMetaMetadata())
			if (isDefinition(child))
				return true;
		
		return false;
	}
	
	/**
	 * Recursively check if a meta-metadata field is a definition, by checking if any of its nested
	 * scalar field contains a scalar_type attribute.
	 * @param mmf
	 * @return
	 */
	protected boolean isDefinition(MetaMetadataField mmf)
	{
		if (mmf instanceof MetaMetadataScalarField)
		{
			return ((MetaMetadataScalarField) mmf).getScalarType() != null;
		}
		
		if (mmf instanceof MetaMetadataNestedField && ((MetaMetadataNestedField) mmf).getType() != null)
			return true;
		
		return mmf.isNewClass();
	}
}
