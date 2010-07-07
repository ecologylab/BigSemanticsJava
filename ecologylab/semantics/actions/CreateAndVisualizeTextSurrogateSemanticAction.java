package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_TEXT_SURROGATE) class CreateAndVisualizeTextSurrogateSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return CREATE_AND_VISUALIZE_TEXT_SURROGATE;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
