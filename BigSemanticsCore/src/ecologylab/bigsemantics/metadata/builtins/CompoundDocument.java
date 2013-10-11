/**
 * 
 */
package ecologylab.bigsemantics.metadata.builtins;

import java.util.ArrayList;
import java.util.List;

import ecologylab.bigsemantics.collecting.Crawler;
import ecologylab.bigsemantics.documentparsers.CompoundDocumentParserCrawlerResult;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.metadata.builtins.declarations.CompoundDocumentDeclaration;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.seeding.Seed;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * A Document that can be broken down into clippings, including references to other documents.
 * HTML and PDF are prime examples.
 * 
 * @author andruid
 */
@simpl_inherit
public class CompoundDocument extends CompoundDocumentDeclaration
{
	
	private static final String	CONTENT_PAGE	= "content_page";

	private static final String	INDEX_PAGE		= "index_page";

//	/**
//	 * For debugging. Type of the structure recognized by information extraction.
//	 **/
//	@mm_name("page_structure") 
//	@simpl_scalar
//	private MetadataString	        		pageStructure;
//	
//	/**
//	 * The search query
//	 **/
//	@simpl_scalar @simpl_hints(Hint.XML_LEAF)
//	private MetadataString							query;
//	
//	/**
//	 * Clippings that this document contains.
//	 */
//	@mm_name("clippings") 
//	@simpl_collection
//	@simpl_classes({ImageClipping.class, TextClipping.class})
////	@simpl_scope(SemanticsNames.REPOSITORY_CLIPPING_TRANSLATIONS)
//	List<Clipping>											clippings;
//
//	/**
//	 * The rootDocument is filled in to create an alternative, connected CompoundDocument instance that is used to 
//	 * store the List<Clipping> clippings object associated with this. 
//	 * It can be used to merge the clippings collection for two or more related documents, such as a metadata page, and an associated PDF.
//	 */
//	@simpl_composite
//	private CompoundDocument						rootDocument;
	
	/**
	 * Seed object associated with this, if this is a seed.
	 */
	private Seed												seed;
	
	/**
	 * Indicates that this Document is a truly a seed, not just one
	 * that is associated into a Seed's inverted index.
	 */
	private boolean											isTrueSeed;


	
	/** Number of surrogates from this container that are currently on screen */
	int						onScreenCount;
	int						onScreenTextCount;
	
	/** Total number of surrogates that have ever been on screen from this container */
	int						totalVisualized;

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
		this(MetaMetadataRepository.getBaseDocumentMM());
		Document.initDocument(this, location);
	}

	@Override
	public boolean isCompoundDocument()
	{
		return true;
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
	 * Heavy Weight Direct setter method for pageStructure
	 **/
	public void hwSetPageStructureMetadata(MetadataString pageStructure)
	{
		if (!isPageStructureNull() && hasTermVector())
			termVector().remove(this.getPageStructureMetadata().termVector());
		this.setPageStructureMetadata(pageStructure);
		rebuildCompositeTermVector();
	}
	
	public boolean isPageStructureNull()
	{
		return this.getPageStructureMetadata() == null || this.getPageStructureMetadata().getValue() == null;
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
	 * Heavy Weight Direct setter method for query
	 **/
	public void hwSetQueryMetadata(MetadataString query)
	{
		if (!isQueryNull() && hasTermVector())
			termVector().remove(this.getQueryMetadata().termVector());
		this.setQueryMetadata(query);
		rebuildCompositeTermVector();
	}
	
	public boolean isQueryNull()
	{
		return this.getQueryMetadata() == null || this.getQueryMetadata().getValue() == null;
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
		setQueryMetadata(query);
		termVector().add(weight, query.termVector());
	}

	
	////////////////////////////////// Downloadable /////////////////////////////////////////////////////
	
	
	@Override
	public void downloadAndParseDone(DocumentParser documentParser)
	{
	  long t0 = System.currentTimeMillis();
	  
		if (numClippings() > 0)
		{	
			getSite(); // initialize this.site if haven't
			if (documentParser.isIndexPage())
			{
				getSite().newIndexPage();
				setPageStructure(INDEX_PAGE);
			}
			else if (documentParser.isContentPage())
			{
				getSite().newContentPage();
				setPageStructure(CONTENT_PAGE);
			}

			// When downloadDone, add best surrogate and best container to infoCollector
			Crawler crawler	= semanticsScope.getCrawler();
			if (documentParser != null && crawler != null)
			{
				CompoundDocumentParserCrawlerResult	crawlerResult	= crawler.constructCompoundDocumentParserResult(this, isJustCrawl());
				crawlerResult.collect();
			}

			//TODO -- completely recycle DocumentParser!?
		}
		else
		{
			// due to dynamic mime type type detection in connect(), 
			// we didnt actually turn out to be a Container object.
			// or, the parse didn't collect any information!
			//			recycle();	// so free all resources, including connectionRecycle()
		}
		
		if (documentParser != null)
		{
  		documentParser.getLogRecord().setMsCompoundDocumentDnpDone(System.currentTimeMillis() - t0);
		}
	}

	@Override
	public boolean isJustCrawl()
	{
		return isTrueSeed && seed != null && seed.isJustCrawl();
	}
	
	@Override
	public void serializationPreHook(TranslationContext translationContext)
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
	
	@Override
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
	@Override
	public Seed getSeed()
	{
		return seed;
	}
	
	/**
	 * Lazy evaluation of clippings field.
	 * If rootDocument non-null, get and construct in that, as necessary; else get and construct in this, as necessary.
	 * @return
	 */
	public List<Clipping> clippings()
	{
		return getRootDocument() != null ? getRootDocument().selfClippings() : selfClippings();
	}

	private List<Clipping> selfClippings()
	{
		List<Clipping> result = this.getClippings();
		if (result == null)
		{
			result = new ArrayList<Clipping>();
			this.setClippings(result);
		}
		return result;
	}

	/**
	 * @return the clippings
	 */
	public List<Clipping> getSelfClippings()
	{
		return getClippings();
	}
	
	/**
	 * Add to collection of clippings, representing our compound documentness.
	 */
	public void addClipping(Clipping clipping)
	{
		clippings().add(clipping);
	}

	/**
	 * 
	 * @return	The number of Clippings that have been collected, if any.
	 */
	public int numClippings()
	{
		return getClippings() == null ? 0 : getClippings().size();
	}

	/**
	 * Used when oldDocument turns out to be re-directed from this.
	 * @param oldDocument
	 */
	@Override
	public void inheritValues(Document oldDocument)
	{
		super.inheritValues(oldDocument);
		
		if (oldDocument instanceof CompoundDocument)
		{
			CompoundDocument oldCompound= (CompoundDocument) oldDocument;
			
			String queryString					= this.getQuery();
			if (queryString == null || queryString.length() == 0)
				this.setQueryMetadata(oldCompound.getQueryMetadata());
			oldCompound.setQueryMetadata(null);
			
			List<Clipping> oldClippings	= oldCompound.getClippings();
			if (this.getClippings() == null && oldClippings != null)
				this.setClippings(oldClippings);
		}
	}
	
}
