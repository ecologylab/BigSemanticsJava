package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
public @xml_tag(SemanticActionStandardMethods.TRY_SYNC_NESTED_METADATA) class TrySyncNestedMetadata extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return TRY_SYNC_NESTED_METADATA;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
