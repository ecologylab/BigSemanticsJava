package ecologylab.semantics.metametadata;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("mmd_scalar_field")
public class MetaMetadataScalarField extends MetaMetadataField
{
	public MetaMetadataScalarField()
	{
		// TODO Auto-generated constructor stub
	}
	
	public MetaMetadataScalarField(MetaMetadataField mmf)
	{
		this.name = mmf.name;
		this.type = mmf.type;
		this.extendsAttribute = mmf.extendsAttribute;
		this.scalarType = mmf.scalarType;
		this.hide = mmf.hide;
		this.alwaysShow = mmf.alwaysShow;
		this.style = mmf.style;
		this.layer = mmf.layer;
		this.xpath = mmf.xpath;
		this.navigatesTo = mmf.navigatesTo;
		this.shadows = mmf.shadows;
		this.stringPrefix = mmf.stringPrefix;
		this.generateClass = false; // for scalar fields, they never generate classes.
		this.childType = mmf.childType;
		this.isNested = mmf.isNested;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.collection = mmf.collection;
		this.noWrap = mmf.noWrap;
		this.comment = mmf.comment;
		this.dontCompile = mmf.dontCompile;
		this.entity = mmf.entity;
		this.key = mmf.key;
		this.textRegex = mmf.textRegex;
		this.matchReplacement = mmf.matchReplacement;
		this.contextNode = mmf.contextNode;
		this.childTag = mmf.childTag;
		this.tag = mmf.tag;
		this.ignoreExtractionError = mmf.ignoreExtractionError;
		this.kids = mmf.kids;
	}

}
