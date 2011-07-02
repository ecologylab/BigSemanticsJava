/**
 * 
 */
package ecologylab.semantics.collecting;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericPrioritizedPool;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.PrioritizedPool;
import ecologylab.collections.WeightSet;
import ecologylab.generic.Debug;
import ecologylab.generic.Generic;
import ecologylab.generic.ThreadMaster;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;
import ecologylab.semantics.seeding.SemanticsPrefs;

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
implements Observer, ThreadMaster, Runnable, SemanticsPrefs
{
	SemanticsSessionScope													semanticsSessionScope;
	
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

	/**
	 * When the {@link #candidateTextSet candidateTextSet} and the {@link #candidateImgSet
	 * candidateImgSet} have more entries than this, they will be pruned.
	 */
	static final int									MAX_MEDIA				= 3072;

	public static final int			NUM_GENERATIONS_IN_MEDIA_POOL = 3; 

	static final int						MAX_MEDIA_PER_GENERATION			= MAX_MEDIA / NUM_GENERATIONS_IN_MEDIA_POOL;

	public static final int			NUM_GENERATIONS_IN_CONTAINER_POOL = 5; 
	public static final int			MAX_PAGES_PER_GENERATION	= MAX_PAGES / NUM_GENERATIONS_IN_CONTAINER_POOL ;
	/**
	 * Contains 3 visual pools. The first holds the first image of each container
	 */
	private final PrioritizedPool<DocumentClosure> 				candidateImagesPool;
	
	/**
	 * Contains 2 FloatWeightSet pools. 
	 * The first holds the first text surrogate of each container
	 */
	private final GenericPrioritizedPool<TextClipping> 	candidateTextClippingsPool;
	
	private final PrioritizedPool<DocumentClosure>			candidateContainersPool;
	

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
	 * Controls whether or not we periodically automatically download the Containers associated with
	 * hrefs that have been discovered. In other words, controls whether or not we crawl at all. Set
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


	/**
	 * 
	 */
	public Crawler()
	{
		TermVector piv 										= InterestModel.getPIV(); 
		piv.addObserver(this);
		
		//Similarly for text surrogates
		GenericWeightSet[] textWeightSets = { 
				new GenericWeightSet<TextClipping>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv)),
				new GenericWeightSet<TextClipping>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv)),
				new GenericWeightSet<TextClipping>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv))
		};
		candidateTextClippingsPool = new GenericPrioritizedPool<TextClipping>(textWeightSets);

		WeightSet<DocumentClosure>[] containerWeightSets = new WeightSet[NUM_GENERATIONS_IN_CONTAINER_POOL];
		
		for(int i = 0; i < NUM_GENERATIONS_IN_CONTAINER_POOL; i++)
				containerWeightSets[i] = new WeightSet<DocumentClosure>(MAX_PAGES_PER_GENERATION, this, 
					(TermVectorWeightStrategy) new DownloadContainerWeightingStrategy(piv));
		candidateContainersPool 	= new PrioritizedPool<DocumentClosure>(containerWeightSets);
		
		
		// Three pools for downloaded images      
		WeightSet<DocumentClosure>[] imageWeightSets	= new WeightSet[NUM_GENERATIONS_IN_MEDIA_POOL];
		for (int i = 0; i < NUM_GENERATIONS_IN_MEDIA_POOL; i++)
			imageWeightSets[i]	= new WeightSet<DocumentClosure>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv));
		candidateImagesPool = new PrioritizedPool<DocumentClosure>(imageWeightSets);
		
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
		candidateImagesPool.stop();

