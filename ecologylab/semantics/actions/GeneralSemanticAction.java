/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * This is the class of the actions which can be invoked via reflection.
 * 
 * @author amathur
 * 
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.GENERAL_ACTION)
class GeneralSemanticAction extends SemanticAction 
{
	/**
	 * Name of the action which should be invoked via reflection
	 */
	@simpl_scalar
	private String	actionName;

	@Override
	public String getActionName()
	{
		return  actionName;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
