package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
@xml_tag("composite")
public class MetaMetadataCompositeField extends MetaMetadataNestedField implements MMDConstants
{

	/**
	 * The type/class of metadata object.
	 */
	@simpl_scalar
	protected String									type;

	@xml_tag("extends")
	@simpl_scalar
	@mm_dont_inherit
	protected String									extendsAttribute;

	@simpl_scalar
	protected String									userAgentName;

	@simpl_scalar
	protected String									userAgentString;

	@simpl_collection("def_var")
	@simpl_nowrap
	private ArrayList<DefVar>					defVars;

	@simpl_scalar
	protected String									schemaOrgItemType;

	private MMSelectorType						mmSelectorType	= MMSelectorType.DEFAULT;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String										typeNameInJava	= null;
	
	/**
	 * (cached scopeMmd).
	 */
	private MetaMetadata							scopeMmd = null;
	
	public MetaMetadataCompositeField()
	{
		
	}

	MetaMetadataCompositeField(String name, HashMapArrayList<String, MetaMetadataField> kids)
	{
		this.name = name;
		this.kids = new HashMapArrayList<String, MetaMetadataField>();
		if (kids != null)
			this.kids.putAll(kids);
	}

	public MetaMetadataCompositeField(MetaMetadataField copy, String name)
	{
		super(copy, name);
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

	public String getTagForTranslationScope()
	{
		return tag != null ? tag : name;
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

	@Deprecated
	@Override
	public String getAnnotationsInJava()
	{
		String tagDecl = getTagDecl();
		return "@simpl_composite" + (tagDecl.length() > 0 ? (" " + tagDecl) : "")
					 + " @mm_name(\"" + getName() + "\")";
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

	public void inheritMetaMetadata()
	{
		if (!inheritFinished && !inheritInProcess)
		{
			debug("inheriting " + this.toString());
			
			// a terminating point of recursion: do not inherit for root mmd 
			if (this instanceof MetaMetadata)
			{
				MetaMetadata thisMmd = (MetaMetadata) this;
				if (MetaMetadata.isRootMetaMetadata(thisMmd))
				{
					for (MetaMetadataField f : this.getChildMetaMetadata())
					{
						f.setDeclaringMmd(thisMmd);
					}
					inheritFinished = true;
					return;
				}
			}
			
			// init
			inheritInProcess = true;
			MetaMetadataRepository repository = getRepository();
			
			// find and prepare inheritedMmd
			MetaMetadata inheritedMmd = findInheritedMetaMetadata(repository);
			inheritedMmd.setRepository(repository);
			inheritedMmd.inheritMetaMetadata();
			
			// inherit from inheritedMmd, for meta-metadata specific things
			if (this instanceof MetaMetadata)
			{
				((MetaMetadata) this).inheritInlineMmds(inheritedMmd);
				this.inheritAttributes(inheritedMmd); // don't do this for fields: fields should inherit attributes from inheritedField
				for (MetaMetadataField field : this.getChildMetaMetadata())
				{
					// initialize declaringMmd to this (some may be changed later)
					field.setDeclaringMmd((MetaMetadata) this);
				}
			}
			
			// inherit fields (with attributes) from inheritedMmd
			for (MetaMetadataField field : inheritedMmd.getChildMetaMetadata())
			{
				if (field instanceof MetaMetadataNestedField)
					((MetaMetadataNestedField) field).inheritMetaMetadata();

				String fieldName = field.getName();
				MetaMetadataField fieldLocal = this.getChildMetaMetadata().get(fieldName);
				if (fieldLocal == null)
				{
					this.getChildMetaMetadata().put(fieldName, field);
				}
				else
				{
					debug("inheriting field: " + fieldLocal);
					fieldLocal.setInheritedField(field);
					fieldLocal.setDeclaringMmd(field.getDeclaringMmd());
					fieldLocal.inheritAttributes(field);
					if (fieldLocal instanceof MetaMetadataNestedField && field instanceof MetaMetadataNestedField)
						((MetaMetadataNestedField)fieldLocal).setInheritedMmd(((MetaMetadataNestedField)field).getInheritedMmd());
				}
			}
			
			// recursively call this method on nested fields
			for (MetaMetadataField f : this.getChildMetaMetadata())
			{
				if (f.getDeclaringMmd() == this)
				{
					if (!(this instanceof MetaMetadata))
					{
						// this must be a nested field w/o extends/child_extends (or we would have processed it
						// as inline mmd), but actually defines new fields 
						throw new MetaMetadataException("to define new fields inline, you need to specify extends!");
					}
					this.setGenerateClassDescriptor(true);
				}
				
				// if it is a nested field, no matter if it is declared in this meta-metadata, we have to
				// process its nested fields by calling inheritedMetaMetadata(). and we know that its
				// attributes have been inherited correctly.
				if (f instanceof MetaMetadataNestedField)
				{
					f.setRepository(repository);
					((MetaMetadataNestedField) f).setPackageName(this.packageName());
					((MetaMetadataNestedField) f).inheritMetaMetadata();
				}
				
				f.inheritInProcess = false;
				f.inheritFinished = true;
			}
			
			// inherit other stuffs
			if (this instanceof MetaMetadata)
				((MetaMetadata) this).inheritNonFieldComponents(inheritedMmd);
			
			sortForDisplay();
			inheritInProcess = false;
			inheritFinished = true;
		}
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
	protected MetaMetadata findInheritedMetaMetadata(MetaMetadataRepository repository)
	{
		MetaMetadata inheritedMmd = this.getInheritedMmd();
		if (inheritedMmd == null)
		{
			if (isInlineDefinition())
			{
				String inheritedMmdName = this.getExtendsAttribute();
				inheritedMmd = repository.getByTagName(inheritedMmdName);
				if (inheritedMmd == null)
				{
					// could be an inline mmd type
					// this must not be meta-metadata; MetaMetadata.isInlineDefinition() returns false
					MetaMetadata scopeMmd = getScopeMmd();
					inheritedMmd = scopeMmd == null ? null : scopeMmd.getInlineMmd(inheritedMmdName);
					if (inheritedMmd == null)
						throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName);
				}
				
				// process inline mmds
				String previousName = this.getTypeOrName();
				MetaMetadata generatedMmd = this.generateMetaMetadata(previousName, inheritedMmd);
				
				// put generatedMmd in to current scope
				MetaMetadata scopeMmd = getScopeMmd();
				scopeMmd.addInlineMmd(previousName, generatedMmd);
				
				generatedMmd.inheritMetaMetadata(); // this will set generateClassDescriptor to true if necessary
				
				this.setGenerateClassDescriptor(false);
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
					this.setGenerateClassDescriptor(true);
				}
				inheritedMmd = repository.getByTagName(inheritedMmdName);
				if (inheritedMmd == null && !(this instanceof MetaMetadata))
				{
					MetaMetadata scopeMmd = getScopeMmd();
					inheritedMmd = scopeMmd == null ? null : scopeMmd.getInlineMmd(inheritedMmdName);
				}
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
	 * get the mmd with a correct scope of inline mmd definitions. more specifically, if this is a
	 * field inside a mmd, we look up that mmd for inline mmd definitions (that mmd should have
	 * already inherited inline definitions from its ancestors); if this is a field inside another
	 * nested field, we look up that nested field's declaringMmd (which should also have already
	 * inherited inline definitions from its ancestors).
	 * <p>
	 * prerequisite: this is not a MetaMetadata
	 * 
	 * @return
	 */
	private MetaMetadata getScopeMmd()
	{
		if (scopeMmd == null)
		{
			assert !(this instanceof MetaMetadata): "should never call getScopeMmd on a MetaMetadata because MetaMetadata will never be inline!";
			MetaMetadataNestedField parent = (MetaMetadataNestedField) this.parent();
			if (parent instanceof MetaMetadata)
				scopeMmd = (MetaMetadata) parent;
			else
				scopeMmd = parent.getDeclaringMmd();
		}
		return scopeMmd;
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
		generatedMmd.inheritAttributes(this);
		
		// move nested fields
		for (String kidKey : this.kids.keySet())
		{
			MetaMetadataField kid = this.kids.get(kidKey);
			generatedMmd.getChildMetaMetadata().put(kidKey, kid);
			kid.setParent(generatedMmd);
		}
		this.kids.clear();
		
		// must set this before generatedMmd.inheritMetaMetadata() to meet inheritMetaMetadata() prerequisites
		this.setInheritedMmd(generatedMmd);
		
		// make this field as if is using generatedMmd as type
		this.setType(generatedMmd.getName());
		this.setExtendsAttribute(null);
		this.setTag(previousName); // but keep the tag name
		
		this.getRepository().addMetaMetadata(generatedMmd); // add to the repository
		return generatedMmd;
	}

	protected boolean isInlineDefinition()
	{
		return this.extendsAttribute != null;
	}
	
	protected void typeChanged(String newType)
	{
		// hook method
	}
	
	protected void extendsChanged(String newExtends)
	{
		// hook method
	}
	
	protected void tagChanged(String newTag)
	{
		// hook method
	}

	@Override
	public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(MetadataClassDescriptor contextCd)
	{
		MetadataFieldDescriptor fd = this.getMetadataFieldDescriptor();
		if (fd == null)
		{
			String tagName = this.resolveTag();
			String fieldName = this.getFieldNameInJava(false);
			String javaTypeName = this.getTypeNameInJava();

			MetaMetadata inheritedMmd = this.getInheritedMmd();
			assert inheritedMmd != null : "IMPOSSIBLE: inheritedMmd == null: something wrong in the inheritance process!";
			MetadataClassDescriptor fieldCd = inheritedMmd.getMetadataClassDescriptor();
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
		return fd;
	}

}
