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
	BufferedImage					sourceImage;
	
	public boolean downloadAndParse()
	{
		return false;
	}
}
