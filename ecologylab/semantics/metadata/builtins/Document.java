package ecologylab.semantics.metadata.builtins;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/

import java.util.ArrayList;
import java.util.Vector;

import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.WeightSet;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.DocumentClosure;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.connectors.SemanticsSite;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;

/**
 * The Document Class
 **/

@simpl_inherit
public class Document extends Metadata
{
	@mm_name("location") 
	@simpl_scalar MetadataParsedURL	location;
	
	@mm_name("title") 
	@simpl_scalar MetadataString		title;
	
	@mm_name("description") 
	@simpl_scalar MetadataString		description;
	

	/**
	 * The search query
	 **/
	@simpl_scalar @simpl_hints(Hint.XML_LEAF)
	private MetadataString					query;
	
	@simpl_collection("additional_location")
	Vector<ParsedURL> 						additionalLocations;

	/**
	 * For debugging. Type of the structure recognized by information extraction.
	 **/

	@mm_name("page_structure") 
	@simpl_scalar
	private MetadataString	        pageStructure;
	
	private DocumentClosure					downloadClosure;
	
	private SemanticInLinks					semanticInlinks;
	
	ArrayList<ImageClipping>				imageClippings;
	
	ArrayList<TextClipping>					textClippings;
	
	/**
	 * Seed object associated with this, if this is a seed.
	 */
	private Seed										seed;
	
	private boolean									isTrueSeed;

	/**
	 * Weighted collection of <code>ImageElement</code>s.
	 * Contain elements that have not been transported to candidatePool. 
	 */
	private GenericWeightSet<ImageClipping>	candidateLocalImages;

	/**
	 * Weighted collection of <code>TextElement</code>s.
	 * Contain elements that have not been transported to candidatePool.
	 */
	private GenericWeightSet<TextClipping>	candidateLocalTexts;
	
	private WeightSet<DocumentClosure>			candidateLocalOutlinks;
	
	SemanticsSite										site;
	
	/** State from retrieval & interaction */
	protected int										badImages;
	
	boolean 												sameDomainAsPrevious;
	
	/**
	 * documentType object for each document type 
	 * such as HTMLType, PDFType. 
	 */
	DocumentParser									documentParser;

	private		NewInfoCollector			infoCollector;

	
	/** Number of surrogates from this container that are currently on screen */
	int						onScreenCount;
	int						onScreenTextCount;
	
	/** Total number of surrogates that have ever been on screen from this container */
	int						totalVisualized;

	private static final double	MIN_WEIGHT_THRESHOLD	= 0.;
	
	private boolean									alwaysAcceptRedirect;
	
	
	//////////////////////////////////////// candidates loops state ////////////////////////////////////////////////////////////
	
	boolean								additionalContainersActive;
	private boolean				additionalImgSurrogatesActive;
	private boolean				additionalTextSurrogatesActive;
	
	/** Number of surrogates from this container in a candidate pool */
	private int numSurrogatesFrom = 0;

	/**
	 * Occasionally, we want to navigate to somewhere other than the regular purl,
	 * as in when this is an RSS feed, but there's an equivalent HTML page.
	 */
//	@simpl_scalar MetadataParsedURL	navLocation;
	
	/**
	 * Constructor
	 **/

	public Document()
	{
		super();
	}

	/**
	 * Constructor
	 **/

	public Document(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}
	
