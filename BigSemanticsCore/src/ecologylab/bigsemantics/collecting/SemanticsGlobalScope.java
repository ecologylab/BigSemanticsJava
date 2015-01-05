/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.documentcache.DocumentCache;
import ecologylab.bigsemantics.documentcache.HashMapDocumentCache;
import ecologylab.bigsemantics.documentcache.PersistentDocumentCache;
import ecologylab.bigsemantics.documentparsers.XPathAmender;
import ecologylab.bigsemantics.downloadcontrollers.DefaultDownloadController;
import ecologylab.bigsemantics.downloadcontrollers.DownloadController;
import ecologylab.bigsemantics.gui.InteractiveSpace;
import ecologylab.bigsemantics.html.dom.IDOMProvider;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.Image;
import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
import ecologylab.bigsemantics.metametadata.FieldParserRegistry;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * The SemanticsScope (also known as Crossroads) contains references to all of the big global
 * singleton object of S.IM.PL Semantics:
 * 
 * (1) GlobalCollection that maps ParsedURL keys to <? extends Document> values.
 * 
 * (2) MetaMetadataRespository
 * 
 * (3) SemanticsDownloadMonitors -- a set of DownloadMonitors, with different priority levels and
 * media assignments.
 * 
 * The SemanticsSessionScope will include references to Crossroads and to Crawler, if there is one.
 * I believe it is also where we store state related to Seeding.
 * 
 * @author andruid
 */
public class SemanticsGlobalScope extends MetaMetadataRepositoryInit
{

  static Logger                               logger;

  static
  {
    logger = LoggerFactory.getLogger(SemanticsGlobalScope.class);
  }

  /**
   * Used to construct a DOM provider.
   */
  final private Class<? extends IDOMProvider> domProviderClass;

  /**
   * Used to construct field parsers.
   */
  final private FieldParserRegistry           fieldParserRegistry;

  /**
   * Maps locations to Document Metadata subclasses. Constructs these Document instances as needed
   * using the MetaMetadataRepository.
   */
  final private LocalDocumentCollections      localDocumentCollection;

  /**
   * Pool of DownloadMonitors used for parsing Documents of various types.
   */
  final private SemanticsDownloadMonitors     downloadMonitors;

  /**
   * Monitoring document downloading, in order to link related metadata.
   */
  final private DocumentDownloadingMonitor    documentDownloadingMonitor;

  private XPathAmender                        xpathAmender;

  public SemanticsGlobalScope(SimplTypesScope metadataTScope,
                              Class<? extends IDOMProvider> domProviderClass)
  {
    this(null, metadataTScope, domProviderClass);
  }

  public SemanticsGlobalScope(File repositoryLocation,
                              SimplTypesScope metadataTScope,
                              Class<? extends IDOMProvider> domProviderClass)
  {
    this(repositoryLocation,
         MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT,
         metadataTScope,
         domProviderClass);
  }

  public SemanticsGlobalScope(File repositoryLocation,
                              Format repositoryFormat,
                              SimplTypesScope metadataTypesScope,
                              Class<? extends IDOMProvider> domProviderClass)
  {
    super(repositoryLocation, repositoryFormat, metadataTypesScope);
    this.domProviderClass = domProviderClass;
    this.fieldParserRegistry = new FieldParserRegistry();
    MetaMetadataRepository repository = this.getMetaMetadataRepository();
    DefaultDocumentMapHelper documentMapHelper = new DefaultDocumentMapHelper(repository);
    DocumentCache<ParsedURL, Document> documentCache = getDocumentCache();
    localDocumentCollection = new LocalDocumentCollections(documentMapHelper, documentCache);
    downloadMonitors = new SemanticsDownloadMonitors();
    documentDownloadingMonitor = new DocumentDownloadingMonitor(this);
    xpathAmender = createXPathAmender();
  }
  
  public IDOMProvider constructDOMProvider()
  {
    return ReflectionTools.getInstance(domProviderClass);
  }

