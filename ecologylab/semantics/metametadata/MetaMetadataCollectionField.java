package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("collection")
public class MetaMetadataCollectionField extends MetaMetadataNestedField
{

	@simpl_scalar
	protected String	childTag;

	/**
	 * The type for collection children.
	 */
	@simpl_scalar
	protected String	childType;
	
	@simpl_scalar
	protected boolean	childEntity	= false;

	/**
	 * Specifies adding @simpl_nowrap to the collection object in cases where items in the collection
	 * are not wrapped inside a tag.
	 */
	@simpl_scalar
	protected boolean	noWrap;

	public MetaMetadataCollectionField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataCollectionField(MetaMetadataField mmf)
	{
		this.name = mmf.name;
		this.extendsAttribute = mmf.extendsAttribute;
		this.hide = mmf.hide;
		this.alwaysShow = mmf.alwaysShow;
		this.style = mmf.style;
		this.layer = mmf.layer;
		this.xpath = mmf.xpath;
		this.navigatesTo = mmf.navigatesTo;
		this.shadows = mmf.shadows;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.comment = mmf.comment;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.kids = mmf.kids;
	}

	public String determineCollectionChildType()
	{
		return (!childEntity) ? childType : DocumentParserTagNames.ENTITY;
	}
	
	public String collectionChildType()
	{
		return childType;
	}

	public String getChildTag()
	{
		return (childTag != null) ? childTag : childType;
	}

	public boolean isNoWrap()
	{
		return noWrap;
	}
	
	public boolean isChildEntity()
	{
		return childEntity;
	}

	/**
	 * @return the tag
	 */
	public String resolveTag()
	{
		if (isNoWrap())
		{
			// is it sure that it will be a collection field?
			String childTag = ((MetaMetadataCollectionField) this).childTag;
			String childType = ((MetaMetadataCollectionField) this).childType;
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
		return childEntity == true ? DocumentParserTagNames.ENTITY : childType != null ? childType : tag != null ? tag : name;
	}

	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		appendCollection(appendable, pass);
	}

	/**
	 * This method append a field of type collection to the Java Class. TODO Have to change it to use
	 * HashMapArrayList instead of array list.
	 * 
	 * @param appendable
	 *          The appendable to append to.
	 * @throws IOException
	 */
	protected void appendCollection(Appendable appendable, int pass) throws IOException
	{
		// name of the element.
		String elementName = this.name;

		// if it belongs to a particular type we will generate a class for it so the type is set to
		// collectionChildType.
		if (this.childType != null)
		{
			elementName = this.childType;
		}
		// getting the class name
		String className = XMLTools.classNameFromElementName(elementName);

		// getting the field name.
		String fieldName = XMLTools.fieldNameFromElementName(name);

		// appending the declaration.
		// String mapDecl = childMetaMetadata.get(key).getScalarType().fieldTypeName() + " , " +
		// className;

		// FIXME- New metadata collection types here!!
		String variableTypeStart = " ArrayList<";
		String variableTypeEnd = ">";
		if (childEntity)
		{
			variableTypeStart = " ArrayList<Entity<";
			variableTypeEnd = ">>";
		}
		
		String childTag = getChildTag();
		if (childTag == null)
		{
			warning("child_tag not specified in meta-metadata for collection field " + this.name);
			return;
		}

		StringBuilder annotation = StringBuilderUtils.acquire();
		annotation.append(" @simpl_collection(\"" + childTag + "\")");
		if (noWrap)
		{
			annotation.append(" @simpl_nowrap");
		}
		else
		{
			annotation.append(" @xml_tag(\"" + resolveTag() + "\")");
		}

		annotation.append(" @mm_name(\"" + name + "\")");
		
		switch (pass)
		{
		case MetadataCompilerUtils.GENERATE_FIELDS_PASS:
			appendMetalanguageDecl(appendable, annotation.toString(), "private" + variableTypeStart,
					className, variableTypeEnd, fieldName);
			break;
		case MetadataCompilerUtils.GENERATE_METHODS_PASS:
			appendLazyEvaluationMethod(appendable, fieldName, variableTypeStart + className
					+ variableTypeEnd);
			appendSetter(appendable, fieldName, variableTypeStart + className
					+ variableTypeEnd);
			appendGetter(appendable, fieldName, variableTypeStart + className
					+ variableTypeEnd);
			break;
		}
	}

	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		if (childEntity)
			return  DocumentParserTagNames.ENTITY;
		else if (childType != null)
			return childType;
		else
			return null;
	}
	
	/**
	 * Does this declaration declare a new field, rather than referring to a previously declared field?
	 * 
	 * @return	true if there is a scalar_type attribute declared.
	 */
	protected boolean isNewDeclaration()
	{
		return childType != null && isNewClass();
	}
	
	@Override
	public void deserializationPostHook()
	{
		MetaMetadataCompositeField composite = new MetaMetadataCompositeField(determineCollectionChildType(), kids);
		if (kids != null)
		{
			kids.clear();
			kids.put(composite.getName(), composite);
		}
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
	
	@Override
	protected void inheritNonDefaultAttributes(MetaMetadataField inheritFrom)
	{
		super.inheritNonDefaultAttributes(inheritFrom);
		
		MetaMetadataCompositeField composite = getChildComposite();
		if (composite != null && composite.getName() == null)
		{
			MetaMetadataCompositeField childComposite = ((MetaMetadataCollectionField) inheritFrom).getChildComposite();
			composite.setName(childComposite != null ? childComposite.getName() : this.childType);
		}
	}
	
	/*@Override
	public void setChildMetaMetadata(HashMapArrayList<String, MetaMetadataField> childMetaMetadata)
	{
		MetaMetadataCompositeField composite = getChildComposite();
		if (composite == null)
		{
			kids = new HashMapArrayList<String, MetaMetadataField>();
			composite = new MetaMetadataCompositeField(determineCollectionChildType(), null);
			kids.put(composite.getName(), composite);
		}
		else
			composite.getChildMetaMetadata().putAll(childMetaMetadata);
	}*/
	
	@Override
	public HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
	{
		kids = new HashMapArrayList<String, MetaMetadataField>();
		MetaMetadataCompositeField composite = new MetaMetadataCompositeField(determineCollectionChildType(), null);
		kids.put(composite.getName(), composite);
		
		return composite.getChildMetaMetadata();
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
	protected String getTypeName()
	{
		if (childType != null)
			return childType;
			
		MetaMetadataField inherited = getInheritedField();
		if (inherited == null)
		{
			// definitive
			return name;
		}
		else
		{
			// decorative
			if (!(inherited instanceof MetaMetadataCollectionField))
				return null; // type mismatch
			return inherited.getTypeName();
		}
	}

}
