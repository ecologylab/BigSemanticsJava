package ecologylab.documentparsers;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.xml.ElementState;


/**
 * DocumentParser base class for all Container documents, that is, those in which the resulting Document
 * will be of type Container, with all of its concomitant local collections et al.
 * </p>
 * References contains fields for search, in case those will be useful.
 * 
 * @author andruid
 */
abstract public class ContainerParser<C extends Container, IC extends InfoCollector<C>, ES extends ElementState> extends DocumentParser<C, IC, ES>
{
	/**
	 * Number of search results that we've processed so far, from the search engine.
	 */
	protected int						resultsSoFar;

	/**
	 * Vital specification of the search, if there is one.
	 */
	protected SearchState 				searchSeed;

	public ContainerParser(IC infoCollector)
	{
		super(infoCollector);
	}

	/**
	 * Set the InfoCollector and the searchSeed while constructing.
	 * @param searchSeed TODO
	 * @param infoCollector
	 */
	public ContainerParser(SearchState searchSeed, IC infoCollector)
	{   
		  super(infoCollector);
		  this.searchSeed			= searchSeed;
	}

	protected C container()
	{
		return container;
	}

	/**
	 * Return true if this DocumentType is a Container.
	 * 
	 * @return	true.
	 */
	public boolean isContainer()
	{
		return true;
	}
	
	/**
	 * Queue a Result Container for download, if needed.
	 * Use the SearchResultAggreator, if there is one.
	 * Otherwise, go straight to the DownloadMonitor.
	 * 
	 * @param resultPURL
	 * @param resultContainer
	 */
	protected void queueResult(ParsedURL resultPURL, Container resultContainer)
	{
		if (!resultContainer.downloadHasBeenQueued())
		{
			int resultNum	= resultsSoFar;
			if (searchSeed != null)
				resultNum	 += searchSeed.currentFirstResultIndex();
			
			final String msg 				= "Queueing search result "+ resultNum + ": " + resultPURL;
			infoCollector.displayStatus(msg);
			SeedDistributor sra		= this.searchSeed.seedDistributer(infoCollector);
			if (sra != null)
			{
				resultContainer.setSearchResult(sra, resultsSoFar);
				sra.queueResult(resultContainer);
			}
			else
				resultContainer.queueDownload();
			resultsSoFar++;
		}
	}
	
	protected void queueSearchRequest(Container searchContainer)
	{
		// Because the only time infoCollector is set is when a seedset is created in SeedSetType and XMLRestType.
//	    if (infoCollector == null && searchContainer!=null && searchContainer.getInfoCollector() != null)
//		    setInfoCollector(searchContainer.getInfoCollector());
	    
	    if( this.searchSeed != null)
		{
			SeedDistributor resultDistributer	= 
				this.searchSeed.seedDistributer(infoCollector);
			if (resultDistributer != null)
			{
				resultDistributer.queueSearchRequest(searchContainer);
				return;
			}
		}
	    searchContainer.queueDownload();
	}

	/**
	 * Get the searchSeed that spawned this, if there is one.
	 * 
	 * @return	SearchState or null.
	 */
	public SearchState searchSeed()
	{
		return searchSeed;
	}
	

}
