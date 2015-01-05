package ecologylab.bigsemantics.seeding;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.namesandnums.CFPrefNames;
import ecologylab.collections.Scope;
import ecologylab.generic.ReflectionTools;
import ecologylab.serialization.ElementStateOrmBase;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Specification of a directive to the agent or otherwise to composition space services.
 * 
 * @author andruid, robinson
 */
@simpl_inherit
abstract public class Seed extends ElementStateOrmBase implements CFPrefNames
{
    public static final String          TRAVERSABLE                  = "traversable";
    public static final String          UNTRAVERSABLE                = "untraversable";
    public static final String    	    REJECT                       = "reject";

    /**
     * When set, indicates that the seed should be processed without using a
     * {@link SeedDistributor ResultDistributer}. This is done, for example in synthesizeSearch().
     * Whenever a Seed is constructed via S.IM.PL Serialization, noAggregator is false.
     */
    protected boolean                     noAggregator;

    protected boolean                     queueInsteadOfImmediate;
    /**
     * Query string to pass to the search engine.
     */
    @simpl_scalar protected String				query;
    
    @simpl_scalar protected float					bias		= 1.0f;

    protected SeedDistributor            	seedDistributer;
    
    protected		SemanticsGlobalScope					infoCollector;
    
    protected 	SeedPeer									seedPeer;
    
    private			boolean										active	= true;
    
    Document	document;

    
    
    public SeedPeer getSeedPeer()
		{
			return seedPeer;
		}

    /**
     * Blank constructor used by automatic ecologylab.serialization instantiations.
     */
		public Seed()
    {}
    
    /**
     * Called right after constructor.
     * 
     * @param infoProcessor
     */
    public void initialize(SemanticsGlobalScope infoProcessor)
    {
    		this.infoCollector	= infoProcessor;
    		
    		seedPeer						= infoProcessor.getSeeding().constructSeedPeer(this);
    }

    /**
     * Called right after constructor.
     * 
     * @param infoProcessor
     */
    public void initialize(SeedPeer ancestor, SemanticsSessionScope infoProcessor)
    {
    		initialize(infoProcessor);
    		
    		ancestor.addKid(seedPeer);
    }

	protected Seed(String name, String action)
	{
		this();
		weird("Instantiated Seed)"+name+","+action+") but this should never be called directly");
	}

    protected Seed(float bias)
    {
      this.bias = bias;
    }

    protected Seed(Seed ancestor, float bias)
    {
    	this(bias);
    	
//FIXME        this.parent = ancestor;
    }

    abstract public void performInternalSeedingSteps(SemanticsGlobalScope infoCollector);
    
    /**
     * This sets the infoCollector for the seed, and then hands off the internal seeding processing to the
     * individual seed type.
     * 
     * @param	infoCollector	Must be non-null!
     */
    public final void performSeedingSteps(SemanticsGlobalScope infoCollector)
    {
    	// desparately seeking non-null InfoCollector
    	if (infoCollector != null)
    		setInfoCollector(infoCollector);
    	else if (this.infoCollector != null)
    		infoCollector	= this.infoCollector;
    	if (infoCollector == null)
    		throw new RuntimeException("Null InfoCollector passed to performSeedingSteps() in " + this);

    	seedPeer	= infoCollector.getSeeding().constructSeedPeer(this);
    	performInternalSeedingSteps(infoCollector);
    	
    	// TODO there is an ancestor for this seed now, so we will have also ADD THIS (ie.. this
        // seed) to the ancestor
        // so that there is a link in both directions. The ancestor seed will have a ArrayList
        // containing this info.
        // if the ArrayList is empty, then the it's not a parent to kids. If it's not empty, then it
        // is a parent...
    	//FIXME! andruid & abhinav
    	
        if (seedPeer != null)
        	seedPeer.addThisToParent();
    }
    
  	/**
  	 * Called to specify that the next set of search results will be retrieved.
  	 * This is relevant for search seeds, but for other seeds it does nothing.
  	 */
  	public int nextResultSet()
  	{
  		return 0;
  	}

    
    abstract public String valueString();
    
    abstract public boolean setValue(String value);

