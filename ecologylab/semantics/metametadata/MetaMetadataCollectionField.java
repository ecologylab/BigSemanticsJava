package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;

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

	@simpl_scalar
	protected String						childExtends;

	@simpl_scalar
	protected String						childScalarType;

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

	public String getChildTag()
	{
		if (childTag != null)
			return childTag;
		if (childType != null)
			return childType;
		if (childScalarType != null)
			return "metadata_" + childScalarType;
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

	public String getChildScalarType()
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
			rst = "ArrayList<" + className + ">";
			typeNameInJava = rst;
		}
		return typeNameInJava;
	}

//	public String determineCollectionChildType()
//	{
//		return getChildType();
//	}
	
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
	
//	@Override
//	public HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
//	{
//		kids = new HashMapArrayList<String, MetaMetadataField>();
//		MetaMetadataCompositeField composite = new MetaMetadataCompositeField(getChildType(), null);
//		kids.put(composite.getName(), composite);
//		
//		return composite.getChildMetaMetadata();
//	}
	
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
		final String childType = getChildType();
		MetaMetadataCompositeField composite = new MetaMetadataCompositeField(childType != null ? childType : UNRESOLVED_NAME, childExtends, kids);
		composite.setParent(this);
		composite.setType(childType);
		if (kids != null)
		{
			kids.clear();
			kids.put(composite.getName(), composite);
		}
		composite.setPromoteChildren(this.shouldPromoteChildren());
	}

	public boolean isCollectionOfScalars()
	{
		return childScalarType != null;
	}

	@Override
	public void inheritMetaMetadata()
	{
		MetaMetadataCompositeField childComposite = this.getChildComposite();
		childComposite.inheritMetaMetadata();
		this.setInheritedMmd(childComposite.getInheritedMmd());
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
			fd.setWrapped(wrapped);
		}
		return fd;
	}

}
