/**
 * 
 */
package ecologylab.semantics.connectors.old;

import java.io.IOException;

import ecologylab.generic.DispatchTarget;
import ecologylab.io.BasicSite;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.seeding.QandDownloadable;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 * 
 */
public abstract class OldContainerI<IC extends InfoCollector> extends ContentElement<Document>
implements QandDownloadable
{
	DocumentParser 	parser;
	
	protected IC		infoCollector;
	
//	Container				ancestor;
	
	public OldContainerI(ContentElement progenitor, IC infoCollector)
	{
		super(progenitor);
		this.infoCollector	= infoCollector;
	}

	public abstract void  redirectInlinksTo(OldContainerI redirectedAbstractContainer);

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
		
		parser = DocumentParser.connect(location(), this, infoCollector);
		if (parser != null)
			parser.parse();
	}

	public abstract void addAdditionalPURL(ParsedURL purl);

	public abstract void resetPURL(ParsedURL connectionPURL);

	public DocumentParser getDocumentParser()
	{
		return parser;
	}
	public abstract ParsedURL location();

	abstract public TranslationScope getGeneratedMetadataTranslationScope();

	public abstract ParsedURL getInitialPURL();
	
	public abstract void setAsTrueSeed(Seed seed);

	public abstract boolean queueDownload();

	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	abstract public SearchResult searchResult();

	abstract public void setJustCrawl(boolean justCrawl);

	abstract public void presetDocumentType(DocumentParser documentType);
	
	abstract public void setDispatchTarget(DispatchTarget documentType);
	
	public boolean downloadHasBeenQueued()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public abstract void setSearchResult(SeedDistributor sra, int resultsSoFar);
	
	public abstract void setQuery(String query);

	
	public void addToCandidateLocalImages(AbstractImgElement imgElement)
	{
		// TODO Auto-generated method stub
	}
	
	public AbstractImgElement createImageElement(ParsedURL parsedImgUrl, String alt, int width,
			int height, boolean isMap, ParsedURL hrefPurl)
	{
		// TODO Auto-generated method stub
		return null;
	}
	public void createImageElementAndAddToPools(ParsedURL imagePurl, String alt, int width,
			int height, boolean isMap, ParsedURL hrefPurl)
	{
		// TODO Auto-generated method stub
	}

	public void createTextFromPhatContextAddToCollections(ParagraphText paraText)
	{
		// TODO Auto-generated method stub
	}

	public void allocLocalCollections()
	{
		// TODO Auto-generated method stub
	}
	
	public boolean crawlLinks()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	abstract public void hwSetTitle(String newTitle);
	
	abstract public int numLocalCandidates();
	
	abstract public boolean addSemanticInLink(SemanticAnchor newAnchor, OldContainerI srcContainer);
	
	abstract public void addCandidateContainer (OldContainerI newContainer );
	
	public abstract boolean isSeed();
	
	public abstract Seed getSeed();
	
	abstract public BasicSite site();
	
	public SemanticInLinks semanticInLinks()
	{
		return null;
	}

	public OldContainerI ancestor()
	{
		return null;
	}
	
	public void perhapsAddAdditionalContainer ( )
	{
		
	}
	
	public int numOutlinks()
	{
		return 0;
	}

	/**
	 * @param infoCollector the infoCollector to set
	 */
	public void setInfoCollector(IC infoCollector)
	{
		this.infoCollector = infoCollector;
	}
}
