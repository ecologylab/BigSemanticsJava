/**
 * 
 */
package ecologylab.semantics.html.documentstructure;

import java.util.regex.Pattern;

import ecologylab.generic.StringTools;
import ecologylab.semantics.html.utils.HTMLAttributeNames;

/**
 * Static methods for operating on images, and recognizing features during information extraction.
 *
 * @author andruid 
 */
public class ImageFeatures
implements HTMLAttributeNames, ImageConstants
{
	/**
	 * Test to see if alt attribute from HTML is garbage.
	 * 
	 * @param alt
	 * 
	 * @return	true if its null, empty string, "null", or looks like a filename.
	 */
	static Pattern BOGUS_ALT_MATCHER = Pattern.compile("^([[:alpha:]]+_*)+$");
	public static boolean altIsBogus(String alt)
	{
		boolean result	= (alt == null) || (alt.length() == 0) || "null".equals(alt) || "image".equals(alt) || alt.endsWith(".jpg") || BOGUS_ALT_MATCHER.matcher(alt).matches();
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

	/**
	 * Use heuristics on width & height to analyze whether this image is junk, such as a
	 * spacer (very small), a nav element, or an advertisement.
	 * <p/>
	 * If width or height is set to 0, the assumption is that we have no data about size, so
	 * we must return UNKNOWN.
	 */
	//FIXME -- unify with ImageFeatures.isInformativeImage()
	public static int designRole(int width, int height, int mimeIndex, boolean isMap)
	{
		float aspectRatio = (float) width / (float) height;
		if (aspectRatio > 1.0f)
			aspectRatio 		=  (float) 1.0f/aspectRatio;
		if (aspectRatio < 0.35f)
			return UN_INFORMATIVE;
		
		//TODO -- should area be a feature? 		int area		= width * height;
		
		int result	= UNKNOWN;
		if ((width > 0) && (height > 0))
		{
			if ((width < MIN_WIDTH) || (height < MIN_HEIGHT))
					result	= UN_INFORMATIVE;
			else
			{
				result		= INFORMATIVE;
				if ((mimeIndex != JPG) && (mimeIndex != UNKNOWN_MIME))
				{	/* if (mimeType == GIF, PNG) */
					if (isMap || (aspectRatio > 0.3f))	// stricter criteria for these mime types
						result	= UN_INFORMATIVE;
				}
			}
		}
		else	// no width/height params
			if (mimeIndex != JPG)
				result			= UN_INFORMATIVE;	// only give benefit of doubt to jpegs
		return result;
	}

	/**
	 * Use heuristics on width & height to analyze whether this image is junk, such as a
	 * spacer (very small), a nav element, or an advertisement.
	 * <p/>
	 * If width or height is set to 0, the assumption is that we have no data about size, so
	 * we must return UNKNOWN.
	 */
	public static int designRole(int width, int height)
	{
		// (JPG images are most privleged
		return designRole(width, height, JPG, false);
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
