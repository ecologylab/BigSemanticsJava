/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.collections.SetElement;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Continuation;
import ecologylab.generic.MathTools;
import ecologylab.io.Downloadable;
import ecologylab.net.ConnectionHelper;
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
import ecologylab.semantics.namesandnums.CFPrefNames;
import ecologylab.semantics.seeding.QandDownloadable;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ElementState.FORMAT;

/**
 * New Container object. Mostly just a closure around Document.
 * Used as a candidate and wrapper for downloading.
 * 
 * @author andruid
 *
 */
public class DocumentClosure<D extends Document, DC extends DocumentClosure> extends SetElement
implements TermVectorFeature, Downloadable, QandDownloadable<DC>
{
	D												document;
	
	SemanticInLinks									semanticInlinks;

	

//	protected double								cachedWeight;
	
	ParsedURL						initialPURL;

	/**
	 * Means we tried to connect to the page and parse it.
	 * The connect may have failed; in any case, we assume no need to try
	 * connecting to this one again. It may be in process now. It may be finished successfully.
	 */
	private boolean			downloadStarted;

	private boolean			downloadHasBeenQueued;

	/**
	 * If true (the normal case), then any MediaElements encountered will be added
	 * to the candidates collection, for possible inclusion in the visual information space.
	 */
	boolean					collectMedia	= true;
	/**
	 * If true (the normal case), then hyperlinks encounted will be fed to the
	 * web crawler, providing that they are traversable() and of the right mime types.
	 */
	boolean					crawlLinks		= true;

	/**
	 * Indicates that this Container is processed via drag and drop.
	 */
	private boolean			isDnd;

	boolean					bad;
	boolean					downloadDone;
	/** Status variable set to true while document is being parsed*/
	boolean					parsing;

	boolean recycling;
	
	private boolean 					cacheHit = false;

	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	protected SearchResult	searchResult;

	private final Object QUEUE_DOWNLOAD_LOCK			= new Object();
	private final Object PERFORM_DOWNLOAD_LOCK		= new Object();   
	private final Object DOCUMENT_LOCK						= new Object();   
	
	protected		NewInfoCollector	infoCollector;
	
	
	DownloadStatus							downloadStatus	= DownloadStatus.UNPROCESSED;
	
	private DocumentParser			documentParser;
	
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
	 */
	public void performDownload()
	throws IOException
	{
		synchronized (PERFORM_DOWNLOAD_LOCK)
		{
			if (downloadStarted)
				return;
			downloadStarted	= true;
		}
		if (infoCollector == null)	// this should NEVER happen!!!!!!!!!!!!!!!!!!!!
		{
			error("Cant downloadAndParse: InfoCollector= null.");
		}
		else
		{
			document.setInfoCollector(infoCollector);
			try
			{
				downloadAndParse();
			} catch (SocketTimeoutException e)
			{
				document.getSite().incrementNumTimeouts();
				downloadStatus	= DownloadStatus.IOERROR;
			}
		}
	}

	/**
	 *  Actually download the document and parse it.
	 *  Connect to the purl. Figure out the appropriate DocumentType.
	 *  If that works, use the DocumentType to parse the PURLConnection's InputStream,
	 *  or for FileDirectoryType, just the thing itself.
	 */
	protected void downloadAndParse()
	throws IOException
	{
		if (recycled() || document.isRecycled())
		{
			println("ERROR: Trying to downloadAndParse() page that's already recycled -- "+ location());
			return;
		}
		if (downloadDone)
		{
			error("Trying to downloadAndParse() page that's already download done.");
			return;
		}

		ParsedURL parsedURL = location();
		connect();					// evolves Document based on redirects & mime-type; sets the documentParser

		if( parsedURL.getTimeout() )
		{
			handleTimeout();
			return;
		}

		if (!bad && (documentParser != null) && 
				(documentParser.nullPURLConnectionOK() || (documentParser.purlConnection() != null)))
		{
			// container or not (it could turn out to be an image or some other mime type), parse the baby!
			parsing					= true;
			downloadStatus	= DownloadStatus.PARSING;

			if (documentParser.downloadingMessageOnConnect())
				infoCollector.displayStatus("Downloading " + location(), 2);

			Document result	= documentParser.parse();
			if (result != null && result != document)
			{
				// need to swap!
				
			}
			parsing					= false;
									
		 	if(Pref.lookupBoolean(CFPrefNames.CRAWL_CAREFULLY) && !documentParser.cacheHit) //infoCollector.getCrawlingSlow() && 
		 	{		 		
			  	int waitTime = (Pref.lookupInt(CFPrefNames.MIN_WAIT_TIME) * 1000) + (MathTools.random(100)*((Pref.lookupInt(CFPrefNames.MAX_WAIT_TIME)-Pref.lookupInt(CFPrefNames.MIN_WAIT_TIME)) * 10));
			 	System.out.println("Downloading slow, waiting: "+((float)waitTime/60000));
				infoCollector.crawlerDownloadMonitor().pause(waitTime);
		 	}
			
			
		}
		// else recycle() if errors like documentType == null in downloadDone()
	}
	
	
	
	interface DocumentParserConnectHelper extends ConnectionHelper
	{
		DocumentParser getResult ( );
		boolean hadRedirect();
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
	 */
	private void connect()
	{
		assert(document != null);
		final Document 	orignalDocument	= document;
		final ParsedURL originalPURL		= document.getLocation();
		
		DocumentParserConnectHelper documentParserConnectHelper = new DocumentParserConnectHelper()
		{
			DocumentParser	result;
			boolean 				hadRedirect = false;

			public void handleFileDirectory(File file)
			{
				// result = new FileDirectoryType(file, container,
				// infoCollector);
//				result = infoCollector.newFileDirectoryType(file);
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

			public void badResult()
			{
				orignalDocument.setRecycled();
				orignalDocument.recycle();
			}
			
			public boolean processRedirect(URL connectionURL) throws Exception
			{
				DocumentLocationMap<? extends Document>	documentLocationMap	= document.getDocumentLocationMap();
				ParsedURL connectionPURL	= new ParsedURL(connectionURL);
				hadRedirect = true;
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

			public DocumentParser getResult()
			{
				return result;
			}
			
			public boolean hadRedirect()
			{
				return this.hadRedirect;
			}
		};


		MetaMetadataCompositeField metaMetadata = document.getMetaMetadata();
		// then try to create a connection using the PURL
		String userAgentString	= /* (metaMetadata == null) ? null : */ metaMetadata.getUserAgentString();
		PURLConnection purlConnection = originalPURL.connect(documentParserConnectHelper, userAgentString);
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

			if (metaMetadata.hasDefaultSuffixOrMimeSelector())
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
		
//		String parserName				= metaMetadata.getParser();	
//		if (parserName == null)			//FIXME Hook HTMLDOMImageText up to html mime-type & suffixes; drop defaultness of parser
//			parserName = SemanticActionsKeyWords.HTML_IMAGE_DOM_TEXT_PARSER;
		
		if (documentParser == null)
			documentParser = DocumentParser.get((MetaMetadata) metaMetadata, infoCollector);
		if (documentParser != null)
		{
			documentParser.fillValues(purlConnection, this, infoCollector);
		}
	}

	public void changeDocument(Document newDocument) 
	{
		synchronized (DOCUMENT_LOCK)
		{
			Document oldDocument	= document;
			this.document					= (D) newDocument;
			
			newDocument.inheritValues(oldDocument);	
			semanticInlinks				= newDocument.semanticInlinks; // probably not needed, but just in case.
			oldDocument.recycle();
		}
	}

	
	
	
	public ParsedURL getInitialPURL()
	{
		return initialPURL;
	}

	public boolean isDownloadDone()
	{
		return downloadDone;
	}

	/**
	 * Called automatically by DownloadMonitor at the end of download.
	 */
	public void downloadAndParseDone()
	{
		downloadDone	= true;
		downloadStatus	= DownloadStatus.DOWNLOAD_DONE;
		document.setDownloadDone(true);
		
		SemanticsSite site	= getSite();
		if (site != null)
			site.endDownload();
		else
			error("site == null in downloadDone().");
		
		/*
		if (this.isTrueSeed && (infoCollector!=null))
			infoCollector.seedDownloadDone(this, bad);
		*/

		// When downloadDone, add best surrogate and best container to infoCollector
		if (!bad)
		{
			document.downloadAndParseDone(documentParser);
		}
		else
		{
			// due to dynamic mime type type detection in connect(), 
			// we didnt actually turn out to be a Container object.
			// or, the parse didn't collect any information!
			recycle(false);	// so free all resources, including connectionRecycle()
		}
	}
	
	

	@Override
	public SemanticsSite getSite()
	{
		Document document = this.document;
		return (document == null) ? null : document.getSite();
	}
	
	public boolean isRecycling()
	{
		return recycling;
	}
	
	public void recycle(boolean unconditional)
	{
		recycle();
	}
	
	public void recycle()
	{
		downloadStatus	= DownloadStatus.RECYCLED;
		
		//TODO -- recycle the thang!
	}
	public boolean recycled()
	{
		Document document = this.document;
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
	public boolean queueDownload()
	{
		if (recycled())
		{
			debugA("ERROR: cant queue download cause already recycled.");
			return false;
		}
		if (!testAndSetQueueDownload())
			return false;
		delete();	// remove from candidate pools! (does deleteHook as well)  
		final boolean result = !filteredOut(); // true!
		if (result)
		{
			SemanticsSite site					= document.getSite();
			if (site != null)
				site.beginDownload();
			
			downloadMonitor().download((DC) this, dispatchTarget());
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
		synchronized (QUEUE_DOWNLOAD_LOCK)
		{
			if (downloadHasBeenQueued)
				return false;
			downloadHasBeenQueued		= true;
			downloadStatus					= DownloadStatus.QUEUED;
			return true;
		}
	}
	/**
	 * Test state variable inside of QUEUE_DOWNLOAD_LOCK.
	 * 
	 * @return true if result has already been queued, so it should not be operated on further.
	 */
	public boolean downloadHasBeenQueued()
	{
		synchronized (QUEUE_DOWNLOAD_LOCK)
		{
			return downloadHasBeenQueued;
		}
	}


	public void cancelDownload()
	{
		if (downloadHasBeenQueued)
		{

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
	public DownloadMonitor<DC> downloadMonitor()
	{
		// GoogleSearch, SearchResults are all seeds
		return (DownloadMonitor<DC>) (isDnd ? NewInfoCollector.DND_DOWNLOAD_MONITOR : 
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
	 * Called in case an IO error happens.
	 */
	public void handleIoError()
	{
		debug("IOERROR");
		handleTimeout();
	}

	/**
	 * Called by DownloadMonitor in case a timeout happens.
	 */
	public boolean handleTimeout()
	{
		parsing					= false;
		downloadDone		= true;
		bad							= true;
		downloadStatus	= DownloadStatus.IOERROR;
		document.setDownloadDone(true);

		SemanticsSite site	= getSite();
		site.incrementNumTimeouts();
		site.endDownload();

		// have to do this by hand in the error case
		if (dispatchTarget != null )
		{
			dispatchTarget.callback((DC) this);
		}

		// When timeout happens and download is not completed, there may have some mediaElements created 
		// before timeout. So, we don't want to recycle the container, which contains mediaElements and those 
		// mediaElements may be in the candidatePools in the InfoCollector. 
		error("TIMEOUT while downloading container recycled=" + this.recycled() + " So, recycling!");
		
		recycle(false);

		return true;
	}

	@Override
	public boolean isRecycled()
	{
		return document == null || document.isRecycled();
	}

	@Override
	public void recycleUnconditionally()
	{
		getSite().setIgnored(true);
		recycle(true);
	}

	@Override
	public boolean shouldCancel()
	{
		SemanticsSite site	= getSite();
		boolean result = (site != null) && site.tooManyTimeouts();
		if (result)
			warning("This site should be cancelled because too many timeouts");
		return result;
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
	public void setDispatchTarget(Continuation<DC> dispatchTarget)
	{
		this.dispatchTarget = dispatchTarget;
	}

	Continuation<DC> 			dispatchTarget;
	
	public Continuation<DC> dispatchTarget()
	{
		return dispatchTarget;
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
		return downloadStatus;
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
