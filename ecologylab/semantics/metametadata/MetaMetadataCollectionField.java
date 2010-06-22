package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.xml.XMLTools;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("mm_collection_field")
public class MetaMetadataCollectionField extends MetaMetadataField
{

	@xml_attribute
	protected String																			childTag;
	
	/**
	 * The type for collection children.
	 */
	@xml_attribute
	protected String															childType;
	
	public MetaMetadataCollectionField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataCollectionField(MetaMetadataField mmf)
	{
		this.name = mmf.name;
		this.type = mmf.type;
		this.extendsAttribute = mmf.extendsAttribute;
		this.hide = mmf.hide;
		this.alwaysShow = mmf.alwaysShow;
		this.style = mmf.style;
		this.layer = mmf.layer;
		this.xpath = mmf.xpath;
		this.navigatesTo = mmf.navigatesTo;
		this.shadows = mmf.shadows;
		this.stringPrefix = mmf.stringPrefix;
		this.generateClass = mmf.generateClass;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.noWrap = mmf.noWrap;
		this.comment = mmf.comment;
		this.dontCompile = mmf.dontCompile;
		this.entity = mmf.entity;
		this.key = mmf.key;
		this.textRegex = mmf.textRegex;
		this.matchReplacement = mmf.matchReplacement;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.ignoreExtractionError = mmf.ignoreExtractionError;
		this.kids = mmf.kids;
	}

	public String getCollectionChildType()
	{
		return childType;
	}

	public String getChildTag()
	{
		return (childTag != null) ? childTag : childType;
	}
	
	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		appendCollection(appendable,pass);
	}

	/**
	 * This method append a field of type collection to the Java Class. TODO Have to change it to use
	 * HashMapArrayList instead of array list.
	 * 
	 * @param appendable
	 *          The appendable to append to.
	 * @throws IOException
	 */
	protected void appendCollection(Appendable appendable,int pass) throws IOException
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
		
		//FIXME- New metadata collection types here!!
		String variableTypeStart =" ArrayList<";
		String variableTypeEnd =">";
		if(isEntity())
		{
			variableTypeStart = " ArrayList<Entity<";
			variableTypeEnd=">>";
		}
		String tag = getChildTag();
		if (tag == null)
		{
			warning("child_tag not specified in meta-metadata for collection field " + this.name);
			return;
		}
		
		StringBuilder annotation = StringBuilderUtils.acquire();
		annotation.append("@xml_collection(\"" + tag + "\")");
		if (noWrap)
			annotation.append(" @xml_nowrap");
		
		
		if(pass == MetadataCompilerUtils.GENERATE_FIELDS_PASS)
		{
			appendMetalanguageDecl(appendable, annotation.toString(),"private" +variableTypeStart , className,variableTypeEnd , fieldName);
		}
		else if(pass == MetadataCompilerUtils.GENERATE_METHODS_PASS)
		{
			appendLazyEvaluationMethod(appendable, fieldName, variableTypeStart + className + variableTypeEnd);
			appendSetterForCollection(appendable, fieldName, variableTypeStart + className + variableTypeEnd);
			appendGetterForCollection(appendable, fieldName, variableTypeStart + className + variableTypeEnd);
		}
	}

}
