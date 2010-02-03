/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.File;

import javax.swing.JFrame;

import ecologylab.collections.Scope;
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
public class MyInfoCollector implements InfoCollector<MyContainer> {
	/**
	 * 
	 */
	public MyInfoCollector(String repoFilepath) {
		mmdRepo = MetaMetadataRepository.load(new File(repoFilepath));
		mmdRepo.initializeRepository(GeneratedMetadataTranslationScope.get());
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#accept(ecologylab.net.ParsedURL)
	 */
	@Override
	public boolean accept(ParsedURL connectionPURL) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#beginSeeding()
	 */
	@Override
	public void beginSeeding() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#constructDocument(ecologylab.net.ParsedURL)
	 */
	@Override
	public Document constructDocument(ParsedURL purl) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#constructDocumentType(ecologylab.xml.ElementState)
	 */
	@Override
	public DocumentParser<MyContainer, ? extends InfoCollector, ?> constructDocumentType(
			ElementState inlineDoc) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#constructSeedPeer(ecologylab.semantics.seeding.Seed)
	 */
	@Override
	public SeedPeer constructSeedPeer(Seed seed) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#createSemanticActionHandler()
	 */
	@Override
	public SemanticActionHandler createSemanticActionHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#decreaseNumImageReferences()
	 */
	@Override
	public void decreaseNumImageReferences() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#displayStatus(java.lang.String)
	 */
	@Override
	public void displayStatus(String message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#endSeeding()
	 */
	@Override
	public void endSeeding() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getContainer(ecologylab.semantics.connectors.Container, ecologylab.net.ParsedURL, boolean, boolean, ecologylab.semantics.metametadata.MetaMetadata)
	 */
	@Override
	public MyContainer getContainer(MyContainer ancestor, ParsedURL purl,
			boolean reincarnate, boolean addToCandidatesIfNeeded,
			MetaMetadata metaMetadata) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getContainerDownloadIfNeeded(ecologylab.semantics.connectors.Container, ecologylab.net.ParsedURL, ecologylab.semantics.seeding.Seed, boolean, boolean, boolean)
	 */
	@Override
	public MyContainer getContainerDownloadIfNeeded(MyContainer ancestor,
			ParsedURL purl, Seed seed, boolean dnd, boolean justCrawl,
			boolean justMedia) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getContainerForSearch(ecologylab.semantics.connectors.Container, ecologylab.net.ParsedURL, ecologylab.semantics.seeding.Seed)
	 */
	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor,
			ParsedURL purl, Seed seed) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getContainerForSearch(ecologylab.semantics.connectors.Container, ecologylab.net.ParsedURL, ecologylab.semantics.seeding.Seed, ecologylab.semantics.metametadata.MetaMetadata)
	 */
	@Override
	public MyContainer getContainerForSearch(MyContainer ancestor,
			ParsedURL purl, Seed seed, MetaMetadata metaMetadata) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getDocumentMM(ecologylab.net.ParsedURL, java.lang.String)
	 */
	@Override
	public MetaMetadata getDocumentMM(ParsedURL purl, String tagName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getDocumentMM(ecologylab.net.ParsedURL)
	 */
	@Override
	public MetaMetadata getDocumentMM(ParsedURL purl) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getImageMM(ecologylab.net.ParsedURL)
	 */
	@Override
	public MetaMetadata getImageMM(ParsedURL purl) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getJFrame()
	 */
	@Override
	public JFrame getJFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getMyClassArg()
	 */
	@Override
	public Class<? extends InfoCollector>[] getMyClassArg() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getResultDistributer()
	 */
	@Override
	public SeedDistributor getResultDistributer() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#getSeedSet()
	 */
	@Override
	public SeedSet getSeedSet() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#globalCollectionContainersLock()
	 */
	@Override
	public Object globalCollectionContainersLock() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#increaseNumImageReferences()
	 */
	@Override
	public void increaseNumImageReferences() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#inlineDocumentTranslations()
	 */
	@Override
	public TranslationScope inlineDocumentTranslations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#instantiateDocumentType(ecologylab.collections.Scope, java.lang.String, ecologylab.semantics.seeding.SearchState)
	 */
	@Override
	public void instantiateDocumentType(Scope registry, String key,
			SearchState searchState) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#lookupAbstractContainer(ecologylab.net.ParsedURL)
	 */
	@Override
	public MyContainer lookupAbstractContainer(ParsedURL connectionPURL) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#mapContainerToPURL(ecologylab.net.ParsedURL, ecologylab.semantics.connectors.Container)
	 */
	@Override
	public void mapContainerToPURL(ParsedURL purl, MyContainer container) {
		// TODO Auto-generated method stub

	}
	
	protected MetaMetadataRepository mmdRepo;

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#metaMetaDataRepository()
	 */
	@Override
	public MetaMetadataRepository metaMetaDataRepository() {
		// TODO Auto-generated method stub
		return mmdRepo;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#newFileDirectoryType(java.io.File)
	 */
	@Override
	public DocumentParser<MyContainer, ? extends InfoCollector, ?> newFileDirectoryType(
			File file) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#reject(java.lang.String)
	 */
	@Override
	public void reject(String domain) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#removeCandidateContainer(ecologylab.semantics.connectors.Container)
	 */
	@Override
	public void removeCandidateContainer(MyContainer candidate) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#sessionScope()
	 */
	@Override
	public Scope sessionScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#setCurrentFileFromUntitled(java.io.File)
	 */
	@Override
	public void setCurrentFileFromUntitled(File file) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#setPlayOnStart(boolean)
	 */
	@Override
	public void setPlayOnStart(boolean b) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#trackFirstSeedSet(ecologylab.semantics.seeding.SeedSet)
	 */
	@Override
	public void trackFirstSeedSet(SeedSet seedSet) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#traversable(ecologylab.net.ParsedURL)
	 */
	@Override
	public void traversable(ParsedURL url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.connectors.InfoCollector#untraversable(ecologylab.net.ParsedURL)
	 */
	@Override
	public void untraversable(ParsedURL url) {
		// TODO Auto-generated method stub

	}

}
