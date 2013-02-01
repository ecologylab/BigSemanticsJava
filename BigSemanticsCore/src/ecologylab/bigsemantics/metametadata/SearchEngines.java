package ecologylab.bigsemantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;

public class SearchEngines extends ElementState
{
	
	@simpl_scalar
	private String																	defaultEngine;

	@simpl_nowrap
	@simpl_map("search_engine")
	private HashMapArrayList<String, SearchEngine>	searchEngines;

	/**
	 * @return the searchEngine
	 */
	public final HashMapArrayList<String, SearchEngine> getSearchEngines()
	{
		return searchEngines;
	}

	/**
	 * @param searchEngines
	 *          the searchEngines to set
	 */
	public final void setSearchEngines(HashMapArrayList<String, SearchEngine> searchEngines)
	{
		this.searchEngines = searchEngines;
	}

	/**
	 * @return the defaultEngine
	 */
	public String getDefaultEngine()
	{
		return defaultEngine;
	}
	
	void setDefaultEngine(String defaultEngine)
	{
		this.defaultEngine = defaultEngine;
	}

	public SearchEngine getEngine(String engineName)
	{
		return searchEngines.get(engineName);
	}

}
