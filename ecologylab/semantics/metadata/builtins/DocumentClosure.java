/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import ecologylab.collections.SetElement;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Continuation;
import ecologylab.io.Downloadable;
import ecologylab.net.ConnectionHelperJustRemote;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.DocumentLocationMap;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.RedirectHandling;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorFeature;
import ecologylab.semantics.seeding.QandDownloadable;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * New Container object. Mostly just a closure around Document.
 * Used as a candidate and wrapper for downloading.
 * 
 * @author andruid
 *
 */
public class DocumentClosure<D extends Document> extends SetElement
implements TermVectorFeature, Downloadable, QandDownloadable<DocumentClosure>, SemanticActionsKeyWords
{
	D															document;
	
	private PURLConnection				purlConnection;

	private DocumentParser				documentParser;
	
	/**
	 * This is tracked mainly for debugging, so we can see what pURL was fed into the meta-metadata address resolver machine.
	 */
	ParsedURL											initialPURL;

	SemanticInLinks								semanticInlinks;

	Continuation<DocumentClosure> continuation;
	
	DownloadStatus								downloadStatus	= DownloadStatus.UNPROCESSED;
	
	protected		NewInfoCollector	infoCollector;
	

	/**
	 * Keeps state about the search process, if this is encapsulates a search result;
	 */
	protected SearchResult	searchResult;


	/**
	 * If true (the normal case), then any MediaElements encountered will be added
	 * to the candidates collection, for possible inclusion in the visual information space.
	 */
	boolean							collectMedia	= true;
	/**
	 * If true (the normal case), then hyperlinks encounted will be fed to the
	 * web crawler, providing that they are traversable() and of the right mime types.
	 */
	boolean							crawlLinks		= true;

	/**
	 * Indicates that this Container is processed via drag and drop.
	 */
	private boolean			isDnd;
	
	private boolean 		cacheHit = false;

	private final Object DOWNLOAD_STATUS_LOCK			= new Object();
	private final Object DOCUMENT_LOCK						= new Object();   
	
	/**
	 * 
	 */
	private DocumentClosure(D document, NewInfoCollector infoCollector, SemanticInLinks semanticInlinks)
	{
		super();
		this.document							= document;
		this.infoCollector				= infoCollector;
		this.semanticInlinks			= semanticInlinks;
	}

	/**
	 * Should only be called by Document.getOrCreateClosure().
	 * 
	 * @param document
	 * @param semanticInlinks
	 */
	DocumentClosure(D document, SemanticInLinks semanticInlinks)
	{
		this(document, document.getInfoCollector(), semanticInlinks);
	}

	/////////////////////// methods for downloadable //////////////////////////
	/**
	 * Called by DownloadMonitor to initiate download, and cleanup afterward.
	 * NO OTHER OBJECT SHOULD EVER CALL THIS METHOD!
	 * 
	 * Actually download the document and parse it.
	 * Connect to the purl. Figure out the appropriate Meta-Metadata and DocumentType.
	 * Process redirects as needed.
	 */
	@Override
	public void performDownload()
	throws IOException
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (!(downloadStatus == DownloadStatus.QUEUED || downloadStatus == DownloadStatus.UNPROCESSED))
				return;
			downloadStatus	= DownloadStatus.CONNECTING;
		}
		if (infoCollector == null)	// this should NEVER happen!!!!!!!!!!!!!!!!!!!!
		{
			NewInfoCollector documentInfoCollector = document.getInfoCollector();
			if (documentInfoCollector == null)
			{
				error("Cant downloadAndParse: InfoCollector= null.");
				return;
			}
			infoCollector	= documentInfoCollector;
		}
		document.setInfoCollector(infoCollector);
		
		if (recycled() || document.isRecycled())
		{
			println("ERROR: Trying to downloadAndParse() page that's already recycled -- "+ location());
			return;
		}

		ParsedURL location = location();
		connect();					// evolves Document based on redirects & mime-type; sets the documentParser

		if (purlConnection.isGood() && documentParser != null)
		{
			// container or not (it could turn out to be an image or some other mime type), parse the baby!
			downloadStatus	= DownloadStatus.PARSING;
			if (documentParser.downloadingMessageOnConnect())
				infoCollector.displayStatus("Downloading " + location(), 2);

			documentParser.parse();
			
			downloadStatus	= DownloadStatus.DOWNLOAD_DONE;
									
//		 	if(Pref.lookupBoolean(CFPrefNames.CRAWL_CAREFULLY) && !documentParser.cacheHit) //infoCollector.getCrawlingSlow() && 
//		 	{		 		
//			  	int waitTime = (Pref.lookupInt(CFPrefNames.MIN_WAIT_TIME) * 1000) + (MathTools.random(100)*((Pref.lookupInt(CFPrefNames.MAX_WAIT_TIME)-Pref.lookupInt(CFPrefNames.MIN_WAIT_TIME)) * 10));
//			 	System.out.println("Downloading slow, waiting: "+((float)waitTime/60000));
//				infoCollector.crawlerDownloadMonitor().pause(waitTime);
//		 	}
		}
		else
		{
			if (documentParser != null)
				warning("Error opening connection for " + location);
			recycle();
		}
		document.setDownloadDone(true);
		
		document.downloadAndParseDone(documentParser);
		
		purlConnection.recycle();
		purlConnection	= null;
	}
	
	/**
	 * Open a connection to the URL. Read the header, but not the content. Look at if the path exists,
	 * if there is a redirect, and the mime type. If there is a redirect, process it.
	 * <p/>
	 * Create an InputStream. Using reflection (Class.newInstance()), create the appropriate
	 * DocumentParser, based on that mimeType, using the allTypes HashMap. Return it.
	 * 
	 * This method returns the parser using one of the cases:
	 * 1) Use URL based look up and find meta-metadata and use binding if (direct or xpath)
	 *    to find the parser.
	 * 2) Else find meta-metadata using URL suffix and mime type and make a direct binding parser.
	 * 3) If still parser is null and binding is also null, use in-build tables to find the parser.
	 * @throws Exception 
	 * @throws IOException 
	 */
	private void connect() throws IOException
	{
		assert(document != null);
		final Document 	orignalDocument	= document;
		final ParsedURL originalPURL		= document.getLocation();
		
		ConnectionHelperJustRemote documentParserConnectHelper = new ConnectionHelperJustRemote()
		{
			public void handleFileDirectory(File file)
			{
					warning("DocumentClosure.connect(): Need to implement handleFileDirectory().");
			}

			/**
			 * For use in local file processing, not for http.
			 */
			public boolean parseFilesWithSuffix(String suffix)
			{
				Document result = infoCollector.getMetaMetadataRepository().constructDocumentBySuffix(suffix);
				changeDocument(result);
				return (result != null);
			}

			public void displayStatus(String message)
			{
				infoCollector.displayStatus(message);
			}
			
			public boolean processRedirect(URL connectionURL) throws IOException
			{
				DocumentLocationMap<? extends Document>	documentLocationMap	= document.getDocumentLocationMap();
				ParsedURL connectionPURL	= new ParsedURL(connectionURL);
				displayStatus("try redirecting: " + originalPURL + " > " + connectionURL);
				Document redirectedDocument	= documentLocationMap.get(connectionPURL); // documentLocationMap.getOrCreate(connectionPURL);
				//TODO -- what if redirectedDocument is already in the queue or being downloaded already?
				if (redirectedDocument != null)	// existing document
				{	// the redirected url has been visited already.
					
					// it seems we don't need to track where the inlinks are from, 
					// though we could in SemanticInlinks, if we kept a DocumentClosure in there
//					if (container != null)
//						container.redirectInlinksTo(redirectedAbstractContainer);

					redirectedDocument.addAdditionalLocation(connectionPURL);
					//TODO -- copy metadata from originalDocument?!!
					changeDocument(redirectedDocument);
					//TODO -- reconnect
					
					// redirectedAbstractContainer.performDownload();

					// we dont need the new container object that was passed in
					// TODO recycle it!
				}
				else
				// redirect to a new url
				{
					MetaMetadata originalMM						= (MetaMetadata) orignalDocument.getMetaMetadata();
					RedirectHandling redirectHandling = originalMM.getRedirectHandling();

					if (document.isAlwaysAcceptRedirect() || infoCollector.accept(connectionPURL))
					{
						println("\tredirect: " + originalPURL + " -> " + connectionPURL);
						String domain 				= connectionPURL.domain();
						String connPURLSuffix = connectionPURL.suffix();
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

						/*
						 * Was unnecessary  because of how ecocache handles the acm gateway pages
						 * But actually, we are not using ecocache :-(
						 */
						//FIXME -- use meta-metadata to express this case!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						if (/* !Pref.lookupBoolean(CFPrefNames.USING_PROXY) && */
								"acm.org".equals(domain) && "pdf".equals(connPURLSuffix))
						{
							MetaMetadata pdfMetaMetadata = infoCollector.metaMetaDataRepository().getMMBySuffix(connPURLSuffix);
							newMetadata = (Document) pdfMetaMetadata.constructMetadata();
							newMetadata.setLocation(connectionPURL);
							return true;
						}
						else
						{
							// regular get new metadata
							newMetadata	= documentLocationMap.getOrConstruct(connectionPURL);
						}

						if (redirectHandling == RedirectHandling.REDIRECT_FOLLOW_DONT_RESET_LOCATION)
						{
							newMetadata.setLocation(originalPURL);
							newMetadata.addAdditionalLocation(connectionPURL);
						}
						else
							newMetadata.addAdditionalLocation(originalPURL);
						
						changeDocument(newMetadata);

						return true;
					}
					else
						println("rejecting redirect: " + originalPURL + " -> " + connectionPURL);
				}
				return false;
			}
		};


		MetaMetadataCompositeField metaMetadata = document.getMetaMetadata();
		// then try to create a connection using the PURL
		String userAgentString				= metaMetadata.getUserAgentString();
		purlConnection								= new PURLConnection(originalPURL);
		if (originalPURL.isFile())
		{
			//TODO handle local files here!
			File file	= originalPURL.file();
			if (file.isDirectory())
			{
				// FileDirectoryParser
				documentParser	= DocumentParser.getParserInstanceFromBindingMap(FILE_DIRECTORY_PARSER, infoCollector);
			}
			else
			{
				purlConnection.fileConnect();
				// we already have the correct meta-metadata, having used suffix to construct.
			}
		}
		else
		{
			purlConnection.networkConnect(documentParserConnectHelper, userAgentString);	// HERE!

			if (purlConnection.isGood())
			{
				Document document				= this.document;					// may have changed during redirect processing
				metaMetadata						= document.getMetaMetadata();
				
		
				// check for a parser that was discovered while processing a re-direct
				
				SemanticsSite site 					= document.getSite();
		
				// if a parser was preset for this container, use it
		//		if ((result == null) && (container != null))
		//			result = container.getDocumentParser();
			
				// if we made PURL connection but could not find parser using container
				if ((purlConnection != null) && !originalPURL.isFile())
				{
					String cacheValue = purlConnection.urlConnection().getHeaderField("X-Cache");
					cacheHit = cacheValue != null && cacheValue.contains("HIT");
		
					if (metaMetadata.isGenericMetadata())
					{ // see if we can find more specifc meta-metadata using mimeType
						final MetaMetadataRepository repository = infoCollector.metaMetaDataRepository();
						String mimeType = purlConnection.mimeType();
						MetaMetadataCompositeField mimeMmd	= repository.getMMByMime(mimeType);
						if (mimeMmd != null && !mimeMmd.equals(metaMetadata))
						{	// new meta-metadata!
							if (!mimeMmd.getMetadataClass().isAssignableFrom(document.getClass()))
							{	// more specifc so we need new metadata!
								document	= (Document) ((MetaMetadata) mimeMmd).constructMetadata(); // set temporary on stack
								changeDocument(document);
							}
							metaMetadata	= mimeMmd;
						}
					}
				}
			}
		}
	//		String parserName				= metaMetadata.getParser();	
	//		if (parserName == null)			//FIXME Hook HTMLDOMImageText up to html mime-type & suffixes; drop defaultness of parser
	//			parserName = SemanticActionsKeyWords.HTML_IMAGE_DOM_TEXT_PARSER;
			
		if (documentParser == null)
			documentParser = DocumentParser.get((MetaMetadata) metaMetadata, infoCollector);
		if (documentParser != null)
		{
			documentParser.fillValues(purlConnection, this, infoCollector);
		}
		else
			warning("No DocumentParser found");
	}

	/**
	 * Document metadata object must change, because we learned something new about its type.
	 * @param newDocument
	 */
	public void changeDocument(Document newDocument) 
	{
		synchronized (DOCUMENT_LOCK)
		{
			Document oldDocument	= document;
			this.document					= (D) newDocument;
			
			newDocument.inheritValues(oldDocument);	
			newDocument.serializeOut("After changeDocument()");
			semanticInlinks				= newDocument.semanticInlinks; // probably not needed, but just in case.
			oldDocument.recycle();
		}
	}

	public ParsedURL getInitialPURL()
	{
		return initialPURL;
	}

	@Override
	public SemanticsSite getSite()
	{
		Document document = this.document;
		return (document == null) ? null : document.getSite();
	}
	
	@Override
	public void recycle()
	{
		recycle(false);
	}
	@Override
	public synchronized void recycle(boolean recycleDocument)
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (downloadStatus == DownloadStatus.RECYCLED)
				return;
			downloadStatus	= DownloadStatus.RECYCLED;
		}
		
		if (documentParser != null)
			documentParser.recycle();

		if (purlConnection != null)
			purlConnection.recycle();

		semanticInlinks	= null;

		initialPURL			= null;

		continuation		= null;
			
		//??? should we recycle Document here -- under what circumstances???
		if (recycleDocument)
			document.recycle();
	}
	@Override
	public boolean recycled()
	{
		Document document = this.document;
		return document == null || document.isRecycled();
	}
	
	@Override
	public boolean isRecycled()
	{
		return document == null || document.isRecycled();
	}


	@Override
	public ParsedURL location()
	{
		Document document = this.document;
		return (document == null) ? null : document.getLocation();
	}

	/**
	 * @return the semanticInlinks
	 */
	public SemanticInLinks getSemanticInlinks()
	{
		return semanticInlinks;
	}

	/**
	 * @return the document
	 */
	public D getDocument()
	{
		synchronized (DOCUMENT_LOCK)
		{
			return document;
		}
	}

	/**
	 * Download if necessary, using the 
	 * {@link ecologylab.concurrent.DownloadMonitor DownloadMonitor} if USE_DOWNLOAD_MONITOR
	 * is set (it seems it always is), or in a new thread.
	 * Control will be passed to {@link #downloadAndParse() downloadAndParse()}.
	 * Does nothing if this has been previously queued, if it has been recycled, or if it isMuted().
	 * 
	 * @return	true if the Container is actually queued for download. false if it was previously, if its been recycled, or if it is muted.
	 */
	@Override
	public boolean queueDownload()
	{
		if (recycled())
		{
			debugA("ERROR: cant queue download cause already recycled.");
			return false;
		}
		final boolean result = !filteredOut(); // for dashboard type on the fly filtering
		if (result)
		{
			if (!testAndSetQueueDownload())
				return false;	
			delete();				// remove from candidate pools! (invokes deleteHook as well)  
			
			downloadMonitor().download((DocumentClosure) this, continuation());
		}
		return result;
	}
	/**
	 * Test and set state variable inside of QUEUE_DOWNLOAD_LOCK.
	 * 
	 * @return		true if this really queues the download, and false if it had already been queued.
	 */
	private boolean testAndSetQueueDownload()
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (downloadStatus != DownloadStatus.UNPROCESSED)
				return false;
			downloadStatus					= DownloadStatus.QUEUED;
			return true;
		}
	}
	/**
	 * Test state variable inside of QUEUE_DOWNLOAD_LOCK.
	 * 
	 * @return true if result has already been queued, connected to, downloaded, ... so it should not be operated on further.
	 */
	public boolean downloadHasBeenQueued()
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			return downloadStatus != DownloadStatus.UNPROCESSED;
		}
	}

	/**
	 * Indicate that this Container is being processed via DnD.
	 *
	 */
	void setDnd()
	{
		isDnd			= true;
	}
	public boolean isDnd()
	{
		return isDnd;
	}
	public DownloadMonitor<DocumentClosure> downloadMonitor()
	{
		// GoogleSearch, SearchResults are all seeds
		return  (isDnd ? NewInfoCollector.DND_DOWNLOAD_MONITOR : 
			isSeed() ? 
		  NewInfoCollector.SEEDING_DOWNLOAD_MONITOR : NewInfoCollector.CRAWLER_DOWNLOAD_MONITOR);
	}

	
	@Override
	public int hashCode()
	{
		return (document == null) ? -1 : document.hashCode();
	}

	public boolean isSeed()
	{
		return (document != null) && document.isSeed();
	}

	@Override
	public ITermVector termVector()
	{
		return (document == null) ? null : document.termVector();
	}

	/**
	 * Called by DownloadMonitor in case a timeout happens.
	 */
	@Override
	public void handleIoError()
	{
		downloadStatus	= DownloadStatus.IOERROR;
		document.setDownloadDone(true);
		documentParser.handleIoError();

		recycle();
	}

	@Override
	public String message()
	{
		return document == null ? "recycled" : document.getLocation().toString();
	}

	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	@Override
	public SearchResult searchResult()
	{
		return searchResult;
	}
	/**
	 * 
	 * @param resultDistributer
	 * @param searchNum			Index into the total number of (seeding) searches specified and being aggregated.
	 * @param resultNum		Result number among those returned by google.
	 */
	public void setSearchResult(SeedDistributor resultDistributer, int resultNum)
	{
		searchResult	= new SearchResult(resultDistributer, resultNum);
	}
	public SeedDistributor resultDistributer()
	{
		return (searchResult == null) ? null : searchResult.resultDistributer();
	}

	
	@Override
	public void setContinuation(Continuation<DocumentClosure> continuation)
	{
		this.continuation = continuation;
	}

	public Continuation<DocumentClosure> continuation()
	{
		return continuation;
	}

	public String getQuery()
	{
		return document != null ? document.getQuery() : null;
	}

	public Seed getSeed()
	{
		return document != null ? document.getSeed() : null;
	}

	/**
	 * @return the downloadStatus
	 */
	public DownloadStatus getDownloadStatus()
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			return downloadStatus;
		}
	}
	
	public boolean isUnprocessed()
	{
		return getDownloadStatus() == DownloadStatus.UNPROCESSED;
	}

	/**
	 * @param presetDocumentParser the presetDocumentParser to set
	 */
	public void setDocumentParser(DocumentParser presetDocumentParser)
	{
		this.documentParser = presetDocumentParser;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + document.getLocation() + "]";
	}
	public void serialize(OutputStream stream)
	{
		serialize(stream, FORMAT.XML);
	}
	public void serialize(OutputStream stream, FORMAT format)
	{
		Document document	= getDocument();
		try
		{
			document.serialize(stream, format);
			System.out.println("\n");
		}
		catch (SIMPLTranslationException e)
		{
			error("Could not serialize " + document);
			e.printStackTrace();
		}
	}
	public void serialize(StringBuilder buffy)
	{
		Document document	= getDocument();
		try
		{
			document.serialize(buffy);
			System.out.println("\n");
		}
		catch (SIMPLTranslationException e)
		{
			error("Could not serialize " + document);
			e.printStackTrace();
		}
	}
}
