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
	/**
	 * the purl that this anchor was parsed from.
	 */
	protected ParsedURL		sourcePurl;
	
	/**
	 * Flags for testing quality of this anchorContext
	 */
	protected boolean			fromContentBody;
	protected boolean			fromSemanticActions;
	
	public AnchorContext(ParsedURL href, String anchorText, String anchorContextString, ParsedURL sourcePurl, boolean fromContentBody, boolean fromSemanticActions)
	{
		this.href									= href;
		this.anchorText 					= anchorText;
		this.anchorContextString	= anchorContextString;
		this.sourcePurl						= sourcePurl;
		this.fromContentBody 			= fromContentBody;
		this.fromSemanticActions	= fromSemanticActions;
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
	
	public ParsedURL getSourcePurl()
	{
		return sourcePurl;
	}
	
	public void setSourcePurl(ParsedURL sourcePurl)
	{
		this.sourcePurl = sourcePurl;
	}
	public String toString()
	{
		return "[href: " + href + " ; anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
	}
	
	public boolean fromContentBody()
	{
		return fromContentBody;
	}
	
	public boolean fromSemanticActions()
	{
		return fromSemanticActions;
	}
}
