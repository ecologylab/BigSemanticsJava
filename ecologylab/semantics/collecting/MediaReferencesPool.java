/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.appframework.Memory;
import ecologylab.appframework.OutOfMemoryErrorHandler;
import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.collections.PrioritizedPool;
import ecologylab.collections.RunnablePool;
import ecologylab.collections.WeightSet;
import ecologylab.concurrent.Monitor;
import ecologylab.generic.ConsoleUtils;
import ecologylab.generic.Generic;
import ecologylab.semantics.metadata.builtins.ImageClosure;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;

/**
 * A PrioritizedPool of references to Media, like Images.
 * A thread wakes up periodically and parses the top ranked one.
 * 
 * @author andruid
 */
public class MediaReferencesPool extends PrioritizedPool<ImageClosure>
	implements Runnable, RunnablePool
	{
	/**
	 * Maximum number of prefetched images to hold on to.
	 */
	   static final int	POOL_SIZE	= 300;
	   static final int	EMPTY_SLEEP	= 900;
	   static final int	MID_SLEEP	= 1500;
	   static final int	FULL_SLEEP	= 3000;
	   static final int	EXTRA_SLEEP	= 15000;

	   Thread				thread;
	   int					priority;
	   /**
	    * How long the agent plans to sleep for in the current / next iteration.
	    * Also used as a comparison to see if sleep time has been decreased.
	    */
	   int 					sleep;
	   
	   private NewInfoCollector	infoCollector;

	   boolean				paused;
	   
	   static final int	LOW_PRIORITY	= NewInfoCollector.IMAGE_DOWNLOAD_MONITOR.lowPriority();
//     PixelBased.pixelBasedDownloadMonitor.lowPriority();


	   public static final int MID_PRIORITY	= LOW_PRIORITY + 1;
	   public static final int HIGH_PRIORITY= MID_PRIORITY + 1;;

	   /**
	    * This lock is for sleeping. prune() takes the lock on this, so we need a separate one.
	    */
	   final Object			sleepLock	= new Object();
		    
	   /**
	    * This lock is for start, stop, pause, and unpause. 
	    * prune() takes the lock on <code>this</code>, so we need a separate one here.
	    */
	   final Object			startAndStopLock	= new Object();
		    
	/**
	 * Controls whether or not we periodically automatically download the 
	 * ImgElements that have been discovered. In other words, controls
	 * whether or not we collect images that we know about.
	 * Set as a preference at runtime, and via
	 * a menu entry.
	 */
	   PrefBoolean downloadImagesAutomatically	= Pref.usePrefBoolean("download_images_automatically", true);

	   public MediaReferencesPool(int numSets, int maxSetSize, TermVector piv, NewInfoCollector infoCollector)
	   {
	      super();
	      this.infoCollector	= infoCollector;
	      
	  		weightSets					= new WeightSet[numSets];
	  		//TODO -- should there be a special weighting strategy here?!
	  		for (int i = 0; i < numSets; i++)
	  			weightSets[i]			= new WeightSet<ImageClosure>(maxSetSize, infoCollector, new TermVectorWeightStrategy(piv));  		
	      	
	   }

	   /**
	    * Pause the image collecting agent.
	    */
	   public void pause()
	   {
		   synchronized (startAndStopLock)
		   {
			   paused	= true;
		   }
	   }
	   /**
	    * Unpause the image collecting agent.
	    */
	   public void unpause()
	   {
		   synchronized (startAndStopLock)
		   {
			   paused	= false;
			   startAndStopLock.notify();
		   }
	   }
	   /**
	    * This is the main loop of the image collecting agent.
	    */
	   public void run()
	   {
	      try
	      {
		      while (thread != null)
		      {
//		      	 ThreadDebugger.waitIfPaused(this.thread);
		
				 synchronized (startAndStopLock)
				 {
				    if (paused || !downloadImagesAutomatically.value())
				    {
				       Monitor.wait(startAndStopLock);
				       //debug("After wait!");
				    }
				 }
			
				 int sleep = adjustPriorityAndSleepTime();
				 
				 NewInfoCollector.IMAGE_DOWNLOAD_MONITOR.waitIfTooManyPending();
				 
				 if (!Memory.reclaimIfLow())
				 {
					 // changed to use imgElement not ImgVisual 2/20/08 by andruid
					 
					 //TODO -- download images here!!!
					 
//					 ImageElement imgElement	= infoCollector.selectImgReference();
//					 if (imgElement != null)
//						 imgElement.download(infoCollector.maxAgentImageDimension());
				 }
				 
				 synchronized (sleepLock)
				 {
					try
					{
						sleepLock.wait(sleep);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				 }
		      }
	      }catch (OutOfMemoryError e)
	      {
	      	stop();
	      	ConsoleUtils.obtrusiveConsoleOutput("BLANKET OutOfMemory in VisualPool");
	      	OutOfMemoryErrorHandler.handleException(e);
	      }
	   }

	   /**
	    * Check to see if we're low on images to present to the user.
	    * Adjust the priority of our agent thread based on this.
	    * 
	    * @param downloadMonitor
	    * 
	    * @return true if our priority has been increased
	    */
	   private int adjustPriorityAndSleepTime()
	   {
		   int size	= size();
		   int sleep	= 
			   (size < 6) && (NewInfoCollector.IMAGE_DOWNLOAD_MONITOR.waitingToDownload() < 4) ? EMPTY_SLEEP : 
				   (size < 15) ? MID_SLEEP : 
					   (size < 30) ? FULL_SLEEP : EXTRA_SLEEP;
		   if (thread != null)
		   {
			   if (size > 10)
				   thread.setPriority(LOW_PRIORITY);
			   else if (size < 5)
				   thread.setPriority(MID_PRIORITY);
		   }
		   // else we're in the midst of shutdown!
		   
		   return sleep;
	   }
	   
	   void adjustPriorityAndWakeupAsNeeded()
	   {
		   this.adjustPriorityAndWakeupAsNeeded();
	   }

	   @Override
	   public void clear(boolean doRecycle)
	   {
	  	 pause();
	  	 super.clear(doRecycle);
	  	 unpause();
	   }

	   /**
	    * Start the image collecting agent.
	    */
	   public void start()
	   {
			if (thread == null)	// inexpensive test!
		   	{
		   		synchronized (startAndStopLock)
		   		{
					if (thread == null)	// test again inside the synchronized block, if we must
		      		{
						thread		= new Thread(this, "VisualPool");
						//ThreadDebugger.registerMyself(thread);
				 		Generic.setPriority(thread, HIGH_PRIORITY);
				 		thread.start();
		      		}
		   		}
	   		}
	   }
	   /**
	    * Stop the image collecting agent.
	    */
	   public void stop()
	   {
		   synchronized (startAndStopLock)
		   {
		     if (thread != null)
				 thread		= null;
		   }
	   }
	   
	   /**
	    * Select the most important image currently in our pool of candidates.
	    * This can call prune(), so it shares the lock on <code>this</code> with prune().
	    */
	   //TODO -- get rid of this method?!
	   public synchronized ImageClosure selectCandidate()
	   {
	  	 return pruneAndMaxSelect();
	   }

	/**
	 * Toggle the on-ness of the image collecting agent that prefetches candidate
	 * images.
	 */
	   public synchronized void toggleCollectingAgent()
	   {
		  boolean shouldDownloadImagesAutomatically = !downloadImagesAutomatically.value();
		  downloadImagesAutomatically.setValue(shouldDownloadImagesAutomatically);
		  if (shouldDownloadImagesAutomatically)	   // turn the crawler back on
		  {
			 if (thread == null)
			 {
				start();
			 }
			 else
				unpause();
			 infoCollector.displayStatus("Turning on agent that downloads images.");
		  }
		  else						   // turn the crawler off
		  {
			 pause();
			 infoCollector.displayStatus("Turning off agent that downloads images.");
		  }
	   }
	   
	   public boolean isRunnable()
	   {
		   return true;
	   }
	}
