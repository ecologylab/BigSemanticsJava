package ecologylab.semantics.html.documentstructure;

import ecologylab.net.ParsedURL;

/**
 * Holds the anchor node with the text string surrounding it
 * @author damaraju
 *
 */
public class AnchorContext
{
	protected ParsedURL 	href;
	protected String 			anchorText;
	protected String 			anchorContextString;
	
	public AnchorContext(ParsedURL href, String anchorText)
	{
		this.href				= href;
		this.anchorText = anchorText;
	}
	
	public AnchorContext(ParsedURL href, String anchorText, String anchorContextString)
	{
		this.href									= href;
		this.anchorText 					= anchorText;
		this.anchorContextString	= anchorContextString;
	}
 
	
	public ParsedURL getHref()
	{
		return href;
	}

	public void setHref(ParsedURL href)
	{
		this.href = href;
	}

	public String getAnchorText()
	{
		return anchorText;
	}
	public void setAnchorText(String anchorText)
	{
		this.anchorText = anchorText;
	}
	public String getAnchorContextString()
	{
		return anchorContextString;
	}
	public void setAnchorContextString(String anchorContextString)
	{
		this.anchorContextString = anchorContextString;
	}
	
	public String toString()
	{
		return "[href: " + href + " ; anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
	}
}
