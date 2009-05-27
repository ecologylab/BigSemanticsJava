package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * 
 * @author amathur
 *
 */
@xml_inherit
public @xml_tag("create-container") class CreateContainerSemanticAction extends SemanticAction
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.CREATE_CONATINER;
	}

}
