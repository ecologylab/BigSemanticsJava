/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.File;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.DocumentType;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.ResultDistributer;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;

/**
 * @author andruid
 *
 */
public interface InfoCollector<AC extends Container, IC extends InfoCollector>
{
	void displayStatus(String message);
	
	AC lookupAbstractContainer(ParsedURL connectionPURL);

	Object globalCollectionContainersLock();
	
	void mapContainerToPURL(ParsedURL purl, AC container);

	boolean accept(ParsedURL connectionPURL);

	MetaMetadataRepository metaMetaDataRepository();

	public MetaMetadata getMetaMetadata(ParsedURL purl);
	
	DocumentType<AC, ? extends IC, ?> newFileDirectoryType(File file);
	
	Class<IC>[] getInfoProcessorClassArg();

	SeedPeer constructSeedPeer(Seed seed);
	
	public void trackFirstSeedSet(SeedSet seedSet);

	void setPlayOnStart(boolean b);

	void clear();
	
	void instantiateDocumentType(Scope registry, String key, SearchState searchState);

	void reject(String domain);

	void traversable(ParsedURL url);

	void untraversable(ParsedURL url);

	DocumentType<AC, ? extends IC, ?> constructDocumentType(ElementState inlineDoc);

	TranslationScope inlineDocumentTranslations();
	
	
	AC getContainerForSearch(AC ancestor, ParsedURL purl, Seed seed);
	
	AC getContainerForSearch(AC ancestor, ParsedURL purl, Seed seed, MetaMetadata metaMetadata);

	AC getContainer(AC ancestor, ParsedURL purl, boolean reincarnate,
			boolean addToCandidatesIfNeeded, MetaMetadata metaMetadata);

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
	
	public ResultDistributer getResultDistributer();

	SemanticActionHandler createSemanticActionHandler();
	
	JFrame getJFrame();
	
}
