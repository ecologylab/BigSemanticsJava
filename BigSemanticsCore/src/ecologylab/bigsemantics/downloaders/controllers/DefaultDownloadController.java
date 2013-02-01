/**
 * 
 */
package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.downloaders.LocalDocumentCache;
import ecologylab.bigsemantics.downloaders.NetworkDocumentDownloader;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;

/**
 * Default download controller just reorganizes the original connect mechanism 
 * 
 * @author ajit
 * 
 */

public class DefaultDownloadController implements DownloadController
{

	public DefaultDownloadController()
	{
	}

	public void connect(DocumentClosure documentClosure) throws IOException
	{
		Document document = documentClosure.getDocument();
		ParsedURL originalPURL = documentClosure.getDownloadLocation();
		PURLConnection purlConnection;
		DocumentParser documentParser = null;
		// boolean bChanged = false;

		if (originalPURL.isFile())
		{
			// get from local store
			LocalDocumentCache localDocumentCache = new LocalDocumentCache(documentClosure.getDocument());
			localDocumentCache.connect();
			purlConnection = localDocumentCache.getPurlConnection();
			documentParser = localDocumentCache.getDocumentParser();
		}
		else
		{
			// request Network Download
			NetworkDocumentDownloader documentDownloader = new NetworkDocumentDownloader(originalPURL,
					document.getMetaMetadata().getUserAgentString());
			documentDownloader.connect(false);
			purlConnection = documentDownloader.getPurlConnection();

			ParsedURL redirectedLocation = documentDownloader.getRedirectedLocation();
			if (redirectedLocation != null)
			{
				SemanticsGlobalScope semanticScope = document.getSemanticsScope();
				Document newDocument = semanticScope.getOrConstructDocument(redirectedLocation);
				newDocument.addAdditionalLocation(originalPURL); // TODO: confirm this!
				documentClosure.changeDocument(newDocument);
			}

			// document.setLocalLocation(ParsedURL.getAbsolute("file://" + document.getLocalLocation()));
		}

		documentClosure.setPurlConnection(purlConnection);
		// document parser is set only when URL is local directory
		if (documentParser != null)
			documentClosure.setDocumentParser(documentParser);
	}

  @Override
  public boolean isCached(ParsedURL purl)
  {
    // TODO Auto-generated method stub
    return false;
  }
  
}
