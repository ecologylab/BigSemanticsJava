/**
 * 
 */
package ecologylab.semantics.downloaders.controllers;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.distributed.client.NIOClient;
import ecologylab.oodss.distributed.exception.MessageTooLargeException;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.downloaders.LocalDocumentCache;
import ecologylab.semantics.downloaders.oodss.DownloadRequest;
import ecologylab.semantics.downloaders.oodss.DownloadResponse;
import ecologylab.semantics.downloaders.oodss.SemanticsServiceDownloadMessageScope;
import ecologylab.semantics.filestorage.FileMetadata;
import ecologylab.semantics.filestorage.FileStorageProvider;
import ecologylab.semantics.filestorage.FileSystemStorage;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SimplTypesScope;

/**
 * OODSS download controller request network download to an OODSS server / worker
 * 
 * @author ajit
 * 
 */

public class OODSSDownloadController extends Debug implements DownloadController
{

  public static int                 OODSS_DOWNLOAD_REQUEST_TIMEOUT = 45000;

  LinkedHashMap<ParsedURL, Boolean> recentlyCached;

  Object                            lockRecentlyCached             = new Object();

  public OODSSDownloadController()
  {
    recentlyCached = new LinkedHashMap<ParsedURL, Boolean>(1000);
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
      if (filePath == null)
      {
        debug("Uncached URL: " + originalPURL);

        // Network download
        SimplTypesScope lookupMetadataTranslations = SemanticsServiceDownloadMessageScope.get();
        Scope sessionScope = new Scope();
        // sessionScope.put("RESPONSE_LISTENER", this);
        NIOClient client = new NIOClient("localhost",
                                         2107,
                                         lookupMetadataTranslations,
                                         sessionScope);
        if (client.connect(OODSS_DOWNLOAD_REQUEST_TIMEOUT))
        {
          String userAgentString = document.getMetaMetadata().getUserAgentString();
          DownloadRequest requestMessage = new DownloadRequest(originalPURL, userAgentString);
          DownloadResponse responseMessage = null;
          try
          {
            debug("Sending OODSS request for accessing " + originalPURL);
            responseMessage = (DownloadResponse) client.sendMessage(requestMessage,
                                                                    OODSS_DOWNLOAD_REQUEST_TIMEOUT);
            String fileLoc = responseMessage == null ? null : responseMessage.getLocation();
            if (fileLoc != null && fileLoc.length() > 0)
            {
              debug("HTML page cached at " + fileLoc);

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
              error("No response from downloader(s) for " + originalPURL);
            }
          }
          catch (MessageTooLargeException e)
          {
            System.err.println("The message was too large!");
            e.printStackTrace();
          }

          client.disconnect();
        }
      }
      else
      {
        debug("Cached URL[" + originalPURL + "] at " + filePath);

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

  /**
   * This method sets the cache status of the input URL to 'cached'. Note that this status is only a
   * temporary status in memory, for quick deciding if this document has been cached or not without
   * hitting the disk, and does not necessarily reflect the real cache status on disk.
   * 
   * @param url
   */
  void setCached(ParsedURL url)
  {
    synchronized (lockRecentlyCached)
    {
      recentlyCached.put(url, true);
    }
  }

  @Override
  public boolean isCached(ParsedURL purl)
  {
    if (!recentlyCached.containsKey(purl))
    {
      synchronized (lockRecentlyCached)
      {
        if (!recentlyCached.containsKey(purl))
        {
          FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();
          String filePath = storageProvider.lookupFilePath(purl);
          File cachedFile = filePath == null ? null : new File(filePath);
          boolean cached = cachedFile != null && cachedFile.exists();
          recentlyCached.put(purl, cached);
          return cached;
        }
      }
    }
    return recentlyCached.get(purl);
  }

}
