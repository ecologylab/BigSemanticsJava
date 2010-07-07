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
import ecologylab.xml.ElementState;
import ecologylab.xml.SIMPLTranslationException;

/**
 * @author amathur
 * 
 */
public class MetaMetadataSearchParser
		extends MetaMetadataLinksetParser implements CFPrefNames, DispatchTarget<Container>,
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
	public MetaMetadataSearchParser(InfoCollector infoCollector,
			SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector,semanticActionHandler);
	

	}

	public MetaMetadataSearchParser(InfoCollector infoCollector, String query, float bias,
			int numResults, String engine)
	{
		this(new SearchState(query, engine, (short) 0, numResults, true), infoCollector, engine);
	}

	public MetaMetadataSearchParser(SearchState searchSeed, InfoCollector infoCollector, String engine)
	{
		this(infoCollector, null, engine, searchSeed);
	}

	/**
	 * @param infoCollector
	 * @param engine
	 * @param semanticAction
	 * @param searchURL
	 */
	public MetaMetadataSearchParser(InfoCollector infoCollector,
			SemanticActionHandler semanticActionHandler, String engine, SearchState searchSeed)
	{
		super(infoCollector, semanticActionHandler);
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
		container.setQuery(searchSeed.getQuery());
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
	public Document populateMetadataObject()
	{
		Document populatedMetadata = container.metadata();
		
		ParsedURL purl = container.purl();
		if (metaMetadata.isSupported(purl, null))
		{
			if ("direct".equals(metaMetadata.getParser()))
			{
				try
				{
						populatedMetadata = (Document) ElementState.translateFromXML(inputStream(), getMetadataTranslationScope());
						if (populatedMetadata.getMetaMetadata() == null)
						{
							populatedMetadata.setMetaMetadata(metaMetadata);
							populatedMetadata.initializeMetadataCompTermVector();
						}
				}
				catch (SIMPLTranslationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if ("xpath".equals(metaMetadata.getParser()))
			{
				recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
						populatedMetadata, xpath, semanticActionHandler.getSemanticActionReturnValueMap(),document);
				container.setMetadata(populatedMetadata);
			}
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