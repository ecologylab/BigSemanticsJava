package ecologylab.bigsemantics.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.Utils;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringBuilderBaseUtils;

/**
 * 
 * @author quyin
 */
public class HttpResponseParser
{

  private static Logger logger;
  
  private static Pattern pStatusLine;

  static
  {
    logger = LoggerFactory.getLogger(HttpResponseParser.class);
    pStatusLine = Pattern.compile("HTTP/\\d.\\d\\s+(\\d+)\\s+(.*)");
  }

  public SimplHttpResponse parse(String url, InputStream istream) throws Exception
  {
    SimplHttpResponse result = new SimplHttpResponse();

    // process headers
    List<String> otherUrls = new ArrayList<String>();
    while (true)
    {
      String line = readUntil(istream, "\r\n");
      Matcher matcher = pStatusLine.matcher(line);
      if (matcher.matches())
      {
        int code = Integer.valueOf(matcher.group(1));
        result.setCode(code);
        HashMapArrayList<String, SimplHttpHeader> headers = readHeaders(istream);
        if (code >= 300 && code < 400)
        {
          // redirection happened
          otherUrls.add(url);
          String previousUrl = url;
          SimplHttpHeader locationHeader = headers.get("Location");
          if (locationHeader == null)
          {
            logger.warn("Redirection without Location header: {}", headers);
          }
          else
          {
            url = locationHeader.getValue();
            if (HttpClientUtils.looksLikeRelative(url))
            {
              url = HttpClientUtils.relativeToAbsolute(previousUrl, url);
            }
          }
          continue;
        }
        result.setHeaders(headers);
        break;
      }
      else
      {
        throw new HttpClientException("When processing raw response from "
                                      + url
                                      + ": Status line expected, got "
                                      + line);
      }
    }
    result.setUrl(url);
    if (otherUrls.size() > 0)
    {
      result.setOtherUrls(otherUrls);
    }

    // change charset if necessary
    Charset charset = Charset.forName("UTF-8");
    String charsetName = result.getCharset();
    if (charsetName != null && Charset.isSupported(charsetName))
    {
      charset = Charset.forName(charsetName);
    }

    InputStreamReader reader = new InputStreamReader(istream, charset);
    String content = Utils.readAllFromReader(reader);
    result.setContent(content);
    return result;
  }

  private HashMapArrayList<String, SimplHttpHeader> readHeaders(InputStream istream)
      throws IOException, HttpClientException
  {
    HashMapArrayList<String, SimplHttpHeader> result =
        new HashMapArrayList<String, SimplHttpHeader>();
    while (true)
    {
      String line = readUntil(istream, "\r\n");
      if (line == null || line.length() == 0)
      {
        break;
      }
      int p = line.indexOf(':');
      if (p > 0)
      {
        String name = line.substring(0, p).trim();
        String value = line.substring(p + 1).trim();
        if (name.length() > 0 && value.length() > 0)
        {
          SimplHttpHeader header = new SimplHttpHeader(name, value);
          result.put(name, header);
          continue;
        }
      }

      throw new HttpClientException("Invalid header: " + line);
    }
    return result;
  }

  private String readUntil(InputStream istream, String end) throws IOException
  {
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    while (true)
    {
      int c = istream.read();
      if (c < 0)
      {
        return null;
      }
      sb.append((char) c);
      int n = sb.length();
      if (n >= end.length() && end.equals(sb.substring(n - end.length(), n)))
      {
        String result = sb.substring(0, n - end.length());
        StringBuilderBaseUtils.release(sb);
        return result;
      }
    }
  }

}
