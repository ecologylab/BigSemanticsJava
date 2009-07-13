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
public @xml_tag(SemanticActionStandardMethods.GET_FIELD_ACTION) class GetFieldSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{


	@Override
	public String getActionName()
	{
		return GET_FIELD_ACTION;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	
}
