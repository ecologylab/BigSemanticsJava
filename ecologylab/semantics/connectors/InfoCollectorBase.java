/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.ApplicationProperties;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.collections.PrefixCollection;
import ecologylab.collections.PrefixPhrase;
import ecologylab.collections.Scope;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapWriteSynch;
import ecologylab.generic.StringTools;
import ecologylab.io.Assets;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.distributed.common.SessionObjects;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.semantics.seeding.SemanticsPrefs;

/**
 * @author amathur
 * 
 */
public abstract class InfoCollectorBase<AC extends Container> extends	Debug 
implements InfoCollector<AC>, SemanticsPrefs, ApplicationProperties, DocumentParserTagNames
{

	/**
	 * Bias for ads: if filter matches a url to an ad (for HTMLPages or MediaElements), multiply bias
	 * by this number. Special values: 0 reject ads (eg, filter matches), altogether
	 */
	protected float															adsBias;

	/**
	 * The number of loops through the web crawler. A performance statistic that roughly corresponds
	 * to how many new <code>Container</code>s have been queued for download and parse, during this
	 * session.
	 */
	protected int																count;

	/**
	 * Hashtable of domains the information space author doesn't want any information elements from.
	 */
	protected HashMapWriteSynch<String, String>	rejectDomains									= new HashMapWriteSynch<String, String>();

	/**
	 * A count of seeds whose downloading failed. Used during startup, to determine whether it is time
	 * to start the crawler. We make sure to download all seeds before giving resources to the
	 * crawler. This makes startup fairer; otherwise, the first recorded seeds get too much priority.
	 */
	protected int																badSeeds;

	/**
	 * Set of expected seed download <code>Container</code>s).
	 */
	protected Vector														seeds;

	/**
	 * Priority to run at when we seem to have an overabundance of <code>MediaElement</code>s ready
	 * for display.
	 */
	protected static final int									LO_PRIORITY										= 3;

	/**
	 * Priority to run at normally.
	 */
	protected static final int									MID_PRIORITY									= 4;

	/**
	 * Priority to run at when we do not have enough <code>MediaElement</code>s ready for display.
	 */
	protected static final int									HI_PRIORITY										= 5;

	/**
	 * Initial priority, which is {@link #LO_PRIORITY LO_PRIORITY}, because we expect to need to do a
	 * bunch of crawling at the start, since our global collections will be empty.
	 */
	protected static final int									PRIORITY											= MID_PRIORITY;

	/**
	 * Controls whether or not we periodically automatically download the Containers associated with
	 * hrefs that have been discovered. In other words, controls whether or not we crawl at all. Set
	 * as a preference at runtime, and via a menu entry.
	 */
	protected PrefBoolean												downloadLinksAutomatically		= CRAWL;

	// +++++++++++ state for thread +++++++++++ //
	/**
	 * The web crawler thread.
	 */
	protected Thread														thread;

	/**
	 * Address prefixes derived from original seeding, traversable| seeding specs, and server-side
	 * redirects. Defines the spanning set of basis vectors of the information space, when limit
	 * traversal (stay close) is true.
	 */
	// private Vector<String> traversableURLStrings = new Vector<String>();
	protected PrefixCollection									traversablePrefixes						= new PrefixCollection();

	/**
	 * Untraversable urls defined at seeding time. These supercede traversable specs.
	 */
	// Vector<String> untraversableURLStrings = new Vector<String>();
	protected PrefixCollection									untraversablePrefixes					= new PrefixCollection();

	protected SeedSet															seedSet												= null;

	private final Scope													sessionScope;
	
	protected static File												METAMETADATA_REPOSITORY_DIR_FILE;
	protected static File												METAMETADATA_SITES_FILE;
		
	protected ArrayList<DownloadMonitor>				downloadMonitors							= new ArrayList<DownloadMonitor>();

	/**
	 * true when the session is ending.
	 */
	protected boolean														finished;

	/**
	 * Controls whether or not the crawler is (temporarily) paused.
	 */
	protected boolean														running												= true;

	/**
	 * Web crawler sleep time when we are in need of collecting more media.
	 */
	protected final int													usualSleep;

	/**
	 * Web crawler sleep time when there seems to be plenty of media already collected.
	 */
	protected final int													longSleep;

	static final int														NUM_CRAWLER_DOWNLOAD_THREADS	= 2;

	/**
	 * This <code>DownloadMonitor</code> is used by the web crawler (i.e.,
	 * {@link CfInfoCollector#run() ContentIntegrator.run()}).
	 * 
	 * It sometimes gets paused by GoogleSearch to promote downloading of search results.
	 */
	protected static final DownloadMonitor			crawlerDownloadMonitor				= new DownloadMonitor<Container>("WebCrawler", NUM_CRAWLER_DOWNLOAD_THREADS, 0);

	static final int														NUM_SEEDING_DOWNLOAD_THREADS	= 4;

	/**
	 * This is the <code>DownloadMonitor</code> used by to process drag and drop operations. It gets
	 * especially high priority, in order to provide rapid response to the user.
	 */
	protected static final DownloadMonitor			dndDownloadMonitor						= new DownloadMonitor<Container>("Dnd", InfoCollectorBase.NUM_SEEDING_DOWNLOAD_THREADS, 6);

	/**
	 * This is the <code>DownloadMonitor</code> used by seeds. It never gets paused.
	 */
	protected static final DownloadMonitor			seedingDownloadMonitor				= new DownloadMonitor<Container>("Seeding", InfoCollectorBase.NUM_SEEDING_DOWNLOAD_THREADS, 1);

	//
	static final Object													SEEDING_STATE_LOCK = new Object();

	protected boolean														duringSeeding;

	// ++++++++++++++++++++++++++++++++++++++++ //
	
	static final File	LOCAL_META_METADATA_DIR_FILE		= new File(EnvironmentGeneric.codeBase().file(), "../ecologylabSemantics/");
	
	static final String	META_METADATA_REPOSITORY_DIR		= "repository";


	/**
	 * This is the xml file defining ALL the metaMetadata required
	 * It resides in the ecologylabSemantics project.
	 */
	public static final String	METAMETADATA_REPOSITORY				= "repository";

	/**
	 * 
	 * The repository has the metaMetadatas of the document types. The repository is populated as the
	 * documents are processed.
	 */
	protected static final MetaMetadataRepository		META_METADATA_REPOSITORY;
	
	public static final MetaMetadata								DOCUMENT_META_METADATA;
	public static final MetaMetadata								PDF_META_METADATA;
	public static final MetaMetadata								SEARCH_META_METADATA;
	public static final MetaMetadata								IMAGE_META_METADATA;

	public boolean 			isHeterogeneousSearchScenario = true;
	
	static
	{
		// MetaMetadata repository file has all the metametadata needed for metadata collection, 
		// retrieval and incontextMetadata display.
		Assets.downloadSemanticsZip(METAMETADATA_REPOSITORY, null, !USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
	
		if(ApplicationEnvironment.runningInEclipse() && Pref.lookupBoolean(USE_LOCAL_CF_PREF_NAME)) //default is set to false in the metaprefs.
		{
			println("\t\t-- Reading meta_metadata from : " + LOCAL_META_METADATA_DIR_FILE);
			METAMETADATA_REPOSITORY_DIR_FILE 		= new File(LOCAL_META_METADATA_DIR_FILE, META_METADATA_REPOSITORY_DIR);			
			
		}
		else
		{
			println("\t\t-- Reading meta_metadata from zip");
			METAMETADATA_REPOSITORY_DIR_FILE 	= Assets.getSemanticsFile(META_METADATA_REPOSITORY_DIR);
		}
		META_METADATA_REPOSITORY = MetaMetadataRepository.load(METAMETADATA_REPOSITORY_DIR_FILE);
		
		DOCUMENT_META_METADATA						= META_METADATA_REPOSITORY.getByTagName(DOCUMENT_TAG);
		PDF_META_METADATA							= META_METADATA_REPOSITORY.getByTagName(PDF_TAG);
		SEARCH_META_METADATA						= META_METADATA_REPOSITORY.getByTagName(SEARCH_TAG);
		IMAGE_META_METADATA							= META_METADATA_REPOSITORY.getByTagName(IMAGE_TAG);
	}

	public InfoCollectorBase(Scope sessionScope)
	{
		super();
		this.sessionScope = sessionScope;
		finished = false;
		usualSleep = 3000;
		longSleep = usualSleep * 5 / 2;

		crawlerDownloadMonitor.setHurry(true);
		println("");
	}

	public SeedDistributor getResultDistributer()
	{
		return seedSet.seedDistributer(this);
	}

	public SeedSet getSeedSet()
	{
		SeedSet result = this.seedSet;
		if (result == null)
		{
			result = new SeedSet();
			this.seedSet = result;
		}
		return result;
	}

	public void addSeeds(SeedSet<? extends Seed> newSeeds)
	{
		
		if (this.seedSet == null)
			this.seedSet = newSeeds;
		else
		{
			for (Seed seed: newSeeds)
			{
				this.seedSet.add(seed);
			}
		}
		int numSeeds = seedSet.size();
		
		//Test for hetero/homogeneity
		Iterator iterator = seedSet.iterator();
		while(iterator.hasNext())
		{
			Seed seed = (Seed) iterator.next();
			if(numSeeds == 1)
			{
				isHeterogeneousSearchScenario = ! seed.isHomogenousSeed();
				break;
			}
			else
			{
				//More than one seed.
				//Even if one isn't homogeneous, scenario is hetero
				if(! seed.isHomogenousSeed())
				{
					isHeterogeneousSearchScenario = true;
					break;
				}
			}
		}
		debugT("The current session scenario is " + (isHeterogeneousSearchScenario ? "Heterogeneous" : "Homogeneous"));
	}

	public void getMoreSeedResults()
	{
		if (seedSet != null)
		{
			System.out.println(this + ".getMoreSeedResults()!!! " + seedSet.getStartingResultNum());
			seedSet.performNextSeeding(sessionScope);
		}
		// for (ecologylab.oodss.messages.cf.Seed s : seedSet)
		// if (s instanceof SearchState)
		// {
		// SearchState searchState = (SearchState) s;
		// if(!searchState.recentlyCreated())
		// {
		// searchState.performNextSearch(this);
		// }
		// else
		// debug("Not hitting poor Search engine. Might do so later.");
		// }

	}

	/**
	 * @return Returns the Session Scope.
	 */
	public Scope sessionScope()
	{
		return sessionScope;
	}

	public MetaMetadataRepository metaMetaDataRepository()
	{
		return META_METADATA_REPOSITORY;
	}

	/**
	 * Lookup MetaMetadata using a String for the XML tag name.
	 * This can be performed generally both for media and for document meta-metadata.
	 * 
	 * @param tagName
	 * @return
	 */
	public MetaMetadata getMM(String tagName)
	{
		return META_METADATA_REPOSITORY.getByTagName(tagName);
	}

	/**
	 * Lookup MetaMetadata using a class object.
	 * This can be performed generally both for media and for document meta-metadata.
	 * 
	 * @param metadataClass
	 * @return
	 */
	public static MetaMetadata getMM(Class<? extends Metadata> metadataClass)
	{
		return META_METADATA_REPOSITORY.getMM(metadataClass);
	}

	/**
	 * Lookup Document MetaMetadata, first using the purl to seek something specific, and otherwise,
	 * 
	 */
	public MetaMetadata getDocumentMM(ParsedURL purl, String tagName)
	{
		return META_METADATA_REPOSITORY.getDocumentMM(purl, tagName);
	}
	public MetaMetadata getDocumentMM(ParsedURL purl)
	{
		return META_METADATA_REPOSITORY.getDocumentMM(purl);
	}

	/**
	 * Uses the location in the metadata, and then its type, to resolve MetaMetadata.
	 * 
	 * @param metadata
	 * @return
	 */
	public MetaMetadata getDocumentMM(Document metadata)
	{
		return META_METADATA_REPOSITORY.getDocumentMM(metadata);
	}

	/**
	 * Uses the location passed in, and then the metadata's type, to resolve MetaMetadata.
	 * 
	 * @param metadata
	 * @return
	 */
	public MetaMetadata getImageMM(ParsedURL purl)
	{
		return META_METADATA_REPOSITORY.getImageMM(purl);
	}

	
	/**
	 * Look-up MetaMetadata for this purl.
	 * If there is no special MetaMetadata, use Document.
	 * Construct Metadata of the correct subtype, base on the MetaMetadata.
	 * Set its location field to purl.
	 * 
	 * @param purl
	 * @return
	 */
	public Document constructDocument(ParsedURL purl)
	{
		return META_METADATA_REPOSITORY.constructDocument(purl);
	}
	
	public JFrame getJFrame()
	{
		return (JFrame) sessionScope().get(SessionObjects.TOP_LEVEL);
	}

	/**
	 * Add the directory that this URL references to the traversable set; that is, to the bounding set
	 * of path prefixes that we are willing to download from, given "limit traversal." This is called
	 * automatically, as well as through traversable|; thus it parses to the directory level, removing
	 * any filename portion of the URL.
	 */
	public void traversable(ParsedURL purl)
	{
		traversable(purl, false);
	}
	
	public void traversable(ParsedURL purl, boolean ignoreReject)
	{
		// String uniquePrefix = purl.directoryString();
		// debug("add traversable " +url +"->" +uniquePrefix);
		// println("-- allow downloads that start with " + uniquePrefix + " --");
		// traversable(uniquePrefix);
		// PrefixPhrase prefixPhrase = traversablePrefixes.add(purl);
		// StringBuilder buffy = new StringBuilder("-- allow downloads that start with ");
		// prefixPhrase.toStringBuilder(buffy, traversablePrefixes.separator());
		// buffy.append(" --");
		// println(buffy);
		if ((ignoreReject || this.isNotReject(purl)) &&
				!traversablePrefixes.match(purl)) //If this purl already exists in traversablePrefixes, don't add/
			recordPrefix(traversablePrefixes, purl, "-- allow downloads that start with ");
	}

	private void recordPrefix(PrefixCollection prefixCollection, ParsedURL purl, String message)
	{
		// String uniquePrefix = purl.directoryString();
		// debug("add traversable " +url +"->" +uniquePrefix);
		// println("-- allow downloads that start with " + uniquePrefix + " --");
		// traversable(uniquePrefix);
		PrefixPhrase prefixPhrase = prefixCollection.add(purl);
		StringBuilder buffy = new StringBuilder(message);
		prefixPhrase.toStringBuilder(buffy, traversablePrefixes.separator());
		buffy.append(" --");
		println(buffy);
	}

	public boolean isNotReject(ParsedURL purl)
	{
		String domain = purl.domain();
		boolean result = domain != null;
		if (result)
		{
			result = rejectDomains.get(domain) == null;
		}
		if (result)
		{
			result = !purl.isUnsupported();
		}
		if (!result)
			warning("Rejecting navigation to " + purl);

		return result;
	}

	/**
	 * Define the directory of the purl as a prefix that will not be crawled to, if limit_traveral is
	 * on.
	 */
	public void untraversable(ParsedURL purl)
	{
		// String uniquePrefix = purl.directoryString();
		// // debug("add traversable " +url +"->" +uniquePrefix);
		// if (!untraversableURLStrings.contains(uniquePrefix))
		// {
		// println("-- refusing downloads that start with " + uniquePrefix + " --");
		// untraversableURLStrings.addElement(uniquePrefix);
		// }
		recordPrefix(untraversablePrefixes, purl, "-- refuse downloads that start with ");
	}

	// ------------------- Thread related state handling ------------------- //
	//
	protected synchronized void waitIfNotRunning()
	{
		if (!running || !downloadLinksAutomatically.value())
		{
			try
			{
				debug("waitIfOff() waiting");
				wait();
				running = true;
			}
			catch (InterruptedException e)
			{
				debug("run(): wait interrupted: ");
				e.printStackTrace();
				Thread.interrupted(); // clear the interrupt
			}
		}
	}

	public void beginSeeding()
	{
		synchronized (SEEDING_STATE_LOCK)
		{
			if (!duringSeeding)
			{
				debug("beginSeeding() pause crawler");
				duringSeeding = true;
				crawlerDownloadMonitor.pause();
				pause();
			}
		}
	}

	/**
	 * Called when seeding is complete.
	 */
	public void endSeeding()
	{
		synchronized (SEEDING_STATE_LOCK)
		{
			if (duringSeeding)
			{
				duringSeeding = false;
				debug("endSeeding() unpause crawler");
				crawlerDownloadMonitor.unpause();
				start();	// start thread *or* unpause()
			}
		}
	}

	public DownloadMonitor getCrawlerDownloadMonitor()
	{
		return crawlerDownloadMonitor;
	}

	public static DownloadMonitor seedingDownloadMonitor()
	{
		return seedingDownloadMonitor;
	}

	public static DownloadMonitor dndDownloadMonitor()
	{
		return dndDownloadMonitor;
	}

	public DownloadMonitor getDndDownloadMonitor()
	{
		return dndDownloadMonitor;
	}

	public void pauseDownloadMonitor()
	{
		crawlerDownloadMonitor().pause();
	}

	public static DownloadMonitor crawlerDownloadMonitor()
	{
		return crawlerDownloadMonitor;
	}

	public void pauseCrawler()
	{
		crawlerDownloadMonitor.pause();
	}

	public DownloadMonitor getSeedingDownloadMonitor()
	{
		return seedingDownloadMonitor;
	}

	public Collection<String> traversableURLStrings()
	{
		return traversablePrefixes.values();
	}

	public Collection<String> untraversableURLStrings()
	{
		return untraversablePrefixes.values();
	}

	public Collection<String> rejectDomainsCollection()
	{
		return rejectDomains.keySet();
	}

	protected boolean seedsArePending()
	{
		return seedingDownloadMonitor.toDownloadSize() > 0;
	}

	public int numSeeds()
	{
		return (seeds == null) ? -1 : seeds.size();
	}

	// Accessors for InfoCollectorState
	// ///////////////////////////////////////////////////////
	public float adsBias()
	{
		return adsBias;
	}

	public int count()
	{
		return count;
	}

	/**
	 * Are we willing to accept this url into one of the agent's candidate pools?
	 *
	 * @return	-1 if this url is null or from a domain we're rejecting.
	 * 		0  if this url is null or from a domain we go justFollow for.
	 * 		1  if this url is null or from a domain we're accepting.
	 */
	public boolean accept(ParsedURL purl)
	{
		boolean result	= !(purl.url().getProtocol().equals("https://"));
		if (result)
		{
			result		= isNotReject(purl);
			if (result)
			{
				result	= !untraversablePrefixes.match(purl);
				if (result && LIMIT_TRAVERSAL.value())
				{
					result	= traversablePrefixes.match(purl);
				}
			}
		}
//		if (!result)
//			debug("accept() NOT " + purl);
		return result;
	}

	public boolean whileSeeding()
	{
		return duringSeeding || (seedingDownloadMonitor.size() > 3);
	}
	public void reject(ParsedURL purl)
	{
		if( purl != null )
		{
			reject(purl.noAnchorNoQueryPageString());
		}
	}
	public void reject(String siteAddr)
	{
		if (siteAddr != null)
		{
			String domain	= StringTools.domain(siteAddr);
			if (domain != null)
			{
				rejectDomains.put(domain, domain);
				println("-- rejecting all web addresses from domain "+domain+ " --");
			}
		}
	}
	
	abstract public void start();
	abstract public void pause();

	/**
	 * Check the Global Metadata collection to see if there is a resolved metadata object for the given purl.
	 * @param purl
	 * @return the metadata of the resolved entity
	 */
	public Metadata resolveEntity(ParsedURL purl)
	{
		return null;
	}
	
	@Override
	public SeedDistributor getSeedDistributor()
	{
		return getSeedSet().seedDistributer(this);
	}

}
