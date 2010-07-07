package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;

public class SearchEngines extends ElementState
{
	@simpl_map("search_engine") private HashMapArrayList<String, SearchEngine> searchEngine;

	/**
	 * @return the searchEngine
	 */
	public final HashMapArrayList<String, SearchEngine> getSearchEngine()
	{
		return searchEngine;
	}

	/**
	 * @param searchEngine the searchEngine to set
	 */
	public final void setSearchEngine(HashMapArrayList<String, SearchEngine> searchEngine)
	{
		this.searchEngine = searchEngine;
	} 
	
}
