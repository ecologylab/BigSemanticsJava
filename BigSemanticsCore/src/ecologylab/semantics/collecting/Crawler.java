/**
 * 
 */
package ecologylab.semantics.collecting;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.collections.GenericElement;
import ecologylab.collections.PrioritizedPool;
import ecologylab.collections.WeightSet;
import ecologylab.generic.Debug;
import ecologylab.generic.Generic;
import ecologylab.generic.ThreadMaster;
import ecologylab.semantics.documentparsers.CompoundDocumentParserCrawlerResult;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;
import ecologylab.semantics.seeding.SemanticsPrefs;

/**
 * Crawler will encapsulate all state regarding web crawling. 
 * This means WeightSets and PriorizedPools of candidate unparsed simple Document + CompoundDocument objects.
 * 
 * @author andruid
 */
public class Crawler extends Debug
implements Observer, ThreadMaster, Runnable, SemanticsPrefs
{
	protected SemanticsSessionScope						semanticsSessionScope;
	
	Seeding																		seeding;
	
	/**
	 * Priority to run at when we seem to have an overabundance of <code>MediaElement</code>s ready
	 * for display.
	 */
	protected static final int									LO_PRIORITY										= 3;

	/**
	 * Priority to run at normally.
	 */
	protected static final int									MID_PRIORITY									= 4;

	/**
	 * Priority to run at when we do not have enough <code>MediaElement</code>s ready for display.
	 */
	protected static final int									HI_PRIORITY										= 5;

	/**
	 * Initial priority, which is {@link #LO_PRIORITY LO_PRIORITY}, because we expect to need to do a
	 * bunch of crawling at the start, since our global collections will be empty.
	 */
	protected static final int									PRIORITY											= MID_PRIORITY;

	/**
	 * A constant for initializing our large HashTables.
	 */
	static final float								HASH_LOAD				= .5f;

	/**
	 * When the candidateContainersPool has more entries than this, it will be pruned.
	 */
	// static final int MAX_PAGES = 2048;
	static final int									MAX_PAGES				= 4096;																			// 3072;

	public static final int			NUM_GENERATIONS_IN_CONTAINER_POOL = 5; 
	public static final int			MAX_PAGES_PER_GENERATION	= MAX_PAGES / NUM_GENERATIONS_IN_CONTAINER_POOL ;

	private final PrioritizedPool<DocumentClosure>			candidateDocumentClosuresPool;
	

	/**
	 * true when the session is ending.
	 */
	protected boolean														finished;

	/**
	 * Controls whether or not the crawler is (temporarily) paused.
	 */
	protected boolean														running												= true;
	
	boolean																			crashed;

	/**
	 * Web crawler sleep time when we are in need of collecting more media.
	 */
	protected final int													usualSleep;

	/**
	 * Web crawler sleep time when there seems to be plenty of media already collected.
	 */
	protected final int													longSleep;

	/**
	 * Controls whether or not we periodically automatically download the DocumentClosures associated with
	 * outlinks that have been discovered. In other words, controls whether or not we crawl at all. Set
	 * as a preference at runtime, and via a menu entry.
	 */
	protected PrefBoolean												downloadLinksAutomatically		= CRAWL;

	// +++++++++++ state for thread +++++++++++ //
	/**
	 * The web crawler thread.
	 */
	protected Thread														thread;

	/**
	 * The number of loops through the web crawler. A performance statistic that roughly corresponds
	 * to how many new <code>Container</code>s have been queued for download and parse, during this
	 * session.
	 */
	protected int																count;

	protected boolean														collectingImages;
	
	public boolean isCollectingImages()
	{
		return collectingImages;
	}

	public boolean isCollectingText()
	{
		return collectingText;
	}
	protected boolean														collectingText;

	/**
	 * 
	 */
	public Crawler()
	{
		TermVector piv 										= InterestModel.getPIV(); 
		piv.addObserver(this);
		
		WeightSet<DocumentClosure>[] documentClosureWeightSets = new WeightSet[NUM_GENERATIONS_IN_CONTAINER_POOL];
		
		for(int i = 0; i < NUM_GENERATIONS_IN_CONTAINER_POOL; i++)
				documentClosureWeightSets[i] = new WeightSet<DocumentClosure>(MAX_PAGES_PER_GENERATION, this, 
					(TermVectorWeightStrategy) new DownloadContainerWeightingStrategy(piv));
		candidateDocumentClosuresPool 	= new PrioritizedPool<DocumentClosure>(documentClosureWeightSets);
		
		
		finished		= false;
		usualSleep	= 3000;
		longSleep		= usualSleep * 5 / 2;

	}

	/**
	 * Stop the threads that are responsible for collecting new surrogates.
	 * Includes the candidateImageVisualsPool, crawler and seeding download monitors.
	 * @param kill
	 */
	public void stopCollectingAgents(boolean kill)
	{
		semanticsSessionScope.getDownloadMonitors().stop(kill);
	}
	final Object startCrawlerSemaphore	= new Object();
	//FIXME
	public synchronized void start()
	{
		if (downloadLinksAutomatically.value())
		{
			if (!crashed && (thread == null) && !seeding.isDuringSeeding())
			{
				debug("Starting up.");
				//	 Thread.dumpStack();
				finished	= false;
				thread = new Thread(this, "InfoCollector");
				//ThreadDebugger.registerMyself(thread);	
				Generic.setPriority(PRIORITY);
				thread.start();
			}
			else
			{
				unpause();
			}
		}
	}

	public synchronized void pause()
	{
		if (thread != null)
		{
			debug("pause()");
			running = false;
		}
	}

	public synchronized void unpause()
	{
//		if ((thread != null) && !running && downloadLinksAutomatically)
		if ((thread != null) && downloadLinksAutomatically.value() && !seeding.isDuringSeeding())
		{
			debug("unpause()");
			running	= true;
			notifyAll();
		}
	}

	/**
	 * 
	 */
	private void checkCandidatesParserResultsForBetterOutlinks()
	{
		synchronized(candidateDocumentClosuresPool)
		{
			WeightSet<DocumentClosure>[] candidateSet = (WeightSet<DocumentClosure>[]) candidateDocumentClosuresPool.getWeightSets();
			int maxSize	= candidateDocumentClosuresPool.maxSize();
			ArrayList<DocumentClosure> removeContainers = new ArrayList<DocumentClosure>(maxSize);
			ArrayList<DocumentClosure> insertContainers = new ArrayList<DocumentClosure>(maxSize);
			for(WeightSet<DocumentClosure> candidates : candidateSet)
			{
				for (DocumentClosure c : candidates)
				{
					Document ancestorDocument	= c.getDocument().getAncestor();
					if (ancestorDocument != null && !(ancestorDocument.isRecycled() /*|| ancestor.isRecycling() */))
					{
						CompoundDocumentParserCrawlerResult crawlerResult	= (CompoundDocumentParserCrawlerResult) ancestorDocument.getParserResult();
						if (crawlerResult != null)
						{
							DocumentClosure d = crawlerResult.swapNextBestOutlinkWith(c);
							if (d != null && c != d)
							{
								removeContainers.add(c);
								insertContainers.add(d); 
							}
						}
					}
				}
				int swapSize = insertContainers.size();
				if(swapSize > 0)
					System.out.println("Swapping Containers:\n\tReplacing " + swapSize);
				for (DocumentClosure c : removeContainers)
					candidates.remove(c);
				removeContainers.clear();
				// Insertion directly into weighset is Ok, 
				// because the replacement container will have the same generation
				for (DocumentClosure c : insertContainers)
					candidates.insert(c);	
				insertContainers.clear();
			}
		}
	}
	
	/**
	 * Blank implementation in base class.
	 * 
	 * @param replaceMe		TextClipping to remove
	 */
	public void removeTextClippingFromPools(GenericElement<TextClipping> replaceMe)
	{
		
	}

	/**
	 * Blank implementation in base class.
	 * 
	 * @param replaceMe		Image to Remove
	 */
	public void removeImageClippingFromPools(DocumentClosure replaceMe)
	{
		
	}

	/**
	 * Always return 0 here in the base class.
	 */
	public int imagePoolsSize()
	{
		return 0;
	}

	/**
	 * Used to assess how much need we have for more TextClippings.
	 * 
	 * @return	false	in base class implementation.
	 */
	public boolean candidateTextClippingsSetIsAlmostEmpty()
	{
		return false;
	}
	
	/**
	 * Collects TextClipping based on its weight and if it is the first representative for that CompoundDocument.
	 * @param numSurrogatesCollectedFromCompoundDocument	
	 * @param clippingPoolPriority TODO
	 * @param textClipping	TextClipping to potentially collect
	 * 
	 * @return	always false in this base class implementation, because we do not collect TextClippings.
	 */
	public boolean collectTextClippingIfWorthwhile(GenericElement<TextClipping> textClippingGE, int numSurrogatesCollectedFromCompoundDocument, int clippingPoolPriority)
	{
		return false;
	}

	/**
	 * This is an Observer of changes in the TermVectors, which change when the interest model changes.
	 * 
	 * When the interest model changes, we iterate through candidate DocumentClosures to see if they have a better link
	 * to contribute to our global crawler state.
	 */
	@Override
	public void update(Observable o, Object arg)
	{
		checkCandidatesParserResultsForBetterOutlinks();
	}
	public void increaseNumImageReferences()
	{
		// TODO Auto-generated method stub
		
	}

	public void decreaseNumImageReferences()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Base class implementation does nothing.
	 * @param textClippingGE
	 * @param poolPriority
	 */
	public void addTextClippingToPool(GenericElement<TextClipping> textClippingGE, int poolPriority)
	{
	
	}
	
	public boolean addDocumentToPool(Document document)
	{
		return addClosureToPool(document.getOrConstructClosure());
	}
	public boolean addClosureToPool(DocumentClosure candidate)
	{
		if (candidate != null)
		{	
			synchronized(candidate)
			{
				if (candidate.isUnprocessed() && !candidate.isSeed())
				{
					Document document	= candidate.getDocument();
					if (!exceedsLinkCountThresholds(document))
					{
						int generation 		= document.getEffectiveGeneration();
						int maxPoolNum	= candidateDocumentClosuresPool.numWeightSets() - 1;
						if (generation > maxPoolNum)
							generation		= maxPoolNum;
						
						debugT("---\t---\t---\tAdding Container to candidateContainersPool: " + candidate 
								/* +  " ancestor=[" + candidate.ancestor() + "]" */);					
						
						candidateDocumentClosuresPool.insert(candidate, generation);
						return true;
					}
				}
			}
		}
		return false;
	}
	
//	public void removeCandidateContainer(DocumentClosure candidate)
//	{
//		if (candidate != null && !candidate.downloadHasBeenQueued() )
//		{
//			candidateContainersPool.remove(candidate);
//		}
//	}

	/**
	 * Determines whether a given <code>Document</code> exceeds the <code>linkCount</code> thresholds.
	 * Crawling too many links without seeing that the user is interested tends to lead to noisy content.
	 * <p/>
	 * Current thresholds are as follows:
	 * <ul>
	 * <li><code>linkCount</code> of 2 if link is to a new domain</li>
	 * <li><code>linkCount</code> of 4 if link is to the same domain</li>
	 * </ul>
	 * These thresholds are overridden if the user has expressed interest in surrogates from this particular <code>Container</code>.
	 *
	 * @param		document		the <code>Container</code> to evaluate for thresholds
	 * @return				<code>true</code> if the element's <code>linkCount</code> exceeds thresholds
	 */
	public boolean exceedsLinkCountThresholds(Document document)
	{
//		debug("---------exceedsLinkCountThresholds---------");

		int					linkCount			= document.getGeneration();
		boolean sameDomainAsPrevious 				= document.isSameDomainAsPrevious();
		short participantInterestIntensity 		= InterestModel.getInterestExpressedInTermVector(document.termVector());

		if (linkCount <= 2)
		{
//			debug("---ACCEPT1: intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
			return false;
		}
		else if (linkCount <= 4 && sameDomainAsPrevious)
		{
//			debug("---ACCEPT2: intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
			return false;
		}
		else if (participantInterestIntensity > 0)					// TODO: make sure participant interest is being kept up
		{
//			debug("---ACCEPT3: intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
			return false;
		}
		else
		{
			//debug("--- exceedsLinkCountThresholds() REJECT : intensity: " + participantInterestIntensity + " link count: " + linkCount + " same domain: " + sameDomainAsPrevious);
//			System.out.println("Ancestors Interest = " + element.ancestor().participantInterest().intensity() );
			return true;
		}
	}

	
	
	/**
	 * Message to the user when the crawler stops because there's no place
	 * left to crawl to.
	 */
	static final String TRAVERSAL_COMPLETE_MSG =
		"The web crawler is pausing:\ntraversal of the information space is complete.\nThere are no more traversable pages to crawl to.";
	
	boolean threadsArePaused;
	boolean collectingThreadsPaused;   
	/**
	 * Pause all the threads we know about.
	 * 
	 * @return	true if threads exist and were not paused.
	 * 			Enables paused-ness state to be restored.
	 */
	@Override
	public boolean pauseThreads()
	{
		boolean needToPause	= (thread != null) && !threadsArePaused;
		if (needToPause)
		{
			threadsArePaused	= true;
			//debug("Container.pauseThreads()");
			InteractiveSpace interactiveSpace	= semanticsSessionScope.getInteractiveSpace();

			if (interactiveSpace != null)
				interactiveSpace.pauseIfPlaying();
			pauseThreadsExceptCompositionAgent();
			if (interactiveSpace != null)
				interactiveSpace.waitIfPlaying();
		}
		return needToPause;
	}

	boolean	threadsExceptCompositionArePause;
	/**
	 * 
	 */
	public boolean pauseThreadsExceptCompositionAgent()
	{
		boolean needToPause	= (thread != null) && !threadsExceptCompositionArePause;
		if (needToPause)
		{
			pause();
			InteractiveSpace interactiveSpace	= semanticsSessionScope.getInteractiveSpace();

			if (interactiveSpace != null)
				interactiveSpace.pausePipeline();
			pauseImageCollecting();
			
			semanticsSessionScope.getDownloadMonitors().pauseRegularDownloadMonitors();

			threadsExceptCompositionArePause = true;
		}
		return needToPause;
	}
	public void pauseCollectingThreads()
	{
		if ((thread != null) && !collectingThreadsPaused)
		{
			collectingThreadsPaused	= true;
			pauseImageCollecting();
			pause();
		}
	}   
	public void unpauseCollectingThreads()
	{
		if (collectingThreadsPaused)
		{
			collectingThreadsPaused	= false;
			unpauseImageCollecting();
			unpause();
		}
	}
	
	/**
	 * Pause the candidate Images collecting thread.
	 * 
	 * Base class implementation does nothing.
	 */
	protected void pauseImageCollecting()
	{
		
	}
	
	/**
	 * Unpause the candidate Images collecting thread.
	 * 
	 * Base class implementation does nothing.
	 */
	protected void unpauseImageCollecting()
	{
		
	}
	
	public String getThreadStatuses()
	{
		return "ThreadsPaused = AllThreads("+ threadsArePaused +  ") " +
										"Collect(" + collectingThreadsPaused + ") " + 
										"NonComposition(" + threadsExceptCompositionArePause + ")";
	}
	/**
	 * Unpause (continue) all the threads we know about.
	 */
	@Override
	public void unpauseThreads()
	{
		if (threadsArePaused)
		{
			threadsArePaused	= false;
			//debug("Container.unpauseThreads() crawlerDownloadMonitor waitingToDownload="+
			//      crawlerDownloadMonitor.waitingToDownload());
			
			InteractiveSpace interactiveSpace	= semanticsSessionScope.getInteractiveSpace();
			if (interactiveSpace != null)
			{
				interactiveSpace.restorePlayIfWasPlaying();
				interactiveSpace.unpausePipeline();
			}
			
			unpauseNonCompositionThreads();
		}
		else if(threadsExceptCompositionArePause)
		{
			unpauseNonCompositionThreads();
		}
	}

	private void unpauseNonCompositionThreads()
	{
		threadsExceptCompositionArePause = false;

		unpauseImageCollecting();
		
		semanticsSessionScope.getDownloadMonitors().unpauseRegularDownloadMonitors();
		unpause();
	}
	public void stop()
	{
		stop(false);
	}
	public void stop(boolean kill)
	{
		if (!finished)
			finished	= true;

		stopCollectingAgents(kill);
		
		semanticsSessionScope.getDownloadMonitors().stop(kill);

		// clear all the collections when the CF browser exits -- eunyee
		//ThreadDebugger.clear();
		clearCollections();
	}
	
	/**
	 * Clear the candidateDocumentClosuresPool.
	 * 
	 */
	public void clearCollections()
	{
		candidateDocumentClosuresPool.clear();
	}

	public boolean isOn()
	{
		return running && downloadLinksAutomatically.value();
	}

	/**
	 * Crawler
	 */
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		
	}
	// ------------------- Thread related state handling ------------------- //
	//
	protected synchronized void waitIfNotRunning()
	{
		if (!running || !downloadLinksAutomatically.value())
		{
			try
			{
				debug("waitIfOff() waiting");
				wait();
				running = true;
			}
			catch (InterruptedException e)
			{
				debug("run(): wait interrupted: ");
				e.printStackTrace();
				Thread.interrupted(); // clear the interrupt
			}
		}
	}

	/**
	 * @return the seeding
	 */
	public Seeding getSeeding()
	{
		return seeding;
	}

	/**
	 * @param seeding the seeding to set
	 */
	public void setSeeding(Seeding seeding)
	{
		this.seeding = seeding;
	}

	/**
	 * Construct a CompoundDocument ParserResult object of type that matches this crawler.
	 * 
	 * @param compoundDocument	Document that is parsed.
	 * @param justCrawl					True if we should not collect Images and TextClippings, even if we could.
	 * 
	 * @return	CompoundDocumentParserCrawlerResult
	 */
	public CompoundDocumentParserCrawlerResult 
	constructCompoundDocumentParserResult(CompoundDocument compoundDocument, boolean justCrawl)
	{
		return new CompoundDocumentParserCrawlerResult(compoundDocument);
	}
	
	public void killSite(final SemanticsSite site)
	{
		ArrayList<DocumentClosure> removalSet = new ArrayList<DocumentClosure>();
		int poolNum = 0;
		for(WeightSet<DocumentClosure> set : candidateDocumentClosuresPool.getWeightSets())
		{
			removalSet.clear();
			for(DocumentClosure documentClosure : set)
				if(documentClosure.isFromSite(site))
					removalSet.add(documentClosure);
			if(removalSet.size() > 0)
			{
				debug("Removing " + removalSet.size() + " candidate documentClosures from " + set);
				for(DocumentClosure toRemove : removalSet)
					set.remove(toRemove);
			}
			else
				debug("No DocumentClosures to remove from poolNum: " + poolNum++ + " :" + set);
		}
	}
}
