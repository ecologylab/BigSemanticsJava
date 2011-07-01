package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
@xml_tag("composite")
public class MetaMetadataCompositeField extends MetaMetadataNestedField
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

	@simpl_scalar
	private String										parser					= null;

	@simpl_collection
	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
	private ArrayList<SemanticAction>	semanticActions;

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

	MetaMetadataCompositeField(String typeName, String extendsName, HashMapArrayList<String, MetaMetadataField> kids)
	{
		this.name = typeName;
		this.type = typeName;
		this.extendsAttribute = extendsName;
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

	public String getParser()
	{
		return parser;
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
	 * 
	 */
	protected void inheritSemanticActionsFromMM(MetaMetadata inheritedMetaMetadata)
	{
		if (semanticActions == null)
		{
			semanticActions = inheritedMetaMetadata.getSemanticActions();
		}
	}

	/**
	 * @return the defVars
	 */
	public final ArrayList<DefVar> getDefVars()
	{
		return defVars;
	}

	/**
	 * @return the semanticActions
	 */
	public ArrayList<SemanticAction> getSemanticActions()
	{
		return semanticActions;
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
	 * prerequisites: type/extends defined.
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
	public void inheritMetaMetadata()
	{
		if (!inheritFinished && !inheritInProcess)
		{
			debug("inheriting " + this.toString());
			
			if (this instanceof MetaMetadata)
			{
				MetaMetadata thisMmd = (MetaMetadata) this;
				if (MetaMetadata.isRootMetaMetadata(thisMmd))
				{
					for (MetaMetadataField f : this.getChildMetaMetadata())
						f.setDeclaringMmd(thisMmd);
					inheritFinished = true;
					return;
				}
			}
			
			inheritInProcess = true;
			MetaMetadataRepository repository = getRepository();
			
			if (this.isInlineDefinition())
			{
				MetaMetadata generatedMmd = this.generateMetaMetadata();
				generatedMmd.inheritMetaMetadata(); // this will set generateClassDescriptor to true if necessary
				this.setInheritedMmd(generatedMmd);
				this.setGenerateClassDescriptor(false);
				return;
			}

			// find inheritedMmd:
			String inheritedMmdName = this.getType();
			if (inheritedMmdName == null)
			{
				inheritedMmdName = this.getExtendsAttribute();
				if (inheritedMmdName == null)
					throw new MetaMetadataException("no type / extends defined for " + this.getName());
				this.setGenerateClassDescriptor(true);
			}
			MetaMetadata inheritedMmd = repository.getByTagName(inheritedMmdName);
			if (inheritedMmd == null)
				throw new MetaMetadataException("meta-metadata not found: " + inheritedMmdName);
			this.setInheritedMmd(inheritedMmd);
			
			debug("inheritedMmd for " + this + ": " + inheritedMmd);

			// prepare inheritedMmd
			inheritedMmd.setRepository(repository);
			inheritedMmd.inheritMetaMetadata();

			// inherit attributes if it is a MetaMetadata object
			if (this instanceof MetaMetadata)
			{
				this.inheritAttributes(inheritedMmd);
				// initialize declaringMmd to this
				for (MetaMetadataField field : this.getChildMetaMetadata())
					field.setDeclaringMmd((MetaMetadata) this);
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
			
			// marking declaringMmd for newly defined fields
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
						((MetaMetadataNestedField) f).inheritMetaMetadata();
					this.setGenerateClassDescriptor(true);
				}
			}
			
			sortForDisplay();
			if (this instanceof MetaMetadata)
			{
				((MetaMetadata) this).inheritNonFieldComponentsFromMM(inheritedMmd);
			}
			inheritInProcess = false;
			inheritFinished = true;
		}
	}

	protected MetaMetadata generateMetaMetadata()
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected boolean isInlineDefinition()
	{
		return this.extendsAttribute != null;
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
