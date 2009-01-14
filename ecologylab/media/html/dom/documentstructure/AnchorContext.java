package ecologylab.media.html.dom.documentstructure;

/**
 * Holds the anchor node with the text string surrounding it
 * @author damaraju
 *
 */
public class AnchorContext
{

	private String href;
	private String anchorText;
	private String anchorContextString;
	
	public AnchorContext(String href, String anchorText)
	{
		this.href		= href;
		this.anchorText = anchorText;
	}
	
	public AnchorContext(String href, String anchorText, String anchorContextString)
	{
		this.href 					= href;
		this.anchorText 			= anchorText;
		this.anchorContextString	= anchorContextString;
	}
	public String getHref()
	{
		return href;
	}
	public void setHref(String href)
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
