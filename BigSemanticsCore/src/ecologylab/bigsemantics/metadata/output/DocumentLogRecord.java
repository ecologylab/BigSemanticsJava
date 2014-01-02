package ecologylab.bigsemantics.metadata.output;

import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class DocumentLogRecord extends DownloadableLogRecord
{

  @simpl_scalar
  private ParsedURL           documentUrl;

  @simpl_scalar
  private long                msHtmlDownload;

  @simpl_scalar
  private long                msExtraction;

  @simpl_scalar
  private long                msSerialization;

  @simpl_scalar
  private long                msContentReadingAndDomCreation;

  @simpl_scalar
  private long                msContentBodyAndClippings;

  @simpl_scalar
  private long                msImageTextParserCallingSuperParse;

  @simpl_scalar
  private long                msMetadataCacheLookup;

  @simpl_scalar
  private long                msMetadataCaching;

  @simpl_scalar
  private long                msCompoundDocumentDnpDone;

  @simpl_scalar
  private boolean             InMemDocumentCacheHit;

  @simpl_scalar
  private boolean             PersistentDocumentCacheHit;

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

  public long getMsSerialization()
  {
    return msSerialization;
  }

  public void setMsSerialization(long msSerialization)
  {
    this.msSerialization = msSerialization;
  }

  public long getMsContentReadingAndDomCreation()
  {
    return msContentReadingAndDomCreation;
  }

  public void setMsContentReadingAndDomCreation(long msContentReadingAndDomCreation)
  {
    this.msContentReadingAndDomCreation = msContentReadingAndDomCreation;
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

  public long getMsMetadataCacheLookup()
  {
    return msMetadataCacheLookup;
  }

  public void setMsMetadataCacheLookup(long msMetadataCacheLookup)
  {
    this.msMetadataCacheLookup = msMetadataCacheLookup;
  }

  public long getMsMetadataCaching()
  {
    return msMetadataCaching;
  }

  public void setMsMetadataCaching(long msMetadataCaching)
  {
    this.msMetadataCaching = msMetadataCaching;
  }

  public long getMsCompoundDocumentDnpDone()
  {
    return msCompoundDocumentDnpDone;
  }

  public void setMsCompoundDocumentDnpDone(long msCompoundDocumentDnpDone)
  {
    this.msCompoundDocumentDnpDone = msCompoundDocumentDnpDone;
  }

  public boolean isInMemDocumentCacheHit()
  {
    return InMemDocumentCacheHit;
  }

  public void setInMemDocumentCacheHit(boolean documentCollectionCacheHit)
  {
    InMemDocumentCacheHit = documentCollectionCacheHit;
  }

  public boolean isPersistentDocumentCacheHit()
  {
    return PersistentDocumentCacheHit;
  }

  public void setPersisentDocumentCacheHit(boolean semanticsDiskCacheHit)
  {
    PersistentDocumentCacheHit = semanticsDiskCacheHit;
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
