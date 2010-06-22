package ecologylab.semantics.metametadata;

import java.io.IOException;

import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.xml.XMLTools;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("mm_nested_field")
public class MetaMetadataNestedField extends MetaMetadataField
{

	public MetaMetadataNestedField()
	{
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataNestedField(MetaMetadataField mmf)
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

	@Override
	protected void doAppending(Appendable appendable, int pass) throws IOException
	{
		appenedNestedMetadataField(appendable,pass);
	}

	/**
	 * Append method for Is_nested=true fields
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	protected void appenedNestedMetadataField(Appendable appendable,int pass) throws IOException
	{
		String variableType=" @xml_nested "+XMLTools.classNameFromElementName(getTypeOrName());
		String fieldType = XMLTools.classNameFromElementName(getTypeOrName());
		if(isEntity())
		{
			variableType = " @xml_nested Entity<"+XMLTools.classNameFromElementName(getTypeOrName())+">";
			fieldType = "Entity<"+XMLTools.classNameFromElementName(getTypeOrName())+">";
		}
		if(pass == MetadataCompilerUtils.GENERATE_FIELDS_PASS)
		{
			appendable.append("\nprivate " + getTagDecl() +variableType + "\t" + name + ";");
		}
		else if(pass == MetadataCompilerUtils.GENERATE_METHODS_PASS)
		{
			appendLazyEvaluationMethod(appendable, getName(), fieldType);
			appendSetterForCollection(appendable, getName(), fieldType);
			appendGetterForCollection(appendable, getName(), fieldType);
		}
	}
	
}
