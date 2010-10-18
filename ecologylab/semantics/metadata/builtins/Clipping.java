/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;

/**
 * Mix-in for adding the context of a clipping to the description of a Document.
 * Example: Image, Video.
 * 
 * @author andruid
 */
public class Clipping extends Metadata
{
	/**
	 * Explicit description of the clipped media in the source document.
	 */
	@mm_name("caption") 
	@simpl_scalar private
	MetadataString					caption;

	/**
	 * Text connected to the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	context;

	/**
	 * 
	 */
	public Clipping()
	{

	}

	/**
	 * @param metaMetadata
	 */
	public Clipping(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	public MetadataString context()
	{
		MetadataString result = this.context;
		if (result == null)
		{
			result = new MetadataString();
			this.context = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field context
	 **/

	@Override
	public String getContext()
	{
		return context().getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	@Override
	public void setContext(String context)
	{
		this.context().setValue(context);
	}

	/**
	 * The heavy weight setter method for field context
	 **/

	@Override
	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

	MetadataString caption()
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
		// return caption;
		return caption().getValue();
	}

	public void setCaption(String caption)
	{
		if (caption != null)
			this.caption().setValue(caption);
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
//			numWithCaption++;
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

	/**
	 * Derive further cFMetadata from an HTML <code>alt</code> attribute.
	 * This is to be executed subsequent to initital construction, in case we
	 * encounter additional references to this (in HTML).
	 */
	public void refineMetadata(Image newMetadata)
	{
		if (newMetadata != null)
		{
			setCaptionIfEmpty(newMetadata.getCaption());
			if (isNullContext() && !newMetadata.isNullContext())
			{
				hwSetContext(newMetadata.getContext());
			}
		}
	}
	public boolean isNullCaption()
	{
		return caption == null || caption.getValue() == null;
	}

	public boolean isNullContext()
	{
		return context == null || context.getValue() == null;
	}

///**
//* used for deriving statistics that track how many images
//* on the web have alt text.
//* @return
//*/   
//public static int hasCaptionPercent()
//{
//	return (int) (100.0f * (float) numWithCaption / ((float) numConstructed));
//}


}
