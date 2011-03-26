/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.ApplicationProperties;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericPrioritizedPool;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.PrefixCollection;
import ecologylab.collections.PrefixPhrase;
import ecologylab.collections.PrioritizedPool;
import ecologylab.collections.Scope;
import ecologylab.collections.WeightSet;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.Generic;
import ecologylab.generic.ReflectionTools;
import ecologylab.generic.ThreadMaster;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.old.InfoCollector;
import ecologylab.semantics.connectors.old.OldContainerI;
import ecologylab.semantics.documentparsers.DirectBindingParser;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.documentparsers.FeedParser;
import ecologylab.semantics.documentparsers.HTMLDOMImageTextParser;
import ecologylab.semantics.documentparsers.XPathParser;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.semantics.seeding.SemanticsPrefs;

/**
 * @author andruid
 *
 */
public class NewInfoCollector extends Debug
implements Observer, ThreadMaster, SemanticsPrefs, ApplicationProperties, DocumentParserTagNames, Runnable
{

	private static final int		MIN_DOWNLOAD_INTERVAL_PER_SITE		= 25;
	/**
	 * A constant for initializing our large HashTables.
	 */
	static final float								HASH_LOAD				= .5f;

	/**
	 * When the candidateContainersPool has more entries than this, it will be pruned.
	 */
	// static final int MAX_PAGES = 2048;
	static final int									MAX_PAGES				= 4096;																			// 3072;

	/**
	 * When the {@link #candidateTextSet candidateTextSet} and the {@link #candidateImgSet
	 * candidateImgSet} have more entries than this, they will be pruned.
	 */
	static final int									MAX_MEDIA				= 3072;

	public static final int			NUM_GENERATIONS_IN_CONTAINER_POOL = 5; 
	public static final int			MAX_PAGES_PER_GENERATION	= MAX_PAGES / NUM_GENERATIONS_IN_CONTAINER_POOL ;
	/**
	 * Contains 2 visual pools. The first holds the first image of each container
	 */
	private final GenericPrioritizedPool<ImageClipping> 						candidateImageClippingsPool;
	
	/**
	 * Contains 2 FloatWeightSet pools. 
	 * The first holds the first text surrogate of each container
	 */
	private final GenericPrioritizedPool<TextClipping> 	candidateTextElementsPool;
	
	private final PrioritizedPool<DocumentClosure>			candidateContainersPool;
	
	GuiBridge																						guiBridge;
	
	TNGGlobalCollections																globalCollections;
	
	MetaMetadataRepository															metaMetadataRepository;

	public NewInfoCollector(Scope sessionScope)
	{
		super();

		TermVector piv 	= InterestModel.getPIV(); 
		piv.addObserver(this);
		// Three pools for image visuals      
		VisualPool[] imgVisualPool = {	
				new VisualPool(this,new TermVectorWeightStrategy(piv)), 
				new VisualPool(this,new TermVectorWeightStrategy(piv)), 
				new VisualPool(this,new TermVectorWeightStrategy(piv))
		};
		//Similarly for text surrogates
		GenericWeightSet[] txtPool = { 
				new GenericWeightSet<TextClipping>(MAX_MEDIA, this, new TermVectorWeightStrategy(piv)),
				new GenericWeightSet<TextClipping>(MAX_MEDIA, this, new TermVectorWeightStrategy(piv)),
				new GenericWeightSet<TextClipping>(MAX_MEDIA, this, new TermVectorWeightStrategy(piv))
		};

		//We could do this within a loop, but it's better to have these initializations explicit here.
		
		WeightSet<DocumentClosure>[] containerWeightSets = new WeightSet[NUM_GENERATIONS_IN_CONTAINER_POOL];
		
		for(int i = 0; i < NUM_GENERATIONS_IN_CONTAINER_POOL; i++)
				containerWeightSets[i] = new WeightSet<DocumentClosure>(MAX_PAGES_PER_GENERATION, this, 
					(TermVectorWeightStrategy) new DownloadContainerWeightingStrategy(piv));
		
		//Instantiate all the Prioritized Pools
		candidateImageClippingsPool = new GenericPrioritizedPool<ImageClipping>(imgVisualPool);
		candidateTextElementsPool = new GenericPrioritizedPool<TextClipping>(txtPool);
		
		candidateContainersPool 	= new PrioritizedPool<DocumentClosure>(containerWeightSets);

		this.sessionScope = sessionScope;
		finished = false;
		usualSleep = 3000;
		longSleep = usualSleep * 5 / 2;

		crawlerDownloadMonitor.setHurry(true);
		println("");
		
		//TODO -- initialize MetaMetadataRepository
		
		globalCollections	= new TNGGlobalCollections(metaMetadataRepository);
	}
	
	public NewInfoCollector()
	{
		this(new Scope());
	}	
	
	InteractiveSpace														interactiveSpace;
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
	protected HashSet<String>	rejectDomains									= new HashSet<String>();

	/**
	 * A count of seeds whose downloading failed. Used during startup, to determine whether it is time
	 * to start the crawler. We make sure to download all seeds before giving resources to the
	 * crawler. This makes startup fairer; otherwise, the first recorded seeds get too much priority.
	 */
	protected int																badSeeds;

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

	private SeedSet														seedSet												= null;

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
	
	boolean																			crashed;

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
	protected static final DownloadMonitor			crawlerDownloadMonitor				= new DownloadMonitor<OldContainerI>("WebCrawler", NUM_CRAWLER_DOWNLOAD_THREADS, 0);

	static final int														NUM_SEEDING_DOWNLOAD_THREADS	= 4;

	/**
	 * This is the <code>DownloadMonitor</code> used by to process drag and drop operations. It gets
	 * especially high priority, in order to provide rapid response to the user.
	 */
	protected static final DownloadMonitor			dndDownloadMonitor						= new DownloadMonitor<OldContainerI>("Dnd", NUM_SEEDING_DOWNLOAD_THREADS, 6);

	/**
	 * This is the <code>DownloadMonitor</code> used by seeds. It never gets paused.
	 */
	protected static final DownloadMonitor			seedingDownloadMonitor				= new DownloadMonitor<OldContainerI>("Seeding", NUM_SEEDING_DOWNLOAD_THREADS, 1);


 	static final int			NUM_DOWNLOAD_THREADS	= 2;

	static final DownloadMonitor IMAGE_DOWNLOAD_MONITOR = //PixelBased.pixelBasedDownloadMonitor;
			new DownloadMonitor("Images", NUM_DOWNLOAD_THREADS, 1);

	public static final DownloadMonitor	highPriorityDownloadMonitor		=
		new DownloadMonitor("ImagesHighPriority", NUM_DOWNLOAD_THREADS, 3);


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
	public static final String 	SEMANTICS											= "semantics/";
	
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
		AssetsRoot mmAssetsRoot = new AssetsRoot(EnvironmentGeneric.configDir().getRelative(SEMANTICS), 
				ApplicationEnvironment.runningInEclipse() ? Files.newFile(EnvironmentGeneric.codeBase().file(), "../ecologylabSemantics/repository") : Files.newFile(PropertiesAndDirectories.thisApplicationDir(), SEMANTICS + "/repository"));

		METAMETADATA_REPOSITORY_DIR_FILE 	= Assets.getAsset(mmAssetsRoot, null, "repository", null, !USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
		println("\t\t-- Reading meta_metadata from " + METAMETADATA_REPOSITORY_DIR_FILE);

		META_METADATA_REPOSITORY 					= MetaMetadataRepository.load(METAMETADATA_REPOSITORY_DIR_FILE);
		
		DOCUMENT_META_METADATA						= META_METADATA_REPOSITORY.getByTagName(DOCUMENT_TAG);
		PDF_META_METADATA									= META_METADATA_REPOSITORY.getByTagName(PDF_TAG);
		SEARCH_META_METADATA							= META_METADATA_REPOSITORY.getByTagName(SEARCH_TAG);
		IMAGE_META_METADATA								= META_METADATA_REPOSITORY.getByTagName(IMAGE_TAG);
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
			result = !rejectDomains.contains(domain);
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
		return rejectDomains;
	}

	protected boolean seedsArePending()
	{
		return seedingDownloadMonitor.toDownloadSize() > 0;
	}

	public int numSeeds()
	{
		return (seedSet == null) ? -1 : seedSet.size();
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

	
	
	public void displayStatus(String message)
	{
		if (guiBridge != null)
			guiBridge.displayStatus(message);
		else
			debug(message);
	}

  public void displayStatus(String message, int ticks)
  {
		if (guiBridge != null)
			guiBridge.displayStatus(message, ticks);
		else
			debug(message);
  }

	public void trackFirstSeedSet(SeedSet seedSet)
	{
		// TODO Auto-generated method stub
		
	}

	public void setPlayOnStart(boolean b)
	{
		//TODO
	}

	public void clear()
	{
		// TODO Auto-generated method stub
		
	}


	public void setCurrentFileFromUntitled(File file)
	{
		// TODO Auto-generated method stub
		
	}

	public void increaseNumImageReferences()
	{
		// TODO Auto-generated method stub
		
	}

	public void decreaseNumImageReferences()
	{
		// TODO Auto-generated method stub
		
	}

	public void setHeterogeneousSearchScenario(boolean b)
	{
		this.isHeterogeneousSearchScenario	= b;
	}


	public DocumentClosure getContainerForSearch(DocumentClosure ancestor, Document metadata, ParsedURL purl, Seed seed,
			boolean reincarnate)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void addCandidateTextClipping(TextClipping textElement)
	{
		// TODO Auto-generated method stub
		
	}

	
	public boolean addCandidateClosure(DocumentClosure candidate)
	{
		if (candidate != null)
		{	
			synchronized(candidate)
			{
				if(!(	candidate.downloadHasBeenQueued() ||
							candidate.recycled() || candidate.isRecycling() ||
							candidate.isSeed())
					)
				{
					Document document	= candidate.getDocument();
					if (!exceedsLinkCountThresholds(document))
					{
						int generation 		= document.getEffectiveGeneration();
						int maxPoolNum	= candidateContainersPool.numWeightSets() - 1;
						if (generation > maxPoolNum)
							generation		= maxPoolNum;
						//Very temporary console noise. Please tell me to remove this, if i haven't already
						 //currentThread = Thread.currentThread();
						// Thread.dumpStack();
						
						debugT("---\t---\t---\tAdding Container to candidateContainersPool: " + candidate 
								/* +  " ancestor=[" + candidate.ancestor() + "]" */);
						
						
						candidateContainersPool.insert(candidate, generation);
						return true;
					}
				}
			}
		}
		return false;
	}
	

	public void removeCandidateContainer(DocumentClosure candidate)
	{
		if (candidate != null && !candidate.downloadHasBeenQueued() )
		{
			candidateContainersPool.remove(candidate);
		}
	}

	/**
	 * Determines whether a given <code>Container</code> exceeds the <code>linkCount</code> thresholds.
	 * Crawling too many links without seeing that the user is interested tends to lead to noisy content.
	 * <p/>
	 * Current thresholds are as follows:
	 * <ul>
	 * <li><code>linkCount</code> of 2 if link is to a new domain</li>
	 * <li><code>linkCount</code> of 4 if link is to the same domain</li>
	 * </ul>
	 * These thresholds are overridden if the user has expressed interest in surrogates from this particular <code>Container</code>.
	 *
	 * @param		document		the <code>Container</code> to evaluate for thresholds
	 * @return				<code>true</code> if the element's <code>linkCount</code> exceeds thresholds
	 */
	public boolean exceedsLinkCountThresholds(Document document)
	{
//		debug("---------exceedsLinkCountThresholds---------");

		int					linkCount			= document.getGeneration();
		boolean sameDomainAsPrevious 				= document.isSameDomainAsPrevious();
		short participantInterestIntensity 		= InterestModel.getInterestExpressedInTermVector(document.termVector());

		if (linkCount <= 2)
		{
//			debug("---ACCEPT1: intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
			return false;
		}
		else if (linkCount <= 4 && sameDomainAsPrevious)
		{
//			debug("---ACCEPT2: intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
			return false;
		}
		else if (participantInterestIntensity > 0)					// TODO: make sure participant interest is being kept up
		{
//			debug("---ACCEPT3: intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
			return false;
		}
		else
		{
			//debug("--- exceedsLinkCountThresholds() REJECT : intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
//			System.out.println("Ancestors Interest = " + element.ancestor().participantInterest().intensity() );
			return true;
		}
	}


	public void update(Observable o, Object arg)
	{
		checkCandidateAncestorsForBetterOutlinks();
		checkCandidatesForBetterImagesAndText();
	}
	
	/**
	 * 
	 */
	private void checkCandidateAncestorsForBetterOutlinks()
	{
		
		synchronized(candidateContainersPool)
		{
			WeightSet<DocumentClosure>[] candidateSet = (WeightSet<DocumentClosure>[]) candidateContainersPool.getWeightSets();
			for(WeightSet<DocumentClosure> candidates : candidateSet)
			{
				int size = candidates.size();
				ArrayList<DocumentClosure> removeContainers = new ArrayList<DocumentClosure>(size);
				ArrayList<DocumentClosure> insertContainers = new ArrayList<DocumentClosure>(size);
				for (DocumentClosure c : candidates)
				{
					DocumentClosure ancestor	= c.getAncestorClosure();
					if (ancestor != null && !(ancestor.recycled() || ancestor.isRecycling()))
					{
						Document ancestorDocument = ancestor.getDocument();
						if (ancestorDocument != null)
						{
							DocumentClosure d = ancestorDocument.swapNextBestOutlinkWith(c);
							if (d != null && c != d)
							{
								removeContainers.add(c);
								insertContainers.add(d); 
							}
						}
					}
				}
				int swapSize = insertContainers.size();
				if(swapSize > 0)
					System.out.println("Swapping Containers:\n\tReplacing " + swapSize);
				for (DocumentClosure c : removeContainers)
					candidates.remove(c);
				// Insertion directly into weighset is Ok, 
				// because the replacement container will have the same generation
				for (DocumentClosure c : insertContainers)
					candidates.insert(c);	
			}
		}
	}
	
	/**
	 * Replace images in the candidates with possible better ones from their containers.
	 */
	private void checkCandidatesForBetterImagesAndText()
	{
		synchronized (candidateImageClippingsPool)
		{
			ArrayList<GenericElement<ImageClipping>> vlist = new ArrayList<GenericElement<ImageClipping>>(candidateImageClippingsPool.size());
			for (GenericWeightSet<ImageClipping> ws : (GenericWeightSet<ImageClipping>[])candidateImageClippingsPool.getWeightSets())
				for (GenericElement<ImageClipping> geImageClipping : ws)
					vlist.add(geImageClipping);
			for (GenericElement<ImageClipping> geImageClipping : vlist)
			{
				ImageClipping imageClipping	= geImageClipping.getGeneric();
				Document sourceDocument		= imageClipping.getSource();
				if (sourceDocument != null)
					sourceDocument.tryToGetBetterImagesAfterInterestExpression(geImageClipping);
			}
		}
		synchronized (candidateTextElementsPool)
		{
			ArrayList<GenericElement<TextClipping>> tlist = new ArrayList<GenericElement<TextClipping>>(candidateTextElementsPool.size());
			for (GenericWeightSet<TextClipping> ws : (GenericWeightSet<TextClipping>[]) candidateTextElementsPool.getWeightSets())
				for (GenericElement<TextClipping> i : ws)
					tlist.add(i);
			for (GenericElement<TextClipping> i : tlist)
			{
				TextClipping textClipping	= i.getGeneric();
				Document sourceDocument		= textClipping.getSource();
				if (sourceDocument != null)
					sourceDocument.tryToGetBetterTextAfterInterestExpression(i);
			}
		}
	}

	public void removeTextClippingFromPools(GenericElement<TextClipping> replaceMe)
	{
		candidateTextElementsPool.remove(replaceMe);
	}

	public void removeImageClippingFromPools(GenericElement<ImageClipping> replaceMe)
	{
		candidateImageClippingsPool.remove(replaceMe);
	}

	/**
	 * The number of potential candidate <code>MediaElement</code>s that
	 * could be displayed. Includes those not yet and those already downloaded.
	 */
	public int countPotentialMediaToDisplay()
	{
		return  candidateTextElementsSetSize() + numberOfImageReferences() +  imagePoolsSize();
	}

	/**
	 * Number of display-able <code>ImgElement</code>s that could be displayed.
	 * @param useCached 
	 */
	public final int imagePoolsSize()
	{
		return candidateImageClippingsPool.size();
//		return candidateFirstImageVisualsPool.size() + candidateImageVisualsPool.size();
	}
	
	/**
	 * Number of images references.
	 */
	private int numImageReferences = 0;

	
	/**
	 * Number of candidate <code>ImgElement</code>s that could be downloaded.
	 */
	public final int numberOfImageReferences()
	{
		return numImageReferences;
	}

	/**
	 * Number of candidate <code>TextElement</code>s that could be displayed.
	 */
	public final int candidateTextElementsSetSize()
	{
		return candidateTextElementsPool.size();
//		return candidateFirstTextElementsSet.size() + candidateTextElementsSet.size();
	}

	public static final int ALMOST_EMPTY_CANDIDATES_SET_THRESHOLD	= 5;
	
	public boolean candidateTextElementsSetIsAlmostEmpty()
	{
		return candidateTextElementsPool.size() <= ALMOST_EMPTY_CANDIDATES_SET_THRESHOLD;
	}
	/**
	 * 
	 * @return	Weighted mean of the members of the candidateFirstTextElementsSet and the candidateTextElementsSet.
	 */
	public float candidateTextElementsSetsMean()
	{
		return candidateTextElementsPool.mean();
	}

	public float imagePoolsMean()
	{
		return candidateImageClippingsPool.mean();
	}


	
	/**
	 * Message to the user when the crawler stops because there's no place
	 * left to crawl to.
	 */
	static final String TRAVERSAL_COMPLETE_MSG =
		"The web crawler is pausing:\ntraversal of the information space is complete.\nThere are no more traversable pages to crawl to.";
	
	boolean threadsArePaused;
	boolean collectingThreadsPaused;   
	/**
	 * Pause all the threads we know about.
	 * 
	 * @return	true if threads exist and were not paused.
	 * 			Enables paused-ness state to be restored.
	 */
	public boolean pauseThreads()
	{
		boolean needToPause	= (thread != null) && !threadsArePaused;
		if (needToPause)
		{
			threadsArePaused	= true;
			//debug("Container.pauseThreads()");
			if (interactiveSpace != null)
				interactiveSpace.pauseIfPlaying();
			pauseThreadsExceptCompositionAgent();
			if (interactiveSpace != null)
				interactiveSpace.waitIfPlaying();
		}
		return needToPause;
	}

	boolean	threadsExceptCompositionArePause;
	/**
	 * 
	 */
	public boolean pauseThreadsExceptCompositionAgent()
	{
		boolean needToPause	= (thread != null) && !threadsExceptCompositionArePause;
		if (needToPause)
		{
			pause();
			if (interactiveSpace != null)
				interactiveSpace.pausePipeline();
			candidateImageClippingsPool.pause();
			crawlerDownloadMonitor.pause();
			IMAGE_DOWNLOAD_MONITOR.pause();
			threadsExceptCompositionArePause = true;
		}
		return needToPause;
	}
	public void pauseCollectingThreads()
	{
		if ((thread != null) && !collectingThreadsPaused)
		{
			collectingThreadsPaused	= true;
			candidateImageClippingsPool.pause();
			pause();
		}
	}   
	public void unpauseCollectingThreads()
	{
		if (collectingThreadsPaused)
		{
			collectingThreadsPaused	= false;
			candidateImageClippingsPool.unpause();
			unpause();
		}
	}
	
	public String getThreadStatuses()
	{
		return "ThreadsPaused = AllThreads("+ threadsArePaused +  ") " +
										"Collect(" + collectingThreadsPaused + ") " + 
										"NonComposition(" + threadsExceptCompositionArePause + ")";
	}
	/**
	 * Unpause (continue) all the threads we know about.
	 */
	public void unpauseThreads()
	{
		if (threadsArePaused)
		{
			threadsArePaused	= false;
			//debug("Container.unpauseThreads() crawlerDownloadMonitor waitingToDownload="+
			//      crawlerDownloadMonitor.waitingToDownload());
			
			if (interactiveSpace != null)
			{
				interactiveSpace.restorePlayIfWasPlaying();
				interactiveSpace.unpausePipeline();
			}
			
			unpauseNonCompositionThreads();
		}
		else if(threadsExceptCompositionArePause)
		{
			unpauseNonCompositionThreads();
		}
	}

	private void unpauseNonCompositionThreads()
	{
		threadsExceptCompositionArePause = false;

		candidateImageClippingsPool.unpause();
		crawlerDownloadMonitor.unpause();
		NewInfoCollector.IMAGE_DOWNLOAD_MONITOR.unpause();
		unpause();
	}
	public void stop()
	{
		stop(false);
	}
	public void stop(boolean kill)
	{
		if (!finished)
			finished	= true;

		stopCollectingAgents(kill);
		dndDownloadMonitor.stop(kill);

		IMAGE_DOWNLOAD_MONITOR.stop(kill);
		
		highPriorityDownloadMonitor.stop(kill);
		// clear all the collections when the CF browser exits -- eunyee
		//ThreadDebugger.clear();
		clearGlobalCollections();
	}
	
	public void clearGlobalCollections()
	{
		//TODO
	}

	/**
	 * Stop the threads that are responsible for collecting new surrogates.
	 * Includes the candidateImageVisualsPool, crawler and seeding download monitors.
	 * @param kill
	 */
	public void stopCollectingAgents(boolean kill)
	{
		candidateImageClippingsPool.stop();

//		candidateImageVisualsPool.stop();
//		candidateFirstImageVisualsPool.stop();
		crawlerDownloadMonitor.stop(kill);
		seedingDownloadMonitor.stop(kill); // stop seeding downloadMonitor
	}
	final Object startCrawlerSemaphore	= new Object();
	//FIXME
	public synchronized void start()
	{
		if (downloadLinksAutomatically.value())
		{
			if (!crashed && (thread == null) && !duringSeeding)
			{
				debug("Starting up.");
				//	 Thread.dumpStack();
				finished	= false;
				thread = new Thread(this, "InfoCollector");
				//ThreadDebugger.registerMyself(thread);	
				Generic.setPriority(PRIORITY);
				thread.start();
			}
			else
			{
				unpause();
			}
		}
	}

	public synchronized void pause()
	{
		if (thread != null)
		{
			debug("pause()");
			running = false;
		}
	}

	public synchronized void unpause()
	{
//		if ((thread != null) && !running && downloadLinksAutomatically)
		if ((thread != null) && downloadLinksAutomatically.value() && !duringSeeding)
		{
			debug("unpause()");
			running	= true;
			notifyAll();
		}
	}
	public boolean isOn()
	{
		return running && downloadLinksAutomatically.value();
	}

	/**
	 * Crawler
	 */
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		
	}

  public int getAppropriateFontIndex()
  {
  	return (guiBridge != null) ? guiBridge.getAppropriateFontIndex() : -1;
  }
  

}