	/**
	 * Construct an instance of this, the base document type, and set its location.
	 * 
	 * @param location
	 */
	public Document(ParsedURL location)
	{
		super(MetaMetadataRepository.getBaseDocumentMM());
		setLocation(location);
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

	public String getTitle()
	{
		return title().getValue();
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
		return description().getValue();
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
	 * Lazy Evaluation for location
	 **/

	public MetadataParsedURL location()
	{
		MetadataParsedURL result = this.location;
		if (result == null)
		{
			result = new MetadataParsedURL();
			this.location = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field location
	 **/

	public ParsedURL getLocation()
	{
		return location().getValue();
	}

	/**
	 * Sets the value of the field location
	 **/

	public void setLocation(ParsedURL location)
	{
		this.location().setValue(location);
		
		Document ancestor	=  getAncestor();
		if (ancestor != null)
		{
			ParsedURL ancestorLocation = ancestor.getLocation();
			String domain = location.domain();
			sameDomainAsPrevious =
				(ancestorLocation != null && domain != null && domain.equals(ancestorLocation.domain()));
		}
	}

	/**
	 * The heavy weight setter method for field location
	 **/

	public void hwSetLocation(ParsedURL location)
	{
		this.location().setValue(location);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the location directly
	 **/

	public void setLocationMetadata(MetadataParsedURL location)
	{
		this.location = location;
	}

	/**
	 * Heavy Weight Direct setter method for location
	 **/

	public void hwSetLocationMetadata(MetadataParsedURL location)
	{
		if (this.location != null && this.location.getValue() != null && hasTermVector())
			termVector().remove(this.location.termVector());
		this.location = location;
		rebuildCompositeTermVector();
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
		return pageStructure().getValue();
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

	public String getQuery()
	{
		return query().getValue();
	}

	/**
	 * Sets the value of the field query
	 **/

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

	/**
	 * @return the alwaysAcceptRedirects
	 */
	public boolean isAlwaysAcceptRedirect()
	{
		return alwaysAcceptRedirect;
	}

	/**
	 * @param alwaysAcceptRedirects the alwaysAcceptRedirects to set
	 */
	public void setAlwaysAcceptRedirect(boolean alwaysAcceptRedirects)
	{
		this.alwaysAcceptRedirect = alwaysAcceptRedirects;
	}

	/**
	 * @return the documentParser
	 */
	public DocumentParser getDocumentParser()
	{
		return documentParser;
	}
	
	//////////////////////////////////////// candidates loops state ////////////////////////////////////////////////////////////
	/**
	 * 
	 * 1. First, only one surrogate goes to candidate pool. 
	 * 2. Good looking surrogates, number of surrogates from current container, and users' interest 
	 *    expression will determine to bring more surrogates from current container to the candidate pool.
	 * @param getText 
	 */
	double mostRecentImageWeight = 0, mostRecentTextWeight = 0;
	
	public synchronized void perhapsAddAdditionalContainer ( )
	{
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
			doRecycle = ! infoCollector.addCandidateClosure(candidate); // successful add means do not recycle
		}
		else
		{
			//Debug only
			debug("This container failed to provide a decent container so is going bye bye, max weight was " + maxWeight );
		}
			
		if(doRecycle)
			makeInactiveAndConsiderRecycling();
	}
	
	
	private void makeInactiveAndConsiderRecycling()
	{
		additionalContainersActive = false;
		recycle();
	}


	public synchronized void perhapsAddAdditionalTextSurrogate()
	{
		TextClipping textClipping = null;
		if (candidateLocalTexts != null)
		{
			textClipping = candidateLocalTexts.maxGenericSelect();
		}
		if( textClipping!=null )
		{
			// If no surrogate has been delivered to the candidate pool from the container, 
			// send it to the candidate pool without checking the media weight. 
			if( numSurrogatesFrom==0 )
				infoCollector.addCandidateTextClipping(textClipping);
			else
			{
				float adjustedWeight 		= InterestModel.getInterestExpressedInTermVector(textClipping.termVector()) / (float) numSurrogatesFrom;
				float meanTxtSetWeight	= infoCollector.candidateTextElementsSetsMean();
				if ((adjustedWeight>=meanTxtSetWeight) || infoCollector.candidateTextElementsSetIsAlmostEmpty())
				{
					infoCollector.addCandidateTextClipping(textClipping);
					mostRecentTextWeight = InterestModel.getInterestExpressedInTermVector(textClipping.termVector());
				}
				else
				{
					textClipping.recycle(false);
					additionalTextSurrogatesActive	= false;
					//recycle(false);
				}
			}
		}
		else
			additionalTextSurrogatesActive	= false;
	}
	public synchronized void perhapsAddAdditionalImgSurrogate()
	{

		ImageClipping imgElement = null;
		if (candidateLocalImages != null)
			imgElement = candidateLocalImages.maxGenericSelect();

		if( imgElement!=null && imgElement.termVector() != null && !imgElement.termVector().isRecycled())
		{
			// If no surrogate has been delivered to the candidate pool from the container, 
			// send it to the candidate pool without checking the media weight.
			boolean goForIt		= numSurrogatesFrom==0;
			if (!goForIt)
			{
				goForIt = infoCollector.imagePoolsSize() < 2;	// we're starved for images so go for it!
				if (!goForIt)
				{
					float adjustedWeight			= InterestModel.getInterestExpressedInTermVector(imgElement.termVector()) / (float) numSurrogatesFrom;
					
					float meanImgPoolsWeight	= infoCollector.imagePoolsMean();
					
					goForIt										= adjustedWeight >= meanImgPoolsWeight;
				}
			}

			if (goForIt)
			{
				if (!imgElement.downloadAndParse())
					perhapsAddAdditionalImgSurrogate();
				mostRecentImageWeight = InterestModel.getInterestExpressedInTermVector(imgElement.termVector());
			}
			else
			{
				imgElement.recycle(false);
				additionalImgSurrogatesActive	= false;
				//recycle(false);
			}
		}
		else
			additionalImgSurrogatesActive	= false;
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
	
	
	public void downloadAndParseDone()
	{

		// When downloadDone, add best surrogate and best container to infoCollector
		if (documentParser != null)
		{
			additionalTextSurrogatesActive	= true;
			additionalImgSurrogatesActive	= true;
			additionalContainersActive	= true;
			perhapsAddAdditionalTextSurrogate();
			perhapsAddAdditionalImgSurrogate();
			perhapsAddAdditionalContainer();
		}
		//TODO reconsider this -- call recycle()?!
		// free resources if nothing was collected
//		if ((outlinks != null) && outlinks.isEmpty())
//			outlinks		= null;
		if ((candidateLocalImages != null) && candidateLocalImages.isEmpty())
			candidateLocalImages	= null;
		if ((candidateLocalTexts != null) && candidateLocalTexts.isEmpty())
			candidateLocalTexts	= null;

		if ((documentParser != null) && documentParser.isContainer() && !isTotallyEmpty())	// add && !isEmpty() -- andruid 3/2/09
		{	
			if (documentParser.isIndexPage())
				site.newIndexPage();
			if (documentParser.isContentPage())
				site.newContentPage();

			// free only some resources
			documentParser.connectionRecycle();
		}
		else
		{
			// due to dynamic mime type type detection in connect(), 
			// we didnt actually turn out to be a Container object.
			// or, the parse didn't collect any information!
			recycle();	// so free all resources, including connectionRecycle()
		}
	}

	/**
	 * @return	true if there are no MediaElements that this Container is tracking
	 */
	private boolean hasEmptyElementCollections()
	{
		return ((candidateLocalImages == null) || (candidateLocalImages.size()==0)) &&
				((candidateLocalTexts == null) || (candidateLocalTexts.size()==0));
	}
	
	private boolean isTotallyEmpty()
	{
		return hasEmptyElementCollections() /* && ((outlinks == null) || (outlinks.size() == 0)) */
		;
	}


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
	
	public synchronized void tryToGetBetterTextAfterInterestExpression(GenericElement<TextClipping> replaceMe)
	{
		if (candidateLocalTexts == null || candidateLocalTexts.size() == 0)
			return;
		
		GenericElement<TextClipping> te = candidateLocalTexts.maxPeek();
		if (InterestModel.getInterestExpressedInTermVector(te.getGeneric().termVector()) > mostRecentTextWeight)
		{
			infoCollector.removeTextClippingFromPools(replaceMe);
			perhapsAddAdditionalTextSurrogate();
			// perhapsAddAdditionalTextSurrogate could call recycle on this container
			if (!this.isRecycled())
				candidateLocalTexts.insert(replaceMe);
		}
	}
	
	public synchronized void tryToGetBetterImagesAfterInterestExpression(GenericElement<ImageClipping> replaceMe)
	{
		if (candidateLocalImages == null || candidateLocalImages.size() == 0)
			return;
		
		GenericElement<ImageClipping> aie = candidateLocalImages.maxPeek();
		if (InterestModel.getInterestExpressedInTermVector(aie.getGeneric().termVector()) > mostRecentImageWeight)
		{
			infoCollector.removeImageClippingFromPools(replaceMe);
			perhapsAddAdditionalImgSurrogate();
			candidateLocalImages.insert(replaceMe);
		}
	}

	
	public Document getAncestor()
	{
		return semanticInlinks == null ? null : semanticInlinks.getAncestor();
	}
	
	public int getGeneration()
	{
		return semanticInlinks == null ? 0 : semanticInlinks.getGeneration();
	}
	
	public int getEffectiveGeneration()
	{
		return semanticInlinks == null ? 0 : semanticInlinks.getEffectiveGeneration();
	}

	/**
	 * @return the sameDomainAsPrevious
	 */
	public boolean isSameDomainAsPrevious()
	{
		return sameDomainAsPrevious;
	}
	
	@Override
	public int hashCode()
	{
		return (location == null) ? -1 : location.hashCode();
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

	/**
	 * @return the downloadClosure
	 */
	public DocumentClosure getDownloadClosure()
	{
		return downloadClosure;
	}

	/**
	 * @param downloadClosure the downloadClosure to set
	 */
	void setDownloadClosure(DocumentClosure downloadClosure)
	{
		this.downloadClosure = downloadClosure;
	}
	
	public SemanticsSite getSite()
	{
		SemanticsSite result	= this.site;
		if (result == null)
		{
			site	= SemanticsSite.getOrConstruct(this, infoCollector);
		}
		return result;
	}

	/**
	 * @return the infoCollector
	 */
	public NewInfoCollector getInfoCollector()
	{
		return infoCollector;
	}

	/**
	 * @param infoCollector the infoCollector to set
	 */
	public void setInfoCollector(NewInfoCollector infoCollector)
	{
		this.infoCollector = infoCollector;
	}
	
	public void addAdditionalLocation(ParsedURL newPurl)
	{
		if (additionalLocations == null)
			additionalLocations	= new Vector<ParsedURL>(3);
		additionalLocations.add(newPurl);
	}
	
	/**
	 * Used when oldDocument turns out to be re-directed from this.
	 * @param oldDocument
	 */
	public void inheritValues(Document oldDocument)
	{
		this.semanticInlinks				= oldDocument.semanticInlinks;
		oldDocument.semanticInlinks	= null;
		this.query									= oldDocument.query;
		oldDocument.query						= null;
		//TODO -- are there other values that should be propagated?!
	}
}
