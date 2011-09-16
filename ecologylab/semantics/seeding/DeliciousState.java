/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.serialization.annotations.simpl_scalar;


/**
 * 
 * @author andruid
 */
public class DeliciousState extends SearchState
{
	private static final String   POPULAR_BOOKMARKS_MSG     = "Popular Bookmarks";
	private static final String   USER_CREATION_MSG         = "user:";
	private static final String   TAG_CREATION_MSG          = "tag:";

    private static final String[][] POPULAR_FIXED_QUERY_ARRAY = {
                                                                 {POPULAR_BOOKMARKS_MSG, POPULAR_BOOKMARKS_MSG},
                                                                 {USER_CREATION_MSG, USER_CREATION_MSG},
                                                                 {TAG_CREATION_MSG, TAG_CREATION_MSG}
                                                                 };
	/**
	 * Specifies delicious popular.
	 */
	@simpl_scalar private boolean		popular;
	public static final String	DELICIOUS	= "delicious";

	/**
	 * 
	 */
	public DeliciousState()
	{
		this.engine		= DeliciousState.DELICIOUS;
	}

	/**
	 * @param query
	 * @param searchEngineType
	 */
	//TODO do we need this constructor? is it invoked by reflection?
//	public DeliciousState(String query, String searchEngineType)
//	{
//		super(query, DELICIOUS);
//	}

	/**
	 * @param query
	 * @param numResults
	 * @param initialIntensity
	 * @param bias
	 */
//	public DeliciousState(String query, int numResults, short initialIntensity,
//			float bias)
//	{
//		super(query, initialIntensity, bias);
//		this.engine		= DELICIOUS;
//	}

	/**
	 * @return true if specified that the user wants to see the most popular tagged 
	 * stuff in del.icio.us
	 */
	public boolean popular()
	{
		return popular;
	}
	
	/**
	 * If the engine only takes a fixed vocabulary of queries, this method
	 * must be overridden to return that vocabulary as an array.
	 * Each entry in the outer array, is another array with 2 entries:
	 *   first entry is human readable, for seeding language and dashboard.
	 *   Second entry is internal String that gets passed to the engine.
	 * 
	 * @return	null, because in the default case, queries can be anything.
	 */
	public String[][] fixedQueryVocabulary()
	{
		return POPULAR_FIXED_QUERY_ARRAY;
	}
	
	public String getFixedQueryVocabularySelection()
    {
        String creator = getCreator();
        if (creator != null) // we have a creator (therefor no query)
            return USER_CREATION_MSG + creator;
        else if (query == null)
            return POPULAR_BOOKMARKS_MSG;

        // else it's a tag search.
        return TAG_CREATION_MSG + query;
    }
	/**
	 * delicious only thing.
	 */
	public String getCreator()
	{
	    return creator();
	}
	
	/**
	 * Set the seed to specify that  the user wants to see the most popular tagged 
	 * stuff in del.icio.us
	 * 
	 * @param popular the popular to set
	 */
	public void setPopular(boolean popular)
	{
		this.popular = popular;
	}

}
