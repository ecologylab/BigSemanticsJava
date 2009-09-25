package ecologylab.semantics.seeding;

import ecologylab.collections.Scope;
import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.connectors.CFPrefNames;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.SeedPeer;

/**
 * Specification of a directive to the agent or otherwise to composition space services.
 * 
 * @author andruid, robinson
 */
abstract public class Seed<AC extends Container> extends ecologylab.services.messages.cf.Seed implements CFPrefNames
{
    public static final String          TRAVERSABLE                  = "traversable";
    public static final String          UNTRAVERSABLE                = "untraversable";
    public static final String    	      REJECT                       = "reject";

    /**
     * When set, indicates that the seed should be processed without using a
     * {@link ResultDistributer ResultDistributer}.
     */
    protected boolean                      noAggregator;

    protected boolean                      queueInsteadOfImmediate;


    protected ResultDistributer            resultDistributer;
    
    protected		InfoCollector							infoCollector;
    
    protected 	SeedPeer									seedPeer;

    public SeedPeer getSeedPeer()
		{
			return seedPeer;
		}

    /**
     * Blank constructor used by automatic ecologylab.xml instantiations.
     */
		public Seed()
    {}
    
    /**
     * Called right after constructor.
     * 
     * @param infoProcessor
     */
    protected void initialize(InfoCollector infoProcessor)
    {
    		this.infoCollector	= infoProcessor;
    		
    		seedPeer						= infoProcessor.constructSeedPeer(this);
    }

    /**
     * Called right after constructor.
     * 
     * @param infoProcessor
     */
    public void initialize(SeedPeer ancestor, InfoCollector infoProcessor)
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

    abstract public void performInternalSeedingSteps(InfoCollector infoCollector);
    
    /**
     * This sets the infoCollector for the seed, and then hands off the internal seeding processing to the
     * individual seed type.
     * 
     * @param	infoCollector	Must be non-null!
     */
    public final void performSeedingSteps(InfoCollector infoCollector)
    {
    	// desparately seeking non-null InfoCollector
    	if (infoCollector != null)
    		setInfoCollector(infoCollector);
    	else if (this.infoCollector != null)
    		infoCollector	= this.infoCollector;
    	if (infoCollector == null)
    		throw new RuntimeException("Null InfoCollector passed to performSeedingSteps() in " + this);

    	seedPeer	= infoCollector.constructSeedPeer(this);
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

    
    abstract public String categoryString();

    abstract public String detailedCategoryString();
    
    abstract public String valueString();
    
    abstract public boolean setValue(String value);

    public void setInfoCollector(InfoCollector infoCollector)
    {
        this.infoCollector = infoCollector;
    }
    
    public boolean validate()
    {
        return true;
    }


    /**
     * A hack for search seeds. Base class implementation is a no-op.
     */
    public void setupNumResults()
    {}

   
    /**
     * @param category
     *            The category (WEB_SITE, TRAVERSABLE, etc..) to be set for the seed. if you are
     *            setting the category for a SEARCH, then this value is what engine you want to use
     *            (GOOGLE, FLICKR, etc..)
     */
    public abstract void setCategory(String value);
 
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
    public <C extends Container> ResultDistributer<C> resultDistributer(InfoCollector infoCollector)
    {
        if (resultDistributer != null)
            return resultDistributer;

        if (noAggregator())
            return null;

        ResultDistributer<C> result = seedSet().resultDistributer(infoCollector);
        this.resultDistributer = result;
        return result;
    }

    public void setResultDistributer(ResultDistributer resultDistributer)
    {
        this.resultDistributer = resultDistributer;
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
  public void bindToContainer(Container container)
  {
      if(container == null)
      {
      	System.out.println("See Why I am Null here");
      }
  		container.setBias(bias);
      container.setAsTrueSeed(this);
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
  
	public void queueSearchrequest(AC container)
	{
		//if (this.searchSeed != null)
		{
			ResultDistributer resultDistributer = resultDistributer(infoCollector);
			if (resultDistributer != null)
			{
				resultDistributer.queueSearchRequest(container);
				// System.out.println("DEBUG::queued search request for\t"+container+"\tusing rd=\t"+resultDistributer);
				return;
			}
		}
		container.queueDownload();
		// System.out.println("DEBUG::queued container\t"+container+"\t for download");
	}
  
}