//		candidateImageVisualsPool.stop();
//		candidateFirstImageVisualsPool.stop();
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
	private void checkCandidateAncestorsForBetterOutlinks()
	{
		
		synchronized(candidateContainersPool)
		{
			WeightSet<DocumentClosure>[] candidateSet = (WeightSet<DocumentClosure>[]) candidateContainersPool.getWeightSets();
			int maxSize	= candidateContainersPool.maxSize();
			ArrayList<DocumentClosure> removeContainers = new ArrayList<DocumentClosure>(maxSize);
			ArrayList<DocumentClosure> insertContainers = new ArrayList<DocumentClosure>(maxSize);
			for(WeightSet<DocumentClosure> candidates : candidateSet)
			{
				for (DocumentClosure c : candidates)
				{
					Document ancestorDocument	= c.getDocument().getAncestor();
					if (ancestorDocument != null && !(ancestorDocument.isRecycled() /*|| ancestor.isRecycling() */))
					{
						if (ancestorDocument != null)
						{
							DocumentClosure d = ancestorDocument.swapNextBestOutlinkWith(c);
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
	 * Replace images in the candidates with possible better ones from their containers.
	 */
	private void checkCandidatesForBetterImagesAndText()
	{
		synchronized (candidateImagesPool)
		{
//			ImageClosure[] poolCopy				= (ImageClosure[]) candidateImagesPool.toArray();
			for (DocumentClosure imageClosure : candidateImagesPool)
			{
				//TODO -- check among all source documents!!!
				Image image								= (Image) imageClosure.getDocument();
				Document sourceDocument		= image.getClippingSource();
				if (sourceDocument != null && sourceDocument.isCompoundDocument())
					sourceDocument.tryToGetBetterImageAfterInterestExpression(imageClosure);
			}
		}
		synchronized (candidateTextClippingsPool)
		{
//			ArrayList<GenericElement<TextClipping>> tlist = new ArrayList<GenericElement<TextClipping>>(candidateTextClippingsPool.size());
//			for (GenericWeightSet<TextClipping> ws : (GenericWeightSet<TextClipping>[]) candidateTextClippingsPool.getWeightSets())
//				for (GenericElement<TextClipping> i : ws)
//					tlist.add(i);
			for (GenericElement<TextClipping> i : candidateTextClippingsPool)
			{
				TextClipping textClipping	= i.getGeneric();
				Document sourceDocument		= textClipping.getSource();
				if (sourceDocument != null)
					sourceDocument.tryToGetBetterTextAfterInterestExpression(i);
			}
		}
	}

	public void removeTextClippingFromPools(GenericElement<TextClipping> replaceMe)
	{
		candidateTextClippingsPool.remove(replaceMe);
	}

	public void removeImageClippingFromPools(DocumentClosure replaceMe)
	{
		candidateImagesPool.remove(replaceMe);
	}

	/**
	 * The number of potential candidate <code>MediaElement</code>s that
	 * could be displayed. Includes those not yet and those already downloaded.
	 */
	public int countPotentialMediaToDisplay()
	{
		return  candidateTextElementsSetSize() + numberOfImageReferences() +  imagePoolsSize();
	}

	/**
	 * Number of display-able <code>ImgElement</code>s that could be displayed.
	 * @param useCached 
	 */
	public final int imagePoolsSize()
	{
		return candidateImagesPool.size();
//		return candidateFirstImageVisualsPool.size() + candidateImageVisualsPool.size();
	}
	
	/**
	 * Number of images references.
	 */
	private int numImageReferences = 0;

	
	/**
	 * Number of candidate <code>ImgElement</code>s that could be downloaded.
	 */
	public final int numberOfImageReferences()
	{
		return numImageReferences;
	}

	/**
	 * Number of candidate <code>TextElement</code>s that could be displayed.
	 */
	public final int candidateTextElementsSetSize()
	{
		return candidateTextClippingsPool.size();
//		return candidateFirstTextElementsSet.size() + candidateTextElementsSet.size();
	}

	public static final int ALMOST_EMPTY_CANDIDATES_SET_THRESHOLD	= 5;
	
	public boolean candidateTextElementsSetIsAlmostEmpty()
	{
		return candidateTextClippingsPool.size() <= ALMOST_EMPTY_CANDIDATES_SET_THRESHOLD;
	}
	/**
	 * 
	 * @return	Weighted mean of the members of the candidateFirstTextElementsSet and the candidateTextElementsSet.
	 */
	public float candidateTextElementsSetsMean()
	{
		return candidateTextClippingsPool.mean();
	}

	public float imagePoolsMean()
	{
		return candidateImagesPool.mean();
	}

	@Override
	public void update(Observable o, Object arg)
	{
		checkCandidateAncestorsForBetterOutlinks();
		checkCandidatesForBetterImagesAndText();
	}
	public void increaseNumImageReferences()
	{
		// TODO Auto-generated method stub
		
	}

	public void decreaseNumImageReferences()
	{
		// TODO Auto-generated method stub
		
	}

	public void addTextClippingToPool(GenericElement<TextClipping> textClippingGE, int poolPriority)
	{
		candidateTextClippingsPool.insert(textClippingGE, poolPriority);
		
		InteractiveSpace interactiveSpace	= semanticsSessionScope.getInteractiveSpace();
		if (seeding.isPlayOnStart() && interactiveSpace != null)
			interactiveSpace.pressPlayWhenFirstMediaArrives();
	}

	public void addImageToPool(Image image)
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
						int maxPoolNum	= candidateContainersPool.numWeightSets() - 1;
						if (generation > maxPoolNum)
							generation		= maxPoolNum;
						//Very temporary console noise. Please tell me to remove this, if i haven't already
						 //currentThread = Thread.currentThread();
						// Thread.dumpStack();
						
						debugT("---\t---\t---\tAdding Container to candidateContainersPool: " + candidate 
								/* +  " ancestor=[" + candidate.ancestor() + "]" */);
						
						
						candidateContainersPool.insert(candidate, generation);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	void addImageClosureToPool(DocumentClosure imageClosure, CompoundDocument source)
	{
		// pressPlayWhenFirstMediaElementArrives();
		imageClosure.addContinuation(source);
//		visualPool.add(imageClosure);
//		imageClosure.s
	}
	public void removeCandidateContainer(DocumentClosure candidate)
	{
		if (candidate != null && !candidate.downloadHasBeenQueued() )
		{
			candidateContainersPool.remove(candidate);
		}
	}

	public int count()
	{
		return count;
	}

	/**
	 * Determines whether a given <code>Container</code> exceeds the <code>linkCount</code> thresholds.
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
			candidateImagesPool.pause();
			
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
			candidateImagesPool.pause();
			pause();
		}
	}   
	public void unpauseCollectingThreads()
	{
		if (collectingThreadsPaused)
		{
			collectingThreadsPaused	= false;
			candidateImagesPool.unpause();
			unpause();
		}
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

		candidateImagesPool.unpause();
		
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
		clearGlobalCollections();
	}
	
	public void clearGlobalCollections()
	{
		//TODO
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


}
