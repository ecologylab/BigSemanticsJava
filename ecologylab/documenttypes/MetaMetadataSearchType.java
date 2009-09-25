/**
 * 
 */
package ecologylab.documenttypes;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.CFPrefNames;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SearchEngineNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.TypeTagNames;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.seeding.ResultDistributer;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;

/**
 * @author amathur
 * 
 */
public class MetaMetadataSearchType<M extends Metadata, C extends Container, IC extends InfoCollector<C>, E extends ElementState>
		extends MetaMetadataDocumentTypeBase<M, C, IC, E> implements CFPrefNames, DispatchTarget,
		SemanticActionsKeyWords
{

	/**
	 * The Serach query URL
	 */
	private ParsedURL							searchURL;

	/**
	 * Name of the Search engine;
	 */
	private String								engine;

	/**
	 * 
	 */
	private SearchState						searchSeed;

	private int										resultsSoFar	= 0;

	/**
	 * 
	 * @param infoCollector
	 * @param semanticActionHandler
	 */
	public MetaMetadataSearchType(IC infoCollector,
			SemanticActionHandler<C, IC> semanticActionHandler)
	{
		super(infoCollector,semanticActionHandler);
	

	}

	public MetaMetadataSearchType(IC infoCollector, String query, float bias,
			int numResults, String engine)
	{
		this(new SearchState(query, engine, (short) 0, numResults, true), infoCollector, engine);
	}

	public MetaMetadataSearchType(SearchState searchSeed, IC infoCollector, String engine)
	{
		this(infoCollector, null, engine, searchSeed);
	}

	/**
	 * @param infoCollector
	 * @param engine
	 * @param semanticAction
	 * @param searchURL
	 */
	public MetaMetadataSearchType(IC infoCollector,
			SemanticActionHandler<C, IC> semanticActionHandler, String engine, SearchState searchSeed)
	{
		super(infoCollector, semanticActionHandler);
		this.engine = engine;
		this.searchSeed = searchSeed;
		this.searchURL = searchSeed.formSearchUrlBasedOnEngine();

		// if search PURL is not null for a container.
		if (searchURL != null)
		{
			// first try to find the search meta-metadata using the purl
			MetaMetadata metaMetadata = infoCollector.metaMetaDataRepository().getDocumentMM(searchURL);
			
			// if this fails assign the default metadata of search
			if(metaMetadata ==null)
				metaMetadata = infoCollector.metaMetaDataRepository().getByTagName(
					TypeTagNames.SEARCH_TAG);
			C container = infoCollector.getContainer(null, searchURL, false, false, metaMetadata);
			setContainer(container);
			searchSeed.eliminatePlusesFromQuery();
			setQuery(searchSeed);
			container.presetDocumentType(this);
			container.setDispatchTarget(this);
			container.setAsTrueSeed(searchSeed);
			queueSearchrequest();
			// System.out.println("DEBUG::queued search request for \t"+searchSeed.getQuery());
			// infoProcessor.pauseDownloadMonitor();
		}
	}

	public void queueSearchrequest()
	{
		if (this.searchSeed != null)
		{
			ResultDistributer resultDistributer = this.searchSeed.resultDistributer(infoCollector);
			if (resultDistributer != null)
			{
				resultDistributer.queueSearchRequest(container);
				// System.out.println("DEBUG::queued search request for\t"+container+"\tusing rd=\t"+resultDistributer);
				return;
			}
		}
		container.queueDownload(); //FIXME: Unconditional queueing of searches ???
		// System.out.println("DEBUG::queued container\t"+container+"\t for download");
	}

	/**
	 * TODO implement this. This method set the query using reflecrtion on search class.
	 */
	private void setQuery(SearchState searchSeed)
	{
		container.setQuery(searchSeed.getQuery());
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
			infoCollector.displayStatus("Processing google search page: " + query);
		}
	}

	

	public void delivery(Object o)
	{
		ResultDistributer aggregator = this.searchSeed.resultDistributer(infoCollector);
		if (aggregator != null)
			aggregator.doneQueueing(searchSeed.searchNum(), resultsSoFar);
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

	public int getResultNum()
	{
		return resultsSoFar + searchSeed.currentFirstResultIndex();
	}

	public ParsedURL purl()
	{
		return searchURL;
	}

	@Override
	public M buildMetadataObject()
	{
		M populatedMetadata = (M)container.metadata();
		
		ParsedURL purl = container.purl();
		if (metaMetadata.isSupported(purl))
		{
			if ("direct".equals(metaMetadata.getBinding()))
			{
				try
				{
						populatedMetadata = (M) ElementState.translateFromXML(inputStream(), getMetadataTranslationScope());
				}
				catch (XMLTranslationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if ("xpath".equals(metaMetadata.getBinding()))
			{
				recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
						populatedMetadata, xpath, semanticActionHandler.getParameter(),document);
				container.setMetadata(populatedMetadata);
			}
		}
		try
		{
			populatedMetadata.translateToXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return populatedMetadata;
	}

/*	protected void createDOMandParse(ParsedURL purl)
	{
		if ("direct".equals(metaMetadata.getBinding()))
		{
			// only in the case of direct binding we need to find the DOM as tidy DOM is useless
			org.w3c.dom.Document document = ElementState.buildDOM(purl);
			semanticActionHandler.getParameter().addParameter(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE,
					document);
		}
	}*/

	public boolean shouldUnpauseCrawler()
	{
		ResultDistributer aggregator = this.searchSeed.resultDistributer(infoCollector);
		return aggregator.checkIfAllSearchesOver();
	}

	public void takeSemanticActions(M populatedMetadata)
	{
		super.takeSemanticActions(populatedMetadata);
		if (shouldUnpauseCrawler())
		{
			System.out.println("Ended Seeding");
			infoCollector.endSeeding();
		}

	}
}