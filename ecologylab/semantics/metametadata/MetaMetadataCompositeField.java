package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;

@SuppressWarnings("rawtypes")
@simpl_inherit
@xml_tag("composite")
public class MetaMetadataCompositeField extends MetaMetadataNestedField implements MMDConstants
{

	/**
	 * The type/class of metadata object.
	 */
	@simpl_scalar
	protected String					type;

	/**
	 * the extends attribute of a composite field / meta-metadata.
	 */
	@xml_tag("extends")
	@simpl_scalar
	@mm_dont_inherit
	protected String					extendsAttribute;

	@simpl_scalar
	protected String					userAgentName;

	@simpl_scalar
	protected String					userAgentString;

	/**
	 * def_vars, used as variables during the extraction and semantic action processes.
	 */
	@simpl_collection("def_var")
	@simpl_nowrap
	private ArrayList<DefVar>	defVars;

	/**
	 * schema.org item_type name for this field.
	 */
	@simpl_scalar
	protected String					schemaOrgItemType;

	private MMSelectorType		mmSelectorType					= MMSelectorType.DEFAULT;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String						typeNameInJava					= null;

	private boolean						useClassLevelOtherTags	= false;

	public MetaMetadataCompositeField()
	{
		
	}

	MetaMetadataCompositeField(String name, HashMapArrayList<String, MetaMetadataField> kids)
	{
		this.name = name;
		this.kids.clear();
		this.kids.putAll(kids);
		for (MetaMetadataField kid : kids)
		{
			kid.setParent(this);
		}
	}

	public MetaMetadataCompositeField(MetaMetadataField copy, String name)
	{
		super(copy, name);
	}

	@Override
	protected Object clone()
	{
		MetaMetadataCompositeField cloned = new MetaMetadataCompositeField();
		cloned.setCloned(true);
		cloned.inheritAttributes(this);
		cloned.copyClonedFieldsFrom(this);
		
		this.cloneKidsTo(cloned);
		
		cloned.clonedFrom = this;
		return cloned;
	}

	@Override
	protected HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
	{
		if (kids == null)
		{
			kids = new HashMapArrayList<String, MetaMetadataField>();
		}
		
		return kids;
	}

	@Override
	public String getType()
	{
		return type;
	}

	public String getTypeOrName()
	{
		if (type != null)
			return type;
		else
			return getName();
	}

	public String getExtendsAttribute()
	{
		return extendsAttribute;
	}
	
	public void setExtendsAttribute(String extendsAttribute)
	{
		this.extendsAttribute = extendsAttribute;
		extendsChanged(extendsAttribute);
	}
	
	public void setTag(String tag)
	{
		super.setTag(tag);
		tagChanged(tag);
	}

	protected String getMetaMetadataTagToInheritFrom()
	{
		return type != null ? type : null;
	}

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return this, because it is a composite itself.
	 */
	public MetaMetadataCompositeField metaMetadataCompositeField()
	{
		return this;
	}

	public String getUserAgentString()
	{
		if (userAgentString == null)
		{
			userAgentString = (userAgentName == null) ? getRepository().getDefaultUserAgentString() :
					getRepository().getUserAgentString(userAgentName);
		}

		return userAgentString;
	}

	/**
	 * @return the defVars
	 */
	public final ArrayList<DefVar> getDefVars()
	{
		return defVars;
	}

	@Override
	public String getAdditionalAnnotationsInJava()
	{
		return " @mm_name(\"" + getName() + "\")";
	}

	@Override
	protected String getTypeNameInJava()
	{
		String rst = typeNameInJava;
		if (rst == null)
		{
			rst = XMLTools.classNameFromElementName(getTypeOrName());
			typeNameInJava = rst;
		}
		return typeNameInJava;
	}

	/**
	 * @return the mmSelectorType
	 */
	public MMSelectorType getMmSelectorType()
	{
		return mmSelectorType;
	}

	/**
	 * @param mmSelectorType the mmSelectorType to set
	 */
	public void setMmSelectorType(MMSelectorType mmSelectorType)
	{
		this.mmSelectorType = mmSelectorType;
	}

	public boolean isGenericMetadata()
	{
		return mmSelectorType == MMSelectorType.SUFFIX_OR_MIME || isBuiltIn();
	}
	
	public void setType(String type)
	{
		this.type = type;
		typeChanged(type);
	}
	
	public boolean isBuiltIn()
	{
		return false;
	}

	/**
	 * @return the schemaOrgItemType
	 */
	public String getSchemaOrgItemType()
	{
		return schemaOrgItemType;
	}

