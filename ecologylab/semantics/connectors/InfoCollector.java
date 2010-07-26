/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.File;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public interface InfoCollector<C extends Container>
{
	void displayStatus(String message);
	
	C lookupAbstractContainer(ParsedURL connectionPURL);

	Object globalCollectionContainersLock();
	
	void mapContainerToPURL(ParsedURL purl, C container);

	boolean accept(ParsedURL connectionPURL);

	MetaMetadataRepository metaMetaDataRepository();

	public MetaMetadata getDocumentMM(ParsedURL purl, String tagName);
	
	public MetaMetadata getDocumentMM(ParsedURL purl);
	
	public MetaMetadata getImageMM(ParsedURL purl);
	
	/**
	 * Look-up MetaMetadata for this purl.
	 * If there is no special MetaMetadata, use Document.
	 * Construct Metadata of the correct subtype, base on the MetaMetadata.
	 * Set its location field to purl.
	 * 
	 * @param purl
	 * @return
	 */
	public Document constructDocument(ParsedURL purl);


	DocumentParser<C, ? extends InfoCollector, ?> newFileDirectoryType(File file);
	
	Class<? extends InfoCollector>[] getMyClassArg();

	SeedPeer constructSeedPeer(Seed seed);
	
	public void trackFirstSeedSet(SeedSet seedSet);

	void setPlayOnStart(boolean b);

	void clear();
	
	void instantiateDocumentType(Scope registry, String key, SearchState searchState);

	void reject(String domain);

	void traversable(ParsedURL url);

	void untraversable(ParsedURL url);

	DocumentParser<C, ? extends InfoCollector, ?> constructDocumentType(ElementState inlineDoc);

	TranslationScope inlineDocumentTranslations();
	
	
	C getContainerForSearch(C ancestor, ParsedURL purl, Seed seed);
	
	C getContainerForSearch(C ancestor, ParsedURL purl, Seed seed, Document metadata, MetaMetadata metaMetadata);

	Container getContainer(C ancestor, ParsedURL purl, boolean reincarnate,
			boolean addToCandidatesIfNeeded, Document metadata, MetaMetadataCompositeField metaMetadata, boolean ignoreRejects);
	
	public void removeCandidateContainer(Container candidate);

	Scope sessionScope();

	public void setCurrentFileFromUntitled(File file);
	public void increaseNumImageReferences(); 
	public void decreaseNumImageReferences();

	/**
	 * Called to inform this that seeding is beginning.
	 */
	void beginSeeding();
	/**
	 * Called to inform this that seeding is complete.
	 */
	public void endSeeding();

	public SeedSet getSeedSet();
	
	public SeedDistributor getResultDistributer();

	SemanticActionHandler createSemanticActionHandler();
	
	JFrame getJFrame();
	
	Container getContainerDownloadIfNeeded(C ancestor, ParsedURL purl, 
			Seed seed, boolean dnd,
			boolean justCrawl, boolean justMedia);

	public abstract SeedDistributor getSeedDistributor();
}
