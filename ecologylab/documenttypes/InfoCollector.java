/**
 * 
 */
package ecologylab.documenttypes;

import java.io.File;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.types.element.ArrayListState;

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

	Scope sessionScope();

	public void setCurrentFileFromUntitled(File file);
}
