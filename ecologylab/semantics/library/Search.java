package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Search extends Document
{

	@xml_attribute 	String	query;
	@xml_leaf		String	snippet;
	
	public String getQuery()
	{
		return query;
	}
	public void hwSetQuery(String query)
	{
		this.query = query;
		rebuildCompositeTermVector();
	}
	public String getSnippet()
	{
		return snippet;
	}
	public void hwSetSnippet(String gist)
	{
		this.snippet = gist;	
		rebuildCompositeTermVector();
	}
	public void setQuery(String query)
	{
		this.query = query;
	}
	public void setSnippet(String snippet)
	{
		this.snippet = snippet;	
	}
}
