/**
 * 
 */
package ecologylab.semantics.downloaders.oodss;

import java.io.IOException;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.semantics.downloaders.NetworkDocumentDownloader;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Request message for dowanloading the document over network
 * 
 * @author ajit
 *
 */

public class DownloadRequest extends RequestMessage
{

	@simpl_scalar
	ParsedURL	location;

	@simpl_scalar
	String		userAgentString;

	public DownloadRequest()
	{
	}

	public DownloadRequest(ParsedURL location, String userAgentString)
	{
		this.location = location;
		this.userAgentString = userAgentString;
	}

	@Override
	public DownloadResponse performService(Scope clientSessionScope)
	{
		debug("document download url: " + this.location);
		
		NetworkDocumentDownloader documentDownloader = new NetworkDocumentDownloader(location,
				userAgentString);
		// boolean bChanged = false;
		ParsedURL redirectedLocation = null;
		String location = null;
		String mimeType = null;
		try
		{
			documentDownloader.connect(true);
			// additional location
			redirectedLocation = documentDownloader.getRedirectedLocation();
			// local saved location
			location = documentDownloader.getLocalLocation();
			// mimeType
			mimeType = documentDownloader.mimeType();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		debug("document from url: " + this.location + " downloaded to: " + location);
		return new DownloadResponse(redirectedLocation, location, mimeType);
	}
}
