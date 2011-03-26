package ecologylab.semantics.actions;

import ecologylab.semantics.connectors.old.InfoCollector;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * This action needs to be implemented by the client.
 * 
 * @author quyin
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_TEXT_SURROGATE)
class CreateAndVisualizeTextSurrogateSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH> implements SemanticActionStandardMethods
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

	@Override
	public Object perform(Object obj)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
