package ecologylab.semantics.actions.exceptions;

import java.util.ArrayList;

import ecologylab.collections.Scope;
import ecologylab.semantics.actions.IfSemanticAction;
import ecologylab.semantics.actions.NestedSemanticAction;
import ecologylab.semantics.actions.SemanticAction;

public class IfActionException extends SemanticActionExecutionException
{

	public IfActionException(Exception e, NestedSemanticAction action,
			Scope<Object> semanticActionReturnValueMap)
	{
		super(action);
		System.out.println(":::All the nested semantic actions might not execute properly:::");
		ArrayList<SemanticAction> nestedActions= action.getNestedSemanticActionList();
		for(int i=0;i<nestedActions.size();i++)
		{
			System.out.println("\t\t\t["+nestedActions.get(i).getActionName().toUpperCase()+"] skipped");
		}
		stackTrace(semanticActionReturnValueMap);
	}


}
