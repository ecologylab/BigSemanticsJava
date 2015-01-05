package ecologylab.bigsemantics.documentparsers;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.seeding.SearchState;
import ecologylab.bigsemantics.seeding.SeedDistributor;
import ecologylab.net.ParsedURL;

/**
 * DocumentParser base class for all Container documents, that is, those in which the resulting
 * Document will be of type Container, with all of its concomitant local collections et al. </p>
 * References contains fields for search, in case those will be useful.
 * 
 * @author andruid
 */
abstract public class ContainerParser<D extends Document> extends DocumentParser<D>
{

  /**
   * Number of search results that we've processed so far, from the search engine.
   */
  protected int         resultsSoFar;

  /**
   * Vital specification of the search, if there is one.
   */
  protected SearchState searchSeed;

  public ContainerParser()
  {
    super();
  }

  /**
   * Set the InfoCollector and the searchSeed while constructing.
   * 
   * @param searchSeed
   */
  public ContainerParser(SearchState searchSeed)
  {
    super();
    this.searchSeed = searchSeed;
  }

  /**
   * Queue a Result Container for download, if needed. Use the SearchResultAggreator, if there is
   * one. Otherwise, go straight to the DownloadMonitor.
   * 
   * @param resultPURL
   * @param resultContainer
   */
  protected void queueResult(ParsedURL resultPURL, DocumentClosure resultContainer)
  {
    if (!resultContainer.downloadHasBeenQueued())
    {
      int resultNum = resultsSoFar;
      if (searchSeed != null)
        resultNum += searchSeed.currentFirstResultIndex();

      final String msg = "Queueing search result " + resultNum + ": " + resultPURL;
      getSemanticsScope().displayStatus(msg);
      SeedDistributor sra = this.searchSeed.seedDistributer(getSemanticsScope());
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

  protected void queueSearchRequest(DocumentClosure searchContainer)
  {
    if (this.searchSeed != null)
    {
      SeedDistributor resultDistributer =
          this.searchSeed.seedDistributer(getSemanticsScope());
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
   * @return SearchState or null.
   */
  public SearchState searchSeed()
  {
    return searchSeed;
  }

}
