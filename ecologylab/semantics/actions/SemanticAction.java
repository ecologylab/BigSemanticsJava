/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.MetaMetadata;
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

	public String getArgumentValue(String argName)
	{
		return (args != null && args.containsKey(argName)) ? args.get(argName).getValue() : null;
	}

	public Object getArgumentObject(String argName)
	{
		String objectName = getArgumentValue(argName);
		if (objectName == null)
			return null;
		return semanticActionHandler.getSemanticActionVariableMap().get(objectName);
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

}
