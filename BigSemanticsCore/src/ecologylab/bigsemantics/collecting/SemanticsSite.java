/**
 * 
 */
package ecologylab.bigsemantics.collecting;

// import java.awt.Color;

import java.util.HashMap;

import ecologylab.bigsemantics.platformspecifics.SemanticsPlatformSpecifics;
import ecologylab.concurrent.BasicSite;
import ecologylab.generic.Colors;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * BasicSite extended to maintain semantics
 * 
 * @author damaraju
 * 
 */
@simpl_inherit
public class SemanticsSite extends BasicSite implements Colors
{
	// static ConcurrentHashMap<String, SemanticsSite> allSites = new ConcurrentHashMap(50);

	/**
	 * true if any Container from this Site is a Seed.
	 */
	private boolean							isSeed;

	protected static final int	MAX_GENERATIONS						= 15;

	static final double					MAX_SURROGATES_FROM_SITE	= 25.0;

	static final double					NON_SEED_FACTOR						= .25;

	int													numElementsInComposition;

	private int									numCandidateText					= 0;

	private int									numCandidateImages				= 0;

	private int									numCandidatesInArticleBody;

	private int									numArticleBodies;

	/**
	 * counts of index pages and content pages initialized to 1 so indexContentRatio is always well
	 * defined.
	 */
	private int									indexPages								= 1;

	/**
	 * initialized to 1 so indexContentRatio is always well defined.
	 */
	private int									contentPages							= 1;

	private int									numSurrogatesFromSite			= 1;

	private int									numContainers;
	
	ParsedURL pathToFavicon = null;
	static public HashMap faviPathHash = new HashMap();
	
	/**
	 * 
	 * @param domain
	 * @param infoCollector
	 *          TODO
	 */
	public SemanticsSite(String domain, SemanticsGlobalScope infoCollector)
	{
		this.domain = domain;
		//pathToFavicon = ParsedURL.getAbsolute("http://www." + domain + "/favicon.ico", "Bad favicon path.");
		//faviPathHash.put(domain, pathToFavicon);

		strokeHue = nextStrokeHue();
		fontIndex = infoCollector.getAppropriateFontIndex();
	}

	/**
	 * DO NOT USE. meant only for XML Exception
	 */
	public SemanticsSite()
	{

	}
	
	public void setFaviconPath(String path, ParsedURL site) 
	{
		ParsedURL favPath = ParsedURL.getAbsolute(path, "Malformed favicon path. Try with relative");
		if(favPath == null) {
			favPath = site.getRelative(path, "Still bad URL. Use root method.");
			if(favPath == null) 
			{
				//I don't like this if statement
				favPath = ParsedURL.getAbsolute("http://" + site.domain() + "/favicon.ico");
			}
		}
		
		if(favPath != null) {
			pathToFavicon = favPath;
			faviPathHash.put(domain, pathToFavicon);
		}
	}
	
	public ParsedURL getFaviconPath()
	{
		return pathToFavicon;
	}

	public void newCandidateImage(boolean inArticleBody)
	{
		numCandidateImages++;
		if (inArticleBody)
			numCandidatesInArticleBody++;
	}

	public void newCandidateText(boolean inArticleBody)
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
	public void newIndexPage()
	{
		indexPages++;
	}

	/**
	 * Adds 1 to the index page count
	 */
	public void newContentPage()
	{
		contentPages++;
	}

	/**
	 * Make crawler work better by taking into account our efficiency in retrieving documents and
	 * extracting content from this site.
	 * 
	 * @return 0 < weightingFactor <= 1
	 */
	public double weightingFactor()
	{
		double result = timeoutsFactor()
				* (numSurrogatesFromSite > MAX_SURROGATES_FROM_SITE ? 1 : numSurrogatesFromSite
						/ MAX_SURROGATES_FROM_SITE) * ((double) contentPages / (double) (numContainers + 1));

		if (!isSeed)
			result *= NON_SEED_FACTOR;

		return result;
	}

	public void incrementNumSurrogatesFrom()
	{
		numSurrogatesFromSite++;

	}

