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
			
			// inherit inline mmds, which could be used by fields inside this
			if (this instanceof MetaMetadata)
			{
				((MetaMetadata) this).inheritInlineMmds(inheritedMmd);
			}

			if (this instanceof MetaMetadata)
			{
				// inherit attributes if it is a MetaMetadata object
				this.inheritAttributes(inheritedMmd);
				// initialize declaringMmd to this
				for (MetaMetadataField field : this.getChildMetaMetadata())
				{
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
					
					if (f instanceof MetaMetadataNestedField)
					{
						f.setRepository(repository);
						((MetaMetadataNestedField) f).setPackageName(this.packageName());
						((MetaMetadataNestedField) f).inheritMetaMetadata();
					}
					this.setGenerateClassDescriptor(true);
				}
			}
			
			// inherit other stuffs
			if (this instanceof MetaMetadata)
				((MetaMetadata) this).inheritNonFieldComponents(inheritedMmd);
			
			sortForDisplay();
			inheritInProcess = false;
			inheritFinished = true;
		}
	}

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
					Scope<MetaMetadata> inlineMmds = scopeMmd.getInlineMmds();
					inheritedMmd = inlineMmds == null ? null : inlineMmds.get(inheritedMmdName);
					if (inheritedMmd == null)
						throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName);
				}
				
				// process inline mmds
				String previousName = this.getTypeOrName();
				MetaMetadata generatedMmd = this.generateMetaMetadata(previousName, inheritedMmd);
				repository.addMetaMetadata(generatedMmd);
				this.setType(generatedMmd.getName());
				this.setExtendsAttribute(null);
				
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
				if (inheritedMmd == null)
					throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName);
				
				// process normal mmd / field
				this.setInheritedMmd(inheritedMmd);
				debug("inheritedMmd for " + this + ": " + inheritedMmd);
			}
		}
		return inheritedMmd;
	}

	private MetaMetadata getScopeMmd()
	{
		MetaMetadataNestedField parent = (MetaMetadataNestedField) this.parent();
		MetaMetadata scopeMmd = null;
		if (parent instanceof MetaMetadata)
			scopeMmd = (MetaMetadata) parent;
		else
			scopeMmd = parent.getDeclaringMmd();
		return scopeMmd;
	}

	protected MetaMetadata generateMetaMetadata(String previousName, MetaMetadata inheritedMmd)
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
		
		MetaMetadata generatedMmd = new MetaMetadata();
		generatedMmd.setName(generatedName);
		generatedMmd.setPackageName(this.packageName());
		generatedMmd.setType(null);
		generatedMmd.setInheritedMmd(inheritedMmd);
		generatedMmd.setExtendsAttribute(inheritedMmd.getName());
		generatedMmd.setRepository(this.getRepository());
		generatedMmd.inheritAttributes(this);
		generatedMmd.setChildMetaMetadata(this.kids);
		for (MetaMetadataField kid : this.kids)
		{
			kid.setParent(generatedMmd);
		}
		
		// must set this before generatedMmd.inheritMetaMetadata() to meet inheritMetaMetadata() prerequisites
		this.setInheritedMmd(generatedMmd);
		return generatedMmd;
	}

	protected boolean isInlineDefinition()
	{
		return this.extendsAttribute != null;
	}
	
	protected void typeChanged(String newType)
	{
		
	}
	
	protected void extendsChanged(String newExtends)
	{
		
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
