/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ecologylab.collections.SetElement;
import ecologylab.concurrent.Downloadable;
import ecologylab.generic.Continuation;
import ecologylab.io.DownloadProcessor;
import ecologylab.io.Files;
import ecologylab.net.ConnectionHelperJustRemote;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.RedirectHandling;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorFeature;
import ecologylab.semantics.seeding.SearchResult;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.library.geom.PointInt;

/**
 * New Container object. Mostly just a closure around Document.
 * Used as a candidate and wrapper for downloading.
 * 
 * @author andruid
 *
 */
public class DocumentClosure extends SetElement
implements TermVectorFeature, Downloadable, SemanticActionsKeyWords, Continuation<DocumentClosure>
{
	Document											document;
	
	private PURLConnection				purlConnection;

	private DocumentParser				documentParser;
	
	/**
	 * This is tracked mainly for debugging, so we can see what pURL was fed into the meta-metadata address resolver machine.
	 */
	ParsedURL											initialPURL;

	SemanticInLinks								semanticInlinks;

	ArrayList<Continuation<DocumentClosure>> continuations;
	
	DownloadStatus								downloadStatus	= DownloadStatus.UNPROCESSED;
	
	PointInt											dndPoint;

	protected		SemanticsGlobalScope	semanticsScope;
	
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
	
	private boolean 		cacheHit = false;

	private final Object DOWNLOAD_STATUS_LOCK			= new Object();
	private final Object DOCUMENT_LOCK						= new Object();   
	
	/**
	 * 
	 */
	private DocumentClosure(Document document, SemanticsGlobalScope semanticsSessionScope, SemanticInLinks semanticInlinks)
	{
		super();
		this.document							= document;
		this.semanticsScope				= semanticsSessionScope;
		this.semanticInlinks			= semanticInlinks;
		this.continuations 				= new ArrayList<Continuation<DocumentClosure>>();
	}

	/**
	 * Should only be called by Document.getOrCreateClosure().
	 * 
	 * @param document
	 * @param semanticInlinks
	 */
	DocumentClosure(Document document, SemanticInLinks semanticInlinks)
	{
		this(document, document.getSemanticsScope(), semanticInlinks);
	}

	/////////////////////// methods for downloadable //////////////////////////
	/**
	 * Called by DownloadMonitor to initiate download, and cleanup afterward.
	 * NO OTHER OBJECT SHOULD EVER CALL THIS METHOD!
	 * 
	 * Actually download the document and parse it.
	 * Connect to the purl. Figure out the appropriate Meta-Metadata and DocumentType.
	 * Process redirects as needed.
	 * @throws IOException 
	 */


	@Override
	public void performDownload()
	throws IOException
	{
		synchronized (DOWNLOAD_STATUS_LOCK)
		{
			if (!(downloadStatus == DownloadStatus.QUEUED || downloadStatus == DownloadStatus.UNPROCESSED))
				return;
			setDownloadStatusInternal(DownloadStatus.CONNECTING);
		}
		if (semanticsScope == null)	// this should NEVER happen!!!!!!!!!!!!!!!!!!!!
		{
			SemanticsGlobalScope documentInfoCollector = document.getSemanticsScope();
			if (documentInfoCollector == null)
			{
				error("Cant downloadAndParse: InfoCollector= null.");
				return;
			}
			semanticsScope	= documentInfoCollector;
		}
		document.setSemanticsSessionScope(semanticsScope);
		
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
			setDownloadStatusInternal(DownloadStatus.PARSING);
			if (documentParser.downloadingMessageOnConnect())
				semanticsScope.displayStatus("Downloading " + location(), 2);
			
			MetaMetadata metaMetadata	= (MetaMetadata) document.getMetaMetadata();
			ArrayList<SemanticAction> beforeSemanticActions	= metaMetadata.getBeforeSemanticActions();
			if (beforeSemanticActions != null)
			{
				SemanticActionHandler handler	= new SemanticActionHandler(semanticsScope, documentParser);
				handler.takeSemanticActions(metaMetadata, document, beforeSemanticActions);
			}
			documentParser.parse();
			
			ArrayList<SemanticAction> afterSemanticActions	= metaMetadata.getAfterSemanticActions();
			if (afterSemanticActions != null)
			{
				SemanticActionHandler handler	= new SemanticActionHandler(semanticsScope, documentParser);
				handler.takeSemanticActions(metaMetadata, document, afterSemanticActions);
			}
			
			setDownloadStatusInternal(DownloadStatus.DOWNLOAD_DONE);
									
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
		final ParsedURL originalPURL		= document.getDownloadLocation();
		
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
				Document result = semanticsScope.getMetaMetadataRepository().constructDocumentBySuffix(suffix);
				changeDocument(result);
				return (result != null);
			}

			@Override
			public void displayStatus(String message)
			{
				semanticsScope.displayStatus(message);
			}
			
			@Override
			public boolean processRedirect(URL redirectedURL) throws IOException
			{
				ParsedURL redirectedPURL	= new ParsedURL(redirectedURL);
				displayStatus("try redirecting: " + originalPURL + " > " + redirectedURL);
				Document redirectedDocument	= semanticsScope.getOrConstructDocument(redirectedPURL); // documentLocationMap.getOrCreate(connectionPURL);
				//TODO -- what if redirectedDocument is already in the queue or being downloaded already?
				if (redirectedDocument != null)	// existing document
				{	// the redirected url has been visited already.
					
					// it seems we don't need to track where the inlinks are from, 
					// though we could in SemanticInlinks, if we kept a DocumentClosure in there
//					if (container != null)
//						container.redirectInlinksTo(redirectedAbstractContainer);

					if (!originalPURL.equals(redirectedPURL))
						redirectedDocument.addAdditionalLocation(redirectedPURL);
					//TODO -- copy metadata from originalDocument?!!
					changeDocument(redirectedDocument);
					//TODO -- reconnect
					
					// redirectedAbstractContainer.performDownload();

					// we dont need the new container object that was passed in
					// TODO recycle it!
					return true;
				}
				else
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

						/*
						 * Was unnecessary  because of how ecocache handles the acm gateway pages
						 * But actually, we are not using ecocache :-(
						 */
						//FIXME -- use meta-metadata to express this case!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						if (/* !Pref.lookupBoolean(CFPrefNames.USING_PROXY) && */
								"acm.org".equals(domain) && "pdf".equals(connPURLSuffix))
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
			File file	= originalPURL.file();
			if (!file.exists())
			{
				// this might be pointing to an entry in a ZIP file, e.g. the packed composition file.
				File ancestor = Files.findFirstExistingAncestor(file);
				if (ancestor != null && !ancestor.isDirectory() && Files.isZipFile(ancestor))
				{
					// read from a ZIP file, which should be the packed composition file
					String entryName = ancestor.toURI().relativize(file.toURI()).toString();
					entryName = URLDecoder.decode(entryName, "utf-8");
					ZipFile zipFile = new ZipFile(ancestor);
					ZipEntry entry = zipFile.getEntry(entryName);
					if (entry == null)
					{
						warning("No zip entry found: " + entryName);
					}
					else
					{
						InputStream in = zipFile.getInputStream(entry);
						purlConnection.streamConnect(in);
					}
				}
			}
			else if (file.isDirectory())
			{
				// FileDirectoryParser
				documentParser	= DocumentParser.getParserInstanceFromBindingMap(FILE_DIRECTORY_PARSER, semanticsScope);
			}
			else
			{
				purlConnection.fileConnect();
				// we already have the correct meta-metadata, having used suffix to construct, or having gotten it from a restore.
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
					}
				}
			}
		}
	//		String parserName				= metaMetadata.getParser();	
	//		if (parserName == null)			//FIXME Hook HTMLDOMImageText up to html mime-type & suffixes; drop defaultness of parser
	//			parserName = SemanticActionsKeyWords.HTML_IMAGE_DOM_TEXT_PARSER;
			
		if (documentParser == null)
			documentParser = DocumentParser.get((MetaMetadata) metaMetadata, semanticsScope);
		if (documentParser != null)
		{
			documentParser.fillValues(purlConnection, this, semanticsScope);
		}
		else if (!DocumentParser.isRegisteredNoParser(purlConnection.getPurl()))
			warning("No DocumentParser found: " + metaMetadata);
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
			this.document					= newDocument;
			
			newDocument.inheritValues(oldDocument);	
			
			SimplTypesScope.serializeOut(newDocument, "After changeDocument()", StringFormat.XML);
			
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
	
	public boolean isFromSite(SemanticsSite site)
	{
		return site != null && site == getSite();
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
			setDownloadStatusInternal(DownloadStatus.RECYCLED);
		}
		
		if (documentParser != null)
			documentParser.recycle();

		if (purlConnection != null)
			purlConnection.recycle();

		semanticInlinks	= null;

		initialPURL			= null;

