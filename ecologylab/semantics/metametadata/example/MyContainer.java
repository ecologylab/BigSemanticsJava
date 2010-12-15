/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.IOException;

import ecologylab.generic.DispatchTarget;
import ecologylab.io.BasicSite;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.AbstractImgElement;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.ContentElement;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.TranslationScope;

/**
 * This is the Container class for this example.
 * 
 * A Container is derived from ecologylab.semantics.connectors.Container. It holds the URL pointing
 * to an information resource, performs the downloading action through the interface Downloadable,
 * and parse the data by calling a Parser.
 * 
 * We use the listener mechanism to allow users to customize their own collecting methods. Use
 * MyInfoCollector to add a listener who implemented the interface
 * MyContainer.MetadataCollectingListener.
 * 
 * This class doesn't implement all the methods in Container. You can implement them by yourself to
 * add more functions, like finer control of the downloading process.
 * 
 * @author quyin
 */
public class MyContainer extends Container
{

	protected MyInfoCollector	infoCollector;

	// needed: used in downloading process
	private ParsedURL					initPurl;

	private boolean						downloadDone	= false;

	private DispatchTarget		dispatchTarget;

	public MyContainer(ContentElement progenitor, MyInfoCollector infoCollector, ParsedURL purl)
	{
		super(progenitor);
		this.infoCollector = infoCollector;
		this.metadata = (Document) infoCollector.constructDocument(purl);
		if (progenitor != null && progenitor instanceof MyContainer)
			this.dispatchTarget = ((MyContainer) progenitor).dispatchTarget;

		initPurl = purl;
	}

	@Override
	public void addAdditionalPURL(ParsedURL purl)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addCandidateContainer(Container newContainer)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addSemanticInLink(SemanticAnchor newAnchor, Container srcContainer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addToCandidateLocalImages(AbstractImgElement imgElement)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void allocLocalCollections()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Document constructAndSetMetadata(MetaMetadata metaMetadata)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document constructMetadata(MetaMetadata metaMetadata)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean crawlLinks()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractImgElement createImageElement(ParsedURL parsedImgUrl, String alt, int width,
			int height, boolean isMap, ParsedURL hrefPurl)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createImageElementAndAddToPools(ParsedURL imagePurl, String alt, int width,
			int height, boolean isMap, ParsedURL hrefPurl)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void createTextElementAndAddToCollections(ParagraphText paraText)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean downloadHasBeenQueued()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DocumentParser getDocumentParser()
	{
		// TODO Auto-generated method stub
		return null;
	}

	// new: generated when repository is compiled
	@Override
	public TranslationScope getGeneratedMetadataTranslationScope()
	{
		return infoCollector.getMetadataTranslationScope();
	}

	@Override
	public ParsedURL getInitialPURL()
	{
		// TODO Auto-generated method stub
		return initPurl;
	}

	@Override
	public void hwSetTitle(String newTitle)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSeed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int numLocalCandidates()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * This method performs the downloading action. It first calls the DocumentParser.connect() method
	 * to get the appropriate parser for the URL, then downloads it and parses it. At last, it calls
	 * the collect() method for each listener to allow the application to collect information from the
	 * parsed metadata.
	 */
	@Override
	public void performDownload() throws IOException
	{
		// calls connect to find the right parser, then calls the infocollector to download the content
		// also process the semantic actions
		
		DocumentParser parser = DocumentParser.connect(purl(), this, infoCollector,
				infoCollector.createSemanticActionHandler(), null);
		if (parser != null)
			parser.parse();

		/*
		 * recording visited urls could exhaust memory!
		 * 
		 * infoCollector.log(purl().toString()); if(!infoCollector.isVisited(purl())) {
		 * System.out.println("\nDownloading slow");
		 * infoCollector.getDownloadMonitor().pause(500);//60000 + (MathTools.random(100)*2000));
		 * infoCollector.setVisited(this); }
		 */
	}

	@Override
	public void presetDocumentType(DocumentParser documentType)
	{
		// TODO Auto-generated method stub

	}

	// new
	@Override
	public ParsedURL purl()
	{
		if (metadata() == null)
			return null;
		return metadata().getLocation();
	}

	@Override
	public boolean queueDownload()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void redirectInlinksTo(Container redirectedAbstractContainer)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void resetPURL(ParsedURL connectionPURL)
	{
		if (metadata() != null)
			metadata().setLocation(connectionPURL);
		// infoCollector.setVisited(this);
	}

	@Override
	public SearchResult searchResult()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAsTrueSeed(Seed seed)
	{
		// TODO Auto-generated method stub

	}

	public DispatchTarget getDispatchTarget()
	{
		return dispatchTarget;
	}
	
	@Override
	public void setDispatchTarget(DispatchTarget dispatchTarget)
	{
		this.dispatchTarget = dispatchTarget;
	}

	@Override
	public void setInArticleBody(boolean value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setJustCrawl(boolean justCrawl)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setQuery(String query)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setSearchResult(SeedDistributor sra, int resultsSoFar)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Class getMetadataClass()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean shouldCancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void downloadAndParseDone()
	{
		downloadDone = true;
	}

	@Override
	public BasicSite getSite()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleIoError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleTimeout()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDownloadDone()
	{
		return downloadDone;
	}

	@Override
	public boolean isRecycled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String message()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITermVector termVector()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container container()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Seed getSeed()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private String cachedToString;
	public String toString()
	{
		if (cachedToString == null)
			cachedToString = getClassName() + "[" + purl() + "]"; 
		return cachedToString;
	}

	@Override
	public BasicSite site()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void recycleUnconditionally()
	{
		
	}
}
