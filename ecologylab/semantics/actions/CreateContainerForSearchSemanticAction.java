/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.semantics.seeding.Seed;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 * 
 */
@xml_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_CONTAINER_FOR_SEARCH)
class CreateContainerForSearchSemanticAction extends CreateContainerSemanticAction
{
	
	Seed seed;

	@Override
	public String getActionName()
	{
		return CREATE_CONTAINER_FOR_SEARCH;
	}

}
