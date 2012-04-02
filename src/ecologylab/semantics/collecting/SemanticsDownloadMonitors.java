/**
 * 
 */
package ecologylab.semantics.collecting;

import java.util.ArrayList;

import ecologylab.appframework.SimpleDownloadProcessor;
import ecologylab.concurrent.BasicSite;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.io.DownloadProcessor;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

/**
 * A set of DownloadMonitors, at various priority levels, for parsing various types of Documents.
 * 
 * @author andruid
 */
public class SemanticsDownloadMonitors extends Debug
{
	protected ArrayList<DownloadMonitor>				downloadMonitors							= new ArrayList<DownloadMonitor>();

	public static final int										REGULAR_DOCUMENT_DOWNLOAD_MONITOR					= 0;
	
	/**
	 * This is the <code>DownloadMonitor</code> used by to process drag and drop operations. It gets
	 * especially high priority, in order to provide rapid response to the user.
	 */
	public static final int										DND_DOCUMENT_DOWNLOAD_MONITOR			= 1;
	
	/**
	 * This is the <code>DownloadMonitor</code> used by seeds. It never gets paused.
	 */
	public static final int										SEEDING_DOCUMENT_DOWNLOAD_MONITOR	= 2;
	
	public static final int										REGULAR_IMAGE_DOWNLOAD_MONITOR						= 3;
	
	public static final int										DND_IMAGE_DOWNLOAD_MONITOR				= 4;
	
	public static final int										NUM_DOWNLOAD_MONITORS							= DND_IMAGE_DOWNLOAD_MONITOR + 1;
	
	private static final DownloadMonitor<DocumentClosure>[]	DOWNLOAD_MONITORS	= new DownloadMonitor[NUM_DOWNLOAD_MONITORS];
	
	public static final SimpleDownloadProcessor<DocumentClosure>	ASSETS_DOWNLOAD_PROCESSOR	=
		new SimpleDownloadProcessor<DocumentClosure>();

	static final int														NUM_CRAWLER_DOWNLOAD_THREADS	= 2;

	static final int														NUM_SEEDING_DOWNLOAD_THREADS	= 4;
	
 	static final int														NUM_IMAGE_DOWNLOAD_THREADS		= 2;

 	static final int														DND_PRIORITY_BOOST						= 6;
	
	static
	{
		DOWNLOAD_MONITORS[REGULAR_DOCUMENT_DOWNLOAD_MONITOR]			= new DownloadMonitor<DocumentClosure>("Documents Regular", NUM_CRAWLER_DOWNLOAD_THREADS, 0);
		
		DOWNLOAD_MONITORS[DND_DOCUMENT_DOWNLOAD_MONITOR]	= new DownloadMonitor<DocumentClosure>("Documents Highest Priority", NUM_SEEDING_DOWNLOAD_THREADS, DND_PRIORITY_BOOST);
		
		DOWNLOAD_MONITORS[SEEDING_DOCUMENT_DOWNLOAD_MONITOR]	= new DownloadMonitor<DocumentClosure>("Documents Seeding", NUM_SEEDING_DOWNLOAD_THREADS, 1);
		
		DOWNLOAD_MONITORS[REGULAR_IMAGE_DOWNLOAD_MONITOR]							= new DownloadMonitor<DocumentClosure>("Images Regular", NUM_IMAGE_DOWNLOAD_THREADS, 1);
		
		DOWNLOAD_MONITORS[DND_IMAGE_DOWNLOAD_MONITOR]					= new DownloadMonitor<DocumentClosure>("Images High Priority", NUM_IMAGE_DOWNLOAD_THREADS, DND_PRIORITY_BOOST);
	};
	
	

	/**
	 * 
	 */
	public SemanticsDownloadMonitors()
	{
		//FIXME -- why do we do this??? i think its wrong -- andruid 7/1/2011
		DOWNLOAD_MONITORS[REGULAR_DOCUMENT_DOWNLOAD_MONITOR].setHurry(true);
	}
	
	/**
	 * Pause the regular crawler Document DownloadMonitor.
	 * @param pause
	 */
	public void pauseRegular(boolean pause)
	{
		pause(REGULAR_DOCUMENT_DOWNLOAD_MONITOR, pause);
	}
	
