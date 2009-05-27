/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
@xml_inherit
public @xml_tag("set") class SetterSemanticAction extends SemanticAction
{

	@Override
	public String getActionName()
	{
		// TODO Auto-generated method stub
		return SemanticActionStandardMethods.SETTER_ACTION;
	}

}
