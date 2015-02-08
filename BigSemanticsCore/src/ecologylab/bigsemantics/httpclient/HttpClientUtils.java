package ecologylab.bigsemantics.httpclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for the HTTP client.
 * 
 * @author quyin
 */
public class HttpClientUtils
{

  static Logger            logger;

  static HttpClientFactory factory;

  static
  {
    logger = LoggerFactory.getLogger(HttpClientUtils.class);
    factory = new HttpClientFactory();
  }

  /**
   * Convert a string URL to a URIBuilder. Do basic syntax validation and correction.
   * 
   * @param url
   * @return The resulting URIBuilder.
   * @throws URISyntaxException
   */
  public static URIBuilder toUriBuilder(String url)
      throws UnsupportedEncodingException, URISyntaxException
  {
    URIBuilder ub = null;
    try
    {
      ub = new URIBuilder(url);
    }
    catch (URISyntaxException e)
    {
      ub = parseToUriBuilder(url);
    }
    return ub;
  }

  static URIBuilder parseToUriBuilder(String url)
      throws URISyntaxException, UnsupportedEncodingException
  {
    String path = null;
    String queries = null;
    int qpos = url.indexOf('?');
    if (qpos < 0)
    {
      path = url;
    }
    else
    {
      path = url.substring(0, qpos);
      queries = url.substring(qpos + 1);
    }

    // TODO more escaping
    path = path.replace(" ", "%20");

    URIBuilder ub = new URIBuilder(path);

    if (queries != null)
    {
      String[] params = queries.split("&");
      for (int i = 0; i < params.length; ++i)
      {
        int epos = params[i].indexOf('=');
        String name = params[i].substring(0, epos);
        String value = params[i].substring(epos + 1);
        // URIBuilder.addParameter() handles URL encoding automatically, no need to do it here.
        // ub.addParameter(name, URLEncoder.encode(value, "UTF-8"));
        ub.addParameter(name, value);
      }
    }
    return ub;
  }

  public static SimplHttpResponse doGet(String userAgent,
                                        String url,
                                        Map<String, String> additionalParams)
      throws URISyntaxException, IOException
  {
    URIBuilder ub = toUriBuilder(url);

    if (additionalParams != null)
    {
      for (String key : additionalParams.keySet())
      {
        String value = additionalParams.get(key);
        ub.addParameter(key, value);
      }
    }
    URI uri = ub.build();

    String hostName = uri.getHost();
    int port = uri.getPort();
    String host = hostName + ((port > 0) ? (":" + port) : "");

    HttpGet get = new HttpGet(uri);
    get.addHeader("HOST", host); // this header is required by HTTP 1.1

    return doRequest(userAgent, url, get);
  }

  public static SimplHttpResponse doPost(String userAgent,
                                         String url,
                                         Map<String, String> formParams)
      throws URISyntaxException, IOException
  {
    URIBuilder ub = toUriBuilder(url);
    URI uri = ub.build();
    String hostName = uri.getHost();
    int port = uri.getPort();
    String host = hostName + ((port > 0) ? (":" + port) : "");
    HttpPost post = new HttpPost(uri);
    post.addHeader("HOST", host); // this header is required by HTTP 1.1

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    for (String key : formParams.keySet())
    {
      String value = formParams.get(key);
      if (value != null)
      {
        params.add(new BasicNameValuePair(key, value));
      }
    }
    HttpEntity entity = new UrlEncodedFormEntity(params, Charset.forName("UTF-8"));
    post.setEntity(entity);

    return doRequest(userAgent, url, post);
  }

  private static SimplHttpResponse doRequest(String userAgent,
                                             final String url,
                                             HttpUriRequest get) throws IOException
  {
    AbstractHttpClient client = factory.create(userAgent);
    // client must use BasicRedirectStrategy!
    final BasicRedirectStrategy redirectStrategy =
        (BasicRedirectStrategy) client.getRedirectStrategy();
    SimplHttpResponse result = client.execute(get, new ResponseHandler<SimplHttpResponse>()
    {
      @Override
      public SimplHttpResponse handleResponse(HttpResponse response)
          throws ClientProtocolException, IOException
      {
        List<String> redirectedLocations = redirectStrategy.getRedirectedLocations();
        return SimplHttpResponse.parse(url, redirectedLocations, response);
      }
    });
    return result;
  }

}
