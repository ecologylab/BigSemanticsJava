package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.httpclient.HttpClientUtils;
import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.net.ParsedURL;

/**
 * The default class that handles document downloading.
 * 
 * @author colton
 */
public class DefaultDownloadController extends AbstractDownloadController
{

  static Logger logger = LoggerFactory.getLogger(DefaultDownloadController.class);

  /**
   * Opens the HttpURLConnection to the specified location and downloads the resource
   * 
   * @param location
   *          a ParsedURL object pointing to a resource
   * @return a boolean indicating the success status of the connection
   */
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    setOriginalLocation(location);
    try
    {
      SimplHttpResponse result = HttpClientUtils.doGet(getUserAgent(), location.toString(), null);
      int httpStatus = result.getCode();
      if (httpStatus >= 200 && httpStatus < 300)
      {
        setIsGood(true);
        setHttpResponse(result);
      }
    }
    catch (Exception e)
    {
      logger.error("Error connecting to " + location, e);
      setIsGood(false);
    }
    return isGood();
  }

}
