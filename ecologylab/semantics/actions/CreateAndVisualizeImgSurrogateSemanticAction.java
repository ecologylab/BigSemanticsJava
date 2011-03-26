/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.semantics.connectors.old.InfoCollector;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * This action needs to be implemented by the client.
 * 
 * @author amathur
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE)
class CreateAndVisualizeImgSurrogateSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH>
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE;
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
