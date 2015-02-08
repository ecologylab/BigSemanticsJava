package ecologylab.bigsemantics.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Response message from a HTTP request. NOT THREAD SAFE! You should NOT write to a HttpResponse
 * object from multiple threads.
 * 
 * @author quyin
 */
public class SimplHttpResponse
{

  @simpl_scalar
  private String                                    url;

  /**
   * Other locations, e.g. redirected locations.
   */
  @simpl_collection("other_url")
  private List<String>                              otherUrls;

  @simpl_scalar
  private int                                       code;

  @simpl_scalar
  private String                                    message;

  @simpl_map("header")
  private HashMapArrayList<String, SimplHttpHeader> headers;

  /**
   * The content of the page, e.g. in HTML.
   */
  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  private String                                    content;

  private String                                    mimeType;

  private String                                    charset;

  private InputStream                               contentStream;

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public List<String> getOtherUrls()
  {
    return otherUrls;
  }

  List<String> otherUrls()
  {
    if (otherUrls == null)
    {
      otherUrls = new ArrayList<String>();
    }
    return otherUrls;
  }

  public void addOtherUrl(String url)
  {
    otherUrls().add(url);
  }

  protected void setOtherUrls(List<String> otherUrls)
  {
    this.otherUrls = otherUrls;
  }

  public List<ParsedURL> getOtherPurls()
  {
    if (otherUrls != null)
    {
      List<ParsedURL> result = new ArrayList<ParsedURL>();
      for (String url : otherUrls)
      {
        ParsedURL purl = ParsedURL.getAbsolute(url);
        result.add(purl);
      }
      return result;
    }
    return null;
  }

  public int getCode()
  {
    return code;
  }

  public void setCode(int code)
  {
    this.code = code;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  Map<String, SimplHttpHeader> headers()
  {
    if (headers == null)
    {
      headers = new HashMapArrayList<String, SimplHttpHeader>();
    }
    return headers;
  }

  public String getHeader(String name)
  {
    if (headers != null)
    {
      SimplHttpHeader header = headers.get(name);
      return header == null ? null : header.getValue();
    }
    return null;
  }

  public void setHeader(String name, String value)
  {
    String prevValue = getHeader(name);
    if (prevValue == null)
    {
      headers().put(name, new SimplHttpHeader(name, value));
    }
    else if (!prevValue.equals(value))
    {
      headers().get(name).setValue(value);
    }
  }

  protected void setHeaders(HashMapArrayList<String, SimplHttpHeader> headers)
  {
    this.headers = headers;
  }

  public String getMimeType()
  {
    if (mimeType == null)
    {
      parseContentType();
    }
    return mimeType;
  }

  public String getCharset()
  {
    if (charset == null)
    {
      parseContentType();
    }
    return charset;
  }

  protected void parseContentType()
  {
    String contentType = getHeader("Content-Type");
    if (contentType != null)
    {
      int p = contentType.indexOf(';');
      if (p < 0)
      {
        mimeType = contentType.trim();
      }
      else
      {
        mimeType = contentType.substring(0, p).trim();
        String charsetSpec = contentType.substring(p + 1).trim();
        if (charsetSpec.startsWith("charset="))
        {
          charset = charsetSpec.substring(8);
        }
      }
    }
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
    this.contentStream = null;
  }

  public int getContentLength()
  {
    return content == null ? 0 : content.length();
  }

  public InputStream getContentAsStream() throws UnsupportedEncodingException
  {
    if (contentStream == null && content != null)
    {
      contentStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
    }
    return contentStream;
  }

  public static SimplHttpResponse parse(String initialUrl, InputStream istream) throws Exception
  {
    return new HttpResponseParser().parse(initialUrl, istream);
  }

  public static SimplHttpResponse parse(String initialUrl,
                                        List<String> redirectedUrls,
                                        HttpResponse resp) throws ParseException, IOException
  {
    SimplHttpResponse result = new SimplHttpResponse();

    if (redirectedUrls == null || redirectedUrls.isEmpty())
    {
      result.setUrl(initialUrl);
    }
    else
    {
      List<String> otherUrls = new ArrayList<String>();
      String url = initialUrl;
      for (String redirectedUrl : redirectedUrls)
      {
        otherUrls.add(url);
        if (HttpClientUtils.looksLikeRelative(redirectedUrl))
        {
          redirectedUrl = HttpClientUtils.relativeToAbsolute(url, redirectedUrl);
        }
        url = redirectedUrl;
      }
      result.setUrl(url);
      result.otherUrls = otherUrls;
    }

    result.setCode(resp.getStatusLine().getStatusCode());
    result.setMessage(resp.getStatusLine().getReasonPhrase());

    Header[] headers = resp.getAllHeaders();
    for (Header header : headers)
    {
      result.setHeader(header.getName(), header.getValue());
    }
    result.parseContentType();

    HttpEntity entity = resp.getEntity();
    String content = EntityUtils.toString(entity, Charset.forName("UTF-8"));
    result.setContent(content);

    return result;
  }

}
