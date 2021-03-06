package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;
import java.util.List;

import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
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
      setOriginalLocation(location);

      SimplHttpResponse resp = new SimplHttpResponse();
      resp.setUrl(location.toString());
      if (additionalLocations != null)
      {
        for (ParsedURL purl : additionalLocations)
        {
          resp.addOtherUrl(purl.toString());
        }
      }
      if (mimeType != null)
      {
        String contentType =
            charset == null ? mimeType : String.format("%s; charset=%s", mimeType, charset);
        resp.setHeader("Content-Type", contentType);
      }
      resp.setCode(statusCode);
      resp.setMessage(statusMessage);
      resp.setContent(cachedRawContent);

      setHttpResponse(resp);
      setIsGood(true);
    }
  }

  @Override
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    return isGood();
  }

}
