package ecologylab.bigsemantics.metadata.builtins;

/**
 * This is not generated code, but a hand-authored base class in the Metadata hierarchy. It is
 * hand-authored in order to provide specific functionalities
 **/

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.collecting.LocalDocumentCollections;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.collecting.SemanticsSite;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.documentparsers.ParserResult;
import ecologylab.bigsemantics.html.documentstructure.SemanticAnchor;
import ecologylab.bigsemantics.html.documentstructure.SemanticInLinks;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.mm_no;
import ecologylab.bigsemantics.metadata.builtins.declarations.DocumentDeclaration;
import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.seeding.SearchState;
import ecologylab.bigsemantics.seeding.Seed;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.FieldUsage;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_exclude_usage;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * The Document Class
 **/
@simpl_inherit
public class Document extends DocumentDeclaration
{

  static public final Document          RECYCLED_DOCUMENT              = new Document(ParsedURL.getAbsolute("http://recycled.document"));

  static public final Document          UNDEFINED_DOCUMENT             = new Document(ParsedURL.getAbsolute("http://undefined.document"));

  protected SemanticsGlobalScope        semanticsScope;

  private SemanticsSite                 site;

  private SemanticInLinks               semanticInlinks;

  @simpl_composite
  private DocumentLogRecord             logRecord;

  private DocumentClosure               documentClosure;

  private ParserResult                  parserResult;

  protected int                         badImages;

  private boolean                       sameDomainAsPrevious;

  private boolean                       alwaysAcceptRedirect;
  
  /**
   * Seed object associated with this, if this is a seed.
   */
  private Seed                          seed;

  /**
   * Indicates that this Container is a truly a seed, not just one that is associated into a Seed's
   * inverted index.
   */
  private boolean                       isTrueSeed;

  /**
   * Indicates that this Container is processed via drag and drop.
   */
  private boolean                       isDnd;

  @simpl_exclude_usage(FieldUsage.SERIALIZATION_IN_STREAM)
  @simpl_scalar
  @mm_no
  private DownloadStatus                downloadStatus                 = DownloadStatus.UNPROCESSED;

  /**
   * Used to keep track of transition times.
   */
  private long                          lastActionTimestamp            = 0;

  /**
   * Stores time in milliseconds taken before reaching a download status.
   */
  private HashMap<DownloadStatus, Long> transitionTimeToDownloadStatus = new HashMap<DownloadStatus, Long>();

  public Document()
  {
    super();
  }

  public Document(MetaMetadataCompositeField metaMetadata)
  {
    super(metaMetadata);
  }

  /**
   * Construct an instance of this, the base document type, and set its location.
   * 
   * @param location
   */
  protected Document(ParsedURL location)
  {
    super(MetaMetadataRepository.getBaseDocumentMM());
    initDocument(this, location);
  }

  /**
   * With the new *Declaration classes, constructor inheritance might be limited. Thus, the
   * initialization process can be abstracted out for subclasses to use.
   * 
   * @param document
   * @param location
   */
  protected static void initDocument(Document document, ParsedURL location)
  {
    document.setLocation(location);
  }

  /**
   * Use the local location if there is one; otherwise, just use the regular location.
   * 
   * @return
   */
  public ParsedURL getDownloadLocation()
  {
    ParsedURL result = getLocation();
    ParsedURL localLocation = getLocalLocation();
    if (localLocation != null)
    {
      File localFile = localLocation.file();
      if (localFile.exists())
        result = localLocation;
    }
    return result;
  }

  /**
   * Sets the value of the field location
   **/
  @Override
  public void setLocation(ParsedURL location)
  {
    if (location != null)
    {
      this.location().setValue(location);

      Document ancestor = getAncestor();
      if (ancestor != null)
      {
        ParsedURL ancestorLocation = ancestor.getLocation();
        String domain = location.domain();
        sameDomainAsPrevious =
            (ancestorLocation != null && domain != null && domain.equals(ancestorLocation.domain()));
      }
    }
  }

  public boolean isLocationNull()
  {
    return getLocationMetadata() == null || getLocationMetadata().getValue() == null;
  }

