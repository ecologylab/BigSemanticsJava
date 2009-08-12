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
//	private ParsedURL inlinkPurl;
	TermVector 				tv;
	boolean 					withinSite = false;
	
	public static final double TEXT_OVER_CONTEXT_EMPHASIS_FACTOR	= 3;
	
	public SemanticAnchor(ParsedURL href, String anchorText, String anchorContextString)
	{
		super(href, anchorText, anchorContextString);
		tv 									= new TermVector();
		if (anchorText != null && anchorText.length() > 0)
			tv.add(anchorText, TEXT_OVER_CONTEXT_EMPHASIS_FACTOR);
		if (anchorContextString != null && anchorContextString.length() > 0)
			tv.add(anchorContextString);
	}
	public SemanticAnchor(AnchorContext aContext /*, ParsedURL inlinkPurl */)
	{
		this(aContext.getHref(), aContext.getAnchorText(), aContext.getAnchorContextString());
	}

	public ITermVector termVector()
	{
		return tv;
	}
	
	public String toString()
	{
		return "[anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
	}

	public void setWithinSite(boolean b)
	{
		withinSite = b;
	}
	
	public void recycle()
	{
		if (tv != null)
		{
			tv.recycle();
			tv					= null;
		}
//		inlinkPurl	= null;
	}
}
