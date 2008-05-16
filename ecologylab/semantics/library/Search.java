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

//	@xml_attribute 	String	query;
//	@xml_leaf		String	snippet;
	
//	@xml_nested MetadataString	query  	= new MetadataString();
//	@xml_nested MetadataString	snippet = new MetadataString();
	@xml_nested private MetadataString	query;
	@xml_nested private MetadataString	snippet;
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
//		return query;
		return query().getValue();
	}
	public void hwSetQuery(String query)
	{
//		this.query = query;
		this.query().setValue(query);
		rebuildCompositeTermVector();
	}
	public String getSnippet()
	{
//		return snippet;
		return snippet().getValue();
	}
	public void hwSetSnippet(String snippet)
	{
//		this.snippet = gist;
		this.snippet().setValue(snippet);
		rebuildCompositeTermVector();
	}
	public void setQuery(String query)
	{
//		this.query = query;
		this.query().setValue(query);
	}
	public void setSnippet(String snippet)
	{
		this.snippet().setValue(snippet);	
	}
}
