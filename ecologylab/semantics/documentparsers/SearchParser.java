/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.DispatchTarget;
import ecologylab.io.Downloadable;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.CFPrefNames;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SearchEngineNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author amathur
 * 
 */
public class SearchParser
		extends LinksetParser implements CFPrefNames, DispatchTarget<Container>,
		SemanticActionsKeyWords
{

	/**
	 * The Search query URL
	 */
	private ParsedURL							searchURL;

	/**
	 * Name of the Search engine;
	 */
	private String								engine;

	private int										resultsSoFar	= 0;

	/**
	 * 
	 * @param infoCollector
	 * @param semanticActionHandler
	 */
	public SearchParser(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	public SearchParser(InfoCollector infoCollector, String query, float bias, int numResults, String engine)
	{
		this(new SearchState(query, engine, (short) 0, numResults, true), infoCollector, engine);
	}

	public SearchParser(SearchState searchSeed, InfoCollector infoCollector, String engine)
	{
		this(infoCollector, engine, searchSeed);
	}

	/**
	 * @param infoCollector
	 * @param engine
	 * @param semanticAction
	 * @param searchURL
	 */
	public SearchParser(InfoCollector infoCollector, String engine, SearchState searchSeed)
	{
		super(infoCollector);
		this.engine = engine;
		this.searchSeed = searchSeed;
		this.searchURL = searchSeed.formSearchUrlBasedOnEngine();

		// if search PURL is not null for a container.
		if (searchURL != null)
		{
			searchSeed.eliminatePlusesFromQuery();

			getMetaMetadataAndContainerAndQueue(infoCollector, searchURL, searchSeed, DocumentParserTagNames.SEARCH_TAG);

			setQuery(searchSeed);
		}
	}




	/**
	 * TODO implement this. This method set the query using reflecrtion on search class.
	 */
	private void setQuery(SearchState searchSeed)
	{
		if (container != null)
			container.setQuery(searchSeed.getQuery());
		else
			warning("WEIRD problem: this.container == null! might have been recycled.");
	}

	/**
	 * Customize the type based on the search engine
	 */
	private void customizeBasedOnEngine()
	{
		String query = searchSeed.valueString();
		if (engine.equals(SearchEngineNames.GOOGLE))
		{
			container.setJustCrawl(true);
			InterestModel.expressInterest(query, (short) 2);
		}
		infoCollector.displayStatus("Processing " + engine + " search page: " + query);
	}

		
	public ParsedURL purl()
	{
		return searchURL;
	}

	@Override
	public Document populateMetadata(SemanticActionHandler handler)
	{
		Document populatedMetadata	= (Document) container.getMetadata();
		
		//FIXME use overrides instead of constants here!!!!!!!!!!
		if ("direct".equals(metaMetadata.getParser()))
		{
			populatedMetadata				= directBindingPopulateMetadata();
		}
		else if ("xpath".equals(metaMetadata.getParser()))
		{
			recursiveExtraction(metaMetadata, populatedMetadata, getDom(), null, handler.getSemanticActionVariableMap());
			container.setMetadata(populatedMetadata);
		}

		return populatedMetadata;
	}

	@Override
	public void delivery(Container downloadedContainer)
	{
		super.delivery(downloadedContainer);
		if (searchSeed != null)
			searchSeed.incrementNumResultsBy(searchSeed.numResults());
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

	/*public boolean shouldUnpauseCrawler()
	{
		ResultDistributer aggregator = this.searchSeed.resultDistributer(infoCollector);
		return aggregator.checkIfAllSearchesOver();
	}

	public void takeSemanticActions(M populatedMetadata)
	{
		super.takeSemanticActions(populatedMetadata);
		}
*/
	}