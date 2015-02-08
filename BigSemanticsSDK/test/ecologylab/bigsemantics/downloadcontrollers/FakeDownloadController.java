package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;

import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.net.ParsedURL;

/**
 * A fake DownloadController for unit test.
 * 
 * @author quyin
 */
public class FakeDownloadController extends AbstractDownloadController
{

  private FakeDownloadControllerFactory factory;

  public FakeDownloadController(FakeDownloadControllerFactory factory)
  {
    this.factory = factory;
  }

  @Override
  public void setUserAgent(String userAgent)
  {
    // no op
  }

  @Override
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    setOriginalLocation(location);
    String url = location.toString();
    if (factory.presetResponses().containsKey(url))
    {
      SimplHttpResponse resp = factory.presetResponses().get(url);
      if (resp.getUrl() == null)
      {
        resp.setUrl(url);
      }
      setHttpResponse(resp);
      return true;
    }
    return false;
  }

  @Override
  public boolean isGood()
  {
    return getHttpResponse() != null;
  }

}
