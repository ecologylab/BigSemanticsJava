package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataField implements PackageSpecifier
{
	
	public static final String	POLYMORPHIC_CLASSES_SEP	= ",";

	@simpl_composite
	@xml_tag("field_parser")
	private FieldParserElement	fieldParserElement;

	@simpl_scalar
	private boolean							promoteChildren;									// if children should be displayed
																																// at this level

	@simpl_scalar
	private String							polymorphicScope;

	/**
	 * used to specify comma-separated class tags for polymorphic classes (@simpl_classes) in
	 * meta-metadata.
	 */
	@simpl_scalar
	private String							polymorphicClasses;

	@xml_tag("package")
	@simpl_scalar
	String											packageName;

	/**
	 * the mmd used by this nested field. corresponding attributes: (child_)type/extends. could be a
	 * generated one for inline definitions.
	 */
	private MetaMetadata				inheritedMmd;

	/**
	 * the meta-metadata holding the inline meta-metadata scope for this field.
	 */
	protected MetaMetadata			scopingMmd;

	/**
	 * should we generate a metadata class descriptor for this field. used by the compiler.
	 */
	private boolean							newMetadataClass				= false;

//	/**
//	 * the set of possible meta-metadata types for this field (used only for polymorphic fields).
//	 */
//	private HashSet<MetaMetadata>	polymorphicMmds					= null;

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
				MetaMetadata superMmd = getRepository().getByName(superMmdName);
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

//	/**
//	 * return if this nested field is polymorphic in descendant fields, that is, for composites if the
//	 * type of this field varies in derived fields, for collections if the child type of this field
//	 * varies in derived fields.
//	 * <p />
//	 * NOTE THAT it is very different from {@code isPolymorphicInherently()}, which means that the field is
//	 * polymorphic by its nature. see the javadoc of the other method for more details.
//	 * 
//	 * @return
//	 * @see isPolymorphicInherently
//	 */
//	public boolean isPolymorphicInDescendantFields()
//	{
//		if (this.getInheritedField() != null)
//			return ((MetaMetadataNestedField) this.getInheritedField()).isPolymorphicInDescendantFields();
//		return polymorphicMmds != null && polymorphicMmds.size() > 0;
//	}

//	/**
//	 * get polymorphic meta-metadata types for this field. used to form @simpl_classes.
//	 * 
//	 * @return
//	 */
//	public HashSet<MetaMetadata> getPolymorphicMmds()
//	{
//		return polymorphicMmds;
//	}

//	/**
//	 * add a possible meta-metadata type for this field (and implicitly mark this field as polymorphic).
//	 * 
//	 * @param polyMmd
//	 */
//	void addPolymorphicMmd(MetaMetadata polyMmd)
//	{
//		if (this.getInheritedField() != null)
//		{
//			((MetaMetadataNestedField) this.getInheritedField()).addPolymorphicMmd(polyMmd);
//		}
//		else
//		{
//			if (polymorphicMmds == null)
//				polymorphicMmds = new HashSet<MetaMetadata>();
//			polymorphicMmds.add(polyMmd);
//		}
//	}

	protected void cloneKidsTo(MetaMetadataNestedField cloned)
	{
		HashMapArrayList<String, MetaMetadataField> childMetaMetadata = this.kids;
		if (childMetaMetadata != null)
		{
			HashMapArrayList<String, MetaMetadataField> newKids = new HashMapArrayList<String, MetaMetadataField>();
			for (String kidName : childMetaMetadata.keySet())
			{
				MetaMetadataField kid = childMetaMetadata.get(kidName);
				if (kid instanceof MetaMetadataNestedField)
					((MetaMetadataNestedField) kid).inheritMetaMetadata();
				MetaMetadataField clonedKid = (MetaMetadataField) kid.clone();
				clonedKid.setParent(cloned);
				newKids.put(kidName, clonedKid);
			}
			cloned.setChildMetaMetadata(newKids);
		}
	}

//	/**
//	 * this method is used for a polymorphic nested field, to make polymorphic meta-metadata types it
//	 * uses to use class-level @xml_other_tags instead of field-level ones. this is needed because
//	 * when a nested field is polymorphic, SIMPL ignores the @xml_other_tags annotation. as a
//	 * workaround, we put those tags as @xml_other_tags on each specific type, so that SIMPL can use
//	 * this information. this will not cause "name polution" because this information is only used in
//	 * very specific and limited context.
//	 */
//	void makePolymorphicMmdsUseClassLevelOtherTags()
//	{
//		if (this.getInheritedField() != null)
//		{
//			((MetaMetadataNestedField) this.getInheritedField()).makePolymorphicMmdsUseClassLevelOtherTags();
//			return;
//		}
//		for (MetaMetadata mmd : this.getPolymorphicMmds())
//		{
//			mmd.setUseClassLevelOtherTags(true);
//		}
//	}

//	/**
//	 * Connect the appropriate MetadataClassDescriptor with this, and likewise, recursively perform
//	 * this binding operation for all the children of this.
//	 * <p>
//	 * This method will remove this metametadata field from it's parent when no appropriate metadata
//	 * subclass was found.
//	 *
//	 * @param metadataTScope the metadata translation scope.
//	 * @return true if successful, otherwise false.
//	 */
//	@Deprecated
//	boolean getClassAndBindDescriptors(TranslationScope metadataTScope)
//	{
//		Class<? extends Metadata> metadataClass = getMetadataClass(metadataTScope);
//		if (metadataClass == null)
//		{
//			ElementState parent = parent();
//			if (parent instanceof MetaMetadataField)
//				((MetaMetadataField) parent).kids.remove(this.getName()); 
//			else if (parent instanceof MetaMetadataRepository)
//			{
//				// TODO remove from the repository level
//			}
//			return false;
//		}
//		bindClassDescriptor(metadataClass, metadataTScope);
//		return true;
//	}

	/**
	 * bind metadata field descriptors to sub-fields of this nested field, with field names as keys,
	 * but without mixins field.
	 * <p>
	 * sub-fields that lack corresponding field descriptors will be removed from this nested field.
	 * <p>
	 * note that this field no longer uses a boolean flag to prevent multiple invocation. this should
	 * have been done by the bindClassDescriptor() method.
	 * 
	 * @param metadataTScope
	 *          the translation scope of (generated) metadata classes.
	 * @param metadataClassDescriptor
	 *          the metadata class descriptor where field descriptors can be found.
	 */
	protected void bindMetadataFieldDescriptors(TranslationScope metadataTScope, MetadataClassDescriptor metadataClassDescriptor)
	{
		// copy the kids collection first to prevent modification to the collection during iteration (which may invalidate the iterator).
		List<MetaMetadataField> fields = new ArrayList<MetaMetadataField>(this.kids.values());
		for (MetaMetadataField thatChild : fields)
		{
			// look up by field name and bind
			MetadataFieldDescriptor metadataFieldDescriptor = thatChild.bindMetadataFieldDescriptor(metadataTScope, metadataClassDescriptor);
			if (metadataFieldDescriptor == null)
			{
				warning("Cannot bind metadata field descriptor for " + thatChild);
				this.kids.remove(thatChild.getName());
				continue;
			}

			// process hide and shadows
			HashSet<String> nonDisplayedFieldNames = nonDisplayedFieldNames();
			if (thatChild.hide)
				nonDisplayedFieldNames.add(thatChild.name);
			if (thatChild.shadows != null)
				nonDisplayedFieldNames.add(thatChild.shadows);

			// recursively process sub-fields
			if (thatChild instanceof MetaMetadataScalarField)
			{
				// process regex filter
				MetaMetadataScalarField scalar = (MetaMetadataScalarField) thatChild;
				if (scalar.getRegexPattern() != null)
				{
					MetadataFieldDescriptor fd = scalar.getMetadataFieldDescriptor();
					if (fd != null)
						fd.setRegexFilter(Pattern.compile(scalar.getRegexPattern()), scalar.getRegexReplacement());
					else
						warning("Encountered null fd for scalar: " + scalar);
				}
			}
			else if (thatChild instanceof MetaMetadataNestedField && thatChild.hasChildren())
			{
				// bind class descriptor for nested sub-fields
				MetadataClassDescriptor elementClassDescriptor = ((MetaMetadataNestedField) thatChild).bindMetadataClassDescriptor(metadataTScope);
				if (elementClassDescriptor == null)
				{
					warning("Cannot determine elementClassDescriptor for " + thatChild);
					this.kids.remove(thatChild.getName());
					continue;
				}
			}

			if (this instanceof MetaMetadata)
			{
				MetaMetadata mmd = (MetaMetadata) this;
				String naturalId = thatChild.getAsNaturalId();
				if (naturalId != null)
				{
					mmd.addNaturalIdField(naturalId, thatChild);
				}
			}
		}
	}

//	/**
//	 * Obtain a map of FieldDescriptors for this class, with the field names as key, but with the
//	 * mixins field removed. Use lazy evaluation, caching the result by class name.
//	 * 
//	 * @param metadataTScope
//	 *          TODO
//	 * 
//	 * @return A map of FieldDescriptors, with the field names as key, but with the mixins field
//	 *         removed.
//	 */
//	@Deprecated
//	final void bindClassDescriptor(Class<? extends Metadata> metadataClass, TranslationScope metadataTScope)
//	{
//		MetadataClassDescriptor metadataClassDescriptor = this.metadataClassDescriptor;
//		if (metadataClassDescriptor == null)
//		{
//			synchronized (this)
//			{
//				metadataClassDescriptor = this.metadataClassDescriptor;
//				if (metadataClassDescriptor == null)
//				{
//					metadataClassDescriptor = (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(metadataClass);
//					bindMetadataFieldDescriptors(metadataTScope, metadataClassDescriptor);
//					this.metadataClassDescriptor = metadataClassDescriptor;
//				}
//			}
//		}
//	}
	
	/**
	 * bind metadata class descriptor to this nested field. bind field descriptors to nested sub-fields
	 * inside this nested field, using the field names as key (but with mixins field removed).
	 * <p>
	 * lazy evaluated. result cached.
	 * 
	 * @param metadataTScope the translation scope for (generated) metadata classes.
	 * @return the bound metadata class descriptor.
	 */
	protected MetadataClassDescriptor bindMetadataClassDescriptor(TranslationScope metadataTScope)
	{
		MetadataClassDescriptor metadataCd = this.metadataClassDescriptor;
		if (metadataCd == null)
		{
			synchronized (this)
			{
				metadataCd = this.metadataClassDescriptor;
				if (metadataCd == null)
				{
					// curerntly this should never happen because binding is after inheritance process
					if (!this.inheritFinished)
						this.inheritMetaMetadata();
					
					String metadataClassSimpleName = this.getMetadataClassSimpleName();
					Class metadataClass = metadataTScope.getClassBySimpleName(metadataClassSimpleName);
					if (metadataClass != null)
					{
						this.metadataClass = metadataClass;
						metadataCd = (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(metadataClass);
						this.metadataClassDescriptor = metadataCd; // early assignment to prevent infinite loop
						this.bindMetadataFieldDescriptors(metadataTScope, metadataCd);
					}
				}
			}
		}
		return metadataCd;
	}
	
	/**
	 * 
	 * @return the corresponding Metadata class simple name.
	 */
	protected String getMetadataClassSimpleName()
	{
		return this.getInheritedMmd().getMetadataClassSimpleName();
	}
	
	
//	/**
//	 * Lookup the Metadata class object that corresponds to tag_name, type, or extends attribute
//	 * depending on which exist.
//	 * <p>
//	 * This method will only be called on composite fields, not scalar fields.
//	 * 
//	 * @return
//	 */
//	@Deprecated
//	public Class<? extends Metadata> getMetadataClass(TranslationScope ts)
//	{
//		Class<? extends Metadata> result = this.metadataClass;
//
//		if (result == null)
//		{
//			String tagForTranslationScope = getTagForTranslationScope();
//			result = (Class<? extends Metadata>) ts.getClassByTag(tagForTranslationScope);
//
//			if (result != null)
//				this.metadataClass = result;
//			else
//				ts.error("Can't resolve: " + this + " using " + tagForTranslationScope);
//		}
//		return result;
//	}

	@Override
	protected void customizeFieldDescriptor(TranslationScope metadataTScope, MetadataFieldDescriptor metadataFieldDescriptor)
	{
		super.customizeFieldDescriptor(metadataTScope, metadataFieldDescriptor);
		
		MetaMetadataNestedField inheritedField = (MetaMetadataNestedField) this.getInheritedField();
		if (inheritedField != null)
		{
			MetaMetadata superMmd = inheritedField.getInheritedMmd();
			MetaMetadata thisMmd = this.getInheritedMmd();
			if (thisMmd == superMmd)
				return;
			if (thisMmd.isDerivedFrom(superMmd))
			{
				// extending type!
				MetadataClassDescriptor metadataClassDescriptor = thisMmd.bindMetadataClassDescriptor(metadataTScope);
				metadataFieldDescriptor.setElementClassDescriptor(metadataClassDescriptor);
			}
			else
			{
				throw new MetaMetadataException("incompatible types: " + inheritedField + " => " + this);
			}
		}
	}

	/**
	 * should we generate a metadata class descriptor for this field. used by the compiler.
	 * 
	 * @return
	 */
	public boolean isNewMetadataClass()
	{
		return newMetadataClass;
	}

	/**
	 * set the flag of generating (or not) metadata class descriptoer.
	 * 
	 * @param newMetadataClass
	 * @see isGenerateClassDescriptor
	 */
	protected void setNewMetadataClass(boolean newMetadataClass)
	{
	//		debug("\n\nMarking generate class descriptor: " + this + " = " + generateClassDescriptor);
			this.newMetadataClass = newMetadataClass;
		}
	
}
