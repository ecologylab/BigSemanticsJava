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

//TODO -- make serializable!
public class SemanticAnchor implements TermVectorFeature
{
	static final float		NO_SPECIAL_SIGNIFICANCE		= 1;
	static final float		CONTENT_BODY_SIGNIFICANCE 	= 1.5f;
	static final float		SAME_DOMAIN_SIGNIFICANCE_PENALTY	= .5f;
	static final int		CITATION_SIGNIFICANCE		= 4;
	
	/**
	 * hrefPurl of the container pointing to this container. <br>
	 */
	protected ParsedURL 	sourcePurl;
	TermVector 				tv;
	
	//FIXME -- these fields must be merged!!!
	LinkType 						linkType;	
	
	private float			significance;
	
	public static final Double TEXT_OVER_CONTEXT_EMPHASIS_FACTOR	= 3.0;
	
	
	/**
	 * 
	 * @param linkType
	 * @param destinationPurl				The linked destination document that this refers to.
	 * @param anchorContexts
	 * @param sourcePurl			The source document that this link originated from.
	 * @param significanceVal
	 */
	public SemanticAnchor(LinkType linkType, 
			ParsedURL destinationPurl, 
			ArrayList<AnchorContext> anchorContexts, 
			ParsedURL sourcePurl, 
			float significanceVal)
	{
		this.sourcePurl 				= sourcePurl;
		this.linkType						= linkType;
		tv 							= new TermVector();
		
		switch (linkType)
		{
		case CITATION_SEMANTIC_ACTION:
			this.significance	= CITATION_SIGNIFICANCE * significanceVal;
			break;
		case WILD_CONTENT_BODY:
			this.significance	= CONTENT_BODY_SIGNIFICANCE;
			//TODO should there be some (but less) penalty here if same domain?
			break;
		case WILD:
			if (sourcePurl != null && sourcePurl.domain().equals(destinationPurl.domain()))
			{
				this.significance	= SAME_DOMAIN_SIGNIFICANCE_PENALTY;
				break;
			}
		default:
			this.significance				= NO_SPECIAL_SIGNIFICANCE;
			break;
		}

		if(anchorContexts != null)
		{
			for(AnchorContext anchorContext : anchorContexts)
				addAnchorContextToTV(anchorContext);
		}
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
	
	public ParsedURL sourcePurl()
	{
		return sourcePurl;
	}
	
	public boolean fromSemanticAction()
	{
		return linkType == LinkType.TRUSTED_SEMANTIC_ACTION || linkType == LinkType.SITE_BOOSTED_SEMANTIC_ACTION;
	}
}
