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
	@xml_leaf		String	gist;
	
	public String getQuery()
	{
		return query;
	}
	public void setQuery(String query)
	{
		this.query = query;
	}
	public String getGist()
	{
		return gist;
	}
	public void setGist(String gist)
	{
		this.gist = gist;
	}
	
}
