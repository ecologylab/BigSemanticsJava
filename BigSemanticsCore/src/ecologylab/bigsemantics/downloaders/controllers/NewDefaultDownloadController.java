package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
<<<<<<< HEAD
import java.net.URL;

=======
>>>>>>> b0d474b2c0e93d896d7f1ddd1038bcf65d0bc20d
import ecologylab.net.ParsedURL;

/**
 * Replaces the default download controller
 * 
 * @author colton
 */
public class NewDefaultDownloadController implements NewDownloadController
{
<<<<<<< HEAD
	private boolean connectedStatus;
	private int httpStatus;
	private String charset;
	private String mimeType;
	private String userAgent;
	private String httpStatusMessage;
	private InputStream connectionStream;
	private ParsedURL originalPurl;
	private ParsedURL connectionPurl;
=======
	private String userAgent;
	private ParsedURL purl;
>>>>>>> b0d474b2c0e93d896d7f1ddd1038bcf65d0bc20d
	private HttpURLConnection connection;
	
	public NewDefaultDownloadController()
	{
	}

<<<<<<< HEAD
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

			if (httpStatus != HttpURLConnection.HTTP_OK
				&& (httpStatus == HttpURLConnection.HTTP_MOVED_PERM
				|| httpStatus == HttpURLConnection.HTTP_MOVED_TEMP))
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
			
			connectionStream = connection.getInputStream();
			connectionPurl = new ParsedURL(connection.getURL());
			httpStatusMessage = connection.getResponseMessage();
			connectedStatus = (200 <= httpStatus && httpStatus < 300);
		}
		catch (MalformedURLException e)
		{
			connectedStatus = false;
		}

		return connectedStatus;
	}

=======
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
>>>>>>> b0d474b2c0e93d896d7f1ddd1038bcf65d0bc20d
	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

<<<<<<< HEAD
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

=======
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
>>>>>>> b0d474b2c0e93d896d7f1ddd1038bcf65d0bc20d
	public String getHeader(String name)
	{
		return connection.getHeaderField(name);
	}

<<<<<<< HEAD
	public InputStream getInputStream()
	{
		return connectionStream;
=======
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
>>>>>>> b0d474b2c0e93d896d7f1ddd1038bcf65d0bc20d
	}
}
