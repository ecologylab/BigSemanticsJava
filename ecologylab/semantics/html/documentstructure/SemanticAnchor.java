package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorFeature;

/**
 * Create a semantic anchor object to represent a link to a new (probably unparsed) Container 
 * from one that is currently being parsed.
 * These are like AnchorContexts, but use term vector representation instead of holding onto the strings 
 * @author andruid
 *
 */
public class SemanticAnchor implements TermVectorFeature
{
	static final int		NO_SPECIAL_SIGNIFICANCE		= 1;
	static final float	CONTENT_BODY_SIGNIFICANCE = 1.5f;
	static final int		SAME_DOMAIN_SIGNIFICANCE	= 2;
	static final int		CITATION_SIGNIFICANCE			= 4;
	
	/**
	 * hrefPurl of the container pointing to this container. <br>
	 */
	protected ParsedURL sourcePurl;
	TermVector 					tv;
	private boolean 		fromContentBody;
	private boolean			fromSemanticAction;
	public float					significance;
	
	public static final Double TEXT_OVER_CONTEXT_EMPHASIS_FACTOR	= 3.0;
	
	
	public SemanticAnchor(ParsedURL href, 
			ArrayList<AnchorContext> anchorContexts, 
			boolean citationSignificance, 
			float significanceVal, 
			ParsedURL sourcePurl,
			boolean fromContentBody, 
			boolean fromSemanticAction)
	{
		this.sourcePurl 					= sourcePurl;
		this.fromContentBody 			= fromContentBody;
		this.fromSemanticAction 	= fromSemanticAction;
		tv 												= new TermVector();
		
		if (citationSignificance)
			this.significance	= CITATION_SIGNIFICANCE*significanceVal;
		else if (sourcePurl != null && sourcePurl.domain().equals(href.domain()))
			this.significance	= SAME_DOMAIN_SIGNIFICANCE;
		else if(!fromSemanticAction && fromContentBody)
			this.significance	= CONTENT_BODY_SIGNIFICANCE;
		else
			this.significance	= NO_SPECIAL_SIGNIFICANCE;
			
		
		if(anchorContexts == null)
			return;
		
		for(AnchorContext anchorContext : anchorContexts)
			addAnchorContextToTV(anchorContext);
	}


	public void addAnchorContextToTV(AnchorContext anchorContext)
	{
		String anchorText = anchorContext.getAnchorText();
		String anchorContextString = anchorContext.getAnchorContextString();
		addAnchorContextToTV(anchorText, anchorContextString);
	}

	/**
	 * Directly add the strings to TV
	 * @param anchorText
	 * @param anchorContextString
	 */
	public void addAnchorContextToTV(String anchorText, String anchorContextString)
	{
		if (anchorText != null && anchorText.length() > 0)
			tv.add(anchorText, TEXT_OVER_CONTEXT_EMPHASIS_FACTOR);
		if (anchorContextString != null && anchorContextString.length() > 0)
			tv.add(anchorContextString);
	}
	public ITermVector termVector()
	{
		return tv;
	}
	
	public String toString()
	{
		return "SemanticAnchor:\n\t\t\tSourcePurl: " + sourcePurl + "\n\t\t\t"+ tv.toString(); 
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
	
	public float getSignificance()
	{
		return significance;
	}
	
	public boolean fromContentBody()
	{
		return fromContentBody;
	}
	
	public boolean fromSemanticAction()
	{
		return fromSemanticAction;
	}
	
	public ParsedURL sourcePurl()
	{
		return sourcePurl;
	}
}