	/**
	 * this flag: 1) in MetaMetadata, enables class-level @xml_other_tags to be generated by the
	 * compiler; 2) in MetaMetadataComposite, adds a potential other tag to inheritedMmd.
	 * 
	 * @return
	 */
	public boolean isUseClassLevelOtherTags()
	{
		return this.useClassLevelOtherTags;
	}

	void setUseClassLevelOtherTags(boolean useClassLevelOtherTags)
	{
		this.useClassLevelOtherTags = useClassLevelOtherTags;
	}

	protected void inheritMetaMetadataHelper()
	{
		// init
		MetaMetadataRepository repository = getRepository();
		
		// determine the structure we should inherit from
		MetaMetadata inheritedMmd = findOrGenerateInheritedMetaMetadata(repository);
		inheritedMmd.setRepository(repository);
		inheritedMmd.inheritMetaMetadata();
		inheritFromInheritedMmd(inheritedMmd);
		if (!(this instanceof MetaMetadata))
		{
//			// potentially, use xml_tag of this field as an xml_other_tag in inheritedMmd.
//			// however, these other tags will be effective only when necessary (used in polymorphic fields)
//			inheritedMmd.addOtherTag(this.getTagOrName());
		}
		MetaMetadataCompositeField inheritedField = (MetaMetadataCompositeField) this.getInheritedField();
		if (inheritedField != null)
		{
			inheritedField.setRepository(repository);
			inheritedField.inheritMetaMetadata();
		}
		MetaMetadataCompositeField inheritedStructure = inheritedField != null ? inheritedField : inheritedMmd;
		
		// inherit fields (with attributes) from inheritedStructure
		for (MetaMetadataField field : inheritedStructure.getChildMetaMetadata())
		{
			if (field instanceof MetaMetadataNestedField)
			{
				((MetaMetadataNestedField) field).inheritMetaMetadata();
			}

			String fieldName = field.getName();
			MetaMetadataField fieldLocal = this.getChildMetaMetadata().get(fieldName);
			if (fieldLocal == null)
			{
				MetaMetadataField clonedField = (MetaMetadataField) field.clone();
				clonedField.setParent(this);
				this.getChildMetaMetadata().put(fieldName, clonedField);
			}
			else
			{
				if (field.getClass() != fieldLocal.getClass())
					warning("local field " + fieldLocal + " hides field " + fieldLocal + " with the same name in super mmd type!");

				//debug("inheriting field " + fieldLocal + " from " + field);
				fieldLocal.setInheritedField(field);
				fieldLocal.setDeclaringMmd(field.getDeclaringMmd());
				fieldLocal.inheritAttributes(field);

//				// check other tags
//				String localTag = fieldLocal.getTagOrName();
//				if (!field.getTagOrName().equals(localTag))
//				{
//					fieldLocal.addOtherTag(localTag);
//					if (fieldLocal instanceof MetaMetadataCompositeField)
//						((MetaMetadataCompositeField) fieldLocal).setUseClassLevelOtherTags(true);
//				}
			}
		}

		// recursively call this method on nested fields
		for (MetaMetadataField f : this.getChildMetaMetadata())
		{
			if (f.parent() != this || f.isCloned())
				continue; // don't need to process purely inherited fields

			// a new field is defined inside this mmd
			if (f.getDeclaringMmd() == this && f.getInheritedField() == null)
				this.setNewMetadataClass(true);

			// recursively call this method on nested fields
			if (f instanceof MetaMetadataNestedField)
			{
				MetaMetadataNestedField f1 = (MetaMetadataNestedField) f;
				f1.setRepository(repository);
				f1.setPackageName(this.packageName());

				f1.inheritMetaMetadata();
				
				MetaMetadataNestedField f0 = (MetaMetadataNestedField) f.getInheritedField();
				if (f0 != null && !f0.getTypeName().equals(f1.getTypeName()))
				{
					// inherited field w changing base type (polymorphic case)
					f1.inheritMetaMetadata();
					MetaMetadata mmd0 = f0.getInheritedMmd();
					MetaMetadata mmd1 = f1.getInheritedMmd();
					if (mmd1.isDerivedFrom(mmd0))
					{
//						f0.addPolymorphicMmd(mmd0); // the base type
//						f0.addPolymorphicMmd(mmd1);
//						f0.makePolymorphicMmdsUseClassLevelOtherTags();
						this.setNewMetadataClass(true);
					}
					else
					{
						throw new MetaMetadataException("incompatible types: " + f0 + " => " + f1);
					}
				}
			}
		}
	}

