package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.semantics.tools.MetaMetadataCompilerUtils;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataField
{
	
	@simpl_composite
	@xml_tag("field_parser")
	private FieldParserElement fieldParserElement;

	public MetaMetadataNestedField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataNestedField(String name, HashMapArrayList<String, MetaMetadataField> set)
	{
		super(name, set);
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataNestedField(MetaMetadataField copy, String name)
	{
		super(copy, name);
		// TODO Auto-generated constructor stub
	}
	
	public FieldParserElement getFieldParserElement()
	{
		return fieldParserElement;
	}

	abstract protected String getMetaMetadataTagToInheritFrom();

	/**
	 * Bind field declarations through the extends and type keywords.
	 */
	public void inheritMetaMetadata(MetaMetadataRepository repository)
	{
		if (!inheritMetaMetadataFinished)
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
			MetaMetadata inheritedMetaMetadata = repository.getByTagName(tagName);
			if (inheritedMetaMetadata != null)
			{
				inheritedMetaMetadata.inheritMetaMetadata(repository);
				// <collection> should not inherit attributes from its child_type
				if (!(this instanceof MetaMetadataCollectionField))
					inheritNonDefaultAttributes(inheritedMetaMetadata);
				for (MetaMetadataField inheritedField : inheritedMetaMetadata.getChildMetaMetadata())
					inheritForField(inheritedField);
				inheritNonFieldComponentsFromMM(inheritedMetaMetadata);
			}

			if (kids != null)
			{
				for (MetaMetadataField childField : kids)
				{
					if (childField instanceof MetaMetadataNestedField)
						((MetaMetadataNestedField) childField).inheritMetaMetadata(repository);
				}

				sortForDisplay();
			}

			inheritMetaMetadataFinished = true;
		}
	}

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
		for (MetaMetadataField child : getChildMetaMetadata())
			if (child.isNewDeclaration())
				return true;

		return false;
	}

	/**
	 * public void setAuthors(HashMapArrayList<String, Author> authors) { this.authorNames = authors;
	 * }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @param fieldType
	 */
	protected void appendSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Set the value of field " + fieldName;
		// write Java doc
		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// write first line
		appendable.append("public void set" + StringTools.capitalize(fieldName) + "( " + fieldType
				+ " " + fieldName + " )\n{\n");
		appendable.append("this." + fieldName + " = " + fieldName + " ;\n}\n");
	}

	protected void appendGetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Get the value of field " + fieldName;
		// write Java doc
		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// write first line
		appendable.append("public " + fieldType + " get" + StringTools.capitalize(fieldName) + "(){\n");
		appendable.append("return this." + fieldName + ";\n}\n");
	}

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return
	 */
	public abstract MetaMetadataCompositeField metaMetadataCompositeField();

	@Override
	protected String getSuperTypeName()
	{
		if (getExtendsAttribute() != null)
			return getExtendsAttribute();
		return "metadata";
	}

	@Override
	protected boolean checkForErrors()
	{
		MetaMetadataNestedField inherited = (MetaMetadataNestedField) getInheritedField();
		if (inherited == null)
		{
			// definitive
			return assertNotNull(getName(), "name must be specified.")
					&& assertNotNull(getTypeName(), "can't resolve type.");
		}
		else
		{
			// declarative
			String inheritedTypeName = inherited.getTypeName();
			return assertEquals(getTypeName(), inheritedTypeName, "field type not matches inherited one: %s", getTypeName());
		}
	}

}
