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
	ParsedURL documentUrl;
	
	@simpl_scalar
	long mSecInHtmlDownload;
	
	@simpl_scalar
	long mSecInExtraction;
	
	@simpl_scalar
	long mSecInSerialization;
	
	@simpl_scalar
	long msContentReadingAndDomCreation;
	
	@simpl_scalar
	long msContentBodyAndClippings;
	
	@simpl_scalar
	long msImageTextParserCallingSuperParse;
	
	@simpl_scalar
	long msMetadataCacheLookup;
	
	@simpl_scalar
	long msMetadataCaching;
	
	@simpl_scalar
  long msCompoundDocumentDnpDone;

	@simpl_scalar
	boolean DocumentCollectionCacheHit;
	
	@simpl_scalar
	boolean SemanticsDiskCacheHit;
	
	@simpl_composite
	DocumentErrorRecord	errorRecord;

	public ParsedURL getDocumentUrl()
	{
		return documentUrl;
	}

	public void setDocumentUrl(ParsedURL documentUrl)
	{
		this.documentUrl = documentUrl;
	}

	public boolean isDocumentCollectionCacheHit()
	{
		return DocumentCollectionCacheHit;
	}

	public void setDocumentCollectionCacheHit(boolean documentCollectionCacheHit)
	{
		DocumentCollectionCacheHit = documentCollectionCacheHit;
	}

	public boolean isSemanticsDiskCacheHit()
	{
		return SemanticsDiskCacheHit;
	}

	public void setSemanticsDiskCacheHit(boolean semanticsDiskCacheHit)
	{
		SemanticsDiskCacheHit = semanticsDiskCacheHit;
	}

	public long getmSecInHtmlDownload() {
		return mSecInHtmlDownload;
	}

	public void setmSecInHtmlDownload(long mSecInHtmlDownload) {
		this.mSecInHtmlDownload = mSecInHtmlDownload;
	}

	public long getmSecInExtraction() {
		return mSecInExtraction;
	}

	public void setmSecInExtraction(long mSecInExtraction) {
		this.mSecInExtraction = mSecInExtraction;
	}

	public long getmSecInSerialization() {
		return mSecInSerialization;
	}

	public void setmSecInSerialization(long mSecInSerialization) {
		this.mSecInSerialization = mSecInSerialization;
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

  static final public DocumentLogRecord DUMMY;
	
	static
	{
	  DUMMY = new DocumentLogRecord();
	}

}
