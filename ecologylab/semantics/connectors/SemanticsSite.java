/**
 * 
 */
package ecologylab.semantics.connectors;

import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;

import ecologylab.generic.Colors;
import ecologylab.generic.Palette;
import ecologylab.io.BasicSite;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.serialization.simpl_inherit;

/**
 * BasicSite extended to maintain semantics 
 * 
 * @author damaraju
 *
 */
@simpl_inherit
public class SemanticsSite extends BasicSite
implements Colors
{
	static ConcurrentHashMap<String, SemanticsSite> allSites	= new ConcurrentHashMap(50);

	/**
	 * true if any Container from this Site is a Seed.
	 */
	private boolean	isSeed;

	protected static final int 	MAX_GENERATIONS							= 15;

	static final double MAX_SURROGATES_FROM_SITE = 25.0;
	
	static final double NON_SEED_FACTOR						= .25;


	int								downloadsInProgress;
	
	int numElementsInComposition;

	private int	numCandidateText = 0;

	private int	numCandidateImages = 0;
	
	private int numCandidatesInArticleBody;
	
	private int numArticleBodies;

	/**
	 * counts of index pages and content pages
	 * initialized to 1 so indexContentRatio is always well defined.
	 */
	private int	indexPages	= 1;
	/**
	 * initialized to 1 so indexContentRatio is always well defined.
	 */
	private int	contentPages	= 1;

	private int	numSurrogatesFromSite	= 1;

	private int	numContainers;

	/**
	 * 
	 * @param domain
	 * @param infoCollector TODO
	 */
	public SemanticsSite(String domain, NewInfoCollector infoCollector)
	{
		this.domain = domain;
		strokeHue		= nextStrokeHue();
		fontIndex		= infoCollector.getAppropriateFontIndex();
	}
	
	/**
	 * DO NOT USE. meant only for XML Exception
	 */
	public SemanticsSite()
	{

	}


	public static SemanticsSite getOrConstruct(Document document, NewInfoCollector infoCollector) 
	{
		ParsedURL parsedURL	= document.getLocation();
		String domain				= parsedURL.domain();
		SemanticsSite result	= null;
		if (domain != null)
		{
			result							= allSites.get(domain);
			if (result == null) 
			{
				// record does not yet exist
				SemanticsSite newRec	= new SemanticsSite(domain, infoCollector);
				result = allSites.putIfAbsent(domain, newRec);
				if (result == null)
				{
					// put succeeded, use new value
					result = newRec;
				}
			}
		}
		return result;
	}

	public void newCandidateImage (boolean inArticleBody )
	{	
		numCandidateImages++;
		if (inArticleBody)
			numCandidatesInArticleBody++;
	}

	public void newCandidateText (boolean inArticleBody )
	{	
		numCandidateText++;
		if (inArticleBody)
			numCandidatesInArticleBody++;
	}
	
	public void newArticleBody()
	{
		this.numArticleBodies++;
	}

	/**
	 * Adds 1 to the index page count
	 */
	public void newIndexPage ( )
	{
		indexPages++;
	}

	/**
	 * Adds 1 to the index page count
	 */
	public void newContentPage ( )
	{
		contentPages++;
	}
	/**
	 * Make crawler work better by taking into account our efficiency in retrieving documents and extracting content from this site.
	 * 
	 * @return	0 < weightingFactor <= 1
	 */
	public double weightingFactor ( )
	{
		double result	= timeoutsFactor() * 
		(numSurrogatesFromSite > MAX_SURROGATES_FROM_SITE ? 1 : numSurrogatesFromSite/MAX_SURROGATES_FROM_SITE) *
		((double) contentPages / (double) (numContainers+1));
		
		if (!isSeed)
			result		 *= NON_SEED_FACTOR;
		
		return result;
	}

	public void incrementNumSurrogatesFrom ( )
	{
		numSurrogatesFromSite++;

	}

	public void incrementNumContainers ( )
	{
		numContainers++;
	}

	public synchronized void beginDownload()
	{
		downloadsInProgress++;
	}

	public synchronized void endDownload()
	{
		downloadsInProgress--;
	}

	/**
	 *  
	 * @return true when this site has downloadables in the download monitor
	 */
	public synchronized boolean hasQueuedDownloadables()
	{
		return downloadsInProgress > 0;
	}

	/**
	 * Gets the ratio of index pages to content pages
	 * @return number of index pages divided by the number of content pages
	 */
	public double getIndexContentRatio ( )
	{
		return indexPages/(double)contentPages;
	}
	
	public boolean isSeed()
	{
		return isSeed;
	}

	
	public static void addSite(SemanticsSite site)
	{
		allSites.put(site.domain(), site);
	}
	
	
	
	///////////////////////////////////////// text color stuff //////////////////////////////////////////////////////
	private float		strokeHue;
	private int			fontIndex;

	final Color[] strokeColors			= new Color[MAX_GENERATIONS];

	static final int 			MAX_INTENSITY	= 1024;

	static float strokeHues[] = 
	{
		YELLOW, GREEN, BLUE, RED, ORANGE, MAGENTA,
		YELLOW_GREEN, RED_ORANGE, BLUE_MAGENTA, CYAN, YELLOW_ORANGE, RED_MAGENTA
	};
	static int 	nextStrokeHue	= 0;

	static final float MIN_STROKE_VALUE	= .8f;
	static final float MAX_STROKE_VALUE	= 1.0f;
	static final float STEP_STROKE_VALUE	= .005f;

	static final float MIN_STROKE_SAT	= .35f;
	static final float MAX_STROKE_SAT	= .7f;
	static final float STEP_STROKE_SAT	= .05f;

	private static float nextStrokeHue()
	{
		float result	= strokeHues[nextStrokeHue++];
		nextStrokeHue    %= strokeHues.length;
		//	  println("nextStrokeHue="+nextStrokeHue+"\n");
		return result;
	}

	public Color getStrokeColor(int generation)
	{
		Color result		= null;
		if (generation < MAX_GENERATIONS)
			result			= strokeColors[generation];
		// comment out for too many printouts during buzz which makes interaction really bad
		// -- eunyee 
		//	  else
		//		  debug("WEIRD: generation="+generation);
		if (result == null)
		{
			result			= calculateStrokeColor(generation);
			//debug("strokeColor(0)="+result);
			if (generation < MAX_GENERATIONS)
				strokeColors[generation] = result;
		}	  
		return result;
	}
	private Color calculateStrokeColor(int generation)
	{
		float strokeSat	= MAX_STROKE_SAT - generation*STEP_STROKE_SAT;
		if (strokeSat < MIN_STROKE_SAT)
			strokeSat	= MIN_STROKE_SAT;
		float strokeValue	= MAX_STROKE_VALUE - generation*STEP_STROKE_VALUE;
		if (strokeValue < MIN_STROKE_VALUE)
			strokeValue	= MIN_STROKE_VALUE;

		float strokeHue	= this.strokeHue;


		if ((strokeHue == MAGENTA) || (strokeHue == GREEN))
		{
			strokeSat  -= .1f;
		}
		else if ((strokeHue == RED) ||
				(strokeHue == BLUE_MAGENTA))
		{
			strokeSat  -= .18f;
		}
		else if (strokeHue == BLUE)
		{
			strokeSat  -= .25f;
		}

		return Palette.hsvColor(strokeHue, strokeSat, strokeValue);
	}

	public int fontIndex()
	{
		return fontIndex;
	}

	public float strokeHue()
	{
		return strokeHue;
	}
	/**
	 * For use only by cm.state for restoring saved values.
	 */
	public void setStrokeHue(float strokeHue)
	{
		this.strokeHue	= strokeHue;
	}
	/**
	 * For use only by cm.state for restoring saved values.
	 */
	public void setFontIndex(int fontIndex)
	{
		this.fontIndex	= fontIndex;
	}

	public String toString()
	{
		return "SemanticsSite: " + domain;
	}

}
