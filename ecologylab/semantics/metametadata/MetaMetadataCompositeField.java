package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("composite")
public class MetaMetadataCompositeField extends MetaMetadataNestedField
{

	/**
	 * The type/class of metadata object.
	 */
	@simpl_scalar
	protected String	type;

	@simpl_scalar
	protected boolean	entity	= false;

	public MetaMetadataCompositeField()
	{
		// TODO Auto-generated constructor stub
	}
	
	public MetaMetadataCompositeField(String name, HashMapArrayList<String, MetaMetadataField> kids)
	{
		this.name = name;
		this.kids = new HashMapArrayList<String, MetaMetadataField>();
		if (kids != null)
			this.kids.putAll(kids);
	}

	public MetaMetadataCompositeField(MetaMetadataField mmf)
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
		this.stringPrefix = mmf.stringPrefix;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.comment = mmf.comment;
		this.dontCompile = mmf.dontCompile;
		this.key = mmf.key;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.ignoreExtractionError = mmf.ignoreExtractionError;
		this.kids = mmf.kids;
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
	
	public boolean isEntity()
	{
		return entity;
	}
	
	public String getTypeOrName()
	{
		if (type != null)
			return type;
		else 
			return getName();
	}
	
	public String getTagForTranslationScope()
	{
		return entity == true ? DocumentParserTagNames.ENTITY : tag != null ? tag : name;
	}
	
	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		StringBuilder annotations = new StringBuilder();
		annotations.append("@simpl_composite @mm_name(\"" + name + "\")");
		annotations.append(" "); // the space between annotations and type
		
		String fieldType = XMLTools.classNameFromElementName(getTypeOrName());
		if (isEntity())
		{
			fieldType = "Entity<" + XMLTools.classNameFromElementName(getTypeOrName()) + ">";
		}
		String fieldName = getFieldName();
		
		switch (pass)
		{
		case MetadataCompilerUtils.GENERATE_FIELDS_PASS:
			appendable.append("\nprivate " + getTagDecl() + annotations + fieldType + "\t" + fieldName + ";");
			break;
		case MetadataCompilerUtils.GENERATE_METHODS_PASS:
			appendLazyEvaluationMethod(appendable, fieldName, fieldType);
			appendSetter(appendable, fieldName, fieldType);
			appendGetter(appendable, fieldName, fieldType);
			break;
		}
	}
	
	protected String getMetaMetadataTagToInheritFrom()
	{
		if (isEntity())
			return  DocumentParserTagNames.ENTITY;
		else if (type != null)
			return type;
		else
			return null;
//			return name;
	}

	/**
	 * Does this declaration declare a new field, rather than referring to a previously declared field?
	 * 
	 * @return	true if there is a scalar_type attribute declared.
	 */
	protected boolean isNewDeclaration()
	{
		return entity || // (getType() != null) && 
			isNewClass();		// recurse
	}

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return	this, because it is a composite itself.
	 */
	public MetaMetadataCompositeField metaMetadataCompositeField()
	{
		return this;
	}

}