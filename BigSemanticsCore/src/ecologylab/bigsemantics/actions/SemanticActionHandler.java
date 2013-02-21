package ecologylab.bigsemantics.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ecologylab.bigsemantics.actions.exceptions.ForLoopException;
import ecologylab.bigsemantics.actions.exceptions.IfActionException;
import ecologylab.bigsemantics.actions.exceptions.SemanticActionExecutionException;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.tools.GenericIterable;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * This is the handler for semantic actions. * It has a <code>handleSemanticAction</code> method
 * which decides what action to take when a semantic action is passed to it. There is one
 * SemanticActionHandler created for each DocumentParser.connect()
 * 
 * @author amathur
 */
// TODO Might want to implement lexical scoping in variables.
public class SemanticActionHandler
		extends Debug
		implements SemanticActionStandardMethods, SemanticActionsKeyWords, SemanticActionNamedArguments
{

	static final Scope<Object>												BUILT_IN_SCOPE	= new Scope<Object>();

	static
	{
		BUILT_IN_SCOPE.put(FALSE, false);
		BUILT_IN_SCOPE.put(TRUE, true);
		BUILT_IN_SCOPE.put(NULL, null);
	}

	private SemanticsGlobalScope											semanticsScope;

	private DocumentParser														documentParser;

	/**
	 * This is a map of return value and objects from semantic action. The key being the return_value
	 * of the semantic action. TODO remane this also to some thing like objectMap or variableMap.
	 */
	private Scope<Object>															semanticActionVariableMap;

	private Map<SemanticAction, Map<String, Object>>	actionStates		= new HashMap<SemanticAction, Map<String, Object>>();

	/**
	 * Error handler for the semantic actions.
	 */
	private SemanticActionErrorHandler								errorHandler;

	boolean																						requestWaiting	= false;

	MetaMetadata																			metaMetadata;

	Metadata																					metadata;

	public SemanticActionHandler(SemanticsGlobalScope semanticsScope, DocumentParser documentParser)
	{
		this(semanticsScope, documentParser, new Scope<Object>(BUILT_IN_SCOPE));
	}

	public SemanticActionHandler(SemanticsGlobalScope semanticsScope, DocumentParser documentParser, 
			Scope<Object> semanticActionVariableMap)
	{
		this.semanticsScope 			= semanticsScope;
		this.documentParser 			= documentParser;
		semanticActionVariableMap.put(
				SemanticActionsKeyWords.PURLCONNECTION_MIME,
				documentParser.purlConnection().mimeType());
		this.semanticActionVariableMap	= semanticActionVariableMap;
	}

	public Scope<Object> getSemanticActionVariableMap()
	{
		return semanticActionVariableMap;
	}

	public void takeSemanticActions()
	{
		if (metaMetadata != null && metadata != null)
			takeSemanticActions(metaMetadata, metadata);
	}

	public void takeSemanticActions(Metadata metadata)
	{
		takeSemanticActions((MetaMetadata) metadata.getMetaMetadata(), metadata);
	}
	public void takeSemanticActions(MetaMetadata metaMetadata, Metadata metadata)
	{
		takeSemanticActions(metaMetadata, metadata, metaMetadata.getSemanticActions());
	}
	public void takeSemanticActions(MetaMetadata metaMetadata, Metadata metadata, ArrayList<? extends SemanticAction> semanticActions)
	{
		if (semanticActions == null)
		{
			System.out.println("[ParserBase] warning: no semantic actions exist");
			return;
		}
		
		if (requestWaiting)
		{
			requestWaiting = false;
		}
		else
		{
			this.metaMetadata = metaMetadata;
			this.metadata = metadata;

			//FIXME -- should not have SemanticActionsKeyWords && SemanticActionsNamedArguments as separate sets !!!
			semanticActionVariableMap.put(DOCUMENT_TYPE, documentParser);
			semanticActionVariableMap.put(SemanticActionsKeyWords.METADATA, metadata);
			semanticActionVariableMap.put(TRUE_PURL, documentParser.getTruePURL());

			preSemanticActionsHook(metadata);
		}
		for (int i = 0; i < semanticActions.size(); i++)
		{
			SemanticAction action = semanticActions.get(i);
			handleSemanticAction(action, documentParser, semanticsScope);
			if (requestWaiting)
				return;
		}
		postSemanticActionsHook(metadata);

		recycle();
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
	public void handleSemanticAction(SemanticAction action, DocumentParser parser, SemanticsGlobalScope infoCollector)
	{
		int state = getActionState(action, "state", SemanticAction.INIT);
		if (state == SemanticAction.FIN || requestWaiting)
			return;
//		debug("["+parser+"] semantic action: " + action.getActionName() + ", SA class: " + action.getClassSimpleName() + "\n");
		
		action.setSemanticActionHandler(this);
		
		// here state must be INTER or INIT

		// if state == INIT, we check pre-conditions
		// if state == INTER, because this must have been started, we skip checking pre-conditions
		if (state == SemanticAction.INIT)
		{
			if (!checkConditionsIfAny(action))
			{
				if (!(action instanceof IfSemanticAction))
					warning(String.format(
							"Semantic action %s not taken since (some) pre-requisite conditions are not met",
							action));
				return;
			}
		}
		else
		{
			debug("re-entering actions with pre-conditions; checking pre-conditions skipped: " + action.getActionName());
		}

		action.setSessionScope(infoCollector);
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
		finally
		{
			if (!requestWaiting)
				setActionState(action, "state", SemanticAction.FIN);
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
			Object returnValue = null;
			returnValue = action.perform(object);
			if (requestWaiting)
				return;
			if (action.getReturnObjectName() != null && returnValue != null)
			{
				semanticActionVariableMap.put(action.getReturnObjectName(), returnValue);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(); // only for debug
			if (e instanceof SemanticActionExecutionException)
				throw (RuntimeException) e;
			throw new SemanticActionExecutionException(e, action, semanticActionVariableMap);
		}
	}

	public synchronized void handleForLoop(ForEachSemanticAction action, DocumentParser parser,
			SemanticsGlobalScope infoCollector)
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
					error("Can't execute loop because collection is null: " + SimplTypesScope.serialize(action, StringFormat.XML));
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

				if (getActionState(action, "state", SemanticAction.INIT) == SemanticAction.INTER)
				{
					start = getActionState(action, "current_index", 0);
				}

				setActionState(action, "state", SemanticAction.INTER);

				// start the loop over each object
				for (int i = start; i < end; i++)
				{
					setActionState(action, "current_index", i);
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
					if (nestedSemanticActions != null)
  					for (SemanticAction nestedSemanticAction : nestedSemanticActions)
  					{
  						handleSemanticAction(nestedSemanticAction, parser, infoCollector);
  					}

					if (requestWaiting)
						break;

					// at the end of each iteration clear flags so that we can do the next iteration
					action.setNestedActionState("state", SemanticAction.INIT);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ForLoopException(e, action, semanticActionVariableMap);
		}
	}

	public void handleIf(IfSemanticAction action, DocumentParser parser, SemanticsGlobalScope infoCollector)
	{
		// conditions have been checked in handleSemanticAction()

		try
		{
			setActionState(action, "state", SemanticAction.INTER);
			ArrayList<SemanticAction> nestedSemanticActions = action.getNestedSemanticActionList();
			if (nestedSemanticActions != null)
  			for (SemanticAction nestedSemanticAction : nestedSemanticActions)
  				handleSemanticAction(nestedSemanticAction, parser, infoCollector);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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

	public <T> T getActionState(SemanticAction action, String name, T defaultValue)
	{
		if (actionStates.containsKey(action))
		{
			Map<String, Object> states = actionStates.get(action);
			Object state = states.get(name);
			if (state != null)
				return (T) state;
		}
		return defaultValue;
	}

	public <T> void setActionState(SemanticAction action, String name, T value)
	{
		if (!actionStates.containsKey(action))
			actionStates.put(action, new HashMap<String, Object>());
		Map<String, Object> states = actionStates.get(action);
		states.put(name, value);
	}

}
