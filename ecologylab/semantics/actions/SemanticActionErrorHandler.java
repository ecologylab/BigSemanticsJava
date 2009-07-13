/**
 * 
 */
package ecologylab.semantics.actions;

/**
 * @author amathur
 *
 */
public class SemanticActionErrorHandler<SA extends SemanticAction>
{

	/**
	 * Handles the semantic action
	 * @param action
	 */
	public void handleError(SA action)
	{
		action.handleError();
	}
	
}
