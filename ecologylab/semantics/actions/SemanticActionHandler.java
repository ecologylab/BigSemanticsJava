/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.DocumentType;
import ecologylab.generic.Debug;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metametadata.Check;
import ecologylab.semantics.metametadata.FlagCheck;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.ArrayListState;

/**
*
* This is the handler for semantic actions. It contains a list of abstract method for the semantic
* action possible. It has a <code>handleSemanticAction</code> method which decides what action to
* take when a semantic action is passed to it. If a new semantic action has to be added we must add a
* case to handle that in this method.
* TODO Might want to implement lexical scoping in variables.
*
* @author amathur
*
*/
public abstract class SemanticActionHandler<SA extends SemanticAction,C extends Container,E extends ElementState> 
extends Debug
implements SemanticActionStandardMethods
{

	/**
	 * This is a map of return value and objects from semantic action. The key being the return_value
	 * of the semantic action.
	 * TODO remane this also to some thing like objectMap or variableMap.
	 */
	protected Scope<Object>		semanticActionReturnValueMap;

	/**
	 * Map of various flags used and set during the semantic actions
	 */
	protected Scope<Boolean>	semanticActionFlagMap;

	/**
	 * Stores the number of items in the collection for loops
	 */
	private int													numberOfCollection;
	
	/**
	 * Error handler for the semantic actions.
	 */
	protected SemanticActionErrorHandler 							errorHandler;

	
	/**
	 * 
	 * @param action
	 * @param paramter
	 */
	public abstract void createAndVisualizeImgSurrogate(SA action, SemanticActionParameters paramter,DocumentType<C,?,?> docType,InfoCollector infoCollector);

	/**
	 * 
	 * @param action
	 * @param paramter
	 * @return
	 */
	public abstract C createContainer(SA action, SemanticActionParameters parameter,DocumentType<C,?,?> docType,InfoCollector infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void processDocument(SA action, SemanticActionParameters parameter,DocumentType docType,InfoCollector infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void setMetadata(SA action, SemanticActionParameters parameter,DocumentType docType,InfoCollector infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @return
	 */
	public abstract C createContainerForSearch(SA action, SemanticActionParameters parameter,DocumentType<C,?,?> docType,InfoCollector<C,?> infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void handleGeneralAction(SA action, SemanticActionParameters parameter);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void setValueAction(SA action, SemanticActionParameters parameter,DocumentType docType,InfoCollector infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void getValueAction(SA action, SemanticActionParameters parameter,DocumentType docType,InfoCollector infoCollector);
	
	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void processSearch(SA action, SemanticActionParameters parameter,DocumentType docType,InfoCollector infoCollector);

	
	/**
	 * Implementation of for loop.
	 * @param parameter
	 * @param documentType TODO
	 * @param infoCollector TODO
	 * @param semanticAction
	 */
	public synchronized void  handleForLoop(ForEachSemanticAction action, SemanticActionParameters parameter, DocumentType documentType, InfoCollector infoCollector)
	{
		// get all the action which have to be performed in loop
		ArrayList<SA> nestedSemanticActions = action.getNestedSemanticActionList();
		
		// get the collection object name on which we have to loop
		String collectionObject = action.getCollection(); 
		
		//get the actual collection object
		Iterable<E> collection = (Iterable<E>) getObjectFromKeyName(collectionObject, parameter);
		
		Iterator<E> itr = collection.iterator();
		
		// start the loop over each object
		while(itr.hasNext())
		{
			// get the kth item
			Object item = itr.next();
									
			//put it in semantic action return value map
			semanticActionReturnValueMap.put(action.getAs(), item);
			
			// kth iteration of loop
			for (int j = 0; j < nestedSemanticActions.size(); j++)
			{
				// get the jth action inside the loop
				SA nestedSemanticAction = nestedSemanticActions.get(j);

				// perform the jth action
				handleSemanticAction(nestedSemanticAction, parameter, documentType, infoCollector);
			}
		}
	}

	/**
   * Method which handles the semantic actions.When you define a new semantic action it must be
   * added here as another <code>if-else</code> clause. Also a corresponding method, mostly abstract
   * should be declared in this class for handling the action. TODO complete this method.
   *
   * @param action
	 * @param parameter
	 * @param documentType TODO
	 * @param infoCollector TODO
   */
	public  void handleSemanticAction(SA action, SemanticActionParameters parameter, DocumentType documentType, InfoCollector infoCollector)
	{
		final String actionName = action.getActionName();
		if (SemanticActionStandardMethods.FOR_EACH.equals(actionName))
		{
			handleForLoop((ForEachSemanticAction)action, parameter, documentType, infoCollector);
		}
		else if (SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE.equals(actionName))
		{
			createAndVisualizeImgSurrogate(action, parameter,documentType,infoCollector);
		}
		else if (SemanticActionStandardMethods.CREATE_CONATINER.equals(actionName))
		{
			createContainer(action, parameter,documentType,infoCollector);
		}
		else if (SemanticActionStandardMethods.PROCESS_DOCUMENT.equals(actionName))
		{
			processDocument(action, parameter,documentType,infoCollector);
		}
		else if (SemanticActionStandardMethods.SET_METADATA.equals(actionName))
		{
			setMetadata(action, parameter,documentType,infoCollector);
		}
		else if (SemanticActionStandardMethods.CREATE_CONTAINER_FOR_SEARCH.equals(actionName))
		{
			createContainerForSearch(action, parameter,documentType,infoCollector);
		}
		else if (SemanticActionStandardMethods.CREATE_SEARCH.equals(actionName))
		{
			// TODO dont know what this action means
		}
		else if (SemanticActionStandardMethods.GET_FIELD_ACTION.equals(actionName))
		{
			getValueAction(action, parameter,documentType,infoCollector);
		}
		else if (SemanticActionStandardMethods.SETTER_ACTION.equals(actionName))
		{
			setValueAction(action, parameter,documentType,infoCollector);
		}
		else if(SemanticActionStandardMethods.PROCESS_SEARCH.equals(actionName))
		{
			processSearch(action,parameter,documentType,infoCollector);
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
	protected  void setFlagIfAny(SA action, Object returnValue)
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
				String checkType = checks.get(i).getCondition();

				// now see which check it is
				if (checkType.equals(SemanticActionsKeyWords.NOT_NULL_CHECK))
				{
					// this is a not null check
					boolean flagValue = true;
					if (returnValue == null)
					{
						flagValue = false;
					}
					semanticActionFlagMap.put(checks.get(i).getName(), flagValue);
				}
				else if (checkType.equals(SemanticActionsKeyWords.METHOD_CHECK))
				{
					// This is a method check
					semanticActionFlagMap.put(checks.get(i).getName(), (Boolean) returnValue);
				}
			} // end for
		}// end if
	}

	/**
	 * This function checks for the pre-condition flag values for this action and returns the "anded"
	 * result.
	 * 
	 * @param action
	 * @return
	 */
	protected   boolean checkPreConditionFlagsIfAny(SA action)
	{
		boolean returnValue = true;
		ArrayListState<FlagCheck> flagChecks = action.getFlagChecks();

		if (flagChecks != null)
		{
			// loop over all the flags to be checked
			for (int i = 0; i < flagChecks.size(); i++)
			{
				boolean flag = semanticActionFlagMap.get(flagChecks.get(i).getValue());
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
