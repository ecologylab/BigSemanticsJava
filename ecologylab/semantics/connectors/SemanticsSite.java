/**
 * 
 */
package ecologylab.semantics.connectors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import ecologylab.generic.HashMapWriteSynch2Args;
import ecologylab.generic.ValueFactory2;
import ecologylab.io.BasicSite;
import ecologylab.net.ParsedURL;
import ecologylab.xml.xml_inherit;

/**
 * BasicSite extended to maintain semantics 
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class SemanticsSite extends BasicSite
{

	static HashMapWriteSynch2Args<String, SemanticsSite, InfoCollector>	allSites	= new HashMapWriteSynch2Args<String, SemanticsSite, InfoCollector>(50);

	/**
	 * true if any Container from this Site is a Seed.
	 */
	private boolean	isSeed;

	protected static final int 	MAX_GENERATIONS							= 15;

	static final double MAX_SURROGATES_FROM_SITE = 25.0;
	
	static final double NON_SEED_FACTOR						= .25;
	
	/**
	 * For counting images that function visually, not spacers, headers,
	 * and nav.
	 */
	protected int			numImgs = 0;
	protected int			numText = 0;

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
	
	private static final String 	ROUTER_PREFIX = "http://csdll.cs.tamu.edu:9080";
	//TODO un-hard code the 48 in the next line!
	private static final int 	ROUTER_PREFIX_OFFSET = 48;


	/**
	 * 
	 * @param domain
	 */
	public SemanticsSite(String domain)
	{
		this.domain = domain;
	}
	
	/**
	 * DO NOT USE. meant only for XML Exception
	 */
	public SemanticsSite()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Is left uninitialized, because we want to store subclasses of SemanticSite (CfSite)
	 *  
	 */
	static  ValueFactory2 siteValueFactory;//	 = new ValueFactory2<String, SemanticSite, InfoCollector>()
//	{
//		public SemanticSite createValue(String domain, InfoCollector infoCollector)
//		{
//			return new SemanticSite(domain);
//		}
//	};
	
	
	public static SemanticsSite getSite(ParsedURL purl, InfoCollector infoCollector)
	{
		return getSite(purl.domain(), infoCollector);
	}

	public static SemanticsSite getSite(Container container, InfoCollector infoCollector)
	{
		ParsedURL parsedURL	= container.purl();

		SemanticsSite result	= null;

		if (parsedURL != null)
		{
			if( parsedURL.toString().startsWith(ROUTER_PREFIX))
			{
				try
				{
					// hack for user studies that use the router
					parsedURL = new ParsedURL(new URL("http://"+parsedURL.toString().substring(ROUTER_PREFIX_OFFSET)));
				} catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
			String domain = parsedURL.domain();   	
			if( domain == null )
			{
			}
			else
			{
				result		= getSite(domain, infoCollector);
				if (container.isSeed())
					result.isSeed	= true;
				
				result.addToIndex(container);
			}
		}
		return result;
	}
	
	/**
	 * @param domain    a String already parsed by StringTools. Must not be null.
	 * @param infoCollector TODO
	 * @return a <code>Site</code>, either a new one, or a matching
	 *    existing one.
	 */
	public static SemanticsSite getSite(String domain, InfoCollector infoCollector)
	{
		if (domain == null)
		{
			println("domain is null ?? ");
			return null;
		}
		else
		{
			SemanticsSite result	= allSites.getOrCreateAndPutIfNew(domain, infoCollector, siteValueFactory);
			return result;
		}
	}
	

	/**
	 * Set the mechanism for instantiating new Site objects, in case you want to use a subclass.
	 * 
	 * @param siteValueFactory
	 */
	public static void setSiteValueFactory(ValueFactory2<String, ? extends SemanticsSite, InfoCollector> siteValueFactory)
	{
		SemanticsSite.siteValueFactory = siteValueFactory;
	}
	
	public void enteringComposition()
	{
		numElementsInComposition++;
	}

	public void exitingComposition()
	{
		numElementsInComposition--;
	}
	
	/**
	 * Insert a Container into the inverted index. This maintains a list of sites, and their 
	 * associated containers. 
	 * 
	 * @param container
	 */
	protected void addToIndex(Container container)
	{
	}

	/**
	 * Remove Container from inverted index, except this doesn't keep one, so do nothing.
	 * 
	 * @param container
	 */
	public void removeFromIndex(Container container)
	{
	}

	public String domain()
	{
		return domain;
	}

	/**
	 * For use only by cm.state for restoring saved values.
	 */
	public void setDomain(String domain)
	{
		this.domain	= domain;
	}

	/**
	 * removes itself from the allSites hash so it can be recreated if needs be
	 */
	protected void removeFromGlobal()
	{
		allSites.remove(this.domain());
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
	
	/* return number of elements per site */
	public int numElements()
	{
		return (numImgs+numText);
	}
	public int numImgs()
	{
		return numImgs;
	}
	public int numTexts()
	{
		return numText;
	}
	
	public static void addMapToSites(HashMap<String, ? extends SemanticsSite> map)
	{
		allSites.putAll(map);
	}
}
