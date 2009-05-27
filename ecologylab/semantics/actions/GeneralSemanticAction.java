/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * This is the class of the actions which can be invoked via reflection.
 * 
 * @author amathur
 * 
 */
@xml_inherit
public @xml_tag("general-action")
class GeneralSemanticAction extends SemanticAction
{
	/**
	 * Name of the action which should be invoked via reflection
	 */
	@xml_attribute
	private String	actionName;

	@Override
	public String getActionName()
	{
		return  actionName;
	}

}
