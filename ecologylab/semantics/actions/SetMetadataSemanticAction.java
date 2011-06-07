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

		throw new RuntimeException("Andruid decided this is no longer part of semantics in tng. Instead, pass a source_document argument to parse_document or create_image_surrogate!");
	}

}
