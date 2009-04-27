package ecologylab.semantics.html.documentstructure;

/**
 * Holds the anchor node with the text string surrounding it
 * @author damaraju
 *
 */
public class AnchorContext
{

	protected String hrefString;
	protected 	String anchorText;
	protected String anchorContextString;
	
	public AnchorContext(String href, String anchorText)
	{
		this.hrefString		= href;
		this.anchorText = anchorText;
	}
	
	public AnchorContext(String href, String anchorText, String anchorContextString)
	{
		this.hrefString 					= href;
		this.anchorText 			= anchorText;
		this.anchorContextString	= anchorContextString;
	}
	public String getHrefString()
	{
		return hrefString;
	}
	public void setHref(String href)
	{
		this.hrefString = href;
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
		return "[href: " + hrefString + " ; anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
	}
}
