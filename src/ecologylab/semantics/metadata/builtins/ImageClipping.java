/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.builtins.declarations.ImageClippingDeclaration;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;


/**
 * @author andruid
 *
 */
@simpl_inherit
public class ImageClipping extends ImageClippingDeclaration
{
	
	public ImageClipping()
	{
		super();
	}
	
	public ImageClipping(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}

	public ImageClipping(MetaMetadataCompositeField metaMetadata, Image clippedMedia, Document source, Document outlink, String caption, String context)
	{
		this(metaMetadata);
		MediaClipping.initMediaClipping(this, clippedMedia, source, outlink, caption, context);
	}
	
	@Override
	public boolean isImage()
	{
		return true;
	}
	
}
