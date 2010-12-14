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
	 * Location of the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	xpath;

	/**
	 * The source document.
	 */
//	@simpl_composite
//	private Document				source;
//	
//	/**
//	 * A hyperlinked Document.
//	 */
//	@simpl_composite
//	private Document				outlink;
	

	static int							numWithCaption;
	/**
	 * Total number of images we have created within this session
	 */
	static int							numConstructed;

	
	/**
	 * 
	 */
	public Clipping()
	{
		numConstructed++;
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

	public String getContext()
	{
		return context == null ? null : context.getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setContext(String context)
	{
		this.context().setValue(context);
	}

	/**
	 * The heavy weight setter method for field context
	 **/

	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
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

	/**
	 * used for deriving statistics that track how many images
	 * on the web have alt text.
	 * @return
	 */   
	public static int hasCaptionPercent()
	{
		return (int) (100.0f * (float) numWithCaption / ((float) numConstructed));
	}


//	/**
//	 * Derive further cFMetadata from an HTML <code>alt</code> attribute.
//	 * This is to be executed subsequent to initital construction, in case we
//	 * encounter additional references to this (in HTML).
//	 */
//	public void refineMetadata(Image newMetadata)
//	{
//		if (newMetadata != null)
//		{
//			setCaptionIfEmpty(newMetadata.getCaption());
//			if (isNullContext() && !newMetadata.isNullContext())
//			{
//				hwSetContext(newMetadata.getContext());
//			}
//		}
//	}
	public boolean isNullCaption()
	{
		return caption == null || caption.getValue() == null;
	}

	public boolean isNullContext()
	{
		return context == null || context.getValue() == null;
	}
	
	public boolean isNullXpath()
	{
		return xpath == null || xpath.getValue() == null;
	}
	
	public MetadataString xpath()
	{
		MetadataString result = this.xpath;
		if (result == null)
		{
			result = new MetadataString();
			this.xpath = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field context
	 **/

	public String getXpath()
	{
		return xpath == null ? null : xpath().getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setXpath(String context)
	{
		this.xpath().setValue(context);
	}



}
