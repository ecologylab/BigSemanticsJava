/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.IOException;

import ecologylab.generic.DispatchTarget;
import ecologylab.io.Downloadable;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.seeding.QandDownloadable;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 * 
 */
public abstract class Container extends ContentElement<Document>
implements QandDownloadable
{

	public Container(ContentElement progenitor)
	{
		super(progenitor);
		
	}

	public abstract void  redirectInlinksTo(Container redirectedAbstractContainer);

	public abstract void performDownload() throws IOException;

	public abstract void addAdditionalPURL(ParsedURL purl);

	public abstract void resetPURL(ParsedURL connectionPURL);

	public abstract DocumentParser getDocumentParser();

	public abstract ParsedURL purl();

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
	
	abstract public boolean downloadHasBeenQueued();

	public abstract void setSearchResult(SeedDistributor sra, int resultsSoFar);
	
	public abstract void setQuery(String query);
	


	abstract public Document constructMetadata(MetaMetadata metaMetadata);
	
	abstract public Document constructAndSetMetadata(MetaMetadata metaMetadata);

	
	abstract public void addToCandidateLocalImages(AbstractImgElement imgElement);
	
	abstract public void createImageElementAndAddToPools(ParsedURL imagePurl, String alt, 
			int width, int height, boolean isMap, ParsedURL hrefPurl);

	abstract public void allocLocalCollections();
	
	abstract public boolean crawlLinks();
	
	abstract public void hwSetTitle(String newTitle);
	
	abstract public void createTextElementAndAddToCollections(ParagraphText paraText);
	
	abstract public int numLocalCandidates();
	
	abstract public boolean addSemanticInLink(SemanticAnchor newAnchor, Container srcContainer, boolean onlyIfUnique);
	
	abstract public void addCandidateContainer (Container newContainer );
	
	public abstract boolean isSeed();
	
	abstract public void setInArticleBody(boolean value);
	
	abstract public AbstractImgElement createImageElement(ParsedURL parsedImgUrl, String alt, 
			int width, int height, boolean isMap, ParsedURL hrefPurl);
}
