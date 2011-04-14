/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.awt.image.BufferedImage;

import ecologylab.serialization.simpl_inherit;


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
	ImageClipping(Image clippedMedia, Document source, Document outlink, String caption, String context)
	{
		super(clippedMedia, source, outlink, null, null);
	}
}
