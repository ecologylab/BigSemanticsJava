/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.namesandnums.CFPrefNames;
import ecologylab.semantics.old.OldContainerI;
import ecologylab.semantics.seeding.SearchState;

/**
 * @author amathur
 * 
 */
public class SearchParser
		extends LinksetParser implements CFPrefNames, DispatchTarget<DocumentClosure>,
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
		Document document	= getDocument();
		if (document != null)
			document.setQuery(searchSeed.getQuery());
		else
			warning("WEIRD problem: this.document == null! might have been recycled.");
	}

		
	public ParsedURL purl()
	{
		return searchURL;
	}

	@Override
	public Document populateMetadata(SemanticActionHandler handler)
	{
		Document populatedMetadata	= getDocument();
		
		//FIXME use overrides instead of constants here!!!!!!!!!!
		if (DIRECT_BINDING_PARSER.equals(metaMetadata.getParser()))
		{
			populatedMetadata				= directBindingPopulateMetadata();
			
			//FIXME -- copy values like query from original metadata to the new one!!!
		}
		else if (XPATH_PARSER.equals(metaMetadata.getParser()))
		{
			recursiveExtraction(metaMetadata, populatedMetadata, getDom(), null, handler.getSemanticActionVariableMap());
//			container.setMetadata(populatedMetadata);
		}

		return populatedMetadata;
	}

	@Override
	public void delivery(DocumentClosure downloadedContainer)
	{
		super.delivery(downloadedContainer);
		if (searchSeed != null)
			searchSeed.incrementNumResultsBy(searchSeed.numResults());
	}

}