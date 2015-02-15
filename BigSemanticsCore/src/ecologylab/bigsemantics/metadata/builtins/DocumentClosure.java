/**
 * 
 */
package ecologylab.bigsemantics.metadata.builtins;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.actions.SemanticAction;
import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.actions.SemanticsConstants;
import ecologylab.bigsemantics.collecting.DocumentDownloadedEventHandler;
import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.collecting.SemanticsDownloadMonitors;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.collecting.SemanticsSite;
import ecologylab.bigsemantics.documentcache.PersistentDocumentCache;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.downloadcontrollers.CachedPageDownloadController;
import ecologylab.bigsemantics.downloadcontrollers.DownloadController;
import ecologylab.bigsemantics.html.documentstructure.SemanticInLinks;
import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.bigsemantics.logging.CachedHtmlStale;
import ecologylab.bigsemantics.logging.CachedMmdStale;
import ecologylab.bigsemantics.logging.ChangeLocation;
import ecologylab.bigsemantics.logging.DocumentLogRecord;
import ecologylab.bigsemantics.logging.PersistenceCacheDocHit;
import ecologylab.bigsemantics.logging.PersistenceCacheHtmlHit;
import ecologylab.bigsemantics.logging.PersistenceCacheMiss;
import ecologylab.bigsemantics.logging.Phase;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.TermVectorFeature;
import ecologylab.bigsemantics.seeding.SearchResult;
import ecologylab.bigsemantics.seeding.Seed;
import ecologylab.bigsemantics.seeding.SeedDistributor;
import ecologylab.collections.SetElement;
import ecologylab.concurrent.Downloadable;
import ecologylab.generic.Continuation;
import ecologylab.io.DownloadProcessor;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.library.geom.PointInt;

