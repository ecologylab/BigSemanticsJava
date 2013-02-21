/**
 *
 */
package ecologylab.bigsemantics.downloaders.controllers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.downloaders.LocalDocumentCache;
import ecologylab.bigsemantics.downloaders.oodss.DownloadResponse;
import ecologylab.bigsemantics.filestorage.FileMetadata;
import ecologylab.bigsemantics.filestorage.FileStorageProvider;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.generic.Debug;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * HTTP download controller request network download to an HTTP server
 * 
 * @author ajit
 * 
 */

public class HTTPDownloadController extends Debug implements DownloadController
{

  public static int                     HTTP_DOWNLOAD_REQUEST_TIMEOUT = 45000;

  public static String                  SERVICE_LOC;

  private static SimplTypesScope        tscope;

  ConcurrentHashMap<ParsedURL, Boolean> recentlyCached;

	// private static Logger htmlCacheLog = Logger.getLogger(BaseLogger.htmlCacheLogger);

	static
	{
		tscope = SimplTypesScope.get("deserialize-download-response", DownloadResponse.class);
	}

	public HTTPDownloadController()
	{
		recentlyCached = new ConcurrentHashMap<ParsedURL, Boolean>(1000);
	}

	@Override
	public void connect(DocumentClosure documentClosure) throws IOException
	{
		Document document = documentClosure.getDocument();
		ParsedURL originalPURL = documentClosure.getDownloadLocation();
		String mimeType = null;

		FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();

		if (!originalPURL.isFile())
		{
			// get equivalent path and check if file exists
			String filePath = storageProvider.lookupFilePath(originalPURL);
			DownloadableLogRecord logRecord = documentClosure.getLogRecord();
			if (filePath == null)
			{
				// htmlCacheLog.debug("Uncached URL: " + originalPURL);
				// Network download
				String userAgentString = document.getMetaMetadata().getUserAgentString();
				ClientResponse resp = getServiceResponse(originalPURL, userAgentString);

				if (resp != null)
				{
					try
					{
						DownloadResponse responseMessage = (DownloadResponse) tscope.deserialize(
								resp.getEntity(String.class), StringFormat.XML);

						String fileLoc = responseMessage == null ? null : responseMessage.getLocation();
						if (fileLoc != null && fileLoc.length() > 0)
						{
							// htmlCacheLog.debug("HTML page cached at " + fileLoc);
							if (logRecord != null)
								logRecord.setUrlHash(fileLoc.substring(fileLoc.lastIndexOf(File.separatorChar)));

							setCached(originalPURL);

							// additional location
							ParsedURL redirectedLocation = responseMessage.getRedirectedLocation();
							if (redirectedLocation != null)
							{
								setCached(redirectedLocation);

								SemanticsGlobalScope semanticScope = document.getSemanticsScope();
								Document newDocument = semanticScope.getOrConstructDocument(redirectedLocation);
								newDocument.addAdditionalLocation(originalPURL);
								documentClosure.changeDocument(newDocument);
							}

							// set local location
							document.setLocalLocation(ParsedURL.getAbsolute("file://" + fileLoc));

							// mimetype
							mimeType = responseMessage.getMimeType();
						}
						else
						{
							error("No location returned in DownloadResponse Message for " + originalPURL);
						}
					}
					catch (Exception e)
					{
						error("Exception deserializing received DownloadResponse for " + originalPURL);
						e.printStackTrace();
					}
				}
				else
				{
					// htmlCacheLog.error("No response from downloader(s) for " + originalPURL);
					error("No response from downloader(s) for " + originalPURL);
				}
			}
			else
			{
				logRecord.setHtmlCacheHit(true);
				// htmlCacheLog.debug("Cached URL[" + originalPURL + "] at " + filePath);

				// document is present in local cache. read meta information as well
				document.setLocalLocation(ParsedURL.getAbsolute("file://" + filePath));

				FileMetadata fileMetadata = storageProvider.getFileMetadata(originalPURL);
				if (fileMetadata != null)
				{
					// additional location
					ParsedURL redirectLoc = fileMetadata == null ? null : fileMetadata
							.getRedirectedLocation();
					if (redirectLoc != null)
					{
						SemanticsGlobalScope semanticScope = document.getSemanticsScope();
						debug("Changing " + document + " using redirected location " + redirectLoc);
						Document newDocument = semanticScope.getOrConstructDocument(redirectLoc);
						newDocument.addAdditionalLocation(originalPURL); // TODO multiple redirects
						documentClosure.changeDocument(newDocument);
					}

					// mimetype
					mimeType = fileMetadata.getMimeType();
				}
			}
		}

		// irrespective of document origin, its now saved to a local location
		LocalDocumentCache localDocumentCache = new LocalDocumentCache(document);
		localDocumentCache.connect();
		PURLConnection purlConnection = localDocumentCache.getPurlConnection();
		DocumentParser documentParser = localDocumentCache.getDocumentParser();

		// set mime type
		if (mimeType != null)
			purlConnection.setMimeType(mimeType);
		debug("Setting purlConnection[" + purlConnection.getPurl() + "] to documentClosure");
		documentClosure.setPurlConnection(purlConnection);

		// document parser is set only when URL is local directory
		if (documentParser != null)
			documentClosure.setDocumentParser(documentParser);
	}

	private ClientResponse getServiceResponse(ParsedURL originalPurl, String userAgentString)
	{
		try
		{
			Client client = Client.create();
			client.setFollowRedirects(true);
			client.setReadTimeout(HTTP_DOWNLOAD_REQUEST_TIMEOUT);

			String requestUri = SERVICE_LOC + "?url="
					+ URLEncoder.encode(originalPurl.toString(), "UTF-8") + "&userAgentString="
					+ URLEncoder.encode(userAgentString, "UTF-8");
			WebResource r = client.resource(requestUri);

			return r.get(ClientResponse.class);
		}
		catch (UnsupportedEncodingException e)
		{
			error("request couldn't be encoded for " + originalPurl);
		}
		catch(NullPointerException e)
		{
			error("request client / web resource couldn't be created for " + originalPurl);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method sets the cache status of the input URL to 'cached'. Note that this status is only a
	 * temporary status in memory, for quick deciding if this document has been cached or not without
	 * hitting the disk, and does not necessarily reflect the real cache status on disk.
	 * 
	 * @param url
	 */
	void setCached(ParsedURL url)
	{
	  if (url != null)
  		recentlyCached.put(url, true);
	}

	@Override
	public boolean isCached(ParsedURL purl)
	{
	  if (purl == null)
	    return false;
		if (!recentlyCached.containsKey(purl))
		{
			FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();
			String filePath = storageProvider.lookupFilePath(purl);
			File cachedFile = filePath == null ? null : new File(filePath);
			boolean cached = cachedFile != null && cachedFile.exists();
			Boolean previous = recentlyCached.putIfAbsent(purl, cached);
			if (previous != null)
			  cached = previous;
			return cached;
		}
		return recentlyCached.get(purl);
	}

}
