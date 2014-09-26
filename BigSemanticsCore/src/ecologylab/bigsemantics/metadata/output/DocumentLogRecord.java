package ecologylab.bigsemantics.metadata.output;

import ecologylab.bigsemantics.documentcache.PersistenceMetaInfo;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class DocumentLogRecord extends DownloadableLogRecord
{

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private ParsedURL           documentUrl;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private boolean             InMemDocumentCacheHit;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private boolean             persistentCacheHit;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msPersistentHtmlRead;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msPersistentDocumentRead;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msPersistentCacheWrite;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msPersistentCacheUpdate;

  @simpl_composite
  private PersistenceMetaInfo persistenceMetaInfo;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msHtmlDownload;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msExtraction;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msDomCreation;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msCompoundDocumentDnpDone;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msContentBodyAndClippings;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msImageTextParserCallingSuperParse;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private long                msSerialization;

  @simpl_composite
  private DocumentErrorRecord errorRecord;

  public ParsedURL getDocumentUrl()
  {
    return documentUrl;
  }

  public void setDocumentUrl(ParsedURL documentUrl)
  {
    this.documentUrl = documentUrl;
  }

  public boolean isInMemDocumentCacheHit()
  {
    return InMemDocumentCacheHit;
  }

  public void setInMemDocumentCacheHit(boolean inMemDocumentCacheHit)
  {
    InMemDocumentCacheHit = inMemDocumentCacheHit;
  }

  public boolean isPersistentCacheHit()
  {
    return persistentCacheHit;
  }

  public void setPersistentCacheHit(boolean persistentCacheHit)
  {
    this.persistentCacheHit = persistentCacheHit;
  }

  public long getMsPersistentHtmlRead()
  {
    return msPersistentHtmlRead;
  }

  public void setMsPersistentHtmlRead(long msPersistentHtmlRead)
  {
    this.msPersistentHtmlRead = msPersistentHtmlRead;
  }

  public long getMsPersistentDocumentRead()
  {
    return msPersistentDocumentRead;
  }

  public void setMsPersistentDocumentRead(long msPersistentDocumentRead)
  {
    this.msPersistentDocumentRead = msPersistentDocumentRead;
  }

  public long getMsPersistentCacheWrite()
  {
    return msPersistentCacheWrite;
  }

  public void setMsPersistentCacheWrite(long msPersistentCacheWrite)
  {
    this.msPersistentCacheWrite = msPersistentCacheWrite;
  }

  public long getMsPersistentCacheUpdate()
  {
    return msPersistentCacheUpdate;
  }

  public void setMsPersistentCacheUpdate(long msPersistentCacheUpdate)
  {
    this.msPersistentCacheUpdate = msPersistentCacheUpdate;
  }

  public PersistenceMetaInfo getPersistenceMetaInfo()
  {
    return persistenceMetaInfo;
  }

  public void setPersistenceMetaInfo(PersistenceMetaInfo persistenceMetaInfo)
  {
    this.persistenceMetaInfo = persistenceMetaInfo;
  }

  public long getMsHtmlDownload()
  {
    return msHtmlDownload;
  }

  public void setMsHtmlDownload(long msHtmlDownload)
  {
    this.msHtmlDownload = msHtmlDownload;
  }

  public long getMsExtraction()
  {
    return msExtraction;
  }

  public void setMsExtraction(long msExtraction)
  {
    this.msExtraction = msExtraction;
  }

  public long getMsDomCreation()
  {
    return msDomCreation;
  }

  public void setMsDomCreation(long msDomCreation)
  {
    this.msDomCreation = msDomCreation;
  }

  public long getMsCompoundDocumentDnpDone()
  {
    return msCompoundDocumentDnpDone;
  }

  public void setMsRichDocumentDnpDone(long msCompoundDocumentDnpDone)
  {
    this.msCompoundDocumentDnpDone = msCompoundDocumentDnpDone;
  }

  public long getMsContentBodyAndClippings()
  {
    return msContentBodyAndClippings;
  }

  public void setMsContentBodyAndClippings(long msContentBodyAndClippings)
  {
    this.msContentBodyAndClippings = msContentBodyAndClippings;
  }

  public long getMsImageTextParserCallingSuperParse()
  {
    return msImageTextParserCallingSuperParse;
  }

  public void setMsImageTextParserCallingSuperParse(long msImageTextParserCallingSuperParse)
  {
    this.msImageTextParserCallingSuperParse = msImageTextParserCallingSuperParse;
  }

  public long getMsSerialization()
  {
    return msSerialization;
  }

  public void setMsSerialization(long msSerialization)
  {
    this.msSerialization = msSerialization;
  }

  public DocumentErrorRecord getErrorRecord()
  {
    return errorRecord;
  }

  public void setErrorRecord(DocumentErrorRecord errorRecord)
  {
    this.errorRecord = errorRecord;
  }

  static final public DocumentLogRecord DUMMY;

  static
  {
    DUMMY = new DocumentLogRecord();
  }

}
