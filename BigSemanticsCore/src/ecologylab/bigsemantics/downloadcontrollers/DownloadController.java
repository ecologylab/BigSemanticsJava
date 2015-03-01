package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;

import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.net.ParsedURL;

/**
 * Interface for download controllers which rely only on a ParsedURL
 * 
 * @author colton
 */
public interface DownloadController
{

  /**
   * Sets the user agent
   * 
   * @param userAgent
   *          a string representation of the user agent
   */
  public void setUserAgent(String userAgent);

  /**
   * Opens the HttpURLConnection to the specified location and downloads the resource
   * 
   * @param location
   *          a ParsedURL object pointing to a resource
   * @return a boolean indicating the success status of the connection
   */
  public boolean accessAndDownload(ParsedURL location) throws IOException;

  /**
   * Returns a ParsedURL object corresponding to the original resource location used to initiate the
   * connection. This value does not change if the connection is redirected
   * 
   * @return a ParsedURL object corresponding to the original resource location used to initiate the
   *         connection
   */
  public ParsedURL getOriginalLocation();

  /**
   * Returns a boolean indicating if the HTTP response code is that of a good connection
   * 
   * @return a boolean indicating if the HTTP response code is that of a good connection
   */
  public boolean isGood();

  /**
   * @return The HTTP response. If accessAndDownload() and isGood() returns true, this should never
   *         be null.
   */
  public SimplHttpResponse getHttpResponse();

  /**
   * Release resources held by this controller. The controller is expected to be ready for next call
   * to accessAndDownload() after this.
   */
  public void recycle();

}
