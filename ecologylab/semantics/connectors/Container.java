/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.IOException;

import ecologylab.documenttypes.DocumentType;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.metadata.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.seeding.ResultDistributer;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.xml.TranslationScope;

/**
 * @author andruid
 * 
 */
public interface Container
{

	void redirectInlinksTo(Container redirectedAbstractContainer);

	void performDownload() throws IOException;

	void addAdditionalPURL(ParsedURL purl);

	void resetPURL(ParsedURL connectionPURL);

	DocumentType documentType();

	ParsedURL purl();

	public TranslationScope getGeneratedMetadataTranslationScope();

	void setBias(float bias);

	void setAsTrueSeed(Seed seed);

	boolean queueDownload();

	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	public SearchResult searchResult();

	public void setJustCrawl(boolean justCrawl);

	public void presetDocumentType(DocumentType documentType);
	
	public void setDispatchTarget(DispatchTarget documentType);
	
	public boolean downloadHasBeenQueued();

	void setSearchResult(ResultDistributer sra, int resultsSoFar);
	
	void setQuery(String query);
	
	void delete();
	
	Document metadata();

	public Document constructMetadata(MetaMetadata metaMetadata);
	
	public Document constructAndSetMetadata(MetaMetadata metaMetadata);

	void setMetadata(Document populatedMetadata);
	
	public void addToCandidateLocalImages(AbstractImgElement imgElement);
	
	public void createImageElementAndAddToPools(ParsedURL imagePurl, String alt, 
			int width, int height, boolean isMap, ParsedURL hrefPurl);

	public void allocLocalCollections();
	
	public boolean crawlLinks();
	
	public void hwSetTitle(String newTitle);
	
	public void createTextElementAndAddToCollections(ParagraphText paraText);
	
	public int numLocalCandidates();
	
	public boolean addSemanticInLink(SemanticAnchor newAnchor, Container srcContainer);
	
	public void addCandidateContainer (Container newContainer );
	
	boolean isSeed();
	
	public void setInArticleBody(boolean value);
	
	public AbstractImgElement createImageElement(ParsedURL parsedImgUrl, String alt, 
			int width, int height, boolean isMap, ParsedURL hrefPurl);
}
