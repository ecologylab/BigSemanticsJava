/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.serialization.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
public @xml_tag(SemanticActionStandardMethods.CREATE_SEARCH) class CreateSearchSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return CREATE_SEARCH;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
