package ecologylab.semantics.metametadata;

import java.util.HashMap;

import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ElementState.simpl_map;
import ecologylab.serialization.ElementState.simpl_nowrap;
import ecologylab.serialization.ElementState.simpl_scalar;

public class SearchEngines extends HashMap<String, SearchEngine>
{	
	@simpl_scalar private String defaultEngine;

	/**
	 * @return the defaultEngine
	 */
	public String getDefaultEngine()
	{
		return defaultEngine;
	} 
	
}
