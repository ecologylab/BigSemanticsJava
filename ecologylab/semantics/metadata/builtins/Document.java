package ecologylab.semantics.metadata.builtins;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/

import java.util.ArrayList;

import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.WeightSet;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.ContainerWeightingStrategy;
import ecologylab.semantics.collecting.DocumentLocationMap;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.MetadataElement;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;
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
	
	@simpl_collection("location")
	ArrayList<MetadataParsedURL> 						additionalLocations;
	
	private DocumentClosure					documentClosure;
	
	SemanticInLinks									semanticInlinks;
	
	private boolean									downloadDone;

	SemanticsSite										site;
	
	/** State from retrieval & interaction */
	protected int										badImages;
	
	boolean 												sameDomainAsPrevious;
	
	/**
	 * documentType object for each document type 
	 * such as HTMLType, PDFType. 
	 */
	DocumentParser									documentParser;

	protected		NewInfoCollector		infoCollector;

	private boolean									alwaysAcceptRedirect;
	
	static public final Document 	RECYCLED_DOCUMENT	= new Document(ParsedURL.getAbsolute("http://recycled.document"));
	static public final Document 	UNDEFINED_DOCUMENT= new Document(ParsedURL.getAbsolute("http://undefined.document"));

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
	protected Document(ParsedURL location)
	{
		super(MetaMetadataRepository.getBaseDocumentMM());
		setLocation(location);
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
		return location == null ? null : location().getValue();
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
	
	final Object CREATE_CLOSURE_LOCK	= new Object();
	
	/**
	 * 
	 * @return A closure for this, or null, if this is not fit to be parsed.
	 */
	public DocumentClosure getOrConstructClosure()
	{
		DocumentClosure result	= this.documentClosure;
		if (result == null && !isRecycled())
		{
			synchronized (CREATE_CLOSURE_LOCK)
			{
				result	= this.documentClosure;
				if (result == null)
				{
					if (semanticInlinks == null)
						semanticInlinks	= new SemanticInLinks();
					
					result	= constructClosure();
					this.documentClosure	= result;
				}
			}
		}
		return result.downloadStatus == DownloadStatus.RECYCLED ? null : result;
	}

	/**
	 * @return
	 */
	protected DocumentClosure constructClosure()
	{
		return new DocumentClosure(this, semanticInlinks);
	}

	/**
	 * Needed in case of a redirect or direct binding, in which cases, the map will need updating
	 * for this's location.
	 */
	DocumentLocationMap<? extends Document> getDocumentLocationMap()
	{
		return infoCollector.getGlobalDocumentMap();
	}
	/**
	 * @param documentClosure the downloadClosure to set
	 */
//	void setDownloadClosure(DocumentClosure downloadClosure)
//	{
//		this.downloadClosure = downloadClosure;
//	}
	
	
	
	public SemanticsSite getSite()
	{
		SemanticsSite result	= this.site;
		if (result == null)
		{
			result		= SemanticsSite.getOrConstruct(this, infoCollector);
			this.site	= result;
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
		addAdditionalLocation(new MetadataParsedURL(newPurl));
	}
	
	public void addAdditionalLocation(MetadataParsedURL newMPurl)
	{
		if (additionalLocations == null)
			additionalLocations	= new ArrayList<MetadataParsedURL>(3);
		additionalLocations.add(newMPurl);
	}
	
	/**
	 * Used when oldDocument turns out to be re-directed from this.
	 * @param oldDocument
	 */
	public void inheritValues(Document oldDocument)
	{
		oldDocument.getDocumentLocationMap().remap(oldDocument, this);
		if (location == null)
		{
			location									= oldDocument.location;
			oldDocument.location			= null;
		}
		this.infoCollector					= oldDocument.infoCollector;
		SemanticInLinks oldInlinks	= oldDocument.semanticInlinks;
		if (semanticInlinks == null || semanticInlinks.size() == 0)
		{
			this.semanticInlinks				= oldInlinks;
			oldDocument.semanticInlinks	= null;
		}
		else if (oldInlinks != null)
			semanticInlinks.merge(oldInlinks);
		
		ArrayList<Metadata> oldMixins = oldDocument.getMixins();
		if (oldMixins != null)
			for (Metadata oldMixin : oldMixins)
				addMixin(oldMixin);

		ArrayList<MetadataParsedURL> oldAdditionalLocations = oldDocument.additionalLocations;
		if (oldAdditionalLocations != null)
			for (MetadataParsedURL otherLocation : oldAdditionalLocations)
				addAdditionalLocation(otherLocation);
		
		//TODO -- are there other values that should be propagated?! -- can use MetadataFieldDescriptors.
	}
	
	public SemanticInLinks getSemanticInlinks()
	{
		SemanticInLinks result	= this.semanticInlinks;
		if (result == null)
		{
			//TODO add concurrency control?!
			result								= new SemanticInLinks();
			this.semanticInlinks	= result;
		}
		return result;
	}
	
	public void addSemanticInlink(SemanticAnchor semanticAnchor, Document source)
	{
		getSemanticInlinks().add(semanticAnchor, source);
	}
	public void addInlink(Document source)
	{
		getSemanticInlinks().add(source);
	}
	
	public boolean queueDownload(Continuation dispatchTarget)
	{
		DocumentClosure documentClosure	= getOrConstructClosure();
		if (documentClosure == null)
			return false;
		if (dispatchTarget != null)
			documentClosure.setDispatchTarget(dispatchTarget);
		return documentClosure.queueDownload();
	}
	
	public boolean queueDownload()
	{
		return queueDownload(null);
	}
//	@Override
//	public void recycle()
//	{
//		super.recycle();
//		downloadStatus							= 
//	}

	/**
	 * @return the downloadDone
	 */
	public boolean isDownloadDone()
	{
		return downloadDone;
	}

	/**
	 * @param downloadDone the downloadDone to set
	 */
	void setDownloadDone(boolean downloadDone)
	{
		this.downloadDone = downloadDone;
	}
	
	/**
	 * Lookout for instances of the AnonymousDocument.
	 * @return	false in the base class and most subs.
	 */
	public boolean isAnonymous()
	{
		return false;
	}
	
	void setRecycled()
	{
		DocumentLocationMap<? extends Document> documentLocationMap = getDocumentLocationMap();
		documentLocationMap.setRecycled(getLocation());
		if (additionalLocations != null)
		{
			for (MetadataParsedURL additionalMPurl: additionalLocations)
				documentLocationMap.setRecycled(additionalMPurl);
		}
	}

	@Override
	public void recycle()
	{
		super.recycle();
		if (semanticInlinks != null)
		{
			semanticInlinks.recycle();
			semanticInlinks	= null;
		}
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "[" + getLocation() + "]";
	}

	public boolean isJustCrawl()
	{
		return false;
	}
	
	public void downloadAndParseDone(DocumentParser documentParser)
	{
		if (documentParser == null)
		{
//			recycle();
		}
		else
		{
			// free only some resources
			documentParser.connectionRecycle();
			
			//TODO -- completely recycle DocumentParser!?
		}
	}
	
	///////////////////////////////// Covers for methods in CompoundDocument that actually do something /////////////////////////////////
	public void addCandidateImage(Image image)
	{
	}
	
	public boolean isSeed()
	{
		return false;
	}
	public String getQuery()
	{
		return null;
	}
	
	public void setQuery(String query)
	{
		
	}
	public void addCandidateOutlink (Document newOutlink )
	{
	}
	public void perhapsAddDocumentClosureToPool ( )
	{
	}
	public void addCandidateTextClipping(TextClipping textClipping)
	{
	}
	public String getTitle()
	{
		return null;
	}
	public DocumentClosure swapNextBestOutlinkWith(DocumentClosure c)
	{
		return null;
	}
	public  void tryToGetBetterTextAfterInterestExpression(GenericElement<TextClipping> replaceMe)
	{
		
	}
	public void tryToGetBetterImageAfterInterestExpression(ImageClosure replaceMe)
	{
	}
	
	/**
	 * Base class does not keep track of clippings, so does nothing.
	 */
	public void addClipping(Clipping clipping)
	{
		
	}
}
