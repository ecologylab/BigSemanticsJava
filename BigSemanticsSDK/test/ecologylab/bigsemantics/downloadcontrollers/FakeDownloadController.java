package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.net.ParsedURL;

/**
 * A fake DownloadController for unit test.
 * 
 * @author quyin
 */
public class FakeDownloadController extends AbstractDownloadController
{

  private FakeDownloadControllerFactory factory;

  private HttpResponse                  result;

  private List<ParsedURL>               redirectedPurls;

  public FakeDownloadController(FakeDownloadControllerFactory factory)
  {
    this.factory = factory;
  }

  @Override
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    String url = location.toString();
    if (factory.presetResponses().containsKey(url))
    {
      result = factory.presetResponses().get(url);
      setContent(result.getContent());
      return true;
    }
    return false;
  }

  @Override
  public String getHeader(String name)
  {
    return null;
  }

  @Override
  public void setUserAgent(String userAgent)
  {
    // no op
  }

  @Override
  public boolean isGood()
  {
    return result != null;
  }

  @Override
  public int getStatus()
  {
    return result == null ? -1 : result.getHttpRespCode();
  }

  @Override
  public String getStatusMessage()
  {
    return result == null ? null : result.getHttpRespMsg();
  }

  @Override
  public ParsedURL getLocation()
  {
    return result == null ? null : ParsedURL.getAbsolute(result.getLocation());
  }

  @Override
  public List<ParsedURL> getRedirectedLocations()
  {
    if (result != null && result.getAdditionalLocations() != null && redirectedPurls == null)
    {
      redirectedPurls = new ArrayList<ParsedURL>();
      for (String otherLoc : result.getAdditionalLocations())
      {
        redirectedPurls.add(ParsedURL.getAbsolute(otherLoc));
      }
    }
    return redirectedPurls;
  }

  @Override
  public String getMimeType()
  {
    return result == null ? null : result.getMimeType();
  }

  @Override
  public String getCharset()
  {
    return result == null ? null : result.getCharset();
  }

}
