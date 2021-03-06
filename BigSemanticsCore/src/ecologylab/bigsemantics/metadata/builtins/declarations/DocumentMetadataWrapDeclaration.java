package ecologylab.bigsemantics.metadata.builtins.declarations;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2016) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.mm_name;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;
import java.util.List;
import java.util.Map;

/** 
 *Used to disambiguated Document subclass composites.
 */ 
@simpl_inherit
public class DocumentMetadataWrapDeclaration extends Metadata
{
	/** 
	 *polymorphic across documents
	 */ 
	@simpl_composite
	@simpl_scope("repository_documents")
	@mm_name("document")
	private Document document;

	public DocumentMetadataWrapDeclaration()
	{ super(); }

	public DocumentMetadataWrapDeclaration(MetaMetadataCompositeField mmd) {
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
