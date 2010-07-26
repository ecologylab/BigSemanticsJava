/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import java.io.File;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.InfoCollectorBase;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.example.MyContainer;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class TestInfoCollector extends InfoCollectorBase<MyContainer>
{
	static
	{
		META_METADATA_REPOSITORY.bindMetadataClassDescriptorsToMetaMetadata(GeneratedMetadataTranslationScope.get());
	}

	public TestInfoCollector()
	{
		super(new Scope());
		// TODO Auto-generated constructor stub
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub
		
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
	public Container getContainerDownloadIfNeeded(MyContainer ancestor, ParsedURL purl, Seed seed,
			boolean dnd, boolean justCrawl, boolean justMedia)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor, ParsedURL purl, Seed seed)
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
	public DocumentParser<MyContainer, ? extends InfoCollector, ?> newFileDirectoryType(File file)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeCandidateContainer(Container candidate)
	{
		// TODO Auto-generated method stub
		
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
	public MyContainer getContainerForSearch(MyContainer ancestor, ParsedURL purl, Seed seed,
			Document metadata, MetaMetadata metaMetadata)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getContainer(MyContainer ancestor, ParsedURL purl, boolean reincarnate,
			boolean addToCandidatesIfNeeded, Document metadata, MetaMetadataCompositeField metaMetadata,
			boolean ignoreRejects)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
