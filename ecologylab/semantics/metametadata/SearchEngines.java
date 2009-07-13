package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.UserAgent;
import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_map;

public class SearchEngines extends ElementState
{
	@xml_map("search_engine") private HashMapArrayList<String, SearchEngine> searchEngine;

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
