/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
public @xml_tag("create-search") class CreateSearchSemanticAction extends SemanticAction
{

	@Override
	public String getActionName()
	{
		// TODO Auto-generated method stub
		return SemanticActionStandardMethods.CREATE_SEARCH;
	}

}
