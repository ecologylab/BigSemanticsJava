/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.Crawler;
import ecologylab.semantics.documentparsers.CompoundDocumentParserCrawlerResult;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.namesandnums.SemanticsNames;
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
{
	private static final String	CONTENT_PAGE	= "content_page";

	private static final String	INDEX_PAGE	= "index_page";

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
	 * Clippings that this document contains.
	 */
	@mm_name("clippings") 
	@simpl_collection
	@simpl_classes({ImageClipping.class, TextClipping.class})
//	@simpl_scope(SemanticsNames.REPOSITORY_CLIPPING_TRANSLATIONS)
	ArrayList<Clipping>								clippings;


	
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
		super(location);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isCompoundDocument()
	{
		return true;
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

	
	
	////////////////////////////////// Downloadable /////////////////////////////////////////////////////
	
	
	public void downloadAndParseDone(DocumentParser documentParser)
	{
		if (clippings != null && clippings.size() > 0)
		{	
			if (documentParser.isIndexPage())
			{
				site.newIndexPage();
				setPageStructure(INDEX_PAGE);
			}
			else if (documentParser.isContentPage())
			{
				site.newContentPage();
				setPageStructure(CONTENT_PAGE);
			}

			// When downloadDone, add best surrogate and best container to infoCollector
			Crawler crawler	= semanticsSessionScope.getCrawler();
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
	}

	@Override
	public boolean isJustCrawl()
	{
		return isTrueSeed && seed != null && seed.isJustCrawl();
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
	
	/**
	 * Used when oldDocument turns out to be re-directed from this.
	 * @param oldDocument
	 */
	@Override
	public void inheritValues(Document oldDocument)
	{
		super.inheritValues(oldDocument);
		
		if (oldDocument instanceof Document)
		{
			CompoundDocument oldCompound= (CompoundDocument) oldDocument;
			
			String queryString					= this.getQuery();
			if (queryString == null || queryString.length() == 0)
				this.query									= oldCompound.query;
			oldCompound.query						= null;
		}
		
	}
	
	/**
	 * Add to collection of clippings, representing our compound documentness.
	 */
	@Override
	public void addClipping(Clipping clipping)
	{
		clippings().add(clipping);
	}

	/**
	 * @return the clippings
	 */
	public ArrayList<Clipping> getClippings()
	{
		return clippings;
	}
	
	/**
	 * 
	 * @return	The number of Clippings that have been collected, if any.
	 */
	public int numClippings()
	{
		return clippings == null ? 0 : clippings.size();
	}

}
