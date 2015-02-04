package ecologylab.bigsemantics.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.ProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This strategy fixes some problems with the default redirect strategy.
 * 
 * @author quyin
 */
class BasicRedirectStrategy extends DefaultRedirectStrategy
{

  private static Logger logger;

  static
  {
    logger = LoggerFactory.getLogger(BasicRedirectStrategy.class);
  }

  private List<String>  redirectedLocations;

  private List<String> redirectedLocations()
  {
    if (redirectedLocations == null)
    {
      synchronized (this)
      {
        if (redirectedLocations == null)
        {
          redirectedLocations = new ArrayList<String>();
        }
      }
    }
    return redirectedLocations;
  }

  public List<String> getRedirectedLocations()
  {
    return redirectedLocations;
  }

  @Override
  protected URI createLocationURI(String location) throws ProtocolException
  {
    URIBuilder ub;
    try
    {
      ub = HttpClientUtils.toUriBuilder(location);
      URI uri = ub.build();
      redirectedLocations().add(location);
      return uri;
    }
    catch (UnsupportedEncodingException e)
    {
      logger.error("Can't create URI using response's location header: [" + location + "]", e);
    }
    catch (URISyntaxException e)
    {
      logger.error("Can't create URI using response's location header: [" + location + "]", e);
    }
    return null;
  }

}
