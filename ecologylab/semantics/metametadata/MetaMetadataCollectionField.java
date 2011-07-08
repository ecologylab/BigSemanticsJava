package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.FieldTypes;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.scalar.ScalarType;

@simpl_inherit
@xml_tag("collection")
public class MetaMetadataCollectionField extends MetaMetadataNestedField
{

	public static final String	UNRESOLVED_NAME	= "&UNRESOLVED_NAME";

	@simpl_scalar
	protected String						childTag;

	/**
	 * The type for collection children.
	 */
	@simpl_scalar
	protected String						childType;

	@mm_dont_inherit
	@simpl_scalar
	protected String						childExtends;

	@simpl_scalar
	protected ScalarType				childScalarType;

	/**
	 * Specifies adding @simpl_nowrap to the collection object in cases where items in the collection
	 * are not wrapped inside a tag.
	 */
	@simpl_scalar
	protected boolean						noWrap;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String							typeNameInJava	= null;

	public MetaMetadataCollectionField()
	{
		
	}

	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		MetaMetadataCollectionField cloned = new MetaMetadataCollectionField();
		cloned.inheritAttributes(this);
		cloned.copyClonedFieldsFrom(this);
		HashMapArrayList<String, MetaMetadataField> newKids = new HashMapArrayList<String, MetaMetadataField>();
		for (String kidName : this.getChildMetaMetadata().keySet())
		{
			MetaMetadataField kid = this.getChildMetaMetadata().get(kidName);
			MetaMetadataField clonedKid = (MetaMetadataField) kid.clone();
			newKids.put(kidName, clonedKid);
		}
		cloned.setChildMetaMetadata(newKids);
		return cloned;
	}

	public String getChildTag()
	{
		if (childTag != null)
			return childTag;
		if (childType != null)
			return childType;
		return null;
	}

	public String getChildType()
	{
		return childType;
	}
	
	public String getChildExtends()
	{
		return childExtends;
	}

	public ScalarType getChildScalarType()
	{
		return childScalarType;
	}

	public boolean isNoWrap()
	{
		return noWrap;
	}

	@Deprecated
	@Override
	public String getAnnotationsInJava()
	{
		StringBuilder annotations = StringBuilderUtils.acquire();
		
		// @simpl_collection
		String childTag = getChildTag();
		if (childTag == null)
		{
			// TODO check for inherited child_tag / child_type !!!
			warning("neither child_tag nor child_type specified in meta_metadata for collection field " + this.name);
		}
		annotations.append("@simpl_collection(\"" + childTag + "\")");
		
		// @simpl_nowrap or @xml_tag
		if (isNoWrap())
		{
			annotations.append(" @simpl_nowrap");
		}
		else
		{
			annotations.append(" @xml_tag(\"" + resolveTag() + "\")");
		}
	
		// @mm_name
		annotations.append(" @mm_name(\"" + getName() + "\")");
		
		return annotations.toString();
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
			String typeName = getTypeName();
			String className = XMLTools.classNameFromElementName(typeName);
			if (this.getFieldType() == FieldTypes.COLLECTION_SCALAR)
				className = "Metadata" + className;
			rst = "ArrayList<" + className + ">";
			typeNameInJava = rst;
		}
		return typeNameInJava;
	}

	/**
	 * @return the tag
	 */
	public String resolveTag()
	{
		if (isNoWrap())
		{
			// is it sure that it will be a collection field?
//			String childTag = ((MetaMetadataCollectionField) this).childTag;
//			String childType = ((MetaMetadataCollectionField) this).childType;
			return (childTag != null) ? childTag : childType;
		}
		else
		{
			return (tag != null) ? tag : name;
		}
		// return (isNoWrap()) ? ((childTag != null) ? childTag : childType) : (tag != null) ? tag : name;
	}
	
	public String getTagForTranslationScope()
	{
		// FIXME: seems broken when rewriting collection xpath without re-indicating child_type
		return childType != null ? childType : tag != null ? tag : name;
	}

	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		return childType != null ? childType : null;
	}
	
	/**
	 * Collection fields with children need to bind a class descriptor to the composite
	 * field created in postDeserializationHook(), and not itself.
	 */
	@Override
	boolean getClassAndBindDescriptors(TranslationScope metadataTScope)
	{
		if (kids != null && kids.size() > 0)
			return kids.get(0).getClassAndBindDescriptors(metadataTScope);
		else
			return false;
	}
	
	@Override
	public HashMapArrayList<String, MetaMetadataField> getChildMetaMetadata()
	{
		return (kids != null && kids.size() > 0) ? kids.get(0).getChildMetaMetadata() : null;
	}
	
	public MetaMetadataCompositeField getChildComposite()
	{
		return (kids != null && kids.size() > 0) ? (MetaMetadataCompositeField) kids.get(0) : null;
	}
	
	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return	this, because it is a composite itself.
	 */
	public MetaMetadataCompositeField metaMetadataCompositeField()
	{
		return getChildComposite();
	}
	
	@Override
	/**
	 * Each object in a collection of metadata require a specific MMdata composite object to be associated with them.
	 * This is unavailable in the MMD XML, and must be generated when the XML is read in.
	 */
	public void deserializationPostHook()
	{
		int typeCode = this.getFieldType();
		if (typeCode == FieldTypes.COLLECTION_SCALAR)
			return;
		
		String childType = getChildType();
		String childCompositeName = childType != null ? childType : UNRESOLVED_NAME;
		final MetaMetadataCollectionField thisField = this;
		MetaMetadataCompositeField composite = new MetaMetadataCompositeField(childCompositeName, kids)
		{
			@Override
			protected void typeChanged(String newType)
			{
				thisField.childType = newType;
			}

			@Override
			protected void extendsChanged(String newExtends)
			{
				thisField.childExtends = newExtends;
			}
			
			@Override
			protected void tagChanged(String newTag)
			{
				thisField.childTag = newTag;
			}
		};
		composite.setParent(this);
		composite.setType(childType);
		composite.setExtendsAttribute(this.childExtends);
		kids.clear();
		kids.put(composite.getName(), composite);
		composite.setPromoteChildren(this.shouldPromoteChildren());
	}

	public boolean isCollectionOfScalars()
	{
		return childScalarType != null;
	}

	@Override
	public void inheritMetaMetadata()
	{
		/*
		 * the childComposite should hide all complexity between collection fields and composite fields,
		 * through hooks when necessary.
		 */
		
		if (!inheritFinished && !inheritInProcess)
		{
			this.inheritInProcess = true;
			
			int typeCode = this.getFieldType();
			switch (typeCode)
			{
			case FieldTypes.COLLECTION_ELEMENT:
			{
				// prepare childComposite: possibly new name, type, extends, tag and inheritedField
				MetaMetadataCompositeField childComposite = this.getChildComposite();
				if (childComposite.getName().equals(UNRESOLVED_NAME))
					childComposite.setName(this.childType == null ? this.name : this.childType);
				childComposite.type = this.childType; // here not using setter to reduce unnecessary re-assignment of this.childType
				childComposite.extendsAttribute = this.childExtends;
				childComposite.tag = this.childTag;
				childComposite.setRepository(this.getRepository());
				// set inheritedField for childComposite, if this has an inheritedField set
				MetaMetadataCollectionField inheritedField = (MetaMetadataCollectionField) this
						.getInheritedField();
				if (inheritedField != null)
					childComposite.setInheritedField(inheritedField.getChildComposite());
				childComposite.setDeclaringMmd(this.getDeclaringMmd());
				childComposite.setInlineMmds(this.getInlineMmds());

				childComposite.inheritMetaMetadata(); // inheritedMmd might be inferred from type/extends
				this.setInheritedMmd(childComposite.getInheritedMmd());

				break;
			}
			case FieldTypes.COLLECTION_SCALAR:
			{
				MetaMetadataField inheritedField = this.getInheritedField();
				if (inheritedField != null)
					this.inheritAttributes(inheritedField);
				break;
			}
			}
			
			inheritFinished = true;
			inheritInProcess = false;
		}
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
			boolean wrapped = !this.isNoWrap();
			if (!wrapped)
				tagName = null;
			
			int typeCode = this.getFieldType();
			switch (typeCode)
			{
			case FieldTypes.COLLECTION_ELEMENT:
			{
				MetaMetadata inheritedMmd = this.getInheritedMmd();
				assert inheritedMmd != null : "IMPOSSIBLE: inheritedMmd == null: something wrong in the inheritance process!";
				MetadataClassDescriptor fieldCd = inheritedMmd.getMetadataClassDescriptor();
				fd = new MetadataFieldDescriptor(
						this,
						tagName,
						this.getComment(),
						typeCode,
						fieldCd,
						contextCd,
						fieldName,
						null,
						null,
						javaTypeName);
				break;
			}
			case FieldTypes.COLLECTION_SCALAR:
			{
				if (this.kids.size() > 0)
					warning("Ignoring nested fields inside " + this + " because child_scalar_type specified ...");
				
				ScalarType scalarType = this.getChildScalarType();
				fd = new MetadataFieldDescriptor(
						this,
						tagName,
						this.getComment(),
						typeCode,
						null,
						contextCd,
						fieldName,
						scalarType,
						null,
						javaTypeName);
			}
			}
			fd.setWrapped(wrapped);
		}
		this.metadataFieldDescriptor = fd;
		return fd;
	}
	
	boolean isTheChildComposite(MetaMetadataCompositeField composite)
	{
		if (kids != null && kids.size() > 0)
			return kids.get(0) == composite;
		return false;
	}

}
