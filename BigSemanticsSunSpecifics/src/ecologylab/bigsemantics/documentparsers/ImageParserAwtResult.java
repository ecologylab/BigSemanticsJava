/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import java.awt.image.BufferedImage;

import ecologylab.bigsemantics.documentparsers.ParserResult;
import ecologylab.generic.Debug;

/**
 * @author andruid
 *
 */
public class ImageParserAwtResult extends Debug
implements ParserResult
{
	public BufferedImage getBufferedImage()
	{
		return bufferedImage;
	}
	BufferedImage				bufferedImage;
	/**
	 * 
	 */
	public ImageParserAwtResult()
	{
		
	}
	
	public ImageParserAwtResult(BufferedImage bufferedImage)
	{
		this.bufferedImage	= bufferedImage;
	}
	
	@Override
	public synchronized void recycle()
	{
		if (bufferedImage != null)
		{
			bufferedImage.flush();
			bufferedImage	= null;
		}
	}

}
