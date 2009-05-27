/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.metametadata.Check;
import ecologylab.semantics.metametadata.IFclause;
import ecologylab.xml.types.element.ArrayListState;

/**
 * 
 * This is the handler for semantic actions. TODO Might want to implement lexical scoping in
 * variables.
 * 
 * @author amathur
 * 
 */
public abstract class SemanticActionHandler implements SemanticActionStandardMethods
{

	/**
	 * This is a map of return value and objects from semantic action. The key being the return_value
	 * of the semantic action.
	 * TODO remane this also to some thing like objectMap or variableMap.
	 */
	protected HashMap<String, Object>		semanticActionReturnValueMap;

	/**
	 * Map of various flags used and set during the semantic actions
	 */
	protected HashMap<String, Boolean>	semanticActionFlagMap;

	/**
	 * Stores the number of items in the collection for loops
	 */
	private int													numberOfCollection;

	
	/**
	 * 
	 * @param action
	 * @param paramter
	 */
	public abstract void createAndVisualizeImgSurrogate(SemanticAction action, SemanticActionParameters paramter);

	/**
	 * 
	 * @param action
	 * @param paramter
	 * @return
	 */
	public abstract Container createContainer(SemanticAction action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void processDocument(SemanticAction action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void setMetadata(SemanticAction action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @return
	 */
	public abstract Container createContainerForSearch(SemanticAction action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void handleGeneralAction(SemanticAction action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void setValueAction(SemanticAction action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void getValueAction(SemanticAction action, SemanticActionParameters parameter);
	
	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void processSearch(ProcessSearchSemanticAction action, SemanticActionParameters parameter);

	
	/**
	 * Implementation of for loop.
	 * 
	 * @param semanticAction
	 * @param parameter
	 */
	public void handleForLoop(ForEachSemanticAction action, SemanticActionParameters parameter)
	{
		// get all the action which have to be performed in loop
		ArrayList<? extends SemanticAction> nestedSemanticActions = action.getNestedSemanticActionList();
		
		// get the collection object name on which we have to loop
		String collectionObject = action.getCollection(); 
		
		//get the actual collection object
		ArrayList collection = (ArrayList)getObjectFromKeyName(collectionObject, parameter);
		
		int collectionSize = collection.size();
		// start the loop over each object
		for (int k = 0; k < collectionSize; k++)
		{
			// get the kth item
			Object item = collection.get(k);
			
			//put it in semantic action return value map
			semanticActionReturnValueMap.put(action.getAs(), item);
			
			// kth iteration of loop
			for (int j = 0; j < nestedSemanticActions.size(); j++)
			{
				// get the jth action inside the loop
				SemanticAction nestedSemanticAction = nestedSemanticActions.get(j);

				// perform the jth action
				handleSemanticAction(nestedSemanticAction, parameter);
			}
		}
	}

	/**
	 * Method which handles the semantic actions. TODO complete this method.
	 * 
	 * @param action
	 * @param parameter
	 */
	public  void handleSemanticAction(SemanticAction action, SemanticActionParameters parameter)
	{
		final String actionName = action.getActionName();
		if (SemanticActionStandardMethods.FOR_EACH.equals(actionName))
		{
			handleForLoop((ForEachSemanticAction)action, parameter);
		}
		else if (SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE.equals(actionName))
		{
			createAndVisualizeImgSurrogate(action, parameter);
		}
		else if (SemanticActionStandardMethods.CREATE_CONATINER.equals(actionName))
		{
			createContainer(action, parameter);
		}
		else if (SemanticActionStandardMethods.PROCESS_DOCUMENT.equals(actionName))
		{
			processDocument(action, parameter);
		}
		else if (SemanticActionStandardMethods.SET_METADATA.equals(actionName))
		{
			setMetadata(action, parameter);
		}
		else if (SemanticActionStandardMethods.CREATE_CONTAINER_FOR_SEARCH.equals(actionName))
		{
			createContainerForSearch(action, parameter);
		}
		else if (SemanticActionStandardMethods.CREATE_SEARCH.equals(actionName))
		{
			// TODO dont know what this action means
		}
		else if (SemanticActionStandardMethods.GET_FIELD_ACTION.equals(actionName))
		{
			getValueAction(action, parameter);
		}
		else if (SemanticActionStandardMethods.SETTER_ACTION.equals(actionName))
		{
			setValueAction(action, parameter);
		}
		else if(SemanticActionStandardMethods.PROCESS_SEARCH.equals(actionName))
		{
			processSearch((ProcessSearchSemanticAction)action,parameter);
		}
		else
		{
			handleGeneralAction(action, parameter);
		}
	}

	
	/**
	 * @param numberOfCollection
	 *          the numberOfCollection to set
	 */
	public void setNumberOfCollection(int numberOfCollection)
	{
		this.numberOfCollection = numberOfCollection;
	}

	/**
	 * Sets the flag if any based on the checks in the action TODO right now 2 types of checks are
	 * implemented. 1) NOT_NULL_CHECK: sets flag true if returnValue is not true 2) METHOD_CHECK: Used
	 * for methods with boolean return value. Sets the flag equal to return value.
	 * 
	 * @param action
	 * @param returnValue
	 */
	protected  void setFlagIfAny(SemanticAction action, Object returnValue)
	{
		// get the checks for this action
		ArrayListState<Check> checks = action.getChecks();

		// if checks are not null
		if (checks != null)
		{
			// loop over all the checks
			for (int i = 0; i < checks.size(); i++)
			{
				// get the name of the check
				String checkType = checks.get(i).getName();

				// now see which check it is
				if (checkType.equals(SemanticActionsKeyWords.NOT_NULL_CHECK))
				{
					// this is a not null check
					boolean flagValue = true;
					if (returnValue == null)
					{
						flagValue = false;
					}
					semanticActionFlagMap.put(checks.get(i).getFlagName(), flagValue);
				}
				else if (checkType.equals(SemanticActionsKeyWords.METHOD_CHECK))
				{
					// This is a method check
					semanticActionFlagMap.put(checks.get(i).getFlagName(), (Boolean) returnValue);
				}
			} // end for
		}// end if
	}

	/**
	 * This function checks for the precondtion flag values for this action and returns the "anded"
	 * result.
	 * 
	 * @param action
	 * @return
	 */
	protected   boolean checkPreConditionFlagsIfAny(SemanticAction action)
	{
		boolean returnValue = true;
		ArrayListState<IFclause> flagChecks = action.getFlagChecks();

		if (flagChecks != null)
		{
			// loop over all the flags to be checked
			for (int i = 0; i < flagChecks.size(); i++)
			{
				boolean flag = semanticActionFlagMap.get(flagChecks.get(i).getName());
				returnValue = returnValue && flag;
			}
		}
		return returnValue;
	}

	protected  Object getObjectFromKeyName(String key, SemanticActionParameters parameters)
	{
		Object returnValue = null;

		// first check if this object is in some returned value of some
		// semanticAction
		returnValue = semanticActionReturnValueMap.get(key);

		if (returnValue == null)
		{
			// if this was passed in parameters
			returnValue = parameters.getObjectInstance(key);
		}
		
		return returnValue;
	}

}
