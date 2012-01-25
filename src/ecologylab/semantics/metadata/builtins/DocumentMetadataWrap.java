/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.builtins.declarations.DocumentMetadataWrapDeclaration;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Workaround for the absence of wrapped composites.
 * Provides isolation to avoid ambiguous deserialization between source and outlink in Clipping.
 * 
 * @author andruid
 */
@simpl_inherit
public class DocumentMetadataWrap extends DocumentMetadataWrapDeclaration
{
	
//	/**
//	 * The Document we are wrapping.
//	 */
//	@simpl_composite
//	@simpl_scope(SemanticsNames.REPOSITORY_DOCUMENT_TRANSLATIONS)
////	@simpl_classes(CompoundDocument.class)
//	@mm_name("document") 
//	private Document				document;

	public DocumentMetadataWrap()
	{
		MetaMetadataRepository repository = SemanticsSessionScope.get().getMetaMetadataRepository();
		MetaMetadata mm = repository.getMMByClass(this.getClass());
		setMetaMetadata(mm);
	}

	public DocumentMetadataWrap(Document document)
	{
		this();
		this.setDocument(document);
	}
	
	@Override
	public void recycle()
	{
		setDocument(null);
	}

}
