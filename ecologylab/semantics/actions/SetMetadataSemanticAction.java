/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

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
		// get the object
		ContentElement container = (ContentElement) obj;
		// invoke the actual method
		if (container != null)
			container.setMetadata(metadata);

		return null;
	}

}
