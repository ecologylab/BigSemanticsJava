package ecologylab.semantics.actions.exceptions;

import ecologylab.collections.Scope;
import ecologylab.semantics.actions.ApplyXPathSemanticAction;
import ecologylab.semantics.html.utils.StringBuilderUtils;

public class ApplyXPathException extends SemanticActionExecutionException
{

	public ApplyXPathException(Exception e, ApplyXPathSemanticAction action,
			Scope<Object> semanticActionReturnValueMap)
	{
		super(action);
		StringBuilder buffy = StringBuilderUtils.acquire();
		buffy.append("Check if the context node is not null::\t").append(action.getNode()).append("\n");
		buffy.append("Check if the XPath Expression is valid::\t").append(action.getXpath()).append("\n");
		buffy.append("Check if the return object type of Xpath evaluation is corect::\t").append(action.getReturnObject()).append("\n");
		String errorMessage = buffy.toString();
		StringBuilderUtils.release(buffy);
		System.out.println(errorMessage);
		stackTrace(semanticActionReturnValueMap);
	}

	

}
