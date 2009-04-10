package ecologylab.semantics.seeding;

import java.util.ArrayList;
import java.util.Collections;

import ecologylab.documenttypes.Container;
import ecologylab.documenttypes.InfoCollector;
import ecologylab.generic.Debug;

/** 
 * Aggregate all results across multiple searches.
 * Then, make a arraylist structure with GoogleResult objects with Google ranking
 * <p/>
 * 
 * We want to keep track of:
 * 	number of searches that will report to us.
 *  has each search (s1) started?
 *  has each search (s1) finished?
 *  
 * @author eunyee
 * @author andruid
 *
 */
public class ResultDistributer<AC extends Container>
extends Debug
{
	InfoCollector			infoCollector;
   
	/**
	 * The number of search engine results to request, when processing
	 * a search specified during seeding.
	 */
	static final int 		TYPICAL_NUM_SEARCH_RESULTS	= 15;
	
	/**
	 * Each entry is a ArrayList/Vector that holds the slice of result i's gathered
	 * across different searches.
	 * The outer array list is in order, and may be sparsely populated 
	 * (that is, may have null entries in the middle).
	 * The inner resultSlice is not ordered, and will never contain null entries.
	 *  
	 */
	private final ArrayList<ArrayList<AC>>			resultSlices	= new ArrayList<ArrayList<AC>>();
	
	/**
	 * the total number of searches being conducted at once and aggregated
	 */
	private int 			totalSearches;
	
	/**
	 * Keep track of how deeply into the set of resultSlices that we're currently processing.
	 */
	private 	int			resultNumLevel	= 0;
    
	/**
	 * Expected the number of searches in the each result level. 
	 * This value is adjusted when the resultNumLevel goes up and when a seach downloading is done. 
	 */
    private		int			expectedNumSearchesInCurrentLevel;
    
    /**
     * Keep the number of results whenever each search is done,
     * and remove the slot when the resultNum has adjusted the expectedNumSearchesInCurrentLevel.
     */
    private		ArrayList<Integer>	resultNumOfDoneSearches		= new ArrayList<Integer>();
    
//    private	ArrayList		numSearchesPerSlice		= new ArrayList(TYPICAL_NUM_SEARCH_RESULTS);
    
    private final Object	DOWNLOAD_RESULTS_LOCK	= new Object();
	
	public ResultDistributer(InfoCollector infoCollector, int numSearches)
	{
		this.infoCollector						= infoCollector;
		this.totalSearches						= numSearches;
		this.expectedNumSearchesInCurrentLevel	= numSearches;
	}
	/**
	 * The ArrayList that corresponds to the ith result
	 * for all searches (that are involved).
	 * 
	 * @param i
	 * @return
	 */
	private synchronized ArrayList<AC> resultSlice(int i)
	{
		ArrayList<AC> thatSlice	= null;
		if (i >= resultSlices.size())
		{	// grow cause number of search results is larger than typical in some case
			thatSlice 		= createNewSlice(i);
		}
		else
		{
			thatSlice		= resultSlices.get(i);
			if (thatSlice == null)
			{
				thatSlice	= createNewSlice(i);
			}
		}
		return thatSlice;
	}
	private ArrayList<AC> createNewSlice(int sliceNum)
	{
		ArrayList<AC> thatSlice				= new ArrayList<AC>(totalSearches);			
		resultSlices.add(sliceNum, thatSlice);
		return thatSlice;
	}

	private boolean		processingDownloads;
	/**
	 * Download results from the array.
	 * The GoogleResult Object that ranked the highest in the array will download first. 
	 * @param onebyone true for downloading the container one by one method call
	 * 		  false for downloading all the containers left by one method call
	 */
	private void downloadResults()
	{
		synchronized (DOWNLOAD_RESULTS_LOCK)
		{
			processingDownloads		= true; // imperfect but useful mechanism of scheduling
	
			ArrayList<AC> currentSlice	= resultSlice(resultNumLevel);
			
			while ((currentSlice != null) && (currentSlice.size()>0))
			{
				AC result = currentSlice.remove(currentSlice.size() - 1);
				if (result != null)
				{
					/*
					debug("\n\n  Total: " + this.expectedNumSearchesInCurrentLevel + " Engine: "
							+ ((SearchState)result.seed()).getEngine()
							+ " SearchNum:" + result.searchNum() + " ResultNum:"+ resultNumLevel + 
							" Result PURL " + result.purl());
					*/
					result.queueDownload();
					queueCount ++;
					
					// reset the searchCount to 0 when the non-search page has been queued. 
					searchCount = 0;
				}
			}

			adjustExpectedNumSearches();

			if ( (this.expectedNumSearchesInCurrentLevel>0) && queueCount >= this.expectedNumSearchesInCurrentLevel )
			{	// current slice level complete; go to next
				resultNumLevel++;
				queueCount = 0;
				downloadResults();
			}
			processingDownloads		= false;

		}
	}
	
	/**
	 * Adjust the expected number of searches in the current level 
	 * based on the download done status of each search. 
	 */
	private void adjustExpectedNumSearches()
	{
		Integer temp;
		if( (resultNumOfDoneSearches.size()>0) &&
				(resultNumLevel >= (temp = Collections.min(resultNumOfDoneSearches)).intValue()) )
		{			
			expectedNumSearchesInCurrentLevel--;				
			this.resultNumOfDoneSearches.remove(temp);
		}
	}
	
	/**
	 * Counts how many searches are queued to the DownloadMonitor. 
	 * This is to control queuing the search and result pages in balanced manner to the DownloadMonitor.
	 * 
	 * This searchCount will be set to 0 when the non-search page become processed. 
	 */
	private int searchCount = 0;
	
	/**
	 * Limit the search pages to be queued to three at a time. 
	 */
	private final int controlSearchLimit = 3; 
	
	/**
	 * We dont want to queue too many search pages to the DownloadMonitor initially, because
	 * then we will not be able to show any surrogates to the user until all searches have run.
	 * Thus, we hold some later search Containers here while processing initial searches and
	 * one result Container from each.
	 * <p/>
	 * When the search pages have been queued to the DownloadMonitor up to a threshold (initially 3), 
	 * the next search page should not go straight to the DownloadMonitor.
	 * Instead, the first result pages from ResultSlice 1 are prioritized.
	 * The searches will stay here in the searchesDelayedUntilFirstResultsCanShow queue
	 * until a non-search result page has been processed. 
	 */
	ArrayList<AC> searchesDelayedUntilFirstResultsCanShow = new ArrayList<AC>();
	
	/**
	 * Queue search pages to DownloadMonitor if the search count is less than the limit number. 
	 * If it is larger than the limit number, add the search page to the waitingPipeToDownloadMonitor, 
	 * which will be stayed until the non-search page to be processed. 
	 * 
	 * @param searchContainer
	 */
	public void queueSearchRequest(AC searchContainer)
	{
		if( searchCount < controlSearchLimit )
		{
			// Queue search pages to DownloadMonitor that have been waiting till the non-search page to be processed. 
			if (downloadNextSearchIfThereIsOne())
				searchesDelayedUntilFirstResultsCanShow.add(searchContainer);
			else	// If there is nothing waiting, just queue search container to DownloadMonitor.
			{
				searchContainer.queueDownload();
			}
			
			// Increment searchCount that is counting search containers queued to DownloadMonitor. 
			searchCount++;
		}
		else
		{
			// If the searchCount is larger than the limit, queue non-search pages to DownloadMonitor.
			synchronized (DOWNLOAD_RESULTS_LOCK)
			{
			if( !this.processingDownloads )
				downloadResults();
			
			}
			// Add searchContainers to be processed to waiting pipeline for DownloadMonitor.
			searchesDelayedUntilFirstResultsCanShow.add(searchContainer);
		}
		
	}
	
	/**
	 * Count how many google results are removed from array structure in a same level(same PageRank).
	 */
	private int queueCount = 0;

	/**
	 * Queue Search Result here to be subsequently queued to a DownloadMonitor
	 * through the round robin across searches, as the proper slice levels are reached.
	 * 
	 * @param resultContainer
	 */
	public void queueResult(AC resultContainer)
	{	
		SearchResult searchResult	= resultContainer.searchResult();
		int resultNum				= searchResult.resultNum();	
		resultSlice(resultNum).add(resultContainer);

		if (!this.processingDownloads)
			downloadResults();
		
		downloadNextSearchIfThereIsOne();
	}
	/**
	 * Queue the next search, if there is one.
	 * 
	 * @return	true if there was a search to queue.
	 */
	private boolean downloadNextSearchIfThereIsOne()
	{
		synchronized (searchesDelayedUntilFirstResultsCanShow)
		{
			int waitingSize = searchesDelayedUntilFirstResultsCanShow.size();
			boolean result	= waitingSize > 0;
			if (result)
			{
				AC searchContainer	= searchesDelayedUntilFirstResultsCanShow.remove(waitingSize - 1);
				searchContainer.queueDownload();
			}
			return result;
		}

	}
	
	private int countingDone = 0;
	public void doneQueueing(int searchNum, int numResults, AC searchForNextRound)
	{
		// Keep the number of results generated by each search.
		resultNumOfDoneSearches.add(new Integer(numResults));
		countingDone++;
		downloadResults();		// download more if there is any more to download
		
		// Do Next Round of Searches
		if( countingDone >= totalSearches )
		{
			
		}
		
		if (searchForNextRound != null)
		{
			//TODO -- move the next round forward
		}
	}

	/** 
	* how many seed search result has processed so far.
	*/
	int seedProcessingNum = 0;
	
	/**
	 * set the  total number of searches being conducted at once and aggregated 
	 * @param seedTotal
	 */
	public void setTotalSearches(int seedTotal)
	{
///		Chage TotalSearches in case of Buzz!!! and also set searchNums!!!!!
//		Integrate other searches with Buzz!! and Buzz with Delicious.
		this.totalSearches 						= seedTotal;
		this.expectedNumSearchesInCurrentLevel 	= seedTotal;
	}
	
	/**
	 * the total number of searches being conducted at once and aggregated
	 * @return
	 */
	public int totalSearches()
	{
		return totalSearches;
	}
	
	public void reset()
	{
		this.resultNumLevel = 0;
		this.countingDone = 0;
		this.resultSlices.clear();
		this.resultNumOfDoneSearches.clear();
	}
}