  /**
   * The heavy weight setter method for field title
   **/
  public void hwSetTitle(String title)
  {
    title().setValue(title);
    rebuildCompositeTermVector();
  }

  /**
   * Heavy Weight Direct setter method for title
   **/
  public void hwSetTitleMetadata(MetadataString title)
  {
    if (!isTitleNull() && hasTermVector())
      termVector().remove(getTitleMetadata().termVector());
    setTitleMetadata(title);
    rebuildCompositeTermVector();
  }

  public boolean isTitleNull()
  {
    return this.getTitleMetadata() == null || this.getTitleMetadata().getValue() == null;
  }

  /**
   * The heavy weight setter method for field description
   **/
  public void hwSetDescription(String description)
  {
    description().setValue(description);
    rebuildCompositeTermVector();
  }

  /**
   * Heavy Weight Direct setter method for description
   **/
  public void hwSetDescriptionMetadata(MetadataString description)
  {
    if (!isDescriptionNull() && hasTermVector())
      termVector().remove(getDescriptionMetadata().termVector());
    setDescriptionMetadata(description);
    rebuildCompositeTermVector();
  }

  public boolean isDescriptionNull()
  {
    return getDescriptionMetadata() == null || getDescriptionMetadata().getValue() == null;
  }

  /**
   * @return the alwaysAcceptRedirects
   */
  public boolean isAlwaysAcceptRedirect()
  {
    return alwaysAcceptRedirect;
  }

  /**
   * @param alwaysAcceptRedirects
   *          the alwaysAcceptRedirects to set
   */
  public void setAlwaysAcceptRedirect(boolean alwaysAcceptRedirects)
  {
    this.alwaysAcceptRedirect = alwaysAcceptRedirects;
  }

  public Document getAncestor()
  {
    return semanticInlinks == null ? null : semanticInlinks.getAncestor();
  }

  public int getGeneration()
  {
    return semanticInlinks == null ? 0 : semanticInlinks.getGeneration();
  }

  public int getEffectiveGeneration()
  {
    return semanticInlinks == null ? 0 : semanticInlinks.getEffectiveGeneration();
  }

  /**
   * @return the sameDomainAsPrevious
   */
  public boolean isSameDomainAsPrevious()
  {
    return sameDomainAsPrevious;
  }

  @Override
  public int hashCode()
  {
    return (getLocationMetadata() == null) ? -1 : getLocationMetadata().hashCode();
  }

  final Object CREATE_CLOSURE_LOCK = new Object();

  public DocumentClosure documentClosure()
  {
    return documentClosure;
  }

  /**
   * 
   * @return A closure for this, or null, if this is not fit to be parsed.
   */
  public DocumentClosure getOrConstructClosure()
  {
    DocumentClosure result = this.documentClosure;
    if (result == null && !isRecycled() && getLocation() != null)
    {
      synchronized (CREATE_CLOSURE_LOCK)
      {
        result = this.documentClosure;
        if (result == null)
        {
          if (semanticInlinks == null)
          {
            semanticInlinks = new SemanticInLinks();
          }

          result = constructClosure();
          this.documentClosure = result;
        }
      }
    }
    return result == null || result.getDownloadStatus() == DownloadStatus.RECYCLED ? null : result;
  }

  private DocumentClosure constructClosure()
  {
    return new DocumentClosure(this, semanticInlinks);
  }

  public SemanticsSite getSite()
  {
    SemanticsSite result = this.site;
    if (result == null)
    {
      result = semanticsScope.getMetaMetadataRepository().getSite(this, semanticsScope);
      this.site = result;
    }
    return result;
  }

  SemanticsSite site()
  {
    return site;
  }

  /**
   * @return the infoCollector
   */
  public SemanticsGlobalScope getSemanticsScope()
  {
    return semanticsScope;
  }

  /**
   * @param semanticsSessionScope
   *          the infoCollector to set
   */
  @Override
  public void setSemanticsSessionScope(SemanticsGlobalScope semanticsSessionScope)
  {
    this.semanticsScope = semanticsSessionScope;
  }
  
