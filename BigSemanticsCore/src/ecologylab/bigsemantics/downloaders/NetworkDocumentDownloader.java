/**
 * 
 */
package ecologylab.bigsemantics.downloaders;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import ecologylab.bigsemantics.filestorage.FileMetadata;
import ecologylab.bigsemantics.filestorage.FileStorageProvider;
import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.generic.Debug;
import ecologylab.net.ConnectionHelperJustRemote;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;

/**
 * url connection to network resource
 * has been kept minimal for the worker machines
 * 
 * @author andruid
 * @author ajit
 *
 */

public class NetworkDocumentDownloader extends Debug {
	
	//private Document				document;
	
	//private SemanticsGlobalScope	semanticsScope;
	
	private String					userAgentString;

	private ParsedURL				location;
	
	private PURLConnection	purlConnection;
	
	//private boolean 				cacheHit 		= false;
	
	private ParsedURL				redirectedLocation;

	//private boolean 				bChanged		= false;
	
	private String					localLocation;
	
	public NetworkDocumentDownloader(/*Document document*/ParsedURL location, String userAgentString)
	{
		//this.document				= document;
		//this.semanticsScope			= document.getSemanticsScope();
		this.location 			 = location;
		this.userAgentString = userAgentString;
	}
	
	/**
	 * Open a connection to the URL. Read the header, but not the content. Look at if the path exists,
	 * if there is a redirect, and the mime type. If there is a redirect, process it.
	 * <p/>
	 * Create an InputStream. Save to a local file. 
	 * 
	 * DocumentParser creation is done by DocumentClosure. 
	 * 
	 * Using reflection (Class.newInstance()), create the appropriate
	 * DocumentParser, based on that mimeType, using the allTypes HashMap. Return it.
	 * This method returns the parser using one of the cases:
	 * 1) Use URL based look up and find meta-metadata and use binding if (direct or xpath)
	 *    to find the parser.
	 * 2) Else find meta-metadata using URL suffix and mime type and make a direct binding parser.
	 * 3) If still parser is null and binding is also null, use in-build tables to find the parser.
	 * @throws Exception 
	 * @throws IOException 
	 */
	public void connect(boolean save) throws IOException
	{
		//assert(document != null);
		//final Document 	orignalDocument	= document;
		//final ParsedURL originalPURL	= document.getDownloadLocation();
		final ParsedURL originalPURL = this.location;
		
		ConnectionHelperJustRemote documentParserConnectHelper = new ConnectionHelperJustRemote()
		{
			public void handleFileDirectory(File file)
			{
				warning("DocumentClosure.connect(): Need to implement handleFileDirectory().");
			}

			/**
			 * For use in local file processing, not for http.
			 */
			//public boolean parseFilesWithSuffix(String suffix)
			//{
				//Document result = semanticsScope.getMetaMetadataRepository().constructDocumentBySuffix(suffix);
				//changeDocument(result);
				//return (result != null);
			//}

			@Override
			public void displayStatus(String message)
			{
				//semanticsScope.displayStatus(message);
			}
			
			@Override
			public boolean processRedirect(URL redirectedURL) throws IOException
			{
				ParsedURL redirectedPURL	= new ParsedURL(redirectedURL);
				displayStatus("try redirecting: " + originalPURL + " > " + redirectedURL);
				//  Document redirectedDocument	= semanticsScope.getOrConstructDocument(redirectedPURL); // documentLocationMap.getOrCreate(connectionPURL);
				//TODO -- what if redirectedDocument is already in the queue or being downloaded already?
				//  if (redirectedDocument != null)	// existing document
				{	// the redirected url has been visited already.
					
					// it seems we don't need to track where the inlinks are from, 
					// though we could in SemanticInlinks, if we kept a DocumentClosure in there
//					if (container != null)
//						container.redirectInlinksTo(redirectedAbstractContainer);

					if (!originalPURL.equals(redirectedPURL))
						redirectedLocation = redirectedPURL; //  redirectedDocument.addAdditionalLocation(redirectedPURL);
					//TODO -- copy metadata from originalDocument?!!
					//  changeDocument(redirectedDocument);
					//TODO -- reconnect
					
					// redirectedAbstractContainer.performDownload();

					// we dont need the new container object that was passed in
					// TODO recycle it!
					return true;
				}
				/*else
				// FIXME this will never happen now! since getOrConstructDocument() never returns null.
				// redirect to a new url
				{
					MetaMetadata originalMM						= (MetaMetadata) orignalDocument.getMetaMetadata();
					RedirectHandling redirectHandling = originalMM.getRedirectHandling();

					if (document.isAlwaysAcceptRedirect() || semanticsScope.accept(redirectedPURL))
					{
						println("\tredirect: " + originalPURL + " -> " + redirectedPURL);
						String domain 				= redirectedPURL.domain();
						String connPURLSuffix = redirectedPURL.suffix();
						// add entry to GlobalCollections containersHash

						// FIXME:hack for acmPortal pdf containers.
						// The redirected URL has a timeout...which creates
						// a problem while
						// opening the saved xml.
						// if(connectionPURL.toString().startsWith(
						// "http://delivery.acm.org"))
						// {
						// return true;
						// }
						Document 				newMetadata;
				*/
						/*
						 * Was unnecessary  because of how ecocache handles the acm gateway pages
						 * But actually, we are not using ecocache :-(
						 */
						//FIXME -- use meta-metadata to express this case!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				//		if (/* !Pref.lookupBoolean(CFPrefNames.USING_PROXY) && */
				/*				"acm.org".equals(domain) && "pdf".equals(connPURLSuffix))
						{
							MetaMetadata pdfMetaMetadata = semanticsScope.getMetaMetadataRepository().getMMBySuffix(connPURLSuffix);
							newMetadata = (Document) pdfMetaMetadata.constructMetadata();
							newMetadata.setLocation(redirectedPURL);
							return true;
						}
						else
						{
							// regular get new metadata
							newMetadata	= semanticsScope.getOrConstructDocument(redirectedPURL);
						}

						if (redirectHandling == RedirectHandling.REDIRECT_FOLLOW_DONT_RESET_LOCATION)
						{
							newMetadata.setLocation(originalPURL);
							newMetadata.addAdditionalLocation(redirectedPURL);
						}
						else
							newMetadata.addAdditionalLocation(originalPURL);
						
						changeDocument(newMetadata);

						return true;
					}
					else
						println("rejecting redirect: " + originalPURL + " -> " + redirectedPURL);
				}*/
				//return false;
			}
		};


		//MetaMetadataCompositeField metaMetadata = document.getMetaMetadata();
		// then try to create a connection using the PURL
		//String userAgentString	= metaMetadata.getUserAgentString(); //"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/530.5 (KHTML, like Gecko) Chrome/2.0.172.43 Safari/530.5";
		purlConnection			= new PURLConnection(originalPURL);
		
		//else{
		purlConnection.networkConnect(documentParserConnectHelper, userAgentString);	// HERE!

		if (purlConnection.isGood())
		{
			//Document document				= this.document;					// may have changed during redirect processing
			//metaMetadata					= document.getMetaMetadata();
			
			// check for a parser that was discovered while processing a re-direct
			
			//SemanticsSite site 				= document.getSite();
	
			// if a parser was preset for this container, use it
	//		if ((result == null) && (container != null))
	//			result = container.getDocumentParser();
		
			// if we made PURL connection but could not find parser using container
			// if ((purlConnection != null) && !originalPURL.isFile())
			{
				/*String cacheValue = purlConnection.urlConnection().getHeaderField("X-Cache");
				cacheHit = cacheValue != null && cacheValue.contains("HIT");
	
				if (metaMetadata.isGenericMetadata())
				{ // see if we can find more specifc meta-metadata using mimeType
					final MetaMetadataRepository repository = semanticsScope.getMetaMetadataRepository();
					String mimeType = purlConnection.mimeType();
					MetaMetadata mimeMmd	= repository.getMMByMime(mimeType);
					if (mimeMmd != null && !mimeMmd.equals(metaMetadata))
					{	// new meta-metadata!
						if (!mimeMmd.getMetadataClass().isAssignableFrom(document.getClass()))
						{	// more specifc so we need new metadata!
							document	= (Document) mimeMmd.constructMetadata(); // set temporary on stack
							changeDocument(document);
						}
						metaMetadata	= mimeMmd;
					}
				}*/
				
				//save to file
				if (save)
				{
					FileStorageProvider storageProvider = FileSystemStorage.getStorageProvider();
					localLocation = storageProvider.saveFile(originalPURL, purlConnection.inputStream());
					FileMetadata fileMetadata = new FileMetadata(originalPURL, redirectedLocation,
							localLocation, purlConnection.mimeType(), new Date());
					storageProvider.saveFileMetadata(fileMetadata);
				}
			}
		}
		//}
	//		String parserName				= metaMetadata.getParser();	
	//		if (parserName == null)			//FIXME Hook HTMLDOMImageText up to html mime-type & suffixes; drop defaultness of parser
	//			parserName = SemanticActionsKeyWords.HTML_IMAGE_DOM_TEXT_PARSER;
		
		//Note: parser assignment is with Document Closure
		//return bChanged;	
	}
	
	/**
	 * Document metadata object must change, because we learned something new about its type.
	 * @param newDocument
	 */
	//public void changeDocument(Document newDocument) 
	//{
		//Document oldDocument	= document;
		//this.document			= newDocument;
		
		//newDocument.inheritValues(oldDocument);	
		//oldDocument.recycle();
		//this.bChanged = true;
	//}

	//public Document getDocument() {
		//return document;
	//}

	//relevant only for DefaultDownloadController i.e. not serialized and sent over OODSS
	public PURLConnection getPurlConnection() {
		return purlConnection;
	}
	
	public String mimeType() {
		return purlConnection.mimeType();
	}

	public String getLocalLocation() {
		return localLocation;
	}

	public ParsedURL getRedirectedLocation()
	{
		return redirectedLocation;
	}
}
