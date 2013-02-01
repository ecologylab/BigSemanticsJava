package ecologylab.bigsemantics.actions.exceptions;

import java.util.ArrayList;

import ecologylab.bigsemantics.actions.NestedSemanticAction;
import ecologylab.bigsemantics.actions.SemanticAction;
import ecologylab.collections.Scope;

public class IfActionException extends SemanticActionExecutionException
{

	public IfActionException(Exception e, NestedSemanticAction action,
			Scope<Object> semanticActionReturnValueMap)
	{
		super(action);
		System.out.println(":::All the nested semantic actions might not execute properly:::");
		ArrayList<SemanticAction> nestedActions= action.getNestedSemanticActionList();
		if (nestedActions != null && nestedActions.size() > 0)
		{
			for(int i=0;i<nestedActions.size();i++)
			{
				System.out.println("\t\t\t["+nestedActions.get(i).getActionName().toUpperCase()+"] skipped");
			}
		}
		stackTrace(semanticActionReturnValueMap);
	}


}
