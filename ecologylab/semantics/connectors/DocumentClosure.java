/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.collections.SetElement;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.DispatchTarget;
import ecologylab.generic.MathTools;
import ecologylab.io.BasicSite;
import ecologylab.io.Downloadable;
import ecologylab.net.ConnectionHelper;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.RedirectHandling;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorFeature;
import ecologylab.semantics.namesandnums.CFPrefNames;
import ecologylab.semantics.seeding.SearchResult;

/**
 * New Container object. Mostly just a closure around Document.
 * Used as a candidate and wrapper for downloading.
 * 
 * @author andruid
 *
 */
public class DocumentClosure<D extends Document> extends SetElement
implements TermVectorFeature, Downloadable
{
	D												document;
	
	SemanticInLinks									semanticInlinks;

	

	double								cachedWeight;
	
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
	 * Indicates that this Container is a truly a seed, not just one
	 * that is associated into a Seed's inverted index.
	 */
	private boolean			isTrueSeed;

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

	private final Object QUEUE_DOWNLOAD_LOCK		= new Object();
	private final Object PERFORM_DOWNLOAD_LOCK		= new Object();   
	
	private		NewInfoCollector	infoCollector;
	
	DocumentLocationMap<D>			documentLocationMap;
	
	/**
	 * 
	 */
	public DocumentClosure(D document, NewInfoCollector infoCollector, DocumentLocationMap<D> documentLocationMap)
	{
		this.document							= document;
		this.infoCollector				= infoCollector;
		this.documentLocationMap	= documentLocationMap;
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
			}
		}
	}

	/**
	 *  Actually download the document and parse it.
	 *  Connect to the purl. Figure out the appropriate DocumentType.
	 *  If that works, use the DocumentType to parse the PURLConnection's InputStream,
	 *  or for FileDirectoryType, just the thing itself.
	 */
	private void downloadAndParse()
	throws IOException
	{
		if (recycled())
		{
			println("ERROR: Trying to downloadAndParse() page that's already recycled -- "+ location());
			return;
		}
		if (downloadDone)
		{
			error("Trying to downloadAndParse() page that's already download done.");
			return;
		}

		ParsedURL parsedURL 				= location();
		DocumentParser documentParser	= connect();

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
		Document newMetadata();
	}

//	private void connect()
//	{
//		{
//			Document metadata			= document;
//			if (metadata == null)
//			{
//				MetaMetadataCompositeField metaMetadata = infoCollector.metaMetaDataRepository().getDocumentMM(purl);
//				metadata						= (Document) ((MetaMetadata) metaMetadata).constructMetadata();
//			}
//			return connect(metadata, container, infoCollector);
//		}
//	}
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
	 * @param metadata TODO
	 */
	private DocumentParser connect()
	{
		assert(document != null);
		final Document 	orignalDocument	= document;
		final ParsedURL originalPurl		= document.getLocation();
		
		DocumentParserConnectHelper documentParserConnectHelper = new DocumentParserConnectHelper()
		{
			DocumentParser	result;
			boolean 				hadRedirect = false;
			Document 				newMetadata;

			public void handleFileDirectory(File file)
			{
				// result = new FileDirectoryType(file, container,
				// infoCollector);
//				result = infoCollector.newFileDirectoryType(file);
			}

			/**
			 * For use in local file processing, not for http.
			 */
			public boolean parseFilesWithSuffix(String suffix)
			{
				MetaMetadata mmd = infoCollector.metaMetaDataRepository().getMMBySuffix(suffix);
				if (mmd!=null)
					result = DocumentParser.getParserInstanceFromBindingMap(mmd.getParser(), infoCollector);
				return (result != null);
			}

			public void displayStatus(String message)
			{
				infoCollector.displayStatus(message);
			}

			public void badResult()
			{
				if (result != null)
				{
					result.recycle();
					result = null;
				}
			}

			public boolean processRedirect(URL connectionURL) throws Exception
			{
				ParsedURL connectionPURL = new ParsedURL(connectionURL);
				hadRedirect = true;
				displayStatus("redirecting: " + originalPurl + " > " + connectionURL);
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
						println("redirect: " + originalPurl + " -> " + connectionPURL);
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
							// get new metadata
							newMetadata	= documentLocationMap.getOrCreate(connectionPURL);

							infoCollector.constructDocument(connectionPURL);
							newMetadata.inheritValues(orignalDocument);
						}

						if (redirectHandling == RedirectHandling.REDIRECT_FOLLOW_DONT_RESET_LOCATION)
						{
							newMetadata.setLocation(originalPurl);
							newMetadata.addAdditionalLocation(connectionPURL);
						}
						else
							newMetadata.addAdditionalLocation(originalPurl);

						DocumentClosure.this.document	= (D) newMetadata;

						return true;
					}
					else
						println("rejecting redirect: " + originalPurl + " -> " + connectionPURL);
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
			public Document newMetadata()
			{
				return this.newMetadata;
			}
		};


		MetaMetadataCompositeField metaMetadata = document.getMetaMetadata();
		// then try to create a connection using the PURL
		String userAgentString	= /* (metaMetadata == null) ? null : */ metaMetadata.getUserAgentString();
		PURLConnection purlConnection = originalPurl.connect(documentParserConnectHelper, userAgentString);
		DocumentParser result 	= documentParserConnectHelper.getResult();
		
		Document document				= this.document;					// may have changed during redirect processing
		metaMetadata						= document.getMetaMetadata();

		// check for a parser that was discovered while processing a re-direct
		
		SemanticsSite site 					= document.getSite();

		// if a parser was preset for this container, use it