	public void incrementNumContainers()
	{
		numContainers++;
	}

	/**
	 * 
	 * @return true when this site has downloadables in the download monitor
	 */
	public synchronized boolean hasQueuedDownloadables()
	{
		return downloadsQueuedOrInProgress > 0;
	}

	/**
	 * Gets the ratio of index pages to content pages
	 * 
	 * @return number of index pages divided by the number of content pages
	 */
	public double getIndexContentRatio()
	{
		return indexPages / (double) contentPages;
	}

	public boolean isSeed()
	{
		return isSeed;
	}

	// /////////////////////////////////////// text color stuff
	// //////////////////////////////////////////////////////
	private float			strokeHue;

	private int				fontIndex;

	// final Color[] strokeColors = new Color[MAX_GENERATIONS];
	final Object[]		strokeColors		= new Object[MAX_GENERATIONS];

	static final int	MAX_INTENSITY		= 1024;

	static float			strokeHues[]		=
																		{ YELLOW, GREEN, BLUE, RED, ORANGE, MAGENTA, YELLOW_GREEN,
			RED_ORANGE, BLUE_MAGENTA, CYAN, YELLOW_ORANGE, RED_MAGENTA };

	static int				nextStrokeHue		= 0;

	// static final float MIN_STROKE_VALUE = .8f;
	// static final float MAX_STROKE_VALUE = 1.0f;
	// static final float STEP_STROKE_VALUE = .005f;
	//
	// static final float MIN_STROKE_SAT = .35f;
	// static final float MAX_STROKE_SAT = .7f;
	// static final float STEP_STROKE_SAT = .05f;

	static final int	DND_GENERATION	= 3;

	private static float nextStrokeHue()
	{
		float result = strokeHues[nextStrokeHue++];
		nextStrokeHue %= strokeHues.length;
		// println("nextStrokeHue="+nextStrokeHue+"\n");
		return result;
	}

	public Object getStrokeColor()
	{
		return SemanticsPlatformSpecifics.get().getStrokeColor(DND_GENERATION, MAX_GENERATIONS,
				strokeColors, strokeHue);
	}

	// public Object getStrokeColor(int generation)
	// {
	// Object result = null;
	// if (generation < MAX_GENERATIONS)
	// result = strokeColors[generation];
	// // comment out for too many printouts during buzz which makes interaction really bad
	// // -- eunyee
	// // else
	// // debug("WEIRD: generation="+generation);
	// if (result == null)
	// {
	// result = calculateStrokeColor(generation);
	// //debug("strokeColor(0)="+result);
	// if (generation < MAX_GENERATIONS)
	// strokeColors[generation] = result;
	// }
	// return result;
	// }
	// private Object calculateStrokeColor(int generation)
	// {
	// float strokeSat = MAX_STROKE_SAT - generation*STEP_STROKE_SAT;
	// if (strokeSat < MIN_STROKE_SAT)
	// strokeSat = MIN_STROKE_SAT;
	// float strokeValue = MAX_STROKE_VALUE - generation*STEP_STROKE_VALUE;
	// if (strokeValue < MIN_STROKE_VALUE)
	// strokeValue = MIN_STROKE_VALUE;
	//
	// float strokeHue = this.strokeHue;
	//
	//
	// if ((strokeHue == MAGENTA) || (strokeHue == GREEN))
	// {
	// strokeSat -= .1f;
	// }
	// else if ((strokeHue == RED) ||
	// (strokeHue == BLUE_MAGENTA))
	// {
	// strokeSat -= .18f;
	// }
	// else if (strokeHue == BLUE)
	// {
	// strokeSat -= .25f;
	// }
	//
	// return Palette.hsvColor(strokeHue, strokeSat, strokeValue);
	// }

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
		this.strokeHue = strokeHue;
	}

	/**
	 * For use only by cm.state for restoring saved values.
	 */
	public void setFontIndex(int fontIndex)
	{
		this.fontIndex = fontIndex;
	}

	public String toString()
	{
		return "SemanticsSite: " + domain;
	}

}
