package ecologylab.bigsemantics.downloadcontrollers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;

import ecologylab.bigsemantics.Utils;

/**
 * A convenient class for DownloadController implementations. Subclass only needs to set either
 * the content string or the content input stream, then getContent() and getInputStream() should
 * both work.
 * 
 * @author quyin
 */
public abstract class AbstractDownloadController implements DownloadController
{

  private String      content;

  private InputStream contentStream;

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

}
