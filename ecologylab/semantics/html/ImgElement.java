/**
 * 
 */
package ecologylab.semantics.html;

import org.w3c.tidy.TdNode;

import ecologylab.generic.Generic;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.ImageFeatures;

/**
 * HTMLElement that corresponds to the img tag.
 * 
 * @author andruid
 *
 */
public class ImgElement extends WithPurlElement
{
	ParsedURL				src;
	
	String					alt;	
	int							width;
	int							height;
	boolean					isMap;
	
	/**
	 * Text context recognized in document.
	 */
	String					textContext;
	/**
	 * Extracted caption recognized in document.
	 */
	String					extractedCaption;
	

	/**
	 * @param node
	 * @param basePurl TODO
	 */
	public ImgElement(TdNode node, ParsedURL basePurl)
	{
		super(node, basePurl);
		int i = 0;
		int j = i + 1;
	}

	@Override
	protected void setAttribute(String key, String value)
	{
		if (SRC.equals(key))
		{
			src 		= ((value != null) && value.startsWith("data:")) ? null : (basePurl == null) ? ParsedURL.getAbsolute(value) : basePurl.createFromHTML(value);
		}
		else if (ALT.equals(key))
			alt			= value;
		else if (WIDTH.equals(key))
			width		= value == null ? INDEX_NOT_CALCULATED : Generic.parseInt(value, INDEX_NOT_CALCULATED );
		else if (HEIGHT.equals(key))
			height	= value == null ? INDEX_NOT_CALCULATED : Generic.parseInt(value, INDEX_NOT_CALCULATED );
		else if (ISMAP.equals(key))
			isMap		= value != null && "true".equals(value);
		else
			super.setAttribute(key, value);
	}

	public ParsedURL getSrc()
	{
		return src;
	}

	public void setSrc(ParsedURL src)
	{
		this.src = src;
	}
	


	public void setAlt(String alt)
	{
		this.alt = alt;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public boolean isMap()
	{
		return isMap;
	}

	public void setMap(boolean isMap)
	{
		this.isMap = isMap;
	}

	public String getTextContext()
	{
		return textContext;
	}

	public void setTextContext(String textContext)
	{
		this.textContext = textContext;
	}

	public void setTextContext(StringBuilder buffy)
	{
		this.textContext = buffy.toString();
	}

	public String getExtractedCaption()
	{
		return extractedCaption;
	}

	public void setExtractedCaption(String extractedCaption)
	{
		this.extractedCaption = extractedCaption;
	}


	public String getAlt()
	{
		return alt;
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
	public String getNonBogusAlt()
	{
		String altText 					= this.getAlt();
		if ((altText != null) && (ImageFeatures.altIsBogus(altText)))
		{
			altText								= null;
			alt										= null;
		}
		return altText;
	}

	
	/**
	 * Recognize whether the image is informative or not based on its attributes and size, aspect ratio. 
	 * 
	 * @param imageNode		HTML node from <code>img</code> tag, with attributes.
	 * 
	 * @return						true if image is recognized as informative otherwise false.
	 */
	public boolean isInformativeImage() 
	{
		if (src == null)
			return false;
		
		String alt 		= getNonBogusAlt();
		
		boolean informImg = !(alt!=null && alt.toLowerCase().contains("advertis")) ;
//	String imgUrl = imageNode.getAttribute(SRC);
		//TODO -- should we do more advertisement filtering here?!
		
		//TODO -- should we use an encompassing hyperlink and its destination as features ???!
		
		if (informImg)
		{
			int mimeIndex		= src.mimeIndex();;
			int designRole	= ImageFeatures.designRole(width, height, mimeIndex, isMap);
			informImg				= (designRole == INFORMATIVE) || (designRole == UNKNOWN);
		}
		return informImg;
	}

	public String toString()
	{
		String location	= src == null ? "" : src.toString();
		return "ImgElement[" + width+","+height+"] " + location;
	}
}
