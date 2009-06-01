/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.ElementState.xml_tag;

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

}