  public boolean hasLogRecord()
  {
    return logRecord != null;
  }
  
  public DocumentLogRecord getLogRecord()
  {
    if (logRecord == null)
    {
      return DocumentLogRecord.DUMMY;
    }
    return logRecord;
  }
  
  public void setLogRecord(DocumentLogRecord logRecord)
  {
    this.logRecord = logRecord;
  }

  public void addAdditionalLocation(ParsedURL newPurl)
  {
    addAdditionalLocation(new MetadataParsedURL(newPurl));
  }

  public void addAdditionalLocation(MetadataParsedURL newMPurl)
  {
    if (location().equals(newMPurl.getValue()))
      return;
    if (containsLocation(getAdditionalLocations(), newMPurl.getValue()))
      return;

    if (getAdditionalLocations() == null)
      setAdditionalLocations(new ArrayList<MetadataParsedURL>(3));
    getAdditionalLocations().add(newMPurl);
  }

  private boolean containsLocation(List<MetadataParsedURL> list, ParsedURL purl)
  {
    if (list != null)
      for (MetadataParsedURL metadataPurl : list)
        if (purl.equals(metadataPurl.getValue()))
          return true;
    return false;
  }

  /**
   * Get the old location from this. Set the location of this to the newLocation. Add a mapping in
   * the GlobalCollection from newLocation to this. Add the old location for this as an
   * additionalLocation for this.
   * 
   * @param newLocation
   */
  public void changeLocation(final ParsedURL newLocation)
  {
    if (newLocation != null)
    {
      ParsedURL origLocation = getLocation();
      if (!origLocation.equals(newLocation))
        ;
      {
        setLocation(newLocation);
        getSemanticsScope().getLocalDocumentCollection().addMapping(newLocation, this);
        addAdditionalLocation(origLocation);
      }
    }
  }

  /**
   * Used when oldDocument turns out to be re-directed from this.
   * 
   * @param oldDocument
   */
  public void inheritValues(Document oldDocument)
  {
    oldDocument.getSemanticsScope().getLocalDocumentCollection().remap(oldDocument, this);
    if (getLocationMetadata() == null)
    {
      setLocationMetadata(oldDocument.getLocationMetadata());
      oldDocument.setLocationMetadata(null);
    }
    this.semanticsScope = oldDocument.semanticsScope;
    SemanticInLinks oldInlinks = oldDocument.semanticInlinks;
    if (semanticInlinks == null || semanticInlinks.size() == 0)
    {
      this.semanticInlinks = oldInlinks;
      oldDocument.semanticInlinks = null;
    }
    else if (oldInlinks != null)
      semanticInlinks.merge(oldInlinks);

    List<Metadata> oldMixins = oldDocument.getMixins();
    if (oldMixins != null)
      for (Metadata oldMixin : oldMixins)
        addMixin(oldMixin);

    List<MetadataParsedURL> oldAdditionalLocations = oldDocument.getAdditionalLocations();
    if (oldAdditionalLocations != null)
    {
      for (MetadataParsedURL otherLocation : oldAdditionalLocations)
        addAdditionalLocation(otherLocation);
    }

    // TODO -- are there other values that should be propagated?! -- can use
    // MetadataFieldDescriptors.
  }

  public SemanticInLinks getSemanticInlinks()
  {
    SemanticInLinks result = this.semanticInlinks;
    if (result == null)
    {
      // TODO add concurrency control?!
      result = new SemanticInLinks();
      this.semanticInlinks = result;
    }
    return result;
  }

  public void addSemanticInlink(SemanticAnchor semanticAnchor, Document source)
  {
    getSemanticInlinks().add(semanticAnchor, source);
  }

  public void addInlink(Document source)
  {
    getSemanticInlinks().add(source);
  }

  public boolean queueDownload(Continuation dispatchTarget)
  {
    DocumentClosure documentClosure = getOrConstructClosure();
    if (documentClosure == null)
      return false;
    if (dispatchTarget != null)
      documentClosure.addContinuation(dispatchTarget);
    return documentClosure.queueDownload();
  }

