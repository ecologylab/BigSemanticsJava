/**
 * 
 */
package ecologylab.semantics.actions.exceptions;

import ecologylab.collections.Scope;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.html.utils.StringBuilderUtils;

/**
 * @author amathur
 * 
 */
public class SemanticActionExecutionException extends RuntimeException
{
	protected static String	ERROR_STRING	= "###########################POSSIBLE CAUSES OF ERROR##############################";

	public SemanticActionExecutionException(SemanticAction action)
	{
		System.out.println("\n########################### ERROR " + action.getActionName()
				+ " FAILED ###########################");
		//System.out.println(ERROR_STRING);
	}

	public SemanticActionExecutionException(SemanticAction action, String message)
	{
		this(action);
		System.out.println(message);
		SemanticActionHandler semanticActionHandler = action.getSemanticActionHandler();
		if (semanticActionHandler != null)
		{
			stackTrace(semanticActionHandler.getSemanticActionVariableMap());
		}
	}

	public SemanticActionExecutionException(Exception e, SemanticAction action,
			Scope<Object> semanticActionReturnValueMap)
	{
		this(action);
		
		StringBuilder buffy = StringBuilderUtils.acquire();
		buffy.append("Action Object:: ").append(action.getObject())
				.append("  :: is NULL or DOES NOT EXIST\n");
		buffy.append("Action ReturnValue:: ").append(action.getReturnObjectName())
				.append(" ::  is NULL or DOES NOT EXIST FOR SPECIFIED OBJECT");
		
		String errorMessage = buffy.toString();
		StringBuilderUtils.release(buffy);
		System.out.println(errorMessage);
		stackTrace(semanticActionReturnValueMap);
		//System.out.println("######## POSSIBLE CAUSE:");
		//e.printStackTrace();
		System.out
				.println("############################################################################################");
	}

	public void stackTrace(Scope<Object> map)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("--------------Meta-Metadata Trace--------------\n");
		map.dumpThis(sb, "");
		System.out.println(sb);
		System.out.println("----------------------------------------");
	}

}
