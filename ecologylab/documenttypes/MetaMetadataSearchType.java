/**
 * 
 */
package ecologylab.documenttypes;

import java.io.IOException;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.CFPrefNames;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SearchEngineNames;
import ecologylab.semantics.connectors.SearchEngineUtilities;
import ecologylab.semantics.library.TypeTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.seeding.ResultDistributer;
import ecologylab.semantics.seeding.SearchState;

/**
 * @author amathur
 * 
 */
public class MetaMetadataSearchType extends MetaMetadataXPathType implements CFPrefNames,
		DispatchTarget
{

	/**
	 * The Serach query URL
	 */
	private ParsedURL		searchURL;

	/**
	 * Name of the Search engine;
	 */
	private String			engine;

	/**
	 * 
	 */
	private SearchState	searchSeed;

	private int					resultsSoFar	= 0;

	/**
	 * 
	 * @param infoProcessor
	 * @param semanticActionHandler
	 */
	public MetaMetadataSearchType(InfoCollector infoProcessor,
			SemanticActionHandler semanticActionHandler)
	{
		super(infoProcessor, semanticActionHandler);

	}

	public MetaMetadataSearchType(InfoCollector infoCollector, String query, float bias,
			int numResults, String engine)
	{
		this(new SearchState(query, engine, (short) 0, numResults, true), infoCollector, engine);
	}

	public MetaMetadataSearchType(SearchState searchSeed, InfoCollector infoCollector, String engine)
	{
		this(infoCollector, null, engine, searchSeed, 0);
	}

	/**
	 * @param infoProcessor
	 * @param semanticAction
	 * @param searchURL
	 * @param engine
	 */
	public MetaMetadataSearchType(InfoCollector infoProcessor,
			SemanticActionHandler semanticActionHandler, String engine, SearchState searchSeed,
			int firstResultIndex)
	{
		super(infoProcessor);
		this.engine = engine;
		this.searchSeed = searchSeed;
		this.semanticActionHandler = semanticActionHandler;
		formSearchUrlBasedOnEngine(firstResultIndex);

		// if search PURL is not null for a container.
		if (searchURL != null)
		{
			MetaMetadata metaMetadata = infoProcessor.metaMetaDataRepository().getByTagName(
					TypeTagNames.SEARCH_TAG);
			Container container = infoProcessor.getContainer(null, searchURL, false, false, metaMetadata);
			setContainer(container);
			// setQuery();
			container.presetDocumentType(this);
			container.setDispatchTarget(this);
			container.setAsTrueSeed(searchSeed);
			queueSearchrequest();
			// System.out.println("DEBUG::queued search request for \t"+searchSeed.getQuery());
//			infoProcessor.pauseDownloadMonitor();
		}
	}

	public void queueSearchrequest()
	{
		if (this.searchSeed != null)
		{
			ResultDistributer resultDistributer = this.searchSeed
					.resultDistributer(abstractInfoCollector);
			if (resultDistributer != null)
			{
				resultDistributer.queueSearchRequest(container);
				// System.out.println("DEBUG::queued search request for\t"+container+"\tusing rd=\t"+resultDistributer);
				return;
			}
		}
		container.queueDownload();
		// System.out.println("DEBUG::queued container\t"+container+"\t for download");
	}

	@Override
	public void parse() throws IOException
	{
		Metadata populatedMetadata = (Metadata) buildMetadataObject();

		takeSemanticActions(populatedMetadata);
		// now here we know that it is of type Search and so we need to set query.
		// since the search class is in generated semantics project will reflection:-)
	}

	/**
	 * TODO implement this. This method set the query using reflecrtion on search class.
	 */
	private void setQuery()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Customize the type based on the search engine
	 */
	private void customizeBasedOnEngine()
	{
		if (engine.equals(SearchEngineNames.GOOGLE))
		{
			container.setJustCrawl(true);
			String query = searchSeed.valueString();
			InterestModel.expressInterest(query, (short) 2);
			abstractInfoCollector.displayStatus("Processing google search page: " + query);
		}
	}

	/**
	 * 
	 */
	private void formSearchUrlBasedOnEngine(int firstResultIndex)
	{
		if (engine.equals(SearchEngineNames.GOOGLE))
		{
			searchURL = ParsedURL
					.getAbsolute(
							((searchSeed.searchType() == SearchEngineNames.REGULAR) ? SearchEngineNames.regularGoogleSearchURLString
									:

									(searchSeed.searchType() == SearchEngineNames.IMAGE) ? SearchEngineNames.imageGoogleSearchURLString
											: (searchSeed.searchType() == SearchEngineNames.SITE) ? SearchEngineUtilities
													.siteGoogleLimitSearchURLString(searchSeed.siteString())
													: SearchEngineNames.relatedGoogleSearchUrlString)
									+ (searchSeed.valueString().replace(' ', '+')).replace("&quot;", "%22")

									+ (IGNORE_PDF.value() ? SearchEngineNames.GOOGLE_NO_PDF_ARG : "")
									+ "&num="
									+ searchSeed.numResults() + "&start=" + firstResultIndex, "broken Google URL");
		}
		else if(engine.equals(SearchEngineNames.FLICKR))
		{
			searchURL=ParsedURL.getAbsolute("http://www.flickr.com/search/?q="+searchSeed.getQuery());
		}
	}


	public void delivery(Object o)
	{
		ResultDistributer aggregator = this.searchSeed.resultDistributer(abstractInfoCollector);
		if (aggregator != null)
			aggregator.doneQueueing(searchSeed.searchNum(), resultsSoFar, null);
		if (this.searchSeed != null)
			this.searchSeed.incrementNumResultsBy(searchSeed.numResults());

	}

	/**
	 * @return the searchSeed
	 */
	public SearchState getSearchSeed()
	{
		return searchSeed;
	}

	public void incrementResultSoFar()
	{
		this.resultsSoFar++;
	}

	public int getResultSoFar()
	{
		return this.resultsSoFar;
	}

}
