package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import ecologylab.net.ParsedURL;

/**
 * Replaces the default download controller
 * 
 * @author colton
 */
public class NewDefaultDownloadController implements NewDownloadController
{
	private final int MAX_REDIRECTS = 10;
	private boolean connectedStatus;
	private int httpStatus;
	private String charset;
	private String mimeType;
	private String userAgent;
	private String httpStatusMessage;
	private InputStream connectionStream;
	private ParsedURL originalPurl;
	private ParsedURL connectionPurl;
	private HttpURLConnection connection;
	
	public NewDefaultDownloadController()
	{
	}

	public boolean accessAndDownload(ParsedURL location) throws IOException
	{
		String[] contentType;
		
		originalPurl = location;
		
		try
		{
			connection = (HttpURLConnection) originalPurl.url().openConnection();
			
			if (userAgent != null)
				connection.setRequestProperty("User-Agent", userAgent);

			httpStatus = connection.getResponseCode();

			for (int redirects = 0; redirects < MAX_REDIRECTS && httpStatus != HttpURLConnection.HTTP_OK
				&& (httpStatus == HttpURLConnection.HTTP_MOVED_PERM
				|| httpStatus == HttpURLConnection.HTTP_MOVED_TEMP); redirects++)
			{
				connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection();
				
				if (userAgent != null)
					connection.setRequestProperty("User-Agent", userAgent);

				httpStatus = connection.getResponseCode();
			}
			
			if (connection.getContentType() != null)
			{
				contentType = connection.getContentType().split(";");
				
				mimeType = contentType[0]; 
				charset = contentType.length > 1 ? contentType[1].substring(" charset=".length()) : null;
			}
			
			connectedStatus = (200 <= httpStatus && httpStatus < 300);
			
			if (connectedStatus)
			{
				connectionStream = connection.getInputStream();
				connectionPurl = new ParsedURL(connection.getURL());
				httpStatusMessage = connection.getResponseMessage();
			}
		}
		catch (MalformedURLException e)
		{
			connectedStatus = false;
		}

		return connectedStatus;
	}

	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	public boolean isGood()
	{
		return connectedStatus;
	}

	public int getStatus()
	{
		return httpStatus;
	}

	public String getStatusMessage()
	{
		return httpStatusMessage;
	}

	public ParsedURL getLocation()
	{
		return originalPurl;
	}

	public ParsedURL getRedirectedLocation()
	{
		return connectionPurl;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public String getCharset()
	{
		return charset;
	}

	public String getHeader(String name)
	{
		return connection.getHeaderField(name);
	}

	public InputStream getInputStream()
	{
		return connectionStream;
	}
}