//		if (continuations != null)
//			continuations.clear();	
//		continuations		= null;
			
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
	public Document getDocument()
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
	 * @return	true if this is actually queued for download. false if it was previously, if its been recycled, or if it is muted.
	 */
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
			
			downloadMonitor().download(this, continuations == null ? null : this);
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
			setDownloadStatusInternal(DownloadStatus.QUEUED);
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

	public DownloadProcessor<DocumentClosure> downloadMonitor()
	{
		return  semanticsScope.getDownloadMonitors().downloadProcessor(document.isImage(), isDnd(), isSeed(), document.isGui());
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
	public void handleIoError(Throwable e)
	{
		setDownloadStatusInternal(DownloadStatus.IOERROR);
		document.setDownloadDone(true);
		if (documentParser != null)
			documentParser.handleIoError(e);

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


	private ArrayList<Continuation<DocumentClosure>> continuations()
	{
		return continuations;
	}
	
	public void addContinuation(Continuation<DocumentClosure> continuation)
	{
		synchronized (continuations)
		{
			continuations().add(continuation);
		}
	}
	public void addContinuationBefore(Continuation<DocumentClosure> continuation)
	{
		synchronized (continuations)
		{
			continuations().add(0, continuation);
		}
	}	

	public void addContinuations(ArrayList<Continuation<DocumentClosure>> incomingContinuations)
	{
		synchronized (continuations)
		{
			ArrayList<Continuation<DocumentClosure>> continuations = continuations();
			for (Continuation<DocumentClosure> continuation: incomingContinuations)
				continuations.add(continuation);
		}
	}

	public ArrayList<Continuation<DocumentClosure>> getContinuations()
	{
		return continuations;
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
	
	/**
	 * (this method does not lock DOWNLOAD_STATUS_LOCK!)
	 * 
	 * @param newStatus
	 */
	private void setDownloadStatusInternal(DownloadStatus newStatus)
	{
		this.downloadStatus = newStatus;
		if (this.document != null)
			document.setDownloadStatus(newStatus);
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
		serialize(stream, StringFormat.XML);
	}
	public void serialize(OutputStream stream, StringFormat format)
	{
		Document document	= getDocument();
		try
		{
			SimplTypesScope.serialize(document, System.out, format);
			
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
			SimplTypesScope.serialize(document, buffy, StringFormat.XML);
			System.out.println("\n");
		}
		catch (SIMPLTranslationException e)
		{
			error("Could not serialize " + document);
			e.printStackTrace();
		}
	}

	/**
	 * @return the infoCollector
	 */
	public SemanticsGlobalScope getSemanticsScope()
	{
		return semanticsScope;
	}

	/**
	 * Dispatch all of our registered callbacks.
	 */
	@Override
	public void callback(DocumentClosure o)
	{
		// FIXME callback() could be called from different DownloadMonitor threads! and this is causing
		// concurrent modification problems for this.continuations collection in some cases.
		// we may need a synchronization mechanism here, but should look out for performance.
		if (continuations == null)
			return;
		
		List<Continuation<DocumentClosure>> currentContinuations;
		synchronized (continuations)
		{
			currentContinuations = new ArrayList<Continuation<DocumentClosure>>(continuations);
		}
		if (currentContinuations != null)
		{
			for (Continuation<DocumentClosure> continuation: currentContinuations)
			{
				try
				{
				continuation.callback(o);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		// wait to recycle continuations until after they have been called.
		if (isRecycled())
		{
			continuations.clear();	
			continuations		= null;
		}
	}
	
	/**
	 * Close the current connection.
	 * Re-open a connection to the same location.
	 * Use the same Document object; don't process re-directs, or anything like that.
	 * Re-connect simply.
	 * 
	 * @return	PURLConnection for the new connection.
	 */
	public PURLConnection reConnect()
	{
		purlConnection.close();
		purlConnection.recycle();
		purlConnection	= document.getLocation().connect(document.getMetaMetadata().getUserAgentString());
		return purlConnection;
	}

	@Override
	public boolean isImage()
	{
		return document.isImage();
	}
	
	public PointInt getDndPoint()
	{
		return dndPoint;
	}

	public void setDndPoint(PointInt dndPoint)
	{
		this.dndPoint = dndPoint;
	}
	
	public boolean isDnd()
	{
		return dndPoint != null;
	}
	
	public DocumentParser getParser()
	{
		return documentParser;
	}
	
	
	}
