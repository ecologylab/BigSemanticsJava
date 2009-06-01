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
public @xml_tag(SemanticActionStandardMethods.SETTER_ACTION) class SetterSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		// TODO Auto-generated method stub
		return SETTER_ACTION;
	}

}