  public boolean queueDownload()
  {
    return queueDownload(null);
  }

  /**
   * Queue this document for downloading, and then block to wait for it to be downloaded and parsed
   * done.
   * 
   * @param timeoutMs
   *          The longest time to wait, in milliseconds.
   * @return If the downloading and parsing succeeded, as queueDownload().
   * @throws InterruptedException
   */
  public boolean queueDownloadAndWait(long timeoutMs) throws InterruptedException
  {
    if (downloadStatus != DownloadStatus.DOWNLOAD_DONE)
    {
      final Object lock = new Object();
      boolean result = false;
      synchronized (lock)
      {
        result = queueDownload(new Continuation<DocumentClosure>()
        {
          @Override
          public void callback(DocumentClosure o)
          {
            synchronized (lock)
            {
              lock.notifyAll();
            }
          }
        });
        if (downloadStatus != DownloadStatus.DOWNLOAD_DONE)
        {
          lock.wait(timeoutMs);
        }
      }
      return result;
    }
    return true;
  }

  /**
   * Lookout for instances of the AnonymousDocument.
   * 
   * @return false in the base class and most subs.
   */
  public boolean isAnonymous()
  {
    return false;
  }

  public boolean isRecycled()
  {
    return super.isRecycled() || downloadStatus == DownloadStatus.RECYCLED;
  }

  void setRecycled()
  {
    LocalDocumentCollections globalCollection = semanticsScope.getLocalDocumentCollection();
    globalCollection.setRecycled(getLocation());
    if (getAdditionalLocations() != null)
    {
      for (MetadataParsedURL additionalMPurl : getAdditionalLocations())
        globalCollection.setRecycled(additionalMPurl.getValue());
    }
  }

  @Override
  public void recycle()
  {
    recycle(new HashSet<Metadata>());
  }

  @Override
  public synchronized void recycle(HashSet<Metadata> visitedMetadata)
  {
    super.recycle(visitedMetadata);
    if (semanticInlinks != null)
    {
      semanticInlinks.recycle();
      semanticInlinks = null;
    }
    if (parserResult != null)
    {
      parserResult.recycle();
      parserResult = null;
    }
    this.downloadStatus = DownloadStatus.RECYCLED;
  }

  @Override
  public String toString()
  {
    return super.toString() + "[" + getLocation() + "]";
  }

  public boolean isJustCrawl()
  {
    return false;
  }

  public void downloadAndParseDone(DocumentParser documentParser)
  {
    setDownloadStatus(DownloadStatus.DOWNLOAD_DONE);
  }

  public boolean isSeed()
  {
    return false;
  }

  public void addCandidateOutlink(Document newOutlink)
  {
    // Overridden in subclasses
  }

  public void perhapsAddDocumentClosureToPool()
  {
    // Overridden in subclasses
  }

  public String getLocationsString()
  {
    String result;
    if (getAdditionalLocations() == null || getAdditionalLocations().size() == 0)
      result = getLocationMetadata().toString();
    else
    {
      StringBuilder buffy = new StringBuilder(getLocationMetadata().toString());
      for (MetadataParsedURL otherLocation : getAdditionalLocations())
      {
        buffy.append(',');
        buffy.append(otherLocation.toString());
      }
      result = buffy.toString();
    }
    return result;
  }

  /**
   * @return the parserResult
   */
  public ParserResult getParserResult()
  {
    return parserResult;
  }

  /**
   * @param parserResult
   *          the parserResult to set
   */
  public void setParserResult(ParserResult parserResult)
  {
    this.parserResult = parserResult;
  }

  /**
   * @return the seed
   */
  public Seed getSeed()
  {
    return seed;
  }

  /**
   * @param seed
   *          the seed to set
   */
  public void setSeed(Seed seed)
  {
    this.seed = seed;
  }

  /**
   * If this Container was a search, the index number of that search among the searches being
   * aggregated at one time. Otherwise, -1.
   * 
   * @return The search index number or -1 if not a search.
   */
  public int searchNum()
  {
    if (isTrueSeed && (seed instanceof SearchState))
    {
      return ((SearchState) seed).searchNum();
    }
    return -1;
  }

