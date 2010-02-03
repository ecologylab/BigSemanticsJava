/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.IOException;

import ecologylab.documenttypes.DocumentParser;
import ecologylab.generic.DispatchTarget;
import ecologylab.io.BasicSite;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.AbstractImgElement;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.ContentElement;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.xml.TranslationScope;

/**
 * @author quyin
 * 
 */
public class MyContainer extends Container {

	protected InfoCollector infoCollector;
	/**
	 * @param progenitor
	 */
	public MyContainer(ContentElement progenitor, InfoCollector infoCollector, ParsedURL purl) {
		super(progenitor);
		this.infoCollector = infoCollector;
		
		// set metaMetadata
		MetaMetadataRepository metaMetaDataRepository = infoCollector
				.metaMetaDataRepository();
		MetaMetadata metaMetadata = metaMetaDataRepository.getDocumentMM(purl);
		TranslationScope ts = GeneratedMetadataTranslationScope.get();
		this.metadata = (Document) metaMetadata.constructMetadata(ts);
		if (this.metadata == null)
			this.metadata = new Document(metaMetadata);
		this.metadata.setLocation(purl);
		initPurl = purl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#addAdditionalPURL(ecologylab
	 * .net.ParsedURL)
	 */
	@Override
	public void addAdditionalPURL(ParsedURL purl) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#addCandidateContainer(ecologylab
	 * .semantics.connectors.Container)
	 */
	@Override
	public void addCandidateContainer(Container newContainer) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#addSemanticInLink(ecologylab
	 * .semantics.html.documentstructure.SemanticAnchor,
	 * ecologylab.semantics.connectors.Container)
	 */
	@Override
	public boolean addSemanticInLink(SemanticAnchor newAnchor,
			Container srcContainer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeecologylab.semantics.connectors.Container#addToCandidateLocalImages(
	 * ecologylab.semantics.connectors.AbstractImgElement)
	 */
	@Override
	public void addToCandidateLocalImages(AbstractImgElement imgElement) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#allocLocalCollections()
	 */
	@Override
	public void allocLocalCollections() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#constructAndSetMetadata(ecologylab
	 * .semantics.metametadata.MetaMetadata)
	 */
	@Override
	public Document constructAndSetMetadata(MetaMetadata metaMetadata) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#constructMetadata(ecologylab
	 * .semantics.metametadata.MetaMetadata)
	 */
	@Override
	public Document constructMetadata(MetaMetadata metaMetadata) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#crawlLinks()
	 */
	@Override
	public boolean crawlLinks() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#createImageElement(ecologylab
	 * .net.ParsedURL, java.lang.String, int, int, boolean,
	 * ecologylab.net.ParsedURL)
	 */
	@Override
	public AbstractImgElement createImageElement(ParsedURL parsedImgUrl,
			String alt, int width, int height, boolean isMap, ParsedURL hrefPurl) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#createImageElementAndAddToPools
	 * (ecologylab.net.ParsedURL, java.lang.String, int, int, boolean,
	 * ecologylab.net.ParsedURL)
	 */
	@Override
	public void createImageElementAndAddToPools(ParsedURL imagePurl,
			String alt, int width, int height, boolean isMap, ParsedURL hrefPurl) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeecologylab.semantics.connectors.Container#
	 * createTextElementAndAddToCollections
	 * (ecologylab.semantics.html.ParagraphText)
	 */
	@Override
	public void createTextElementAndAddToCollections(ParagraphText paraText) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#downloadHasBeenQueued()
	 */
	@Override
	public boolean downloadHasBeenQueued() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#getDocumentParser()
	 */
	@Override
	public DocumentParser getDocumentParser() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeecologylab.semantics.connectors.Container#
	 * getGeneratedMetadataTranslationScope()
	 */
	@Override
	public TranslationScope getGeneratedMetadataTranslationScope() {
		return GeneratedMetadataTranslationScope.get();
	}
	
	protected ParsedURL initPurl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#getInitialPURL()
	 */
	@Override
	public ParsedURL getInitialPURL() {
		// TODO Auto-generated method stub
		return initPurl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#hwSetTitle(java.lang.String)
	 */
	@Override
	public void hwSetTitle(String newTitle) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#isSeed()
	 */
	@Override
	public boolean isSeed() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#numLocalCandidates()
	 */
	@Override
	public int numLocalCandidates() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#performDownload()
	 */
	@Override
	public void performDownload() throws IOException {
		DocumentParser parser = DocumentParser.connect(
				purl(),
				this,
				infoCollector,
				new MySemanticActionHandler()
				);
		parser.parse();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#presetDocumentType(ecologylab
	 * .documenttypes.DocumentParser)
	 */
	@Override
	public void presetDocumentType(DocumentParser documentType) {
		// TODO Auto-generated method stub

	}

	@Override
	public ParsedURL purl() {
		if (metadata() == null)
			return null;
		return metadata().getLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#queueDownload()
	 */
	@Override
	public boolean queueDownload() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#redirectInlinksTo(ecologylab
	 * .semantics.connectors.Container)
	 */
	@Override
	public void redirectInlinksTo(Container redirectedAbstractContainer) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#resetPURL(ecologylab.net.ParsedURL
	 * )
	 */
	@Override
	public void resetPURL(ParsedURL connectionPURL) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#searchResult()
	 */
	@Override
	public SearchResult searchResult() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#setAsTrueSeed(ecologylab.semantics
	 * .seeding.Seed)
	 */
	@Override
	public void setAsTrueSeed(Seed seed) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#setDispatchTarget(ecologylab
	 * .generic.DispatchTarget)
	 */
	@Override
	public void setDispatchTarget(DispatchTarget documentType) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#setInArticleBody(boolean)
	 */
	@Override
	public void setInArticleBody(boolean value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#setJustCrawl(boolean)
	 */
	@Override
	public void setJustCrawl(boolean justCrawl) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Container#setQuery(java.lang.String)
	 */
	@Override
	public void setQuery(String query) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ecologylab.semantics.connectors.Container#setSearchResult(ecologylab.
	 * semantics.seeding.SeedDistributor, int)
	 */
	@Override
	public void setSearchResult(SeedDistributor sra, int resultsSoFar) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.ContentElement#getMetadataClass()
	 */
	@Override
	public Class getMetadataClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#cancel()
	 */
	@Override
	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#downloadAndParseDone()
	 */
	@Override
	public void downloadAndParseDone() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#getSite()
	 */
	@Override
	public BasicSite getSite() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#handleIoError()
	 */
	@Override
	public void handleIoError() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#handleTimeout()
	 */
	@Override
	public boolean handleTimeout() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#isDownloadDone()
	 */
	@Override
	public boolean isDownloadDone() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#isRecycled()
	 */
	@Override
	public boolean isRecycled() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.io.Downloadable#message()
	 */
	@Override
	public String message() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.model.text.TermVectorFeature#termVector()
	 */
	@Override
	public ITermVector termVector() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ecologylab.semantics.connectors.Hyperlink#container()
	 */
	@Override
	public Container container() {
		// TODO Auto-generated method stub
		return null;
	}

}
