package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.scalar.ScalarType;

@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataField
{

	public MetaMetadataNestedField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataNestedField(String name, ScalarType metadataType,
			HashMapArrayList<String, MetaMetadataField> set)
	{
		super(name, metadataType, set);
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataNestedField(MetaMetadataField copy, String name)
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
			/**************************************************************************************
			 * Inheritance works here in a top-down manner: first we know the type or extends of a
			 * meta-metadata, from which we can infer scalar_type/type/child_type of its first-level
			 * children (those not defined inline). In this way we can resolve type information
			 * recursively.
			 **************************************************************************************/
			
			/*
			 * tagName will be type / extends attribute for <composite>, or child_type attribute for
			 * <collection>
			 */
			String tagName = getMetaMetadataTagToInheritFrom();
			MetaMetadata inheritedMetaMetadata =  repository.getByTagName(tagName);
			if(inheritedMetaMetadata != null)
			{
				inheritedMetaMetadata.inheritMetaMetadata(repository);
				// <collection> should not inherit attributes from its child_type
				if (!(this instanceof MetaMetadataCollectionField))
					inheritNonDefaultAttributes(inheritedMetaMetadata);
				for(MetaMetadataField inheritedField : inheritedMetaMetadata.getChildMetaMetadata())
					inheritForField(inheritedField);
				inheritNonFieldComponentsFromMM(inheritedMetaMetadata);
			}
			
			if (kids != null)
			{
				for(MetaMetadataField childField : kids)
				{
					if (childField instanceof MetaMetadataNestedField)
						((MetaMetadataNestedField)childField).inheritMetaMetadata(repository);
				}
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
		
		if (mmf instanceof MetaMetadataCompositeField && ((MetaMetadataCompositeField) mmf).getType() != null)
			return true;
		
		return mmf.isNewClass();
	}
}
