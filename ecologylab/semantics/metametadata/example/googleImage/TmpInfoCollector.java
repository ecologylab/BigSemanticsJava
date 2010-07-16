package ecologylab.semantics.metametadata.example.googleImage;

import java.io.File;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.TranslationScope;

public class TmpInfoCollector implements InfoCollector<Container>
{

	@Override
	public boolean accept(ParsedURL connectionPURL)
	{
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentParser<Container, ? extends InfoCollector, ?> constructDocumentType(
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
	public Container getContainer(Container ancestor, ParsedURL purl, boolean reincarnate,
			boolean addToCandidatesIfNeeded, MetaMetadata metaMetadata)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getContainerDownloadIfNeeded(Container ancestor, ParsedURL purl, Seed seed,
			boolean dnd, boolean justCrawl, boolean justMedia)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getContainerForSearch(Container ancestor, ParsedURL purl, Seed seed)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getContainerForSearch(Container ancestor, ParsedURL purl, Seed seed,
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
	public Container lookupAbstractContainer(ParsedURL connectionPURL)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mapContainerToPURL(ParsedURL purl, Container container)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public MetaMetadataRepository metaMetaDataRepository()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentParser<Container, ? extends InfoCollector, ?> newFileDirectoryType(File file)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reject(String domain)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCandidateContainer(Container candidate)
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
