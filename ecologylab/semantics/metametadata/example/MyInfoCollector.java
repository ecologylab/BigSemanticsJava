/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;

/**
 * This is the InfoCollector class for this example.
 * 
 * An InfoCollector initiates the seeding process, holds containers and manages the downloading
 * process (e.g. through DownloadMonitor as here). It also provides methods to add / remove
 * listeners for the containers.
 * 
 * Note that we use DownloadMonitor (from ecologylabFundamental) to manage the downloading process.
 * It provides multi-thread downloading in a easy-to-use way. However, you should remember to set
 * the VM arguments to allocate enough memory for it, or it can't start working (you'll see console
 * output like "Memory.reclaim...").
 * 
 * Also, we don't implement all the methods from the interface InfoCollector.
 * 
 * @author quyin
 * 
 */
public class MyInfoCollector implements InfoCollector<MyContainer>
{
	//how many threads for downloads - how many downloads to allow concurrently
	public final static int					DEFAULT_COUNT_DOWNLOAD_THREAD	= 1;

	//reference to mmd repso
	private MetaMetadataRepository	mmdRepo;

	//stores the infocollects list of rejected domains
	private Set<String>							rejectDomains;

	private DownloadMonitor					downloadMonitor;

	/**
	 * @return the downloadMonitor.
	 */
	public DownloadMonitor getDownloadMonitor()
	{
		return downloadMonitor;
	}
	// to change to single listener for all metadata
	private ArrayList<MyContainer.MetadataCollectingListener>	collectingListeners;

	/**
	 * Add a listener for all containers.
	 * 
	 * @param listener
	 *          The listener you want to add.
	 */
	public void addListener(MyContainer.MetadataCollectingListener listener)
	{
		collectingListeners.add(listener);
	}

	/**
	 * Remove a listener from all containers.
	 * 
	 * @param listener
	 *          The listener you want to remove.
	 */
	public void removeListener(MyContainer.MetadataCollectingListener listener)
	{
		collectingListeners.remove(listener);
	}

	/**
	 * Get all the listeners.
	 * 
	 * @return An ArrayList containing all the listeners.
	 */
	public ArrayList<MyContainer.MetadataCollectingListener> getListeners()
	{
		return collectingListeners;
	}
	
	//load repository file and stuff
	public MyInfoCollector(String repoFilepath, TranslationScope metaMetadataTScope, TranslationScope metadataTScope)
	{
		mmdRepo = MetaMetadataRepository.load(new File(repoFilepath), metaMetadataTScope);
		mmdRepo.initializeRepository(metadataTScope);
		rejectDomains = new HashSet<String>();
		downloadMonitor = new DownloadMonitor("info-collector_download-monitor",
				DEFAULT_COUNT_DOWNLOAD_THREAD);
		collectingListeners = new ArrayList<MyContainer.MetadataCollectingListener>();
	}
	
	public MyInfoCollector(String repoFilepath)
	{
		this(repoFilepath, MetaMetadataRepository.META_METADATA_TSCOPE, GeneratedMetadataTranslationScope.get());
	}

	/**
	 * Test if a URL is acceptable.
	 * 
	 */
	@Override
	public boolean accept(ParsedURL connectionPURL)
	{
		String domain = connectionPURL.domain();
		if (rejectDomains.contains(domain))
			return false;
		return true;
	}

	@Override
	public void beginSeeding()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Construct the appropriate Document object from the meta-metadata repository, given a URL. It
	 * compares the URL with URL patterns in the repository, retrieve the appropriate meta-metadata
	 * and metadata objects.
	 */
	@Override
	public Document constructDocument(ParsedURL purl)
	{
		return mmdRepo.constructDocument(purl);
	}

	@Override
	public DocumentParser<MyContainer, ? extends InfoCollector, ?> constructDocumentType(
			ElementState inlineDoc)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeedPeer constructSeedPeer(Seed seed)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SemanticActionHandler createSemanticActionHandler()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void decreaseNumImageReferences()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void displayStatus(String message)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void endSeeding()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Create a MyContainer object for a given URL, and add listeners for it.
	 */
	@Override
	public MyContainer getContainer(MyContainer ancestor, ParsedURL purl, boolean reincarnate,
			boolean addToCandidatesIfNeeded, MetaMetadata metaMetadata)
	{
		if (!accept(purl))
			return null;

		MyContainer result = new MyContainer(ancestor, this, purl);
		result.setCollectingListeners(collectingListeners);
		return result;
	}

	/**
	 * Add a MyContainer object to the to-be-downloaded queue of the DownloadMonitor. The
	 * DownloadMonitor will decide when the container will be actually downloaded and processed.
	 */
	@Override
	public MyContainer getContainerDownloadIfNeeded(MyContainer ancestor, ParsedURL purl, Seed seed,
			boolean dnd, boolean justCrawl, boolean justMedia)
	{
		MyContainer result = getContainer(ancestor, purl, false, false, null);
		downloadMonitor.download(result, null);
		return result;
	}

	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor, ParsedURL purl, Seed seed)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor, ParsedURL purl, Seed seed,
			MetaMetadata metaMetadata)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaMetadata getDocumentMM(ParsedURL purl, String tagName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaMetadata getDocumentMM(ParsedURL purl)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaMetadata getImageMM(ParsedURL purl)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JFrame getJFrame()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends InfoCollector>[] getMyClassArg()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeedDistributor getResultDistributer()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeedSet getSeedSet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object globalCollectionContainersLock()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void increaseNumImageReferences()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public TranslationScope inlineDocumentTranslations()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void instantiateDocumentType(Scope registry, String key, SearchState searchState)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public MyContainer lookupAbstractContainer(ParsedURL connectionPURL)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mapContainerToPURL(ParsedURL purl, MyContainer container)
	{
		// TODO Auto-generated method stub

	}

	// new added mmdRepo
	@Override
	public MetaMetadataRepository metaMetaDataRepository()
	{
		// TODO Auto-generated method stub
		return mmdRepo;
	}

	// new add return null
	@Override
	public DocumentParser<MyContainer, ? extends InfoCollector, ?> newFileDirectoryType(File file)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/** New 
	 * Reject all the URLs from a domain from now on.
	 */
	@Override
	public void reject(String domain)
	{
		if (domain != null)
		{
			rejectDomains.add(domain);
		}
	}

	@Override
	public void removeCandidateContainer(MyContainer candidate)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Scope sessionScope()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCurrentFileFromUntitled(File file)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlayOnStart(boolean b)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trackFirstSeedSet(SeedSet seedSet)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void traversable(ParsedURL url)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void untraversable(ParsedURL url)
	{
		// TODO Auto-generated method stub

	}
}
