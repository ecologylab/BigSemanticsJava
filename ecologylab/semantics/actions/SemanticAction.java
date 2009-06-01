/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.Check;
import ecologylab.semantics.metametadata.FlagCheck;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.ArrayListState;

/**
 * This is the abstract class which defines the semantic action. All the semantic actions must
 * extend it.
 * To add a new semantic action following steps needed to be taken.
 * 1) Create a class for that semantic action which extends SemanticAction class.
 * 2) Write all the custom code for that semantic action in this new class file. [Example see <code>ForEachSemanticAction.java</code>
 *    which implements for_each semantic action.]
 * 3) Modify the <code>handleSemanticAction</code> method of <code>SemanticActionHandle.java</code> to add case for new semantic action.
 * 4) Add  a new method in <code>SemanticActionHandler.java </code> to handle this action. Mostly this method should be abstract unless
 *    the action is a flow control action like FOR LOOP.
 * 5) For code clarity and readability define a constant for the new action name in <code>SemanticActionStandardMethods.java</code>
 * @author amathur
 *
 */


public abstract class SemanticAction<SA extends SemanticAction> extends ElementState
{

	/**
	 * List of nested semantic actions.
	 */
	@xml_collection()
	@xml_classes(
	{ CreateAndVisualizeImgSurrogateSemanticAction.class,
			CreateContainerForSearchSemanticAction.class, CreateContainerSemanticAction.class,
			ForEachSemanticAction.class, GeneralSemanticAction.class,
			ProcessDocumentSemanticAction.class, ProcessSearchSemanticAction.class,
			SetMetadataSemanticAction.class, SetterSemanticAction.class,
			CreateSearchSemanticAction.class, GetFieldSemanticAction.class, })
	private ArrayList<SA>							nestedSemanticActionList;

	/**
	 * Checks if any for this action. Any action can have 0 to any number of checks
	 */
	@xml_tag("checks")
	@xml_collection("checks")
	private ArrayListState<Check>			checks;

	/**
	 * Flags to check before performing this action.
	 */
	@xml_tag("if")
	@xml_collection("if")
	private ArrayListState<FlagCheck>	flagChecks;

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
	 * The list of arguments for this semantic action.
	 */
	@xml_tag("args")
	@xml_collection("args")
	private ArrayListState<Argument>	args;

	public SemanticAction()
	{

	}

	/**
	 * @return the nestedSemanticActionList
	 */
	public ArrayList<SA> getNestedSemanticActionList()
	{
		return nestedSemanticActionList;
	}

	/**
	 * returns the name of the action.
	 * 
	 * @return
	 */
	public abstract String getActionName();

	/**
	 * @return the checks
	 */
	public ArrayListState<Check> getChecks()
	{
		return checks;
	}

	/**
	 * @return the flagChecks
	 */
	public ArrayListState<FlagCheck> getFlagChecks()
	{
		return flagChecks;
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

	/**
	 * @return the arguments
	 */
	public ArrayListState<Argument> getArguments()
	{
		return args;
	}

	/**
	 * @param nestedSemanticActionList
	 *          the nestedSemanticActionList to set
	 */
	public void setNestedSemanticActionList(ArrayList<SA> nestedSemanticActionList)
	{
		this.nestedSemanticActionList = nestedSemanticActionList;
	}

	/**
	 * @param checks
	 *          the checks to set
	 */
	public void setChecks(ArrayListState<Check> checks)
	{
		this.checks = checks;
	}

	/**
	 * @param flagChecks
	 *          the flagChecks to set
	 */
	public void setFlagChecks(ArrayListState<FlagCheck> flagChecks)
	{
		this.flagChecks = flagChecks;
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
	 * @param arguments
	 *          the arguments to set
	 */
	public void setArguments(ArrayListState<Argument> arguments)
	{
		this.args = arguments;
	}

}
