/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.Check;
import ecologylab.semantics.metametadata.DefVar;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.xml.ElementState;

/**
 * This is the abstract class which defines the semantic action. All the semantic actions must
 * extend it. To add a new semantic action following steps needed to be taken. 1) Create a class for
 * that semantic action which extends SemanticAction class. 2) Write all the custom code for that
 * semantic action in this new class file. [Example see <code>ForEachSemanticAction.java</code> 
 * or<code>IfSemanticAction</code>
 * which implements for_each semantic action.] 3) Modify the <code>handleSemanticAction</code>
 * method of <code>SemanticActionHandle.java</code> to add case for new semantic action. 4) Add a
 * new method in <code>SemanticActionHandler.java </code> to handle this action. Mostly this method
 * should be abstract unless the action is a flow control action like FOR LOOP. 5) For code clarity
 * and readability define a constant for the new action name in
 * <code>SemanticActionStandardMethods.java</code>
 * 
 * @author amathur
 * 
 */

public abstract class SemanticAction extends ElementState
{

	/**
	 * Checks if any for this action. Any action can have 0 to any number of checks
	 */
	@simpl_nowrap 
	@simpl_collection("check")
	private ArrayList<Check>					checks;
	
	/**
	 * The map of arguments for this semantic action.
	 */
	@simpl_nowrap 
	@simpl_map("arg")
	private HashMap<String, Argument>	args;

	/**
	 * List of variables which can be used inside this action
	 */
	@simpl_nowrap 
	@simpl_collection("def_var")
	private ArrayList<DefVar> 				defVars;
	

	/**
	 * Object on which the Action is to be taken
	 */
	@simpl_scalar
	private String										object;

	/**
	 * Return type of the semantic Action
	 */
	@simpl_scalar
	private String										returnType;

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

	private InfoCollector							infoCollector;

	public SemanticAction()
	{
		args = new HashMap<String, Argument>();
	}
	
	/**
	 * returns the name of the action.
	 * 
	 * @return
	 */
	public abstract String getActionName();

	/**
	 * Handles the error for the action
	 */
	public abstract void handleError();

	/**
	 * @return the checks
	 */
	public ArrayList<Check> getChecks()
	{
		return checks;
	}

	
	/**
	 * @return the object
	 */
	public String getObject()
	{
		return object;
	}

	/**
	 * @return the returnType
	 */
	public String getReturnType()
	{
		return returnType;
	}

	/**
	 * @return the returnValue
	 */
	public String getReturnValue()
	{
		return name;
	}

	public Argument getArgument(String name)
	{
		return (args == null) ? null : args.get(name);
	}
	public boolean hasArguments()
	{
		return args != null && args.size() > 0;
	}
	
	public Map<String, Argument> getArgs()
	{
		return (args == null) ? null : args;
	}
	
	/**
	 * @param object
	 *          the object to set
	 */
	public void setObject(String object)
	{
		this.object = object;
	}

	/**
	 * @param returnType
	 *          the returnType to set
	 */
	public void setReturnType(String returnType)
	{
		this.returnType = returnType;
	}

	/**
	 * @param returnValue
	 *          the returnValue to set
	 */
	public void setReturnValue(String returnValue)
	{
		this.name = returnValue;
	}

	/**
	 * @return the error
	 */
	public final String getError()
	{
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public final void setError(String error)
	{
		this.error = error;
	}


	/**
	 * @return the defVars
	 */
	public ArrayList<DefVar> getDefVars()
	{
		return defVars;
	}
	
	/**
	 * Handle this semantic action. User defined semantic actions should override this method.
	 * 
	 * @param obj The object the action operates on.
	 * @param args The arguments passed to the action, in the form of name-object pair.
	 * @return The result of this semantic action (if any), or null.
	 */
	public Object handle(Object obj, Map<String, Object> args)
	{
		return null;
	}

	/**
	 * Register a user defined semantic action to the system, so that the reading/writing of
	 * MetaMetadata repository works properly.
	 * <p>
	 * We don't distinguish nested / non-nested semantic actions here.
	 * <p>
	 * This method should be called before compiling or loading the MetaMetadata repository, if user
	 * defined semantic actions are used.
	 *  
	 * @param semanticActionClasses Classes of user defined semantic actions.
	 * @see {@link NestedSemanticAction}, {@link NestedSemanticActionTranslationScope}
	 */
	public static void register(Class<? extends SemanticAction>... semanticActionClasses)
	{
		for (Class<? extends SemanticAction> SAClass : semanticActionClasses)
		{
			MetaMetadataTranslationScope.get().addTranslation(SAClass);
			NestedSemanticActionsTranslationScope.get().addTranslation(SAClass);
		}
	}
	
	public InfoCollector getInfoCollector()
	{
		return infoCollector;
	}
	
	public void setInfoCollector(InfoCollector infoCollector)
	{
		this.infoCollector = infoCollector;
	}
}
