/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.documenttypes.DocumentParser;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SeedPeer;
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
 * @author quyin
 * 
 */
public class MyInfoCollector implements InfoCollector<MyContainer>
{
	public final static int					DEFAULT_COUNT_DOWNLOAD_THREAD	= 1;

	private MetaMetadataRepository	mmdRepo;

	private Set<String>				rejectDomains;

	private DownloadMonitor					downloadMonitor;
	
	/**
	 * @return the downloadMonitor
	 */
	public DownloadMonitor getDownloadMonitor()
	{
		return downloadMonitor;
	}

	private ArrayList<MyContainer.MetadataCollectingListener>	collectingListeners;

	public void addListener(MyContainer.MetadataCollectingListener listener)
	{
		collectingListeners.add(listener);
	}

	public void removeListener(MyContainer.MetadataCollectingListener listener)
	{
		collectingListeners.remove(listener);
	}
	
	public ArrayList<MyContainer.MetadataCollectingListener> getListeners()
	{
		return collectingListeners;
	}

	public MyInfoCollector(String repoFilepath)
	{
		mmdRepo = MetaMetadataRepository.load(new File(repoFilepath));
		mmdRepo.initializeRepository(GeneratedMetadataTranslationScope.get());
		rejectDomains = new HashSet<String>();
		downloadMonitor = new DownloadMonitor("info-collector_download-monitor",
				DEFAULT_COUNT_DOWNLOAD_THREAD);
		collectingListeners = new ArrayList<MyContainer.MetadataCollectingListener>();
	}

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

	@Override
	public MetaMetadataRepository metaMetaDataRepository()
	{
		// TODO Auto-generated method stub
		return mmdRepo;
	}

	@Override
	public DocumentParser<MyContainer, ? extends InfoCollector, ?> newFileDirectoryType(File file)
	{
		// TODO Auto-generated method stub
		return null;
	}

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
