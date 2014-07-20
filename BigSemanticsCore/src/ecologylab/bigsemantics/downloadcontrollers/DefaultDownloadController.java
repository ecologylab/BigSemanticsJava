package ecologylab.bigsemantics.downloadcontrollers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.net.ParsedURL;

/**
 * The default class that handles document downloading.
 * 
 * @author colton
 */
public class DefaultDownloadController extends AbstractDownloadController
{

  static Logger             logger;

  static
  {
    logger = LoggerFactory.getLogger(DefaultDownloadController.class);
  }

  public static final int   MAX_REDIRECTS = 10;

  private HttpURLConnection connection;

  /**
   * Opens the HttpURLConnection to the specified location and downloads the resource
   * 
   * @param location
   *          a ParsedURL object pointing to a resource
   * @return a boolean indicating the success status of the connection
   */
  public boolean accessAndDownload(ParsedURL location) throws IOException
  {
    setLocation(location);

    try
    {
      connection = (HttpURLConnection) location.url().openConnection();

      // Attempt to follow redirects until redirect limit is reached
      int redirects = 0;
      Set<String> redirectedLocs = new HashSet<String>();
      while (true)
      {
        String userAgent = getUserAgent();
        if (userAgent != null)
        {
          connection.setRequestProperty("User-Agent", userAgent);
        }
        int httpStatus = connection.getResponseCode();
        setStatus(httpStatus);

        if (isRedirecting(httpStatus) && redirects <= MAX_REDIRECTS)
        {
          String redirectedLoc = connection.getHeaderField("Location");
          addToSet(redirectedLocs, redirectedLoc);
          connection = (HttpURLConnection) new URL(redirectedLoc).openConnection();
        }
        else
        {
          break;
        }
      }
      if (redirectedLocs.size() > 0)
      {
        for (String loc : redirectedLocs)
        {
          addRedirectedLocation(ParsedURL.getAbsolute(loc));
        }
      }

      if (connection.getContentType() != null)
      {
        String[] contentType = connection.getContentType().split(";");

        setMimeType(contentType[0]);
        String charset = contentType.length > 1
            ? contentType[1].trim().substring("charset=".length())
            : null;
        setCharset(charset);
      }

      int httpStatus = getStatus();
      boolean isGood = (200 <= httpStatus && httpStatus < 300);
      setIsGood(isGood);

      if (isGood)
      {
        InputStream contentStream = connection.getInputStream();
        this.setInputStream(contentStream);
        setStatusMessage(connection.getResponseMessage());
      }
    }
    catch (MalformedURLException e)
    {
      logger.error("Error connecting to " + location, e);
      setIsGood(false);
    }

    return isGood();
  }

  private boolean isRedirecting(int httpStatus)
  {
    return httpStatus == HttpURLConnection.HTTP_MOVED_PERM
        || httpStatus == HttpURLConnection.HTTP_MOVED_TEMP;
  }

  private void addToSet(Set<String> redirectedLocs, String redirectedLoc)
  {
    if (redirectedLoc != null && !redirectedLoc.equals(getLocation().toString()))
    {
      redirectedLocs.add(redirectedLoc);
    }
  }

}
