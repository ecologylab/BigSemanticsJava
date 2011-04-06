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
	public ImageClipping(Image clippedMedia, Document source, Document outlink)
	{
		super(clippedMedia, source, outlink);
	}
}
