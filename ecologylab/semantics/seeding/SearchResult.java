package ecologylab.semantics.seeding;

import ecologylab.generic.Debug;

public class SearchResult 
extends Debug
{
	/**
	 * ranking among sites google returned.
	 * in other words, of the n results that google returned, this is the number of the current one.
	 */
	int						resultNum;

	ResultDistributer	resultDistributer;
	
	public SearchResult(ResultDistributer resultDistributer, int resultNum)
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
	public ResultDistributer resultDistributer()
	{
		return resultDistributer;
	}
}
