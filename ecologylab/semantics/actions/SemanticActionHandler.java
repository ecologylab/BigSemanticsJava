/**
 * 
 */
package ecologylab.semantics.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.DocumentParser;
import ecologylab.generic.Debug;
import ecologylab.semantics.actions.exceptions.ApplyXPathException;
import ecologylab.semantics.actions.exceptions.ForLoopException;
import ecologylab.semantics.actions.exceptions.NestedActionException;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metametadata.Argument;
import ecologylab.semantics.metametadata.Check;
import ecologylab.semantics.metametadata.DefVar;
import ecologylab.semantics.tools.GenericIterable;
import ecologylab.xml.types.element.ArrayListState;

/**
*
* This is the handler for semantic actions. It contains a list of abstract method for the semantic
* action possible. It has a <code>handleSemanticAction</code> method which decides what action to
* take when a semantic action is passed to it. If a new semantic action has to be added we must add a
* case to handle that in this method.
* There is one SemanticActionHandler created for each DocumentType.connect
* TODO Might want to implement lexical scoping in variables.
*
* @author amathur
*
*/
public abstract class SemanticActionHandler<C extends Container, IC extends InfoCollector<C>> 
extends Debug
implements SemanticActionStandardMethods,SemanticActionsKeyWords,SemanticActionNamedArguments
{

	/**
	 * TODO move this also to super class This is the Map of StandadObject on which we might call some
	 * methods.
	 */
	//FIXME -- andruid > abhinav: this Scope is currently never initialized. what is the use case to keep it?
	protected Scope<Object>	standardObjectMap	= new Scope<Object>();
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
	 * Error handler for the semantic actions.
	 */
	protected SemanticActionErrorHandler 							errorHandler;
	
	/**
	 * Parameters
	 */
	protected SemanticActionParameters parameter; 

	public SemanticActionHandler()
	{
		semanticActionReturnValueMap = new Scope<Object>();
		semanticActionFlagMap = new Scope<Boolean>();
		parameter = new SemanticActionParameters(standardObjectMap);
	}
	
	/**
	 * 
	 * @param action
	 * @param paramter
	 */
	public abstract void createAndVisualizeImgSurrogate(SemanticAction action, SemanticActionParameters paramter,DocumentParser<C,?,?> docType, IC infoCollector);

	/**
	 * 
	 * @param action
	 * @param paramter
	 * @return
	 */
	public abstract C createContainer(SemanticAction action, SemanticActionParameters parameter,DocumentParser<C,?,?> docType, IC infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void processDocument(SemanticAction action, SemanticActionParameters parameter,DocumentParser docType, IC infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void setMetadata(SemanticAction action, SemanticActionParameters parameter,DocumentParser docType, IC infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @return
	 */
	public abstract C createContainerForSearch(SemanticAction action, SemanticActionParameters parameter,DocumentParser<C,?,?> docType, IC infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public abstract void handleGeneralAction(SemanticAction action, SemanticActionParameters parameter) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public abstract void setFieldAction(SemanticAction action, SemanticActionParameters parameter,DocumentParser docType, IC infoCollector) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void getFieldAction(SemanticAction action, SemanticActionParameters parameter,DocumentParser docType, IC infoCollector);
	
	/**
	 * 
	 * @param action
	 * @param parameter
	 */
	public abstract void processSearch(SemanticAction action, SemanticActionParameters parameter,DocumentParser docType, IC infoCollector);

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @param documentType
	 * @param infoCollector
	 */
	public abstract void createSemanticAnchor(SemanticAction action, SemanticActionParameters parameter,DocumentParser<C,?,?> documentType, IC infoCollector);
	
	/**
	 * 
	 * @param action
	 * @param parameter
	 * @param documentType
	 * @param infoCollector
	 */
	public abstract void queueDocumentForDownload(SemanticAction action, SemanticActionParameters parameter,DocumentParser documentType, IC infoCollector);
	
	/**
	 * 
	 * @param action
	 * @param parameter2
	 * @param documentType
	 * @param infoCollector
	 */
	public abstract void createAndVisualizeTextSurrogateSemanticAction(SemanticAction action,
			SemanticActionParameters parameter2, DocumentParser documentType, IC infoCollector);
	
	/**
	 * 
	 * @param action
	 * @param parameter
	 * @param documentType
	 * @param infoCollector
	 */
	public abstract void syncNestedMetadataSemanticAction(SemanticAction action,	SemanticActionParameters parameter, DocumentParser documentType, IC infoCollector);
	
	
	
	/**
	 * Implementation of for loop.
	 * @param parameter
	 * @param documentType TODO
	 * @param infoCollector TODO
	 * @param semanticAction
	 */
	public synchronized void  handleForLoop(ForEachSemanticAction action, SemanticActionParameters parameter, DocumentParser documentType,  IC infoCollector)
	{
		try
		{
			// get all the action which have to be performed in loop
			ArrayList<SemanticAction> nestedSemanticActions = action.getNestedSemanticActionList();
			
			// get the collection object name on which we have to loop
			String collectionObjectName = action.getCollection(); 
			//if(checkPreConditionFlagsIfAny(action))
			{
				//get the actual collection object
				Object collectionObject =  getObjectFromKeyName(collectionObjectName, parameter);
				GenericIterable gItr = new GenericIterable(collectionObject);
				System.out.println(documentType.purl());
				Iterator itr = gItr.iterator();
				int collectionSize=gItr.size();
				
				// set the size of collection in the for loop action.
				if(action.getSize()!=null)
				{
					// we have the size value. so we add it in parameters
					parameter.addParameter(action.getSize(), collectionSize);
				}
				
				int start =0;
				int end =collectionSize;
				
				if(action.getStart()!=null)
				{
					start =Integer.parseInt(action.getStart());
				}
				if(action.getEnd()!=null)
				{
					end = Integer.parseInt(action.getEnd());
				}
				
				// start the loop over each object
				for(int i=start;i<end;i++)
				{	
					Object item = gItr.get(i);
					//put it in semantic action return value map
					semanticActionReturnValueMap.put(action.getAs(), item);
					
					// see if current index is needed
					if(action.getCurIndex()!=null)
					{
						// set the value of this variable in parameters
						parameter.addParameter(action.getCurIndex(), i);
					}
					
					//now take all the actions nested inside for loop
					for (SemanticAction nestedSemanticAction  : nestedSemanticActions)
						handleSemanticAction(nestedSemanticAction, documentType, infoCollector);
				}
			}
		}
		catch(Exception e)
		{
			throw new ForLoopException(e,action,semanticActionReturnValueMap);
		}
	}
	
	/**
	 * Handles the IF statement
	 * @param action
	 * @param parameter2
	 * @param documentType
	 * @param infoCollector
	 */
	public void handleIf(IfSemanticAction action, SemanticActionParameters parameter2,
			DocumentParser documentType, IC infoCollector)
	{
		try
		{
			ArrayList<SemanticAction> nestedSemanticActions = action.getNestedSemanticActionList();
			
			// check if all the flags are true
			if(checkPreConditionFlagsIfAny(action))
			{
				// handle each of the nested action
				for (SemanticAction nestedSemanticAction  : nestedSemanticActions)
					handleSemanticAction(nestedSemanticAction, documentType, infoCollector);
			}
		}
		catch(Exception e)
		{
			throw new NestedActionException(e,action,semanticActionReturnValueMap);
		}
	}

	/**
	 *  Implementation of apply xpath
	 * @param action
	 * @param parameter2
	 * @param documentType
	 * @param infoCollector
	 */
	public void applyXPath(ApplyXPathSemanticAction action, SemanticActionParameters parameters,
			DocumentParser documentType, IC infoCollector)
	{
			try
			{
					// get the XPath object
					XPath xpath = XPathFactory.newInstance().newXPath();
				
					Node node =null;
					
					// if no node is specified, we will apply XPaths by deafult on document root.
					if(action.getNode()==null)
					{
							node =(Node) parameters.getObjectInstance(DOCUMENT_ROOT_NODE);
					}
					else
					{
						// get the node on which XPAth needs to be applied
						node = (Node)getObjectFromKeyName(action.getNode(), parameters);
					}
					
					final String xpathExpression = action.getXpath();
					
					//if return object is not null [Right now its gonna be NodeList only TODO; For other cases]
					if(action.getReturnObject()!=null)
					{
						  // apply xpath and get the node list
							NodeList nList =(NodeList) xpath.evaluate(xpathExpression, node, action.getReturnObject());
							
							// put the value in the map
							semanticActionReturnValueMap.put(action.getReturnValue(), nList);
					}
					else
					{
							// its gonna be a simple string evaluation
							String evaluation = xpath.evaluate(xpathExpression, node);
							
							// put it into returnValueMap
							semanticActionReturnValueMap.put(action.getReturnValue(), evaluation);
					}
					
			}
			catch(Exception e)
			{
				throw new ApplyXPathException(e,action,semanticActionReturnValueMap);
			}
		
	}
	

		
	/**
	 *  Function which evaluates rank weight
	 * @param action
	 * @param parameter
	 * @param documentType
	 * @param infoCollector
	 * @return
	 */
	public float evaluateRankWeight(SemanticAction action, SemanticActionParameters parameters,
			DocumentParser documentType, IC infoCollector)
	{
		Argument indexA = getNamedArgument(action, INDEX);
		int index = (Integer)getObjectFromKeyName(indexA.getValue(), parameters);
		
		Argument sizeA =getNamedArgument(action, SIZE);
		int size = (Integer)getObjectFromKeyName(sizeA.getValue(), parameters);
		
		float result = ((float)size-index)/size;
		
		semanticActionReturnValueMap.put(action.getReturnValue(), result);
		
		return result;
	}
	
	/**
	 * This function evaluates  any local variables which are decalred inside the semantic action
	 * and sets them in semantic action return value map.
	 * Right now we assume that only numerical constants will be passed to as variables.
	 * TODO implement a generic evaluate variables for local and global variables
	 * @param action
	 * @param documentType
	 * @param infoCollector
	 */
	protected void evalauateVaiablesIfAny(SemanticAction action, DocumentParser documentType, IC infoCollector)
	{
		ArrayList<DefVar> defVars = action.getDefVars();
		if(defVars!=null)
		{
			// proceed only if some variables are defined.
			for(DefVar defVar :	defVars)
			{
				 // get value[TODO have to change if any thing apart from numerical value can be defined as local variable]
				 float value = Float.parseFloat(defVar.getValue());
				 parameter.addParameter(defVar.getName(), value);
			}
		}
	}
	
	
	
	/**
   * Method which handles the semantic actions.When you define a new semantic action it must be
   * added here as another <code>if-else</code> clause. Also a corresponding method, mostly abstract
   * should be declared in this class for handling the action. TODO complete this method.
   *
   * @param action
	 * @param documentType TODO
	 * @param infoCollector TODO
   */
	public  void handleSemanticAction(SemanticAction action, DocumentParser documentType, IC infoCollector)
	{
		final String actionName = action.getActionName();
		
		try
		{
			// evaluate local variables
			evalauateVaiablesIfAny(action,documentType,infoCollector);
			
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
				getFieldAction(action, parameter,documentType,infoCollector);
			}
			else if (SemanticActionStandardMethods.SET_FIELD_ACTION.equals(actionName))
			{
				setFieldAction(action, parameter,documentType,infoCollector);
			}
			else if(SemanticActionStandardMethods.PROCESS_SEARCH.equals(actionName))
			{
				processSearch(action,parameter,documentType,infoCollector);
			}
			else if(SemanticActionStandardMethods.CREATE_SEMANTIC_ANCHOR.equals(actionName))
			{
				createSemanticAnchor((CreateSemanticAnchorSemanticAction)action,parameter,documentType,infoCollector);
			}
			else if(SemanticActionStandardMethods.QUEUE_DOCUMENT_DOWNLOAD.equals(actionName))
			{
				queueDocumentForDownload(action, parameter, documentType, infoCollector);
			}
			else if(SemanticActionStandardMethods.APPLY_XPATH.equals(actionName))
			{
				applyXPath((ApplyXPathSemanticAction)action, parameter, documentType, infoCollector);
			}
			else if(SemanticActionStandardMethods.IF.equals(actionName))
			{
				handleIf((IfSemanticAction)action,parameter,documentType,infoCollector);
			}
			else if(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_TEXT_SURROGATE.equals(actionName))
			{
				createAndVisualizeTextSurrogateSemanticAction(action,parameter,documentType,infoCollector);
			}
			else if(SemanticActionStandardMethods.TRY_SYNC_NESTED_METADATA.equals(actionName))
			{
				syncNestedMetadataSemanticAction(action,parameter,documentType,infoCollector);
			}
			else if(SemanticActionStandardMethods.EVALUATE_RANK_WEIGHT.equals(actionName))
			{
				evaluateRankWeight(action,parameter,documentType,infoCollector);
			}
			else
			{
				handleGeneralAction(action, parameter);
			}
		}
		catch(Exception e)
		{
			System.out.println("The action "+actionName+" could not be executed. Please see the stack trace for errors.");
		}
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
		ArrayList<Check> checks = action.getChecks();

		// if checks are not null
		if (checks != null)
		{
			// loop over all the checks
			for (Check check : checks)
			{
				String checkType = check.getCondition();

				// now see which check it is
				if (SemanticActionsKeyWords.NOT_NULL_CHECK.equals(checkType))
				{
					// this is a not null check
					boolean flagValue = true;
					if (returnValue == null)
					{
						flagValue = false;
					}
					semanticActionFlagMap.put(check.getName(), flagValue);
				}
				else if (SemanticActionsKeyWords.METHOD_CHECK.equals(checkType))
				{
					// This is a method check
					semanticActionFlagMap.put(check.getName(), (Boolean) returnValue);
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
	protected boolean checkPreConditionFlagsIfAny(IfSemanticAction action)
	{
		boolean returnValue = true;
		ArrayList<FlagCheck> flagChecks = action.getFlagChecks();

		if (flagChecks != null)
		{
			// loop over all the flags to be checked
			for (FlagCheck flagCheck : flagChecks)
			{
				boolean flag = semanticActionFlagMap.get(flagCheck.getValue());
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

	/**
	 * This function replaces the old value of a variable by new value. 
	 * This is right now used only in case of redirected containers.
	 * @param oldValue
	 * @param newValue
	 */
	public void replaceVariableValue(Object oldValue,Object newValue)
	{
		 // get the set of all the values stored in the return value map
		 Collection<Object> valueSet=semanticActionReturnValueMap.values();
		 
		 // get the set of all the keys
		 Collection<String> keySet= semanticActionReturnValueMap.keySet();
		 
		 // get an iterator over it
		 Iterator<Object> valueItr=valueSet.iterator();
		 
		 // get an iterator for the keys
		 Iterator<String> keyItr = keySet.iterator();
		 
		 // iterate over the values
		 while(valueItr.hasNext())
		 {
			 Object obj = valueItr.next();
			 String key = keyItr.next();
			 
			 // if the current value equals old value
			 if(obj.equals(oldValue))
			 {
				  //replace currentvalue with old value.
				 	semanticActionReturnValueMap.put(key, newValue);
				 	break;
			 }
		 }
	}

	/**
	 * @return the parameter
	 */
	public final SemanticActionParameters getParameter()
	{
		return parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public final void setParameter(SemanticActionParameters parameter)
	{
		this.parameter = parameter;
	}
	
		
	protected Argument getNamedArgument(SemanticAction action, String name)
	{
		return action.getArgument(name);
	}
	
	public void recycle()
	{
		parameter.recycle();
		semanticActionFlagMap.clear();
		semanticActionFlagMap = null;
		semanticActionReturnValueMap.clear();
		semanticActionReturnValueMap = null;
	}
}
