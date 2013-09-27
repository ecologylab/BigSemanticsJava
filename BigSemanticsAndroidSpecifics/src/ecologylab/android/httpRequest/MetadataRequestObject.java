package ecologylab.android.httpRequest;

import ecologylab.net.ParsedURL;

/**
 * a request object for requesting metadata from the semantics service.
 * @author fei
 *
 */
public class MetadataRequestObject {
	private ParsedURL url;
	private String serializedMetadata;
	private HttpRequester requester;
	private String server_url_prefix;
	
	/**
	 * create a request object for requesting metadata from the semantics service.
	 * @param url
	 * @param requester
	 * 			the activity that makes the request
	 */
	public MetadataRequestObject(ParsedURL url, HttpRequester requester, String server_url_prefix)
	{		
		this.url = url;
		this.requester = requester;
		this.server_url_prefix = server_url_prefix;
	}
	
	/**
	 * get the activity that makes the request
	 * @return
	 */
	public HttpRequester getRequester() {
		return requester;
	}

	/**
	 * set the requester
	 * @param requester
	 */
	public void setRequester(HttpRequester requester) {
		this.requester = requester;
	}

	public String getServer_url_prefix() {
		return server_url_prefix;
	}

	/**
	 * get the url of the request
	 * @return
	 */
	public ParsedURL getUrl() {
		return url;
	}
	
	public void setUrl(ParsedURL url) {
		this.url = url;
	}
	
	/**
	 * get metadata in the serialized form
	 * @return
	 */
	public String getSerializedMetadata() {
		return serializedMetadata;
	}
	
	public void setSerializedMetadata(String serializedMetadata) {
		this.serializedMetadata = serializedMetadata;
	}
	
	
}
