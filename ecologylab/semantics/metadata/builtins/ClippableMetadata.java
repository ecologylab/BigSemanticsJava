/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;

/**
 * Metadata with a Clipping in it.
 * Used for Image, Text, Video, ...
 * 
 * @author andruid
 *
 */
public class ClippableMetadata extends Metadata
{
	@simpl_composite
	private Clipping										clipping;

	/**
	 * 
	 */
	public ClippableMetadata()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param metaMetadata
	 */
	public ClippableMetadata(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	static MetaMetadataCompositeField 		clippingMM;
	
	static MetaMetadataCompositeField getClippingMM(Class<? extends ClippableMetadata> parentClass)
	{
		MetaMetadataCompositeField result = clippingMM;
		if (result == null)
		{
			result						= repository().getMM(parentClass, "clipping");
			clippingMM							= result;
		}
		return result;
	}
	
	public Clipping clipping()
	{
		Clipping result	= this.clipping;
		if (result == null)
		{
			result				= new Clipping(getClippingMM(this.getClass()));
			this.clipping	= result;
		}
		return result;
	}
	
	
	public MetadataString context()
	{
		return clipping().context();
	}
	
	public String getContext()
	{
		return clipping == null ? null : clipping.getContext();
	}
	
	/**
	 * Sets the value of the field context
	 **/
	public void setContext(String context)
	{
		clipping().setContext(context);
	}

	public void hwSetContext(String context)
	{
		clipping().hwSetContext(context);
	}
	
	
	public MetadataString caption()
	{
		return clipping().caption();
	}
	public String getCaption()
	{
		return clipping == null ? null : clipping.getCaption();
	}
	
	
	public void setCaption(String context)
	{
		Clipping clipping = clipping();
		clipping.setCaption(context);
	}

	public void hwSetCaption(String context)
	{
		Clipping clipping = clipping();
		clipping.hwSetCaption(context);
	}
	public void setCaptionIfGood(String newCandidateCaption)
	{
		clipping().setCaptionIfGood(newCandidateCaption);
	}
	public void setCaptionIfEmpty(String newCandidateCaption)
	{
		clipping().setCaptionIfEmpty(newCandidateCaption);
	}
	
	public boolean isNullCaption()
	{
		return clipping == null || clipping.isNullCaption();
	}

	public boolean isNullContext()
	{
		return clipping == null || clipping.isNullContext();
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

	/**
	 * @return the clipping
	 */
	public Clipping getClipping()
	{
		return clipping;
	}

	/**
	 * @param clipping the clipping to set
	 */
	public void setClipping(Clipping clipping)
	{
		this.clipping = clipping;
	}
	
	

}
