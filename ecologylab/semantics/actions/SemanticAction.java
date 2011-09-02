/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.Seeding;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.scalar.MetadataScalarBase;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;

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

@simpl_inherit
public abstract class SemanticAction extends ElementState implements SemanticActionNamedArguments
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

	protected SemanticsGlobalScope				sessionScope;

	protected SemanticActionHandler		semanticActionHandler;

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
				if (result != null && result instanceof MetadataScalarBase)
					result = ((MetadataScalarBase) result).getValue();
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

	public void setSessionScope(SemanticsGlobalScope infoCollector)
	{
		this.sessionScope = infoCollector;
	}

	public void setSemanticActionHandler(SemanticActionHandler handler)
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
	 * @throws IOException 
	 */
	public abstract Object perform(Object obj) throws IOException;

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

	/**
	 * @return
	 */
	protected Document resolveSourceDocument()
	{
		Document sourceDocument = (Document) getArgumentObject(SOURCE_DOCUMENT);
		if (sourceDocument == null)
			sourceDocument				= documentParser.getDocument();
		return sourceDocument;
	}
	
	public Document getOrCreateDocument(DocumentParser documentParser, LinkType linkType)
	{
		Document result = (Document) getArgumentObject(DOCUMENT);
		// get the ancestor container
		Document sourceDocument = resolveSourceDocument();

		// get the seed. Non null only for search types .
		Seed seed = documentParser.getSeed();					
		
		if (result == null)
		{
			ParsedURL outlinkPurl	= (ParsedURL) getArgumentObject(LOCATION);
			if (outlinkPurl != null)
				result							= sessionScope.getOrConstructDocument(outlinkPurl);
		}
	
		if (result == null)
			result	= sourceDocument;	//direct binding?!
		
		if (result != null && !result.isRecycled())
		{
			result.setSemanticsSessionScope(sessionScope);
			
			Metadata mixin				= (Metadata) getArgumentObject(MIXIN);
			if (mixin != null)
				result.addMixin(mixin);
			
			if (seed != null)
			{
				seed.bindToDocument(result);
			}

			String anchorText = (String) getArgumentObject(ANCHOR_TEXT);
			// create anchor text from Document title if there is none passed in directly, and we won't
			// be setting metadata
			if (anchorText == null)
				anchorText = result.getTitle();

			// work to avoid honey pots!
			String anchorContextString 		= (String) getArgumentObject(ANCHOR_CONTEXT);
			boolean citationSignificance	= getArgumentBoolean(CITATION_SIGNIFICANCE, false);
			float significanceVal 				= getArgumentFloat(SIGNIFICANCE_VALUE, 1);
			boolean traversable 					= getArgumentBoolean(TRAVERSABLE, true);
			boolean ignoreContextForTv 		= getArgumentBoolean(IGNORE_CONTEXT_FOR_TV, false);
			
			ParsedURL location						= result.getLocation();
			
			if (traversable)
			{
				Seeding seeding = sessionScope.getSeeding();
				if (seeding != null)
					seeding.traversable(location);
			}
			boolean anchorIsInSource = false;
			if (sourceDocument != null)
			{
				// Chain the significance from the ancestor
				SemanticInLinks sourceInLinks = sourceDocument.getSemanticInlinks();
				if (sourceInLinks != null)
				{
					significanceVal *= sourceInLinks.meanSignificance();
					anchorIsInSource = sourceInLinks.containsKey(location);
				}
			}
			if(! anchorIsInSource)
			{
				//By default use the boost, unless explicitly stated in this site's MMD
				SemanticsSite site = result.getSite();
				boolean useSemanticBoost = !site.ignoreSemanticBoost();
				if (citationSignificance)
					linkType	= LinkType.CITATION_SEMANTIC_ACTION;
				else if (useSemanticBoost && linkType == LinkType.OTHER_SEMANTIC_ACTION)
					linkType	= LinkType.SITE_BOOSTED_SEMANTIC_ACTION;
				SemanticAnchor semanticAnchor = new SemanticAnchor(linkType, location, null,
						sourceDocument.getLocation(), significanceVal);// this is not fromContentBody,
				// but is fromSemanticActions
				if(ignoreContextForTv)
					semanticAnchor.addAnchorContextToTV(anchorText, null);
				else
					semanticAnchor.addAnchorContextToTV(anchorText, anchorContextString);
				result.addSemanticInlink(semanticAnchor, sourceDocument);
			}
			else
			{
				debug("Ignoring inlink, because ancestor contains the same, we don't want cycles in the graph just yet: sourceContainer -- outlinkPurl :: " + sourceDocument + " -- " + location);
			}
		}
		// adding the return value to map
		Scope semanticActionVariableMap = semanticActionHandler.getSemanticActionVariableMap();
		if(semanticActionVariableMap != null)
			semanticActionVariableMap.put(getReturnObjectName(), result);
		else
			error("semanticActionVariableMap is null !! Not frequently reproducible either. Place a breakpoint here to try fixing it next time.");
		// set flags if any
		// setFlagIfAny(action, localContainer);

		// return the value. Right now no need for it. Later it might be used.
		return result;
	}

	/**
	 * Lookup the container in the named arguments map, and return it if its there. Otherwise, use
	 * location and metadata arguments from that map to create the container.
	 * @param documentParser
	 * @param linkType Changes to significanceVal based on LinkType
	 * 
	 * @return
	 */
//	public OldContainerI getOrCreateContainer(DocumentParser documentParser, LinkType linkType)
//	{
//		OldContainerI result	= (OldContainerI) getArgumentObject(CONTAINER);
//		if (result == null)
//		{
//			result					= getOrCreateDocument(documentParser, linkType);
//			if (result == null)
//				result				= documentParser.getContainer();
//		}
//		return result;
//	}

	static final int INIT = 0; // this action has not been started
	static final int INTER = 10; // this action has been started but not yet finished
	static final int FIN = 20; // this action has already been finished
	
	void setNestedActionState(String name, Object value)
	{
		
	}
	
	public SemanticActionHandler getSemanticActionHandler()
	{
		SemanticActionHandler result	= this.semanticActionHandler;
		if (result == null)
		{
			ElementState parentES = parent();
			if (parentES != null && parentES instanceof SemanticAction)
			{
				SemanticAction parent	= (SemanticAction) parentES;
				result	= parent.getSemanticActionHandler();
				this.semanticActionHandler	= result;
			}
		}
		return result;
	}
}
