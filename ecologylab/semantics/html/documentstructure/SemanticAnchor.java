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
	private ParsedURL inlinkPurl;
	TermVector 				tv;
	boolean 					withinSite = false;
	
	//FIXME -- look at w sashi
	public SemanticAnchor(AnchorContext aContext, ParsedURL inlinkPurl)
	{
		super(aContext.getHref(), aContext.getAnchorText(), aContext.getAnchorContextString());
		this.inlinkPurl			= inlinkPurl;
		//FIXME -- huh???		hrefString = null;
		tv 			= new TermVector(aContext.getAnchorText() + aContext.getAnchorContextString());
	}

	public ITermVector termVector()
	{
		return tv;
	}
	
	public String toString()
	{
		return "[hrefPurl: " + inlinkPurl + " ; anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
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
		if (inlinkPurl != null)
		{
			inlinkPurl.recycle();
			inlinkPurl	= null;
		}
	}
}
