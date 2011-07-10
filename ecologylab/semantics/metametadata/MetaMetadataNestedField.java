package ecologylab.semantics.metametadata;

import java.util.HashSet;

import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataField implements PackageSpecifier
{

	@simpl_composite
	@xml_tag("field_parser")
	private FieldParserElement		fieldParserElement;

	@simpl_scalar
	private boolean								promoteChildren;									// if children should be
																																	// displayed at this level

	@simpl_scalar
	private boolean								polymorphicGlobal;

	@xml_tag("package")
	@simpl_scalar
	String												packageName;

	/**
	 * the mmd used by this nested field. corresponding attributes: (child_)type/extends. could be a
	 * generated one for inline definitions.
	 */
	private MetaMetadata					inheritedMmd;

	private boolean								generateClassDescriptor	= false;
	
	protected MetaMetadata				scopingMmd;

	// private boolean isPolymorphic = false;

	private HashSet<MetaMetadata>	polymorphicMmds					= null;

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
	
	/**
	 * @return the packageAttribute
	 */
	public final String packageName()
	{
		return packageName;
	}

	void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public MetaMetadata getInheritedMmd()
	{
		return inheritedMmd;
	}
	
	protected void setInheritedMmd(MetaMetadata inheritedMmd)
	{
		this.inheritedMmd = inheritedMmd;
	}
	
	public FieldParserElement getFieldParserElement()
	{
		return fieldParserElement;
	}

	abstract protected String getMetaMetadataTagToInheritFrom();
	
	public void inheritMetaMetadata()
	{
		if (!inheritFinished && !inheritInProcess)
		{
			debug("inheriting " + this.toString());
			inheritInProcess = true;
			this.inheritMetaMetadataHelper();
			this.sortForDisplay();
			inheritInProcess = false;
			inheritFinished = true;
		}
	}
	
	/**
	 * prerequisites:<br>
	 * <ul>
	 *   <li>for meta-metadata: name & type/extends set;</li>
	 *   <li>for fields: attributes inherited & declaringMmd set;</li>
	 *   <li>for both: parent element inheritedMmd set.</li>
	 * </ul>
	 * <p>
	 * consequences:<br>
	 * <ul>
	 *   <li>if this is a MetaMetadata object, attributes inherited from inheritedMmd;</li>
	 *   <li>inheritedMmd set;</li>
	 *   <li>(first-level) fields inherited from inheritedMmd;</li>
	 *   <li>inheritedField and declaringMmd set for all (first-level) fields;</li>
	 *   <li>for all (first-level) fields, attributes inherited from their inheritedField. (enabling recursion)</li>
	 * </ul>
	 */
	abstract protected void inheritMetaMetadataHelper();

	//FIXME -- make it work for type graphs!!!
	/**
	 * Bind field declarations through the extends and type keywords.
	 */
	@Deprecated
	public void inheritMetaMetadata(MetaMetadataRepository repository)
	{
		if (!visitedMetaMetadata.contains(this) && !inheritFinished && !inheritInProcess)
		{
			/**************************************************************************************
			 * Inheritance works here in a top-down manner: first we know the type or extends of a
			 * meta-metadata, from which we can infer scalar_type/type/child_type of its first-level
			 * children (those not defined inline). In this way we can resolve type information
			 * recursively.
			 **************************************************************************************/
			
			// inheritInProcess prevents infinite loops, e.g. when A.b refers B while B.a refers A, then
			// when you initialize A.b you will have to initialize A.b.a and you will have to initialize
			// A.b.a.b ...
			inheritInProcess = true;
			visitedMetaMetadata.add(this);
		
			/*
			 * tagName will be type / extends attribute for <composite>, or child_type attribute for
			 * <collection>
			 */
			String tagName = getMetaMetadataTagToInheritFrom();
			MetaMetadata inheritedMetaMetadata = repository.getByTagName(tagName);
			if (inheritedMetaMetadata != null)
			{
				this.setInheritedMmd(inheritedMetaMetadata);
				inheritedMetaMetadata.inheritMetaMetadata(repository);
				// <collection> should not inherit attributes from its child_type
				if (!(this instanceof MetaMetadataCollectionField))
					inheritAttributes(inheritedMetaMetadata);
				for (MetaMetadataField inheritedField : inheritedMetaMetadata.getChildMetaMetadata())
				{
					if (!visitedMetaMetadata.contains(inheritedField) && !inheritedField.inheritInProcess && !inheritedField.ignoreCompletely)
						inheritForField(inheritedField);
				}
				if (this instanceof MetaMetadata)
				{
					((MetaMetadata) this).inheritNonFieldComponents(inheritedMetaMetadata);
				}
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
			
			inheritInProcess 						= false;
			inheritFinished = true;
		}
	}
	
	@Override
	public boolean isNewClass()
	{
		// if indicated by the author explicitly, do not generate a class
		boolean isGenerateClass = this instanceof MetaMetadata && ((MetaMetadata) this).isGenerateClass();
		
		boolean typeDefinitionIsThis	= getTypeDefinition() == this;
		
		boolean result 								= !isGenerateClass && typeDefinitionIsThis;
		return result;
	}

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return
	 */
	public abstract MetaMetadataCompositeField metaMetadataCompositeField();

	@Deprecated
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
	
	/**
	 * for caching getTypeName().
	 */
	private String typeName;
	
	@Override
	public String getTypeName()
	{
		String result = typeName;
		if (result == null)
		{
			if (this instanceof MetaMetadataCompositeField)
			{
				MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) this;
				if (mmcf.type != null)
					result = mmcf.type;
			}
			else if (this instanceof MetaMetadataCollectionField)
			{
				MetaMetadataCollectionField mmcf = (MetaMetadataCollectionField) this;
				if (mmcf.childType != null)
					result = mmcf.childType;
				else if (mmcf.childScalarType != null)
					result = mmcf.childScalarType.getJavaType();
			}
			
			if (result == null)
			{
				MetaMetadataField inherited = getInheritedField();
				if (inherited != null)
				{
					// use inherited field's type
					result = inherited.getTypeName();
				}
			}
				
			if (result == null)
			{
				// defining new type inline without using type= / child_type=
				result = getName();
			}
			
			typeName = result;
		}
		return typeName;
	}

	/**
	 * for caching getTypeDefinition().
	 */
	private MetaMetadataNestedField typeDefinition;
	
	@Deprecated
	@Override
	protected MetaMetadataNestedField getTypeDefinition()
	{
		MetaMetadataNestedField result = typeDefinition;
		if (result == null)
		{
			// get the type name
			String typeName = getTypeName();
			// search for mmd type in repository
			MetaMetadata globalMmd = getRepository().getByTagName(typeName);
			if (globalMmd != null)
				result = globalMmd;

			if (result == null)
			{
				// check if inherited field is defining a new type inline
				MetaMetadataField inherited = getInheritedField();
				if (inherited != null)
				{
					MetaMetadataNestedField inheritedDef = inherited.getTypeDefinition();
					if (inheritedDef != null)
						result = inheritedDef;
				}
			}

			if (result == null)
			{
				if (this instanceof MetaMetadataCollectionField)
				{
					// check if this is a collection field defining a new type inline
					MetaMetadataCollectionField mmcf = (MetaMetadataCollectionField) this;
					String childType = mmcf.getChildType();
					if (childType != null && childType.equals(typeName))
						result = this;
				}
				else if (this instanceof MetaMetadataCompositeField)
				{
					// check if this is a composite field defining a new type inline
					MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) this;
					String type = mmcf.getType();
					String name = mmcf.getName();
					if (type != null && type.equals(typeName) || name != null && name.equals(typeName))
						result = this;
				}
			}
			
			typeDefinition = result;
		}

		// TODO error if null: wrong type specifier!
		return typeDefinition;
	}
	
	/**
	 * this method searches for a particular child field in this meta-metadata. if not found, it also
	 * searches super mmd class along the inheritance tree.
	 * 
	 * @param name
	 * @return
	 */
	public MetaMetadataField searchForChild(String name)
	{
		MetaMetadataField child = lookupChild(name);
		if (child == null)
		{
			if (this instanceof MetaMetadata)
			{
				if (this.getName().equals("metadata"))
					return null;
				
				MetaMetadata mmd = (MetaMetadata) this;
				String superMmdName = mmd.getSuperMmdTypeName();
				MetaMetadata superMmd = getRepository().getByTagName(superMmdName);
				if (superMmd == null)
				{
					error(mmd + " specifies " + superMmdName + " as parent, but no meta-metadata for this type has been declared.");
					return null;
				}
				return superMmd.searchForChild(name);
			}
			else
			{
				MetaMetadata inheritedMmd = (MetaMetadata) getInheritedField();
				if (inheritedMmd != null)
					return inheritedMmd.searchForChild(name);
			}
		}
		return child;
	}
	
	public boolean shouldPromoteChildren()
	{
		return promoteChildren;
	}

	public void setPromoteChildren(boolean promoteChildren)
	{
		this.promoteChildren = promoteChildren;
	}

	/**
	 * @return the polymorphicGlobal
	 */
	public boolean isPolymorphicGlobal()
	{
		return polymorphicGlobal;
	}
	
	public boolean isGenerateClassDescriptor()
	{
		return generateClassDescriptor;
	}

	public void setGenerateClassDescriptor(boolean generateClassDescriptor)
	{
		this.generateClassDescriptor = generateClassDescriptor;
	}
	
	protected MetaMetadata getScopingMmd()
	{
		if (scopingMmd == null)
		{
			MetaMetadataNestedField p = (MetaMetadataNestedField) this.parent();
			if (p instanceof MetaMetadata)
				scopingMmd =  (MetaMetadata) p;
			else
				scopingMmd = p.getDeclaringMmd();
		}
		return scopingMmd;
	}
	
	void setScopingMmd(MetaMetadata scopingMmd)
	{
		this.scopingMmd = scopingMmd;
	}

