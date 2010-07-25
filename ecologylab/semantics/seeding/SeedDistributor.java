package ecologylab.semantics.seeding;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.generic.Generic;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;

/**
 * now how SeedDistributor works:
 * <p />
 * searches: at most 2 searches are being processed (download and parse) at one time, in order not
 * to block traffic. when there are already 2 being processed, new searches will be put in a waiting
 * list.
 * <p />
 * search results: will be put into a priority queue when they come. will be processed by a consumer
 * thread one group by one group. group size is set to 1 at this time (i.e. each time 1 search
 * result is processed).
 * <p />
 * consumer thread: will be start when the first search request comes, and stopped when all the
 * searches and search results are processed. check the search waiting list to process waiting
 * searches at proper time. check the search results to process them. check for ending condition in
 * order to call endSeeding().
 * <p />
 * -- above updated 7/24/2010, Yin Qu
 * 
 * <p />
 * <p />
 * Aggregate all results across multiple searches and feeds. Interleave the results from each Seed.
 * Round-robin the scheduling of parsing each.
 * <p/>
 * We want to keep track of: number of searches that will report to us. has each search (s1)
 * started? has each search (s1) finished?
 * 
 * @author eunyee
 * @author andruid
 * @param <slice>
 */
public class SeedDistributor<AC extends Container> extends Debug implements Runnable,
		DispatchTarget<AC>
{

	public static interface DistributeCallBack<C extends QandDownloadable>
	{
		void distribute(C result);
	}

	/**
	 * initial size of the search result queue
	 */
	private static final int																INIT_CAPACITY											= 128;

	private static final int																MIN_INTERVAL_BTW_SEARCHES					= 1000;

	private static final int																MIN_INTERVAL_BTW_QUEUE_PROCESSING	= 1000;

	/**
	 * to generate surrogates as soon as possible, during each queue processing we will process the
	 * top NUM_RESULTS_PROCESSED_EACH_TIME search results.
	 */
	private static final int																NUM_RESULTS_PROCESSED_EACH_TIME		= 1;

	/**
	 * limit the number of searches being downloaded at one time in order to prevent blocking the
	 * traffic by searches, since some search engine limits the rate we can access them, and we have
	 * only 4 threads for downloading during seeding.
	 */
	private static final int																MAX_NUM_SEARCHES_PROCESSING				= 2;

	private InfoCollector																		infoCollector;

	/**
	 * number of searches that we have to queue and process in total
	 */
	private int																							numSearchesToQueue								= 0;

	/**
	 * number of searches that have been queued to DownloadMonitor, but not yet finished
	 */
	private int																							numSearchesProcessing							= 0;

	/**
	 * number of searches that have been finished (will call doneQueueing()). track this number to
	 * decide when to finish seeding.
	 */
	private int																							numSearchesDone										= 0;

	/**
	 * a waiting list for search requests, in case that there are already MAX_NUM_SEARCHES_PROCESSING
	 * searches in processing.
	 */
	private final Queue<AC>																	waitingSearches										= new LinkedList<AC>();

	/**
	 * the comparator to decide the order of search results to be processed. can be customized through
	 * constructor. by default, search results will be ordered according to their ranks in the search
	 * result list.
	 */
	private final Comparator<QandDownloadable>							comparator;

	/**
	 * The priority queue holding (weighted) search results waiting for downloading and parsing. Note
	 * that PriorityQueue is not synchronized.
	 */
	private final PriorityQueue<QandDownloadable>						queuedResults;

	private final Map<QandDownloadable, DistributeCallBack>	callbackMap												= new HashMap<QandDownloadable, SeedDistributor.DistributeCallBack>();

	private long																						lastSearchTimestamp;

	private long																						lastQueueProcessingTimestamp;

	private boolean																					started														= false;

	private boolean																					stopFlag;

	public SeedDistributor(InfoCollector infoCollector, Comparator<QandDownloadable> comparator)
	{
		this.infoCollector = infoCollector;
		this.comparator = comparator;
		this.queuedResults = new PriorityQueue<QandDownloadable>(INIT_CAPACITY, comparator);
	}

	public SeedDistributor(InfoCollector infoCollector)
	{
		this(infoCollector, new Comparator<QandDownloadable>()
		{

			@Override
			public int compare(QandDownloadable o1, QandDownloadable o2)
			{
				int i1 = getRank(o1);
				int i2 = getRank(o2);
				return i1 - i2;
			}

		});
	}

	/**
	 * Queue a search request to the SeedDistributor. The request might be put to a waiting list in
	 * order not to block the traffic.
	 * 
	 * @param searchContainer
	 */
	public void queueSearchRequest(AC searchContainer)
	{
		numSearchesToQueue++;
		if (numSearchesProcessing >= MAX_NUM_SEARCHES_PROCESSING)
		{
			synchronized (waitingSearches)
			{
				waitingSearches.offer(searchContainer);
			}
		}
		else
		{
			downloadSearchRequest(searchContainer);
		}

		if (!started)
		{
			start();
		}
	}

	/**
	 * Actually process a search request (send it to DownloadMonitor). Will wait for some time after
	 * each processing.
	 * 
	 * @param searchContainer
	 */
	public void downloadSearchRequest(AC searchContainer)
	{
		searchContainer.queueDownload();
		numSearchesProcessing++;
		debug("sending search request to DownloadMonitor: " + searchContainer);

		waitForAtMost(lastSearchTimestamp, MIN_INTERVAL_BTW_SEARCHES);
		lastSearchTimestamp = System.currentTimeMillis();
	}

	/**
	 * Called whenever a search is downloaded and parsed.
	 * 
	 * @param searchContainer
	 * @param searchNum
	 * @param numResults
	 */
	public void doneQueueing(Container searchContainer)
	{
		debug("search parsed: " + searchContainer);
		numSearchesProcessing--;
		numSearchesDone++;
	}

	/**
	 * Queue a search result to the queue. Queued search results are interleaved and processed by the
	 * consumer thread.
	 * 
	 * @param resultContainer
	 */
	public void queueResult(QandDownloadable resultContainer)
	{
		queueResult(resultContainer, null);
	}

	/**
	 * Queue a search result to the queue, with a specific callback method. The callback method will
	 * be called when the result is distributed (polled from the queue and processed), instead of
	 * calling queueDownload() on the result.
	 * 
	 * @param resultContainer
	 * @param callback
	 */
	public void queueResult(QandDownloadable resultContainer, DistributeCallBack callback)
	{
		synchronized (queuedResults)
		{
			debug("queuing result: " + resultContainer);
			queuedResults.offer(resultContainer);
			if (callback != null)
			{
				callbackMap.put(resultContainer, callback);
			}
		}
	}

	/**
	 * Process one group of search results. The size of one group is controlled by
	 * NUM_RESULTS_PROCESSED_EACH_TIME.
	 */
	private void downloadResults()
	{
		int i = 0;
		while (queuedResults.size() > 0 && i < NUM_RESULTS_PROCESSED_EACH_TIME)
		{
			synchronized (queuedResults)
			{
				if (queuedResults.size() > 0)
				{
					QandDownloadable downloadable = queuedResults.poll();
					String query = getQuery(downloadable);
					int rank = getRank(downloadable);
					debug(String.format("sending container to DownloadMonitor: [%s:%d]%s", query, rank,
							downloadable));
					downloadable.setDispatchTarget(this);
					if (callbackMap.containsKey(downloadable))
					{
						callbackMap.get(downloadable).distribute(downloadable);
						callbackMap.remove(downloadable);
					}
					else
					{
						downloadable.queueDownload();
					}
					i++;
				}
			}
		}
	}

	private static int getRank(QandDownloadable downloadable)
	{
		int r = -1;
		if (downloadable instanceof Container)
		{
			Container container = (Container) downloadable;
			r = container.searchResult() == null ? -2 : container.searchResult().resultNum();
		}
		return r;
	}

	private static String getQuery(QandDownloadable downloadable)
	{
		String q = null;
		if (downloadable instanceof Container)
		{
			Container container = (Container) downloadable;
			q = container.getSeed() == null ? null : container.getSeed().getQuery();
		}
		return q == null ? "" : q;
	}

	/**
	 * Start the consumer thread.
	 */
	public void start()
	{
		if (!started)
		{
			stopFlag = false;
			debug("starting seed distributor consumer thread ...");
			Thread t = new Thread(this, toString() + " consumer");
			t.start();
			started = true;
		}
	}

	/**
	 * Stop the consumer thread.
	 */
	public void stop()
	{
		stopFlag = true;
		debug("stopping seed distributor consumer thread ...");
	}

	/**
	 * Reset the SeedDistributor.
	 */
	public void reset()
	{
		this.numSearchesToQueue = 0;
		this.numSearchesProcessing = 0;
		this.numSearchesDone = 0;
	}

	private void waitForAtMost(long timestamp, int minMillis)
	{
		if (timestamp == 0)
		{
			// first time, no need to wait
			return;
		}

		int wait = (int) (System.currentTimeMillis() - timestamp);
		if (wait < 0)
			wait = 0;

		if (wait < minMillis)
		{
			int sleepMillis = minMillis - wait;
			Generic.sleep((int) sleepMillis);
			debug("waiting (in milliseconds): " + sleepMillis);
		}
	}

	/**
	 * The consumer thread. It will be started when the first search request comes, and activated
	 * every MIN_INTERVAL_BTW_QUEUE_PROCESSING milliseconds.
	 * <p />
	 * During each activation, it will first check if there are waiting search requests that could be
	 * processed. If there are, it will poll one from the queue and process it. If there are not, it
	 * will process a group of search results from the priority queue.
	 * <p />
	 * At last, it will check for the ending condition in order to call endSeeding() when all the
	 * seeds have been processed.
	 */
	@Override
	public void run()
	{
		while (!stopFlag)
		{
			waitForAtMost(lastQueueProcessingTimestamp, MIN_INTERVAL_BTW_QUEUE_PROCESSING);
			lastQueueProcessingTimestamp = System.currentTimeMillis();

			if (numSearchesProcessing < MAX_NUM_SEARCHES_PROCESSING && waitingSearches.size() > 0)
			{
				synchronized (waitingSearches)
				{
					if (waitingSearches.size() > 0)
					{
						AC search = waitingSearches.poll();
						downloadSearchRequest(search);
					}
				}
			}
			else
			{
				downloadResults();
			}

			checkForEndSeeding();
		}

		started = false;
	}

	/**
	 * Check for ending conditions and call endSeeding() at the right time. The ending condition is:
	 * all the searches that are queued have been downloaded and parsed, and all the search results
	 * have been processed.
	 */
	private void checkForEndSeeding()
	{
		int numResultsRemaining = 0;
		synchronized (queuedResults)
		{
			numResultsRemaining = queuedResults.size();
		}
		debug(String.format(
				"checking for endSeeding(): toQueue=%d, processing=%d, done=%d, remaining results=%d",
				numSearchesToQueue, numSearchesProcessing, numSearchesDone, numResultsRemaining));
		if (numSearchesDone == numSearchesToQueue && numSearchesProcessing == 0
				&& numResultsRemaining == 0)
		{
			System.out
					.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			System.out
					.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			System.out
					.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			System.out
					.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			infoCollector.endSeeding();
			stop();
		}
	}

	/**
	 * (Currently for debugging only.)
	 */
	@Override
	public void delivery(AC o)
	{
		if (!o.isRecycled())
		{
			debug("done downloading: " + o);
		}
	}

}