package ecologylab.semantics.metadata.builtins;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.collecting.TNGGlobalCollections;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.documentparsers.ParserResult;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.mm_name;
import ecologylab.semantics.metadata.mm_no;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * The Document Class
 **/

@simpl_inherit
public class Document extends Metadata
{
	@mm_name("location")
	@simpl_scalar
	MetadataParsedURL								location;

	@mm_name("title") 
	@simpl_scalar MetadataString		title;
	
	@mm_name("description") 
	@simpl_scalar MetadataString		description;

	@mm_name("additional_locations")
	@simpl_collection("location")
	List<MetadataParsedURL>					additionalLocations;

	private DocumentClosure					documentClosure;

	SemanticInLinks									semanticInlinks;

	private boolean									downloadDone;

	SemanticsSite										site;

	protected SemanticsGlobalScope	semanticsScope;

	/**
	 * documentType object for each document type such as HTMLType, PDFType.
	 */
	DocumentParser									documentParser;

	ParserResult										parserResult;

	/** State from retrieval & interaction */
	protected int										badImages;

	boolean													sameDomainAsPrevious;

	private boolean									alwaysAcceptRedirect;

	/**
	 * Seed object associated with this, if this is a seed.
	 */
	private Seed										seed;

	/**
	 * Indicates that this Container is a truly a seed, not just one that is associated into a Seed's
	 * inverted index.
	 */
	private boolean									isTrueSeed;

	/**
	 * Indicates that this Container is processed via drag and drop.
	 */
	private boolean									isDnd;

	static public final Document 	RECYCLED_DOCUMENT	= new Document(ParsedURL.getAbsolute("http://recycled.document"));
	static public final Document 	UNDEFINED_DOCUMENT= new Document(ParsedURL.getAbsolute("http://undefined.document"));
	
	/**
	 * used in the database.
	 */
	@simpl_scalar
	@mm_no
	private int											downloadStatus;

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

	@Override
	public ParsedURL getLocation()
	{
		return location == null ? null : location.getValue();
	}
	
	/**
	 * Just use the regular location.
	 * 
	 * @return
	 */
	public ParsedURL getDownloadLocation()
	{
		return getLocation();
	}
	/**
	 * Sets the value of the field location
	 **/

	@Override
	public void setLocation(ParsedURL location)
	{
		if (location != null)
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
	}

	/**
	 * The heavy weight setter method for field location
	 **/

	@Override
	public void hwSetLocation(ParsedURL location)
	{
		setLocation(location);
	}

