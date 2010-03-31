/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.Check;
import ecologylab.semantics.metametadata.DefVar;
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
	@xml_nowrap 
	@xml_collection("check")
	private ArrayList<Check>					checks;
	
	/**
	 * The map of arguments for this semantic action.
	 */
	@xml_nowrap 
	@xml_map("arg")
	private HashMap<String, Argument>	args;

	/**
	 * List of variables which can be used inside this action
	 */
	@xml_nowrap 
	@xml_collection("def_var")
	private ArrayList<DefVar> 				defVars;
	

	/**
	 * Object on which the Action is to be taken
	 */
	@xml_attribute
	private String										object;

	/**
	 * Return type of the semantic Action
	 */
	@xml_attribute
	private String										returnType;

	/**
	 * The value returned from the action
	 */
	@xml_attribute
	private String										name;

	/**
	 * Error string for this action
	 */
	@xml_attribute
	private String										error;


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
	
	public Object handle(Object obj, Map<String, Object> args)
	{
		return null;
	}

}
