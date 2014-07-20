package ecologylab.bigsemantics.downloadcontrollers;

import java.util.ArrayList;
import java.util.List;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Data structure for a HTTP response.
 * 
 * @author quyin
 */
public class HttpResponse
{

  @simpl_scalar
  private String       location;

  @simpl_scalar
  private int          httpRespCode;

  @simpl_scalar
  private String       httpRespMsg;

  @simpl_scalar
  private String       mimeType;

  /**
   * Charset as returned in the Content-Type section.
   */
  @simpl_scalar
  private String       charset;

  /**
   * The content of the page, e.g. in HTML.
   */
  @simpl_scalar
  private String       content;

  /**
   * Other locations, e.g. redirected locations.
   */
  @simpl_collection("location")
  private List<String> additionalLocations;

  public String getLocation()
  {
    return location;
  }

  public void setLocation(String location)
  {
    this.location = location;
  }

  public int getHttpRespCode()
  {
    return httpRespCode;
  }

  public void setHttpRespCode(int httpRespCode)
  {
    this.httpRespCode = httpRespCode;
  }

  public String getHttpRespMsg()
  {
    return httpRespMsg;
  }

  public void setHttpRespMsg(String httpRespMsg)
  {
    this.httpRespMsg = httpRespMsg;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getCharset()
  {
    return charset;
  }

  public void setCharset(String charset)
  {
    this.charset = charset;
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public List<String> getAdditionalLocations()
  {
    return additionalLocations;
  }

  public void setAdditionalLocations(List<String> additionalLocations)
  {
    this.additionalLocations = additionalLocations;
  }

  public void addAdditionalLocation(String location)
  {
    if (additionalLocations == null)
    {
      additionalLocations = new ArrayList<String>();
    }
    additionalLocations.add(location);
  }

}
