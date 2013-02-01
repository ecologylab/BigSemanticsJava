/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.metadata.builtins.declarations.MediaClippingDeclaration;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * @author andruid
 *
 */
@simpl_inherit
public class MediaClipping<ME extends ClippableDocument<ME>> extends MediaClippingDeclaration<ME>
{
	
//	/**
//	 * Explicit description of the clipped media in the source document.
//	 */
//	@mm_name("caption")
//	@simpl_scalar
//	private MetadataString	caption;
//	
//	@simpl_composite
//	@simpl_scope(SemanticsNames.REPOSITORY_MEDIA_TRANSLATIONS)
//	@mm_name("media")
//	@simpl_wrap
//	private ME							media;

	public MediaClipping()
	{
		super();
	}
	
	public MediaClipping(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}
	
	public MediaClipping(MetaMetadataCompositeField metaMetadata, ME clippedMedia, Document source, Document outlink, String caption, String context)
	{
		this(metaMetadata);
		initMediaClipping(this, clippedMedia, source, outlink, caption, context);
	}

	public static <ME extends ClippableDocument<ME>> void initMediaClipping(
			MediaClipping<ME> mediaClipping, ME clippedMedia, Document source, Document outlink,
			String caption, String context)
	{
		mediaClipping.setSourceDoc(source);
		Clipping.initClipping(mediaClipping, outlink, context);
		if (caption != null)
			mediaClipping.setCaption(caption);
		mediaClipping.setMedia(clippedMedia);
	}
	
	public void hwSetCaption(String caption)
	{
		if (caption != null)
		{
			this.setCaption(caption);
			rebuildCompositeTermVector();
		}
	}

	/**
	 * Derive cFMetadata from the HTML <code>alt</code> attribute,
	 * or the filename. This is to be executed when we construct <code>this</code>
	 * initially.
	 */
	public void setCaptionIfGood(String newCandidateCaption)
	{
		if (!ImageFeatures.altIsBogus(newCandidateCaption))
		{
			numWithCaption++;
			hwSetCaption(newCandidateCaption);
		}
	}

	/**
	 * Derive further cFMetadata from an HTML <code>alt</code> attribute.
	 * This is to be executed subsequent to initital construction, in case we
	 * encounter additional references to this (in HTML).
	 */
	public void setCaptionIfEmpty(String newCandidateCaption)
	{
		if (isNullCaption())
			setCaptionIfGood(newCandidateCaption);
	}

	public boolean isNullCaption()
	{
		return getCaptionMetadata() == null || getCaptionMetadata().getValue() == null;
	}

}
