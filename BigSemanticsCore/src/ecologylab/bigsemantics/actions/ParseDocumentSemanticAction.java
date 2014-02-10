/**
 * 
 */
package ecologylab.bigsemantics.actions;

import java.util.List;

import ecologylab.bigsemantics.collecting.Crawler;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.documentparsers.SearchParser;
import ecologylab.bigsemantics.html.documentstructure.LinkType;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.seeding.Feed;
import ecologylab.bigsemantics.seeding.SearchState;
import ecologylab.bigsemantics.seeding.Seed;
import ecologylab.bigsemantics.seeding.SeedDistributor;
import ecologylab.bigsemantics.seeding.SeedDistributor.DistributorContinuation;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * 
 */
@simpl_inherit
public @simpl_tag(SemanticActionStandardMethods.PARSE_DOCUMENT)
class ParseDocumentSemanticAction extends ContinuableSemanticAction
{

  @simpl_scalar
  @simpl_hints(Hint.XML_ATTRIBUTE)
  protected boolean  now                  = false;

  @simpl_scalar
  @simpl_hints(Hint.XML_ATTRIBUTE)
  protected LinkType linkType             = LinkType.OTHER_SEMANTIC_ACTION;

  /**
   * This attribute is meant to be used when we only require the top document to actually be sent to
   * the infoCollector. It requires two strings
   */
  @simpl_scalar
  @simpl_hints(Hint.XML_ATTRIBUTE)
  protected boolean  onlyPickTopDocuments = false;

  @simpl_scalar
  @simpl_hints(Hint.XML_ATTRIBUTE)
  protected int      numberOfTopDocuments = 1;

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

      Document source = documentParser.getDocument();
      if (source != null)
      {
        document.addInlink(source);
        // if there is a source, we should re-use that dispatch target.
        // e.g. search results from a search
        List<Continuation<DocumentClosure>> continuations =
            source.getOrConstructClosure().getContinuations();
        document.getOrConstructClosure().addContinuations(continuations);
      }
      document.queueDownload();
    }
    return null;
  }

  @Override
  public Object perform(Object obj)
  {
    if (sessionScope.isService())
    {
      return null;
    }

    // TODO -- add pref to choose performFull!
    return sessionScope.hasCrawler() ? performBasic(obj) : null;
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
    else if (onlyPickTopDocument())
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
    if (ancestor == null || candidateDocument == null)
    {
      warning("Parsing a document [" + candidateDocument + "] with ancestor ! - "
          + candidateDocument);
    }
    else
      ancestor.addCandidateOutlink(candidateDocument);

    // if currentIndex of foreach (if it is in one) == size - 1
    // ancestor.perhapsAddAdditionalContainer
    int curAnchorIndex = getArgumentInteger(CURRENT_INDEX, -1);
    int anchorListSize = getArgumentInteger(SIZE, -1);
    int outerLoopIndex = getArgumentInteger(OUTER_LOOP_INDEX, -1);
    int outerLoopSize = getArgumentInteger(OUTER_LOOP_SIZE, -1);

    // If the outerLoop exists, the outerIndex must be size -1, else outerLoop doesn't exist, so
    // disregard.
    boolean outerLoopEnd = outerLoopSize > 0 ? outerLoopIndex == outerLoopSize - 1 : true;
    boolean loopEnd = anchorListSize > 0 && curAnchorIndex == anchorListSize - 1;

    if (loopEnd && outerLoopEnd && ancestor != null)
    {
      debugT(" Reached end of iterations with outlinks size (" /* + ancestor.numOutlinks() */
          + ").\n\t\tPicking " + numberOfTopDocuments
          + " top documents from outlinks of container: " + ancestor);
      int numDocumentsRemaining = numberOfTopDocuments;
      while (numDocumentsRemaining-- > 0)
        ancestor.perhapsAddDocumentClosureToPool();
    }
  }

  protected void parseDocumentNow(Document document)
  {

    // In current implementation create_container_for_search may return null[for rejected
    // domains.]
    if (document != null)
    {
      DocumentClosure documentClosure = document.getOrConstructClosure();
      if (continuation != null)
      {
        documentClosure.addContinuation(this); // for continuation semantic actions :-)!
      }
      if (documentClosure == null)
        warning("Can't parse " + document.getLocation() + " because null container.");
      else if (!distributeSeedingResults(this, documentParser, documentClosure, null))
        documentClosure.queueDownload(); // case for normal documents
    }
  }

  protected void parseDocumentLater(Document document)
  {
    DocumentClosure documentClosure = document.getOrConstructClosure();

    if (documentClosure == null || documentClosure.downloadHasBeenQueued())
      warning("Can't parse " + document.getLocation()
          + " because null container or already queued.");
    else
    {
      final Crawler crawler = sessionScope.getCrawler();
      if (!distributeSeedingResults(this, documentParser, documentClosure,
                                    new DistributorContinuation()
                                    {
                                      @Override
                                      public void distribute(DocumentClosure result)
                                      {
                                        if (crawler != null)
                                          crawler.addClosureToPool(result); // ?? just curious:
                                                                            // isn't result the same
                                                                            // as documentClosure?!
                                      }
                                    }))
      {
        if (crawler != null)
        {
          if (continuation != null)
          {
            documentClosure.addContinuation(this); // for continuation semantic actions :-)!
          }
          crawler.addClosureToPool(documentClosure);
        }
      }
    }
  }

  /**
   * If possible, distribute a seeding result through SeedDistributor. e.g. for &lt;search&gt; or
   * &lt;feed&gt;.
   * 
   * @param action
   * @param documentParser
   * @param semanticsSessionScope
   * @param resultContainer
   * @return true if a seeding result is distributed; false if not applicable (e.g. a normal page).
   */
  protected boolean distributeSeedingResults(SemanticAction action,
                                             DocumentParser documentParser,
                                             DocumentClosure resultContainer,
                                             DistributorContinuation distributorContinuation)
  {
    SeedDistributor resultsDistributor = null;
    Seed searchSeed = documentParser.getSeed();
    String engineString = "";

    if (searchSeed != null)
    {
      // its a search type
      resultsDistributor = searchSeed.seedDistributer(sessionScope);
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
      final String msg =
          "Queueing " + engineString + "search result " + (resultNum) + ": " + resultPURL;
      sessionScope.displayStatus(msg);
      int resultSoFar = metaMetadataSearchParser.getResultSoFar();
      resultContainer.setSearchResult(resultsDistributor, resultSoFar);
      // TODO -- add continuation semantic actions!!!
      resultsDistributor.queueResult(resultContainer, distributorContinuation);
      metaMetadataSearchParser.incrementResultSoFar();

      return true;
    }
    else if (searchSeed instanceof Feed)
    {
      int rank = action.getArgumentInteger(SemanticActionNamedArguments.RANK, 0);
      resultContainer.setSearchResult(resultsDistributor, rank);
      // TODO -- add continuation semantic actions!!!
      resultsDistributor.queueResult(resultContainer, distributorContinuation);

      return true;
    }

    return false;
  }

}