	/**
	 * Pause all DownloadMonitors and wait until all the ongoing parsing is done. After this we can
	 * expect that all the metadata objects downloaded will not change due to downloading and parsing.
	 * @param pause
	 */
	public void pauseAllAndWaitUntilNonePending(boolean pause)
	{
		// there has to be two loops, because we want to first request pause for every DownloadMonitor
		// so that no new closures are processed, and then wait for them to finish their ongoing jobs.
		// if we merge the two, we may wait for a longer time because some closures get processed
		// when we are waiting on another DownloadMonitor to finish its ongoing job.
		for (DownloadMonitor<DocumentClosure> downloadMonitor : DOWNLOAD_MONITORS)
		{
			downloadMonitor.pause(pause);
		}
		for (DownloadMonitor<DocumentClosure> downloadMonitor : DOWNLOAD_MONITORS)
		{
			downloadMonitor.waitUntilNonePending();
		}
	}
	
	/**
	 * Pause a particular DownloadMonitor.
	 * 
	 * @param whichMonitor
	 * @param pause
	 */
	void pause(int whichMonitor, boolean pause)
	{
		DownloadMonitor<DocumentClosure> downloadMonitor = DOWNLOAD_MONITORS[whichMonitor];
		if (pause)
			downloadMonitor.pause();
		else
			downloadMonitor.unpause();
	}


	/**
	 * 
	 * @return	true if there are there are Downloadables enqueued in the Seeding DownloadMonitor.
	 */
	public boolean seedsArePending()
	{
		return DOWNLOAD_MONITORS[SEEDING_DOCUMENT_DOWNLOAD_MONITOR].toDownloadSize() > 0;
	}
	
	/**
	 * Get the DownloadMonitor that is appropriate for the Document and context at hand.
	 * 
	 * @param isImage
	 * @param isDnd
	 * @param isSeed
	 * @param isGui
	 * 
	 * @return	Appropriate DownloadMonitor, based on the input parameters.
	 */
  public DownloadProcessor<DocumentClosure> downloadProcessor(boolean isImage, boolean isDnd, boolean isSeed, boolean isGui)
	{
		DownloadProcessor<DocumentClosure> result;
		if (isGui)
			result	= ASSETS_DOWNLOAD_PROCESSOR;
		else
		{
			int downloadMonitorIndex	= isImage ? (isDnd ? DND_IMAGE_DOWNLOAD_MONITOR : REGULAR_IMAGE_DOWNLOAD_MONITOR) :
				(isDnd ? DND_DOCUMENT_DOWNLOAD_MONITOR : 
					isSeed ? SEEDING_DOCUMENT_DOWNLOAD_MONITOR : REGULAR_DOCUMENT_DOWNLOAD_MONITOR);
			result	= DOWNLOAD_MONITORS[downloadMonitorIndex];
		}
		return result;
	}
	
  /**
   * Stop all the DownloadMonitors.
   * 
   * @param kill
   */
	public void stop(boolean kill)
	{
		for (DownloadMonitor<DocumentClosure> downloadMonitor: DOWNLOAD_MONITORS)
			downloadMonitor.stop(kill);
	}
	
	/**
	 * Ask all the DownloadMonitors to stop when they have exhausted their queues.
	 * 
	 */
	public void requestStops()
	{
		for (DownloadMonitor<DocumentClosure> downloadMonitor: DOWNLOAD_MONITORS)
			downloadMonitor.requestStop();
	}
	/**
	 * Pause regular DownloadMonitors for documents and images.
	 */
	public void pauseRegularDownloadMonitors()
	{
		DOWNLOAD_MONITORS[REGULAR_DOCUMENT_DOWNLOAD_MONITOR].pause();
		DOWNLOAD_MONITORS[REGULAR_IMAGE_DOWNLOAD_MONITOR].pause();
	}
	/**
	 * Unpause regular DownloadMonitors for documents and images.
	 */
	public void unpauseRegularDownloadMonitors()
	{
		DOWNLOAD_MONITORS[REGULAR_DOCUMENT_DOWNLOAD_MONITOR].unpause();
		DOWNLOAD_MONITORS[REGULAR_IMAGE_DOWNLOAD_MONITOR].unpause();
	}
	
	public void unpauseAll()
	{
		for (DownloadMonitor<DocumentClosure> downloadMonitor : DOWNLOAD_MONITORS)
		{
			downloadMonitor.unpause();
		}
	}
	
	/**
	 * The regular DownloadMonitor for operating on Images.
	 * @return
	 */
	public static DownloadMonitor<DocumentClosure> regularImageDownloadMonitor()
	{
		return DOWNLOAD_MONITORS[REGULAR_IMAGE_DOWNLOAD_MONITOR];
	}

	public void killSite(BasicSite site)
	{
		for (DownloadMonitor<DocumentClosure> downloadMonitor: DOWNLOAD_MONITORS)
			downloadMonitor.removeAllDownloadClosuresFromSite(site);
	}
}
