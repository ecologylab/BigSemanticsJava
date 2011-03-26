/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.metadata.scalar.MetadataString;

/**
 * @author andruid
 *
 */
public class MediaClipping<ME extends Document> extends Clipping
{
	/**
	 * Explicit description of the clipped media in the source document.
	 */
	@mm_name("caption")
	@simpl_scalar
	private MetadataString	caption;
	
	@simpl_composite
	ME											clippedMedia;
	

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

	public void setCaption(String captionString)
	{
		MetadataString caption = this.caption();
		caption.setValue(captionString);
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

}
