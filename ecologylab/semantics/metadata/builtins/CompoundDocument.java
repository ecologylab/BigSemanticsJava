/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.ArrayList;

import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.WeightSet;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.ContainerWeightingStrategy;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.MetadataElement;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;

/**
 * A Document that can be broken down into clippings, including references to other documents.
 * HTML and PDF are prime examples.
 * 
 * @author andruid
 */
@simpl_inherit
public class CompoundDocument extends Document
implements Continuation<DocumentClosure>
{
	/**
	 * For debugging. Type of the structure recognized by information extraction.
	 **/

	@mm_name("page_structure") 
	@simpl_scalar
	private MetadataString	        pageStructure;
	
	@mm_name("title") 
	@simpl_scalar MetadataString		title;
	
	@mm_name("description") 
	@simpl_scalar MetadataString		description;

	/**
	 * The search query
	 **/
	@simpl_scalar @simpl_hints(Hint.XML_LEAF)
	private MetadataString					query;
	
	/**
	 * Seed object associated with this, if this is a seed.
	 */
	private Seed										seed;
	
	/**
	 * Indicates that this Document is a truly a seed, not just one
	 * that is associated into a Seed's inverted index.
	 */
	private boolean									isTrueSeed;

	/**
	 * Weighted collection of <code>ImageElement</code>s.
	 * Contain elements that have not been transported to candidatePool. 
	 */
	private WeightSet<DocumentClosure<Image>>	candidateImageClosures;

	/**
	 * Weighted collection of <code>TextElement</code>s.
	 * Contain elements that have not been transported to candidatePool.
	 */
	private GenericWeightSet<TextClipping>	candidateTextClippings;
	
	
	@mm_name("clippings") 
	@simpl_collection
	@simpl_classes({ImageClipping.class, TextClipping.class})
//	ArrayList<Metadata>							clippings;
	ArrayList<Clipping>								clippings;

	private WeightSet<DocumentClosure>			candidateLocalOutlinks;
	

	
	/** Number of surrogates from this container that are currently on screen */
	int						onScreenCount;
	int						onScreenTextCount;
	
	/** Total number of surrogates that have ever been on screen from this container */
	int						totalVisualized;

	private static final double	MIN_WEIGHT_THRESHOLD	= 0.;
	
	private boolean									useFirstCandidateWeight	= true;
	
	
	//////////////////////////////////////// candidates loops state ////////////////////////////////////////////////////////////
	
	boolean								additionalContainersActive;
	private boolean				additionalImgSurrogatesActive;
	private boolean				additionalTextSurrogatesActive;
	
	/** Number of surrogates from this container in a candidate pool */
	private int numSurrogatesFrom = 0;
	

	/**
	 * 
	 */
	public CompoundDocument()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param metaMetadata
	 */
	public CompoundDocument(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param location
	 */
	public CompoundDocument(ParsedURL location)
	{
		super(location);
		// TODO Auto-generated constructor stub
	}

	
	
	/**
	 * Lazy Evaluation for pageStructure
	 **/

	public MetadataString pageStructure()
	{
		MetadataString result = this.pageStructure;
		if (result == null)
		{
			result = new MetadataString();
			this.pageStructure = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field pageStructure
	 **/

	public String getPageStructure()
	{
		return pageStructure == null ? null : pageStructure().getValue();
	}

	/**
	 * Sets the value of the field pageStructure
	 **/

	public void setPageStructure(String pageStructure)
	{
		this.pageStructure().setValue(pageStructure);
	}

	/**
	 * The heavy weight setter method for field pageStructure
	 **/

	public void hwSetPageStructure(String pageStructure)
	{
		this.pageStructure().setValue(pageStructure);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the pageStructure directly
	 **/

	public void setPageStructureMetadata(MetadataString pageStructure)
	{
		this.pageStructure = pageStructure;
	}

	/**
	 * Heavy Weight Direct setter method for pageStructure
	 **/

	public void hwSetPageStructureMetadata(MetadataString pageStructure)
	{
		if (this.pageStructure != null && this.pageStructure.getValue() != null && hasTermVector())
			termVector().remove(this.pageStructure.termVector());
		this.pageStructure = pageStructure;
		rebuildCompositeTermVector();
	}


	/**
	 * Lazy Evaluation for title
	 **/

	public MetadataString title()
	{
		MetadataString result = this.title;
		if (result == null)
		{
			result = new MetadataString();
			this.title = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field title
	 **/
	@Override
	public String getTitle()
	{
		return title == null ? null : title().getValue();
	}

	/**
	 * Sets the value of the field title
	 **/

	public void setTitle(String title)
	{
		this.title().setValue(title);
	}

	/**
	 * The heavy weight setter method for field title
	 **/

	public void hwSetTitle(String title)
	{
		this.title().setValue(title);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the title directly
	 **/

	public void setTitleMetadata(MetadataString title)
	{
		this.title = title;
	}

	/**
	 * Heavy Weight Direct setter method for title
	 **/

	public void hwSetTitleMetadata(MetadataString title)
	{
		if (this.title != null && this.title.getValue() != null && hasTermVector())
			termVector().remove(this.title.termVector());
		this.title = title;
		rebuildCompositeTermVector();
	}


	/**
	 * Lazy Evaluation for description
	 **/

	public MetadataString description()
	{
		MetadataString result = this.description;
		if (result == null)
		{
			result = new MetadataString();
			this.description = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field description
	 **/

	public String getDescription()
	{
		return description == null ? null : description().getValue();
	}

	/**
	 * Sets the value of the field description
	 **/

	public void setDescription(String description)
	{
		this.description().setValue(description);
	}

	/**
	 * The heavy weight setter method for field description
	 **/

	public void hwSetDescription(String description)
	{
		this.description().setValue(description);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the description directly
	 **/

	public void setDescriptionMetadata(MetadataString description)
	{
		this.description = description;
	}

	/**
	 * Heavy Weight Direct setter method for description
	 **/

	public void hwSetDescriptionMetadata(MetadataString description)
	{
		if (this.description != null && this.description.getValue() != null && hasTermVector())
			termVector().remove(this.description.termVector());
		this.description = description;
		rebuildCompositeTermVector();
	}

	/**
	 * Lazy Evaluation for query
	 **/

	public MetadataString query()
	{
		MetadataString result = this.query;
		if (result == null)
		{
			result = new MetadataString();
			this.query = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field query
	 **/
	@Override
	public String getQuery()
	{
		return query == null ? null : query().getValue();
	}

	/**
	 * Sets the value of the field query
	 **/
	@Override
	public void setQuery(String query)
	{
		this.query().setValue(query);
	}

	/**
	 * The heavy weight setter method for field query
	 **/

	public void hwSetQuery(String query)
	{
		this.query().setValue(query);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the query directly
	 **/

	public void setQueryMetadata(MetadataString query)
	{
		this.query = query;
	}

	/**
	 * Heavy Weight Direct setter method for query
	 **/

	public void hwSetQueryMetadata(MetadataString query)
	{
		if (this.query != null && this.query.getValue() != null && hasTermVector())
			termVector().remove(this.query.termVector());
		this.query = query;
		rebuildCompositeTermVector();
	}
	/**
	 * Insert the queryMetadata into the composite term vector FOR THE FIRST TIME.
	 * Use a coefficient to control its emphasis, in order to avoid overpowering
	 * the weighting with a weak (distantly crawled) relationship to the original search.
	 * 
	 * @param query
	 * @param weight		Factor to affect the impact of the search query on the composite term vector weights.
	 */
	public void hwInitializeQueryMetadata(MetadataString query, double weight)
	{
		this.query = query;
		termVector().add(weight, query.termVector());
	}

	//////////////////////////////////////// candidates loops state ////////////////////////////////////////////////////////////
	@Override
	public void addCandidateOutlink (Document newOutlink )
	{
		if (!newOutlink.isSeed() && !newOutlink.isDownloadDone())	// a seed is never a candidate
		{
			DocumentClosure documentClosure	= newOutlink.getOrConstructClosure();
			if (documentClosure != null && documentClosure.getDownloadStatus() == DownloadStatus.UNPROCESSED)
			{
				if (candidateLocalOutlinks == null)
					candidateLocalOutlinks			=  new WeightSet<DocumentClosure>(new ContainerWeightingStrategy(InterestModel.getPIV()));
				candidateLocalOutlinks.insert(documentClosure);
			}
		}
	}
	
	int clippingPoolPriority()
	{
		int result = useFirstCandidateWeight ? 
				(isSeed() ? 0 : 1) : 2;
		useFirstCandidateWeight		= false;
				
		return result;
	}
	/**
	 * 
	 * 1. First, only one surrogate goes to candidate pool. 
	 * 2. Good looking surrogates, number of surrogates from current container, and users' interest 
	 *    expression will determine to bring more surrogates from current container to the candidate pool.
	 * @param getText 
	 */
	double mostRecentImageWeight = 0, mostRecentTextWeight = 0;
	
	@Override
	public synchronized void perhapsAddDocumentClosureToPool ( )
	{
		if (!infoCollector.isCollectCandidatesInPools())
			return;
		
		if (candidateLocalOutlinks == null || candidateLocalOutlinks.size() == 0)
		{
			makeInactiveAndConsiderRecycling();
			return;
		}
		
		double maxWeight = candidateLocalOutlinks.maxWeight();
		boolean doRecycle = true;
		if(maxWeight > MIN_WEIGHT_THRESHOLD)
		{
			DocumentClosure candidate = candidateLocalOutlinks.maxSelect();
			doRecycle = ! infoCollector.addClosureToPool(candidate); // successful add means do not recycle
		}
		else
		{
			//Debug only
			debug("This container failed to provide a decent container so is going bye bye, max weight was " + maxWeight );
		}
			
		if (doRecycle)
			makeInactiveAndConsiderRecycling();
	}
	
	
	private void makeInactiveAndConsiderRecycling()
	{
		additionalContainersActive = false;
		recycle();
	}


	public synchronized void perhapsAddTextClippingToPool()
	{
		if (!infoCollector.isCollectCandidatesInPools())
			return;
		
		GenericElement<TextClipping> textClippingGE = null;
		if (candidateTextClippings != null)
		{
			textClippingGE = candidateTextClippings.maxSelect();
		}
		if( textClippingGE!=null )
		{
			// If no surrogate has been delivered to the candidate pool from the container, 
			// send it to the candidate pool without checking the media weight. 
			if( numSurrogatesFrom==0 )
				infoCollector.addTextClippingToPool(textClippingGE, clippingPoolPriority());
			else
			{
				TextClipping textClipping	= textClippingGE.getGeneric();
				float adjustedWeight 		= InterestModel.getInterestExpressedInTermVector(textClipping.termVector()) / (float) numSurrogatesFrom;
				float meanTxtSetWeight	= infoCollector.candidateTextElementsSetsMean();
				if ((adjustedWeight>=meanTxtSetWeight) || infoCollector.candidateTextElementsSetIsAlmostEmpty())
				{
					infoCollector.addTextClippingToPool(textClippingGE, clippingPoolPriority());
					mostRecentTextWeight = InterestModel.getInterestExpressedInTermVector(textClipping.termVector());
				}
				else
				{
					textClippingGE.recycle(false);
					additionalTextSurrogatesActive	= false;
					//recycle(false);
				}
			}
		}
		else
			additionalTextSurrogatesActive	= false;
	}
	public synchronized void perhapsAddImageClosureToPool()
	{
		if (!infoCollector.isCollectCandidatesInPools())
			return;
		
		DocumentClosure<Image> imageClosure = null;
		if (candidateImageClosures != null)
			imageClosure = candidateImageClosures.maxSelect();

		if (imageClosure!=null && imageClosure.termVector() != null && !imageClosure.termVector().isRecycled())
		{
			// If no surrogate has been delivered to the candidate pool from the container, 
			// send it to the candidate pool without checking the media weight.
			boolean firstClipping = numSurrogatesFrom==0;
			boolean goForIt				= firstClipping;
			if (!goForIt)
			{
				goForIt = infoCollector.imagePoolsSize() < 2;	// we're starved for images so go for it!
				if (!goForIt)
				{
					float adjustedWeight			= InterestModel.getInterestExpressedInTermVector(imageClosure.termVector()) / (float) numSurrogatesFrom;
					
					float meanImgPoolsWeight	= infoCollector.imagePoolsMean();
					
					goForIt										= adjustedWeight >= meanImgPoolsWeight;
				}
			}

			if (goForIt)
			{
				// if (firstClipping) else queue in MediaReferencesPool
				
				if (!imageClosure.queueDownload())
					perhapsAddImageClosureToPool();
			}
			else
			{
				imageClosure.recycle(false);
				additionalImgSurrogatesActive	= false;
				//recycle(false);
			}
		}
		else
			additionalImgSurrogatesActive	= false;
	}
	
	public void callback(DocumentClosure imageClosure)
	{
		mostRecentImageWeight = InterestModel.getInterestExpressedInTermVector(imageClosure.termVector());
		perhapsAddImageClosureToPool();
	}
	
	private void considerRecycling()
	{
		if (additionalContainersActive || additionalImgSurrogatesActive || additionalTextSurrogatesActive)
			debug("DIDNT RECYCLE AFTER CONSIDERATION.\nCONTAINERS_ACTIVE: " 
					+ additionalContainersActive
					+ "\tTEXT_SURROGATES_ACTIVE: "
					+ additionalTextSurrogatesActive
					+ "\tIMAGE_SURROGATES_ACTIVE: "
					+ additionalImgSurrogatesActive);
		else
			recycle();
	}
	

	
	
	////////////////////////////////// Downloadable /////////////////////////////////////////////////////
	
	
	public void downloadAndParseDone(DocumentParser documentParser)
	{
		// When downloadDone, add best surrogate and best container to infoCollector
		if (documentParser != null)
		{
			additionalTextSurrogatesActive	= true;
			additionalImgSurrogatesActive	= true;
			additionalContainersActive	= true;
			perhapsAddTextClippingToPool();
			perhapsAddImageClosureToPool();
			perhapsAddDocumentClosureToPool();
		}

		if ((documentParser != null) && !isTotallyEmpty())	// add && !isEmpty() -- andruid 3/2/09
		{	
			if (documentParser.isIndexPage())
				site.newIndexPage();
			if (documentParser.isContentPage())
				site.newContentPage();
			
			//TODO -- completely recycle DocumentParser!?
		}
		else
		{
			// due to dynamic mime type type detection in connect(), 
			// we didnt actually turn out to be a Container object.
			// or, the parse didn't collect any information!
//			recycle();	// so free all resources, including connectionRecycle()
		}
	}

	/**
	 * @return	true if there are no MediaElements that this Container is tracking
	 */
	private boolean hasEmptyElementCollections()
	{
		return ((candidateImageClosures == null) || (candidateImageClosures.size()==0)) &&
				((candidateTextClippings == null) || (candidateTextClippings.size()==0));
	}
	
	private boolean isTotallyEmpty()
	{
		return hasEmptyElementCollections() /* && ((outlinks == null) || (outlinks.size() == 0)) */
		;
	}

	@Override
	public DocumentClosure swapNextBestOutlinkWith(DocumentClosure c)
	{
		
		if (candidateLocalOutlinks == null || candidateLocalOutlinks.size() == 0)
			return null;
		synchronized(candidateLocalOutlinks)
		{
			candidateLocalOutlinks.insert(c);
			return candidateLocalOutlinks.maxSelect();
		}
	}
	
	@Override
	public synchronized void tryToGetBetterTextAfterInterestExpression(GenericElement<TextClipping> replaceMe)
	{
		if (candidateTextClippings == null || candidateTextClippings.size() == 0)
			return;
		
		GenericElement<TextClipping> te = candidateTextClippings.maxPeek();
		if (InterestModel.getInterestExpressedInTermVector(te.getGeneric().termVector()) > mostRecentTextWeight)
		{
			infoCollector.removeTextClippingFromPools(replaceMe);
			perhapsAddTextClippingToPool();
			// perhapsAddAdditionalTextSurrogate could call recycle on this container
			if (!this.isRecycled())
				candidateTextClippings.insert(replaceMe);
		}
	}
	
	@Override
	public synchronized void tryToGetBetterImageAfterInterestExpression(DocumentClosure<Image> replaceMe)
	{
		if (candidateImageClosures == null || candidateImageClosures.size() == 0)
			return;
		
		DocumentClosure<Image> aie = candidateImageClosures.maxPeek();
		if (InterestModel.getInterestExpressedInTermVector(aie.termVector()) > mostRecentImageWeight)
		{
			infoCollector.removeImageClippingFromPools(replaceMe);
			perhapsAddImageClosureToPool();
			candidateImageClosures.insert(replaceMe);
		}
	}

	@Override
	public boolean isJustCrawl()
	{
		return isTrueSeed && seed != null && seed.isJustCrawl();
	}
	
	int sizeCandidateTextClippings()
	{
		return candidateTextClippings == null ? 0 : candidateTextClippings.size();
	}
	
	int sizeCandidateImageClosures()
	{
		return candidateImageClosures == null ? 0 : candidateImageClosures.size();
	}
	
	public int sizeLocalCandidates()
	{
		return sizeCandidateTextClippings() + sizeCandidateImageClosures();
	}
	
	@Override
	protected void serializationPreHook()
	{
//		if (clippings == null)
//		{
//			int size	= 0;
//			boolean doImages	= false;
//			if  (candidateImageClosures != null)
//			{
//				size		 += candidateImageClosures.size();
//				doImages	= true;
//			}
//			if (candidateTextClippings != null)
//			{
//				size 		 += candidateTextClippings.size();
//				clippings	= new ArrayList<Metadata>(size);
//				for (GenericElement<TextClipping>	textClippingGE  : candidateTextClippings)
//					clippings.add(textClippingGE.getGeneric());
//			}
//			if (doImages)
//			{
//				if (clippings == null)
//					clippings	= new ArrayList<Metadata>(size);
//				for (ImageClosure ic : candidateImageClosures)
//					clippings.add(ic.getDocument());
//			}
//		}
	}
	
	public void setAsTrueSeed(Seed seed)
	{
		associateSeed(seed);
		isTrueSeed		= true;
	}
	/**
	 * Associate the Seed object with this Container.
	 * Calls to this method may reflect that this Container is just a Seed, or
	 * they may only reflect that this Container needs to be in the Seed's inverted index.
	 * @param seed
	 */
	public void associateSeed(Seed seed)
	{
		this.seed		= seed; 
	}

	@Override
	public boolean isSeed()
	{
		return isTrueSeed;
	}
	/**
	 * return the seed from where the container originated
	 * @return
	 */
	public Seed getSeed()
	{
		return seed;
	}
	
	@Override
	public void addCandidateTextClipping(TextClipping textClipping)
	{
		if (!isJustCrawl())
		{
			if (candidateTextClippings == null)
				candidateTextClippings	=  new GenericWeightSet<TextClipping>(new TermVectorWeightStrategy(InterestModel.getPIV()))
				{
					@Override
					public boolean insert(TextClipping go)
					{
						return insert(new MetadataElement<TextClipping>(go));
					}

				};
			candidateTextClippings.insert(textClipping);
			clippings().add(textClipping);
		}
	}
	
	ArrayList<Clipping> clippings()
	{
		ArrayList<Clipping> result	= this.clippings;
		if (result == null)
		{
			result										= new ArrayList<Clipping>();
			this.clippings						= result;
		}
		return result;
	}
	
	
	@Override
	public void addCandidateImage(Image image)
	{
		if (!isJustCrawl())
		{
			//FIXME -- look out for already downloaded!!!
			if (candidateImageClosures == null)
				candidateImageClosures	=  new WeightSet<DocumentClosure<Image>>(new TermVectorWeightStrategy(InterestModel.getPIV()));
			candidateImageClosures.insert((DocumentClosure<Image>) image.getOrConstructClosure());
		}
	}
	
	/**
	 * Used when oldDocument turns out to be re-directed from this.
	 * @param oldDocument
	 */
	@Override
	public void inheritValues(Document oldDocument)
	{
		super.inheritValues(oldDocument);
		
		CompoundDocument oldCompound= (CompoundDocument) oldDocument;
		
		String queryString					= this.getQuery();
		if (queryString == null || queryString.length() == 0)
			this.query									= oldCompound.query;
		oldCompound.query						= null;
		
		
	}
	
	/**
	 * Add to collection of clippings, representing our compound documentness.
	 */
	@Override
	public void addClipping(Clipping clipping)
	{
		clippings().add(clipping);
	}

}
