package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.types.element.Mappable;

/**
 * 
 * @author damaraju
 * 
 */
public class Author extends Metadata implements Mappable<String>
{

	@xml_attribute String		name;
	@xml_leaf ParsedURL			resultsPage;
	@xml_leaf String			affiliation;


	public String key() {
		
		return null;
	}

}
