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
public @xml_tag("get_field") class GetFieldSemanticAction extends SemanticAction
{


	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.GET_FIELD_ACTION;
	}

	
}
