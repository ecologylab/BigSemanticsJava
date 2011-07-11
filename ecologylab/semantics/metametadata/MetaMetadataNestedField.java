package ecologylab.semantics.metametadata;

import java.util.HashSet;

import ecologylab.generic.HashMapArrayList;
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
	 * <li>for meta-metadata: name & type/extends set;</li>
	 * <li>for fields: attributes inherited & declaringMmd set;</li>
	 * <li>for both: parent element inheritedMmd set.</li>
	 * </ul>
	 * <p>
	 * consequences:<br>
	 * <ul>
	 * <li>if this is a MetaMetadata object, attributes inherited from inheritedMmd;</li>
	 * <li>inheritedMmd set;</li>
	 * <li>(first-level) fields inherited from inheritedMmd;</li>
	 * <li>inheritedField and declaringMmd set for all (first-level) fields;</li>
	 * <li>for all (first-level) fields, attributes inherited from their inheritedField. (enabling
	 * recursion)</li>
	 * </ul>
	 */
	abstract protected void inheritMetaMetadataHelper();

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return
	 */
	public abstract MetaMetadataCompositeField metaMetadataCompositeField();

	@Override
	public String getTypeName()
	{
		String result = null;
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

		return result;
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
					error(mmd + " specifies " + superMmdName
							+ " as parent, but no meta-metadata for this type has been declared.");
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
				scopingMmd = (MetaMetadata) p;
			else
				scopingMmd = p.getDeclaringMmd();
		}
		return scopingMmd;
	}

	void setScopingMmd(MetaMetadata scopingMmd)
	{
		this.scopingMmd = scopingMmd;
	}

	/**
	 * return if this nested field is polymorphic, that is, for composites if the type of this field
	 * varies in derived fields, for collections if the child type of this field varies in derived
	 * fields.
	 * 
	 * @return
	 */
	public boolean isPolymorphic()
	{
		if (this.getInheritedField() != null)
			return ((MetaMetadataNestedField) this.getInheritedField()).isPolymorphic();
		return polymorphicMmds != null && polymorphicMmds.size() > 0;
	}

	/**
	 * get polymorphic mmds for this field, used in @simpl_classes.
	 * 
	 * @return
	 */
	public HashSet<MetaMetadata> getPolymorphicMmds()
	{
		return polymorphicMmds;
	}

	void addPolymorphicMmd(MetaMetadata polyMmd)
	{
		if (this.getInheritedField() != null)
		{
			((MetaMetadataNestedField) this.getInheritedField()).addPolymorphicMmd(polyMmd);
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

	public void makePolymorphicMmdsUseClassLevelOtherTags()
	{
		if (this.getInheritedField() != null)
		{
			((MetaMetadataNestedField) this.getInheritedField()).makePolymorphicMmdsUseClassLevelOtherTags();
			return;
		}
		for (MetaMetadata mmd : this.getPolymorphicMmds())
		{
			mmd.setUseClassLevelOtherTags(true);
		}
	}

}
