package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_AND_QUEUE_TEXT_SURROGATE) class CreateAndQueueTextSurrogateSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return CREATE_AND_QUEUE_TEXT_SURROGATE;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