	public MetadataParsedURL getLocationMetadata()
	{
		return this.location;
	}
	
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
		return title == null ? null : title().getValue();
	}
	
	/**
	 * (used by the ORM layer.)
	 * 
	 * @return
	 */
	public MetadataString getTitleMetadata()
	{
		return title;
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
	 * (used by the ORM layer.)
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
	 * (used by the ORM layer.)
	 * 
	 * @return
	 */
	public MetadataString getDescriptionMetadata()
	{
		return description;
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
	public DocumentClosure constructClosure()
	{
		return new DocumentClosure(this, semanticInlinks);
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
			result		= semanticsScope.getMetaMetadataRepository().getSite(this, semanticsScope);
			this.site	= result;
		}
		return result;
	}
	/**
	 * @return the infoCollector
	 */
	public SemanticsGlobalScope getSemanticsScope()
	{
		return semanticsScope;
	}

	/**
	 * @param semanticsSessionScope the infoCollector to set
	 */
	public void setSemanticsSessionScope(SemanticsGlobalScope semanticsSessionScope)
	{
		this.semanticsScope = semanticsSessionScope;
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
	 * Get the old location from this.
	 * Set the location of this to the newLocation.
	 * Add a mapping in the GlobalCollection from newLocation to this.
	 * Add the old location for this as an additionalLocation for this.
	 * 
	 * @param newLocation
	 */
	public void changeLocation(final ParsedURL newLocation)
	{
		if (newLocation != null)
		{
			ParsedURL origLocation	= getLocation();
			if (!origLocation.equals(newLocation));
			{
				setLocation(newLocation);
				getSemanticsScope().getGlobalCollection().addMapping(newLocation, this);
				addAdditionalLocation(origLocation);
			}
		}
	}

	/**
	 * Used when oldDocument turns out to be re-directed from this.
	 * @param oldDocument
	 */
	public void inheritValues(Document oldDocument)
	{
		oldDocument.getSemanticsScope().getGlobalCollection().remap(oldDocument, this);
		if (location == null)
		{
			location									= oldDocument.location;
			oldDocument.location			= null;
		}
		this.semanticsScope					= oldDocument.semanticsScope;
		SemanticInLinks oldInlinks	= oldDocument.semanticInlinks;
		if (semanticInlinks == null || semanticInlinks.size() == 0)
		{
			this.semanticInlinks				= oldInlinks;
			oldDocument.semanticInlinks	= null;
		}
		else if (oldInlinks != null)
			semanticInlinks.merge(oldInlinks);
		
		List<Metadata> oldMixins = oldDocument.getMixins();
		if (oldMixins != null)
			for (Metadata oldMixin : oldMixins)
				addMixin(oldMixin);

		List<MetadataParsedURL> oldAdditionalLocations = oldDocument.additionalLocations;
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
			documentClosure.addContinuation(dispatchTarget);
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
		TNGGlobalCollections globalCollection = semanticsScope.getGlobalCollection();
		globalCollection.setRecycled(getLocation());
		if (additionalLocations != null)
		{
			for (MetadataParsedURL additionalMPurl: additionalLocations)
				globalCollection.setRecycled(additionalMPurl.getValue());
		}
	}
	
	@Override
	public void recycle()
	{
		recycle(new HashSet<Metadata>());
	}

	@Override
	public synchronized void recycle(HashSet<Metadata> visitedMetadata)
	{
		super.recycle(visitedMetadata);
		if (semanticInlinks != null)
		{
			semanticInlinks.recycle();
			semanticInlinks	= null;
		}
		if (parserResult != null)
		{
			parserResult.recycle();
			parserResult		= null;
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

	}
	
	///////////////////////////////// Covers for methods in CompoundDocument that actually do something /////////////////////////////////
	
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
	
	/**
	 * Base class does not keep track of clippings, so does nothing.
	 */
	public void addClipping(Clipping clipping)
	{
		
	}
	
	public String getLocationsString()
	{
		String result;
		if (additionalLocations == null || additionalLocations.size() == 0)
			result	= location.toString();
		else
		{
			StringBuilder buffy	= new StringBuilder(location.toString()).append(',');
			for (MetadataParsedURL otherLocation : additionalLocations)
				buffy.append(otherLocation.toString());
			result	= buffy.toString();
		}
		return result;
	}

	/**
	 * @return the parserResult
	 */
	public ParserResult getParserResult()
	{
		return parserResult;
	}

	/**
	 * @param parserResult the parserResult to set
	 */
	public void setParserResult(ParserResult parserResult)
	{
		this.parserResult = parserResult;
	}
	
	/**
	 * @return the seed
	 */
	public Seed getSeed()
	{
		return seed;
	}

	/**
	 * @param seed
	 *          the seed to set
	 */
	public void setSeed(Seed seed)
	{
		this.seed = seed;
	}

	/**
	 * If this Container was a search, the index number of that search among the searches being
	 * aggregated at one time. Otherwise, -1.
	 * 
	 * @return The search index number or -1 if not a search.
	 */
	public int searchNum()
	{
		if (isTrueSeed && (seed instanceof SearchState))
		{
			return ((SearchState) seed).searchNum();
		}
		return -1;
	}

	/**
	 * Called for true seed Containers. Calling this method does more than bind the Seed object with
	 * the Container in the model. It also sets the crucial isSeed flag, establishing that this
	 * Container is truly a Seed.
	 * <p/>
	 * NB: The seed object will also be bound with ancestors of the Container.
	 * 
	 * @param seed
	 */
	public void setAsTrueSeed(Seed seed)
	{
		// associateSeed(seed);
		this.seed = seed;
		isTrueSeed = true;
	}

	/**
	 * Indicate that this Container is being processed via DnD.
	 * 
	 */
	void setDnd()
	{
		isDnd = true;
	}

	public boolean isDnd()
	{
		return isDnd;
	}

	public List<MetadataParsedURL> getAdditionalLocations()
	{
		return additionalLocations;
	}

	public void setAdditionalLocations(List<MetadataParsedURL> additionalLocations)
	{
		this.additionalLocations = additionalLocations;
	}

	@Override
	public boolean hasLocation()
	{
		return location != null;
	}

	public int getDownloadStatus()
	{
		return downloadStatus;
	}

	public void setDownloadStatus(int downloadStatus)
	{
		this.downloadStatus = downloadStatus;
	}
}
