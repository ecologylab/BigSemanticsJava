package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;
import java.util.List;

import ecologylab.net.ParsedURL;

/**
 * For cached pages.
 * 
 * @author quyin
 */
public class CachedPageDownloadController extends AbstractDownloadController
{
  
  public CachedPageDownloadController(ParsedURL location,
                                      List<ParsedURL> additionalLocations,
                                      String charset,
                                      String mimeType,
                                      int statusCode,
                                      String statusMessage,
                                      String cachedRawContent)
  {
    if (location != null && cachedRawContent != null)
    {
      setLocation(location);
      setRedirectedLocations(additionalLocations);
      setCharset(charset);
      setMimeType(mimeType);
      setStatus(statusCode);
      setStatusMessage(statusMessage);
      setContent(cachedRawContent);
      setIsGood(true);
    }
  }

  @Override
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    return isGood();
  }

}
