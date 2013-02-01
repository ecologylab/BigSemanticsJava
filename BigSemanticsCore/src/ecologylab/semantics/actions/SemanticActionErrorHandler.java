/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.generic.Debug;

/**
 * @author amathur
 *
 */
public class SemanticActionErrorHandler  extends Debug implements SemanticActionErrorCodes
{

	/**
	 * Handles the semantic action
	 * @param action
	 */
	public void handleError(SemanticAction action)
	{
		action.handleError();
	}

	public void handleError(SemanticAction action, String errorCode,
			Class<? extends Object> objectClass, String objectName)
	{
		
		// Print Error For NULL Method
		if(NULL_METHOD_ERROR.equals(errorCode))
		{
			println("");
		}
		
	}
	
}
