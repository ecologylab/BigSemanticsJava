package ecologylab.bigsemantics.metadata.builtins.declarations;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2013) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.builtins.CreativeAct;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import java.lang.String;
import java.util.List;
import java.util.Map;

@simpl_inherit
public class AnnotateDeclaration extends CreativeAct
{
	@simpl_scalar
	private MetadataString text;

	public AnnotateDeclaration()
	{ super(); }

	public AnnotateDeclaration(MetaMetadataCompositeField mmd) {
		super(mmd);
	}


	public MetadataString	text()
	{
		MetadataString	result = this.text;
		if (result == null)
		{
			result = new MetadataString();
			this.text = result;
		}
		return result;
	}

	public String getText()
	{
		return this.text == null ? null : text().getValue();
	}

	public MetadataString getTextMetadata()
	{
		return text;
	}

	public void setText(String text)
	{
		if (text != null)
			this.text().setValue(text);
	}

	public void setTextMetadata(MetadataString text)
	{
		this.text = text;
	}
}