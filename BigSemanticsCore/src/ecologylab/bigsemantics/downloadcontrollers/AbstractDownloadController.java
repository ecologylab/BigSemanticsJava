package ecologylab.bigsemantics.downloadcontrollers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;

import ecologylab.bigsemantics.Utils;
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

  private String              userAgent;

  private ParsedURL           location;

  private List<ParsedURL>     redirectedLocations;

  private int                 status;

  private String              statusMessage;

  private String              charset;

  private String              mimeType;

  private Map<String, String> headers;

  private String              content;

  private InputStream         contentStream;

  private boolean             isGood;

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
  public ParsedURL getLocation()
  {
    return location;
  }

  protected void setLocation(ParsedURL location)
  {
    this.location = location;
  }

  @Override
  public List<ParsedURL> getRedirectedLocations()
  {
    return redirectedLocations;
  }

  protected void setRedirectedLocations(List<ParsedURL> redirectedLocations)
  {
    this.redirectedLocations = redirectedLocations;
  }

  protected void addRedirectedLocation(ParsedURL location)
  {
    if (redirectedLocations == null)
    {
      redirectedLocations = new ArrayList<ParsedURL>();
    }
    redirectedLocations.add(location);
  }

  @Override
  public int getStatus()
  {
    return status;
  }

  protected void setStatus(int status)
  {
    this.status = status;
  }

  @Override
  public String getStatusMessage()
  {
    return statusMessage;
  }

  protected void setStatusMessage(String statusMessage)
  {
    this.statusMessage = statusMessage;
  }

  @Override
  public String getCharset()
  {
    return charset;
  }

  protected void setCharset(String charset)
  {
    this.charset = charset;
  }

  @Override
  public String getMimeType()
  {
    return mimeType;
  }

  protected void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  @Override
  public String getHeader(String name)
  {
    if (headers != null)
    {
      return headers.get(name);
    }
    return null;
  }

  protected void addHeader(String name, String value)
  {
    if (headers == null)
    {
      headers = new HashMap<String, String>();
    }
    headers.put(name, value);
  }

  @Override
  public String getContent() throws IOException
  {
    if (content == null)
    {
      synchronized (this)
      {
        if (content == null)
        {
          if (contentStream != null)
          {
            String charsetName = this.getCharset();
            Charset charset = Utils.getCharsetByName(charsetName, Charsets.UTF_8);
            content = Utils.readInputStream(contentStream, charset);
            contentStream = new ByteArrayInputStream(content.getBytes(charset));
          }
        }
      }
    }
    return content;
  }

  protected void setContent(String content)
  {
    this.content = content;
  }

  @Override
  public InputStream getInputStream()
  {
    if (contentStream == null)
    {
      synchronized (this)
      {
        if (contentStream == null)
        {
          if (content != null)
          {
            String charsetName = this.getCharset();
            Charset charset = Utils.getCharsetByName(charsetName, Charsets.UTF_8);
            contentStream = new ByteArrayInputStream(content.getBytes(charset));
          }
        }
      }
    }
    return contentStream;
  }

  protected void setInputStream(InputStream inputStream)
  {
    this.contentStream = inputStream;
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

}