  public FieldParserRegistry getFieldParserRegistry()
  {
    return fieldParserRegistry;
  }

  public LocalDocumentCollections getLocalDocumentCollection()
  {
    return localDocumentCollection;
  }

  /**
   * Pool of DownloadMonitors used for parsing Documents of various types.
   * 
   * @return the downloadMonitors
   */
  public SemanticsDownloadMonitors getDownloadMonitors()
  {
    return downloadMonitors;
  }

  public DocumentDownloadingMonitor getDocumentDownloadingMonitor()
  {
    return documentDownloadingMonitor;
  }

  public Document lookupDocument(ParsedURL location)
  {
    return location == null ? null : localDocumentCollection.lookupDocument(location);
  }

  public Document getOrConstructDocument(ParsedURL location)
  {
    if (location == null)
      return null;
    Document result = localDocumentCollection.getOrConstruct(location, false);
    result.setSemanticsSessionScope(this);
    if (!result.hasLogRecord())
    {
      result.setLogRecord(createLogRecord());
    }
    return result;
  }

  public void putDocumentIfAbsent(Document document)
  {
    localDocumentCollection.putIfAbsent(document);
  }

  public Image getOrConstructImage(ParsedURL location)
  {
    if (location == null)
      return null;
    Document constructDocument = localDocumentCollection.getOrConstruct(location, true);

    Image result = null;
    if (constructDocument.isImage())
    {
      result = (Image) constructDocument;
      result.setSemanticsSessionScope(this);
    }
    return result;
  }

  /**
   * Create a DownloadController using the given DocumentClosure.
   * 
   * @param closure
   * @return
   */
  public DownloadController createDownloadController(DocumentClosure closure)
  {
    return new DefaultDownloadController();
  }

  /**
   * Tells if this scope is a web service scope.
   * 
   * @return
   */
  public boolean isService()
  {
    return false;
  }

  /**
   * Unlike the session scope, the global scope is not discriminating.
   * 
   * @param purl
   * @return true
   */
  public boolean accept(ParsedURL purl)
  {
    return true;
  }

  /**
   * @param location
   * @return always true in this base class.
   */
  public boolean isLocationNew(ParsedURL location)
  {
    return true;
  }

  /**
   * @return If this scope will automatically update document references when referred documents are
   *         downloaded.
   */
  public boolean ifAutoUpdateDocRefs()
  {
    return false;
  }

  /**
   * @return If the extraction module should try to find the favicon for downloaded documents (more
   *         specifically, their sites).
   */
  public boolean ifLookForFavicon()
  {
    return false;
  }

  /**
   * Does nothing in this base class.
   */
  public Seeding getSeeding()
  {
    return null;
  }
  
  public XPathAmender getXPathAmender()
  {
    return xpathAmender;
  }

  protected XPathAmender createXPathAmender()
  {
    return new XPathAmender();
  }

  /**
   * Does nothing in this base class.
   */
  public int getAppropriateFontIndex()
  {
    return -1;
  }

  /**
   * Does nothing in this base class.
   */
  public boolean hasCrawler()
  {
    return false;
  }

  /**
   * Does nothing in this base class.
   */
  public Crawler getCrawler()
  {
    return null;
  }

  /**
   * Does nothing in this base class.
   */
  public InteractiveSpace getInteractiveSpace()
  {
    return null;
  }

  /**
   * @return A DocumentCache for caching extracted Document objects. Subclasses can override this
   *         method to use different caches.
   */
  protected DocumentCache<ParsedURL, Document> getDocumentCache()
  {
    return new HashMapDocumentCache();
  }

  /**
   * @return Null in this base class. Subclasses can override this to use different persistent
   *         caches.
   */
  public PersistentDocumentCache getPersistentDocumentCache()
  {
    return null;
  }
  
  public DocumentLogRecord createLogRecord()
  {
    return new DocumentLogRecord();
  }

	public void displayStatus(String message)
	{
    logger.info(message);
	}
	
  public void displayStatus(String message, int ticks)
  {
    logger.info(message);
  }

}
