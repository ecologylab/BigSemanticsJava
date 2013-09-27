package ecologylab.android.httpRequest;

/**
 * provides a call back interface when gets the metadata from the semantic service
 * @author fei
 *
 */
public interface HttpRequester {
	public void callbackFromHttpRequest(MetadataRequestObject o);
}
