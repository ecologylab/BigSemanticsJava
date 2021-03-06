package ecologylab.bigsemantics.metadata.builtins.declarations;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2016) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.builtins.CreativeAct;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.mm_name;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_wrap;
import java.util.List;
import java.util.Map;

@simpl_inherit
public class CurateLinkDeclaration extends CreativeAct
{
	@simpl_composite
	@simpl_wrap
	@simpl_scope("repository_documents")
	@mm_name("document")
	private Document document;

	public CurateLinkDeclaration()
	{ super(); }

	public CurateLinkDeclaration(MetaMetadataCompositeField mmd) {
		super(mmd);
	}


	public Document getDocument()
	{
		return document;
	}

	public void setDocument(Document document)
	{
		this.document = document;
	}
}
