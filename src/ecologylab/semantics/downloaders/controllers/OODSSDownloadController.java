/**
 * 
 */
package ecologylab.semantics.downloaders.controllers;

import java.io.IOException;

import ecologylab.collections.Scope;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.distributed.client.NIOClient;
import ecologylab.oodss.distributed.exception.MessageTooLargeException;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.downloaders.LocalDocumentCache;
import ecologylab.semantics.downloaders.oodss.DownloadRequest;
import ecologylab.semantics.downloaders.oodss.DownloadResponse;
import ecologylab.semantics.downloaders.oodss.SemanticServiceOODSSTranslationScope;
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

public class OODSSDownloadController implements DownloadController
{

	@Override
	public void connect(DocumentClosure documentClosure) throws IOException
	{
		Document document = documentClosure.getDocument();
		ParsedURL originalPURL = documentClosure.getDownloadLocation();
		PURLConnection purlConnection;
		String mimeType = null;
		DocumentParser documentParser = null;

		FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();

		if (!originalPURL.isFile())
		{
			// get equivalent path and check if file exists
			String filePath = storageProvider.lookupFilePath(originalPURL);
			if (filePath == null)
			{
				// Network download
				SimplTypesScope lookupMetadataTranslations = SemanticServiceOODSSTranslationScope
						.getOODSSTranslationScope();

				Scope sessionScope = new Scope();

				// sessionScope.put("RESPONSE_LISTENER", this);

				NIOClient client = new NIOClient("localhost", 2107, lookupMetadataTranslations,
						sessionScope);
				client.connect();

				DownloadRequest requestMessage = new DownloadRequest(originalPURL, document
						.getMetaMetadata().getUserAgentString());

				DownloadResponse responseMessage;

				try
				{
					responseMessage = (DownloadResponse) client.sendMessage(requestMessage);

					// additional location
					ParsedURL redirectedLocation = responseMessage.getRedirectedLocation();
					if (redirectedLocation != null)
					{
						SemanticsGlobalScope semanticScope = document.getSemanticsScope();
						Document newDocument = semanticScope.getOrConstructDocument(redirectedLocation);
						newDocument.addAdditionalLocation(originalPURL); // TODO: confirm this!
						documentClosure.changeDocument(newDocument);
					}

					// set local location
					document
							.setLocalLocation(ParsedURL.getAbsolute("file://" + responseMessage.getLocation()));
					// mimetype
					mimeType = responseMessage.getMimeType();
				}
				catch (MessageTooLargeException e)
				{
					System.err.println("The message was too large!");
					e.printStackTrace();
				}

				client.disconnect();
			}
			else
			{
				// document is present in local cache. read meta information as well
				document.setLocalLocation(ParsedURL.getAbsolute("file://" + filePath));

				FileMetadata fileMetadata = storageProvider.getFileMetadata(originalPURL);
				// additional location
				ParsedURL redirectedLocation = fileMetadata.getRedirectedLocation();
				if (redirectedLocation != null)
				{
					SemanticsGlobalScope semanticScope = document.getSemanticsScope();
					Document newDocument = semanticScope.getOrConstructDocument(redirectedLocation);
					newDocument.addAdditionalLocation(originalPURL); // TODO:confirm+multiple redirects
					documentClosure.changeDocument(newDocument);
				}
				// mimetype
				mimeType = fileMetadata.getMimeType();
			}
		}

		// irrespective of document origin, its now saved to a local location
		LocalDocumentCache localDocumentCache = new LocalDocumentCache(document);
		localDocumentCache.connect();
		purlConnection = localDocumentCache.getPurlConnection();
		documentParser = localDocumentCache.getDocumentParser();

		// set mime type
		if (mimeType != null)
			purlConnection.setMimeType(mimeType);
		documentClosure.setPurlConnection(purlConnection);

		// document parser is set only when URL is local directory
		if (documentParser != null)
			documentClosure.setDocumentParser(documentParser);
	}
}
