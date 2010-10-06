/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarScalarType;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.TranslationScope;

/**
 * This is the InfoCollector class for this example.
 * 
 * An InfoCollector initiates the seeding process, holds containers and manages the downloading
 * process (e.g. through DownloadMonitor as here).
 * 
 * Note that we use DownloadMonitor (from ecologylabFundamental) to manage the downloading process.
 * It provides multi-threaded downloading in an easy-to-use way. However, you should remember to set
 * the VM arguments to allocate enough memory for it, or it can't start working (you'll see console
 * output like "Memory.reclaim...").
 * 
 * Also, we don't implement all the methods from the interface InfoCollector, since we don't need
 * all of them.
 * 
 * @author quyin
 * 
 */
public class MyInfoCollector<C extends MyContainer> extends Debug implements InfoCollector<C>
{
	
	public final static int								DEFAULT_COUNT_DOWNLOAD_THREAD	= 1;

	// how many threads for downloads - how many downloads to allow concurrently
	private int														numDownloadThreads;

	// reference to mmd repso
	private MetaMetadataRepository				mmdRepo;

	private TranslationScope							metadataTranslationScope;

	private SemanticActionHandlerFactory	semanticActionHandlerFactory;

	// stores the infocollects list of rejected domains
	private Set<String>										rejectDomains;

	protected DownloadMonitor							downloadMonitor;

	private Logger												logger;

	static
	{
		// need to register scalar types BEFORE any translation scope is set up, or some scalar types
		// cannot be recognized.
		MetadataScalarScalarType.init();
	}

	public MyInfoCollector(MetaMetadataRepository repo, TranslationScope metadataTranslationScope)
	{
		this(repo, metadataTranslationScope, null, DEFAULT_COUNT_DOWNLOAD_THREAD);
	}

	/**
	 * This constructor also loads and initializes the {@link MetaMetadataRepository}, given a
	 * metadata {@link TranslationScope}. Typically this TranslationScope should have been generated
	 * by the {@link MetadataCompiler}, named as <i>GeneratedMetadataTranslationScope</i>.
	 * 
	 * @param repo
	 *          The metametadata repository.
	 * @param metadataTranslationScope
	 *          The metadata TranslationScope.
	 */
	public MyInfoCollector(MetaMetadataRepository repo, TranslationScope metadataTranslationScope,
			SemanticActionHandlerFactory semanticActionHandlerFactory, int numDownloadThreads)
	{
		this.mmdRepo = repo;
		this.metadataTranslationScope = metadataTranslationScope;
		this.semanticActionHandlerFactory = semanticActionHandlerFactory;
		this.numDownloadThreads = numDownloadThreads;

		mmdRepo.bindMetadataClassDescriptorsToMetaMetadata(metadataTranslationScope);

		rejectDomains = new HashSet<String>();
	}

	/**
	 * @return the downloadMonitor.
	 */
	public DownloadMonitor getDownloadMonitor()
	{
		if (downloadMonitor == null)
			downloadMonitor = new DownloadMonitor("my-info-collector", numDownloadThreads);
		return downloadMonitor;
	}

	public void log(String s)
	{
		logger.fine(s);
	}

	public Logger getLogger()
	{
		return logger;
	}

	public void setLogger(Logger l)
	{
		logger = l;
	}

	public TranslationScope getMetadataTranslationScope()
	{
		return metadataTranslationScope;
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
	public DocumentParser constructDocumentType(ElementState inlineDoc)
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
		if (semanticActionHandlerFactory == null)
			return new SemanticActionHandler();
		return semanticActionHandlerFactory.create();
	}

	@Override
	public void decreaseNumImageReferences()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void displayStatus(String message)
	{
		debug(message);
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
			boolean addToCandidatesIfNeeded, Document metadata, MetaMetadataCompositeField metaMetadata,
			boolean ignoreRejects)
	{
		if (!accept(purl) && !ignoreRejects)
			return null;
		return new MyContainer(ancestor, this, purl);
	}

	/**
	 * Add a MyContainer object to the to-be-downloaded queue of the DownloadMonitor. The
	 * DownloadMonitor will decide when the container will be actually downloaded and processed.
	 */
	@Override
	public MyContainer getContainerDownloadIfNeeded(MyContainer ancestor, ParsedURL purl, Seed seed,
			boolean dnd, boolean justCrawl, boolean justMedia)
	{
		MyContainer result = getContainer(ancestor, purl, false, false, null, null, false);
		getDownloadMonitor().download(result, result.getDispatchTarget());
		return result;
	}

	public MyContainer getContainerDownloadIfNeeded(MyContainer ancestor, ParsedURL purl, Seed seed,
			boolean dnd, boolean justCrawl, boolean justMedia, DispatchTarget dispatchTarget)
	{
		MyContainer result = getContainer(ancestor, purl, false, false, null, null, false);
		result.setDispatchTarget(dispatchTarget);
		getDownloadMonitor().download(result, dispatchTarget);
		return result;
	}

	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor, ParsedURL purl, Seed seed)
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
	public C lookupAbstractContainer(ParsedURL connectionPURL)
	{
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
	public DocumentParser newFileDirectoryType(File file)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * New Reject all the URLs from a domain from now on.
	 */
	@Override
	public void reject(String domain)
	{
		if (domain != null)
		{
			rejectDomains.add(domain);
		}
	}

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

	public SeedDistributor getSeedDistributor()
	{
		return null;
	}

	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor, ParsedURL purl, Seed seed,
			Document metadata, MetaMetadata metaMetadata)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setHeterogeneousSearchScenario(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

}
