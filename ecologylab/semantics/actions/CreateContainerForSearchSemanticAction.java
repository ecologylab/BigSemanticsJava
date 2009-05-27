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
public @xml_tag("create_container_for_search")
class CreateContainerForSearchSemanticAction extends CreateContainerSemanticAction
{
	
	Seed seed;

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.CREATE_CONTAINER_FOR_SEARCH;
	}

}
