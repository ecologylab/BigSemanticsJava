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
	@xml_leaf String			affiliation;
	@xml_leaf ParsedURL			resultsPage;
	public Author()
	{
		// TODO Auto-generated constructor stub
	}
	
	public Author(String name)
	{
		this.name = name;
	}

	public String key() {
		
		return name;
	}

}
