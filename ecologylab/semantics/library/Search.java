package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Search extends Document
{
	@xml_nested private MetadataString	query;
	@xml_nested private MetadataString	snippet;
	
	//Lazy evaluation for efficient retrieval.
	MetadataString query()
	{
		MetadataString result = this.query;
		if(result == null)
		{
			result 			= new MetadataString();
			this.query 	= result;
		}
		return result;
	}
	
	MetadataString snippet()
	{
		MetadataString result = this.snippet;
		if(result == null)
		{
			result 			= new MetadataString();
			this.snippet 	= result;
		}
		return result;
	}
	
	public String getQuery()
	{
		return query().getValue();
	}
	
	public void hwSetQuery(String query)
	{
		this.setQuery(query);
		rebuildCompositeTermVector();
	}
	
	public String getSnippet()
	{
		return snippet().getValue();
	}
	
	public void hwSetSnippet(String snippet)
	{
		this.setSnippet(snippet);
		rebuildCompositeTermVector();
	}
	
	public void setQuery(String query)
	{
		this.query().setValue(query);
	}
	
	public void setSnippet(String snippet)
	{
		this.snippet().setValue(snippet);	
	}
}
