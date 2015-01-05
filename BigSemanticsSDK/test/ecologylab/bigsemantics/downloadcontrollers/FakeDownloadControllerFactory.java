package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import ecologylab.bigsemantics.Utils;
import ecologylab.net.ParsedURL;

/**
 * A factory that produces FakeDownloadControllers. Tests can preset responses for specified
 * locations, so that no network requests need to be actually made.
 * 
 * @author quyin
 */
public class FakeDownloadControllerFactory
{

  static Logger                     logger;

  static
  {
    logger = LoggerFactory.getLogger(FakeDownloadControllerFactory.class);
  }

  private Map<String, HttpResponse> presetResponses;

  public FakeDownloadControllerFactory()
  {
    presetResponses = new HashMap<String, HttpResponse>();
  }

  /**
   * Set a preset response for the given location.
   * 
   * @param location
   * @param response
   */
  public void setResponse(String location, HttpResponse response)
  {
    if (response.getLocation() == null)
    {
      response.setLocation(location.toString());
    }
    presetResponses.put(location, response);
  }

  /**
   * Set a preset response for the given location, and use the given content as the response body.
   * 
   * @param location
   * @param response
   * @param content
   */
  public void setResponse(String location, HttpResponse response, String content)
  {
    response.setContent(content == null ? "" : content);
    setResponse(location, response);
  }

  /**
   * Set a preset response for the given location, and read the response body from the given stream.
   * 
   * @param location
   * @param response
   * @param contentStream
   */
  public void setResponse(ParsedURL location, HttpResponse response, InputStream contentStream)
  {
    Charset charset = Utils.getCharsetByName(response.getCharset(), Charsets.UTF_8);
    String content = "";
    try
    {
      content = Utils.readInputStream(contentStream, charset);
    }
    catch (IOException e)
    {
      logger.warn("Cannot read content stream for {}", location);
    }
    response.setContent(content);
  }

  protected Map<String, HttpResponse> presetResponses()
  {
    return presetResponses;
  }

  public FakeDownloadController createDownloadController()
  {
    FakeDownloadController result = new FakeDownloadController(this);
    return result;
  }

}
