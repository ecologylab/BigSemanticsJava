package ecologylab.semantics.actions.exceptions;

import ecologylab.collections.Scope;
import ecologylab.semantics.actions.ForEachSemanticAction;
import ecologylab.semantics.actions.SemanticAction;

public class ForLoopException extends SemanticActionExecutionException
{


	public ForLoopException(Exception e, ForEachSemanticAction action,
			Scope<Object> semanticActionReturnValueMap)
	{
		super(e,action,semanticActionReturnValueMap);
		System.out.println(((ForEachSemanticAction)action).getCollection()+" :: is NULL or does not exists");
		stackTrace(semanticActionReturnValueMap);
	}


}
