/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.connectors.old.OldContainerI;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.namesandnums.CFPrefNames;
import ecologylab.semantics.seeding.SearchState;

/**
 * @author amathur
 * 
 */
public class SearchParser
		extends LinksetParser implements CFPrefNames, DispatchTarget<OldContainerI>,
		SemanticActionsKeyWords
{

	/**
	 * The Search query URL
	 */
	private ParsedURL							searchURL;

	private int										resultsSoFar	= 0;

	/**
	 * 
	 * @param infoCollector
	 * @param semanticActionHandler
	 */
	public SearchParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

	/**
	 * @param infoCollector
	 * @param semanticAction
	 * @param searchURL
	 */
	public SearchParser(NewInfoCollector infoCollector, SearchState searchSeed)
	{
		super(infoCollector);

		this.searchSeed = searchSeed;
		this.searchURL  = searchSeed.formSearchUrlBasedOnEngine();

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
	public void delivery(OldContainerI downloadedContainer)
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