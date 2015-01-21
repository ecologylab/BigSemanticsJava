/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;

import ecologylab.bigsemantics.actions.SemanticsConstants;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.downloadcontrollers.DownloadController;
import ecologylab.bigsemantics.logging.DocumentLogRecord;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.seeding.Seed;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;

/**
 * Super class for all document parser types. This class obtains the connection to a document. A
 * parse method may be present to process the document.
 * <p/>
 * Their role is to translate a Document into some kind of metadata and semantic actions. They start
 * with a PURL, which the static connect() method translates into an PURLConnection, and an
 * appropriate instance of a subclass. The {@link #parse() parse()} method is then called to
 * translate the document into the semantic model, using the PURLConnection.
 * <p/>
 * The translation from a PURL to an appropriate subclass instance is performed using a combination
 * of the PURLs extension, if it has a useful one, and then the mime-type returned by the
 * URLConnection response header. These keys are then used to perform a lookup in one of the
 * registries maintained in this class.
 * 
 * @author andruid
 * @author eunyee
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
abstract public class DocumentParser<D extends Document> extends Debug
{

  static final Scope<Class<? extends DocumentParser>> bindingParserMap     = new Scope<Class<? extends DocumentParser>>();

  static final Scope<Class<? extends DocumentParser>> registryByMimeType   = new Scope<Class<? extends DocumentParser>>();

  static final Scope<Class<? extends DocumentParser>> registryBySuffix     = new Scope<Class<? extends DocumentParser>>();

  static final Scope<Class<? extends DocumentParser>> registryByClassName  = new Scope<Class<? extends DocumentParser>>();

  static final HashSet<String>                        NO_PARSER_SUFFIX_MAP = new HashSet<String>();

  static
  {
    register(SemanticsConstants.DIRECT_BINDING_PARSER, DirectBindingParser.class);
    register(SemanticsConstants.XPATH_PARSER, XPathParser.class);
    register(SemanticsConstants.FEED_PARSER, FeedParser.class);
    register(SemanticsConstants.HTML_IMAGE_DOM_TEXT_PARSER,
             HTMLDOMImageTextParser.class);
    register(SemanticsConstants.PDF_PARSER, PdfParser.class);
  }

  /**
   * Allow clients to register or re-register parsers by name.
   * 
   * This can be useful when a client needs to provide an alternative implementation for an parser.
   * For example, the semantics service may need to use a different algorithm for HTML image text
   * clipping derivation in another parser.
   * 
   * @param binding
   * @param parserClass
   */
  public static void register(String binding, Class<? extends DocumentParser> parserClass)
  {
    bindingParserMap.put(binding, parserClass);
  }

  /**
   * Get registered parser by binding.
   * 
   * @param binding
   * @param semanticsScope
   * @param documentClosure
   * @param downloadController
   * @return
   */
  public static DocumentParser getByBinding(String binding,
                                            SemanticsGlobalScope semanticsScope,
                                            DocumentClosure documentClosure,
                                            DownloadController downloadController)
  {
    DocumentParser result = null;
    if (binding != null)
    {
      Class<? extends DocumentParser> documentTypeClass =
          (Class<? extends DocumentParser>) bindingParserMap.get(binding);

      if (documentTypeClass != null)
      {
        Object[] constructorArgs = new Object[1];
        constructorArgs[0] = semanticsScope;
        result = ReflectionTools.getInstance(documentTypeClass);

        result.setSemanticsScope(semanticsScope);
        result.setDocumentClosure(documentClosure);
        result.setDownloadController(downloadController);
      }
    }
    return result;
  }

  /**
   * Get registered parser by meta-metadata.
   * 
   * @param mmd
   * @param semanticsScope
   * @param documentClosure
   * @param downloadController
   * @return
   */
  public static DocumentParser getByMmd(MetaMetadata mmd,
                                        SemanticsGlobalScope semanticsScope,
                                        DocumentClosure documentClosure,
                                        DownloadController downloadController)
  {
    String binding = mmd.getParser();
    if (binding != null)
    {
      return DocumentParser.getByBinding(binding,
                                         semanticsScope,
                                         documentClosure,
                                         downloadController);
    }
    return null;
  }

  /**
   * Record the input PURL as not assocaited with any parser.
   * 
   * @param purl
   * @return true if purl is already recorded.
   */
  public static boolean isRegisteredNoParser(ParsedURL purl)
  {
    boolean result = false;
    if (purl != null)
    {
      String suffix = purl.suffix();
      if (suffix != null && suffix.length() > 0)
      {
        result = NO_PARSER_SUFFIX_MAP.contains(suffix);
        if (!result)
          NO_PARSER_SUFFIX_MAP.add(suffix);
      }
    }
    return result;
  }

  private SemanticsGlobalScope semanticsScope;

  private DownloadController   downloadController;

  private DocumentClosure      documentClosure;

  /**
   * Default constructor.
   */
  protected DocumentParser()
  {
    super();
  }

  public SemanticsGlobalScope getSemanticsScope()
  {
    return semanticsScope;
  }

  protected void setSemanticsScope(SemanticsGlobalScope semanticsScope)
  {
    this.semanticsScope = semanticsScope;
  }

  public DocumentClosure getDocumentClosure()
  {
    return documentClosure;
  }

  protected void setDocumentClosure(DocumentClosure documentClosure)
  {
    this.documentClosure = documentClosure;
  }

  public DownloadController getDownloadController()
  {
    return downloadController;
  }

  protected void setDownloadController(DownloadController downloadController)
  {
    this.downloadController = downloadController;
  }

  /**
   * @return The document.
   */
  public D getDocument()
  {
    return (D) documentClosure.getDocument();
  }

  /**
   * Subclasses can implement this for looking up downloaded document.
   * 
   * @param metadata
   * @return
   */
  protected Document lookupDownloadedDocument(Metadata metadata)
  {
    return null;
  }

  /**
   * Subclasses can implement this for looking up true meta-metadata and returning the right
   * metadata object.
   * 
   * @param repository
   * @param thisMetadata
   * @return
   */
  protected Metadata lookupTrueMetaMetadata(MetaMetadataRepository repository, Metadata thisMetadata)
  {
    return null;
  }

  /**
   * @return Meta-metadata of the corresponding document.
   */
  public MetaMetadataCompositeField getMetaMetadata()
  {
    Document document = documentClosure.getDocument();
    return document == null ? null : document.getMetaMetadata();
  }

  /**
   * Connects to a SeedDistributor, when appropriate.
   * 
   * @return null always for the default base class.
   */
  public Seed getSeed()
  {
    return null;
  }

  /**
   * @return A LogRecord that can be used to log events through the lifecycle of a Document.
   */
  public DocumentLogRecord getLogRecord()
  {
    return documentClosure.getLogRecord();
  }

  /**
   * Parse the document.
   * 
   * @throws IOException
   */
  public abstract void parse() throws IOException;

  /**
   * @return The ParsedURL value of the current document. If you want to get a URL value, you can
   *         use url() method in ParsedURL.
   */
  public ParsedURL purl()
  {
    if (documentClosure == null)
      return null;

    Document document = documentClosure.getDocument();
    if (document == null)
      return null;

    ParsedURL docPurl = document.getLocation();

    if (downloadController != null)
    {
      ParsedURL connPurl = downloadController.getLocation();
      if (connPurl == null)
      {
        List<ParsedURL> redirects = downloadController.getRedirectedLocations();
        if (redirects != null && redirects.size() > 0)
        {
          connPurl = redirects.get(0);
        }
      }

      if (docPurl == null || docPurl.isFile()
          || (connPurl != null && !connPurl.isFile()) || !semanticsScope.isService())
      {
        return connPurl;
      }
    }
    return docPurl;
  }

  /**
   * The document's PURL, without considering connection PURLs.
   * 
   * @return
   */
  public ParsedURL getTruePURL()
  {
    Document document = documentClosure.getDocument();
    return document == null ? null : document.getLocation();
  }

  /**
   * Use the DocumentClosure to close the current connection. Re-open a connection to the same
   * location. Use the same Document object; don't process re-directs, or anything like that.
   * Re-connect simply. Reset the purlConnection field of this to the new PURLConnection.
   * 
   * @return InputStream for the new connection.
   * @throws IOException
   */
  public InputStream reConnect() throws IOException
  {
    DownloadController downloadController = documentClosure.reConnect();
    this.downloadController = downloadController;
    return downloadController.getInputStream();
  }

  /**
   * @return The input stream to the raw document being downloaded, if any.
   */
  protected InputStream inputStream()
  {
    return (downloadController == null) ? null : downloadController.getInputStream();
  }

  /**
   * @return The reader of the raw document.
   */
  protected Reader reader()
  {
    return null;
  }

  /**
   * Handle I/O error.
   * 
   * @param e
   */
  public void handleIoError(Throwable e)
  {
    recycle();
  }

  /**
   * Free resources.
   */
  public void recycle()
  {
    semanticsScope = null;
    documentClosure = null;
  }

  /**
   * True if our analysis indicates the present AbstractContainer is an article, and not a
   * collection of links. This affects calls to getWeight() in the model!
   * 
   * @return true for an article. false for a collection of links (like a homepage).
   */
  public boolean isAnArticle()
  {
    return true;
  }

  public boolean isIndexPage()
  {
    return false;
  }

  public boolean isContentPage()
  {
    return false;
  }

  /**
   * Differentiates referential documents, like HTML from composite documents, like PDF.
   * 
   * @return true if images referred to by this document are stored within the document itself. The
   *         default implementation, here returns false.
   */
  public boolean isCompositeDocument()
  {
    return false;
  }

  /**
   * @return true to avoid building a DOM at the beginning.
   */
  public boolean doesDirectBinding()
  {
    return false;
  }

  public String toString()
  {
    ParsedURL purl = purl();
    String purlString = (purl != null) ? purl.toString() : null;
    if (purlString == null)
      purlString = "no purl";
    return super.toString() + "[" + purlString + "]";
  }

}
