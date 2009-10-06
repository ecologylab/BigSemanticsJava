package ecologylab.semantics.html.documentstructure;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorFeature;

/**
 * Create a semantic anchor object to represent a link to a new (probably unparse) Container 
 * from one that is currently being parsed.
 * 
 * @author andruid
 *
 */
public class SemanticAnchor extends AnchorContext implements TermVectorFeature
{
	static final int	NO_SPECIAL_SIGNIFICANCE		= 1;
	static final int	SAME_DOMAIN_SIGNIFICANCE	= 2;
	static final int	CITATION_SIGNIFICANCE			= 4;
	
	/**
	 * hrefPurl of the container pointing to this container. <br>
	 * It clears the hrefString, because at this stage, the container is created and the hrefString is no longer required.
	 */
//	private ParsedURL inlinkPurl;
	TermVector 				tv;
	
	final float					signficance;
	
	public static final Double TEXT_OVER_CONTEXT_EMPHASIS_FACTOR	= 3.0;
	
	public SemanticAnchor(ParsedURL containerPURL, ParsedURL href, String anchorText, String anchorContextString, boolean citationSignificance, float significanceVal)
	{
		super(href, anchorText, anchorContextString);
		tv 									= new TermVector();
		if (anchorText != null && anchorText.length() > 0)
			tv.add(anchorText, TEXT_OVER_CONTEXT_EMPHASIS_FACTOR);
		if (anchorContextString != null && anchorContextString.length() > 0)
			tv.add(anchorContextString);
		if (citationSignificance)
			this.signficance	= CITATION_SIGNIFICANCE*significanceVal;
		else if (containerPURL != null && containerPURL.domain().equals(href.domain()))
			this.signficance	= SAME_DOMAIN_SIGNIFICANCE;
		else
			this.signficance	= NO_SPECIAL_SIGNIFICANCE;
			
	}
	public SemanticAnchor(ParsedURL containerPURL, AnchorContext aContext /*, ParsedURL inlinkPurl */)
	{
		this(containerPURL, aContext.getHref(), aContext.getAnchorText(), aContext.getAnchorContextString(), false, 1);
	}

	public ITermVector termVector()
	{
		return tv;
	}
	
	public String toString()
	{
		return "[anchorText: " + anchorText + " ; anchorContext: " + anchorContextString + "]";  
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
