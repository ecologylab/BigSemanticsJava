package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.types.element.Mappable;

/**
 * 
 * @author damaraju
 * 
 */
public class Author extends Metadata implements Mappable<String>
{

//	@xml_attribute String		name;
//	@xml_leaf String			affiliation;
//	@xml_leaf ParsedURL			resultsPage;
	
//	@xml_nested MetadataString			name = new MetadataString();
//	@xml_nested MetadataString			affiliation = new MetadataString();
//	@xml_nested MetadataParsedURL		resultsPage = new MetadataParsedURL();
	
	@xml_nested MetadataString			name;
	@xml_nested MetadataString			affiliation;
	@xml_nested MetadataParsedURL		resultsPage;
	
	public Author()
	{
		// TODO Auto-generated constructor stub
	}
	
	public Author(String name)
	{
//		this.name = name;
		this.name().setValue(name);
	}

	MetadataString name()
	{
		MetadataString result = this.name;
		if(result == null)
		{
			result 			= new MetadataString();
			this.name 	= result;
		}
		return result;
	}
	MetadataString affiliation()
	{
		MetadataString result = this.affiliation;
		if(result == null)
		{
			result 			= new MetadataString();
			this.affiliation 	= result;
		}
		return result;
	}
	
	MetadataParsedURL resultsPage()
	{
		MetadataParsedURL result = this.resultsPage;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.resultsPage 	= result;
		}
		return result;
	}
	public String key() {
		
//		return name;
		return name().getValue();
	}

}
