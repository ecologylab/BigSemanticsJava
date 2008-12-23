/**
 * 
 */
package ecologylab.documenttypes;

import java.io.IOException;

import ecologylab.net.ParsedURL;

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
	
	

}