	/**
	 * inherit stuffs from inheritedMmd of this composite field. currently nothing to inherit.
	 * <p>
	 * in MetaMetadata, this method is overridden to inherit inline meta-metadata definitions.
	 * @param inheritedMmd
	 */
	protected void inheritFromInheritedMmd(MetaMetadata inheritedMmd)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * find inherited meta-metadata for this field/mmd. for fields, inheritedMmd is the mmd type it is
	 * using for itself or its children. for mmd, inheritedMmd is the mmd it directly uses (through
	 * type/extends). this method will use generatedMmd() to automatically generate a mmd definition
	 * when needed.
	 * 
	 * @param repository
	 * @return
	 */
	protected MetaMetadata findOrGenerateInheritedMetaMetadata(MetaMetadataRepository repository)
	{
 		MetaMetadata inheritedMmd = this.getInheritedMmd();
		if (inheritedMmd == null)
		{
			if (isInlineDefinition())
			{
				// try type first
				String inheritedMmdName = this.getType();
				if (inheritedMmdName != null)
				{
					inheritedMmd = repository.getByName(inheritedMmdName);
					if (inheritedMmd != null)
					{
						warning("meta-metadata " + inheritedMmdName + " found, ignoring extends/child_extends.");
					}
				}
				// try extends
				if (inheritedMmd == null)
				{
					inheritedMmdName = this.getExtendsAttribute();
					inheritedMmd = repository.getByName(inheritedMmdName);
				}
				// could be an inline mmd
				if (inheritedMmd == null)
				{
					inheritedMmd = this.getScopingMmd().getInlineMmd(inheritedMmdName);
				}
				if (inheritedMmd == null)
					throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName);
				
				// process inline mmds
				String previousName = this.getTypeOrName();
				MetaMetadata generatedMmd = this.generateMetaMetadata(previousName, inheritedMmd);
				
				// put generatedMmd in to current scope
				this.getScopingMmd().addInlineMmd(previousName, generatedMmd);
				generatedMmd.addInlineMmd(previousName, generatedMmd); // so that fields inside a inline mmd can refer to that inline mmd itself
				
				generatedMmd.inheritMetaMetadata(); // this will set generateClassDescriptor to true if necessary
				
//				this.setGenerateClassDescriptor(true);
				return generatedMmd;
			}
			else
			{
				// use type / extends
				String inheritedMmdName = this.getType();
				if (inheritedMmdName == null)
				{
					inheritedMmdName = this.getExtendsAttribute();
					if (inheritedMmdName == null)
						throw new MetaMetadataException("no type / extends defined for " + this.getName());
					this.setNewMetadataClass(true);
				}
				inheritedMmd = repository.getByName(inheritedMmdName);
				if (inheritedMmd == null && !(this instanceof MetaMetadata))
				{
					inheritedMmd = this.getScopingMmd().getInlineMmd(inheritedMmdName);
					if (inheritedMmd != null)
						this.makeThisFieldUseGeneratedMmd(this.getTypeOrName(), inheritedMmd);
				}
				
				// workaround! -- start --
				if (inheritedMmd == null)
				{
					MetaMetadataCompositeField inheritedField = (MetaMetadataCompositeField) this.getInheritedField();
					if (inheritedField != null && inheritedField.getInheritedMmd() != null)
						inheritedMmd = inheritedField.getInheritedMmd();
				}
				// workaround! -- end --
				
				if (inheritedMmd == null)
					throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName + " (if you want to define new types inline, you need to specify extends/child_extends)");
				
				// process normal mmd / field
				debug("setting " + this + ".inheritedMmd to " + inheritedMmd);
				this.setInheritedMmd(inheritedMmd);
			}
		}
		return inheritedMmd;
	}
	
	/**
	 * this method generates a new mmd from this field, and makes this field as if is using that mmd
	 * as type.
	 * 
	 * @param previousName
	 * @param inheritedMmd
	 * @return
	 */
	protected MetaMetadata generateMetaMetadata(String previousName, MetaMetadata inheritedMmd)
	{
		// generate a globally unique name
		StringBuilder sb = StringBuilderUtils.acquire();
		sb.append(MMD_PREFIX_INLINE).append(previousName);
		MetaMetadataNestedField f = (MetaMetadataNestedField) this.parent();
		while (true)
		{
			sb.append("_in_").append(f.getName());
			if (f instanceof MetaMetadata)
				break;
			f = (MetaMetadataNestedField) f.parent();
		}
		String generatedName = sb.toString();
		StringBuilderUtils.release(sb);
		
		// generate the mmd and set attributes
		MetaMetadata generatedMmd = new MetaMetadata();
		generatedMmd.setName(generatedName);
		generatedMmd.setPackageName(this.packageName());
		generatedMmd.setType(null);
		generatedMmd.setInheritedMmd(inheritedMmd);
		generatedMmd.setExtendsAttribute(inheritedMmd.getName());
		generatedMmd.setRepository(this.getRepository());
//		generatedMmd.inheritAttributes(this); // this is unnecessary here: we only have to set name/package/type/extends, which has been done in the above lines.
		
		// move nested fields (they will be cloned later)
		for (String kidKey : this.kids.keySet())
		{
			MetaMetadataField kid = this.kids.get(kidKey);
			generatedMmd.getChildMetaMetadata().put(kidKey, kid);
			kid.setParent(generatedMmd);
		}
		this.kids.clear();
		
		makeThisFieldUseGeneratedMmd(previousName, generatedMmd);
//		if (this.tag != null)
//			generatedMmd.addOtherTag(this.tag);
//		else
//			generatedMmd.addOtherTag(previousName); // potentially use @xml_other_tags if used by polymorphic fields
		this.getRepository().addMetaMetadata(generatedMmd); // add to the repository
		return generatedMmd;
	}

	/**
	 * set attributes and other members of this field, so that it is equivalent to define this field
	 * using a generated (inline) meta-metadata.
	 * 
	 * @param previousName
	 *          the previous name of this field (in contrast to generated names for inline
	 *          meta-metadatas).
	 * @param generatedMmd
	 */
	protected void makeThisFieldUseGeneratedMmd(String previousName, MetaMetadata generatedMmd)
	{
		// must set this before generatedMmd.inheritMetaMetadata() to meet inheritMetaMetadata() prerequisites
		this.setInheritedMmd(generatedMmd);
		// make this field as if is using generatedMmd as type
		this.setType(generatedMmd.getName());
		this.setExtendsAttribute(null);
		this.setTag(previousName); // but keep the tag name
	}

	/**
	 * determine if there is an inline meta-metadata defined by this field.
	 * 
	 * @return
	 */
	protected boolean isInlineDefinition()
	{
		return this.extendsAttribute != null;
	}
	
	/**
	 * hook method for updating collection field's child_type when its childComposite changes type.
	 * 
	 * @param newType
	 */
	protected void typeChanged(String newType)
	{
		// hook method
	}
	
	/**
	 * hook method for updating collection field's child_extends when its childComposite changes extends.
	 * 
	 * @param newType
	 */
	protected void extendsChanged(String newExtends)
	{
		// hook method
	}
	
	/**
	 * hook method for updating collection field's child_tag when its childComposite changes tag.
	 * 
	 * @param newType
	 */
	protected void tagChanged(String newTag)
	{
		// hook method
	}

	@Override
	public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(TranslationScope tscope, MetadataClassDescriptor contextCd)
	{
		MetadataFieldDescriptor fd = this.getMetadataFieldDescriptor();
		if (fd == null)
		{
			String tagName = this.resolveTag();
			String fieldName = this.getFieldNameInJava(false);
			String javaTypeName = this.getTypeNameInJava();

			MetaMetadata inheritedMmd = this.getInheritedMmd();
			assert inheritedMmd != null : "IMPOSSIBLE: inheritedMmd == null: something wrong in the inheritance process!";
			inheritedMmd.findOrGenerateMetadataClassDescriptor(tscope);
			MetadataClassDescriptor fieldCd = inheritedMmd.getMetadataClassDescriptor();
			assert fieldCd != null : "IMPOSSIBLE: fieldCd == null: something wrong in the inheritance process!";
			fd = new MetadataFieldDescriptor(
						this,
						tagName,
						this.getComment(),
						this.getFieldType(),
						fieldCd,
						contextCd,
						fieldName,
						null,
						null,
						javaTypeName);
		}
		this.metadataFieldDescriptor = fd;
		return fd;
	}

