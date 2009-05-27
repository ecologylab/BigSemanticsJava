/**
 * 
 */
package ecologylab.semantics.connectors;

import ecologylab.semantics.seeding.SearchState;

/**
 * @author amathur
 *
 */
public class SearchEngineUtilities
{

	static public String siteGoogleLimitSearchURLString(SearchState searchSeed)
	{   	
		return SearchEngineNames.regularGoogleSearchURLString + siteGoogleAppend(searchSeed.siteString());
	}

	static public String siteGoogleLimitSearchURLString(String siteString)
	{   	
		return SearchEngineNames.regularGoogleSearchURLString + siteGoogleAppend(siteString);
	}

	static public String siteGoogleAppend(String siteString)
	{   	
		return (siteString == null) ? "" : "+site%3A" +siteString;
	}

}