  /**
   * Called for true seed Containers. Calling this method does more than bind the Seed object with
   * the Container in the model. It also sets the crucial isSeed flag, establishing that this
   * Container is truly a Seed.
   * <p/>
   * NB: The seed object will also be bound with ancestors of the Container.
   * 
   * @param seed
   */
  public void setAsTrueSeed(Seed seed)
  {
    // associateSeed(seed);
    this.seed = seed;
    isTrueSeed = true;
  }

  /**
   * Indicate that this Container is being processed via DnD.
   * 
   */
  void setDnd()
  {
    isDnd = true;
  }

  public boolean isDnd()
  {
    return isDnd;
  }

  @Override
  public boolean hasLocation()
  {
    return getLocationMetadata() != null;
  }

  public boolean hasLocation(ParsedURL location)
  {
    if (location.equals(getLocation()))
      return true;
    List<MetadataParsedURL> additionalLocations = getAdditionalLocations();
    if (additionalLocations != null && additionalLocations.size() > 0)
    {
      for (MetadataParsedURL mpurl : additionalLocations)
      {
        if (mpurl != null && location.equals(mpurl.getValue()))
          return true;
      }
    }
    return false;
  }

  public DownloadStatus getDownloadStatus()
  {
    return downloadStatus;
  }

  public void setDownloadStatus(DownloadStatus downloadStatus)
  {
    if (lastActionTimestamp == 0)
    {
      lastActionTimestamp = System.currentTimeMillis();
    }
    long now = System.currentTimeMillis();
    long delta = now - lastActionTimestamp;
    transitionTimeToDownloadStatus.put(downloadStatus, delta);
    this.downloadStatus = downloadStatus;
    lastActionTimestamp = now;
  }

  public HashMap<DownloadStatus, Long> getTransitionTimeToDownloadStatus()
  {
    return transitionTimeToDownloadStatus;
  }

  public ParsedURL getLocationOrFirstAdditionLocation()
  {
    if (getLocation() != null)
      return getLocation();
    if (getAdditionalLocations() != null && getAdditionalLocations().size() > 0)
      return getAdditionalLocations().get(0).getValue();
    return null;
  }

  /**
   * Get a collection of clippings, if we have one.
   * 
   * @return always null in the base class
   */
  public List<Clipping> getClippings()
  {
    return null;
  }

  /**
   * Deserialize A JSON metadataString to form a Document. If that works, add it to the global
   * collection, with location as key. Set its downloadStatus to DOWNLOAD_DONE.
   * 
   * @param metadataString
   * @param semanticsScope
   * @param metadataFormat
   * @return
   */
  public static Document constructAndMapFromJson(String jsonMetadata,
                                                 SemanticsGlobalScope semanticsScope)
  {
    return constructAndMapFromSerialized(jsonMetadata, semanticsScope, StringFormat.JSON);
  }

  /**
   * Deserialize the metadataString to form a Document. If that works, add it to the global
   * collection, with location as key. Set its downloadStatus to DOWNLOAD_DONE.
   * 
   * @param metadataString
   * @param semanticsScope
   * @param metadataFormat
   * @return
   */
  public static Document constructAndMapFromSerialized(String metadataString,
                                                       SemanticsGlobalScope semanticsScope,
                                                       StringFormat metadataFormat)
  {
    Document document = null;
    try
    {
      SimplTypesScope documentsTypeScope = semanticsScope.getDocumentsTypeScope();
      document = (Document) documentsTypeScope.deserialize(metadataString, metadataFormat);
      if (document != null)
      {
        document.setSemanticsSessionScope(semanticsScope);
        document.setDownloadStatus(DownloadStatus.DOWNLOAD_DONE);
        // andruid 2012/08 -- perhaps this should be unconditional put to change map to latest
        // extracted metadata
        semanticsScope.putDocumentIfAbsent(document);
      }
    }
    catch (SIMPLTranslationException e)
    {
      e.printStackTrace();
    }
    return document;
  }
}
