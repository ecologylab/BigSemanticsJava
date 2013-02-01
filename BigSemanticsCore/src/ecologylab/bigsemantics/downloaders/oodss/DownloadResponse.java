/**
 * 
 */
package ecologylab.bigsemantics.downloaders.oodss;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Response message with downloaded document information
 * 
 * @author ajit
 *
 */

public class DownloadResponse extends ResponseMessage
{

	@simpl_scalar
	ParsedURL	redirectedLocation;

	@simpl_scalar
	String		location;

	@simpl_scalar
	String		mimeType;

	public DownloadResponse()
	{
	}

	public DownloadResponse(ParsedURL redirectedLocation, String location, String mimeType)
	{
		this.redirectedLocation = redirectedLocation;
		this.location = location;
		this.mimeType = mimeType;
	}

	/*
	 * Called automatically by OODSS on client
	 */
	@Override
	public void processResponse(Scope appObjScope)
	{
		// MetadataResponseListener responseListener = (MetadataResponseListener)
		// appObjScope.get("RESPONSE_LISTENER");
		// responseListener.setResponse(this.metadata);
	}

	@Override
	public boolean isOK()
	{
		return true;
	}

	public ParsedURL getRedirectedLocation()
	{
		return redirectedLocation;
	}

	public String getLocation()
	{
		return location;
	}

	public String getMimeType()
	{
		return mimeType;
	}
}
