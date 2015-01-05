package ecologylab.bigsemantics.seeding;

import ecologylab.generic.Debug;

public class SearchResult 
extends Debug
{
	/**
	 * ranking among sites google returned.
	 * in other words, of the n results that google returned, this is the number of the current one.
	 */
	int						resultNum;

	SeedDistributor	resultDistributer;
	
	public SearchResult(SeedDistributor resultDistributer, int resultNum)
	{
		this.resultDistributer	= resultDistributer;
		this.resultNum				= resultNum;
	}
	
	/**
	 * ranking among sites google returned.
	 * in other words, of the n results that google returned, this is the number of the current one.
	 */
	public int resultNum()
	{
		return resultNum;
	}
	
	/**
	 * @return Returns the ResultDistributer.
	 */
	public SeedDistributor resultDistributer()
	{
		return resultDistributer;
	}
}
