package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.exceptions.ForLoopException;
import ecologylab.semantics.actions.exceptions.IfActionException;
import ecologylab.semantics.actions.exceptions.SemanticActionExecutionException;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.tools.GenericIterable;

/**
 * This is the handler for semantic actions. * It has a <code>handleSemanticAction</code> method
 * which decides what action to take when a semantic action is passed to it. There is one
 * SemanticActionHandler created for each DocumentParser.connect()
 * 
 * @author amathur
 */
// TODO Might want to implement lexical scoping in variables.
public class SemanticActionHandler<C extends Container, IC extends InfoCollector<C>>
		extends Debug implements SemanticActionStandardMethods, SemanticActionsKeyWords,
		SemanticActionNamedArguments
{
	IC infoCollector;

	static final Scope<Object>						BUILT_IN_SCOPE	= new Scope<Object>();

	static
	{
		BUILT_IN_SCOPE.put(FALSE, false);
		BUILT_IN_SCOPE.put(TRUE, true);
		BUILT_IN_SCOPE.put(NULL, null);
	}

	/**
	 * This is a map of return value and objects from semantic action. The key being the return_value
	 * of the semantic action. TODO remane this also to some thing like objectMap or variableMap.
	 */
	protected Scope<Object>								semanticActionVariableMap;

	/**
	 * Error handler for the semantic actions.
	 */
	protected SemanticActionErrorHandler	errorHandler;

	public SemanticActionHandler(IC infoCollector)
	{
		this.infoCollector				= infoCollector;
		semanticActionVariableMap = new Scope<Object>(BUILT_IN_SCOPE);
	}

	public Scope<Object> getSemanticActionVariableMap()
	{
		return semanticActionVariableMap;
	}

	/**
	 * main entry to handle semantic actions. FOR loops and IFs are handled directly (they have built-
	 * in semantics and are not overridable). otherwise it will can the perform() method on the action
	 * object.
	 * 
	 * @param action
	 * @param parser
	 * @param infoCollector
	 */
	public void handleSemanticAction(SemanticAction action, DocumentParser parser, IC infoCollector)
	{
		if (!checkConditionsIfAny(action))
		{
			if (!(action instanceof IfSemanticAction))
				warning(String.format(
						"Semantic action %s not taken since (some) pre-requisite conditions are not met",
						action));
			return;
		}

		action.setInfoCollector(infoCollector);
		action.setSemanticActionHandler(this);
		action.setDocumentParser(parser);

		final String actionName = action.getActionName();

		try
		{
			if (SemanticActionStandardMethods.FOR_EACH.equals(actionName))
			{
				handleForLoop((ForEachSemanticAction) action, parser, infoCollector);
			}
			else if (SemanticActionStandardMethods.IF.equals(actionName))
			{
				handleIf((IfSemanticAction) action, parser, infoCollector);
			}
			else
			{
				handleGeneralAction(action);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("The action " + actionName
					+ " could not be executed. Please see the stack trace for errors.");
		}
	}

	/**
	 * handle semantic actions other than FOR and IF.
	 * 
	 * @param action
	 */
	public void handleGeneralAction(SemanticAction action)
	{
		// get the object on which the action has to be taken
		String objectName = action.getObject();
		if (objectName == null)
			objectName = SemanticActionsKeyWords.METADATA;
		Object object = semanticActionVariableMap.get(objectName);

		try
		{
			Object returnValue = action.perform(object);
			if (action.getReturnObjectName() != null)
			{
				semanticActionVariableMap.put(action.getReturnObjectName(), returnValue);
			}
		}
		catch (Exception e)
		{
			throw new SemanticActionExecutionException(e, action, semanticActionVariableMap);
		}
	}

	public synchronized void handleForLoop(ForEachSemanticAction action, DocumentParser parser,
			IC infoCollector)
	{
		try
		{
			// get all the action which have to be performed in loop
			ArrayList<SemanticAction> nestedSemanticActions = action.getNestedSemanticActionList();

			// get the collection object name on which we have to loop
			String collectionObjectName = action.getCollection();
			// if(checkPreConditionFlagsIfAny(action))
			{
				// get the actual collection object
				Object collectionObject = semanticActionVariableMap.get(collectionObjectName);

				if (collectionObject == null)
				{
					error("Can't execute loop because collection is null: " + action.serialize());
					return;
				}

				GenericIterable gItr = new GenericIterable(collectionObject);
				// debug(documentType.purl().toString()); <<Annoying as hell !
				Iterator itr = gItr.iterator();
				int collectionSize = gItr.size();

				// set the size of collection in the for loop action.
				if (action.getSize() != null)
				{
					// we have the size value. so we add it in parameters
					semanticActionVariableMap.put(action.getSize(), collectionSize);
				}

				int start = 0;
				int end = collectionSize;

				if (action.getStart() != null)
				{
					start = Integer.parseInt(action.getStart());
				}
				if (action.getEnd() != null)
				{
					end = Integer.parseInt(action.getEnd());
				}

				// start the loop over each object
				for (int i = start; i < end; i++)
				{
					Object item = gItr.get(i);
					// put it in semantic action return value map
					semanticActionVariableMap.put(action.getAs(), item);

					// see if current index is needed
					if (action.getCurIndex() != null)
					{
						// set the value of this variable in parameters
						semanticActionVariableMap.put(action.getCurIndex(), i);
					}

					// now take all the actions nested inside for loop
					for (SemanticAction nestedSemanticAction : nestedSemanticActions)
						handleSemanticAction(nestedSemanticAction, parser, infoCollector);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ForLoopException(e, action, semanticActionVariableMap);
		}
	}

	public void handleIf(IfSemanticAction action, DocumentParser parser, IC infoCollector)
	{
		// conditions have been checked in handleSemanticActio()
		try
		{
			ArrayList<SemanticAction> nestedSemanticActions = action.getNestedSemanticActionList();
			for (SemanticAction nestedSemanticAction : nestedSemanticActions)
				handleSemanticAction(nestedSemanticAction, parser, infoCollector);
		}
		catch (Exception e)
		{
			throw new IfActionException(e, action, semanticActionVariableMap);
		}
	}

	/**
	 * This function checks for the pre-condition flag values for this action and returns the "anded"
	 * result.
	 * 
	 * @param action
	 * @return true if conditions are satisfied; false otherwise.
	 */
	protected boolean checkConditionsIfAny(SemanticAction action)
	{
		ArrayList<Condition> conditions = action.getChecks();

		if (conditions != null)
		{
			// loop over all the flags to be checked
			for (Condition condition : conditions)
			{
				boolean flag = condition.evaluate(this);
				if (!flag)
					return false;
			}
		}
		return true;
	}

	/*********************** utilities used by semantic actions ************************/

	/**
	 * Create a container for a specific link.
	 * 
	 * @param container_link
	 *          the link you want to create a container for.
	 * 
	 * @return The appropriate container for the URL, or null if there is an error.
	 */
	public C createContainer(SemanticAction action, DocumentParser parser, IC infoCollector)
	{
		ParsedURL purl = (ParsedURL) action
				.getArgumentObject(SemanticActionNamedArguments.CONTAINER_LINK);
		if (purl != null)
		{
			Container ancestor = parser.getContainer();
			MetaMetadata mmd = infoCollector.metaMetaDataRepository().getDocumentMM(purl);
			C container = infoCollector.getContainer((C) ancestor, null, mmd, purl, false, true, false);
			return container;
		}
		return null;
	}

	/*********************** hooks ************************/

	public void preSemanticActionsHook(Metadata metadata)
	{

	}

	public void postSemanticActionsHook(Metadata metadata)
	{

	}

	/*********************** used by the library ************************/

	public void recycle()
	{
		semanticActionVariableMap.clear();
		semanticActionVariableMap = null;
	}

}
