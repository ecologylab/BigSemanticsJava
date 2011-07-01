/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.generic.Debug;

/**
 * Crawler will encapsulate all state regarding web crawling. 
 * This means WeightSets and PriorizedPools of candidate unparsed simple Document + CompoundDocument objects. 
 * The Crawler will also maintain a new map of locations for Document objects the user has already seen. 
 * CrawlerWithImages will extend Crawler live in the ecologylabImage project. 
 * It will include a pool of downloaded images.
 * 
 * @author andruid
 */
public class Crawler extends Debug
{

	/**
	 * 
	 */
	public Crawler()
	{
		// TODO Auto-generated constructor stub
	}

}
