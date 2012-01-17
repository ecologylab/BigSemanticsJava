package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.collections.MultiAncestorScope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.mm_dont_inherit;
import ecologylab.semantics.metametadata.MetaMetadata.Visibility;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

@SuppressWarnings({"rawtypes", "unchecked"})
@simpl_inherit
@simpl_tag("composite")
public class MetaMetadataCompositeField extends MetaMetadataNestedField implements MMDConstants
{
	
	static interface AttributeChangeListener
	{
			void typeChanged(String newType);
			void extendsChanged(String newExtends);
			void tagChanged(String newTag);
	}

	/**
	 * The type/class of metadata object.
	 */
	@simpl_scalar
	protected String					type;

	/**
	 * the extends attribute of a composite field / meta-metadata.
	 */
	@simpl_tag("extends")
	@simpl_scalar
	@mm_dont_inherit
	protected String					extendsAttribute;

	// FIXME move to MetaMetadata
	@simpl_scalar
	protected String					userAgentName;

	// FIXME move to MetaMetadata
	@simpl_scalar
	protected String					userAgentString;

	/**
	 * def_vars, used as variables during the extraction and semantic action processes.
	 */
	@simpl_collection("def_var")
	@simpl_nowrap
	private ArrayList<DefVar>	defVars;

	private MMSelectorType		mmSelectorType					= MMSelectorType.DEFAULT;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String						typeNameInJava					= null;

	private boolean						useClassLevelOtherTags	= false;
	
