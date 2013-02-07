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
}
