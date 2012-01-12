/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;


/**
 * @author andruid
 *
 */
@simpl_inherit
public class ImageClipping extends MediaClipping<Image>
{
	public ImageClipping()
	{
		
	}
	public ImageClipping(MetaMetadataCompositeField metaMetadata, Image clippedMedia, Document source, Document outlink, String caption, String context)
	{
		super(metaMetadata, clippedMedia, source, outlink, caption, context);
	}
	
	@Override
	public boolean isImage()
	{
		return true;
	}
}
