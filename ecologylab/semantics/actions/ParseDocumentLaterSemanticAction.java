package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_DOCUMENT_LATER)
class ParseDocumentLaterSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return PARSE_DOCUMENT_LATER;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
