/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * @author amathur
 * 
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.SET_METADATA)
class SetMetadataSemanticAction
		extends SemanticAction
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.SET_METADATA;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		// get the argument
		Metadata metadata = (Metadata) getArgumentObject(SemanticActionNamedArguments.FIELD_VALUE);
		throw new RuntimeException("Andruid did not understand this case, and so did not implement it. Please show him this message!");

//		return null;
	}

}
