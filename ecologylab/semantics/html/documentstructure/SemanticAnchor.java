package ecologylab.semantics.html.documentstructure;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorFeature;

public class SemanticAnchor extends AnchorContext implements TermVectorFeature
{
	/**
	 * hrefPurl of the container pointing to this container. <br>
	 * It clears the hrefString, because at this stage, the container is created and the hrefString is no longer required.
	 */
	private ParsedURL hrefPurl;
	TermVector tv;
	boolean withinSite = false;
	
	public SemanticAnchor(AnchorContext aContext, ParsedURL purl)
	{
		super(aContext.getHrefString(), aContext.getAnchorText(), aContext.getAnchorContextString());
		this.setHrefPurl(purl);
		hrefString = null;
		tv = new TermVector(aContext.getAnchorText() + aContext.getAnchorContextString());
	}

	public void setHrefPurl(ParsedURL hrefPurl)
	{
		this.hrefPurl = hrefPurl;
	}

	public ParsedURL getHrefPurl()
	{
		return hrefPurl;
	}

	public ITermVector termVector()
	{
		return tv;
	}
	
	public String toString()
	{
		return "[hrefPurl: " + hrefPurl + " ; anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
	}

	public void setWithinSite(boolean b)
	{
		withinSite = b;
	}
}
