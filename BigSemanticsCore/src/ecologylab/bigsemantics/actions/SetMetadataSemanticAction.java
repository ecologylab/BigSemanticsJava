/**
 * 
 */
package ecologylab.bigsemantics.actions;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * @author amathur
 * 
 */
@simpl_inherit
public @simpl_tag(SemanticActionStandardMethods.SET_METADATA)
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
