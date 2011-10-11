/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.mm_name;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;

/**
 * Workaround for the absence of wrapped composites.
 * Provides isolation to avoid ambiguous deserialization between source and outlink in Clipping.
 * 
 * @author andruid
 */
@simpl_inherit
public class DocumentMetadataWrap extends Metadata
{
	/**
	 * The Document we are wrapping.
	 */
	@simpl_composite
	@simpl_scope(SemanticsNames.REPOSITORY_DOCUMENT_TRANSLATIONS)
//	@simpl_classes(CompoundDocument.class)
	@mm_name("document") 
	private Document				document;

	/**
	 * 
	 */
	public DocumentMetadataWrap()
	{
		MetaMetadataRepository repository = SemanticsSessionScope.getRepository();
		MetaMetadata mm = repository.getMMByClass(this.getClass());
		setMetaMetadata(mm);
	}

	public DocumentMetadataWrap(Document document)
	{
		this();
		this.document		= document;
	}
	
	/**
	 * @return the document
	 */
	public Document getDocument()
	{
		return document;
	}

	public void recycle()
	{
		document	= null;
	}

	public void setDocument(Document document)
	{
		this.document = document;
	}
}
