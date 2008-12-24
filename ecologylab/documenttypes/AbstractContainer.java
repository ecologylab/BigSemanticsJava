/**
 * 
 */
package ecologylab.documenttypes;

import java.io.IOException;

import ecologylab.net.ParsedURL;
import ecologylab.xml.TranslationScope;

/**
 * @author andruid
 *
 */
public interface AbstractContainer
{

	void redirectInlinksTo(AbstractContainer redirectedAbstractContainer);

	void performDownload()
	throws IOException
;

	void addAdditionalPURL(ParsedURL purl);

	void resetPURL(ParsedURL connectionPURL);

	DocumentType documentType();

	ParsedURL purl();
	
	public TranslationScope getTranslationScope();

}
