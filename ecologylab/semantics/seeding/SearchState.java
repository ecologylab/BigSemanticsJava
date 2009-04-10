package ecologylab.semantics.seeding;

import java.io.File;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.Container;
import ecologylab.documenttypes.InfoCollector;
import ecologylab.semantics.connectors.SearchEngineNames;
import ecologylab.semantics.connectors.SeedPeer;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.library.dc.Dc;

/**
 * {@link Seed Seed} element that directs combinFormation to perform a search.
 * 
 * Starts by providing a basis for specification of search seeds.
 * Then, keeps state during processing of the search.
 */
@xml_inherit
public class SearchState extends Seed
implements SemanticsPrefs, SearchEngineNames
{
	private static final float	NO_BIAS	= 1.0f;
	private static final short	INITIAL_INTEREST_LEVEL	= (short) 2;
	public static final int	NUM_IMAGE_RESULTS		= 40;
   
   /**
    * Search engine to use. Currently supported are google, flickr, yahoo, yahoo_image, yahoo_news, yahoo_buzz, delicious.
    */
   @xml_attribute protected String			engine;
   /**
    * Query string to pass to the search engine.
    */
   @xml_attribute protected String			query;
   
   protected String							queryNoPluses;
   
   /**
    * For del.icio.us only. Allows querying the delicious tags for a particular user.
    */
   @xml_attribute protected String			creator;
   
   /**
    * For Yahoo Buzz. Queries to buzz can either be "leaders" (the default) or "movers".
    * The latter tend to be more current and interesting.
    */
   @xml_attribute protected boolean			isMovers;
   
   /**
    * For any query that allows searching on DC fields.
    */
   @xml_nested protected Dc 				dc; 
   
   private int				numResults		= NUM_SEARCH_RESULTS.value();
   
   private int				firstResult;

   private int				searchType;
   
   /** 
    * how many results we have actually seen
    */
   private int numResultsFrom = 0;
   
   private String			site;
   
   /**
    * Initial level of interest in this seed.
    */
   private short			initialIntensity;

   /**
    * Among the searches that were specified (as part of seeding), which one is this?
    */
   private int				searchNum;
   
   private boolean			generatingTermDictionary;

   /**
    * Registry that maps the engine attribute in a SearchState Seed to a
    * DocumentType that will initiate and process the requested search.
    */
   private static final Scope	SEARCH_DOCUMENT_TYPE_REGISTRY	= new Scope();

   
   public SearchState()
   {
	   super();
   }
   public SearchState(String query, String engine)
   {
  	 this(query, engine, INITIAL_INTEREST_LEVEL);
   }
   public SearchState(String query, String engine, short interestLevel)
   {
  	 this(query, engine, interestLevel, NUM_SEARCH_RESULTS.value());
   }
   public SearchState(String query, String engine, short interestLevel, int numResults)
   {
  	 super();
	   setQuery(query);
	   this.engine 						= engine;
	   this.initialIntensity	= interestLevel;
	   if (YAHOO_IMAGE.equals(engine))
	   {
	  	 numResults					= NUM_IMAGE_RESULTS;
	   }
	   else
	  	 this.numResults		= numResults;
   }
   public SearchState(String query, String engine, short interestLevel, int numResults, boolean generatingTermDictionary)
   {
  	 this(query, engine, interestLevel, numResults);
	   this.noAggregator				= generatingTermDictionary;
	   this.generatingTermDictionary	= generatingTermDictionary;
   }
   /**
    * this is when we do a buzz, etc search, and seeds are created because they are a result.
    * 
    * @param ancestor the original seed (i.e. the seed that spawned this one)
    * @param query
    * @param engine
    */
   public SearchState(SeedPeer ancestor, InfoCollector infoProcessor, String query, String engine)
   {
	   this(query, engine);
	   //FIXME -- process ancestor
	   initialize(infoProcessor);
   }

   public static SearchState getGoogleSearchState(String query, int numResults)
   {
  	 return new SearchState(query, GOOGLE);
   }

   /**
    * Get the next results from the previous search seed.
    * 
    * @param previousSearch
    */
//   public SearchState(SearchState previousSearch)
//   {
//	   this(null, previousSearch.query, previousSearch.numResults, previousSearch.initialIntensity, previousSearch.bias);
//	   this.firstResult		= previousSearch.firstResult + numResults;
//	   this.engine			= previousSearch.engine;
//	   this.resultDistributer	= previousSearch.resultDistributer;
//   }
   

	/**
	 * Check the validity of this seed.
	 */
   public boolean validate()
   {
	   if (engine == null)
	   {
		   error("Can't process search seed with null engine.");
		   return false;
	   }
	   if (((query == null) || (query.length()<=0) ) && !DELICIOUS.equals(engine)) // delicious handles empty queries nicely
	   {
		   try
		   {
			   error("Can't process search seed with null query: " + this.translateToXML());
		   } catch (XMLTranslationException e)
		   {
			   e.printStackTrace();
		   }
		   return false;
	   }
	   
	   return true;
   }
   
	/**
	 * Bring this seed into the agent or directly into the composition.
	 * 
	 * @param objectRegistry		Context passed between services calls.
	 * @param infoCollector TODO
	 */
   @Override
   public void performInternalSeedingSteps(InfoCollector infoCollector)
   {
  	 infoCollector.instantiateDocumentType(SEARCH_DOCUMENT_TYPE_REGISTRY, engine, this);
   }

 	/**
 	 * Create an entry for the DocumentType class in the registry, for dynamic dispatch.
 	 * 
 	 * @param engineName
 	 * @param documentTypeClass
 	 */
 	static public void registerEngine(String engineName, Class<?> documentTypeClass)
 	{
	  if (engineName != null)
		  SEARCH_DOCUMENT_TYPE_REGISTRY.put(engineName, documentTypeClass);
 	}

	/**
	 * Do stuff in the DocumentType constructor to setup this search result Container.
	 * For Search, the extra stuff we do involves setting up the TermVector to match the query,
	 * and squirting some initial interest into these Terms.
	 * 
	 * @param container
	 */
	public void bindToContainer(Container container)
	{
		super.bindToContainer(container);
		//container.metadata.initializeFromSearch(query);
		if(query != null)
		{
			String queryValue			= query.replace('+', ' ');
			//Change either MetdataTypeNames.java or create a new names file.
			container.hwSetMetadataField(QUERY, queryValue);
		}
//		container.setMetadataField("query", queryValue);
//		queryField.incrementParticipantInterest(initialIntensity);
//		container.incrementTermVectorInterest((short) initialIntensity);
		
		//container.setIntensity(this.initialIntensity);
	}
	
	/**
	 * The String the dashboard needs to show.
	 * 
	 * @return	The search query.
	 */
	public String valueString()
	{
		return query;
	}
	/**
	 * The category the dashboard uses to show.
	 * 
	 * @return	The search category.
	 */
	public String categoryString()
	{
		return SEARCH;
	}
	
	/**
	 * The category the dashboard uses to show.
	 * 
	 * @return	The search engine category.
	 */
	public String detailedCategoryString()
	{
		return engine.toString();
	}
	
	/**
	 * @return Returns the initialIntensity.
	 */
	public short initialIntensity()
	{
		return initialIntensity;
	}
	
	/**
	 * The requested number of results to retrieve.
	 * 
	 * @return Returns the numResults.
	 */
	public int numResults()
	{
		return numResults;
	}
	
	/**
	 * If multiple searches were specified within the {@link ecologylab.semantics.seeding.SeedSet SeedSet}, this field specifies
	 * the index of this one.
	 * 
	 * @return Returns the searchNum.
	 */
	public int searchNum()
	{
		return searchNum;
	}
	
	public int searchType()
	{
		return searchType;
	}
	
	protected boolean noAggregator()
	{
		return this.noAggregator || (seedSet() == null) || imageSearchResults();
	}
	
	public boolean imageSearchResults()
	{
		return FLICKR.equals(engine) || YAHOO_IMAGE.equals(engine); // || DELICIOUS.equals(engine);
	}
	/**
	 * @return Returns the siteString.
	 */
	public String siteString()
	{
		return site;
	}
	/**
	 * @param query The query to set.
	 */
	public boolean setValue(String query)
	{
		this.query = query;
		return true;
	}
	
	/**
	 * @param engineTypeString
	 */
	public void setCategory(String engineTypeString)
	{
		this.engine = engineTypeString;
	}
	/**
	 * @return Returns the firstResult.
	 */
	public int firstResult()
	{
		return firstResult;
	}
	/**
	 * @param firstResult The firstResult to set.
	 */
	public void setFirstResult(int firstResult)
	{
		this.firstResult = firstResult;
	}
	/**
	 * @param queueInsteadOfImmediate The queueInsteadOfImmediate to set.
	 */
	public void setQueueInsteadOfImmediate(boolean queueInsteadOfImmediate)
	{
		this.queueInsteadOfImmediate = queueInsteadOfImmediate;
	}
	/**
	 * @return the creator
	 */
	public String creator()
	{
		return creator;
	}
	
	public boolean generatingTermDictionary()
	{
		return generatingTermDictionary;
	}
	
	
	/**
	 * @return true if specified that the user wants to see the most popular tagged 
	 * stuff in del.icio.us
	 */
	public boolean popular()
	{
		return false;
	}

	/**
	 * Set the seed to specify that  the user wants to see the most popular tagged 
	 * stuff in del.icio.us
	 * 
	 * @param popular the popular to set
	 */
	public void setPopular(boolean popular)
	{
		
	}
	
	public void setSearchNum(int searchNum)
	{
		this.searchNum	= searchNum;
	}
	public String getQuery() 
	{
		return query;
	}
	/**
	 * Set query and queryNoPluses.
	 * 
	 * @param query
	 * @return	Query with any plus characters removed.
	 */
	public String setQuery(String query)
	{
		this.query = query;
		String result	= null;
		if (query != null)
		{
			result			= query.replace('+', ' ');
			queryNoPluses	= result;
		}
		return result;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	
	/**
	 * Pass back the array of choices, when an engine takes only a fixed set of them.
	 * 
	 * @return	null in the default implemenation, indicating vocabulary is not controlled.
	 */
	public String[] controlledVocabulary()
	{
		return null;
	}
	/**
	 * For Yahoo Buzz. Queries to buzz can either be "leaders" (the default) or "movers".
	 * The latter tend to be more current and interesting.
	 */
	public boolean isMovers()
	{
		return isMovers;
	}
	/**
	 * For Yahoo Buzz. Queries to buzz can either be "leaders" (the default) or "movers".
	 * The latter tend to be more current and interesting.
	 */
	public void setIsMovers(boolean isMovers)
	{
		this.isMovers = isMovers;
	}

	
	public boolean canChangeVisibility()
	{
		return true;
	}
	public boolean isDeletable()
	{
		return true;
	}
	public boolean isEditable()
	{
		return true;
	}
	public boolean isRejectable()
	{
		return false;
	}
	
	public void setDcQuery(Dc dcQuery)
	{
		this.dc = dcQuery;
	}
	
	public Dc getDcQuery()
	{
		return dc;
	}

	static final File	testFile	= new File("config/preferences/katrinaLocationAware.xml");
	
//	public static void main(String[] a)
//	{
//		try
//		{
//			PrefSet searchState		= (PrefSet) ElementState.translateFromXML(testFile, CFServicesTranslations.get());
//			searchState.translateToXML(System.out);
//		} catch (XMLTranslationException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
	public int numResultsFrom() 
	{
		// TODO Auto-generated method stub
		return numResultsFrom;
	}
	
	public void incrementNumResultsBy(int i)
	{
		numResultsFrom += i;
	}


  /**
   * Processing peformed for each Seed in a SeedSet in a loop before performSeeding() will be called in a separate loop.
   * 
   * @return	true if this is not an image search, and we are processing it.
   */
	@Override
	public boolean bookkeeping(SeedSet seedSet, int searchNum)
	{
		boolean result = !imageSearchResults();
		if (result)
		{
			setSearchNum(searchNum);
			setSeedSet(seedSet);
		}
		return result;
	}
}