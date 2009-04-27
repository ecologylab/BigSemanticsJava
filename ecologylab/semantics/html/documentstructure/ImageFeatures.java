/**
 * 
 */
package ecologylab.semantics.html.documentstructure;

import ecologylab.generic.StringTools;
import ecologylab.media.PixelBased;
import ecologylab.semantics.html.HTMLElement;
import ecologylab.semantics.html.utils.HTMLAttributeNames;

/**
 * Static methods for operating on images, and recognizing features during information extraction.
 *
 * @author andruid 
 */
public class ImageFeatures
implements HTMLAttributeNames
{

	/**
	 * Recognize whether the image is informative or not based on its attributes and size, aspect ratio. 
	 * 
	 * @param imageNode		HTML node from <code>img</code> tag, with attributes.
	 * 
	 * @return						true if image is recognized as informative otherwise false.
	 */
	public static boolean isInformativeImage(HTMLElement imageNode) 
	{
		int width 		= imageNode.getAttributeAsInt(WIDTH, -1);
		int height 		= imageNode.getAttributeAsInt(HEIGHT, -1);
		String alt 		= getNonBogusAlt(imageNode);
		
		return isInformativeImage(width, height, alt);
	}

	/**
	 * Recognize whether the image is informative or not based on its attributes and size, aspect ratio. 
	 * 
	 * @param width
	 * @param height
	 * @param alt
	 * 
	 * @return						true if image is recognized as informative otherwise false.
	 */
	//TODO -- Resolve with ImgElement.designRole().
	public static boolean isInformativeImage(int width, int height, String alt)
	{
		float aspectRatio = (float) width / (float) height;
		aspectRatio 			= (aspectRatio>1.0f) ?  (float)1.0f/aspectRatio : aspectRatio;
	
		boolean informImg = !(alt!=null && alt.toLowerCase().contains("advertis")) ;
//	String imgUrl = imageNode.getAttribute(SRC);
		//TODO -- should we do more advertisement filtering here?!
		
		if( (width!=-1 && width<PixelBased.MIN_WIDTH) || (height!=-1 && height<PixelBased.MIN_HEIGHT) )
			informImg = false;
	
		if( aspectRatio > 0.9 )
			informImg = false;
	
		return informImg;
	}
	
	/**
	 * Get the alt text attribute from the image node, if there is one.
	 * Check to see if it is not bogus (not empty, "null", a url, contains advertis).
	 * If it is bogus, clear the attribute in the image node.
	 * Otherwise, return it.
	 * 
	 * @param imageNode
	 * @return		null, or a usable alt String.
	 */
	public static String getNonBogusAlt(HTMLElement imageNode)
	{
		String altText 					= imageNode.getAttribute(ALT);
		if ((altText != null) && (ImageFeatures.altIsBogus(altText)))
		{
			altText								= null;
			imageNode.clearAttribute(ALT);
		}
		return altText;
	}

	/**
	 * Test to see if alt attribute from HTML is garbage.
	 * 
	 * @param alt
	 * 
	 * @return	true if its null, empty string, "null", or looks like a filename.
	 */
	public static boolean altIsBogus(String alt)
	{
		boolean result	= (alt == null) || (alt.length() == 0) || "null".equals(alt) || "image".equals(alt);
		if (!result)
		{
			if (!StringTools.contains(alt, ' ')) // no spaces
			{
				// contains ., and not at end?
				int dotIndex	= alt.indexOf('.');
				if ((dotIndex > -1) && (dotIndex < (alt.length() - 1)))
				{
					//			   debug("This alt is really a filename: " + caption);
					result	= true;
				}	       
			}
		}
		
		return result;
	}

	/*	We don't need this as we have an advertisement filter in the container.createImgElement(), and the filter is in cf.model.Filter.
	 * -- Eunyee 	
	if( imgUrl!=null )
	{

		String urlChunks[] = imgUrl.split("/");
		for(int j=0; j<urlChunks.length; j++)
		{
			String temp = urlChunks[j];
		//	System.out.println("url Chunk:" + temp);
			if( temp.toLowerCase().equals("ad") || temp.toLowerCase().equals("adv") ||
					temp.toLowerCase().contains("advertis") )
				informImg = false;
		}
	}
	 */		

}
