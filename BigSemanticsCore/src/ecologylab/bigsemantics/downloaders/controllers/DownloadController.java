package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.io.InputStream;
import ecologylab.net.ParsedURL;

/** 
 * Interface for download controllers which rely only on a ParsedURL
 * 
 * @author colton
 */
public interface DownloadController
{

	/**
	 * Sets the user agent
	 * 
	 * @param userAgent a string representation of the user agent
	 */
	public void setUserAgent(String userAgent);

	/**
	 * Opens the HttpURLConnection to the specified location and downloads the
	 * resource
	 * 
	 * @param location a ParsedURL object pointing to a resource
	 * @return a boolean indicating the success status of the connection
	 */
	public boolean accessAndDownload(ParsedURL location) throws IOException;

	/**
	 * Returns a boolean indicating if the HTTP response code is that of a good
	 * connection
	 * 
	 * @return a boolean indicating if the HTTP response code is that of a good
	 *         connection
	 */
	public boolean isGood();

	/**
	 * Returns the status code of the HTTP response message for the connection
	 * 
	 * @return the status code of the HTTP response message for the connection
	 */
	public int getStatus();

	/**
	 * Returns the message from the HTTP response message
	 * 
	 * @return the message from the HTTP response message
	 */
	public String getStatusMessage();

	/**
	 * Returns a ParsedURL object corresponding to the original resource
	 * location used to initiate the connection. This value does not change if
	 * the connection is redirected
	 * 
	 * @return a ParsedURL object corresponding to the original resource
	 *         location used to initiate the connection
	 */
	public ParsedURL getLocation();

	/**
	 * Returns a ParsedURL object corresponding to the location of the resource
	 * with which the connection is associated. This value does change with
	 * redirects
	 * 
	 * @return a ParsedURL object corresponding to the location of the resource
	 *         with which the connection is associated
	 */
	public ParsedURL getRedirectedLocation();

	/**
	 * Returns the String representation of the content type
	 * 
	 * @return the String representation of the content type
	 */
	public String getMimeType();

	/**
	 * Returns the content encoding type (character set)
	 * 
	 * @return a String representation of the content encoding type (character
	 *         set)
	 */
	public String getCharset();

	/**
	 * Returns the content of the named header field
	 * 
	 * @param name
	 *            the name of the requested header field
	 * @return a String of the content of the named header field
	 */
	public String getHeader(String name);

	/**
	 * Returns an input stream which reads from the connection
	 * 
	 * @return an input stream which reads from the connection
	 */
	public InputStream getInputStream();

}
