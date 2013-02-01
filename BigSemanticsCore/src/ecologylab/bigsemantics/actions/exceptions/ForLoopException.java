package ecologylab.bigsemantics.actions.exceptions;

import ecologylab.bigsemantics.actions.ForEachSemanticAction;
import ecologylab.collections.Scope;

public class ForLoopException extends SemanticActionExecutionException
{


	public ForLoopException(Exception e, ForEachSemanticAction action,
			Scope<Object> semanticActionReturnValueMap)
	{
		super(e,action,semanticActionReturnValueMap);
		if(e instanceof IndexOutOfBoundsException)
		{
			System.out.println("Invalid bounds for FOR LOOP:: start ="+action.getStart()+"\t end = "+action.getEnd());
		}
		else
		{	
			System.out.println(((ForEachSemanticAction)action).getCollection()+" :: is NULL or does not exists");
		}
			stackTrace(semanticActionReturnValueMap);
	}


}