/**
 * New Container object. Mostly just a closure around Document. Used as a candidate and wrapper for
 * downloading.
 * 
 * @author andruid
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DocumentClosure extends SetElement
    implements TermVectorFeature, Downloadable, SemanticsConstants,
    Continuation<DocumentClosure>
{

  static Logger                               logger;

  static
  {
    logger = LoggerFactory.getLogger(DocumentClosure.class);
  }

  private SemanticsGlobalScope                semanticsScope;

  /**
   * This is tracked mainly for debugging, so we can see what pURL was fed into the meta-metadata
   * address resolver machine.
   */
  private ParsedURL                           initialPURL;

  private Document                            document;

  private final Object                        DOCUMENT_LOCK        = new Object();

  private DownloadStatus                      downloadStatus       = DownloadStatus.UNPROCESSED;

  private final Object                        DOWNLOAD_STATUS_LOCK = new Object();

  private DocumentParser                      documentParser;

  private SemanticInLinks                     semanticInlinks;

  private List<Continuation<DocumentClosure>> continuations;

  /**
   * Keeps state about the search process, if this is encapsulates a search result;
   */
  private SearchResult                        searchResult;

  private PointInt                            dndPoint;

  /**
   * If true (the normal case), then any MediaElements encountered will be added to the candidates
   * collection, for possible inclusion in the visual information space.
   */
  private boolean                             collectMedia         = true;

  /**
   * If true (the normal case), then hyperlinks encounted will be fed to the web crawler, providing
   * that they are traversable() and of the right mime types.
   */
  private boolean                             crawlLinks           = true;
  
  private boolean                             reload;

  private final Object                        DOWNLOAD_LOCK        = new Object();

  /**
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  private DocumentClosure(Document document,
                          SemanticsGlobalScope semanticsSessionScope,
                          SemanticInLinks semanticInlinks)
  {
    super();
    this.semanticsScope = semanticsSessionScope;
    this.initialPURL = document.getLocation();
    this.document = document;
    this.semanticInlinks = semanticInlinks;
    this.continuations = new ArrayList<Continuation<DocumentClosure>>();
  }

  /**
   * Should only be called by Document.getOrCreateClosure().
   * 
   * @param document
   * @param semanticInlinks
   */
  DocumentClosure(Document document, SemanticInLinks semanticInlinks)
  {
    this(document, document.getSemanticsScope(), semanticInlinks);
  }

  /**
   * @return the infoCollector
   */
  public SemanticsGlobalScope getSemanticsScope()
  {
    return semanticsScope;
  }

  public ParsedURL getInitialPURL()
  {
    return initialPURL;
  }

  /**
   * @return the document
   */
  public Document getDocument()
  {
    synchronized (DOCUMENT_LOCK)
    {
      return document;
    }
  }

  public DocumentParser getDocumentParser()
  {
    return documentParser;
  }

  /**
   * @param presetDocumentParser
   *          the presetDocumentParser to set
   */
  public void setDocumentParser(DocumentParser presetDocumentParser)
  {
    this.documentParser = presetDocumentParser;
  }

  @Override
  public SemanticsSite getSite()
  {
    Document document = this.document;
    return (document == null) ? null : document.getSite();
  }

  @Override
  public SemanticsSite getDownloadSite()
  {
    Document document = this.document;
    if (document != null)
    {
      if (document.getDownloadLocation().isFile())
        return null;
    }
    return (document == null) ? null : document.getSite();
  }

  public boolean isFromSite(SemanticsSite site)
  {
    return site != null && site == getSite();
  }

  @Override
  public ParsedURL location()
  {
    Document document = this.document;
    return (document == null) ? null : document.getLocation();
  }

  @Override
  public ParsedURL getDownloadLocation()
  {
    Document document = this.document;
    return (document == null) ? null : document.getDownloadLocation();
  }

  /**
   * @return the semanticInlinks
   */
  public SemanticInLinks getSemanticInlinks()
  {
    return semanticInlinks;
  }

  /**
   * Keeps state about the search process, if this Container is a search result;
   */
  public SearchResult searchResult()
  {
    return searchResult;
  }

  /**
   * 
   * @param resultDistributer
   * @param searchNum
   *          Index into the total number of (seeding) searches specified and being aggregated.
   * @param resultNum
   *          Result number among those returned by google.
   */
  public void setSearchResult(SeedDistributor resultDistributer, int resultNum)
  {
    searchResult = new SearchResult(resultDistributer, resultNum);
  }

  public SeedDistributor resultDistributer()
  {
    return (searchResult == null) ? null : searchResult.resultDistributer();
  }

  @Override
  public DocumentLogRecord getLogRecord()
  {
    return document.logRecord();
  }

  @Override
  public boolean isImage()
  {
    return document.isImage();
  }

  public boolean isSeed()
  {
    return (document != null) && document.isSeed();
  }

  public Seed getSeed()
  {
    return document != null ? document.getSeed() : null;
  }

  public boolean isDnd()
  {
    return dndPoint != null;
  }

  public PointInt getDndPoint()
  {
    return dndPoint;
  }

  public void setDndPoint(PointInt dndPoint)
  {
    this.dndPoint = dndPoint;
  }

  /**
   * This method is called before we actually hit the website. Thus, it uses the initial URL to test
   * if we need to hit the website. If it returns true, we definitely don't need to hit the website;
   * if it returns false, we need to hit the website, but the actual document might have been cached
   * using another URL.
   */
  @Override
  public boolean isCached()
  {
    return false;
  }

  /**
   * @return the downloadStatus
   */
  public DownloadStatus getDownloadStatus()
  {
    synchronized (DOWNLOAD_STATUS_LOCK)
    {
      return downloadStatus;
    }
  }

  public boolean isUnprocessed()
  {
    return getDownloadStatus() == DownloadStatus.UNPROCESSED;
  }
  
  public boolean isReload()
  {
    return reload;
  }

  public void setReload(boolean reload)
  {
    this.reload = reload;
  }

  /**
   * Test state variable inside of QUEUE_DOWNLOAD_LOCK.
   * 
   * @return true if result has already been queued, connected to, downloaded, ... so it should not
   *         be operated on further.
   */
  public boolean downloadHasBeenQueued()
  {
    return getDownloadStatus() != DownloadStatus.UNPROCESSED;
  }

  /**
   * Test and set state variable inside of QUEUE_DOWNLOAD_LOCK.
   * 
   * @return true if this really queues the download, and false if it had already been queued.
   */
  private boolean testAndSetQueueDownload()
  {
    synchronized (DOWNLOAD_STATUS_LOCK)
    {
      if (downloadStatus != DownloadStatus.UNPROCESSED)
        return false;
      setDownloadStatusInternal(DownloadStatus.QUEUED);
      return true;
    }
  }

  private void setDownloadStatus(DownloadStatus newStatus)
  {
    synchronized (DOWNLOAD_STATUS_LOCK)
    {
      setDownloadStatusInternal(newStatus);
    }
  }

  /**
   * (this method does not lock DOWNLOAD_STATUS_LOCK!)
   * 
   * @param newStatus
   */
  private void setDownloadStatusInternal(DownloadStatus newStatus)
  {
    this.downloadStatus = newStatus;
    if (this.document != null)
    {
      document.setDownloadStatus(newStatus);
    }
  }

  public DownloadProcessor<DocumentClosure> downloadMonitor()
  {
    SemanticsDownloadMonitors downloadMonitors = semanticsScope.getDownloadMonitors();
    return downloadMonitors.downloadProcessor(document.isImage(),
                                              isDnd(),
                                              isSeed(),
                                              document.isGui());
  }

  /**
   * Download if necessary, using the {@link ecologylab.concurrent.DownloadMonitor DownloadMonitor}
   * if USE_DOWNLOAD_MONITOR is set (it seems it always is), or in a new thread. Control will be
   * passed to {@link #downloadAndParse() downloadAndParse()}. Does nothing if this has been
   * previously queued, if it has been recycled, or if it isMuted().
   * 
   * @return true if this is actually queued for download. false if it was previously, if its been
   *         recycled, or if it is muted.
   */
  public boolean queueDownload()
  {
    if (recycled())
    {
      debugA("ERROR: cant queue download cause already recycled.");
      return false;
    }
    if (this.getDownloadLocation() == null)
      return false;
    final boolean result = !filteredOut(); // for dashboard type on the fly filtering
    if (result)
    {
      if (!testAndSetQueueDownload())
        return false;
      delete(); // remove from candidate pools! (invokes deleteHook as well)

      downloadMonitor().download(this, continuations == null ? null : this);
    }
    return result;
  }

  /**
   * Connect to the information resource. Figure out the appropriate MetaMetadata and DocumentType.
   * Download the information resource and parse it. Do cleanup afterwards.
   * 
   * This method is typically called by DownloadMonitor.
   * 
   * @throws IOException
   */
  @Override
  public void performDownload() throws IOException
  {
    MetaMetadata metaMetadata = (MetaMetadata) document.getMetaMetadata();
    boolean noCache = metaMetadata.isNoCache();

    synchronized (DOWNLOAD_STATUS_LOCK)
    {
      if (noCache || reload)
      {
        switch (downloadStatus)
        {
        case CONNECTING:
        case PARSING:
          return;
        default:
          break;
        }
      }
      else
      {
        if (recycled() || document.isRecycled())
        {
          logger.error("Recycled document closure in performDownload(): " + document);
          return;
        }
        switch (downloadStatus)
        {
        case CONNECTING:
        case PARSING:
        case DOWNLOAD_DONE:
        case IOERROR:
        case RECYCLED:
          return;
        default:
          break;
        }
      }
      setDownloadStatusInternal(DownloadStatus.CONNECTING);
    }
    
    ParsedURL location = location();
    DocumentLogRecord logRecord = getLogRecord();
    PersistentDocumentCache pCache = semanticsScope.getPersistentDocumentCache();
    
    logRecord.beginPhase(Phase.DOWNLOAD_AND_PARSE);

    // Check the persistent cache first
    PersistenceMetaInfo cacheMetaInfo = null;
    String cachedRawContent = null;
    Document cachedDoc = null;
    if (pCache != null && !noCache && !reload)
    {
      logRecord.beginPhase(Phase.PCACHE_READ);

      try
      {
        cacheMetaInfo = pCache.getMetaInfo(location);
        if (cacheMetaInfo != null)
        {
          logRecord.setPersistenceMetaInfo(cacheMetaInfo);

          // check if cached raw content is too old.
          Date accessTime = cacheMetaInfo.getAccessTime();
          Date currentTime = new Date();
          long diff = currentTime.getTime() - accessTime.getTime();
          long cacheLifeMs = metaMetadata.getCacheLifeMs();
          if (diff <= cacheLifeMs)
          {
            // it's not too old, we should use the cached raw content.
            cachedRawContent = pCache.retrieveRawContent(cacheMetaInfo);
            logRecord.logPost().addEventNow(new PersistenceCacheHtmlHit());

            // check if cached document needs to be re-extracted
            String currentHash = metaMetadata.getHashForExtraction();
            if (currentHash.equals(cacheMetaInfo.getMmdHash()))
            {
              cachedDoc = pCache.retrieveDoc(cacheMetaInfo);
              logRecord.logPost().addEventNow(new PersistenceCacheDocHit());
            }
            else
            {
              logRecord.logPost().addEventNow(new CachedMmdStale());
            }
          }
          else
          {
            logRecord.logPost().addEventNow(new CachedHtmlStale());
          }
        }
        else
        {
          logRecord.logPost().addEventNow(new PersistenceCacheMiss());
        }
      }
      catch (Exception e)
      {
        String errMsg = "Error accessing persistence cache.";
        logger.error(errMsg, e);
        logRecord.addErrorRecord(errMsg, e);
      }

      logRecord.endPhase(Phase.PCACHE_READ);
    }

    // If not in the persistent cache, download the raw page and parse
    if (cachedDoc != null)
    {
      changeDocument(cachedDoc);
    }
    else
    {
      DownloadController downloadController = null;
      boolean rawContentDownloaded = false;
      if (cachedRawContent != null)
      {
        downloadController =
            new CachedPageDownloadController(cacheMetaInfo.getLocation(),
                                             cacheMetaInfo.getRawAdditionalLocations(),
                                             cacheMetaInfo.getCharset(),
                                             cacheMetaInfo.getMimeType(),
                                             200,
                                             "OK",
                                             cachedRawContent);
      }
      else
      {
        downloadController = downloadRawPage(location);
        rawContentDownloaded = true;
      }

      if (downloadController.isGood())
      {
        handleRedirections(downloadController, location);
        metaMetadata =
            changeMetaMetadataIfNeeded(downloadController.getHttpResponse().getMimeType());

        findParser(metaMetadata, downloadController);
        if (documentParser != null)
        {
          doParse(metaMetadata);
          if (pCache != null && !noCache)
          {
            doPersist(pCache, downloadController, document, rawContentDownloaded);
          }
        }
      }
      else
      {
        logger.error("Network connection error: " + document);
        setDownloadStatus(DownloadStatus.IOERROR);
        logRecord.endPhase(Phase.DOWNLOAD_AND_PARSE);
        return;
      }
    }

    document.downloadAndParseDone(documentParser);
    logRecord.endPhase(Phase.DOWNLOAD_AND_PARSE);
    setDownloadStatus(DownloadStatus.DOWNLOAD_DONE);
  }

  private DownloadController downloadRawPage(ParsedURL location) throws IOException
  {
    getLogRecord().beginPhase(Phase.DOWNLOAD);
    String userAgent = document.getMetaMetadata().getUserAgentString();
    DownloadController downloadController = semanticsScope.createDownloadController(this);
    downloadController.setUserAgent(userAgent);
    if (downloadController.accessAndDownload(location))
    {
      SimplHttpResponse httpResp = downloadController.getHttpResponse();
      getLogRecord().setDownloadStatusCode(httpResp.getCode());
    }
    getLogRecord().endPhase(Phase.DOWNLOAD);
    return downloadController;
  }

  private void handleRedirections(DownloadController downloadController, ParsedURL location)
  {
    String newUrl = downloadController.getHttpResponse().getUrl();
    ParsedURL newPurl = ParsedURL.getAbsolute(newUrl);
    Document newDoc = semanticsScope.getOrConstructDocument(newPurl);
    changeDocument(newDoc);

    // handle other locations:
    List<ParsedURL> otherLocations = downloadController.getHttpResponse().getOtherPurls();
    if (otherLocations != null)
    {
      for (ParsedURL otherLocation : otherLocations)
      {
        if (otherLocation != null)
        {
          document.addAdditionalLocation(otherLocation);
          semanticsScope.getLocalDocumentCollection().addMapping(otherLocation, document);
        }
      }
    }
  }

  private MetaMetadata changeMetaMetadataIfNeeded(String mimeType)
  {
    MetaMetadata metaMetadata = (MetaMetadata) document.getMetaMetadata();
    // check for more specific meta-metadata
    if (metaMetadata.isGenericMetadata())
    { // see if we can find more specifc meta-metadata using mimeType
      MetaMetadataRepository repository = semanticsScope.getMetaMetadataRepository();
      MetaMetadata mimeMmd = repository.getMMByMime(mimeType);
      if (mimeMmd != null && !mimeMmd.equals(metaMetadata))
      {
        // new meta-metadata!
        if (!mimeMmd.getMetadataClass().isAssignableFrom(document.getClass()))
        {
          // more specific so we need new metadata!
          Document document = (Document) mimeMmd.constructMetadata(); // set temporary on stack
          changeDocument(document);
        }
        metaMetadata = mimeMmd;
        document.setMetaMetadata(mimeMmd);
      }
    }
    return metaMetadata;
  }

  private void findParser(MetaMetadata metaMetadata, DownloadController downloadController)
  {
    if (documentParser == null)
    {
      boolean noParser = false;
      
//      // First check if registered no parser
//      noParser = DocumentParser.isRegisteredNoParser(document.getLocation());
//      List<MetadataParsedURL> additionalLocations = document.getAdditionalLocations();
//      if (additionalLocations != null)
//      {
//        for (int i = 0; i < additionalLocations.size() && !noParser; ++i)
//        {
//          noParser |= DocumentParser.isRegisteredNoParser(additionalLocations.get(i).getValue());
//        }
//      }

      if (noParser)
      {
        logger.warn("Registered no parser: " + document);
      }
      else
      {
        // If not registered no parser, try to find one
        documentParser =
            DocumentParser.getByMmd(metaMetadata, semanticsScope, this, downloadController);
        if (documentParser == null)
        {
          logger.warn("No parser found: " + metaMetadata);
        }
      }
    }
  }

  private void doParse(MetaMetadata metaMetadata) throws IOException
  {
    getLogRecord().beginPhase(Phase.EXTRACT);

    // container or not (it could turn out to be an image or some other mime type), parse the baby!
    setDownloadStatus(DownloadStatus.PARSING);
    takeSemanticActions(metaMetadata, metaMetadata.getBeforeSemanticActions());
    documentParser.parse();
    takeSemanticActions(metaMetadata, metaMetadata.getAfterSemanticActions());
    addDocGraphCallbacksIfNeeded();

    getLogRecord().endPhase(Phase.EXTRACT);
  }

  private void takeSemanticActions(MetaMetadata metaMetadata, ArrayList<SemanticAction> actions)
  {
    if (metaMetadata != null && actions != null)
    {
      SemanticActionHandler handler = new SemanticActionHandler(semanticsScope, documentParser);
      handler.takeSemanticActions(metaMetadata, document, actions);
    }
  }

  private void addDocGraphCallbacksIfNeeded()
  {
    if (this.getSemanticsScope().ifAutoUpdateDocRefs())
    {
      // add callbacks so that when this document is downloaded and parsed, references to it will
      // be updated automatically.
      Set<DocumentDownloadedEventHandler> listeners =
          semanticsScope.getDocumentDownloadingMonitor().getListenersForDocument(document);
      if (listeners != null && listeners.size() > 0)
      {
        addContinuations(listeners);
      }
    }
  }

  private void doPersist(PersistentDocumentCache pCache,
                         DownloadController downloadController,
                         Document doc,
                         boolean rawContentDownloaded)
      throws IOException
  {
    getLogRecord().beginPhase(Phase.PCACHE_WRITE);
    try
    {
      if (rawContentDownloaded)
      {
        PersistenceMetaInfo metaInfo =
            pCache.store(doc,
                         downloadController.getHttpResponse().getContent(),
                         downloadController.getHttpResponse().getCharset(),
                         downloadController.getHttpResponse().getMimeType(),
                         doc.getMetaMetadata().getHashForExtraction());
        getLogRecord().setId(metaInfo.getDocId());
        getLogRecord().setPersistenceMetaInfo(metaInfo);
      }
      else
      {
        PersistenceMetaInfo metaInfo = pCache.getMetaInfo(doc.getLocation());
        pCache.updateDoc(metaInfo, doc);
      }
    }
    catch (Exception e)
    {
      String errMsg = "Error storing to persistence cache.";
      logger.error(errMsg, e);
      getLogRecord().addErrorRecord(errMsg, e);
    }
    getLogRecord().endPhase(Phase.PCACHE_WRITE);
  }

  /**
   * In use cases such as the service, we want to be able to call performDownload() synchronously,
   * and in the same time make sure that the same closure will be downloaded by one thread at a
   * time. This method uses a lock to implement this.
   * 
   * @throws IOException
   */
  public void performDownloadSynchronously() throws IOException
  {
    synchronized (DOWNLOAD_LOCK)
    {
      performDownload();
    }
  }

  /**
   * Dispatch all of our registered callbacks.
   */
  @Override
  public void callback(DocumentClosure o)
  {
    if (continuations == null)
      return;

    List<Continuation<DocumentClosure>> currentContinuations;
    synchronized (continuations)
    {
      currentContinuations = new ArrayList<Continuation<DocumentClosure>>(continuations);
    }
    if (currentContinuations != null)
    {
      for (Continuation<DocumentClosure> continuation : currentContinuations)
      {
        try
        {
          continuation.callback(o);
        }
        catch (Exception e)
        {
          logger.error("Error calling back: " + o + ": " + continuation, e);
        }
      }
    }

    // wait to recycle continuations until after they have been called.
    if (isRecycled())
    {
      continuations.clear();
      continuations = null;
    }
  }

  public List<Continuation<DocumentClosure>> getContinuations()
  {
    return continuations;
  }

  private List<Continuation<DocumentClosure>> continuations()
  {
    return continuations;
  }

  public void addContinuation(Continuation<DocumentClosure> continuation)
  {
    synchronized (continuations)
    {
      continuations().add(continuation);
    }
  }

  public void addContinuations(Collection<? extends Continuation<DocumentClosure>> incomingContinuations)
  {
    synchronized (continuations)
    {
      List<Continuation<DocumentClosure>> continuations = continuations();
      for (Continuation<DocumentClosure> continuation : incomingContinuations)
        continuations.add(continuation);
    }
  }

  public void addContinuationBefore(Continuation<DocumentClosure> continuation)
  {
    synchronized (continuations)
    {
      continuations().add(0, continuation);
    }
  }

  /**
   * Add a continuation to this closure before it is downloaded (i.e. before its performDownload()
   * method finishes).
   * 
   * This gives the client the possibility of making sure the continuation will be called when the
   * closure finishes downloading.
   * 
   * @param continuation
   * @return true if the continuation is added before the closure finishes downloading; false if the
   *         closure is already downloaded.
   */
  public boolean addContinuationBeforeDownloadDone(Continuation<DocumentClosure> continuation)
  {
    if (downloadStatus != DownloadStatus.DOWNLOAD_DONE
        && downloadStatus != DownloadStatus.IOERROR
        && downloadStatus != DownloadStatus.RECYCLED)
    {
      synchronized (DOWNLOAD_STATUS_LOCK)
      {
        if (downloadStatus != DownloadStatus.DOWNLOAD_DONE
            && downloadStatus != DownloadStatus.IOERROR
            && downloadStatus != DownloadStatus.RECYCLED)
        {
          addContinuation(continuation);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Document metadata object must change, because we learned something new about its type.
   * 
   * @param newDocument
   */
  public void changeDocument(Document newDocument)
  {
    synchronized (DOCUMENT_LOCK)
    {
      if (newDocument != document)
      {
        Document oldDocument = document;
        document = newDocument;

        logger.info("Changing {} to {}", oldDocument, newDocument);

        SemanticsSite oldSite = oldDocument.site();
        SemanticsSite newSite = newDocument.site();
        if (oldSite != null && oldSite != newSite)
        {
          // calling changeDocument() because of redirecting?
          if (oldSite.isDownloading())
            oldSite.endDownload(oldDocument.getDownloadLocation());
        }

        newDocument.inheritValues(oldDocument);

        semanticInlinks = newDocument.getSemanticInlinks(); // probably not needed, but just in
                                                            // case.

        newDocument.setLogRecord(oldDocument.getLogRecord());

        ParsedURL oldLoc = oldDocument.getLocation();
        ParsedURL newLoc = newDocument.getLocation();
        if (oldLoc != null && !oldLoc.equals(newLoc))
        {
          ChangeLocation changeLocationEvent = new ChangeLocation(oldLoc, newLoc);
          getLogRecord().logPost().addEventNow(changeLocationEvent);
        }

        oldDocument.recycle();
      }
    }
  }

  /**
   * Close the current connection. Re-open a connection to the same location. Use the same Document
   * object; don't process re-directs, or anything like that. Re-connect simply.
   * 
   * @return PURLConnection for the new connection.
   * @throws IOException
   */
  public DownloadController reConnect() throws IOException
  {
    DownloadController downloadController = semanticsScope.createDownloadController(this);
    downloadController.accessAndDownload(document.getLocation());
    return downloadController;
  }

  @Override
  public void recycle()
  {
    recycle(false);
  }

  @Override
  public synchronized void recycle(boolean recycleDocument)
  {
    synchronized (DOWNLOAD_STATUS_LOCK)
    {
      if (downloadStatus == DownloadStatus.RECYCLED)
        return;
      setDownloadStatusInternal(DownloadStatus.RECYCLED);
    }

    if (documentParser != null)
      documentParser.recycle();

    semanticInlinks = null;

    initialPURL = null;

    // ??? should we recycle Document here -- under what circumstances???
    if (recycleDocument)
      document.recycle();
  }

  @Override
  public boolean recycled()
  {
    Document document = this.document;
    return document == null || document.isRecycled();
  }

  @Override
  public boolean isRecycled()
  {
    return document == null || document.isRecycled();
  }

  /**
   * Resets this closure as if it is newly created.
   */
  public void reset()
  {
    setDownloadStatus(DownloadStatus.UNPROCESSED);
    if (document != null)
    {
      document.resetRecycleStatus();
    }
  }

  @Override
  public String toString()
  {
    return super.toString() + "[" + document.getLocation() + "]";
  }

  @Override
  public int hashCode()
  {
    return (document == null) ? -1 : document.hashCode();
  }

  @Override
  public ITermVector termVector()
  {
    return (document == null) ? null : document.termVector();
  }

  /**
   * Called by DownloadMonitor in case a timeout happens.
   */
  @Override
  public void handleIoError(Throwable e)
  {
    setDownloadStatus(DownloadStatus.IOERROR);
    if (documentParser != null)
    {
      documentParser.handleIoError(e);
    }
    recycle();
  }

  @Override
  public String message()
  {
    return document == null ? "recycled" : document.getLocation().toString();
  }

  public void serialize(OutputStream stream)
  {
    serialize(stream, StringFormat.XML);
  }

  public void serialize(OutputStream stream, StringFormat format)
  {
    Document document = getDocument();
    try
    {
      SimplTypesScope.serialize(document, System.out, format);

      System.out.println("\n");
    }
    catch (SIMPLTranslationException e)
    {
      error("Could not serialize " + document);
      e.printStackTrace();
    }
  }

  public void serialize(StringBuilder buffy)
  {
    Document document = getDocument();
    try
    {
      SimplTypesScope.serialize(document, buffy, StringFormat.XML);
      System.out.println("\n");
    }
    catch (SIMPLTranslationException e)
    {
      error("Could not serialize " + document);
      e.printStackTrace();
    }
  }

}
