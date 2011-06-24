/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.documentparsers.SearchParser;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.seeding.Feed;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedDistributor.DistributorContinuation;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;

/**
 * 
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_DOCUMENT)
class ParseDocumentSemanticAction
		extends SemanticAction
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected boolean	now										= false;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected LinkType linkType									= LinkType.OTHER_SEMANTIC_ACTION;
	
	/**
	 * This attribute is meant to be used when we only require the top document to actually be sent to
	 * the infoCollector. It requires two strings
	 */
	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected boolean	onlyPickTopDocuments	= false;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected int			numberOfTopDocuments	= 1;

	public boolean isNow()
	{
		return now;
	}

	public boolean onlyPickTopDocument()
	{
		return onlyPickTopDocuments;
	}

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.PARSE_DOCUMENT;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	public Object performBasic(Object obj)
	{
		if (isNow())
		{
			Document document = getOrCreateDocument(documentParser, linkType);
	
	//		ParsedURL purl = (ParsedURL) getArgumentObject(SemanticActionNamedArguments.CONTAINER_LINK);
			Document source = documentParser.getDocument();
			if (source != null)
			{
				document.addInlink(source);
				// if there is a source, we should re-use that dispatch target.
				// e.g. search results from a search
				Continuation dispatchTarget = source.getOrConstructClosure().continuation();
				document.getOrConstructClosure().setContinuation(dispatchTarget);
			}
			document.queueDownload();
		}
		return null;
	}

	@Override
	public Object perform(Object obj)
	{
		//TODO -- add pref to choose performFull!
		return infoCollector.isCollectCandidatesInPools() ? performBasic(obj) : null;
	}

	public Object performFull(Object obj)
	{
		
		Document document = getOrCreateDocument(documentParser, linkType);
		if (document == null)
		{
			// candidateContainer can be null, e.g. the url is actually an image url (in which case
			// infoCollector.getContainer() will return null). if this is the case we return immediately
			// since there is no document to parse.
			return null;
		}
		
		if (isNow())
		{
			parseDocumentNow(document);
		}
		else if(onlyPickTopDocument())
		{
			pickTopDocuments(document);
		}
		else
		{
			parseDocumentLater(document);
		}

		return null;
	}

	private void pickTopDocuments(Document candidateDocument)
	{
		Document ancestor = candidateDocument.getAncestor();
		if(ancestor == null || candidateDocument == null)
		{
			warning("Parsing a document [" + candidateDocument+ "] with ancestor ! - " + candidateDocument);
		}
		else
			ancestor.addCandidateOutlink(candidateDocument);
		
		//if currentIndex of foreach (if it is in one) == size - 1
		//ancestor.perhapsAddAdditionalContainer
		int curAnchorIndex = getArgumentInteger(CURRENT_INDEX, -1);
		int anchorListSize = getArgumentInteger(SIZE, -1);
		int outerLoopIndex = getArgumentInteger(OUTER_LOOP_INDEX, -1);
		int outerLoopSize = getArgumentInteger(OUTER_LOOP_SIZE, -1);

		
		//If the outerLoop exists, the outerIndex must be size -1, else outerLoop doesn't exist, so disregard.
		boolean outerLoopEnd = outerLoopSize > 0 ? outerLoopIndex == outerLoopSize - 1 : true;
		boolean loopEnd = anchorListSize > 0 && curAnchorIndex == anchorListSize - 1;
		
		if(loopEnd && outerLoopEnd && ancestor != null)
		{
			debugT(" Reached end of iterations with outlinks size (" /* + ancestor.numOutlinks() */ +").\n\t\tPicking " + numberOfTopDocuments + " top documents from outlinks of container: " + ancestor);
			int numDocumentsRemaining = numberOfTopDocuments;
			while(numDocumentsRemaining-- > 0)
				ancestor.perhapsAddDocumentClosureToPool();
		}
	}
	
	protected void parseDocumentNow(Document document)
	{
		
		// In current implementation create_container_for_search may return null[for rejected
		// domains.]
		if (document != null)
		{
			DocumentClosure documentClosure	= document.getOrConstructClosure();
			if (documentClosure == null)
				warning("Can't parse " + document.getLocation() + " because null container." );
			else if (!distributeSeedingResults(this, documentParser, documentClosure, null))
				documentClosure.queueDownload(); // case for normal documents
		}
	}

	protected void parseDocumentLater(Document document)
	{
		DocumentClosure documentClosure	= document.getOrConstructClosure();
		if (documentClosure == null || documentClosure.downloadHasBeenQueued())
			warning("Can't parse " + document.getLocation() + " because null container or already queued." );
		else if (!distributeSeedingResults(this, documentParser, documentClosure,
				new DistributorContinuation<DocumentClosure>()
				{
			@Override
			public void distribute(DocumentClosure result)
			{
				infoCollector.addClosureToPool(result);	// ?? just curious: isn't result the same as documentClosure?!
			}
				}))
		{
			infoCollector.addClosureToPool(documentClosure);
		}
	}

	/**
	 * If possible, distribute a seeding result through SeedDistributor. e.g. for &lt;search&gt; or &lt;feed&gt;.
	 * 
	 * @param action
	 * @param documentParser
	 * @param infoCollector
	 * @param resultContainer
	 * @return true if a seeding result is distributed; false if not applicable (e.g. a normal page).
	 */
	protected boolean distributeSeedingResults(SemanticAction action, DocumentParser documentParser, 
			DocumentClosure resultContainer, DistributorContinuation callback)
	{
		SeedDistributor resultsDistributor = null;
		Seed searchSeed = documentParser.getSeed();
		String engineString = "";

		if (searchSeed != null)
		{
			// its a search type
			resultsDistributor = searchSeed.seedDistributer(infoCollector);
			// will be non-null only for search result documents or feed item documents
			if (resultsDistributor == null)
				return false;

			if (searchSeed instanceof SearchState)
			{
				engineString = ((SearchState) searchSeed).getEngine() + " ";
			}
		}

		resultContainer.delete(); // remove from any and all candidate pools!

		if (searchSeed instanceof SearchState)
		{
			SearchParser metaMetadataSearchParser = (SearchParser) documentParser;
			int resultNum = metaMetadataSearchParser.getResultNum();
			ParsedURL resultPURL = resultContainer.location();
			// (ParsedURL)
			// semanticActionReturnValueMap.get(getNamedArgument(action,CONTAINER_LINK).getValue());
			;
			final String msg = "Queueing " + engineString + "search result " + (resultNum) + ": "
					+ resultPURL;
			// System.out.println(msg);
			infoCollector.displayStatus(msg);
			resultContainer.setSearchResult(resultsDistributor, metaMetadataSearchParser.getResultSoFar());
			resultsDistributor.queueResult(resultContainer, callback);
			metaMetadataSearchParser.incrementResultSoFar();

			return true;
		}
		else if (searchSeed instanceof Feed)
		{
			int rank = action.getArgumentInteger(SemanticActionNamedArguments.RANK, 0);
			resultContainer.setSearchResult(resultsDistributor, rank);
			// resultContainer.setQuery(searchSeed.getQuery());
			resultsDistributor.queueResult(resultContainer, callback);

			return true;
		}

		return false;
	}

}
