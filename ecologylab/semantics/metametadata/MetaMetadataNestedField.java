package ecologylab.semantics.metametadata;

import java.util.HashSet;

import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataField implements PackageSpecifier
{
	
	public static final String		POLYMORPHIC_CLASSES_SEP	= ",";

	@simpl_composite
	@xml_tag("field_parser")
	private FieldParserElement		fieldParserElement;

	@simpl_scalar
	private boolean								promoteChildren;									// if children should be displayed
																																	// at this level

	@simpl_scalar
	private String								polymorphicScope;

	/**
	 * used to specify comma-separated class tags for polymorphic classes (@simpl_classes) in meta-metadata.
	 */
	@simpl_scalar
	private String								polymorphicClasses;

	@xml_tag("package")
	@simpl_scalar
	String												packageName;

	/**
	 * the mmd used by this nested field. corresponding attributes: (child_)type/extends. could be a
	 * generated one for inline definitions.
	 */
	private MetaMetadata					inheritedMmd;
	
	/**
	 * should we generate a metadata class descriptor for this field. used by the compiler.
	 */
	private boolean								generateClassDescriptor	= false;

	/**
	 * the meta-metadata holding the inline meta-metadata scope for this field. 
	 */
	protected MetaMetadata				scopingMmd;

	/**
	 * the set of possible meta-metadata types for this field (used only for polymorphic fields).
	 */
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

	/**
	 * get the inherited meta-metadata type of this field.
	 * 
	 * @return
	 */
	public MetaMetadata getInheritedMmd()
	{
		return inheritedMmd;
	}

	/**
	 * set the inherited meta-metadata type of this field.
	 * 
	 * @param inheritedMmd
	 */
	protected void setInheritedMmd(MetaMetadata inheritedMmd)
	{
		this.inheritedMmd = inheritedMmd;
		if (this instanceof MetaMetadata)
			inheritedMmd.addDerivedMmd((MetaMetadata) this);
	}

	public FieldParserElement getFieldParserElement()
	{
		return fieldParserElement;
	}

	abstract protected String getMetaMetadataTagToInheritFrom();

	/**
	 * The main entrance to the inheritance process. Also ensures that the inheritance process will
	 * not be carried out on the same field / meta-metadata twice.
	 * <p>
	 * Note that this inheritance process is used by both the compiler and the run-time repository
	 * loading process, thus there should be nothing about generating Class/FieldDescriptors inside.
	 * <p>
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
	 * Helper method that actually does the inheritance process. This should be overridden in
	 * sub-classes to fine-control the inheritance process.
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
				result = mmcf.childScalarType.getJavaTypeName();
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

	public String getPolymorphicScope()
	{
		return this.polymorphicScope;
	}
	
	public String getPolymorphicClasses()
	{
		return this.polymorphicClasses;
	}

	/**
	 * to determine if this field is polymorphic inherently, that is, a field which we don't have
	 * prior knowledge of its specific meta-metadata type when its encompassing meta-metadata is used.
	 * <p />
	 * NOTE THAT this is different from {@code isPolymorphicInDescendantFields()} which determines if
	 * this field is used for extended types in descendant fields. in that case although the field is
	 * polymorphic, too, but we can determine the specific meta-metadata type for this field if the
	 * encompassing meta-metadata is used.
	 * 
	 * @return
	 * @see isPolymorphicInDescendantFields
	 */
	public boolean isPolymorphicInherently()
	{
		return (polymorphicScope != null && polymorphicScope.length() > 0) || (polymorphicClasses != null && polymorphicClasses.length() > 0);
	}
	
	/**
	 * should we generate a metadata class descriptor for this field. used by the compiler.
	 * 
	 * @return
	 */
	public boolean isGenerateClassDescriptor()
	{
		return generateClassDescriptor;
	}

	/**
	 * set the flag of generating (or not) metadata class descriptoer.
	 * 
	 * @param generateClassDescriptor
	 * @see isGenerateClassDescriptor
	 */
	void setGenerateClassDescriptor(boolean generateClassDescriptor)
	{
		this.generateClassDescriptor = generateClassDescriptor;
	}

	/**
	 * get the meta-metadata holding the inline meta-metadata scope for this field.
	 * 
	 */
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

	/**
	 * set the meta-metadata holding the inline meta-metadata scope for this field.
	 * 
	 * @param scopingMmd
	 */
	void setScopingMmd(MetaMetadata scopingMmd)
	{
		this.scopingMmd = scopingMmd;
	}

	/**
	 * return if this nested field is polymorphic in descendant fields, that is, for composites if the
	 * type of this field varies in derived fields, for collections if the child type of this field
	 * varies in derived fields.
	 * <p />
	 * NOTE THAT it is very different from {@code isPolymorphicInherently()}, which means that the field is
	 * polymorphic by its nature. see the javadoc of the other method for more details.
	 * 
	 * @return
	 * @see isPolymorphicInherently
	 */
	public boolean isPolymorphicInDescendantFields()
	{
		if (this.getInheritedField() != null)
			return ((MetaMetadataNestedField) this.getInheritedField()).isPolymorphicInDescendantFields();
		return polymorphicMmds != null && polymorphicMmds.size() > 0;
	}

	/**
	 * get polymorphic meta-metadata types for this field. used to form @simpl_classes.
	 * 
	 * @return
	 */
	public HashSet<MetaMetadata> getPolymorphicMmds()
	{
		return polymorphicMmds;
	}

	/**
	 * add a possible meta-metadata type for this field (and implicitly mark this field as polymorphic).
	 * 
	 * @param polyMmd
	 */
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
				clonedKid.setParent(cloned);
				newKids.put(kidName, clonedKid);
			}
			cloned.setChildMetaMetadata(newKids);
		}
	}

	/**
	 * this method is used for a polymorphic nested field, to make polymorphic meta-metadata types it
	 * uses to use class-level @xml_other_tags instead of field-level ones. this is needed because
	 * when a nested field is polymorphic, SIMPL ignores the @xml_other_tags annotation. as a
	 * workaround, we put those tags as @xml_other_tags on each specific type, so that SIMPL can use
	 * this information. this will not cause "name polution" because this information is only used in
	 * very specific and limited context.
	 */
	void makePolymorphicMmdsUseClassLevelOtherTags()
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
