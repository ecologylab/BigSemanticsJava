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
	public void setQuery(String query)
	{
		this.query = query;
		rebuildCompositeTermVector();
	}
	public String getSnippet()
	{
		return snippet;
	}
	public void setSnippet(String gist)
	{
		this.snippet = gist;	
		rebuildCompositeTermVector();
	}
	public void lwSetQuery(String query)
	{
		this.query = query;
	}
	public void lwSetSnippet(String gist)
	{
		this.snippet = gist;	
	}
}