    public void setInfoCollector(SemanticsGlobalScope infoCollector)
    {
        this.infoCollector = infoCollector;
    }

    protected boolean useDistributor()
    {
    	return false;
    }
    
    /**
     * A hack for search seeds. Base class implementation is a no-op.
     */
    public void setupNumResults()
    {}
 
    /**
     * @return Returns the bias.
     */
    public float bias()
    {
        return bias;
    }

   
    /**
     * @return Returns the queueInsteadOfImmediate.
     */
    public boolean queueInsteadOfImmediate()
    {
        return queueInsteadOfImmediate;
    }

    private SeedSet seedSet = null;

    public SeedSet seedSet()
    {
        return (this.seedSet != null) ? this.seedSet : (SeedSet) parent();
    }

    public void setSeedSet(SeedSet set)
    {
        this.seedSet = set;
    }

    protected boolean noAggregator()
    {
        return this.noAggregator;
    }

    /**
     * @return Returns the ResultDistributer.
     */
    public SeedDistributor seedDistributer(SemanticsGlobalScope infoCollector)
    {
    	if (seedDistributer != null)
    		return seedDistributer;

    	if (noAggregator())
    		return null;

    	SeedSet seedSet 						= seedSet();
    	SeedDistributor result	= null;
    	if (seedSet != null)
    	{
    		result										= seedSet.seedDistributer(infoCollector);
    		this.seedDistributer		= result;
    	}
    	return result;
    }

    public void setResultDistributer(SeedDistributor resultDistributer)
    {
        this.seedDistributer = resultDistributer;
    }

    abstract 	public boolean isEditable();
    
    static final Class<?>[] CONSTRUCTOR_ARG_TYPES	=
    {
    	String.class, String.class	
    };
	public Seed newInstanceOf(String query, String type)
	{
		String[] args	= new String[2];
		args[0]			= query;
		args[1]			= type;
		return ReflectionTools.getInstance(this.getClass(), CONSTRUCTOR_ARG_TYPES, args);
	}
	
  /**
   * Do stuff in the DocumentType constructor to setup this Container.
   * 
   * @param container
   */
  public void bindToContainer(DocumentClosure container)
  {
  	assert(container != null);
//      {
//      	System.out.println("See Why I am Null here");
//      }
//      else
  	bindToDocument(container.getDocument());
  }
  /**
   * Do stuff in the DocumentType constructor to setup this Container.
   * 
   * @param container
   */
  public void bindToDocument(Document document)
  {
  	assert(document != null);
  	document.setAsTrueSeed(this);
  }
  public boolean isRejectable()
  {
  	return true;
  }
  
  public void notifyInterface(Scope scope, String key)
  {
  	if (seedPeer != null)
  		seedPeer.notifyInterface(scope, key);
  }
  
  /**
   * A hack for search seeds. Base class implementation is a no-op.
   */
  public void fixNumResults()
  {}

  /**
   * Processing peformed for each Seed in a SeedSet in a loop before performSeeding() will be called in a separate loop.
   */
  public boolean initializeSeedingSteps(SeedSet seedSet, int searchNum)
  {
  		return false;
  }
  
  /**
   * Use the SeedDistributor to queue/parse this container if appropriate, or just queue it directly.
   * 
   * @param container
   */
	public void queueSeedOrRegularContainer(DocumentClosure container)
	{
		SeedDistributor seedDistributer = seedDistributer(infoCollector);
		if (seedDistributer != null)
		{
			seedDistributer.queueSearchRequest(container);
		}
		else
			container.queueDownload();
	}

	/**
	 * @return the inActive
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * Called after a seed is parsed to prevent it being parsed again later during re-seeding.
	 * 
	 * @param inActive the inActive to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	public String getQuery() 
	{
		return query;
	}
	
	/**
	 * @return Returns the bias.
	 */
	public float getBias()
	{
		return bias;
	}

	/**
	 * @param bias The bias to set.
	 */
	public void setBias(float bias)
	{
		this.bias = bias;
	}

	public boolean isHomogenousSeed()
	{
	
		return false;
	}
	
	public boolean isJustCrawl()
	{
		return false;
	}

	public boolean isJustMedia()
	{
		return false;
	}

}