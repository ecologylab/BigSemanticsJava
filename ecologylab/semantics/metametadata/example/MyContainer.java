/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.IOException;
import java.util.ArrayList;

import ecologylab.generic.DispatchTarget;
import ecologylab.io.BasicSite;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandlerBase;
import ecologylab.semantics.connectors.AbstractImgElement;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.ContentElement;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.xml.TranslationScope;

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
	/**
	 * New !!
	 * This interface enables users to customize their own collecting methods. Use MyInfoCollector to
	 * add a listener who implemented this interface.
	 * 
	 * @author quyin
	 * 
	 */
	public interface MetadataCollectingListener
	{
		void collect(Metadata metadata);
	}
	
	private ArrayList<MetadataCollectingListener>	collectingListeners;

	public ArrayList<MetadataCollectingListener> getCollectingListeners()
	{
		return collectingListeners;
	}

	public void setCollectingListeners(ArrayList<MetadataCollectingListener> collectingListeners)
	{
		this.collectingListeners = collectingListeners;
	}

	protected InfoCollector	infoCollector;

	public MyContainer(ContentElement progenitor, InfoCollector infoCollector, ParsedURL purl)
	{
		super(progenitor);
		this.infoCollector = infoCollector;
		this.metadata = (Document) infoCollector.constructDocument(purl);

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

	// New -generated when repository is compiled
	@Override
	public TranslationScope getGeneratedMetadataTranslationScope()
	{
		return GeneratedMetadataTranslationScope.get();
	}

	// needed used in downloading process
	private ParsedURL	initPurl;

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
		//calls connect to find the right parser, then calls the infocollector to download the content
		//also process the semantic actions
		DocumentParser parser = DocumentParser.connect(purl(), this, infoCollector,
				new SemanticActionHandlerBase());
		
		parser.parse();

		//listeners again
		for (MetadataCollectingListener listener : collectingListeners)
		{
			listener.collect(metadata);
		}
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
		// TODO Auto-generated method stub

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

	@Override
	public void setDispatchTarget(DispatchTarget documentType)
	{
		// TODO Auto-generated method stub

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
	public boolean cancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void downloadAndParseDone()
	{
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return false;
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

}