//	public boolean isPolymorphic()
//	{
//		return isPolymorphic;
//	}

//	void setPolymorphic(boolean isPolymorphic)
//	{
//		this.isPolymorphic = isPolymorphic;
//	}

	public HashSet<MetaMetadata> getPolymorphicMmds()
	{
		return polymorphicMmds;
	}

	void addPolymorphicMmd(MetaMetadata polyMmd)
	{
		if (this.getInheritedField() != null)
		{
			((MetaMetadataNestedField)this.getInheritedField()).addPolymorphicMmd(polyMmd);
		}
		else
		{
			if (polymorphicMmds == null)
				polymorphicMmds = new HashSet<MetaMetadata>();
			polymorphicMmds.add(polyMmd);
		}
	}

	protected void cloneKidsTo(MetaMetadataNestedField cloned)
	{
		HashMapArrayList<String, MetaMetadataField> childMetaMetadata = this.kids;
		if (childMetaMetadata != null)
		{
			HashMapArrayList<String, MetaMetadataField> newKids = new HashMapArrayList<String, MetaMetadataField>();
			for (String kidName : childMetaMetadata.keySet())
			{
				MetaMetadataField kid = childMetaMetadata.get(kidName);
				MetaMetadataField clonedKid = (MetaMetadataField) kid.clone();
				newKids.put(kidName, clonedKid);
			}
			cloned.setChildMetaMetadata(newKids);
		}
	}
	
}
