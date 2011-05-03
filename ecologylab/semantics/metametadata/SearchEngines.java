package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ElementState;

public class SearchEngines extends ElementState
{	
	@simpl_scalar private String defaultEngine;

	@simpl_nowrap @simpl_map("search_engine")
	private HashMapArrayList<String, SearchEngine> searchEngines;

	/**
	 * @return the searchEngine
	 */
	public final HashMapArrayList<String, SearchEngine> getSearchEngine()
	{
		return searchEngines;
	}

	/**
	 * @param searchEngine the searchEngine to set
	 */
	public final void setSearchEngine(HashMapArrayList<String, SearchEngine> searchEngine)
	{
		this.searchEngines = searchEngine;
	} 
	/**
	 * @return the defaultEngine
	 */
	public String getDefaultEngine()
	{
		return defaultEngine;
	} 
	
	public SearchEngine get(String that)
	{
		return searchEngines.get(that);
	}
}
