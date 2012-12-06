package ecologylab.semantics.metadata.output;

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
	float secondsInHtmlDownload;
	
	@simpl_scalar
	float secondsInExtraction;
	
	@simpl_scalar
	float secondsInSerialization;
	
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
	
	public float getSecondsInHtmlDownload()
	{
		return secondsInHtmlDownload;
	}

	public void setSecondsInHtmlDownload(float secondsInHtmlDownload)
	{
		this.secondsInHtmlDownload = secondsInHtmlDownload;
	}

	public float getSecondsInExtraction()
	{
		return secondsInExtraction;
	}

	public void setSecondsInExtraction(float secondsInExtraction)
	{
		this.secondsInExtraction = secondsInExtraction;
	}

	public float getSecondsInSerialization()
	{
		return secondsInSerialization;
	}

	public void setSecondsInSerialization(float secondsInSerialization)
	{
		this.secondsInSerialization = secondsInSerialization;
	}
}