//		if ((result == null) && (container != null))
//			result = container.getDocumentParser();
	
		// if we made PURL connection but could not find parser using container
		if ((purlConnection != null) && !originalPurl.isFile())
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
						Document mimeMetadata	= (Document) ((MetaMetadata) mimeMmd).constructMetadata();
						mimeMetadata.inheritValues(document);
						document.recycle();
						document	= mimeMetadata;
						this.document	= (D) document;
					}
					metaMetadata	= mimeMmd;
				}
			}
			// if we haven't got a DocumentParser here, using the binding hint; if we already have one,
			// it is very likely a predefined one, e.g. MetaMetadataSearchParser
			if (result == null)
			{
				String parserName = metaMetadata.getParser();
				if (parserName == null)
					parserName = SemanticActionsKeyWords.DEFAULT_PARSER;
				result = DocumentParser.getParserInstanceFromBindingMap(parserName, infoCollector);
			}
		}
		
		if (result != null)
		{
			result.fillValues(purlConnection, document, infoCollector);
		}
		
		return result;
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
			document.downloadAndParseDone();
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
		return document;
	}
	
	DocumentClosure getAncestorClosure()
	{
		DocumentClosure result = null;
		if (document != null)
		{
			Document ancestor	= document.getAncestor();
			if (ancestor != null)
				result					= ancestor.getDownloadClosure();
		}
		return  result;
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
			SemanticsSite site					= SemanticsSite.getOrConstruct(getDocument(), infoCollector);
			if (site != null)
				site.beginDownload();
			
			downloadMonitor().download(this, dispatchTarget());
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
	protected DownloadMonitor downloadMonitor()
	{
		// GoogleSearch, SearchResults are all seeds
		return isDnd ? infoCollector.getDndDownloadMonitor() : 
			isTrueSeed ? 
				infoCollector.getSeedingDownloadMonitor(): infoCollector.getCrawlerDownloadMonitor();
	}

	DispatchTarget dispatchTarget = null;
	DispatchTarget dispatchTarget()
	{
		return dispatchTarget;
	}

	public void setDispatchTarget(DispatchTarget dispatchTarget)
	{
		this.dispatchTarget = dispatchTarget;
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

		SemanticsSite site	= getSite();
		site.incrementNumTimeouts();
		site.endDownload();

		// have to do this by hand in the error case
		if (dispatchTarget != null )
		{
			dispatchTarget.delivery(this);
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



}
