/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Entity;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.ElementState;

/**
 * This is the abstract class which defines the semantic action. All the semantic actions must
 * extend it. To add a new semantic action following steps needed to be taken. 
 * <li> 1) Create a class for  that semantic action which extends SemanticAction class.
 * <li> 2) Write all the custom code for that
 * semantic action in this new class file. [Example see <code>ForEachSemanticAction.java</code> or
 * <code>IfSemanticAction</code> which implements for_each semantic action.] 
 * <li> 3) Modify the <code>handleSemanticAction</code> method of 
 * <code>SemanticActionHandle.java</code> to add case
 * for new semantic action. 
 * <li> 4) Add a new method in <code>SemanticActionHandler.java </code> to
 * handle this action. Mostly this method should be abstract unless the action is a flow control
 * action like FOR LOOP. 
 * <li> 5) For code clarity and readability define a constant for the new action
 * name in <code>SemanticActionStandardMethods.java</code>
 * 
 * @author amathur
 * 
 */

public abstract class SemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends ElementState
		implements SemanticActionNamedArguments
{

	@simpl_collection
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	@simpl_nowrap
	private ArrayList<Condition>			checks;

	/**
	 * The map of arguments for this semantic action.
	 */
	@simpl_nowrap
	@simpl_map("arg")
	private HashMap<String, Argument>	args;

	/**
	 * Object on which the Action is to be taken
	 */
	@simpl_scalar
	@xml_tag("object")
	private String										objectStr;

	/**
	 * The value returned from the action
	 */
	@simpl_scalar
	private String										name;

	/**
	 * Error string for this action
	 */
	@simpl_scalar
	private String										error;

	protected IC											infoCollector;

	protected SAH											semanticActionHandler;

	protected DocumentParser					documentParser;

	public SemanticAction()
	{
		args = new HashMap<String, Argument>();
	}

	public ArrayList<Condition> getChecks()
	{
		return checks;
	}

	public String getObject()
	{
		return objectStr;
	}

	public void setObject(String object)
	{
		this.objectStr = object;
	}

	public String getReturnObjectName()
	{
		return name;
	}

	public boolean hasArguments()
	{
		return args != null && args.size() > 0;
	}

	public String getArgumentValueName(String argName)
	{
		String result = null;
		if (args != null)
		{
			Argument argument = args.get(argName);
			if (argument != null) 
			{
				result	= argument.getValue();
			}
		}
		return result;
	}

	public String getArgumentAltValueName(String argName)
	{
		String result = null;
		if (args != null)
		{
			Argument argument = args.get(argName);
			if (argument != null) 
			{
				result	= argument.getAltValue();
			}
		}
		return result;
	}

	public Object getArgumentObject(String argName)
	{
		Object result			= null;
		if (args != null)
		{
			Argument argument = args.get(argName);
			if (argument != null)
			{
				String argumentValueName 					= argument.getValue();
				if (argumentValueName != null)
				{
					Scope semanticActionVariableMap = semanticActionHandler.getSemanticActionVariableMap();
					result = semanticActionVariableMap.get(argumentValueName);				
					if (result == null)
					{
						argumentValueName 							= argument.getAltValue();
						if (argumentValueName != null)
						{
							result = semanticActionVariableMap.get(argumentValueName);				
						}					
					}
				}
			}
		}
		return result;
	}

	public int getArgumentInteger(String argName, int defaultValue)
	{
		Integer value = (Integer) getArgumentObject(argName);
		return (value != null) ? value : defaultValue;
	}

	public boolean getArgumentBoolean(String argName, boolean defaultValue)
	{
		Boolean value = (Boolean) getArgumentObject(argName);
		return (value != null) ? value : defaultValue;
	}

	public float getArgumentFloat(String argName, float defaultValue)
	{
		Float value = (Float) getArgumentObject(argName);
		return (value != null) ? value : defaultValue;
	}

	public final String getError()
	{
		return error;
	}

	public final void setError(String error)
	{
		this.error = error;
	}

	public void setInfoCollector(IC infoCollector)
	{
		this.infoCollector = infoCollector;
	}

	public void setSemanticActionHandler(SAH handler)
	{
		this.semanticActionHandler = handler;
	}

	public void setDocumentParser(DocumentParser documentParser)
	{
		this.documentParser = documentParser;
	}

	/**
	 * return the name of the action.
	 * 
	 * @return
	 */
	public abstract String getActionName();

	/**
	 * handle error during action performing.
	 */
	public abstract void handleError();

	/**
	 * Perform this semantic action. User defined semantic actions should override this method.
	 * 
	 * @param obj
	 *          The object the action operates on.
	 * @return The result of this semantic action (if any), or null.
	 */
	public abstract Object perform(Object obj);

	/**
	 * Register a user defined semantic action to the system. This method should be called before
	 * compiling or using the MetaMetadata repository.
	 * <p />
	 * To override an existing semantic action, subclass your own semantic action class, use the same
	 * tag (indicated in @xml_tag), and override perform().
	 * 
	 * @param semanticActionClass
	 * @param canBeNested
	 *          indicates if this semantic action can be nested by other semantic actions, like
	 *          <code>for</code> or <code>if</code>. if so, it will also be registered to
	 *          NestedSemanticActionTranslationScope.
	 */
	public static void register(Class<? extends SemanticAction>... semanticActionClasses)
	{
		for (Class<? extends SemanticAction> semanticActionClass : semanticActionClasses)
		{
			SemanticActionTranslationScope.get().addTranslation(semanticActionClass);
		}
	}
	
	protected MetaMetadata getMetaMetadata()
	{
		return getMetaMetadata(this);
	}
	
	static public MetaMetadata getMetaMetadata(ElementState that)
	{
		if (that instanceof MetaMetadata)
		{
			return (MetaMetadata) that;
		}
		ElementState parent	= that.parent();
		
		return (parent == null) ? null : getMetaMetadata(parent);
	}


	
	public Container createContainer(DocumentParser documentParser, LinkType linkType)
	{
		Container outlinkContainer = null;
		// get the ancestor container
		Container sourceContainer = documentParser.getContainer();

		// get the seed. Non null only for search types .
		Seed seed = documentParser.getSeed();
		ParsedURL outlinkPurl = null;
		Document metadata = (Document) getArgumentObject(DOCUMENT);
		Metadata mixin		= (Metadata) getArgumentObject(MIXIN);
		outlinkPurl = (ParsedURL) getArgumentObject(CONTAINER_LINK);
		
		
		if (metadata == null)
		{
//			purl = (ParsedURL) getArgumentObject(CONTAINER_LINK);
			if (outlinkPurl == null)
			{
				Entity entity = (Entity) getArgumentObject(ENTITY);
				if (entity != null)
				{
					outlinkPurl = entity.key();
					metadata = (Document) entity.getLinkedDocument();
				}
			}
		}
		if (outlinkPurl == null && metadata != null)
			outlinkPurl = metadata.getLocation();

		// FIXME check for some weird case when entity has nothing.
		if (outlinkPurl != null)
		{
			boolean containerIsOld;
			synchronized (infoCollector.globalCollectionContainersLock())
			{
				containerIsOld = infoCollector.aContainerExistsFor(outlinkPurl);
				if (seed == null)
					outlinkContainer = infoCollector.getContainer(sourceContainer, metadata, null, outlinkPurl, false, false, false);
				else
				{
					// Avoid recycling containers.
					// We trust GlobalCollections has had a good reason to discard the container.
					outlinkContainer 	= infoCollector.getContainerForSearch(sourceContainer, metadata, outlinkPurl, seed, true);
				}
				if (outlinkContainer != null)
					metadata				= (Document) outlinkContainer.getMetadata();
				if (mixin != null && metadata != null)
					metadata.addMixin(mixin);
			}
			
			String anchorText = (String) getArgumentObject(ANCHOR_TEXT);
			// create anchor text from Document title if there is none passed in directly, and we won't
			// be setting metadata
			if (containerIsOld && anchorText == null && metadata != null)
				anchorText = metadata.getTitle();

			String anchorContextString 		= (String) getArgumentObject(ANCHOR_CONTEXT);
			boolean citationSignificance	= getArgumentBoolean(CITATION_SIGNIFICANCE, false);
			float significanceVal 				= getArgumentFloat(SIGNIFICANCE_VALUE, 1);
			boolean traversable 					= getArgumentBoolean(TRAVERSABLE, true);
			boolean ignoreContextForTv 		= getArgumentBoolean(IGNORE_CONTEXT_FOR_TV, false);
			
			if (traversable)
				infoCollector.traversable(outlinkPurl);

			if (outlinkContainer != null)
			{
				boolean anchorIsInSource = false;
				if (sourceContainer != null)
				{
					// Chain the significance from the ancestor
					SemanticInLinks sourceInLinks = sourceContainer.semanticInLinks();
					if (sourceInLinks != null)
					{
						significanceVal *= sourceInLinks.meanSignificance();
						anchorIsInSource = sourceInLinks.containsKey(outlinkPurl);
					}
				}
				if(! anchorIsInSource)
				{
					//By default use the boost, unless explicitly stated in this site's MMD
					boolean useSemanticBoost = !outlinkContainer.site().ignoreSemanticBoost();
					SemanticAnchor semanticAnchor = new SemanticAnchor(outlinkPurl, null, citationSignificance,
							significanceVal, documentParser.purl(), false, useSemanticBoost, linkType);// this is not fromContentBody,
																							// but is fromSemanticActions
					if(ignoreContextForTv)
						semanticAnchor.addAnchorContextToTV(anchorText, null);
					else
						semanticAnchor.addAnchorContextToTV(anchorText, anchorContextString);
					outlinkContainer.addSemanticInLink(semanticAnchor, sourceContainer);
				}
				else
				{
					debug("Ignoring inlink, because ancestor contains the same, we don't want cycles in the graph just yet: sourceContainer -- outlinkPurl :: " + sourceContainer + " -- " + outlinkPurl);
				}
			}
			// adding the return value to map
			Scope semanticActionVariableMap = semanticActionHandler.getSemanticActionVariableMap();
			if(semanticActionVariableMap != null)
				semanticActionVariableMap.put(getReturnObjectName(), outlinkContainer);
			else
				error("semanticActionVariableMap is null !! Not frequently reproducible either. Place a breakpoint here to try fixing it next time.");
			// set flags if any
			// setFlagIfAny(action, localContainer);
		}
		// return the value. Right now no need for it. Later it might be used.
		return outlinkContainer;
	}

	/**
	 * Lookup the container in the named arguments map, and return it if its there. Otherwise, use
	 * location and metadata arguments from that map to create the container.
	 * @param documentParser
	 * @param linkType Changes to significanceVal based on LinkType
	 * 
	 * @return
	 */
	public Container getOrCreateContainer(DocumentParser documentParser, LinkType linkType)
	{
		Container result	= (Container) getArgumentObject(CONTAINER);
		if (result == null)
		{
			result					= createContainer(documentParser, linkType);
			if (result == null)
				result				= documentParser.getContainer();
		}
		return result;
	}

	static final int INIT = 0; // this action has not been started
	static final int INTER = 10; // this action has been started but not yet finished
	static final int FIN = 20; // this action has already been finished
	
	void setNestedActionState(String name, Object value)
	{
		
	}
	
}
