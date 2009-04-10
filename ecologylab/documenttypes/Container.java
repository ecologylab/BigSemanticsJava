/**
 * 
 */
package ecologylab.documenttypes;

import java.io.IOException;

import ecologylab.net.ParsedURL;
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

	void performDownload()
	throws IOException
;

	void addAdditionalPURL(ParsedURL purl);

	void resetPURL(ParsedURL connectionPURL);

	DocumentType documentType();

	ParsedURL purl();
	
	public TranslationScope getTranslationScope();

	void setBias(float bias);

	void setAsTrueSeed(Seed seed);

  boolean queueDownload();
	
	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	public SearchResult searchResult();

	public void hwSetMetadataField(String query, String queryValue);

}
