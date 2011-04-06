/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.awt.image.BufferedImage;


/**
 * @author andruid
 *
 */
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
