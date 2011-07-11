/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.Metadata.mm_name;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.simpl_composite;
import ecologylab.serialization.ElementState.simpl_scope;

/**
 * Workaround for the absence of wrapped composites.
 * Provides isolation to avoid ambiguous deserialization betwee source and outlink in Clipping.
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
	@simpl_scope(SemanticsNames.GENERATED_DOCUMENT_TRANSLATIONS)
	@mm_name("document") 
	private Document				document;

	/**
	 * 
	 */
	public DocumentMetadataWrap()
	{
	}

	public DocumentMetadataWrap(Document document)
	{
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
}