//	@Deprecated
//	@Override
//	public Class<? extends Metadata> getMetadataClass(TranslationScope ts)
//	{
//		Class<? extends Metadata> result = this.metadataClass;
//		
//		if (result == null)
//		{
//			// if type= defined, use it
//			String type = getType();
//			if (type != null)
//			{
//				result = (Class<? extends Metadata>) ts.getClassByTag(type);
//				if (result == null)
//				{
//					// the type name doesn't work, but we can try super mmd class
//					MetaMetadata superMmd = getRepository().getByTagName(type);
//					if (superMmd != null)
//						result = superMmd.getMetadataClass(ts);
//				}
//			}
//			
//			if (result == null)
//			{
//				// then try name
//				String tagOrName = getTagOrName();
//				result = (Class<? extends Metadata>) ts.getClassByTag(tagOrName);
//			}
//			
//			if (result == null)
//			{
//				// if type and name don't work, try extends=
//				// there is no class for this tag we can use class of meta-metadata it extends
//				result = (Class<? extends Metadata>) ts.getClassByTag(((MetaMetadataCompositeField)this).getExtendsAttribute());
//			}
//			
//			if (result != null)
//				this.metadataClass = result;
//			else
//				warning("Can't resolve metadata class for " + this);
//		}
//		
//		return result;
//	}
	
}
