package ecologylab.bigsemantics.downloadcontrollers;

import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.net.ParsedURL;

/**
 * A convenient class for DownloadController implementations. Subclass only needs to set either the
 * content string or the content input stream, then getContent() and getInputStream() should both
 * work.
 * 
 * @author quyin
 */
public abstract class AbstractDownloadController implements DownloadController
{

  private String            userAgent;

  private ParsedURL         originalLocation;

  private boolean           isGood;

  private SimplHttpResponse httpResponse;

  public String getUserAgent()
  {
    return userAgent;
  }

  @Override
  public void setUserAgent(String userAgent)
  {
    this.userAgent = userAgent;
  }

  @Override
  public ParsedURL getOriginalLocation()
  {
    return originalLocation;
  }

  protected void setOriginalLocation(ParsedURL location)
  {
    this.originalLocation = location;
  }

  @Override
  public boolean isGood()
  {
    return isGood;
  }

  protected void setIsGood(boolean isGood)
  {
    this.isGood = isGood;
  }

  @Override
  public SimplHttpResponse getHttpResponse()
  {
    return httpResponse;
  }

  protected void setHttpResponse(SimplHttpResponse httpResponse)
  {
    this.httpResponse = httpResponse;
  }

  public void recycle()
  {
    this.isGood = false;
    this.originalLocation = null;
    this.httpResponse = null;
  }

}