	private AttributeChangeListener	attributeChangeListener = null;

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
		return type == null ? name : type;
	}

	public String getExtendsAttribute()
	{
		return extendsAttribute;
	}
	
	public void setExtendsAttribute(String extendsAttribute)
	{
		this.extendsAttribute = extendsAttribute;
		if (attributeChangeListener != null)
			attributeChangeListener.extendsChanged(extendsAttribute);
	}
	
	@Override
	public void setTag(String tag)
	{
		super.setTag(tag);
		if (attributeChangeListener != null)
			attributeChangeListener.tagChanged(tag);
	}

	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		return type != null ? type : null;
	}

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return this, because it is a composite itself.
	 */
	@Override
	public MetaMetadataCompositeField metaMetadataCompositeField()
	{
		return this;
	}

	public String getUserAgentString()
	{
		if (userAgentString == null)
		{
			userAgentString = (userAgentName == null) ? getRepository().getDefaultUserAgentString() : getRepository().getUserAgentString(userAgentName);
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
		if (attributeChangeListener != null)
			attributeChangeListener.typeChanged(type);
	}
	
	public boolean isBuiltIn()
	{
		return false;
	}

	/**
	 * this flag: 1) in MetaMetadata, enables class-level @simpl_other_tags to be generated by the
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

	@Override
	protected void inheritMetaMetadataHelper()
	{
		// init
		MetaMetadataRepository repository = this.getRepository();
		
		// determine the structure we should inherit from
		MetaMetadata inheritedMmd = findOrGenerateInheritedMetaMetadata(repository);
		if (inheritedMmd != null)
		{
			inheritedMmd.inheritMetaMetadata();
			inheritFromInheritedMmd(inheritedMmd);
			inheritMetaMetadataFrom(repository, inheritedMmd);
		}
		MetaMetadataCompositeField inheritedField = (MetaMetadataCompositeField) this.getInheritedField();
		if (inheritedField != null)
		{
			inheritedField.setRepository(repository);
			inheritedField.inheritMetaMetadata();
			inheritMetaMetadataFrom(repository, inheritedField);
		}
		
		// for the root meta-metadata, this may happend
		if (inheritedMmd == null && inheritedField == null)
			inheritMetaMetadataFrom(repository, null);
	}

	protected void inheritMetaMetadataFrom(MetaMetadataRepository repository, MetaMetadataCompositeField inheritedStructure)
	{
		// init nested fields inside this
		for (MetaMetadataField f : this.getChildMetaMetadata())
			if (f instanceof MetaMetadataNestedField)
			{
				f.setRepository(repository);
				MetaMetadataNestedField nested = (MetaMetadataNestedField) f;
				if (nested.packageName() == null)
					nested.setPackageName(this.packageName());
				nested.setMmdScope(this.getMmdScope());
			}
		
		// inherit fields with attributes from inheritedStructure
		// if inheritedStructure == null, this must be the root meta-metadata
		if (inheritedStructure != null)
		{
			for (MetaMetadataField field : inheritedStructure.getChildMetaMetadata())
			{
				if (field instanceof MetaMetadataNestedField)
				{
					((MetaMetadataNestedField) field).inheritMetaMetadata();
				}
				String fieldName = field.getName();
				MetaMetadataField fieldLocal = this.getChildMetaMetadata().get(fieldName);
				if (fieldLocal != null)
				{
					if (field.getClass() != fieldLocal.getClass())
						warning("local field " + fieldLocal + " hides field " + fieldLocal + " with the same name in super mmd type!");
					if (field != fieldLocal)
						fieldLocal.setInheritedField(field);
					fieldLocal.setDeclaringMmd(field.getDeclaringMmd());
					fieldLocal.inheritAttributes(field);
					if (fieldLocal instanceof MetaMetadataNestedField)
						((MetaMetadataNestedField) fieldLocal).setPackageName(((MetaMetadataNestedField) field).packageName());
				}
			}
		}

		// recursively call inheritMetaMetadata() on nested fields
		for (MetaMetadataField f : this.getChildMetaMetadata())
		{
			// a new field is defined inside this mmd
			if (f.getDeclaringMmd() == this && f.getInheritedField() == null)
				this.setNewMetadataClass(true);

			// recursively call this method on nested fields
			f.setRepository(repository);
			if (f instanceof MetaMetadataNestedField)
			{
				MetaMetadataNestedField f1 = (MetaMetadataNestedField) f;
				f1.inheritMetaMetadata();
				if (f1.isNewMetadataClass())
					this.setNewMetadataClass(true);
				
				MetaMetadataNestedField f0 = (MetaMetadataNestedField) f.getInheritedField();
				if (f0 != null && !f0.getTypeName().equals(f1.getTypeName()))
				{
					// inherited field w changing base type (polymorphic case)
					f1.inheritMetaMetadata();
					MetaMetadata mmd0 = f0.getInheritedMmd();
					MetaMetadata mmd1 = f1.getInheritedMmd();
					if (mmd1.isDerivedFrom(mmd0))
						this.setNewMetadataClass(true);
					else
						throw new MetaMetadataException("incompatible types: " + f0 + " => " + f1);
				}
			}
		}
		
		// clone fields only declared in inheritedStructure.
		// must clone them after recursively calling inheritMetaMetadata(), so that their nested
		// structures (which may be inherited too) can be cloned.
		if (inheritedStructure != null)
		{
			for (MetaMetadataField field : inheritedStructure.getChildMetaMetadata())
			{
				String fieldName = field.getName();
				MetaMetadataField fieldLocal = this.getChildMetaMetadata().get(fieldName);
				if (fieldLocal == null)
				{
//					MetaMetadataField clonedField = (MetaMetadataField) field.clone();
//					clonedField.setParent(this);
//					this.getChildMetaMetadata().put(fieldName, clonedField);
					this.getChildMetaMetadata().put(fieldName, field);
				}
			}
		}
	}

	/**
	 * inherit stuffs from inheritedMmd of this composite field.
	 * 
	 * @param inheritedMmd
	 */
	protected void inheritFromInheritedMmd(MetaMetadata inheritedMmd)
	{
		this.setMmdScope(new MultiAncestorScope<MetaMetadata>(this.getMmdScope(), inheritedMmd.getMmdScope()));
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
			MultiAncestorScope<MetaMetadata> mmdScope = this.getMmdScope();
			
			if (isInlineDefinition())
			{
				// determine new type name
				String newTypeName = this.getType();
				if (newTypeName != null && mmdScope.get(newTypeName) != null)
					// currently we don't encourage re-using existing name. however, in the future, when package names are available, we can change this.
					throw new MetaMetadataException("meta-metadata '" + newTypeName + "' already exists! please use another name to prevent name collision. hint: use 'tag' to change the tag if needed.");
				if (newTypeName == null)
					newTypeName = this.getName();
				if (newTypeName == null)
					throw new MetaMetadataException("attribute 'name' must be specified: " + this);
				
				// determine from which meta-metadata to inherit
				String inheritedMmdName = this.getExtendsAttribute();
				if (inheritedMmdName == null || mmdScope.get(inheritedMmdName) == null)
					throw new MetaMetadataException("super type not specified or not found: " + this + ", super type name: " + inheritedMmdName);
				inheritedMmd = mmdScope.get(inheritedMmdName);
				
				// generate inline mmds and put it into current scope
				MetaMetadata generatedMmd = this.generateMetaMetadata(newTypeName, inheritedMmd);
				mmdScope.put(newTypeName, generatedMmd);
				mmdScope.put(generatedMmd.getName(), generatedMmd);
				
				// recursively do inheritance on generated mmd
				generatedMmd.inheritMetaMetadata(); // this will set generateClassDescriptor to true if necessary
				
				this.makeThisFieldUseMmd(newTypeName, generatedMmd);
				return generatedMmd;
			}
			else
			{
				// use type / extends
				String inheritedMmdName = this.getType() == null ? this.getName() : this.getType();
				if (inheritedMmdName == null || mmdScope.get(inheritedMmdName) == null)
				{
					inheritedMmdName = this.getExtendsAttribute();
					if (inheritedMmdName == null)
						throw new MetaMetadataException("no type / extends defined for " + this);
					this.setNewMetadataClass(true);
				}
				inheritedMmd = mmdScope.get(inheritedMmdName);
				if (inheritedMmd != null && !inheritedMmdName.equals(inheritedMmd.getName()))
				{
					// could be inline mmd
					this.makeThisFieldUseMmd(this.getTypeOrName(), inheritedMmd);
				}
				
				if (inheritedMmd == null)
					throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName + " (if you want to define new types inline, you need to specify extends/child_extends)");
				
				// process normal mmd / field
//				debug("setting " + this + ".inheritedMmd to " + inheritedMmd);
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
		String generatedName = getGeneratedMmdName2(previousName);
		
		// generate the mmd and set attributes
		MetaMetadata generatedMmd = new MetaMetadata();
		generatedMmd.setName(generatedName);
		generatedMmd.setPackageName(this.packageName());
		generatedMmd.setType(null);
		generatedMmd.setInheritedMmd(inheritedMmd);
		generatedMmd.setExtendsAttribute(inheritedMmd.getName());
		generatedMmd.setRepository(this.getRepository());
		generatedMmd.visibility = Visibility.PACKAGE;
		generatedMmd.setMmdScope(new MultiAncestorScope<MetaMetadata>(this.getMmdScope(), inheritedMmd.getMmdScope()));
		if (this.getSchemaOrgItemtype() != null)
			generatedMmd.setSchemaOrgItemtype(this.getSchemaOrgItemtype());
		generatedMmd.setNewMetadataClass(true);
		
		// move nested fields (they will be cloned later)
		for (String kidKey : this.kids.keySet())
		{
			MetaMetadataField kid = this.kids.get(kidKey);
			generatedMmd.getChildMetaMetadata().put(kidKey, kid);
			kid.setParent(generatedMmd);
		}
		this.kids.clear();
		
		makeThisFieldUseMmd(previousName, generatedMmd);
		return generatedMmd;
	}

	private String getGeneratedMmdName1(String previousName)
	{
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
		return generatedName;
	}
	
	private String getGeneratedMmdName2(String previousName)
	{
		return previousName;
	}

	/**
	 * set attributes and other members of this field, so that it is equivalent to define this field
	 * using a specific (inline) meta-metadata.
	 * 
	 * @param previousName
	 *          the previous name of this field (in contrast to generated names for inline
	 *          meta-metadatas).
	 * @param mmd
	 */
	protected void makeThisFieldUseMmd(String previousName, MetaMetadata mmd)
	{
		// must set this before generatedMmd.inheritMetaMetadata() to meet inheritMetaMetadata() prerequisites
		this.setInheritedMmd(mmd);
		// make this field as if is using generatedMmd as type
		this.setType(mmd.getName());
		this.setExtendsAttribute(null);
		if (this.tag == null)
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
	
	public void setAttributeChangeListener(AttributeChangeListener attributeChangeListener)
	{
		this.attributeChangeListener = attributeChangeListener;
	}

	@Override
	public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(SimplTypesScope tscope, MetadataClassDescriptor contextCd)
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

}
