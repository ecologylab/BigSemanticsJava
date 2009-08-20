/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.IOException;

import ecologylab.documenttypes.DocumentType;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
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

	void setContainerMetadata(Document populatedMetadata);
}
