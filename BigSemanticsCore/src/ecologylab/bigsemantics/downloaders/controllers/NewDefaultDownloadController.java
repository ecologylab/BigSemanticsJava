package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.io.InputStream;
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
	private String userAgent;
	private ParsedURL purl;
	private HttpURLConnection connection;
	
	public NewDefaultDownloadController()
	{
	}

	/**
	 * Opens the HttpURLConnection to the specified location
	 * 
	 * @param location a ParsedURL object pointing to a resource
	 * @return a boolean indicating the success status of the connection
	 */
	public boolean connect(ParsedURL location) throws IOException
	{
		boolean success;
		purl = location;

		try
		{
			connection = (HttpURLConnection) purl.url().openConnection();

			success = true;
		}
		catch (MalformedURLException e)
		{
			success = false;
		}

		return success && (200 <= connection.getResponseCode() && connection
				.getResponseCode() < 300);
	}

	/**
	 * Sets the user agent
	 * 
	 * @param userAgent a string representation of the user agent
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
		boolean success;

		try
		{
			success = (200 <= connection.getResponseCode() && connection
					.getResponseCode() < 300);
		}
		catch (Exception e)
		{
			success = false;
		}

		return success;
	}

	/**
	 * Returns the status code of the HTTP response message for the connection
	 * 
	 * @return the status code of the HTTP response message for the connection
	 */
	public int getStatus()
	{
		int status;

		try
		{
			status = connection.getResponseCode();
		}
		catch (Exception e)
		{
			status = -1;
		}

		return status;
	}

	/**
	 * Returns the message from the HTTP response message
	 * 
	 * @return the messgae from the HTTP response message
	 */
	public String getStatusMessage()
	{
		String message = null;

		try
		{
			message = connection.getResponseMessage();
		}
		catch (Exception e)
		{
			// do nothing
		}

		return message;
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
		return purl;
	}

	/** 
	 * Returns a ParsedURL object corresponding to the location of the resource with which the connection is associated. This value does change with redirects
	 *
	 * @return a ParsedURL object corresponding to the location of the resource with which the connection is associated
	 */
	public ParsedURL getRedirectedLocation()
	{
		return new ParsedURL(connection.getURL());
	}

	/**
	 * Returns the String representation of the content type
	 *
	 * @return the String representation of the content type
	 */
	public String getMimeType()
	{
		return connection.getContentType().split(";")[0];
	}

	/**
	 * Returns the content encoding type (character set)
	 * 
	 * @return a String representation of the content encoding type (character set)
	 */
	public String getCharset()
	{
		String[] result = connection.getContentType().split(";");
		
		return result.length > 1 ? 
			result[1].substring(" charset=".length()).trim() : null;
	}

	/**
	 * Returns the content of the named header field
	 * 
	 * @param name the name of the requested header field
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
		InputStream is = null;

		try
		{
			is = connection.getInputStream();
		}
		catch (IOException e)
		{
			// do nothing
		}

		return is;
	}
}
