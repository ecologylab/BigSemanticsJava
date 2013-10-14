package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import ecologylab.net.ParsedURL;

/**
 * Replaces the default download controller by implementing NewDownloadConroller
 * 
 * @author colton
 */
public class DefaultDownloadController implements DownloadController
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

	public DefaultDownloadController()
	{
	}

	/**
	 * Opens the HttpURLConnection to the specified location and downloads the
	 * resource
	 * 
	 * @param location
	 *            a ParsedURL object pointing to a resource
	 * @return a boolean indicating the success status of the connection
	 */
	public boolean accessAndDownload(ParsedURL location) throws IOException
	{
		String[] contentType;

		originalPurl = location;

		try
		{
			connection = (HttpURLConnection) originalPurl.url()
					.openConnection();

			if (userAgent != null)
				connection.setRequestProperty("User-Agent", userAgent);

			httpStatus = connection.getResponseCode();

			// Attempt to follow redirects until redirect limit is reached
			for (int redirects = 0; redirects < MAX_REDIRECTS
					&& httpStatus != HttpURLConnection.HTTP_OK
					&& (httpStatus == HttpURLConnection.HTTP_MOVED_PERM 
					|| httpStatus == HttpURLConnection.HTTP_MOVED_TEMP); redirects++)
			{
				connection = (HttpURLConnection) new URL(
						connection.getHeaderField("Location")).openConnection();

				if (userAgent != null)
					connection.setRequestProperty("User-Agent", userAgent);

				httpStatus = connection.getResponseCode();
			}

			if (connection.getContentType() != null)
			{
				contentType = connection.getContentType().split(";");

				mimeType = contentType[0];
				charset = contentType.length > 1 ? contentType[1]
						.substring(" charset=".length()) : null;
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

	/**
	 * Sets the user agent
	 * 
	 * @param userAgent
	 *            a string representation of the user agent
	 */
	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	/**
	 * Returns a boolean indicating if the HTTP response code is that of a good
	 * connection
	 * 
	 * @return a boolean indicating if the HTTP response code is that of a good
	 *         connection
	 */
	public boolean isGood()
	{
		return connectedStatus;
	}

	/**
	 * Returns the status code of the HTTP response message for the connection
	 * 
	 * @return the status code of the HTTP response message for the connection
	 */
	public int getStatus()
	{
		return httpStatus;
	}

	/**
	 * Returns the message from the HTTP response message
	 * 
	 * @return the message from the HTTP response message
	 */
	public String getStatusMessage()
	{
		return httpStatusMessage;
	}

	/**
	 * Returns a ParsedURL object corresponding to the original resource
	 * location used to initiate the connection. This value does not change if
	 * the connection is redirected
	 * 
	 * @return a ParsedURL object corresponding to the original resource
	 *         location used to initiate the connection
	 */
	public ParsedURL getLocation()
	{
		return originalPurl;
	}

	/**
	 * Returns a ParsedURL object corresponding to the location of the resource
	 * with which the connection is associated. This value does change with
	 * redirects
	 * 
	 * @return a ParsedURL object corresponding to the location of the resource
	 *         with which the connection is associated
	 */
	public ParsedURL getRedirectedLocation()
	{
		return connectionPurl;
	}

	/**
	 * Returns the String representation of the content type
	 * 
	 * @return the String representation of the content type
	 */
	public String getMimeType()
	{
		return mimeType;
	}

	/**
	 * Returns the content encoding type (character set)
	 * 
	 * @return a String representation of the content encoding type (character
	 *         set)
	 */
	public String getCharset()
	{
		return charset;
	}

	/**
	 * Returns the content of the named header field
	 * 
	 * @param name
	 *            the name of the requested header field
	 * @return a String of the content of the named header field
	 */
	public String getHeader(String name)
	{
		return connection.getHeaderField(name);
	}

	/**
	 * Returns an input stream which reads from the connection
	 * 
	 * @return an input stream which reads from the connection
	 */
	public InputStream getInputStream()
	{
		return connectionStream;
	}
}
