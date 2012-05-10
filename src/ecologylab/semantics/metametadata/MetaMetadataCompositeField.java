package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.Stack;

import ecologylab.collections.MultiAncestorScope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.mm_dont_inherit;
import ecologylab.semantics.metametadata.InheritanceHandler.NameType;
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
			void usedForInlineMmdDefChanged(boolean usedForInlineMmdDef);
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
	 * if this composite should be wrapped.
	 */
	@simpl_scalar
	private boolean						wrap;

	private MMSelectorType		mmSelectorType					= MMSelectorType.DEFAULT;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String						typeNameInJava					= null;

	private boolean						useClassLevelOtherTags	= false;
	
	private AttributeChangeListener	attributeChangeListener = null;
	
//	/**
//	 * holds the generic type name used for attribute "type", after resolving it to a concrete type.
//	 * we need to hold the original generic type name for subfields to inherit from.
//	 */
//	String genericType;
//	
//	String genericExtends;
	
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
	protected void inheritMetaMetadataHelper(final InheritanceHandler inheritanceHandler)
	{
		inheritanceHandler.push(this);
		
		// init
		final MetaMetadataRepository repository = this.getRepository();
		
		// determine the structure we should inherit from
		final MetaMetadata inheritedMmd = findOrGenerateInheritedMetaMetadata(repository, inheritanceHandler);
		if (inheritedMmd != null)
		{
			if (inheritedMmd.inheritInProcess)
			{
				synchronized (lockInheritCommands)
				{
					// if inheriting from the root mmd, we need to clone and keep the environment right now.
					final InheritanceHandler inheritanceHandlerToUse = inheritanceHandler.clone();
					inheritanceHandler.pop(this);
					
					if (waitForFinish == null)
						waitForFinish = new Stack<InheritCommand>();
					waitForFinish.push(new InheritCommand()
					{
						@Override
						public void doInherit(Object... eventArgs)
						{
							debug("now inherit from " + inheritedMmd);
							inheritFromInheritedMmd(inheritedMmd, inheritanceHandlerToUse);
							inheritFrom(repository, inheritedMmd, inheritanceHandlerToUse);
							
							inheritFromSuperField(inheritanceHandlerToUse);
						}
					});
				
					inheritedMmd.addInheritFinishEventListener(new InheritFinishEventListener()
					{
						@Override
						public void inheritFinish(Object... eventArgs)
						{
							processWaitingInheritanceCommands();
						}
					});
					
					debug("delaying inheriting from " + inheritedMmd);
					return;
				}
			}
			else
			{
				inheritedMmd.inheritMetaMetadata();
				inheritFromInheritedMmd(inheritedMmd, inheritanceHandler);
				inheritFrom(repository, inheritedMmd, inheritanceHandler);
			}
		}
		
		MetaMetadataCompositeField inheritedField = inheritFromSuperField(inheritanceHandler);
		
		// for the root meta-metadata, this may happend
		if (inheritedMmd == null && inheritedField == null)
			inheritFrom(repository, null, inheritanceHandler);
	}

	private MetaMetadataCompositeField inheritFromSuperField(final InheritanceHandler inheritanceHandler)
	{
		final MetaMetadataRepository repository = this.getRepository();
		
		final MetaMetadataCompositeField inheritedField = (MetaMetadataCompositeField) this.getInheritedField();
		if (inheritedField != null)
		{
			if (inheritedField.inheritInProcess)
			{
				synchronized (lockInheritCommands)
				{
					final InheritanceHandler inheritanceHandlerToUse = inheritanceHandler.clone();
					inheritanceHandler.pop(this);
					
					if (waitForFinish == null)
						waitForFinish = new Stack<InheritCommand>();
					waitForFinish.push(new InheritCommand()
					{
						@Override
						public void doInherit(Object... eventArgs)
						{
							debug("now inherit from " + inheritedField);
							inheritFrom(repository, inheritedField, inheritanceHandlerToUse);
							inheritanceHandlerToUse.pop(MetaMetadataCompositeField.this);
						}
					});
				
					inheritedField.addInheritFinishEventListener(new InheritFinishEventListener()
					{
						@Override
						public void inheritFinish(Object... eventArgs)
						{
							processWaitingInheritanceCommands();
						}
					});
					
					debug("delaying inheriting from " + inheritedField);
					return inheritedField;
				}
			}
			else
			{
				inheritedField.setRepository(repository);
				inheritedField.inheritMetaMetadata(inheritanceHandler);
				inheritFrom(repository, inheritedField, inheritanceHandler);
			}
		}
		return inheritedField;
	}
	
	private void processWaitingInheritanceCommands()
	{
		synchronized (lockInheritCommands)
		{
			InheritCommand aCommand = waitForFinish.pop();
			if (waitForExecute == null)
				waitForExecute = new Stack<InheritCommand>();
			waitForExecute.push(aCommand);
			
			if (waitForFinish.isEmpty())
			{
				while (!waitForExecute.isEmpty())
					waitForExecute.pop().doInherit();
			}
		}
	}

	protected void inheritFrom(MetaMetadataRepository repository, MetaMetadataCompositeField inheritedStructure, InheritanceHandler inheritanceHandler)
	{
		// init nested fields inside this
		for (MetaMetadataField f : this.getChildMetaMetadata())
			prepareChildFieldForInheritance(repository, f);
		
		// inherit fields with attributes from inheritedStructure
		// if inheritedStructure == null, this must be the root meta-metadata
		if (inheritedStructure != null)
		{
			for (MetaMetadataField field : inheritedStructure.getChildMetaMetadata())
			{
				if (field instanceof MetaMetadataNestedField)
				{
					((MetaMetadataNestedField) field).inheritMetaMetadata(inheritanceHandler);
				}
				String fieldName = field.getName();
				MetaMetadataField fieldLocal = this.getChildMetaMetadata().get(fieldName);
				if (fieldLocal == null && inheritanceHandler.isUsingGenerics(field))
				{
					// if the super field is using generics, we will need to re-evaluate generic type vars
					fieldLocal = ReflectionTools.getInstance(field.getClass());
					prepareChildFieldForInheritance(repository, fieldLocal);
				}
				if (fieldLocal != null)
				{
					if (field.getClass() != fieldLocal.getClass())
						warning("local field " + fieldLocal + " hides field " + field + " with the same name in super mmd type!");
					
					if (field != fieldLocal && field.getInheritedField() != fieldLocal)
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
				f1.inheritMetaMetadata(inheritanceHandler);
				if (f1.isNewMetadataClass())
					this.setNewMetadataClass(true);
				
				MetaMetadataNestedField f0 = (MetaMetadataNestedField) f.getInheritedField();
				if (f0 != null && !f0.getTypeName().equals(f1.getTypeName()))
				{
					// inherited field w changing base type (polymorphic case)
					f1.inheritMetaMetadata(inheritanceHandler);
					MetaMetadata mmd0 = f0.getInheritedMmd();
					MetaMetadata mmd1 = f1.getInheritedMmd();
					if (mmd1.isDerivedFrom(mmd0))
						this.setNewMetadataClass(true);
					else
						throw new MetaMetadataException("incompatible types: " + f0 + " => " + f1);
				}
			}
		}
		
		// copy fields (references) that are only declared in inheritedStructure.
		// should copy them after recursively calling inheritMetaMetadata(), in case they are changed
		// during inheritMetaMetadata()
		if (inheritedStructure != null)
		{
			for (MetaMetadataField field : inheritedStructure.getChildMetaMetadata())
			{
				String fieldName = field.getName();
				MetaMetadataField fieldLocal = this.getChildMetaMetadata().get(fieldName);
				if (fieldLocal == null)
				{
					this.getChildMetaMetadata().put(fieldName, field);
				}
			}
		}
	}

	protected void prepareChildFieldForInheritance(MetaMetadataRepository repository,
			MetaMetadataField childField)
	{
		childField.setRepository(repository);
		if (childField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) childField;
			if (nested.packageName() == null)
				nested.setPackageName(this.packageName());
			nested.setMmdScope(this.getMmdScope());
		}
	}

	/**
	 * inherit stuffs from inheritedMmd of this composite field.
	 * 
	 * @param inheritedMmd
	 * @param inheritanceHandler TODO
	 */
	protected void inheritFromInheritedMmd(MetaMetadata inheritedMmd, InheritanceHandler inheritanceHandler)
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
	protected MetaMetadata findOrGenerateInheritedMetaMetadata(MetaMetadataRepository repository, InheritanceHandler inheritanceHandler)
	{
 		MetaMetadata inheritedMmd = this.getInheritedMmd();
		if (inheritedMmd == null)
		{
			MultiAncestorScope<MetaMetadata> mmdScope = this.getMmdScope();
			
			String typeName = getType() == null ? getName() : getType();
			String extendsName = getExtendsAttribute();
			
			if (isInlineDefinition())
			{
				// determine new type name
				if (typeName == null)
					throw new MetaMetadataException("attribute 'name' must be specified: " + this);
				if (inheritanceHandler.resolveMmdName(typeName) != null)
					// currently we don't encourage re-using existing name. however, in the future, when package names are available, we can change this.
					throw new MetaMetadataException("meta-metadata '" + typeName + "' already exists! please use another name to prevent name collision. hint: use 'tag' to change the tag if needed.");
				
				// determine from which meta-metadata to inherit
				inheritedMmd = inheritanceHandler.resolveMmdName(extendsName);
				if (extendsName == null || inheritedMmd == null)
					throw new MetaMetadataException("super type not specified or recognized: " + this + ", super type name: " + extendsName);
				
				// generate inline mmds and put it into current scope
				MetaMetadata generatedMmd = this.generateMetaMetadata(typeName, inheritedMmd);
				mmdScope.put(typeName, generatedMmd);
				mmdScope.put(generatedMmd.getName(), generatedMmd);
				
				// recursively do inheritance on generated mmd
				generatedMmd.inheritMetaMetadata(); // this will set generateClassDescriptor to true if necessary
				
				this.makeThisFieldUseMmd(typeName, generatedMmd);
				return generatedMmd;
			}
			else
			{
				// use type / extends
				if (typeName == null)
					throw new MetaMetadataException("no type / extends defined for " + this
							+ " (note that due to a limitation explicit child_scalar_type is needed for scalar collection fields, even if it has been declared in super field).");
				NameType[] nameType = new NameType[1];
				inheritedMmd = inheritanceHandler.resolveMmdName(typeName, nameType);
				if (inheritedMmd == null)
					throw new MetaMetadataException("meta-metadata not found: " + typeName + " (if you want to define new types inline, you need to specify extends/child_extends).");
				if (!typeName.equals(inheritedMmd.getName()) && nameType[0] == NameType.MMD)
				{
					// could be inline mmd
					this.makeThisFieldUseMmd(typeName, inheritedMmd);
				}
				
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
		this.setUsedForInlineMmdDef(true);
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
		fd.setWrapped(wrap);
		this.metadataFieldDescriptor = fd;
		return fd;
	}

	@Override
	public void setUsedForInlineMmdDef(boolean usedForInlineMmdDef)
	{
		super.setUsedForInlineMmdDef(usedForInlineMmdDef);
		if (this.attributeChangeListener != null)
			attributeChangeListener.usedForInlineMmdDefChanged(usedForInlineMmdDef);
	}
	
}
