/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.simpl_scope;

/**
 * @author andruid
 *
 */
@simpl_inherit
public class MediaClipping<ME extends ClippableDocument> extends Clipping
{
	/**
	 * Explicit description of the clipped media in the source document.
	 */
	@mm_name("caption")
	@simpl_scalar
	private MetadataString	caption;
	
	@simpl_composite
	@simpl_scope(SemanticsNames.REPOSITORY_MEDIA_TRANSLATIONS)
	@mm_name("media")
	private ME							media;
	

	public MediaClipping()
	{
		
	}
	public MediaClipping(MetaMetadataCompositeField metaMetadata, ME clippedMedia, Document source, Document outlink, String caption, String context)
	{
		super(metaMetadata, source, outlink, context);
		if (caption != null)
			setCaption(caption);
		this.media			= clippedMedia;
	}
	
	public MetadataString caption()
	{
		MetadataString result = this.caption;
		if (result == null)
		{
			result = new MetadataString();
			this.caption = result;
		}
		return result;
	}

	public String getCaption()
	{
		return caption == null ? null : caption.getValue();
	}

	public MetadataString getCaptionMetadata()
	{
		return caption;
	}

	public void setCaption(String captionString)
	{
		MetadataString caption = this.caption();
		caption.setValue(captionString);
	}

	public void setCaptionMetadata(MetadataString caption)
	{
		this.caption = caption;
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
		return caption == null || caption.getValue() == null;
	}

	public ME getMedia()
	{
		return media;
	}
}
